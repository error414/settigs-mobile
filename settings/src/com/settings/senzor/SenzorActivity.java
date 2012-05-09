package com.settings.senzor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.settings.BaseActivity;
import com.settings.R;

public class SenzorActivity extends BaseActivity{
	@SuppressWarnings("unused")
	final private String TAG = "SenzorActivity";
	
	private DstabiProvider stabiProvider;
	/**
	 * zavolani pri vytvoreni instance aktivity servos
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.senzor);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.senzor_button_text)));
        
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
    }
	
	/**
	 * znovu nacteni aktovity, priradime dstabi svuj handler a zkontrolujeme jestli sme pripojeni
	 */
	@Override
	public void onResume(){
		super.onResume();
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED){
			((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
		}else{
			finish();
		}
	}
	
	/**
	 * otevreni aktivity senzivitu senzoru
	 * 
	 * @param v
	 */
	public void openSenzorSenzivityActivity(View v)
	{
		Intent i = new Intent(SenzorActivity.this, SenzorSenzivityActivity.class);
    	startActivity(i);
	}
	
	/**
	 * otevreni aktivity reverz senzoru
	 * 
	 * @param v
	 */
	public void openSenzorReverseActivity(View v)
	{
		Intent i = new Intent(SenzorActivity.this, SenzorReverseActivity.class);
		startActivity(i);
	}
	
	/**
	 * otevreni aktivity reverz senzoru
	 * 
	 * @param v
	 */
	public void openSenzorRotationSpeedActivity(View v)
	{
		Intent i = new Intent(SenzorActivity.this, SenzorRotationSpeedActivity.class);
		startActivity(i);
	}
	
	
	
	// The Handler that gets information back from the 
	 private final Handler connectionHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	switch(msg.what){
	        		case DstabiProvider.MESSAGE_STATE_CHANGE:
						if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
							sendInError();
							finish();
						}else{
							((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
						}
						break;
	        	}
	        }
	    };
}
