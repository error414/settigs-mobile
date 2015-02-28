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

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.helpers.ByteOperation;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.spirit.BaseActivity;
import com.spirit.R;

public class PiroOptimalizationActivity extends BaseActivity
{

	@SuppressWarnings("unused")
	final private String TAG = "PiroOptimalizationActivity";

	final private int PROFILE_CALL_BACK_CODE = 16;

	private final String protocolCode[] = {"PIRO_OPT",};

	private int formItems[] = {R.id.piro_opt,};

	private int lock = 0;

	/**
	 * zavolani pri vytvoreni instance aktivity servo type
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		initSlideMenu(R.layout.advanced_piro_opt);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat("... \u2192 ", getString(R.string.piro_opt)));

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
        return DEFAULT_VALUE_TYPE_CHECKBOX;
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
	 * prirazeni udalosti k prvkum
	 */
	private void delegateListener()
	{
		//nastaveni posluchacu pro formularove prvky
		for (int i = 0; i < formItems.length; i++) {
			((CheckBox) findViewById(formItems[i])).setOnCheckedChangeListener(checkboxListener);
		}
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

			if (!getAppBasicMode()) {
				stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0x02)); //povoleni nastaveni optimalizace
			}
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
			CheckBox check = (CheckBox) findViewById(formItems[i]);
			ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
			
			check.setEnabled(!(getAppBasicMode() && item.isDeactiveInBasicMode()));
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (!getAppBasicMode()) {
			stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0xff)); //zakazani nastaveni optimalizace
		}
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

		for (int i = 0; i < formItems.length; i++) {
			CheckBox tempCheckbox = (CheckBox) findViewById(formItems[i]);

			Boolean checked = profileCreator.getProfileItemByName(protocolCode[i]).getValueForCheckBox();
			if (checked) lock = lock + 1;
			tempCheckbox.setChecked(checked);
		}
	}


	private OnCheckedChangeListener checkboxListener = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{

			if (lock != 0) {
				lock -= 1;
				return;
			}
			lock = Math.max(lock - 1, 0);

			// TODO Auto-generated method stub
			// prohledani jestli udalost vyvolal znamy prvek
			// pokud prvek najdeme vyhledame si k prvku jeho protkolovy kod a odesleme
			for (int i = 0; i < formItems.length; i++) {
				if (buttonView.getId() == formItems[i]) {
					ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
					item.setValueFromCheckBox(isChecked);
					stabiProvider.sendDataNoWaitForResponce(item);

					showInfoBarWrite();
				}
			}
            initDefaultValue();

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
    public void onStop()
    {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}
