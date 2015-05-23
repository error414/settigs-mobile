/*
Copyright (C) Petr Cada and Tomas Jedrzejek
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
import android.widget.ImageView;
import android.widget.TextView;

import com.customWidget.picker.ProgresEx;
import com.customWidget.picker.ProgresEx.OnChangedListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.lib.translate.StabiPichProgressExTranslate;
import com.spirit.BaseActivity;
import com.spirit.R;

public class StabiColActivity extends BaseActivity
{

	@SuppressWarnings("unused")
	final private String TAG = "SenzorActivity";

	final private int PROFILE_CALL_BACK_CODE = 16;

	private final String protocolCode[] = {"STABI_COL",};

	private int formItems[] = {R.id.stabi_pitch,};

	private int formItemsTitle[] = {R.string.stabi_col,};

	/**
	 * zavolani pri vytvoreni instance aktivity stabi
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		initSlideMenu(R.layout.stabi_col);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.stabi_button_text), " \u2192 ", getString(R.string.stabi_col)));

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
	 * znovu nacteni aktivity, priradime dstabi svuj handler a zkontrolujeme jestli sme pripojeni
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		if (stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED) {
            ((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
            initDefaultValue();
        }else{
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

			tempPicker.setTranslate(new StabiPichProgressExTranslate());
			tempPicker.setOffset(-127);             // zobrazujeme od stredu, 127 => 0
			tempPicker.setRange(117, 137, -10, 10); // hack, ble
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
			ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
			ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);

			tempPicker.setCurrentNoNotify(item.getValueInteger());

            if(item.getValueInteger() < 127 + 4 && item.getValueInteger() > 127 - 4){
                showWarning(View.VISIBLE);
            }else{
                showWarning(View.INVISIBLE);
            }

			if(profileCreator.getProfileItemByName("ALT_FUNCTION").getValueInteger() == 65
                    || profileCreator.getProfileItemByName("ALT_FUNCTION").getValueInteger() == 68
                    || profileCreator.getProfileItemByName("ALT_FUNCTION").getValueInteger() == 69
            ){ // 65 is "A" in profile, 68 is D,  69 is E
				tempPicker.setEnabled(false);
			}
		}

	}

    /**
     *
     * @param show
     */
    private void showWarning(int show)
    {
        findViewById(R.id.warning_header).setVisibility(show);
        findViewById(R.id.warning).setVisibility(show);
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
                        if(newVal < 127 + 4 && newVal > 127 - 4){
                            showWarning(View.VISIBLE);
                        }else{
                            showWarning(View.INVISIBLE);
                        }

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
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}



