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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.helpers.ByteOperation;
import com.helpers.DstabiProfile.ProfileItem;
import com.spirit.heli.governorthr.governor.GovernorRpmSenzor;

import org.apache.http.util.EncodingUtils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * trida pro praci s protokolem 4dstabi a posilanim pres BT
 *
 * @author error414
 */
public class DstabiProvider {
    private final String TAG = "DstabiProvider";

    private Timer securityTimer;

    private static DstabiProvider instance;

    private Handler connectionHandler;
    private CommandService service;

    final static public String OK = "KK";

    private String retrieveCode = "";

    final protected BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    final static public int MESSAGE_STATE_CHANGE = 1;
    final static public int MESSAGE_READ = 2;
    final static public int MESSAGE_SEND_COMAND_ERROR = 3;
    final static public int MESSAGE_SEND_COMPLETE = 4;

    final static private int PROTOCOL_STATE_NONE = 0;
    final static private int PROTOCOL_STATE_SENDED_VALUES = 3;
    final static private int PROTOCOL_STATE_WAIT_FOR_ALL_DATA = 4;
    final static private int PROTOCOL_STATE_WAIT_FOR_ALL_DATA_DIAGNOSTIC = 5;
    final static private int PROTOCOL_STATE_WAIT_FOR_ALL_DATA_GRAPH = 6;
    final static private int PROTOCOL_STATE_WAIT_FOR_ALL_DATA_GOV_RPM = 7;

    final protected String GET_PROFILE = "G";
    final protected String GET_STICKED_AND_SENZORS_VALUE = "D";
    final protected String GET_GOV_RPM_VALUE = "q";
    final public String SAVE_PROFILE = "g";
    final protected String GET_LOG = "L";
    final protected String SERIAL_NUMBER = "h";
    final protected String GET_GRAPH = "A\1";
    final protected String SET_FAILSAFE = ".";
    final public int DIAGNOSTIC_PROFILE_LENGTH = 17;

    final public String REACTIVATION_BANK = "e";

    private int protocolState = 0;

    private int sendErrorCount = 0;

    private String sendCode;
    private byte[] sendValue;

    private int callBackCode = 0;

    // v jakym modu se provider nachazi, jestli v jednoduchem pozadavku nebo v profilu
    final private int NORMAL = 1;
    final private int PROFILE = 2;
    final private int SERIAL = 3;
    final private int DIAGNOSTIC = 4;
    final private int GRAPH = 5;
    final private int LOG = 6;
    final private int GOV_RPM = 7;
    private int mode = NORMAL;

    private DataBuilder dataBuilder;

    private final Queue queue = new Queue();

