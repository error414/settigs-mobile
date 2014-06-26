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

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.exception.ProfileNotValidException;
import com.helpers.DiffListAdapter;
import com.helpers.DstabiProfile;
import com.lib.BluetoothCommandService;
import com.lib.ChangeInProfile;
import com.lib.DstabiProvider;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author error414
 */
public class DiffActivity extends BaseActivity
{
	@SuppressWarnings("unused")
	final private String TAG = "DiffActivity";

	final static private int PROFILE_CALL_BACK_CODE = 101;

	private DiffListAdapter adapter;

	private ArrayList<HashMap<Integer, String>> diffListData;

	/**
	 * zavolani pri vytvoreni instance aktivity settings
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.profile_diff);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.profile_diff)));

		////////////////////////////////////////////////////////////////////////
		ListView diffList = (ListView) findViewById(R.id.listMenu);
		adapter = new DiffListAdapter(this, new ArrayList<HashMap<Integer, String>>());
		diffList.setAdapter(adapter);

		//dame pozadavek na ziskani profilu z jednotky
		initConfiguration();
	}

	/**
	 *
	 */
	protected void initConfiguration()
	{
		showDialogRead();
		// ziskani konfigurace z jednotky
		stabiProvider.getProfile(PROFILE_CALL_BACK_CODE);
	}

	/**
	 *
	 * @param profile
	 */
	protected void updateGui(byte[] profile){
		DstabiProfile changedProfile = new DstabiProfile(profile);

		diffListData = new ArrayList<HashMap<Integer, String>>();


		try {
			for(ChangeInProfile.DiffItem diffItem : ChangeInProfile.getInstance().getDiff(changedProfile)) {
				HashMap<Integer, String> row = new HashMap<Integer, String>();
				row.put(DiffListAdapter.NAME, diffItem.getLabel());
				row.put(DiffListAdapter.FROM, diffItem.getOriginalValue().getValueString());
				row.put(DiffListAdapter.TO,   diffItem.getChangedValue().getValueString());
				diffListData.add(row);
			}

			adapter.setData(diffListData);
			adapter.notifyDataSetChanged();

		} catch (ProfileNotValidException e) {
			e.printStackTrace();
		}
	}

	public void onResume()
	{
		super.onResume();
		if (stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED) {
			((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
		}
	}

	/**
	 *
	 * @param msg
	 * @return
	 */
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
			case DiffActivity.PROFILE_CALL_BACK_CODE:
				sendInSuccessDialog();
				if (msg.getData().containsKey("data")) {
					updateGui(msg.getData().getByteArray("data"));
				}
				break;

			default:
				super.handleMessage(msg);
		}
		return true;
	}
}