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

package com.spirit.heli.limit;

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
import com.lib.CommandService;
import com.spirit.BaseActivity;
import com.spirit.R;

public class ServosRudderEndPointsActivity extends BaseActivity
{

	@SuppressWarnings("unused")
	final private String TAG = "ServosRudderEndPointsActivity";

	final private int PROFILE_CALL_BACK_CODE = 16;

    private final String protocolCode[] = {"RUDDER_MAX", "RUDDER_MIN",};

    private int formItems[] = {R.id.rudder_limit_left, R.id.rudder_limit_right,};

    private int formItemsTitle[] = {R.string.left_limit, R.string.right_limit,};

	/**
	 * zavolani pri vytvoreni instance aktivity servos
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		initSlideMenu(R.layout.servos_rudder_end_points);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat("...", " \u2192 ", getString(R.string.limits), " \u2192 ", getString(R.string.rudder_end_points_no_break)));

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

            if (!getAppBasicMode()) {
                stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0x00));
            }

            initDefaultValue();
		} else {
			finish();
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (!getAppBasicMode()) {
			stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0xff));
		}
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
			stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0x00));
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
			tempPicker.setRange(32, 255); // tohle rozmezi asi brat ze stabi profilu
			tempPicker.setTitle(formItemsTitle[i]); // tohle rozmezi asi brat ze stabi profilu
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
            ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
            tempPicker.setRange(item.getMinimum(), item.getMaximum()); // nastavuji rozmezi prvku z profilu
			tempPicker.setCurrentNoNotify(size);

            switch (i) {
                case 0:
                    initLeftWarning(size);
                    break;
                case 1:
                    initRightWarning(size);
                    break;
            }
        }
    }

    /**
     * @param size
     */
    private void initLeftWarning(int size) {
        if (size < 70) {
            ((TextView) findViewById(R.id.warning_rud_left)).setText(R.string.warning_low_value);
        } else if (size > 170) {
            ((TextView) findViewById(R.id.warning_rud_left)).setText(R.string.warning_high_value);
        } else {
            ((TextView) findViewById(R.id.warning_rud_left)).setText("");
        }
    }

    /**
     * @param size
     */
    private void initRightWarning(int size) {
        if (size < 70) {
            ((TextView) findViewById(R.id.warning_rud_right)).setText(R.string.warning_low_value);
        } else if (size > 170) {
            ((TextView) findViewById(R.id.warning_rud_right)).setText(R.string.warning_high_value);
        } else {
            ((TextView) findViewById(R.id.warning_rud_right)).setText("");
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

                        switch (i) {
                            case 0:
                                initLeftWarning(newVal);
                                break;
                            case 1:
                                initRightWarning(newVal);
                                break;
                        }
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
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}
