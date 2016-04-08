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

package com.spirit.aero.senzor;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.customWidget.picker.ProgresEx;
import com.customWidget.picker.ProgresEx.OnChangedListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.CommandService;
import com.lib.translate.StabiSenzivityXProgressExTranslate;
import com.spirit.BaseActivity;
import com.spirit.R;

public class SenzorSenzivityActivity extends BaseActivity
{

	final private String TAG = "SenzorSenzivityActivity";

	final private int PROFILE_CALL_BACK_CODE = 16;

	private final String protocolCode[] = {"AERO_SENSOR_SENX", "AERO_SENSOR_SENY", "AERO_SENSOR_SENZ"
			//"SENSOR_RUDDER_COMMON_GAIN",
	};

	private int formItems[] = {R.id.sen_x, R.id.sen_y,  R.id.sen_z,
			//R.id.z_yaw,
	};

	private int formItemsTitle[] = {R.string.diag_aileron, R.string.diag_elevator,  R.string.diag_rudder,
			//R.string.z_yaw,
	};

	/**
	 * zavolani pri vytvoreni instance aktivity servo type
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		initSlideMenu(R.layout.aero_senzor_senzivity);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.senzor_button_text), " \u2192 ", getString(R.string.senzivity)));

		initGui();
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
		return DEFAULT_VALUE_TYPE_SEEK;
	}

	/**
	 * znovu nacteni aktovity, priradime dstabi svuj handler a zkontrolujeme jestli sme pripojeni
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		if (stabiProvider.getState() == CommandService.STATE_CONNECTED) {
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
			ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
			ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);

			tempPicker.setEnabled(!(getAppBasicMode() && item.isDeactiveInBasicMode()));
		}
	}

	private void initGui()
	{
		for (int i = 0; i < formItems.length; i++) {
			ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
            tempPicker.setTranslate(new StabiSenzivityXProgressExTranslate());
            tempPicker.setRange(0, 80); // nastavuji rozmezi prvku z profilu
			tempPicker.setTitle(formItemsTitle[i]); // nastavime popisek
		}
	}

	/**
	 * prirazeni udalosti k prvkum
	 */
	private void delegateListener()
	{
		//nastaveni posluchacu pro formularove prvky
		for (int i = 0; i < formItems.length; i++) {
			((ProgresEx) findViewById(formItems[i])).setOnChangeListener(numberPicekrListener);
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
			if (profileCreator.exits(protocolCode[i])) {
				ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
				ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
                tempPicker.setRange(item.getMinimum(), item.getMaximum()); // nastavuji rozmezi prvku z profilu
				tempPicker.setCurrentNoNotify(item.getValueInteger());
			}
		}

		 /*if(profileCreator.getProfileItemByName("MODEL").getValueInteger() != 67){ // letadlo
			 ProgresEx tempPicker = (ProgresEx) findViewById(formItems[1]);
			 tempPicker.setVisibility(View.GONE);
		 }*/

	}

	protected OnChangedListener numberPicekrListener = new OnChangedListener()
	{


		@Override
		public void onChanged(ProgresEx parent, int newVal)
		{
			// TODO Auto-generated method stub
			// prohledani jestli udalost vyvolal znamy prvek
			// pokud prvek najdeme vyhledame si k prvku jeho protkolovy kod a odesleme
			for (int i = 0; i < formItems.length; i++) {
				if (parent.getId() == formItems[i]) {
					showInfoBarWrite();
					ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);

					if(item != null) {
						item.setValue(newVal);
						stabiProvider.sendDataNoWaitForResponce(item);
					}
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
