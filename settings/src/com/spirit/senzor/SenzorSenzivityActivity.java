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

package com.spirit.senzor;

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
import com.lib.BluetoothCommandService;
import com.lib.translate.StabiSenzivityXProgressExTranslate;
import com.lib.translate.StabiSenzivityYProgressExTranslate;
import com.lib.translate.StabiSenzivityZProgressExTranslate;
import com.spirit.BaseActivity;
import com.spirit.R;

public class SenzorSenzivityActivity extends BaseActivity
{

	final private String TAG = "SenzorSenzivityActivity";

	final private int PROFILE_CALL_BACK_CODE = 16;

	private final String protocolCode[] = {"SENSOR_SENX", "SENSOR_SENZ", "SENSOR_GYROGAIN"
			//"SENSOR_SENZ",
	};

	private int formItems[] = {R.id.x_cyclic, R.id.z_rudder,  R.id.gyro_gain,
			//R.id.z_yaw,
	};

	private int formItemsTitle[] = {R.string.x_cyclic, R.string.z_rudder,  R.string.gyro_gain,
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
		initSlideMenu(R.layout.senzor_senzivity);

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
			ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
			ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
			
			tempPicker.setEnabled(!(getAppBasicMode() && item.isDeactiveInBasicMode()));
		}
	}

	private void initGui()
	{
		for (int i = 0; i < formItems.length; i++) {
			ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
			
			switch(i){
				case 0:
                    tempPicker.setTranslate(new StabiSenzivityXProgressExTranslate());
					tempPicker.setRange(0, 80); // nastavuji rozmezi prvku z profilu
					break;
				case 1:
					tempPicker.setTranslate(new StabiSenzivityZProgressExTranslate());
					tempPicker.setRange(50, 100); // nastavuji rozmezi prvku z profilu
					break;
				case 2:
                    tempPicker.setTranslate(new StabiSenzivityYProgressExTranslate());
					tempPicker.setRange(0, 200); // nastavuji rozmezi prvku z profilu
					break;
			
			}

			tempPicker.setTitle(formItemsTitle[i]); // nastavime titulek
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
				
				if(profileCreator.getProfileItemByName("CHANNELS_GAIN").getValueInteger() != 7 && i == 2){ // 7 = neprirazeno / i = 2 = SENSOR_GYROGAIN
					tempPicker.setEnabled(false);
                    tempPicker.setEnabledDefaultValue(false);
                    tempPicker.setCurrentNoNotify(getText(R.string.in_transmitter).toString());
				}else {
                    tempPicker.setCurrentNoNotify(item.getValueInteger());
                }
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
