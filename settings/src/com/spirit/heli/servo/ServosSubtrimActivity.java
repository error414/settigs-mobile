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
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.customWidget.picker.ProgresEx;
import com.customWidget.picker.ProgresEx.OnChangedListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.helpers.ByteOperation;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.lib.translate.ServoSubtrimProgressExTranslate;
import com.spirit.BaseActivity;
import com.spirit.R;

public class ServosSubtrimActivity extends BaseActivity
{

	@SuppressWarnings("unused")
	final private String TAG = "ServosSubtrimActivity";

	final private int PROFILE_CALL_BACK_CODE = 16;

	private final String protocolCode[] = {"SUBTRIM_AIL", "SUBTRIM_ELE", "SUBTRIM_PIT", "SUBTRIM_RUD",};

	private int formItems[] = {R.id.aileron_picker, R.id.elevator_picker, R.id.pitch_picker, R.id.rudder_picker,};

	private int formItemsTitle1[] = {R.string.servo_ch1, R.string.servo_ch2, R.string.servo_ch3, R.string.servo_rudder,};

	private int formItemsTitle2[] = {R.string.servo_ch1_inverted, R.string.servo_ch2, R.string.servo_ch3_inverted, R.string.servo_rudder,};
	/**
	 * zavolani pri vytvoreni instance aktivity servo type
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		initSlideMenu(R.layout.servos_subtrim);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.servos_button_text), " \u2192 ", getString(R.string.subtrim)));

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

			if (!getAppBasicMode()) {
				stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0x00)); //povoleni subtrimu
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
			ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
			ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
			
			tempPicker.setEnabled(!(getAppBasicMode() && item.isDeactiveInBasicMode()));
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0xff));
	}

    /**
     *
     * @param bankNumber
     */
    protected void beforeChangeBank(int bankNumber)
    {
        stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0xff));
    }

    /**
     *
     * @param bankNumber
     */
    protected void afterChangeBank(int bankNumber)
    {
        if (!getAppBasicMode()) {
            stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0x00)); //povoleni ladeni cyclic ringu
        }
    }

	private void initGui()
	{
		for (int i = 0; i < formItems.length; i++) {
			ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
            tempPicker.setRange(1, 255); // tohle rozmezi asi brat ze stabi profilu
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
			ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
			int size = profileCreator.getProfileItemByName(protocolCode[i]).getValueInteger();

            switch (i) {
                case 0:
                    tempPicker.setTranslate(new ServoSubtrimProgressExTranslate());
                    tempPicker.setInverted(profileCreator.getProfileItemByName("SERVO_REV_CH1").getValueForCheckBox());
                    break;
                case 1:
                    tempPicker.setTranslate(new ServoSubtrimProgressExTranslate());
                    tempPicker.setInverted(profileCreator.getProfileItemByName("SERVO_REV_CH2").getValueForCheckBox());
                    break;
                case 2:
                    tempPicker.setTranslate(new ServoSubtrimProgressExTranslate());
                    tempPicker.setInverted(profileCreator.getProfileItemByName("SERVO_REV_CH3").getValueForCheckBox());
                    break;
                default:
                    tempPicker.setTranslate(new ServoSubtrimProgressExTranslate());
                    tempPicker.setInverted(profileCreator.getProfileItemByName("SERVO_REV_CH4").getValueForCheckBox());
                    break;
            }

			int val = profileCreator.getProfileItemByName("MIX").getValueInteger();
			if(val % 2 == 1){
				tempPicker.setTitle(formItemsTitle1[i]);
			}else{
				tempPicker.setTitle(formItemsTitle2[i]);
			}

			tempPicker.setCurrentNoNotify(size);
		}

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
