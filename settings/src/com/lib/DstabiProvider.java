package com.lib;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.util.EncodingUtils;

import com.helpers.ByteOperation;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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
	
	final protected String GET_PROFILE = "G";
	final protected String GET_STICKED_AND_SENZORS_VALUE = "D";
	final protected String SAVE_PROFILE = "g";
	
	private int protocolState = 0;
	
	private String sendCode;
	private byte[] sendValue;
	
	private int callBackCode = 0;
	
	// v jakym modu se provider nachazi, jestli v jednoduchem pozadavku nebo v profilu
	final private int NORMAL = 1;
	final private int PROFILE = 2;
	private int mode = NORMAL;
	
	private ProfileBuilder profileBuilder;
	
	private final Queue queue = new Queue();
	
	private void startCecurityTimer(){
		securityTimer = new Timer();
		securityTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				TimerMethod();
			}

		}, 5000, 5000);
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
		clearState();
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
	 * odelsani dat do zarizeni
	 * 
	 * @param command
	 * @param data
	 */
	private void sendData(String command, byte[] data){
		if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
			sendCode = command;
			sendValue = data;
			sendData();
		}else{
			// nejaky pozadavek uz bezi tak ho dame do fronty
			queue.add(command, data);
		}
	}
	
	/**
	 * odelsani dat do zarizeni
	 * 
	 * @param command
	 * @param data
	 */
	private void sendData(String command){
		if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
			sendCode = command;
			sendValue = null;
			sendData();
		}else{
			// nejaky pozadavek uz bezi tak ho dame do fronty
			queue.add(command, null);
		}
	}
	
	/**
	 * ziskani profilu z jednotky
	 * 
	 * @param callBackCode
	 */
	public void getProfile(int callBackCode){
		if(DstabiProvider.PROTOCOL_STATE_NONE == protocolState){
			mode = PROFILE;
			sendDataForResponce(GET_PROFILE, callBackCode);
		}else{
			// tady bude naplneni fronty
			//queue
		}
	}
	
	//////////////////WAIT RESPONSE//////////////////
	public void sendDataForResponce(String command, int callBackCode){
		this.callBackCode = callBackCode;
		sendData(command, null);
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
	
	public BluetoothDevice getBluetoothDevice(){
		return BTservice.getBluetoothDevice();
	}
	
	private void sendData(){
		switch(protocolState){
			case DstabiProvider.PROTOCOL_STATE_NONE:
				startCecurityTimer(); // zapneme casovac
				protocolState = DstabiProvider.PROTOCOL_STATE_SENDED_INIT_CODE;
				BTservice.write("4D".getBytes());
				break;
			case DstabiProvider.PROTOCOL_STATE_RETRIEVE_INIT_CODE:
				protocolState = DstabiProvider.PROTOCOL_STATE_SENDED_VALUES;
				BTservice.write(ByteOperation.combineByteArray(sendCode.getBytes(),  sendValue));
		}
		
	}
	
	/**
	 * zastaveni probohajiciho pozadavku
	 */
	public void abort(){
		clearState();
	}
	
	private void clearState(){
		sendCode 		= null;
		sendValue 		= null;
		callBackCode	= 0;
		protocolState 	= DstabiProvider.PROTOCOL_STATE_NONE;
		mode = NORMAL;
		
		stopCecurityTimer(); // vypneme casovac
		profileBuilder = null;
		
		if(queue.hasNextQueue()){
			com.lib.DstabiProvider.Queue.QueueRow tempQueue = queue.getNextQueue();
			sendData(tempQueue.getCommand(), tempQueue.getData());
		}
	}
	
	/**
	 * handler pro komunikaci s BT servisem
	 */
	// The Handler that gets information back from the 
    protected final Handler serviceBThandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	switch(msg.what){
        	 	//zmena stavu BT modulu
        		case DstabiProvider.MESSAGE_STATE_CHANGE: 
        			connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_STATE_CHANGE);
        			clearState();
        			//zmena stavu BT modulu
        		case DstabiProvider.MESSAGE_READ:
        			
        			Bundle b = msg.getData();
        			if(b.containsKey("msg")){
        				
        				byte[] byteMessage = b.getByteArray("msg");
        				
        				String message 			= parseMessagegetCode(byteMessage);
        				byte[] data 			= parseMessagegetData(byteMessage);
        				
        				
        				switch(protocolState){
        					case DstabiProvider.PROTOCOL_STATE_NONE:
        						//prisla sprava ale nic necekame, tak ignorujem
        						break;
        					
        					case DstabiProvider.PROTOCOL_STATE_SENDED_INIT_CODE:
        						//byl odeslan init kod, cekame O nebo K
        						if(message.equals(DstabiProvider.OK)){ // OK
        							protocolState = DstabiProvider.PROTOCOL_STATE_RETRIEVE_INIT_CODE;
        							sendData();
        						}else{ // ERROR
        							connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_SEND_COMAND_ERROR);
        							clearState();
        						}
        						break;
        						
        					case DstabiProvider.PROTOCOL_STATE_SENDED_VALUES:
	    						//byl odeslan init kod, cekame O nebo K
	    						if(message.equals(DstabiProvider.OK)){ // OK nebo sme v senzor 
	    							//pokud sem v normal modu tak odelsle zpravu ze sme prijaly potvrzeni K
	    							if(callBackCode == 0 && mode == NORMAL){
	    								connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_SEND_COMPLETE);
	    								clearState();
	    							
	    							//aktovita meni cislo zada od zaslani callbecu
	    							}else if(callBackCode != 0 && mode == NORMAL){ 
	    								connectionHandler.sendEmptyMessage(callBackCode);
	    								clearState();
	    								
	    							//PROFILE pokud zada aktivita o profil tak podle delky privniho bytu cekame na cely profil
	    							}else if(mode == PROFILE){
	    								//zmenime state protokokolu na pripadne cekani na konec profilu
	    								protocolState = PROTOCOL_STATE_WAIT_FOR_ALL_DATA;
	    								profileBuilder = new ProfileBuilder();
	    								profileBuilder.add(data);
	    								
	    								// profil je cely odesilame zpravu s profilem, poud neni cely zachytava to
	    								// case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA: kde se dal ceka na dalsi data
	    								if(profileBuilder.itsAll()){
	    									sendHandle(callBackCode, profileBuilder.getData());
	    								}
	    							}
	    						 }else{ // ERROR
	    							connectionHandler.sendEmptyMessage(DstabiProvider.MESSAGE_SEND_COMAND_ERROR);
	    							clearState();
	    						 }
        						break;
        					case DstabiProvider.PROTOCOL_STATE_WAIT_FOR_ALL_DATA:
        						profileBuilder.add(data);
        						if(profileBuilder.itsAll()){
        							sendHandle(callBackCode, profileBuilder.getData());
								}
        						break;
        				}
        			}
        	}
        	
        }
    };
    
    private void sendHandle(int callBackCode, byte[] data){
    	Bundle budleForMsg = new Bundle();
		budleForMsg.putByteArray("data", data);
        Message m = connectionHandler.obtainMessage( callBackCode );
        m.setData(budleForMsg);
        connectionHandler.sendMessage(m);
        clearState();
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
    	if(msg.length == 1){// prisel jednoduchy vystup K nebo E
    		return null;
    	}else if(msg.length > 1){
    		String code= EncodingUtils.getAsciiString(msg, 0, 1);
    		
    		if(code.equals(OK) || code.equals(ERROR)){ // prisel zacatek profilu odstranime K na zacatku profilu
    			System.arraycopy(msg, 1, msg, 0, msg.length-1);
    			return msg;
    		}
    	}
    	
    	return msg;
    }
    
    
    /**
     * PROFILE BUILDER
     */
    
    private class ProfileBuilder
    {
    	
    	private byte[] profile = null;
    	private int lenght = 0;
    	
    	
    	@SuppressWarnings("unused")
		public ProfileBuilder(int lenght)
    	{
    		this.lenght = lenght;
    	}
    	
    	public ProfileBuilder()
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
    				if(lenght == 0){
    					lenght = ByteOperation.byteToUnsignedInt(part[0]);
    				}
    				profile = part;
    			}
    		}else{
    			profile = ByteOperation.combineByteArray(profile, part);
    		}
    	}
    	
    	/**
    	 * je profil vetsi nez pocet dat ktere chceme
    	 * 
    	 * @return
    	 */
    	public Boolean itsAll()
    	{
    		if(profile != null){
    			return (lenght <= profile.length && lenght != 0);
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
    	public void add(String command, byte[] data) {
    		queueRow.add(new QueueRow(command, data));
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
    	
    	
    	/**
    	 * trida radku fronty
    	 * 
    	 * @author error414
    	 *
    	 */
    	private class QueueRow
    	{
    		private String command;
    		private byte[] data;
			
    		
    		public QueueRow(String command, byte[] data)
    		{
    			this.command = command;
    			this.data 	 = data;
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
			
    		
    	}
    		
    }
	
}
