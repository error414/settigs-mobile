package com.settings.stabi;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.helpers.MenuListAdapter;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.settings.BaseActivity;
import com.settings.R;

public class StabiActivity extends BaseActivity{
	@SuppressWarnings("unused")
	final private String TAG = "StabiActivity";
	
	private DstabiProvider stabiProvider;
	/**
	 * zavolani pri vytvoreni instance aktivity stabi
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.stabi);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.stabi_button_text)));
        
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		
		ListView menuList = (ListView) findViewById(R.id.listMenu);
		MenuListAdapter adapter = new MenuListAdapter(this, createArrayForMenuList());
		menuList.setAdapter(adapter);
		menuList.setOnItemClickListener(new OnItemClickListener() {
			@Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	
            	switch(position){
            		case 0://function
            			openStabiFunctionActivity(view);
            			break;
            		case 1://pitch
            			openStabiPitchActivity(view);
            			break;
            		/*case 2://
            			openSenzorRotationSpeedActivity(view);
            			break;*/
            	}
            }
		});
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
	 * vytvoreni pole pro adapter menu listu
	 * 
	 * tohle se bude vytvaret dynamicky z pole
	 * 
	 * @return
	 */
	public ArrayList<HashMap<Integer, Integer>> createArrayForMenuList(){
		ArrayList<HashMap<Integer, Integer>> menuListData = new ArrayList<HashMap<Integer, Integer>>();
		//function
		HashMap<Integer, Integer> function = new HashMap<Integer, Integer>();
		function.put(TITLE_FOR_MENU, R.string.stabi_function);
		function.put(ICO_RESOURCE_ID, R.drawable.na);
		menuListData.add(function);
		
		//pitch
		HashMap<Integer, Integer> pitch = new HashMap<Integer, Integer>();
		pitch.put(TITLE_FOR_MENU, R.string.stabi_pitch);
		pitch.put(ICO_RESOURCE_ID, R.drawable.na);
		menuListData.add(pitch);
		
		//rotation speed
		/*HashMap<Integer, Integer> rotation = new HashMap<Integer, Integer>();
		rotation.put(TITLE_FOR_MENU, R.string.rotation_speed);
		rotation.put(ICO_RESOURCE_ID, R.drawable.i18);
		menuListData.add(rotation);*/
		
		return menuListData;
	}
	
	/**
	 * otevreni aktivity senzivitu senzoru
	 * 
	 * @param v
	 */
	public void openStabiFunctionActivity(View v)
	{
		Intent i = new Intent(StabiActivity.this, StabiFunctionActivity.class);
    	startActivity(i);
	}
	
	/**
	 * otevreni aktivity reverz senzoru
	 * 
	 * @param v
	 */
	public void openStabiPitchActivity(View v)
	{
		Intent i = new Intent(StabiActivity.this, StabiPitchActivity.class);
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
