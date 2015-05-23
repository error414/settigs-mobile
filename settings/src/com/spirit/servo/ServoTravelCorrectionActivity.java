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

package com.spirit.servo;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.customWidget.picker.ProgresEx;
import com.google.analytics.tracking.android.EasyTracker;
import com.helpers.ByteOperation;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.lib.translate.ServoCorrectionProgressExTranslate;
import com.spirit.BaseActivity;
import com.spirit.R;


public class ServoTravelCorrectionActivity extends BaseActivity
{

	@SuppressWarnings("unused")
	final private String TAG = "TravelCorrectionActivity";

	final private int PROFILE_CALL_BACK_CODE = 16;
    final private int DIAGNOSTIC_CALL_BACK_CODE = 21;


	private final String protocolCode[] = {"TRAVEL_UAIL", "TRAVEL_UELE", "TRAVEL_UPIT", "TRAVEL_DAIL", "TRAVEL_DELE", "TRAVEL_DPIT",};

	private int formItems[] = {R.id.servo_travel_ch1_max, R.id.servo_travel_ch2_max, R.id.servo_travel_ch3_max, R.id.servo_travel_ch1_min, R.id.servo_travel_ch2_min, R.id.servo_travel_ch3_min,};

	private int formItemsTitle[] = {R.string.max, R.string.max, R.string.max, R.string.min, R.string.min, R.string.min,};

    /**
     *
     */
    final private Handler delayHandle = new Handler();

	/**
	 * zavolani pri vytvoreni instance aktivity servos
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.servos_travel_correction);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat("...", " \u2192 ", getString(R.string.servos_button_text), getString(R.string.servo_travel_correction)));

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
	 * znovu nacteni aktovity, priradime dstabi svuj handler a zkontrolujeme jestli sme pripojeni
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		if (stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED) {
			((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
			if (!getAppBasicMode()) {
				stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0x04)); //povoleni ladeni cyclic ringu| tady je to protoze to pouziva cysclick rink jako ladeni
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
		stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0xff)); //zakazani ladeni
	}

	private void initGui()
	{
		for (int i = 0; i < formItems.length; i++) {
			ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
			tempPicker.setTranslate(new ServoCorrectionProgressExTranslate());
			tempPicker.setTitle(formItemsTitle[i]); // nastavime popisek
            tempPicker.setEnabled(false);
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
     * ziskani informace o poloze kniplu z jednotky
     */
    protected void getPositionFromUnit()
    {
        delayHandle.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                stabiProvider.getDiagnostic(DIAGNOSTIC_CALL_BACK_CODE);
            }
        }, 400); // ms

    }

    /**
     *
     * @param b
     */
    protected void updateControlItem(byte[] b)
    {
        //PITCH
        int pitch = ByteOperation.twoByteToSigInt(b[4], b[5]);
        int pitchPercent = ((100 * pitch) / 340);
        ((ProgressBar) findViewById(R.id.pitch_progress)).setProgress(pitchPercent + 100);

        if(pitchPercent > 20){
            for(int i = 0; i < formItems.length; i++){
                ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
                tempPicker.setEnabled(i < 3);
            }
        }else if(pitchPercent < -20){
            for(int i = 0; i < formItems.length; i++){
                ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
                tempPicker.setEnabled(i >= 3);
            }
        }else{
            for(int i = 0; i < formItems.length; i++){
                ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
                tempPicker.setEnabled(false);
            }
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
			ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
			int size = profileCreator.getProfileItemByName(protocolCode[i]).getValueInteger();

			DstabiProfile.ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
			tempPicker.setRange(item.getMinimum(), item.getMaximum()); // nastavuji rozmezi prvku z profilu
			tempPicker.setCurrentNoNotify(size);
            tempPicker.setEnabled(false);
		}

	}

    /**
     *
     * @param menu
     */
    @Override
    protected void createBanksSubMenu(Menu menu) {
        //v subtrimu nejsou banky povoleny
    }

    /**
     * handle for change banks
     *
     * @param v
     */
    public void changeBankOpenDialog(View v){
        //disabled change bank in this activity
    }

	protected ProgresEx.OnChangedListener numberPicekrListener = new ProgresEx.OnChangedListener()
	{
		@Override
		public void onChanged(ProgresEx parent, int newVal)
		{
			// prohledani jestli udalost vyvolal znamy prvek
			// pokud prvek najdeme vyhledame si k prvku jeho protkolovy kod a odesleme
			for (int i = 0; i < formItems.length; i++) {
				if (parent.getId() == formItems[i]) {
					showInfoBarWrite();
					DstabiProfile.ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
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

                    if(!getAppBasicMode()) {
                        getPositionFromUnit();
                    }
				}
				break;
			case BANK_CHANGE_CALL_BACK_CODE:
				initConfiguration();
				super.handleMessage(msg);
				break;
            case DIAGNOSTIC_CALL_BACK_CODE:
                if (msg.getData().containsKey("data")) {
                    updateControlItem(msg.getData().getByteArray("data"));
                    getPositionFromUnit();
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
