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

package com.spirit.heli.servo;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.exception.IndexOutOfException;
import com.google.analytics.tracking.android.EasyTracker;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.spirit.BaseActivity;
import com.spirit.R;

public class ServosTypeActivity extends BaseActivity
{

	@SuppressWarnings("unused")
	final private String TAG = "ServosTypeActivity";

	final private int PROFILE_CALL_BACK_CODE = 16;

	private final String protocolCode[] = {"CYCLIC_TYPE", "CYCLIC_FREQ", "RUDDER_TYPE", "RUDDER_FREQ"};

	private int formItems[] = {R.id.cyclic_pulse, R.id.cyclic_frequency, R.id.rudder_pulse, R.id.rudder_frequency};

	private int lock = formItems.length;

	/**
	 * zavolani pri vytvoreni instance aktivity servo type
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		initSlideMenu(R.layout.servos_type);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.servos_button_text), " \u2192 ", getString(R.string.type)));

		initConfiguration();
		delegateListener();
	}

    /**
     *
     * @return
     */
    public int[] getFormItems() {
        return formItems;
    }

    /**
     *
     * @return
     */
    public String[] getProtocolCode() {
        return protocolCode;
    }

    /**
     *
     */
    protected int getDefaultValueType(){
        return DEFAULT_VALUE_TYPE_SPINNER;
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
            initDefaultValue();
		} else {
			finish();
		}
	}

	/**
	 * disablovani prvku v bezpecnem rezimu
	 */
	protected void initBasicMode()
	{
		for (int i = 0; i < formItems.length; i++) {
			Spinner spinner = (Spinner) findViewById(formItems[i]);
			ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
			
			spinner.setEnabled(!(getAppBasicMode() && item.isDeactiveInBasicMode()));
		}
	}

	/**
	 * prirazeni udalosti k prvkum
	 */
	private void delegateListener()
	{
		//nastaveni posluchacu pro formularove prvky
		for (int i = 0; i < formItems.length; i++) {
			((Spinner) findViewById(formItems[i])).setOnItemSelectedListener(spinnerListener);
		}
	}

	/**
	 * prvotni konfigurace view
	 */
	private void initConfiguration()
	{
		showDialogRead();
		// ziskani konfigurace z jednotky
		stabiProvider.getProfile(PROFILE_CALL_BACK_CODE);
	}

	/**
	 * pri zmenene rudder pulse se podvame jestli neni potreba zmenit i select
	 * rudder frequency
	 *
	 * @param pos
	 */
	private void updateItemRudderFrequency(int pos)
	{
		ArrayAdapter<?> adapter;
		Spinner rudderFrequency = (Spinner) findViewById(R.id.rudder_frequency);
		int freqPos = (int) rudderFrequency.getSelectedItemPosition();
		if (pos == 2) {
			adapter = ArrayAdapter.createFromResource(this, R.array.rudder_frequency_value_extend, android.R.layout.simple_spinner_item);
		} else {
			adapter = ArrayAdapter.createFromResource(this, R.array.rudder_frequency_value, android.R.layout.simple_spinner_item);
		}

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		rudderFrequency.setAdapter(adapter);
		rudderFrequency.setSelection(Math.min(freqPos, adapter.getCount() - 1));

	}

	/**
	 * naplneni formulare
	 *
	 * @param profile
	 */
	private void initGuiByProfileString(byte[] profile)
	{
		profileCreator = new DstabiProfile(profile);

		if (!profileCreator.isValid()) {
			errorInActivity(R.string.damage_profile);
			return;
		}

		checkBankNumber(profileCreator);
		initBasicMode();

		try {
			for (int i = 0; i < formItems.length; i++) {
				Spinner tempSpinner = (Spinner) findViewById(formItems[i]);

				//TOHLE MUSIM VYRESIT LIP
				if (tempSpinner.getId() == formItems[2] && profileCreator.getProfileItemByName(protocolCode[i]).getValueForSpinner(tempSpinner.getCount()) == 2) {
                    ArrayAdapter<?> adapter;
                    Spinner rudderFrequency = (Spinner) findViewById(R.id.rudder_frequency);
                    adapter = ArrayAdapter.createFromResource(this, R.array.rudder_frequency_value_extend, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    rudderFrequency.setAdapter(adapter);
				}

				int pos = profileCreator.getProfileItemByName(protocolCode[i]).getValueForSpinner(tempSpinner.getCount());

				if (pos != tempSpinner.getSelectedItemPosition()) {
					lock = lock + 1;
				}
				tempSpinner.setSelection(pos);
			}
		} catch (IndexOutOfException e) {
			errorInActivity(R.string.damage_profile);
			return;
		}
	}

	protected OnItemSelectedListener spinnerListener = new OnItemSelectedListener()
	{
		@Override
		synchronized public void  onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			if (lock != 0) {
				lock -= 1;
				return;
			}
			lock = Math.max(lock - 1, 0);

			// prohledani jestli udalost vyvolal znamy prvek
			// pokud prvek najdeme vyhledame si k prvku jeho protkolovy kod a odesleme
			for (int i = 0; i < formItems.length; i++) {
				if (parent.getId() == formItems[i]) {
					ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
					item.setValueFromSpinner(pos);

					stabiProvider.sendDataNoWaitForResponce(item);

					showInfoBarWrite();

					//pro prvrk rudder pulse volame jeste obsluhu zmeny seznamu rudder frequency
					if (parent.getId() == formItems[2]) {
						updateItemRudderFrequency(pos);
					}
				}
			}
            initDefaultValue();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0)
		{
			// TODO Auto-generated method stub

		}
	};


	public boolean handleMessage(Message msg)
	{
		switch (msg.what) {
			case PROFILE_CALL_BACK_CODE:
				if (msg.getData().containsKey("data")) {
					initGuiByProfileString(msg.getData().getByteArray("data"));
                    initDefaultValue();
					sendInSuccessDialog();
				}
				break;
			case BANK_CHANGE_CALL_BACK_CODE:
				initConfiguration();
				super.handleMessage(msg);
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
