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

package com.spirit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.helpers.MenuListAdapter;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.lib.menu.Menu;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
	@SuppressWarnings("unused")
	final private String TAG = "SettingsActivity";
	
	private DstabiProvider stabiProvider;

    /**
     * seznam polozek pro menu
     */
    protected Integer[] menuListIndex;
	
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

        //naplnime seznam polozek pro menu
        menuListIndex = Menu.getInstance().getItemForGroup(Menu.MENU_INDEX_SETTINGS);

		
		ListView menuList = (ListView) findViewById(R.id.listMenu);
		MenuListAdapter adapter = new MenuListAdapter(this, createArrayForMenuList());
		menuList.setAdapter(adapter);
		menuList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(position != 0){ // jen u connection coz musi byt prvni nekontrolujeme jestli je zarizeni pripojene
                    if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
                        Toast.makeText(getApplicationContext(), R.string.must_first_connect_to_device, Toast.LENGTH_SHORT).show();
                        return ;
                    }
                }

                Intent i = new Intent(SettingsActivity.this,  Menu.getInstance().getItem(menuListIndex[position]).getActivity());
                startActivity(i);
            }
        });

        menuList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {

                    final SharedPreferences prefs = getSharedPreferences(PREF_FAVOURITES, Context.MODE_PRIVATE);
                    final SharedPreferences.Editor editor = prefs.edit();

                    new AlertDialog.Builder(SettingsActivity.this)
                            .setTitle("Favourites")
                            .setMessage(prefs.getAll().containsKey(String.valueOf(menuListIndex[position])) ? "odebrat" : "pridat")
                            .setPositiveButton("ano", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (prefs.getAll().containsKey(String.valueOf(menuListIndex[position]))) {
                                        editor.remove(String.valueOf(menuListIndex[position]));
                                        Toast.makeText(getApplicationContext(), "odebrano z oblibenych", Toast.LENGTH_SHORT).show();
                                    } else {
                                        editor.putInt(String.valueOf(menuListIndex[position]), menuListIndex[position]);
                                        Toast.makeText(getApplicationContext(), "pridano do oblibenych", Toast.LENGTH_SHORT).show();
                                    }
                                    editor.commit();

                                    Map<String,?> keys = prefs.getAll();

                                    for (Map.Entry<String, ?> entry : keys.entrySet()) {
                                        Log.d("map values", entry.getKey() + ": " +
                                                entry.getValue().toString());
                                    }


                                }

                            })
                            .setNegativeButton("ne", null)
                            .show();

                    return true;
                }
            }
        );
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
	@SuppressLint("UseSparseArrays")
	public ArrayList<HashMap<Integer, Integer>> createArrayForMenuList(){
		ArrayList<HashMap<Integer, Integer>> menuListData = new ArrayList<HashMap<Integer, Integer>>();

        // vytvorime pole pro adapter
        for(Integer key : menuListIndex){
            HashMap<Integer, Integer> item = new HashMap<Integer, Integer>();
            item.put(Menu.TITLE_FOR_MENU,  Menu.getInstance().getItem(key).getTitle());
            item.put(Menu.ICO_RESOURCE_ID, Menu.getInstance().getItem(key).getIcon());
            menuListData.add(item);
        }

		return menuListData;
	}
	
 // The Handler that gets information back from the
 	 private final Handler connectionHandler = new Handler(new Handler.Callback() {
	 	    @Override
	 	    public boolean handleMessage(Message msg) {
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
 	        	return true;
 	        }
 	    });
}