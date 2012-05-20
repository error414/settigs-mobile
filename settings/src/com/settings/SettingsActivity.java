package com.settings;

import java.util.ArrayList;
import java.util.HashMap;

import com.helpers.MenuListAdapter;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.settings.senzor.SenzorActivity;
import com.settings.servo.ServosActivity;

import advanced.AdvancedActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * aktivita pro hlavni obrazku
 * 
 * @author error414
 *
 */
public class SettingsActivity extends BaseActivity {
	final private String TAG = "SettingsActivity";
	
	private DstabiProvider stabiProvider;
	
	/**
	 * zavolani pri vytvoreni instance aktivity settings
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(getText(R.string.full_app_name));
		
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		
		
		ListView menuList = (ListView) findViewById(R.id.listMenu);
		MenuListAdapter adapter = new MenuListAdapter(this, createArrayForMenuList());
		menuList.setAdapter(adapter);
		menuList.setOnItemClickListener(new OnItemClickListener() {
			 
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	
            	switch(position){
            		case 0://connection
            			openConnectionIndent(view);
            			break;
            		case 1://general
            			openGeneralIndent(view);
            			break;
            		case 2://servo
            			openServosIndent(view);
            			break;
            		case 3://senzor
            			openSenzorIndent(view);
            			break;
            		case 4://advanced
            			openAdvancedIndent(view);
            			break;
            	}
 
            }
        });
    }
	
	public void onResume(){
		super.onResume();
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED){
			((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
		}else{
			((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.red);
		}
	}
	
	/**
	 * vytvoreni pole pro adapter menu listu
	 * 
	 * tohle se bude vytvaret dynamicky z pole
	 * 
	 * @return
	 */
	public ArrayList<HashMap<Integer, Integer>> createArrayForMenuList(){
		ArrayList<HashMap<Integer, Integer>> menuListData = new ArrayList<HashMap<Integer, Integer>>();
		//connection
		HashMap<Integer, Integer> connection = new HashMap<Integer, Integer>();
		connection.put(TITLE_FOR_MENU, R.string.connection_button_text);
		connection.put(ICO_RESOURCE_ID, R.drawable.i4);
		menuListData.add(connection);
		
		//general
		HashMap<Integer, Integer> general = new HashMap<Integer, Integer>();
		general.put(TITLE_FOR_MENU, R.string.general_button_text);
		general.put(ICO_RESOURCE_ID, R.drawable.i6);
		menuListData.add(general);
		
		//servo
		HashMap<Integer, Integer> servo = new HashMap<Integer, Integer>();
		servo.put(TITLE_FOR_MENU, R.string.servos_button_text);
		servo.put(ICO_RESOURCE_ID, R.drawable.i8);
		menuListData.add(servo);
		
		//senzor
		HashMap<Integer, Integer> senzor = new HashMap<Integer, Integer>();
		senzor.put(TITLE_FOR_MENU, R.string.senzor_button_text);
		senzor.put(ICO_RESOURCE_ID, R.drawable.i15);
		menuListData.add(senzor);
		
		//advanced
		HashMap<Integer, Integer> advanced = new HashMap<Integer, Integer>();
		advanced.put(TITLE_FOR_MENU, R.string.advanced_button_text);
		advanced.put(ICO_RESOURCE_ID, R.drawable.i20);
		menuListData.add(advanced);
		
		return menuListData;
	}
	
	/**
	 * kliknuti na tlacitko connection na hlavni obrazovce
	 * 
	 * @param v
	 */
    public void openConnectionIndent(View v)
    {
    	Intent i = new Intent(SettingsActivity.this, ConnectionActivity.class);
    	startActivity(i);
    }
    
	/**
	 * kliknuti na tlacitko general na hlavni obrazovce
	 * 
	 * @param v
	 */
    public void openGeneralIndent(View v)
    {
    	if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED){
    		Intent i = new Intent(SettingsActivity.this, GeneralActivity.class);
        	startActivity(i);
    	}else{
    		Toast.makeText(getApplicationContext(), R.string.must_first_connect_to_device, Toast.LENGTH_SHORT).show();
    	}
    	
    }
    
    /**
   	 * kliknuti na tlacitko servos na hlavni obrazovce
   	 * 
   	 * @param v
   	 */
    public void openServosIndent(View v)
    {
    	if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED){
			Intent i = new Intent(SettingsActivity.this, ServosActivity.class);
			startActivity(i);
    	}else{
    		Toast.makeText(getApplicationContext(), R.string.must_first_connect_to_device, Toast.LENGTH_SHORT).show();
    	}
    }
    
    /**
	 * kliknuti na tlacitko senzor na hlavni obrazovce
	 * 
	 * @param v
	 */
    public void openSenzorIndent(View v)
    {
    	if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED){
    		Intent i = new Intent(SettingsActivity.this, SenzorActivity.class);
        	startActivity(i);
    	}else{
    		Toast.makeText(getApplicationContext(), R.string.must_first_connect_to_device, Toast.LENGTH_SHORT).show();
    	}
    }
    
    /**
	 * kliknuti na tlacitko senzor na hlavni obrazovce
	 * 
	 * @param v
	 */
    public void openAdvancedIndent(View v)
    {
    	if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED){
    		Intent i = new Intent(SettingsActivity.this, AdvancedActivity.class);
        	startActivity(i);
    	}else{
    		Toast.makeText(getApplicationContext(), R.string.must_first_connect_to_device, Toast.LENGTH_SHORT).show();
    	}
    }
    
 // The Handler that gets information back from the 
 	 private final Handler connectionHandler = new Handler() {
 	        @Override
 	        public void handleMessage(Message msg) {
 	        	switch(msg.what){
 	        		case DstabiProvider.MESSAGE_STATE_CHANGE:
 						if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
 							sendInError(false);
 							((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.red);
 						}else{
 							((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
 						}
 						break;
 	        	}
 	        }
 	    };
    
    
}