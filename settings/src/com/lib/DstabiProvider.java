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

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.helpers.ByteOperation;
import com.helpers.DstabiProfile.ProfileItem;
import com.spirit.governor.GovernorRpmSenzor;

import org.apache.http.util.EncodingUtils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * trida pro praci s protokolem 4dstabi a posilanim pres BT
 * 
 * @author error414
 *
 */
public class DstabiProvider {
	private final String TAG = "DstabiProvider";
	
	private Timer securityTimer;
	
	private static DstabiProvider instance;
	
	private Handler connectionHandler;
	private BluetoothCommandService BTservice;
	
	final static public String OK 		= "KK";

    private String retrieveCode = "";

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
    final public int DIAGNOSTIC_PROFILE_LENGTH = 17;

    final public String REACTIVATION_BANK = "e";

	private int protocolState = 0;

    private int sendErrorCount = 0;

	private String sendCode;
	private byte[] sendValue;
	
	private int callBackCode = 0;
	
	// v jakym modu se provider nachazi, jestli v jednoduchem pozadavku nebo v profilu
	final private int NORMAL 		= 1;
	final private int PROFILE 		= 2;
	final private int SERIAL 		= 3;
	final private int DIAGNOSTIC 	= 4;
	final private int GRAPH 		= 5;
	final private int LOG 			= 6;
    final private int GOV_RPM 	    = 7;
	private int mode = NORMAL;
	
	private DataBuilder dataBuilder;
	
	private final Queue queue = new Queue();
	
