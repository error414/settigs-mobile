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

package com.spirit.stabi;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
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

public class StabiFunctionActivity extends BaseActivity
{

	final private String TAG = "StabiFunctionActivity";

	final private int PROFILE_CALL_BACK_CODE = 16;

	private final String protocolCode[] = {"ALT_FUNCTION",};

	private int formItems[] = {R.id.function_select_id};

	private int lock = formItems.length;

	/**
	 * zavolani pri vytvoreni instance aktivity servo type
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		initSlideMenu(R.layout.stabi_function);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.stabi_button_text), " \u2192 ", getString(R.string.stabi_function)));

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
	 * prvotni konfigurace view
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
	 * ziskani profilu z jednotky
	 */
	private void initConfiguration()
	{
		showDialogRead();
		// ziskani konfigurace z jednotky
		stabiProvider.getProfile(PROFILE_CALL_BACK_CODE);
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

				int pos = profileCreator.getProfileItemByName(protocolCode[i]).getValueForSpinner(tempSpinner.getCount());

				if (pos != tempSpinner.getSelectedItemPosition()) lock = lock + 1;
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
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
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

                    if(i == 0 && pos != 0){ // i == 0 je stabi function a pokud se vybere nejaka jina moznost nez vypnuto tak zobrazit upozorneni
                        showConfirmDialog(R.string.stabi_piruete_warning);
                    }

					showInfoBarWrite();
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
					sendInSuccessDialog();
                    initDefaultValue();
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

