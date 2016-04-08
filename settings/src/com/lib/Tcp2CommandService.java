/*
Copyright (C) Petr Cada and Tomas Jedrzejek
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package com.lib;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Tcp2CommandService extends CommandService {
    // Debugging
    private static final String TAG = "Tcp2CommandService";
    private static final boolean D = true;

    public static final String SERVER_IP = "192.168.4.1"; //your computer IP address
    public static final int SERVER_PORT = 23;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    protected boolean mRun = false;
//    private BluetoothDevice mSavedDevice;
//    private int mConnectionLostCount;

    public String getName() {
        return "WIFI";
    }

    /**
     *
     */
    public String getAddress() {
        return SERVER_IP;
    }

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param handler A Handler to send messages back to the UI Activity
     */
    public Tcp2CommandService(Handler handler) {
        super(handler);
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(DstabiProvider.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     */
    public synchronized void connect() {
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread();
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     */
    public synchronized void connected(Socket socket) {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        //Message msg = mHandler.obtainMessage(RemoteBluetooth.MESSAGE_DEVICE_NAME);
        //Bundle bundle = new Bundle();
        //bundle.putString(RemoteBluetooth.DEVICE_NAME, device.getName());
        //msg.setData(bundle);
        //mHandler.sendMessage(msg);

        // save connected device
        //mSavedDevice = device;
        // reset connection lost count
        //mConnectionLostCount = 0;

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        mRun = false;
        if (D) Log.d(TAG, "stop");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        mConnectedThread.write(out);
    }

    public void write(int out) {
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        mConnectedThread.write(out);
    }

    public void cancel() {
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        mConnectedThread.cancel();
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        ////  Message msg = mHandler.obtainMessage(RemoteBluetooth.MESSAGE_TOAST);
        ////  Bundle bundle = new Bundle();
        ////  bundle.putString(RemoteBluetooth.TOAST, "Unable to connect device");
        ////   msg.setData(bundle);
        ////    mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
//        mConnectionLostCount++;
//        if (mConnectionLostCount < 3) {
//        	// Send a reconnect message back to the Activity
//	        Message msg = mHandler.obtainMessage(RemoteBluetooth.MESSAGE_TOAST);
//	        Bundle bundle = new Bundle();
//	        bundle.putString(RemoteBluetooth.TOAST, "Device connection was lost. Reconnecting...");
//	        msg.setData(bundle);
//	        mHandler.sendMessage(msg);
//	        
//        	connect(mSavedDevice);   	
//        } else {
        setState(STATE_NONE);
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            //mConnectedThread.cancel();
            //mConnectedThread = null;
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private Socket mmSocket;
        private InetAddress serverAddr;

        public ConnectThread() {
            try {
                serverAddr = InetAddress.getByName(SERVER_IP);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
        }

        public void run() {
            setName("ConnectThread");

            try {
                mmSocket = new Socket();
                mmSocket.connect(new InetSocketAddress(serverAddr, SERVER_PORT), 1000);
                Log.i(TAG, "BEGIN mConnectThread2");
                // Reset the ConnectThread because we're done
                synchronized (Tcp2CommandService.this) {
                    mConnectThread = null;
                }

                connected(mmSocket);
                return;
            } catch (Exception e) {
                Log.e(TAG, "connect Exception", e);
            }

            Log.e(TAG, "connection failed");
            connectionFailed();
            try {
                if(mmSocket != null){
                    mmSocket.close();
                }

            } catch (IOException e2) {
                Log.e(TAG, "unable to close() socket during connection failure", e2);
            }
            return;
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private Socket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(Socket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpOut = socket.getOutputStream();
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            mRun = true;
            byte[] buffer = new byte[1024];
            // Keep listening to the InputStream while connected
            long currentTime = 0;
            ByteArrayBuffer message = new ByteArrayBuffer(1024);
            while (mRun) {
                try {
                    // Read from the InputStream
                    if(mmInStream.available() > 0 && currentTime == 0){
                        currentTime = System.currentTimeMillis();
                    }

                    if(mmInStream.available() > 100 || (System.currentTimeMillis() - currentTime > 100 && mmInStream.available() > 0)) {
                        int bytes = mmInStream.read(buffer);
                        // Send the obtained bytes to the UI Activity
                        if (bytes > 0) {

                            message.append(buffer, 0, bytes);

                            Bundle b = new Bundle();
                            b.putByteArray("msg", message.toByteArray());

                            Message m = mHandler.obtainMessage(DstabiProvider.MESSAGE_READ);
                            m.setData(b);

                            message.clear();
                            mHandler.sendMessage(m);

                            currentTime = 0;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    connectionLost();
                    break;
                }
            }

            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                mmOutStream.flush();
            } catch (Exception e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void write(int out) {
            try {
                mmOutStream.write(out);
                mmOutStream.flush();
                // Share the sent message back to the UI Activity
//                mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
//                        .sendToTarget();
            } catch (Exception e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmInStream.close();
                mmOutStream.close();
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
