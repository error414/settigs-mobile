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

package com.spirit.governor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.helpers.MenuListAdapter;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.lib.menu.Menu;
import com.spirit.BaseActivity;
import com.spirit.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * aktivita pro hlavni obrazku
 *
 * @author error414
 */
public class GovernorActivity extends BaseActivity
{

	@SuppressWarnings("unused")
	final private String TAG = "SettingsActivity";

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

		//setContentView(R.layout.main);
        initSlideMenu(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
        ((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.governor)));

		//naplnime seznam polozek pro menu
		menuListIndex = Menu.getInstance().getItemForGroup(Menu.MENU_INDEX_GOVERNOR);


		ListView menuList = (ListView) findViewById(R.id.listMenu);
		MenuListAdapter adapter = new MenuListAdapter(this, createArrayForMenuList());
		menuList.setAdapter(adapter);
		menuList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (position != 0 && position != 1) { // jen u connection coz musi byt prvni nekontrolujeme jestli je zarizeni pripojene a u favourites
					if (stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED) {
						Toast.makeText(getApplicationContext(), R.string.must_first_connect_to_device, Toast.LENGTH_SHORT).show();
						return;
					}
				}

				Intent i = new Intent(GovernorActivity.this, Menu.getInstance().getItem(menuListIndex[position]).getActivity());
				startActivity(i);
			}
		});

        menuList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id)
            {

                final SharedPreferences prefs = getSharedPreferences(PREF_FAVOURITES, Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = prefs.edit();

                new AlertDialog.Builder(GovernorActivity.this).setTitle(prefs.getAll().containsKey(String.valueOf(menuListIndex[position])) ? R.string.remove_from_favourites : R.string.add_to_favourites).setMessage(Menu.getInstance().getItem(menuListIndex[position]).getTitle()).setPositiveButton(prefs.getAll().containsKey(String.valueOf(menuListIndex[position])) ? R.string.remove : R.string.add, new DialogInterface.OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (prefs.getAll().containsKey(String.valueOf(menuListIndex[position]))) {
                            editor.remove(String.valueOf(menuListIndex[position]));
                            Toast.makeText(getApplicationContext(), R.string.remove_from_favourites_done, Toast.LENGTH_SHORT).show();
                        } else {
                            editor.putInt(String.valueOf(menuListIndex[position]), menuListIndex[position]);
                            Toast.makeText(getApplicationContext(), R.string.add_to_favourites_done, Toast.LENGTH_SHORT).show();
                        }
                        editor.commit();
                    }

                }).setNegativeButton(R.string.cancel, null).show();

                return true;
            }
        });
	}

	public void onResume()
	{
		super.onResume();
		if (stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED) {
			((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
		} else {
			((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.red);
		}
	}
	
	/**
	 * vytvoreni pole pro adapter menu listu
	 * <p/>
	 * tohle se bude vytvaret dynamicky z pole
	 *
	 * @return
	 */
	@SuppressLint("UseSparseArrays")
	public ArrayList<HashMap<Integer, Integer>> createArrayForMenuList()
	{
		ArrayList<HashMap<Integer, Integer>> menuListData = new ArrayList<HashMap<Integer, Integer>>();

		// vytvorime pole pro adapter
		for (Integer key : menuListIndex) {
			HashMap<Integer, Integer> item = new HashMap<Integer, Integer>();
			item.put(Menu.TITLE_FOR_MENU, Menu.getInstance().getItem(key).getTitle());
			item.put(Menu.ICO_RESOURCE_ID, Menu.getInstance().getItem(key).getIcon());
			menuListData.add(item);
		}

		return menuListData;
	}

	/**
	 * obsluha callbacku
	 *
	 * @param msg
	 * @return
	 */
	public boolean handleMessage(Message msg)
	{
		switch (msg.what) {
			case DstabiProvider.MESSAGE_SEND_COMAND_ERROR:
				sendInError(false);
				break;
			case DstabiProvider.MESSAGE_SEND_COMPLETE:
				sendInSuccessInfo();
				break;
			case DstabiProvider.MESSAGE_STATE_CHANGE:
				if (stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED) {
					sendInError(false);
					((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.red);
				} else {
					((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
				}
				break;
			default:
				super.handleMessage(msg);

		}
		return true;
	}

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}