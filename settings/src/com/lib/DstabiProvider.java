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

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.util.EncodingUtils;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.helpers.ByteOperation;
import com.helpers.DstabiProfile.ProfileItem;

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
	
	final static public String OK 		= "K";
	final static public String ERROR 	= "E";
	
	final static public int MESSAGE_STATE_CHANGE = 1;
	final static public int MESSAGE_READ = 2;
	final static public int MESSAGE_SEND_COMAND_ERROR = 3;
	final static public int MESSAGE_SEND_COMPLETE = 4;
	
	final static private int PROTOCOL_STATE_NONE = 0;
	final static private int PROTOCOL_STATE_SENDED_INIT_CODE = 1;
	final static private int PROTOCOL_STATE_RETRIEVE_INIT_CODE = 2;
	final static private int PROTOCOL_STATE_SENDED_VALUES = 3;
	final static private int PROTOCOL_STATE_WAIT_FOR_ALL_DATA = 4;
	final static private int PROTOCOL_STATE_WAIT_FOR_ALL_DATA_DIAGNOSTIC = 5;
	final static private int PROTOCOL_STATE_WAIT_FOR_ALL_DATA_GRAPH = 6;
	
	final protected String GET_PROFILE = "G";
	final protected String GET_STICKED_AND_SENZORS_VALUE = "D";
	final protected String SAVE_PROFILE = "g";
	final protected String GET_LOG = "L";
	final protected String SERIAL_NUMBER = "h";
	final protected String GET_GRAPH = "A\1";
	
	private int protocolState = 0;
	
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
	private int mode = NORMAL;
	
	private DataBuilder dataBuilder;
	
	private final Queue queue = new Queue();
	
	private void startCecurityTimer(){
		Log.d(TAG, "zapinam timer");
		securityTimer = new Timer();
		securityTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				TimerMethod();
			}

		}, 7000, 7000);
	}
	
	private void stopCecurityTimer(){
		if(securityTimer != null){
			securityTimer.cancel();
			securityTimer.purge();
			securityTimer = null;
		}
	}
	
	private void TimerMethod()
	{
		connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_SEND_COMAND_ERROR);
		clearState("TimerMethod");
	}
	
	/**
	 * privatni konstruktor na singleton
	 */
	private DstabiProvider() {
		this.BTservice = new BluetoothCommandService(serviceBThandler);
	}
	
	/**
	 * singleton ziskani instance
	 * 
	 * @param connectionHandler
	 * @return
	 */
	public static DstabiProvider getInstance(Handler connectionHandler){
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
	public void connect(BluetoothDevice device) {
		BTservice.connect(device);
	}
	
	/**
	 * zprava pro odpojeni device
	 * 
	 * @param device
	 */
	public void disconnect() {
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
	public void getSerial(int callBackCode){
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
	public void getDiagnostic(int callBackCode){
		if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
			mode = DIAGNOSTIC;
			sendDataForResponce(GET_STICKED_AND_SENZORS_VALUE, callBackCode);
		}else{
			queue.add(GET_STICKED_AND_SENZORS_VALUE, null, DIAGNOSTIC, callBackCode);
		}
	}
	
	/**
	 * ziskani profilu z jednotky
	 * 
	 * @param callBackCode
	 */
	public void getProfile(int callBackCode){
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
	public void getLog(int callBackCode){
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
	 * @param comand
	 * @param callBack
	 */
	public void getGraph(int callBack){
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
	 * @param comand
	 * @param callBack
	 */
	public void stopGraph(){
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
	private void sendData(String command, byte[] data){
		
		Log.d(TAG, "pozadavek na odeslani pozadavku do zarizeni cmd+data");
		
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
	 * @param data
	 */
	private void sendData(String command){
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
	public void sendDataForResponce(String command, int callBackCode){
		if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
			this.callBackCode = callBackCode;
			sendData(command, null);
		}else{
			queue.add(command, null, NORMAL, callBackCode);
		}
	}
	/////////////////////////////////////////////////
	
	////////////////// NO RESPONSE//////////////////
	public void sendDataNoWaitForResponce(String command, byte[] data){
		sendData(command, data);
	}
	
	public void sendDataNoWaitForResponce(String command, int data){
		sendData(command, ByteOperation.intToByteArray(data));
	}
	
	public void sendDataNoWaitForResponce(String command, String data){
		sendData(command, data.getBytes());
	}
	
	public void sendDataNoWaitForResponce(String command){
		sendData(command);
	}
	
	public void sendDataNoWaitForResponce(ProfileItem item){
		if(item.isValid() && item.getCommand() != null){
			sendDataNoWaitForResponce(item.getCommand(), item.getValueBytesArray());
		}else{
			connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_SEND_COMAND_ERROR);
		}
	}
	/////////////////////////////////////////////////
	
	////////// NO RESPONSE / DIRECT WRITE ///////////
	public void sendDataImmediately(byte[] data){
		BTservice.write(data);
	}
	/////////////////////////////////////////////////
	
	public BluetoothDevice getBluetoothDevice(){
		return BTservice.getBluetoothDevice();
	}
	
	private void sendData(){
		
		Log.d(TAG, "odesilam data do zarizeni");
		
		switch(protocolState){
			case DstabiProvider.PROTOCOL_STATE_NONE:
				startCecurityTimer(); // zapneme casovac
				protocolState = DstabiProvider.PROTOCOL_STATE_SENDED_INIT_CODE;
				BTservice.write("4D".getBytes());
				
				Log.d(TAG, "odesilam inicializacni string 4D");
				
				break;
			case DstabiProvider.PROTOCOL_STATE_RETRIEVE_INIT_CODE:
				
				Log.d(TAG, "odesilam prikaz: " + sendCode + " a hodnoty: " + ByteOperation.getHexStringByByteArray(sendValue));
				
				protocolState = DstabiProvider.PROTOCOL_STATE_SENDED_VALUES;
				if(sendCode != null){
					BTservice.write(ByteOperation.combineByteArray(sendCode.getBytes(),  sendValue));
				}
		}
		
	}
	
	/**
	 * zastaveni probohajiciho pozadavku
	 */
	public void abort(){
		clearState("abort");
	}
	
	/**
	 * zastaveni vsech pozadavku
	 */
	public void abortAll(){
		queue.clear();
		clearState("abort all");
	}
	
	private void clearState(String kdo){
		
		Log.d(TAG, "mazu stav:" + kdo);
		
		sendCode 		= null;
		sendValue 		= null;
		callBackCode	= 0;
		protocolState 	= DstabiProvider.PROTOCOL_STATE_NONE;
		mode = NORMAL;
		
		Log.d(TAG, "vypinam timer");
		stopCecurityTimer(); // vypneme casovac
		dataBuilder = null;
		
		
		if(queue.hasNextQueue()){
			
			Log.d(TAG, "ve fronte je dalsi pozadavek odbavuji");
			
			if(getState() == BluetoothCommandService.STATE_CONNECTED){
				com.lib.DstabiProvider.Queue.QueueRow tempQueue = queue.getNextQueue();
				mode = tempQueue.getMode();
				callBackCode = tempQueue.getCallback();
				sendData(tempQueue.getCommand(), tempQueue.getData());
			}else{
				queue.clear();
				connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_SEND_COMAND_ERROR);
			}
		}
	}
	
	/**
	 * handler pro komunikaci s BT servisem
	 */
	// The Handler that gets information back from the 
    protected final Handler serviceBThandler = new Handler(new Handler.Callback() {
	    @Override
	    public boolean handleMessage(Message msg) {
        	switch(msg.what){
        	 	//zmena stavu BT modulu
        		case DstabiProvider.MESSAGE_STATE_CHANGE: 
        			connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_STATE_CHANGE);
        			clearState("handler 1");
        			//zmena stavu BT modulu
        		case DstabiProvider.MESSAGE_READ:
        			
        			Bundle b = msg.getData();
        			if(b.containsKey("msg")){
        				
        				byte[] byteMessage = b.getByteArray("msg");
        				
        				Log.d(TAG, "prijmam data");
        				
        				String message 			= parseMessagegetCode(byteMessage);
        				byte[] data 			= parseMessagegetData(byteMessage);
        				
        				switch(protocolState){
        					case DstabiProvider.PROTOCOL_STATE_NONE:
        						//prisla sprava ale nic necekame, tak ignorujem
        						break;
        					
        					case DstabiProvider.PROTOCOL_STATE_SENDED_INIT_CODE:
        						Log.d(TAG, "prijmana data byla odpoved na inicializacni kod");
        						//byl odeslan init kod, cekame O nebo K
        						if(message.equals(DstabiProvider.OK)){ // OK
        							Log.d(TAG, "prijmana data byla odpoved na inicializacni kod, OK");
        							protocolState = DstabiProvider.PROTOCOL_STATE_RETRIEVE_INIT_CODE;
        							sendData();
        						}else{ // ERROR
        							Log.d(TAG, "prijmana data byla odpoved na inicializacni kod, ERROR");
        							connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_SEND_COMAND_ERROR);
        							abortAll(); // zrusime celou frontu
        							clearState("handler 2");
        						}
        						break;
        						
        					case DstabiProvider.PROTOCOL_STATE_SENDED_VALUES:
        						Log.d(TAG, "prijmana data byla odpoved na prikaz");
	    						//byl odeslan init kod, cekame O nebo K
	    						if(message.equals(DstabiProvider.OK) || mode == DIAGNOSTIC){ // OK nebo sme v diagnostice 
	    							
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
	    								dataBuilder = new DataBuilder(16); // diagnostika je dlouhe 16 bytu
	    								dataBuilder.add(byteMessage); // pouzijeme celou zpravu co nam prisla,protoze diagnostika nema zadne K na zacatku
	    								
	    								// profil je cely odesilame zpravu s profilem, poud neni cely zachytava to
	    								// case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA: kde se dal ceka na dalsi data
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
	    							connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_SEND_COMAND_ERROR);
	    							abortAll(); 
	    							clearState("handler 5");
	    						 }
        						break;
        						
        					//cekameme na dalsi data z profilu nebo ze serioveho cisla
        					case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA:
        						Log.d(TAG, "Prislo x :" + ByteOperation.getIntegerStringByByteArray(data));
        						dataBuilder.add(data);
        						if(dataBuilder.itsAll()){
        							Log.d(TAG, "x  cele odesilam handle");
        							sendHandle(callBackCode, dataBuilder.getData());
								}else{
									Log.d(TAG, "x neni cele :" + dataBuilder.length);
								}
        						break;
        						
        					// prijmame dalsi casti z diagnostiky, musime pouzit vlastni switch protoze diagnistika nepouziva promenou data ale byteMessage
        					case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA_DIAGNOSTIC:
        						Log.d(TAG, "Prislo diag :" + ByteOperation.getIntegerStringByByteArray(data));
        						dataBuilder.add(byteMessage);  // pouzijeme celou zpravu co nam prisla,protoze diagnostika nema zadne K na zacatku
        						if(dataBuilder.itsAll()){
        							Log.d(TAG, "diag  cele odesilam handle");
        							sendHandle(callBackCode, dataBuilder.getData());
								}else{
									Log.d(TAG, "diag neni cele :" + dataBuilder.length);
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
    
    private void sendHandle(int callBackCode, byte[] data){
    	Bundle budleForMsg = new Bundle();
		budleForMsg.putByteArray("data", data);
        Message m = connectionHandler.obtainMessage( callBackCode );
        m.setData(budleForMsg);
        connectionHandler.sendMessage(m);
        Log.d(TAG, "zprava poslana");
        clearState("send handle");
    }
    
    private void sendHandleNotStop(int callBackCode, byte[] data){
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
	 * @param msg
	 * @return
	 */
    private String parseMessagegetCode(byte[] msg){
    	if(msg.length == 1){// prisel jednoduchy vystup K nebo E
    		return EncodingUtils.getAsciiString(msg, 0, 1);
    	}else if(msg.length > 1){
    		
        	String code= EncodingUtils.getAsciiString(msg, 0, 1);

    		if(code.equals(OK) || code.equals(ERROR)){ // prisel zacatek profilu odstranime K na zacatku profilu
    			return code;
    		}
    	}

    	return new String();
    }
    
    /**
     * ziskani dat ze zpravy
     * 
     * @param msg
     * @return
     */
    private byte[] parseMessagegetData(byte[] msg){
    	//zprava je 
    	if(msg.length == 1 && protocolState != PROTOCOL_STATE_WAIT_FOR_ALL_DATA){// prisel jednoduchy vystup K nebo E
    		return null;
    	}else if(msg.length > 1){
    		
    		String code= EncodingUtils.getAsciiString(msg, 0, 1);
    		Log.d(TAG, code);
    		if((code.equals(OK) || code.equals(ERROR)) && protocolState != PROTOCOL_STATE_WAIT_FOR_ALL_DATA){ // prisel zacatek profilu odstranime K na zacatku profilu a nasmime byt v modu cekani na data
    			
    			byte[] result = new byte[msg.length-1];
    			System.arraycopy(msg, 1, result, 0, msg.length-1);
    			return result;
    		}
    	}
    	
    	return msg;
    }
    
    
    /**
     * PROFILE BUILDER
     */
    
    private class DataBuilder
    {
    	
    	private byte[] profile = null;
    	private int length = 0;
    	
    	
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
    	public void add(byte[] part)
    	{
    		if(profile == null || profile.length == 0){ // prvni cast 
    			if(part != null && part.length != 0){
    				if(length == 0){
    					length = ByteOperation.byteToUnsignedInt(part[0]);
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
    	public void clear()
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
    	public byte[] getData()
    	{
    		return profile;
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
    	public void add(String command, byte[] data, int mode, int callback) {
    		queueRow.add(new QueueRow(command, data, mode, callback));
    	}
    	
    	public int count()
    	{
    		return queueRow.size();
    	}
    	
    	public Boolean hasNextQueue()
    	{
    		return this.count() > 0;
    	}
    	
    	public QueueRow getNextQueue()
    	{
    		if(hasNextQueue()){
    			QueueRow temObjectQueueRow = queueRow.get(0);
    			queueRow.remove(0);
    			return temObjectQueueRow;
    		}
    		return null;
    	}
    	
    	public void clear()
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