    private synchronized void startCecurityTimer() {
        securityTimer = new Timer();
        securityTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

        }, 2000, 2000);
    }

    private synchronized void stopCecurityTimer() {
        if (securityTimer != null) {
            securityTimer.cancel();
            securityTimer.purge();
            securityTimer = null;
        }
    }

    private synchronized void TimerMethod() {
        this.sendError(callBackCode);
        clearState("TimerMethod");
    }

    /**
     * privatni konstruktor na singleton
     */
    private DstabiProvider() {

    }

    /**
     * singleton ziskani instance
     *
     * @param connectionHandler
     * @return
     */
    public synchronized static DstabiProvider getInstance(Handler connectionHandler) {
        if (instance == null) {
            instance = new DstabiProvider();
        }

        instance.connectionHandler = connectionHandler;
        return instance;
    }


    /**
     * zprava pro pripojeni device
     *
     * @param deviceAddress
     */
    public synchronized void connect(String deviceAddress) {

        synchronized (this) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
            this.service = new BluetoothCommandService(serviceBThandler);


            final BluetoothDevice deviceFinal = device;
            final Handler handler = new Handler();
            handler.post(new
                                 Runnable() {
                                     @Override
                                     public void run() {
                                         service.connect(deviceFinal);
                                     }
                                 });
        }
    }

    /**
     * zprava pro pripojeni device
     */
    public synchronized void connect(String ip, String port) {

        synchronized (this) {
            service = new Tcp2CommandService(serviceBThandler);

            final String ip_f = ip;
            final String port_f = port;

            final Handler handler = new Handler();
            handler.post(new
                                 Runnable() {
                                     @Override
                                     public void run() {
                                         service.connect(ip_f, port_f);
                                     }
                                 });
        }
    }

    /**
     * zprava pro odpojeni device
     */
    public synchronized void disconnect() {
        final Handler handler = new Handler();
        handler.post(new
                             Runnable() {
                                 @Override
                                 public void run() {
                                     if (service != null) {
                                         service.stop();
                                     }

                                     service = null;
                                 }
                             });

    }

    /**
     * zisani stavu BT konexe
     *
     * @return
     */
    public int getState() {
        if (service != null) {
            return service.getState();
        }
        return CommandService.STATE_NONE;
    }

    /**
     * ziskani serioveho cisla z jednotky
     *
     * @param callBackCode
     */
    public synchronized void getSerial(int callBackCode) {
        if (DstabiProvider.PROTOCOL_STATE_NONE == protocolState) {
            mode = SERIAL;
            sendDataForResponce(SERIAL_NUMBER, callBackCode);
        } else {
            queue.add(SERIAL_NUMBER, null, SERIAL, callBackCode);
        }
    }

    /**
     * ziskani dat diagnostiky
     *
     * @param callBackCode
     */
    public synchronized void getDiagnostic(int callBackCode) {
        if (DstabiProvider.PROTOCOL_STATE_NONE == protocolState) {
            mode = DIAGNOSTIC;
            sendDataForResponce(GET_STICKED_AND_SENZORS_VALUE, callBackCode);
        } else {
            queue.add(GET_STICKED_AND_SENZORS_VALUE, null, DIAGNOSTIC, callBackCode);
        }
    }

    /**
     * ziskani dat diagnostiky
     *
     * @param callBackCode
     */
    public synchronized void getGovRpm(int callBackCode) {
        if (DstabiProvider.PROTOCOL_STATE_NONE == protocolState) {
            mode = GOV_RPM;
            sendDataForResponce(GET_GOV_RPM_VALUE, callBackCode);
        } else {
            queue.add(GET_GOV_RPM_VALUE, null, GOV_RPM, callBackCode);
        }
    }

    /**
     * ziskani profilu z jednotky
     *
     * @param callBackCode
     */
    public synchronized void getProfile(int callBackCode) {
        if (DstabiProvider.PROTOCOL_STATE_NONE == protocolState) {
            mode = PROFILE;
            sendDataForResponce(GET_PROFILE, callBackCode);
        } else {
            queue.add(GET_PROFILE, null, PROFILE, callBackCode);
        }
    }

    /**
     * ziskani profilu z jednotky
     *
     * @param callBackCode
     */
    public synchronized void getLog(int callBackCode) {
        if (DstabiProvider.PROTOCOL_STATE_NONE == protocolState) {
            mode = LOG;
            sendDataForResponce(GET_LOG, callBackCode);
        } else {
            queue.add(GET_LOG, null, LOG, callBackCode);
        }
    }

    /**
     * ziska informace pro graf z jednotky
     *
     * @param callBack
     */
    public synchronized void getGraph(int callBack) {
        if (DstabiProvider.PROTOCOL_STATE_NONE == protocolState) {
            mode = GRAPH;
            sendDataForResponce(GET_GRAPH, callBack);
        } else {
            queue.add(GET_GRAPH, null, GRAPH, callBack);
        }
    }

    /**
     * nastaveni failsafe
     *
     * @param callBack
     */
    public synchronized void setFailSafe(int callBack) {
        this.sendDataForResponce(SET_FAILSAFE, callBack);
    }

    /**
     * ziska informace pro graf z jednotky
     */
    public synchronized void stopGraph() {
        if (mode == GRAPH) {
            service.write("4DA\0".getBytes());
            clearState("stop graph");
        }
    }

    /**
     * odelsani dat do zarizeni
     *
     * @param command
     * @param data
     */
    private synchronized void sendData(String command, byte[] data) {
        if (DstabiProvider.PROTOCOL_STATE_NONE == protocolState) {
            sendCode = command;
            sendValue = data;
            sendData();
        } else {
            // nejaky pozadavek uz bezi tak ho dame do fronty
            queue.add(command, data, NORMAL, 0);
        }
    }

    /**
     * odelsani dat do zarizeni
     *
     * @param command
     */
    private synchronized void sendData(String command) {
        if (DstabiProvider.PROTOCOL_STATE_NONE == protocolState) {
            sendCode = command;
            sendValue = null;
            sendData();
        } else {
            // nejaky pozadavek uz bezi tak ho dame do fronty
            queue.add(command, null, NORMAL, 0);
        }
    }

    //////////////////WAIT RESPONSE//////////////////
    public synchronized void sendDataForResponce(String command, int callBackCode) {
        if (DstabiProvider.PROTOCOL_STATE_NONE == protocolState) {
            this.callBackCode = callBackCode;
            sendData(command, null);
        } else {
            queue.add(command, null, NORMAL, callBackCode);
        }
    }

    public synchronized void sendDataForResponce(String command, byte[] data, int callBackCode) {
        if (DstabiProvider.PROTOCOL_STATE_NONE == protocolState) {
            this.callBackCode = callBackCode;
            sendData(command, data);
        } else {
            queue.add(command, data, NORMAL, callBackCode);
        }
    }

    public synchronized void sendDataForResponce(ProfileItem item, int callBackCode) {
        if (item.isValid() && item.getCommand() != null) {
            if (DstabiProvider.PROTOCOL_STATE_NONE == protocolState) {
                this.callBackCode = callBackCode;
                sendData(item.getCommand(), item.getValueBytesArray());
            } else {
                queue.add(item.getCommand(), item.getValueBytesArray(), NORMAL, callBackCode);
            }
        } else {
            this.sendError(callBackCode);
        }
    }
    /////////////////////////////////////////////////

    ////////////////// NO RESPONSE//////////////////
    public synchronized void sendDataNoWaitForResponce(String command, byte[] data) {
        sendData(command, data);
    }

    public synchronized void sendDataNoWaitForResponce(String command, int data) {
        sendData(command, ByteOperation.intToByteArray(data));
    }

    public synchronized void sendDataNoWaitForResponce(String command, String data) {
        sendData(command, data.getBytes());
    }

    public synchronized void sendDataNoWaitForResponce(String command) {
        sendData(command);
    }

    public synchronized void sendDataNoWaitForResponce(ProfileItem item) {
        if (item.isValid() && item.getCommand() != null) {
            sendDataNoWaitForResponce(item.getCommand(), item.getValueBytesArray());
        } else {
            this.sendError(callBackCode);
        }
    }
    /////////////////////////////////////////////////

    ////////// NO RESPONSE / DIRECT WRITE ///////////
    public synchronized void sendDataImmediately(byte[] data) {
        service.write(data);
    }
    /////////////////////////////////////////////////

    public synchronized String getDeviceName() {
        if (service != null) {
            return service.getName();
        }

        return "";

    }

    public synchronized String getDeviceAddress() {
        if (service != null) {
            return service.getAddress();
        }

        return "";

    }

    private synchronized void sendData() {
        retrieveCode = "";
        dataBuilder = null;

        startCecurityTimer(); // zapneme casovac
        protocolState = DstabiProvider.PROTOCOL_STATE_SENDED_VALUES;
        service.write("4D".getBytes());
        if (sendCode != null) {
            service.write(ByteOperation.combineByteArray(sendCode.getBytes(), sendValue));
        }
    }

    /**
     * zastaveni probohajiciho pozadavku
     */
    public synchronized void abort() {
        clearState("abort");
    }

    /**
     * zastaveni vsech pozadavku
     */
    public synchronized void abortAll() {
        queue.clear();
        clearState("abort all");
    }

    /**
     * @param who
     */
    private synchronized void clearState(String who) {
        int callBackCodeBuff = callBackCode;
        sendCode = null;
        sendValue = null;
        callBackCode = 0;
        protocolState = DstabiProvider.PROTOCOL_STATE_NONE;
        mode = NORMAL;
        retrieveCode = "";
        stopCecurityTimer(); // vypneme casovac
        dataBuilder = null;
        sendErrorCount = 0;

        synchronized (this) {
            if (queue.hasNextQueue()) {
                if (getState() == CommandService.STATE_CONNECTED) {
                    com.lib.DstabiProvider.Queue.QueueRow tempQueue = queue.getNextQueue();
                    mode = tempQueue.getMode();
                    callBackCode = tempQueue.getCallback();
                    sendData(tempQueue.getCommand(), tempQueue.getData());
                } else {
                    queue.clear();
                    this.sendError(callBackCodeBuff);
                }
            }
        }
    }

    /**
     * handler pro komunikaci s BT servisem
     */
    // The Handler that gets information back from the
    protected final Handler serviceBThandler = new Handler(new Handler.Callback() {
        @Override
        public synchronized boolean handleMessage(Message msg) {
            switch (msg.what) {
                //zmena stavu BT modulu
                case DstabiProvider.MESSAGE_STATE_CHANGE:
                    connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_STATE_CHANGE);
                    clearState("handler 1");
                    //zmena stavu BT modulu
                    break;
                case DstabiProvider.MESSAGE_READ:

                    Bundle b = msg.getData();
                    if (b.containsKey("msg")) {

                        byte[] byteMessage = b.getByteArray("msg");


                        int kCount = mode == DIAGNOSTIC || mode == GOV_RPM ? 1 : 2; //jestli prichazi jedno nebo 2 K

                        byte[] data = parseMessagegetData(byteMessage, kCount);
                        String message = retrieveCode;

                        // neprisli jeste oba potvrzovaci kody
                        if (message.length() < kCount) {
                            return true;
                        }

                        switch (protocolState) {
                            case DstabiProvider.PROTOCOL_STATE_NONE:
                                //prisla sprava ale nic necekame, tak ignorujem
                                break;

                            case DstabiProvider.PROTOCOL_STATE_SENDED_VALUES:
                                //byl odeslan init kod, cekame O nebo K
                                if (message.equals(DstabiProvider.OK) || mode == DIAGNOSTIC || mode == GOV_RPM) { // OK nebo sme v diagnostice
                                    //pokud sem v normal modu tak odelsle zpravu ze sme prijaly potvrzeni K
                                    if (callBackCode == 0 && mode == NORMAL) {
                                        connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_SEND_COMPLETE);
                                        clearState("handler 3 ");

                                        //aktovita meni cislo zada od zaslani callbecu
                                    } else if (callBackCode != 0 && mode == NORMAL) {
                                        connectionHandler.sendEmptyMessage(callBackCode);
                                        clearState("handler 4");

                                        //PROFILE a LOG pokud zada aktivita o profil tak podle delky privniho bytu cekame na cely profil
                                    } else if (mode == PROFILE || mode == LOG) {

                                        //zmenime state protokokolu na pripadne cekani na konec profilu
                                        protocolState = PROTOCOL_STATE_WAIT_FOR_ALL_DATA;
                                        dataBuilder = new DataBuilder();
                                        dataBuilder.setLengthCorrection(mode == LOG ? 2 : 1);// log ma na zacatku delku a informaci jestli je zaznam z predchoziho letu, a profil ma na zacatku delku profilu
                                        dataBuilder.setLengthMultiCorrection(mode == LOG ? 2 : 1);
                                        dataBuilder.add(data);

                                        // profil je cely odesilame zpravu s profilem, pokud neni cely zachytava to
                                        // case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA: kde se dal ceka na dalsi data
                                        if (dataBuilder.itsAll()) {
                                            sendHandle(callBackCode, dataBuilder.getData());
                                        }

                                    } else if (mode == SERIAL) {
                                        //zmenime state protokokolu na pripadne cekani na konec profilu
                                        protocolState = PROTOCOL_STATE_WAIT_FOR_ALL_DATA;
                                        dataBuilder = new DataBuilder(6); // serial je dlouhe 6 bytu
                                        dataBuilder.add(data);

                                        // profil je cely odesilame zpravu s profilem, poud neni cely zachytava to
                                        // case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA: kde se dal ceka na dalsi data
                                        if (dataBuilder.itsAll()) {
                                            sendHandle(callBackCode, dataBuilder.getData());
                                        }
                                    } else if (mode == DIAGNOSTIC) {
                                        //zmenime state protokokolu na pripadne cekani na konec profilu
                                        protocolState = PROTOCOL_STATE_WAIT_FOR_ALL_DATA_DIAGNOSTIC;
                                        dataBuilder = new DataBuilder(DIAGNOSTIC_PROFILE_LENGTH); // diagnostika je dlouhe 17 bytu
                                        dataBuilder.add(data);

                                        // profil je cely odesilame zpravu s profilem, poud neni cely zachytava to
                                        // case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA: kde se dal ceka na dalsi data
                                        if (dataBuilder.itsAll()) {
                                            sendHandle(callBackCode, dataBuilder.getData());
                                        }
                                    } else if (mode == GOV_RPM) {
                                        protocolState = PROTOCOL_STATE_WAIT_FOR_ALL_DATA_GOV_RPM;
                                        dataBuilder = new DataBuilder(GovernorRpmSenzor.RPM_SENZOR_LENGTH);
                                        dataBuilder.add(data);

                                        if (dataBuilder.itsAll()) {
                                            sendHandle(callBackCode, dataBuilder.getData());
                                        }

                                    } else if (mode == GRAPH) {

                                        stopCecurityTimer();
                                        startCecurityTimer();
                                        //zmenime state protokokolu na pripadne cekani na konec profilu
                                        protocolState = PROTOCOL_STATE_WAIT_FOR_ALL_DATA_GRAPH;
                                        dataBuilder = new DataBuilder();
                                        dataBuilder.add(byteMessage);

                                        sendHandleNotStop(callBackCode, dataBuilder.getData());
                                        dataBuilder.clear();
                                    }
                                } else { // ERROR
                                    //pokud selhalo odeslani pokusime se to odelsat znova
                                    if (sendErrorCount == 0) {
                                        sendErrorCount++;
                                        Log.w(TAG, "posilam pozadavek znovu");
                                        stopCecurityTimer();
                                        sendData(); // again send data
                                    } else {
                                        sendError(callBackCode);
                                        Log.w(TAG, "druhy pokus selhal");
                                        abortAll();
                                        clearState("handler 5");
                                    }
                                }
                                break;

                            //cekameme na dalsi data z profilu nebo ze serioveho cisla
                            case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA:

                                // prijmame dalsi casti z diagnostiky, musime pouzit vlastni switch protoze diagnistika nepouziva promenou data ale byteMessage
                            case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA_DIAGNOSTIC:

                            case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA_GOV_RPM:
                                dataBuilder.add(byteMessage);  // pouzijeme celou zpravu co nam prisla

                                if (dataBuilder.itsAll()) {
                                    sendHandle(callBackCode, dataBuilder.getData());
                                }
                                break;

                            // prijmame dalsi streamu pro graf
                            case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA_GRAPH:
                                dataBuilder.add(byteMessage);

                                sendHandleNotStop(callBackCode, dataBuilder.getData());
                                dataBuilder.clear();

                                stopCecurityTimer();
                                startCecurityTimer();

                                break;
                        }

                    }
            }

            return true;

        }
    });

    private synchronized void sendHandle(int callBackCode, byte[] data) {
        Bundle budleForMsg = new Bundle();
        budleForMsg.putByteArray("data", data);
        Message m = connectionHandler.obtainMessage(callBackCode);
        m.setData(budleForMsg);
        connectionHandler.sendMessage(m);
        clearState("send handle");
    }

    private synchronized void sendHandleNotStop(int callBackCode, byte[] data) {
        Bundle budleForMsg = new Bundle();
        budleForMsg.putByteArray("data", data);
        Message m = connectionHandler.obtainMessage(callBackCode);
        m.setData(budleForMsg);
        connectionHandler.sendMessage(m);
        //Log.d(TAG, "zprava poslana not stop: " + callBackCode);
    }

    private synchronized void sendError(int callBackCode) {
        Bundle budleForMsg = new Bundle();
        budleForMsg.putInt("callBack", callBackCode);
        Message m = connectionHandler.obtainMessage(MESSAGE_SEND_COMAND_ERROR);
        m.setData(budleForMsg);
        connectionHandler.sendMessage(m);
    }

    private synchronized void sendError() {
        this.sendError(0);
    }

    /**
     * ziskani stavoveho kodu ze zpravy
     */
    private synchronized byte[] parseMessagegetData(byte[] msg, int kCouunt) {
        if (retrieveCode.length() == kCouunt) {
            return msg;
        }

        for (int i = 0; i < msg.length; i++) {
            if (retrieveCode.length() < kCouunt) {
                retrieveCode = retrieveCode + EncodingUtils.getAsciiString(msg, i, 1);
            } else {
                byte[] result = new byte[msg.length - i];
                System.arraycopy(msg, i, result, 0, msg.length - i);
                return result;
            }
        }


        return new byte[0];
    }

    /**
     * PROFILE BUILDER
     */

    private class DataBuilder {

        private byte[] profile = null;
        private int length = 0;
        private int lengthCorrection = 0;
        private int lengthMultiCorrection = 1;


        public DataBuilder(int length) {
            this.length = length;
        }

        public DataBuilder() {
        }

        /**
         * prodani casti zpravy do pole bytu
         *
         * @param part
         */
        public synchronized void add(byte[] part) {
            if (profile == null || profile.length == 0) { // prvni cast
                if (part != null && part.length != 0) {
                    if (length == 0) {
                        length = (ByteOperation.byteToUnsignedInt(part[0]) * lengthMultiCorrection) + lengthCorrection;
                    }
                    profile = part;
                }
            } else {
                profile = ByteOperation.combineByteArray(profile, part);
            }
        }

        /**
         * vycisteni zasobniku
         */
        public synchronized void clear() {
            length = 0;
            profile = null;
        }

        /**
         * je profil vetsi nez pocet dat ktere chceme
         *
         * @return
         */
        public Boolean itsAll() {
            if (profile != null) {
                return (length <= profile.length && length != 0);
            }
            return false;
        }

        /**
         * vrati data
         *
         * @return
         */
        public synchronized byte[] getData() {
            return profile;
        }

        /**
         * @param lengthCorrection
         */
        public void setLengthCorrection(int lengthCorrection) {
            this.lengthCorrection = lengthCorrection;
        }

        public void setLengthMultiCorrection(int lengthMultiCorrection) {
            this.lengthMultiCorrection = lengthMultiCorrection;
        }
    }

    /**
     * trida fronty
     *
     * @author error414
     */
    private class Queue {

        private ArrayList<QueueRow> queueRow = new ArrayList<QueueRow>();

        /**
         * pridani pozadavku do fronty
         *
         * @param command
         * @param data
         */
        public synchronized void add(String command, byte[] data, int mode, int callback) {
            queueRow.add(new QueueRow(command, data, mode, callback));
        }

        public synchronized int count() {
            return queueRow.size();
        }

        public synchronized Boolean hasNextQueue() {
            return this.count() > 0;
        }

        public synchronized QueueRow getNextQueue() {
            if (hasNextQueue()) {
                QueueRow temObjectQueueRow = queueRow.get(0);
                queueRow.remove(0);
                return temObjectQueueRow;
            }
            return null;
        }

        public synchronized void clear() {
            queueRow = new ArrayList<QueueRow>();
        }


        /**
         * trida radku fronty
         *
         * @author error414
         */
        private class QueueRow {
            private String command;
            private int mode;
            private int callback;
            private byte[] data;


            public QueueRow(String command, byte[] data, int mode, int callback) {
                this.command = command;
                this.data = data;
                this.mode = mode;
                this.callback = callback;
            }


            /**
             * getter
             *
             * @return
             */
            public String getCommand() {
                return command;
            }

            /**
             * getter
             *
             * @return
             */
            public byte[] getData() {
                return data;
            }

            /**
             * getter
             *
             * @return
             */
            public int getMode() {
                return mode;
            }


            /**
             * getter
             *
             * @return
             */
            public int getCallback() {
                return callback;
            }


        }

    }

}
