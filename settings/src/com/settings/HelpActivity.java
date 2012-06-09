package com.settings;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;

public class HelpActivity extends BaseActivity{
	final private String TAG = "HelpActivity";

	
	private DstabiProvider stabiProvider;
	
	/**
	 * zavolani pri vytvoreni instance aktivity settings
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.help);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
        ((TextView)findViewById(R.id.title)).setText(
				TextUtils.concat("...", " \u2192 " , getString(R.string.help))
		);
		
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		
		Bundle extras = getIntent().getExtras(); 
		if(extras != null){
			
			LayoutInflater inflate = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View mDecor = inflate.inflate(extras.getInt("content"), null);
			
			
			WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
	        
			View l1 = getWindow().getDecorView().findViewById(android.R.id.content);
		    ((ViewGroup) l1).addView(mDecor, mParams);
			
		}
		
		

		
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
