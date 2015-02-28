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
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;

public class AuthorActivity extends BaseActivity
{

	@SuppressWarnings("unused")
	final private String TAG = "AuthorActivity";

	/**
	 * zavolani pri vytvoreni instance aktivity settings
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		initSlideMenu(R.layout.author);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(getText(R.string.full_app_name));
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

	// The Handler that gets information back from the
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

    /**
     * vytvoreni kontextoveho menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return false;
    }

    /**
     * reakce na kliknuti polozky v kontextovem menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return false;
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
