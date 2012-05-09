package com.settings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;

public class AuthorActivity extends BaseActivity{

	final private String TAG = "AuthorActivity";
	
	private DstabiProvider stabiProvider;
	
	/**
	 * zavolani pri vytvoreni instance aktivity settings
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.author);
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
	
	// The Handler that gets information back from the 
	 	 private final Handler connectionHandler = new Handler() {
	 	        @Override
	 	        public void handleMessage(Message msg) {
	 	        	switch(msg.what){
	 	        		case DstabiProvider.MESSAGE_STATE_CHANGE:
	 						if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
	 							((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.red);
	 						}else{
	 							((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
	 						}
	 						break;
	 	        	}
	 	        }
	 	    };
	
}
