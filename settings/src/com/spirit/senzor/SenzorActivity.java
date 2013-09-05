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

package com.spirit.senzor;

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
import com.spirit.R;
import com.spirit.BaseActivity;

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
		
		ListView menuList = (ListView) findViewById(R.id.listMenu);
		MenuListAdapter adapter = new MenuListAdapter(this, createArrayForMenuList());
		menuList.setAdapter(adapter);
		menuList.setOnItemClickListener(new OnItemClickListener() {
			@Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	
            	switch(position){
            		case 0://senzivity
            			openSenzorSenzivityActivity(view);
            			break;
            		case 1://reverse
            			openSenzorReverseActivity(view);
            			break;
            		case 2://rotation speed
            			openSenzorRotationSpeedActivity(view);
            			break;
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
		//senzivity
		HashMap<Integer, Integer> senzivity = new HashMap<Integer, Integer>();
		senzivity.put(TITLE_FOR_MENU, R.string.senzivity);
		senzivity.put(ICO_RESOURCE_ID, R.drawable.i16);
		menuListData.add(senzivity);
		
		//reverse
		HashMap<Integer, Integer> reverse = new HashMap<Integer, Integer>();
		reverse.put(TITLE_FOR_MENU, R.string.reverse);
		reverse.put(ICO_RESOURCE_ID, R.drawable.i17);
		menuListData.add(reverse);
		
		//rotation speed
		HashMap<Integer, Integer> rotation = new HashMap<Integer, Integer>();
		rotation.put(TITLE_FOR_MENU, R.string.rotation_speed);
		rotation.put(ICO_RESOURCE_ID, R.drawable.i18);
		menuListData.add(rotation);
		
		return menuListData;
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
	 * otevreni aktivity rotation speed senzoru
	 * 
	 * @param v
	 */
	public void openSenzorRotationSpeedActivity(View v)
	{
		Intent i = new Intent(SenzorActivity.this, SenzorRotationSpeedActivity.class);
		startActivity(i);
	}
	
	
	
	// The Handler that gets information back from the 
	 private final Handler connectionHandler = new Handler(new Handler.Callback() {
		    @Override
		    public boolean handleMessage(Message msg) {
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
	        	return true;
	        }
	    });
}
