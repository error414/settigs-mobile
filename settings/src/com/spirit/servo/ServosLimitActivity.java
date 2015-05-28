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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.lib.menu.Menu;
import com.spirit.BaseActivity;
import com.spirit.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ServosLimitActivity extends BaseActivity
{

	/**
	 * seznam polozek pro menu
	 */
	protected Integer[] menuListIndex;

	/**
	 * zavolani pri vytvoreni instance aktivity servos
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		initSlideMenu(R.layout.servos_limit);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.limits)));

		//naplnime seznam polozek pro menu
		menuListIndex = Menu.getInstance().getItemForGroup(Menu.MENU_INDEX_SERVOLIMIT);

		ListView menuList = (ListView) findViewById(R.id.listMenu);
		MenuListAdapter adapter = new MenuListAdapter(this, createArrayForMenuList());
		menuList.setAdapter(adapter);
		menuList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{

				Intent i = new Intent(ServosLimitActivity.this, Menu.getInstance().getItem(menuListIndex[position]).getActivity());
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

				new AlertDialog.Builder(ServosLimitActivity.this).setTitle(prefs.getAll().containsKey(String.valueOf(menuListIndex[position])) ? R.string.remove_from_favourites : R.string.add_to_favourites).setMessage(Menu.getInstance().getItem(menuListIndex[position]).getTitle()).setPositiveButton(prefs.getAll().containsKey(String.valueOf(menuListIndex[position])) ? R.string.remove : R.string.add, new DialogInterface.OnClickListener()
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

	/**
	 * znovu nacteni aktovity, priradime dstabi svuj handler a zkontrolujeme jestli sme pripojeni
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		if (stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED) {
			((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
		} else {
			finish();
		}
	}

	/**
	 * vytvoreni pole pro adapter menu listu
	 * <p/>
	 * tohle se bude vytvaret dynamicky z pole
	 *
	 * @return
	 */
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
