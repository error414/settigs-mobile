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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * aktivita pro obrazku favourites
 *
 * @author error414
 */
public class FavouritesActivity extends BaseActivity
{

	@SuppressWarnings("unused")
	final private String TAG = "FavouritesActivity";

	/**
	 * seznam polozek pro menu
	 */
	protected Integer[] menuListIndex;

	private MenuListAdapter adapter;

	/**
	 * zavolani pri vytvoreni instance aktivity settings
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		initSlideMenu(R.layout.favourites);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.favourites_button_text)));

		//naplnime seznam polozek pro menu
		SharedPreferences prefs = getSharedPreferences(PREF_FAVOURITES, Context.MODE_PRIVATE);
		Map<String, ?> keys = prefs.getAll();

		menuListIndex = new Integer[keys.size()];
		int i = 0;
		for (Map.Entry<String, ?> entry : keys.entrySet()) {
			menuListIndex[i++] = Integer.parseInt(entry.getValue().toString());
		}

		////////////////////////////////////////////////////////////////////////
		ListView menuList = (ListView) findViewById(R.id.listMenu);
		adapter = new MenuListAdapter(this, createArrayForMenuList());
		menuList.setAdapter(adapter);

		menuList.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED) {
					Toast.makeText(getApplicationContext(), R.string.must_first_connect_to_device, Toast.LENGTH_SHORT).show();
					return;
				}

				Intent i = new Intent(FavouritesActivity.this, Menu.getInstance().getItem(menuListIndex[position]).getActivity());
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

				if (prefs.getAll().containsKey(String.valueOf(menuListIndex[position]))) {
					new AlertDialog.Builder(FavouritesActivity.this).setTitle(R.string.remove_from_favourites).setMessage(Menu.getInstance().getItem(menuListIndex[position]).getTitle()).setPositiveButton(R.string.remove, new DialogInterface.OnClickListener()
					{

						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							editor.remove(String.valueOf(menuListIndex[position]));
							Toast.makeText(getApplicationContext(), R.string.remove_from_favourites_done, Toast.LENGTH_SHORT).show();
							editor.commit();

							updateListView();
						}

					}).setNegativeButton(R.string.cancel, null).show();
				}

				return true;
			}
		});
	}

	public void onResume()
	{
		super.onResume();
		if (stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED) {
			((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
			updateListView();
		}else{
            ((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.red);
        }
	}

	private void updateListView()
	{
		SharedPreferences prefs = getSharedPreferences(PREF_FAVOURITES, Context.MODE_PRIVATE);
		Map<String, ?> keys = prefs.getAll();

		menuListIndex = new Integer[keys.size()];
		int i = 0;
		for (Map.Entry<String, ?> entry : keys.entrySet()) {
			menuListIndex[i++] = Integer.parseInt(entry.getValue().toString());
		}

		adapter.setData(createArrayForMenuList());
		adapter.notifyDataSetChanged();
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

	public boolean handleMessage(Message msg)
	{
		switch (msg.what) {
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
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}