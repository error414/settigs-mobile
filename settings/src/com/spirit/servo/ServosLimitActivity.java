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

package com.spirit.servo;

import java.util.ArrayList;
import java.util.HashMap;

import com.helpers.MenuListAdapter;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.spirit.R;
import com.spirit.BaseActivity;

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

public class ServosLimitActivity extends BaseActivity{
	
	private DstabiProvider stabiProvider;
	
	
	
	/**
	 * zavolani pri vytvoreni instance aktivity servos
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.servos_limit);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.servos_button_text), " \u2192 " , getString(R.string.limit)));
        
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		
		ListView menuList = (ListView) findViewById(R.id.listMenu);
		MenuListAdapter adapter = new MenuListAdapter(this, createArrayForMenuList());
		menuList.setAdapter(adapter);
		menuList.setOnItemClickListener(new OnItemClickListener() {
			@Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	
            	switch(position){
            		case 0://cyclic ring
            			openCyclicRingRange(view);
            			break;
            		case 1://rudder endpoints
            			openRudderEndPoints(view);
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
		//cyclic ring
		HashMap<Integer, Integer> ring = new HashMap<Integer, Integer>();
		ring.put(TITLE_FOR_MENU, R.string.cyclic_ring_range_no_break);
		ring.put(ICO_RESOURCE_ID, R.drawable.i12);
		menuListData.add(ring);
		
		//rudder endpoints
		HashMap<Integer, Integer> endpoints = new HashMap<Integer, Integer>();
		endpoints.put(TITLE_FOR_MENU, R.string.rudder_end_points_no_break);
		endpoints.put(ICO_RESOURCE_ID, R.drawable.i13);
		menuListData.add(endpoints);
		
		return menuListData;
	}
	
	/**
	 * otevreni aktivity pro typ serva
	 * 
	 * @param v
	 */
	public void openCyclicRingRange(View v)
	{
		Intent i = new Intent(ServosLimitActivity.this, ServosCyclickRingRangeActivity.class);
    	startActivity(i);
	}
	
	/**
	 * otevreni aktivity pro subtrim serva
	 * 
	 * @param v
	 */
	public void openRudderEndPoints(View v)
	{
		Intent i = new Intent(ServosLimitActivity.this, ServosRudderEndPointsActivity.class);
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
