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


import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.exception.IndexOutOfException;
import com.google.analytics.tracking.android.EasyTracker;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.spirit.general.ChannelsActivity;
import com.spirit.governor.GovernorActivity;

/**
 * aktivita na zobrazeni general moznosti nastaveni
 *
 * @author error414
 */
public class GeneralActivity extends BaseActivity
{

	@SuppressWarnings("unused")
	final private String TAG = "GeneralActivity";

	final private int PROFILE_CALL_BACK_CODE = 16;

    protected String protocolCode[] = {"POSITION", "MIX", "RECEIVER", "FLIGHT_STYLE",};

	// gui prvky ktere sou v teto aktivite aktivni
	protected int formItems[] = {R.id.position_select_id, R.id.mix_select_id, R.id.receiver_select_id, R.id.flight_style_select_id};

	private int lock = formItems.length;

    /**
     * je mozne odesilat data do zarizeni
     */
    private Boolean isPosibleSendData = true;

    /**
     * je potreba prenacist kanaly
     */
    private Boolean needRestoreChannels = false;

    /**
	 * zavolani pri vytvoreni instance aktivity settings
	 */
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		//setContentView(R.layout.general);
        initSlideMenu(R.layout.general);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.general_button_text)));
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
	 * stiknuti tlacitka channels
	 * 
	 * @param v
	 */
	public void openChannelsActivity(View v)
	{
        if(!getAppBasicMode()) {
            Intent i = new Intent(GeneralActivity.this, ChannelsActivity.class);
            startActivity(i);
        }
	}

    /**
     *
     * @param v
     */
    public void openGovernorActivity(View v)
    {
        Intent i = new Intent(GeneralActivity.this, GovernorActivity.class);
        startActivity(i);
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
			initConfiguration();
            initDefaultValue();

            ((Button)findViewById(R.id.channels)).setEnabled(!getAppBasicMode());
            this.checkGovernorButton();

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
     *
     */
    private void checkGovernorButton()
    {
        if(profileCreator != null){
            if(profileCreator.getProfileItemByName("RECEIVER").getValueInteger() < 67 /*A 65 - B 66*/ || profileCreator.getProfileItemByName("CHANNELS_THT").getValueInteger() == 7) {
                ((Button) findViewById(R.id.governor)).setEnabled(false);
            }else {
                ((Button)findViewById(R.id.governor)).setEnabled(true);
            }
        }else{
            ((Button)findViewById(R.id.governor)).setEnabled(!getAppBasicMode());
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

		try {
			for (int i = 0; i < formItems.length; i++) {
				Spinner tempSpinner = (Spinner) findViewById(formItems[i]);

				int pos = profileCreator.getProfileItemByName(protocolCode[i]).getValueForSpinner(tempSpinner.getCount());
				if (pos != tempSpinner.getSelectedItemPosition()){
					lock = lock + 1;
				}

				tempSpinner.setSelection(pos);
			}

            //governor jen pro NEpwm a musi byt prirazen kanal plynu
            this.checkGovernorButton();
		} catch (IndexOutOfException e) {
			errorInActivity(R.string.damage_profile);
			return;
		}
	}

    protected void restoreChannels(int receiverPosition){
        isPosibleSendData = true;
        // musime byt pripojeni
        if (stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.connection_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if(profileCreator == null){
            return;
        }

        final String[] map = {"CHANNELS_THT", "CHANNELS_AIL", "CHANNELS_ELE", "CHANNELS_RUD", "CHANNELS_GAIN", "CHANNELS_PITH", "CHANNELS_BANK"};
        switch (receiverPosition) {
            case 2:
                //spectrum
                Log.d(TAG, "spectrum");
                profileCreator.getProfileItemByName("CHANNELS_THT").setValue(0);
                profileCreator.getProfileItemByName("CHANNELS_AIL").setValue(1);
                profileCreator.getProfileItemByName("CHANNELS_ELE").setValue(2);
                profileCreator.getProfileItemByName("CHANNELS_RUD").setValue(3);
                profileCreator.getProfileItemByName("CHANNELS_GAIN").setValue(4);
                profileCreator.getProfileItemByName("CHANNELS_PITH").setValue(5);
                profileCreator.getProfileItemByName("CHANNELS_BANK").setValue(7);

                break;
            default:
                //other receiver
                Log.d(TAG, "other");
                profileCreator.getProfileItemByName("CHANNELS_THT").setValue(7);
                profileCreator.getProfileItemByName("CHANNELS_AIL").setValue(1);
                profileCreator.getProfileItemByName("CHANNELS_ELE").setValue(2);
                profileCreator.getProfileItemByName("CHANNELS_RUD").setValue(3);
                profileCreator.getProfileItemByName("CHANNELS_GAIN").setValue(4);
                profileCreator.getProfileItemByName("CHANNELS_PITH").setValue(5);
                profileCreator.getProfileItemByName("CHANNELS_BANK").setValue(7);
                break;
        }

        for(String item : map){
            if (isPosibleSendData) {
                showInfoBarWrite();
                stabiProvider.sendDataNoWaitForResponce(profileCreator.getProfileItemByName(item));
            }else if (!isPosibleSendData) {
                isPosibleSendData = true;
                break;
            }else{
                break;
            }
        }
        checkGovernorButton();
        checkChange(profileCreator);

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
                    Log.d(TAG, "odesilam spinner");
					showInfoBarWrite();

                    if(i == 2){
                        needRestoreChannels = true;
                    }

				}
			}

            //governor jen pro NEpwm a musi byt prirazen kanal plynu
            checkGovernorButton();
            initDefaultValue();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0)
		{
			// TODO Auto-generated method stub

		}
	};

	/**
	 * obsluha callbacku
	 *
	 * @param msg
	 * @return
	 */
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
                ((Button)findViewById(R.id.channels)).setEnabled(!getAppBasicMode());
                checkGovernorButton();
				break;
            case DstabiProvider.MESSAGE_SEND_COMAND_ERROR:
                isPosibleSendData = false;
                stabiProvider.abortAll();
                super.handleMessage(msg);
                break;
            case DstabiProvider.MESSAGE_SEND_COMPLETE:
                Log.d(TAG, "response");
                super.handleMessage(msg);
                if(needRestoreChannels){
                    needRestoreChannels = false;
                    if(profileCreator != null){
                        try {
                            restoreChannels(profileCreator.getProfileItemByName("RECEIVER").getValueForSpinner(profileCreator.getProfileItemByName("RECEIVER").getMaximum()));
                        } catch (IndexOutOfException e) {
                            e.printStackTrace();
                        }
                    }
                }

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
