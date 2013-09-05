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

package com.spirit.advanced;

import java.util.ArrayList;
import java.util.HashMap;
import android.annotation.SuppressLint;
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

public class AdvancedActivity extends BaseActivity{
	@SuppressWarnings("unused")
	final private String TAG = "AdvancedActivity";
	
	private DstabiProvider stabiProvider;
	/**
	 * zavolani pri vytvoreni instance aktivity servos
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.advanced);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.advanced_button_text)));
        
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		
		ListView menuList = (ListView) findViewById(R.id.listMenu);
		MenuListAdapter adapter = new MenuListAdapter(this, createArrayForMenuList());
		menuList.setAdapter(adapter);
		menuList.setOnItemClickListener(new OnItemClickListener() {
			 
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	
            	switch(position){
            		case 0://deadband
            			openStickDeadBandActivity(view);
            			break;
            		case 1://geometry angle
            			openGeometryAngleActivity(view);
            			break;
            		case 2://pirouette optimization
            			openPiroOptimalizationActivity(view);
            			break;
            		case 3://rudder delay
            			openRudderDelayActivity(view);
            			break;
            		case 4://pirouette const
            			openPirouetteConsistencyActivity(view);
            			break;
            		case 5://rudder dynamic
            			openRudderDynamicActivity(view);
            			break;
            		case 6://rudder revomix
            			openRudderRevomixActivity(view);
            			break;
            		case 7://elevator filter
            			openEFilterActivity(view);
            			break;
            		case 8://elevator pitchup
            			openPitchupActivity(view);
            			break;
            		case 9://cyclic phase
            			openCyclicPhaseActivity(view);
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
	@SuppressLint("UseSparseArrays")
	public ArrayList<HashMap<Integer, Integer>> createArrayForMenuList(){
		ArrayList<HashMap<Integer, Integer>> menuListData = new ArrayList<HashMap<Integer, Integer>>();
	
		//stick deadband
		HashMap<Integer, Integer> deadband = new HashMap<Integer, Integer>();
		deadband.put(TITLE_FOR_MENU, R.string.stick_deadband);
		deadband.put(ICO_RESOURCE_ID, R.drawable.i22);
		menuListData.add(deadband);
		
		//geometry 6deg
		HashMap<Integer, Integer> geom_6deg = new HashMap<Integer, Integer>();
		geom_6deg.put(TITLE_FOR_MENU, R.string.geom_6deg);
		geom_6deg.put(ICO_RESOURCE_ID, R.drawable.na);
		menuListData.add(geom_6deg);
		
		//piruette opt
		HashMap<Integer, Integer> piro = new HashMap<Integer, Integer>();
		piro.put(TITLE_FOR_MENU, R.string.piro_opt);
		piro.put(ICO_RESOURCE_ID, R.drawable.i26);
		menuListData.add(piro);
		
		//rudder delay
		HashMap<Integer, Integer> rudder_delay = new HashMap<Integer, Integer>();
		rudder_delay.put(TITLE_FOR_MENU, R.string.rudder_delay);
		rudder_delay.put(ICO_RESOURCE_ID, R.drawable.na);
		menuListData.add(rudder_delay);
		
		//piruette const
		HashMap<Integer, Integer> piro_const = new HashMap<Integer, Integer>();
		piro_const.put(TITLE_FOR_MENU, R.string.pirouette_consistency);
		piro_const.put(ICO_RESOURCE_ID, R.drawable.i36);
		menuListData.add(piro_const);
		
		//rudder dynamic
		HashMap<Integer, Integer> rudder_dynamic = new HashMap<Integer, Integer>();
		rudder_dynamic.put(TITLE_FOR_MENU, R.string.rudder_dynamic);
		rudder_dynamic.put(ICO_RESOURCE_ID, R.drawable.i23);
		menuListData.add(rudder_dynamic);
		
		//rudder revomix
		HashMap<Integer, Integer> rudder_revomix = new HashMap<Integer, Integer>();
		rudder_revomix.put(TITLE_FOR_MENU, R.string.rudder_revomix);
		rudder_revomix.put(ICO_RESOURCE_ID, R.drawable.i24);
		menuListData.add(rudder_revomix);
		
		//elevator filter
		HashMap<Integer, Integer> e_filter = new HashMap<Integer, Integer>();
		e_filter.put(TITLE_FOR_MENU, R.string.e_filter);
		e_filter.put(ICO_RESOURCE_ID, R.drawable.i33);
		menuListData.add(e_filter);
		
		//elevator pitchup
		HashMap<Integer, Integer> pitchup = new HashMap<Integer, Integer>();
		pitchup.put(TITLE_FOR_MENU, R.string.pitchup);
		pitchup.put(ICO_RESOURCE_ID, R.drawable.i33);
		menuListData.add(pitchup);
		
		//cyclic phase
		HashMap<Integer, Integer> cyclic_phase = new HashMap<Integer, Integer>();
		cyclic_phase.put(TITLE_FOR_MENU, R.string.cyclic_phase);
		cyclic_phase.put(ICO_RESOURCE_ID, R.drawable.na);
		menuListData.add(cyclic_phase);
	
		return menuListData;
	}
	
	/**
	 * 
	 * 
	 * @param v
	 */
	public void openStickDeadBandActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, StickDeadBandActivity.class);
    	startActivity(i);
	}
	

	/**
	 * 
	 * 
	 * @param v
	 */
	public void openGeometryAngleActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, GeometryAngleActivity.class);
    	startActivity(i);
	}
	
	/**
	 * 
	 * 
	 * @param v
	 */
	public void openRudderDelayActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, RudderDelayActivity.class);
    	startActivity(i);
	}

	/**
	 * 
	 * 
	 * @param v
	 */
	public void openPiroOptimalizationActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, PiroOptimalizationActivity.class);
		startActivity(i);
	}
	
	/**
	 * 
	 * 
	 * @param v
	 */
	public void openRudderDynamicActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, RudderDynamicActivity.class);
		startActivity(i);
	}
	
	/**
	 * 
	 * 
	 * @param v
	 */
	public void openRudderRevomixActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, RudderRevomixActivity.class);
		startActivity(i);
	}
	
	/**
	 * 
	 * 
	 * @param v
	 */
	public void openPirouetteConsistencyActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, PirouetteConsistencyActivity.class);
		startActivity(i);
	}

	/**
	 * 
	 * 
	 * @param v
	 */
	public void openEFilterActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, EFilterActivity.class);
		startActivity(i);
	}
	
	/**
	 * 
	 * 
	 * @param v
	 */
	public void openPitchupActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, PitchupActivity.class);
		startActivity(i);
	}
	
	/**
	 * 
	 * 
	 * @param v
	 */
	public void openCyclicPhaseActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, CyclicPhaseActivity.class);
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
