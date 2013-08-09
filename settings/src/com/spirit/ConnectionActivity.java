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

package com.spirit;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.helpers.ByteOperation;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.lib.FileDialog;
import com.lib.SelectionMode;
import com.spirit.R;
import com.spirit.R.string;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectionActivity extends BaseActivity{
	private final String TAG = "ConnectionActivity";
	
	final protected int REQUEST_SAVE = 1;
	final protected int REQUEST_OPEN = 2;
	
	final protected int GROUP_PROFILE = 3;  
	final protected int PROFILE_LOAD = 1;
	final protected int PROFILE_SAVE = 2;
	
	final static protected String DEFAULT_PROFILE_PATH = "/sdcard/gravity_profile/";
	
	private TextView textStatusView;
	private Spinner  btDeviceSpinner;
	private Button   connectButton;
	private TextView curentDeviceText;
	
	final private int PROFILE_CALL_BACK_CODE = 16;
	final private int PROFILE_CALL_BACK_CODE_FOR_SAVE = 17;
	final private int GET_SERIAL_NUMBER = 18;
	
	final private String PREF_BT_ADRESS= "pref_bt_adress";
	
	final static String FILE_EXT = "4ds";
	
	private DstabiProvider stabiProvider;
	
	DstabiProfile profileCreator;
	
	private String fileForSave;
	
	/**
	 * je mozne odesilat data do zarizeni
	 */
	private Boolean isPosibleSendData = true;
	
	/**
	 * zavolani pri vytvoreni instance aktivity settings
	 */
	public void onCreate(Bundle savedInstanceState) 
	{
		 super.onCreate(savedInstanceState);
		 
		 requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		 
		 setContentView(R.layout.connection);

		 getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		 ((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.connection_button_text)));
		 
		 
		 stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		 
		 textStatusView 	= (TextView)findViewById(R.id.status_text);
		 btDeviceSpinner 	= (Spinner)findViewById(R.id.bt_device_spinner);
		 connectButton 		= (Button)findViewById(R.id.connection_button);
		 curentDeviceText 	= (TextView)findViewById(R.id.curent_device_text);
		 
		 Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		 ArrayAdapter<CharSequence> BTListSpinnerAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		 BTListSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		 
		 //pozice vybraneho selectu
		 int position = 0;
		 if (pairedDevices.size() > 0) {
			 SharedPreferences settings = getSharedPreferences(PREF_BT_ADRESS, 0);
			 String prefs_adress = settings.getString(PREF_BT_ADRESS, "");
			
			 Log.d(TAG, prefs_adress);
			// iterator
			int i = 0;
		    // Loop through paired devices
		    for (BluetoothDevice device : pairedDevices) {
		        // Add the name and address to an array adapter to show in a ListView
		    	BTListSpinnerAdapter.add(device.getName().toString() + " [" + device.getAddress().toString() + "]");
		    	
		    	//hledani jestli se zarizeni v aktualni iteraci nerovna zarizeni ulozene v preference
		    	if(prefs_adress.equals( device.getAddress().toString())){
		    		position = i;
		    	}
		    	i++;
		    }
		}else{
			Toast.makeText(getApplicationContext(), R.string.first_paired_device, Toast.LENGTH_SHORT).show();
			finish();
		}
		 
		btDeviceSpinner.setAdapter(BTListSpinnerAdapter);
		//ulozime do selectu zarizeni hodnotu nalezeneho zarizeni, MAth.min je tam jen pro jistotu
		btDeviceSpinner.setSelection(Math.min(btDeviceSpinner.getCount() - 1, position)); 
		////////////////////////////////////////////////////////////////
	 }
	
	/**
	  * prvotni konfigurace view
	  */
	 @Override
	public void onResume(){
		super.onResume();
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		
		TextView serial  = (TextView) findViewById(R.id.serial_number);
		serial.setText(R.string.unknow_serial);

		updateState();
	}
	 
	/**
	  * naplneni formulare
	  * 
	  * @param profile
	  */
	 private void initGuiByProfileString(byte[] profile){
		 profileCreator = new DstabiProfile(profile);
		 TextView version = (TextView) findViewById(R.id.version);
		 
		 if(profileCreator.isValid()){
			 int minor 		= 0;
			 version.setText(profileCreator.getProfileItemByName("MAJOR").getValueString() + "." + String.valueOf(minor) + "." + profileCreator.getProfileItemByName("MINOR").getValueString());
		 }else{
			 version.setText(R.string.unknow_version);
		 }
	 }
	 
	 /**
	  * prisla informace o seriovem cisle
	  * initGuiBySerialNumber
	  * @param profile
	  */
	 private void initGuiBySerialNumber(byte[] serialNumber){
		 Log.d(TAG, ByteOperation.getIntegerStringByByteArray(serialNumber));
		 
		 if(serialNumber == null || serialNumber.length != 6){
			 return;
		 }
		 
		 TextView serial = (TextView) findViewById(R.id.serial_number);
		 
		 
		 String serialFormat = "";
		 for (byte b : serialNumber) {
			 serialFormat = serialFormat + ByteOperation.byteToHexString(b) + " ";
		 }
			 
		 serial.setText(serialFormat);
	 }
	
	 /**
	  * stisknuti tlacitka pripojeni
	  * 
	  * @param v
	  */
	 public void manageConnectionToBTDevice(View v){
		if(stabiProvider.getState() == BluetoothCommandService.STATE_LISTEN || stabiProvider.getState() == BluetoothCommandService.STATE_NONE){
			
			String deviceAdress = btDeviceSpinner.getSelectedItem().toString().substring(btDeviceSpinner.getSelectedItem().toString().indexOf("[") + 1, btDeviceSpinner.getSelectedItem().toString().indexOf("]"));
			
			//ulozeni vybraneho selectu / zarizeni
			SharedPreferences settings = getSharedPreferences(PREF_BT_ADRESS, 0);
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putString(PREF_BT_ADRESS, deviceAdress);

	        // Commit the edits!
	        editor.commit();

			
			
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAdress);
			stabiProvider.connect(device);
		}else if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTING){
			Toast.makeText(getApplicationContext(), R.string.BT_connection_progress, Toast.LENGTH_SHORT).show();
		}else if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED){
			// DISCONNECT
			stabiProvider.disconnect();
		}
	 }
	
	private void updateState(){
		initGuiByProfileString(null);
		
		switch(stabiProvider.getState()){
			case BluetoothCommandService.STATE_CONNECTING: 
				textStatusView.setText(R.string.connecting); 
				textStatusView.setTextColor(Color.MAGENTA);
				curentDeviceText.setText(null);
				sendInSuccessDialog();
				break;
			case BluetoothCommandService.STATE_CONNECTED: 
				textStatusView.setText(R.string.connected);
				textStatusView.setTextColor(Color.GREEN);
				
				connectButton.setText(R.string.disconnect);
				
				BluetoothDevice device = stabiProvider.getBluetoothDevice();
				curentDeviceText.setText(device.getName() + " [" + device.getAddress() + "]");
				
				 showDialogRead();
				 stabiProvider.getProfile(PROFILE_CALL_BACK_CODE);
				 
				 showDialogRead();
				 stabiProvider.getSerial(GET_SERIAL_NUMBER);
				 
				 ((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
				
				break;
			default:
				textStatusView.setText("Disconnect");
				textStatusView.setTextColor(Color.RED);
				
				connectButton.setText(R.string.connect);
				
				curentDeviceText.setText(null);
				sendInSuccessDialog();
				
				((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.red);
				break;
		}
	}
	
	
	/**
     * vytvoreni kontextoveho menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	    super.onCreateOptionsMenu(menu);
	    
	    SubMenu profile = menu.addSubMenu(R.string.profile);
	    profile.add(GROUP_PROFILE, PROFILE_LOAD, Menu.NONE, R.string.load_profile);
	    profile.add(GROUP_PROFILE, PROFILE_SAVE, Menu.NONE, R.string.save_profile);
	    
	    return true;
    }
	
	/**
     * reakce na kliknuti polozky v kontextovem menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	super.onOptionsItemSelected(item); 
    	//nahrani / ulozeni profilu
    	if(item.getGroupId() == GROUP_PROFILE){
    		// musime byt pripojeni k zarizeni
    		/*if(stabiProvider == null || stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
        		Toast.makeText(getApplicationContext(), R.string.must_first_connect_to_device, Toast.LENGTH_SHORT).show();
        		return false;
        	}*/
    		
    		
    		Intent intent = new Intent(getBaseContext(), FileDialog.class);
            intent.putExtra(FileDialog.START_PATH, DEFAULT_PROFILE_PATH);
            intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
            intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { FILE_EXT });

            
            if(item.getItemId() == PROFILE_LOAD){
            	intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
            	startActivityForResult(intent, REQUEST_OPEN);
            }else if(item.getItemId() == PROFILE_SAVE){
            	intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_CREATE);
            	startActivityForResult(intent, REQUEST_SAVE);
            }
    	}
    	return false;
    }
    
    /**
     * zachytavani vysledku z aktivit
     * 
     */
    public synchronized void onActivityResult(final int requestCode,
        int resultCode, final Intent data) {
    	switch (requestCode) {
    		case REQUEST_SAVE:
    		case REQUEST_OPEN:
		        if (resultCode == Activity.RESULT_OK) {
		        	String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
	                
		        	if (requestCode == REQUEST_SAVE) {
	                	//SAVE
		        		
		        		if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED){
			        		showDialogWrite();
			        		fileForSave = filePath;
							stabiProvider.getProfile(PROFILE_CALL_BACK_CODE_FOR_SAVE);
		        		}
	                	
	                } else if (requestCode == REQUEST_OPEN) {
	                	//OPEN
	                    File file = new File(filePath);
	                    try {
	                    	byte[] profile = DstabiProfile.loadProfileFromFile(file);
	    				    //////////////////////////////////////////////////////////////////////////////
	    					insertProfileTounit(profile);
	    					
	    				} catch (FileNotFoundException e) {
	    					Toast.makeText(getApplicationContext(), R.string.file_not_found, Toast.LENGTH_SHORT).show();
	    				} catch (IOException e) {
	    					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
	    				}
	                }
		                
		        } else if (resultCode == Activity.RESULT_CANCELED) {
		        	// zruzeni vybirani souboru
		        }
		        break;
    	}
    }
	
	
	// The Handler that gets information back from the 
    private final Handler connectionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	switch(msg.what){
    			case DstabiProvider.MESSAGE_STATE_CHANGE: 
    				updateState();	
    				break;
    			case DstabiProvider.MESSAGE_SEND_COMAND_ERROR:
    				isPosibleSendData = false;
    				stabiProvider.abortAll();
    				sendInError(false); // ukazat error ale neukoncovat activitu
        			break;
        		case DstabiProvider.MESSAGE_SEND_COMPLETE:
        			sendInSuccessDialog();
        			Log.d(TAG, "prisly data count je " + String.valueOf(progressCount));
        			break;
    			case PROFILE_CALL_BACK_CODE:
    				sendInSuccessDialog();
        			if(msg.getData().containsKey("data")){
        				initGuiByProfileString(msg.getData().getByteArray("data"));
        			}
        			break;
    			case PROFILE_CALL_BACK_CODE_FOR_SAVE:
    				sendInSuccessDialog();
        			if(msg.getData().containsKey("data")){
        				saveProfileTofile(msg.getData().getByteArray("data"));
        			}
        			break;
    			case GET_SERIAL_NUMBER:
    				sendInSuccessDialog();
    				if(msg.getData().containsKey("data")){
    					initGuiBySerialNumber(msg.getData().getByteArray("data"));
    				}
    				break;
        	}
        }
    };
    
    /**
     * nacteni profilu ze souboru a nahrani do jednotky
     * 
     * @param profile
     */
    private void insertProfileTounit(byte[] profile)
    {
    	isPosibleSendData = true;
    	Log.d(TAG, "delka profilu na odeslani " + String.valueOf(profile.length));
    	// musime byt pripojeni
    	if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
    		Toast.makeText(getApplicationContext(), R.string.connection_error, Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	byte[] lenght = new byte[1];
    	lenght[0] = ByteOperation.intToByte(profile.length - 1); 
    			
    	DstabiProfile mstabiProfile = new DstabiProfile(ByteOperation.combineByteArray(lenght, profile));
    	
    	if(mstabiProfile.isValid()){
    		HashMap<String, ProfileItem> items = mstabiProfile.getProfileItems();
    		
    		for (ProfileItem item : items.values()) {
    			if(item.getCommand() != null && isPosibleSendData){
    				showDialogRead();
    				Log.d(TAG, "odesilam prikaz "+ item.getCommand() + " : count je " + String.valueOf(progressCount));
    				stabiProvider.sendDataNoWaitForResponce(item);
    			}else if(!isPosibleSendData){
    				isPosibleSendData = true;
    				break;
    			}else{
    				continue;
    			}
    		}
    	}else{
    		Toast.makeText(getApplicationContext(), R.string.damage_profile, Toast.LENGTH_SHORT).show();
    	}
    }
    
    /**
     * ulozeni proiflu do souboru
     * 
     * @param profile
     */
    private void saveProfileTofile(byte[] profile)
    {
    	// musime byt pripojeni
    	if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
    		Toast.makeText(getApplicationContext(), R.string.connection_error, Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	if(fileForSave == null){
    		Toast.makeText(getApplicationContext(), R.string.file_not_found, Toast.LENGTH_SHORT).show();
    		return;
    	}
    	showDialogWrite();
		try {
			System.arraycopy(profile, 1, profile, 0, profile.length-1);
			if(fileForSave.endsWith(".4ds")){ // konci nazev souboru na string .4ds, pokud ano nepridavame priponu
				DstabiProfile.saveProfileToFile(new File(fileForSave), profile);
			}else{
				DstabiProfile.saveProfileToFile(new File(fileForSave + "." + FILE_EXT), profile);
			}
			
			fileForSave = null;
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), R.string.file_not_found, Toast.LENGTH_SHORT).show();
		}
		sendInSuccessDialog();
    }
    
    
}
