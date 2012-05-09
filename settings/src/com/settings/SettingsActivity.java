package com.settings;

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