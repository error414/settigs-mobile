package com.settings;

import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class ServosActivity extends BaseActivity{
	final private String TAG = "ServosActivity";
	
	private DstabiProvider stabiProvider;
	/**
	 * zavolani pri vytvoreni instance aktivity servos
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.servos);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.servos_button_text)));
        
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
	 * otevreni aktivity pro typ serva
	 * 
	 * @param v
	 */
	public void openServosTypeActivity(View v)
	{
		Intent i = new Intent(ServosActivity.this, ServosTypeActivity.class);
    	startActivity(i);
	}
	
	/**
	 * otevreni aktivity pro subtrim serva
	 * 
	 * @param v
	 */
	public void openServosSubtrimActivity(View v)
	{
		Intent i = new Intent(ServosActivity.this, ServosSubtrimActivity.class);
    	startActivity(i);
	}
	
	/**
	 * otevreni aktivity pro limit serva
	 * 
	 * @param v
	 */
	public void openServosLimitActivity(View v)
	{
		Intent i = new Intent(ServosActivity.this, ServosLimitActivity.class);
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
