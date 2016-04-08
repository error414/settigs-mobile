package com.lib;


import android.bluetooth.BluetoothDevice;
import android.os.Handler;

abstract public class CommandService {

    private static final String TAG = "CommandService";


    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    protected int mState;

    protected final Handler mHandler;

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public CommandService(Handler handler) {
        mState = STATE_NONE;
        mHandler = handler;
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    public synchronized void connect(BluetoothDevice device) {}

    public synchronized void connect() {}

    public synchronized void stop() {}

    public void write(byte[] out) {}

    public void write(int out) {}

    public void cancel(){}

    /**
     *
     */
    public String getName(){
        return "Unknow";
    }

    /**
     *
     */
    public String getAddress(){
        return "Unknow";
    }

    public BluetoothDevice getBluetoothDevice(){
        return null;

    }

}