	private synchronized void  startCecurityTimer(){
		Log.d(TAG, "zapinam timer");
		securityTimer = new Timer();
		securityTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				TimerMethod();
			}

		}, 7000, 7000);
	}
	
	private synchronized void stopCecurityTimer(){
		if(securityTimer != null){
			securityTimer.cancel();
			securityTimer.purge();
			securityTimer = null;
		}
	}
	
	private synchronized void TimerMethod()
	{
		connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_SEND_COMAND_ERROR);
		clearState("TimerMethod");
	}
	
	/**
	 * privatni konstruktor na singleton
	 */
	private DstabiProvider() {
		synchronized (this) {
            this.BTservice = new BluetoothCommandService(serviceBThandler);
        }
	}
	
	/**
	 * singleton ziskani instance
	 * 
	 * @param connectionHandler
	 * @return
	 */
	public synchronized static DstabiProvider getInstance(Handler connectionHandler){
		if(instance == null){
			instance = new DstabiProvider();
		}
		
		instance.connectionHandler = connectionHandler;
		return instance;
	}
	
	/**
	 * zprava pro pripojeni device
	 * 
	 * @param device
	 */
	public synchronized void connect(BluetoothDevice device) {
		BTservice.connect(device);
	}
	
	/**
	 * zprava pro odpojeni device
	 * 
	 */
	public synchronized void disconnect() {
		BTservice.stop();
        BTservice.cancel();
	}
	
	/**
	 * zisani stavu BT konexe
	 * 
	 * @return
	 */
	public int getState(){
		return  BTservice.getState();
	}
	
	/**
	 * ziskani serioveho cisla z jednotky
	 * 
	 * @param callBackCode
	 */
	public synchronized void getSerial(int callBackCode){
		Log.d(TAG, "pozadavek na serial");
		if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
			
			Log.d(TAG, "pozadavek na serial je ihned odbaven");
			
			mode = SERIAL;
			sendDataForResponce(SERIAL_NUMBER, callBackCode);
		}else{
			
			Log.d(TAG, "pozadavek na serial je pridan do fronty");
			queue.add(SERIAL_NUMBER, null, SERIAL, callBackCode);
		}
	}
	
	/**
	 * ziskani dat diagnostiky
	 * 
	 * @param callBackCode
	 */
	public synchronized void getDiagnostic(int callBackCode){
		if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
			mode = DIAGNOSTIC;
			sendDataForResponce(GET_STICKED_AND_SENZORS_VALUE, callBackCode);
		}else{
			queue.add(GET_STICKED_AND_SENZORS_VALUE, null, DIAGNOSTIC, callBackCode);
		}
	}

    /**
     * ziskani dat diagnostiky
     *
     * @param callBackCode
     */
    public synchronized void getGovRpm(int callBackCode){
        if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
            mode = GOV_RPM;
            sendDataForResponce(GET_GOV_RPM_VALUE, callBackCode);
        }else{
            queue.add(GET_GOV_RPM_VALUE, null, GOV_RPM, callBackCode);
        }
    }
	
	/**
	 * ziskani profilu z jednotky
	 * 
	 * @param callBackCode
	 */
	public synchronized void getProfile(int callBackCode){
		Log.d(TAG, "pozadavek na profil");
		
		if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
			Log.d(TAG, "pozadavek na profil je ihned odbaven");
			
			mode = PROFILE;
			sendDataForResponce(GET_PROFILE, callBackCode);
		}else{
			
			Log.d(TAG, "pozadavek na profil je pridan do fronty");
			queue.add(GET_PROFILE, null, PROFILE, callBackCode);
		}
	}
	
	/**
	 * ziskani profilu z jednotky
	 * 
	 * @param callBackCode
	 */
	public synchronized void getLog(int callBackCode){
		Log.d(TAG, "pozadavek na profil");
		
		if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
			Log.d(TAG, "pozadavek na log je ihned odbaven");
			
			mode = LOG;
			sendDataForResponce(GET_LOG, callBackCode);
		}else{
			
			Log.d(TAG, "pozadavek na log je pridan do fronty");
			queue.add(GET_LOG, null, LOG, callBackCode);
		}
	}
	
	/**
	 * ziska informace pro graf z jednotky
	 * 
	 * @param callBack
	 */
	public synchronized void getGraph(int callBack){
		Log.d(TAG, "pozadavek na stream");
		
		if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
			mode = GRAPH;
			sendDataForResponce(GET_GRAPH, callBack);
		}else{
			queue.add(GET_GRAPH, null, GRAPH, callBack);
		}
	}
	
	/**
	 * ziska informace pro graf z jednotky
	 * 
	 */
	public synchronized void stopGraph(){
		if(mode == GRAPH){
			BTservice.write("4DA\0".getBytes());
			clearState("stop graph");
		}
	}
	
	/**
	 * odelsani dat do zarizeni
	 * 
	 * @param command
	 * @param data
	 */
	private synchronized void sendData(String command, byte[] data){
		
		Log.d(TAG, "pozadavek na odeslani pozadavku do zarizeni cmd+data: " + command + ":" + ByteOperation.getHexStringByByteArray(data));

		if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
			Log.d(TAG, "pozadavek na odeslani pozadavku do zarizeni je ihned odbaven cmd+data");
			sendCode = command;
			sendValue = data;
			sendData();
		}else{
			// nejaky pozadavek uz bezi tak ho dame do fronty
			Log.d(TAG, "pozadavek na odeslani pozadavku do zarizeni je pridan do fronty cmd+data");
			queue.add(command, data, NORMAL, 0);
		}
	}
	
	/**
	 * odelsani dat do zarizeni
	 * 
	 * @param command
	 */
	private synchronized void sendData(String command){
		Log.d(TAG, "pozadavek na odeslani pozadavku do zarizeni cmd");
		
		if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
			
			Log.d(TAG, "pozadavek na odeslani pozadavku do zarizeni je ihned odbaven cmd");
			
			sendCode = command;
			sendValue = null;
			sendData();
		}else{
			// nejaky pozadavek uz bezi tak ho dame do fronty
			
			Log.d(TAG, "pozadavek na odeslani pozadavku do zarizeni je pridan do fronty cmd");
			
			queue.add(command, null, NORMAL, 0);
		}
	}
	
	//////////////////WAIT RESPONSE//////////////////
	public synchronized void sendDataForResponce(String command, int callBackCode){
		if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
			this.callBackCode = callBackCode;
			sendData(command, null);
		}else{
			queue.add(command, null, NORMAL, callBackCode);
		}
	}

	public synchronized void sendDataForResponce(String command, byte[] data, int callBackCode){
		if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
			this.callBackCode = callBackCode;
			sendData(command, data);
		}else{
			queue.add(command, data, NORMAL, callBackCode);
		}
	}
	
	public synchronized void sendDataForResponce(ProfileItem item, int callBackCode){
		if(item.isValid() && item.getCommand() != null){
			if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
				this.callBackCode = callBackCode;
				sendData(item.getCommand(), item.getValueBytesArray());
			}else{
				queue.add(item.getCommand(), item.getValueBytesArray(), NORMAL, callBackCode);
			}
		}else{
			connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_SEND_COMAND_ERROR);
		}
	}
	/////////////////////////////////////////////////
	
	////////////////// NO RESPONSE//////////////////
	public synchronized void sendDataNoWaitForResponce(String command, byte[] data){
		sendData(command, data);
	}
	
	public synchronized void sendDataNoWaitForResponce(String command, int data){
		sendData(command, ByteOperation.intToByteArray(data));
	}
	
	public synchronized void sendDataNoWaitForResponce(String command, String data){
		sendData(command, data.getBytes());
	}
	
	public synchronized void sendDataNoWaitForResponce(String command){
		sendData(command);
	}
	
	public synchronized void sendDataNoWaitForResponce(ProfileItem item){
		if(item.isValid() && item.getCommand() != null){
			sendDataNoWaitForResponce(item.getCommand(), item.getValueBytesArray());
		}else{
			connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_SEND_COMAND_ERROR);
		}
	}
	/////////////////////////////////////////////////
	
	////////// NO RESPONSE / DIRECT WRITE ///////////
	public synchronized void sendDataImmediately(byte[] data){
		BTservice.write(data);
	}
	/////////////////////////////////////////////////
	
	public synchronized BluetoothDevice getBluetoothDevice(){
		return BTservice.getBluetoothDevice();
	}
	
	private synchronized void sendData(){
		
		Log.d(TAG, "odesilam data do zarizeni:" + sendCode );

        retrieveCode = "";
        dataBuilder = null;

        startCecurityTimer(); // zapneme casovac
        protocolState = DstabiProvider.PROTOCOL_STATE_SENDED_VALUES;
        BTservice.write("4D".getBytes());
        if(sendCode != null){
            BTservice.write(ByteOperation.combineByteArray(sendCode.getBytes(),  sendValue));
        }
	}
	
	/**
	 * zastaveni probohajiciho pozadavku
	 */
	public synchronized void abort(){
		clearState("abort");
	}
	
	/**
	 * zastaveni vsech pozadavku
	 */
	public synchronized void abortAll(){
		queue.clear();
		clearState("abort all");
	}

    /**
     *
     * @param who
     */
	private synchronized void clearState(String who){
		
		Log.d(TAG, "mazu stav:" + who);
        sendCode 		= null;
        sendValue 		= null;
        callBackCode	= 0;
        protocolState 	= DstabiProvider.PROTOCOL_STATE_NONE;
        mode = NORMAL;
        retrieveCode = "";
        Log.d(TAG, "vypinam timer");
        stopCecurityTimer(); // vypneme casovac
        dataBuilder = null;
        sendErrorCount = 0;

        synchronized (this) {
            if (queue.hasNextQueue()) {
                Log.d(TAG, "ve fronte je dalsi pozadavek odbavuji");
                if (getState() == BluetoothCommandService.STATE_CONNECTED) {
                    com.lib.DstabiProvider.Queue.QueueRow tempQueue = queue.getNextQueue();
                    mode = tempQueue.getMode();
                    callBackCode = tempQueue.getCallback();
                    sendData(tempQueue.getCommand(), tempQueue.getData());
                } else {
                    queue.clear();
                    connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_SEND_COMAND_ERROR);
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
        	switch(msg.what){
        	 	//zmena stavu BT modulu
        		case DstabiProvider.MESSAGE_STATE_CHANGE: 
        			connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_STATE_CHANGE);
        			clearState("handler 1");
        			//zmena stavu BT modulu
                    break;
        		case DstabiProvider.MESSAGE_READ:
        			
        			Bundle b = msg.getData();
        			if(b.containsKey("msg")){
        				
        				byte[] byteMessage = b.getByteArray("msg");
        				
        				Log.d(TAG, "prijmam data");
        				
                        int kCount = mode == DIAGNOSTIC || mode == GOV_RPM ? 1 : 2; //jestli prichazi jedno nebo 2 K

        				byte[] data 			= parseMessagegetData(byteMessage, kCount);
                        String message 			= retrieveCode;

                        // neprisli jeste oba potvrzovaci kody
                        if(message.length() < kCount){
                            return true;
                        }

        				switch(protocolState){
        					case DstabiProvider.PROTOCOL_STATE_NONE:
        						//prisla sprava ale nic necekame, tak ignorujem
        						break;
        					
        					case DstabiProvider.PROTOCOL_STATE_SENDED_VALUES:
        						Log.d(TAG, "prijmana data byla odpoved na prikaz: " + message);
	    						//byl odeslan init kod, cekame O nebo K
	    						if(message.equals(DstabiProvider.OK) || mode == DIAGNOSTIC || mode == GOV_RPM){ // OK nebo sme v diagnostice
	    							
	    							Log.d(TAG, "prijmana data byla odpoved na prikaz OK");
	    							Log.d(TAG, "jsme v modu :" + mode);
	    							//pokud sem v normal modu tak odelsle zpravu ze sme prijaly potvrzeni K
	    							if(callBackCode == 0 && mode == NORMAL){
	    								connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_SEND_COMPLETE);
	    								clearState("handler 3 ");
	    							
	    							//aktovita meni cislo zada od zaslani callbecu
	    							}else if(callBackCode != 0 && mode == NORMAL){ 
	    								connectionHandler.sendEmptyMessage(callBackCode);
	    								clearState("handler 4");
	    								
	    							//PROFILE a LOG pokud zada aktivita o profil tak podle delky privniho bytu cekame na cely profil
	    							}else if(mode == PROFILE || mode == LOG){
	    								
	    								Log.d(TAG, "Prislo 1 :" + ByteOperation.getIntegerStringByByteArray(data));
	    								
	    								//zmenime state protokokolu na pripadne cekani na konec profilu
	    								protocolState = PROTOCOL_STATE_WAIT_FOR_ALL_DATA;
	    								dataBuilder = new DataBuilder();
                                        dataBuilder.setLengthCorrection(mode == LOG ? 2 : 1);// log ma na zacatku delku a informaci jestli je zaznam z predchoziho letu, a profil ma na zacatku delku profilu
	    								dataBuilder.add(data);
	    								
	    								// profil je cely odesilame zpravu s profilem, pokud neni cely zachytava to
	    								// case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA: kde se dal ceka na dalsi data
	    								if(dataBuilder.itsAll()){
	    									sendHandle(callBackCode, dataBuilder.getData());
	    								}
	    								
	    							}else if(mode == SERIAL){

	    								Log.d(TAG, "Prislo serial :" + ByteOperation.getIntegerStringByByteArray(data));
	    								
	    								//zmenime state protokokolu na pripadne cekani na konec profilu
	    								protocolState = PROTOCOL_STATE_WAIT_FOR_ALL_DATA;
	    								dataBuilder = new DataBuilder(6); // serial je dlouhe 6 bytu
	    								dataBuilder.add(data);
	    								
	    								// profil je cely odesilame zpravu s profilem, poud neni cely zachytava to
	    								// case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA: kde se dal ceka na dalsi data
	    								if(dataBuilder.itsAll()){
	    									sendHandle(callBackCode, dataBuilder.getData());
	    								}
	    							}else if(mode == DIAGNOSTIC){

	    								Log.d(TAG, "Prisla diagnosticka :" + ByteOperation.getIntegerStringByByteArray(data));
	    								
	    								//zmenime state protokokolu na pripadne cekani na konec profilu
	    								protocolState = PROTOCOL_STATE_WAIT_FOR_ALL_DATA_DIAGNOSTIC;
	    								dataBuilder = new DataBuilder(DIAGNOSTIC_PROFILE_LENGTH); // diagnostika je dlouhe 17 bytu
	    								dataBuilder.add(data);
	    								
	    								// profil je cely odesilame zpravu s profilem, poud neni cely zachytava to
	    								// case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA: kde se dal ceka na dalsi data
	    								if(dataBuilder.itsAll()){
	    									sendHandle(callBackCode, dataBuilder.getData());
	    								}
                                    }else if(mode == GOV_RPM){

                                        Log.d(TAG, "Prislo GOV RPM :" + ByteOperation.getIntegerStringByByteArray(data));

                                        protocolState = PROTOCOL_STATE_WAIT_FOR_ALL_DATA_GOV_RPM;
                                        dataBuilder = new DataBuilder(GovernorRpmSenzor.RPM_SENZOR_LENGTH);
                                        dataBuilder.add(data);

                                        if(dataBuilder.itsAll()){
                                            sendHandle(callBackCode, dataBuilder.getData());
                                        }

	    							}else if(mode == GRAPH){
	    								
	    								stopCecurityTimer();
	    								//zmenime state protokokolu na pripadne cekani na konec profilu
	    								protocolState = PROTOCOL_STATE_WAIT_FOR_ALL_DATA_GRAPH;
	    								dataBuilder = new DataBuilder();
	    								dataBuilder.add(byteMessage);
	    								
	    								sendHandleNotStop(callBackCode, dataBuilder.getData());
	    								dataBuilder.clear();
	    							}
	    						 }else{ // ERROR
                                    //pokud selhalo odeslani pokusime se to odelsat znova
                                    if(sendErrorCount == 0){
                                        sendErrorCount++;
                                        Log.w(TAG, "posilam pozadavek znovu");
                                        stopCecurityTimer();
                                        sendData(); // again send data
                                    }else {
                                        connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_SEND_COMAND_ERROR);
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

                                if(dataBuilder.itsAll()) {
                                    sendHandle(callBackCode, dataBuilder.getData());
                                }
                                break;
        						
        					// prijmame dalsi streamu pro graf
        					case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA_GRAPH:
        						dataBuilder.add(byteMessage);  
        					
        						sendHandleNotStop(callBackCode, dataBuilder.getData());
        						dataBuilder.clear();
        						
        						break;
        				}
        					
        			}
        	}
        	
        	return true;
        	
        }
    });
    
    private synchronized void sendHandle(int callBackCode, byte[] data){
    	Bundle budleForMsg = new Bundle();
		budleForMsg.putByteArray("data", data);
        Message m = connectionHandler.obtainMessage( callBackCode );
        m.setData(budleForMsg);
        connectionHandler.sendMessage(m);
        Log.d(TAG, "zprava poslana");
        clearState("send handle");
    }
    
    private synchronized void sendHandleNotStop(int callBackCode, byte[] data){
    	Bundle budleForMsg = new Bundle();
		budleForMsg.putByteArray("data", data);
        Message m = connectionHandler.obtainMessage( callBackCode );
        m.setData(budleForMsg);
        connectionHandler.sendMessage(m);
        //Log.d(TAG, "zprava poslana not stop: " + callBackCode);
    }
    
	/**
	 * ziskani stavoveho kodu ze zpravy
	 * 
	 */
    private synchronized byte[] parseMessagegetData(byte[] msg, int kCouunt){
        if(retrieveCode.length() == kCouunt){
            return msg;
        }

        for(int i = 0; i < msg.length; i++)
        {
            if(retrieveCode.length() < kCouunt) {
                retrieveCode = retrieveCode + EncodingUtils.getAsciiString(msg, i, 1);
            }else{
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
    
    private class DataBuilder
    {
    	
    	private byte[] profile = null;
    	private int length = 0;
        private int lengthCorrection = 0;
    	
    	
		public DataBuilder(int length)
    	{
    		this.length = length;
    	}
    	
    	public DataBuilder()
    	{
    	}
    	
    	/**
    	 * prodani casti zpravy do pole bytu
    	 * 
    	 * @param part
    	 */
    	public synchronized void add(byte[] part)
    	{
    		if(profile == null || profile.length == 0){ // prvni cast 
    			if(part != null && part.length != 0){
    				if(length == 0){
    					length = ByteOperation.byteToUnsignedInt(part[0]) + lengthCorrection;
    				}
    				profile = part;
    			}
    		}else{
    			profile = ByteOperation.combineByteArray(profile, part);
    		}
    	}
    	
    	/**
    	 * vycisteni zasobniku 
    	 */
    	public synchronized void clear()
    	{
    		length = 0;
    		profile = null;
    	}
    	
    	/**
    	 * je profil vetsi nez pocet dat ktere chceme
    	 * 
    	 * @return
    	 */
    	public Boolean itsAll()
    	{
    		if(profile != null){
    			Log.d(TAG, "its all length: " +  length);
    			Log.d(TAG, "its all profile.length: " +  profile.length);
    			return (length <= profile.length && length != 0);
    		}
    		return false;
    	}
    	
    	/**
    	 * vrati data
    	 * 
    	 * @return
    	 */
    	public synchronized byte[] getData()
    	{
    		return profile;
    	}

        /**
         *
         * @param lengthCorrection
         */
        public void setLengthCorrection(int lengthCorrection) {
            this.lengthCorrection = lengthCorrection;
        }
    }
    
    /**
     * 
     * trida fronty
     * @author error414
     *
     */
    private class Queue
    {
    	
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
    	
    	public synchronized int count()
    	{
    		return queueRow.size();
    	}
    	
    	public synchronized Boolean hasNextQueue()
    	{
    		return this.count() > 0;
    	}
    	
    	public synchronized QueueRow getNextQueue()
    	{
    		if(hasNextQueue()){
    			QueueRow temObjectQueueRow = queueRow.get(0);
    			queueRow.remove(0);
    			return temObjectQueueRow;
    		}
    		return null;
    	}
    	
    	public synchronized void clear()
    	{
    		queueRow = new ArrayList<QueueRow>();
    	}
    	
    	
    	/**
    	 * trida radku fronty
    	 * 
    	 * @author error414
    	 *
    	 */
    	private class QueueRow
    	{
    		private String command;
    		private int mode;
    		private int callback;
    		private byte[] data;
			
    		
    		public QueueRow(String command, byte[] data, int mode, int callback)
    		{
    			this.command 		= command;
    			this.data 	 		= data;
    			this.mode 	 		= mode;
    			this.callback 	 	= callback;
    		}
    		
    		
    		/**
    		 * getter
    		 * @return
    		 */
    		public String getCommand() 
    		{
				return command;
			}
    		
			/**
			 * getter
			 * @return
			 */
			public byte[] getData() 
			{
				return data;
			}
			
		/**
   		 * getter
   		 * @return
   		 */
   		public int getMode() 
   		{
			return mode;
		}
			
			
		/**
   		 * getter
   		 * @return
   		 */
   		public int getCallback() 
   		{
			return callback;
		}
			
    		
    	}
    		
    }
	
}
