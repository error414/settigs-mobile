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

package com.spirit.diagnostic;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.exception.IndexOutOfException;
import com.google.analytics.tracking.android.EasyTracker;
import com.helpers.ByteOperation;
import com.helpers.DstabiProfile;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.spirit.BaseActivity;
import com.spirit.R;

public class InputChannelsActivity extends BaseActivity
{

	final private String TAG = "InputChannelsActivity";

	final private int PROFILE_CALL_BACK_CODE = 16;
	final private int DIAGNOSTIC_CALL_BACK_CODE = 21;

	/**
	 * mrtva zona kterou ziskame z profilu
	 */
	private int stickDB;

	/**
	 * v jakem stavu je stabilizace, pokud je zapnuta tak se pro gyro vypisuji jine hodnoty
	 */
	private int stabiMode;

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
		setContentView(R.layout.input_channels);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
        ((TextView) findViewById(R.id.title)).setText(TextUtils.concat("... \u2192 ", getString(R.string.diagnostic_button_text), " \u2192 ", getString(R.string.input_channels)));

		initConfiguration();
	}
	
	/**
	 * handle for change banks
	 * 
	 * @param v
	 */
	public void changeBankOpenDialog(View v){
		//disabled change bank in this activity
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
	 * zjistime ulozime si hodnotu mrtve zony
	 *
	 * @param profile
	 */
	private void initByProfileString(byte[] profile)
	{
		profileCreator = new DstabiProfile(profile);

		if (!profileCreator.isValid()) {
			errorInActivity(R.string.damage_profile);
			return;
		}
	    /* MTODO nazvy udelat v konstantach */
		this.stickDB = profileCreator.getProfileItemByName("STICK_DB").getValueInteger();
		this.stabiMode = profileCreator.getProfileItemByName("ALT_FUNCTION").getValueInteger();

		//mame profil muzeme zazadat o data o pohybu kniplu
		getPositionFromUnit();
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
		} else {
			finish();
		}
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
		}, 50); // ms

	}

    /**
     *
     * @param b
     */
	protected void updateGui(byte[] b)
	{

		//AILERON
		int aileron = ByteOperation.twoByteToSigInt(b[0], b[1]);
		int aileronPercent = ((100 * aileron) / 340) * -1;
		((ProgressBar) findViewById(R.id.aileron_progress_diagnostic)).setProgress(aileronPercent + 100);
		((TextView) findViewById(R.id.aileron_value_diagnostic)).setText(String.valueOf(aileronPercent));

		if (Math.abs(aileron) > this.stickDB) {
			((TextView) findViewById(R.id.aileron_value_diagnostic)).setTypeface(null, Typeface.BOLD);
		} else {
			((TextView) findViewById(R.id.aileron_value_diagnostic)).setTypeface(null, Typeface.NORMAL);
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////

		//ELEVATOR
		int elevator = ByteOperation.twoByteToSigInt(b[2], b[3]);
		int elevatorPercent = ((100 * elevator) / 340) * -1;
		((ProgressBar) findViewById(R.id.elevator_progress_diagnostic)).setProgress(elevatorPercent + 100);
		((TextView) findViewById(R.id.elevator_value_diagnostic)).setText(String.valueOf(elevatorPercent));

		if (Math.abs(elevator) > this.stickDB) {
			((TextView) findViewById(R.id.elevator_value_diagnostic)).setTypeface(null, Typeface.BOLD);
		} else {
			((TextView) findViewById(R.id.elevator_value_diagnostic)).setTypeface(null, Typeface.NORMAL);
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//PITCH
		int pitch = ByteOperation.twoByteToSigInt(b[4], b[5]);
		int pitchPercent = ((100 * pitch) / 340);
		((ProgressBar) findViewById(R.id.pitch_progress_diagnostic)).setProgress(pitchPercent + 100);
		((TextView) findViewById(R.id.pitch_value_diagnostic)).setText(String.valueOf(pitchPercent));

		////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//RUDDER
		int rudder = ByteOperation.twoByteToSigInt(b[6], b[7]);
		int rudderPercent = ((100 * rudder) / 340);
		((ProgressBar) findViewById(R.id.rudder_progress_diagnostic)).setProgress((rudderPercent + 100));
		((TextView) findViewById(R.id.rudder_value_diagnostic)).setText(String.valueOf(rudderPercent));

		if (Math.abs(rudder) > this.stickDB) {
			((TextView) findViewById(R.id.rudder_value_diagnostic)).setTypeface(null, Typeface.BOLD);
		} else {
			((TextView) findViewById(R.id.rudder_value_diagnostic)).setTypeface(null, Typeface.NORMAL);
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////

		//GYRO 
		int gyro = ByteOperation.twoByteToSigInt(b[8], b[9]);
		int gyroPercent = ((100 * gyro) / 388);

		String mode = "";
		if (this.stabiMode == 65 /* A z profilu */ && gyro < 0) {
			mode = " N";
		} else if(this.stabiMode > 65 && gyro < 0) {
			mode = " HF";
		}else{
            mode = " HL";
        }
		
		((ProgressBar) findViewById(R.id.gyro_progress_diagnostic)).setProgress((gyroPercent + 100));
		((TextView) findViewById(R.id.gyro_value_diagnostic)).setText(String.valueOf(Math.abs(gyroPercent)) + mode);
		////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//AUX2  / banks 
		int banks = ByteOperation.twoByteToSigInt(b[10], b[11]);
		int banksPercent = ((100 * banks) / 340);
		
		int bank = 1;
		if (banks < (1400-1520)){
			bank = 0;
		}else if (banks > (1640-1520)){
			bank = 2;
		}
		
		// pokud neni v bankach prirazen zadny kanal
		TextView bankProgressDiagnostic = (TextView) findViewById(R.id.bank_value_diagnostic);
		try {
			int max = getResources().getStringArray(R.array.channels_values).length;
			if(profileCreator != null && profileCreator.getProfileItemByName("CHANNELS_BANK").getValueForSpinner(max - 1) == 7){ // 7  = unbind
				bankProgressDiagnostic.setTextColor(getResources().getColor(R.color.grey));
			}else{
				bankProgressDiagnostic.setTextColor(getResources().getColor(R.color.text_color));
			}
		} catch (IndexOutOfException e) {
			e.printStackTrace();
		}
		
		((ProgressBar) findViewById(R.id.bank_progress_diagnostic)).setProgress((banksPercent + 100));
		bankProgressDiagnostic.setText(String.valueOf(getString(R.string.bank) + " " + String.valueOf(bank)));
		////////////////////////////////////////////////////////////////////////////////////////////////////////////

		//AUX1  / throttle
		int throttle = ByteOperation.twoByteToSigInt(b[12], b[13]);
		int throttlePercent = ((50 * throttle) / 340);
		
		// pokud neni throttle prirazen zadny kanal
		TextView throttleValueDiagnostic = (TextView) findViewById(R.id.throttle_value_diagnostic);
		try {
			int max = getResources().getStringArray(R.array.channels_values).length;
			if(profileCreator != null && profileCreator.getProfileItemByName("CHANNELS_THT").getValueForSpinner(max - 1) == 7){ // 7 = unbind
				throttleValueDiagnostic.setTextColor(getResources().getColor(R.color.grey));
			}else{
				throttleValueDiagnostic.setTextColor(getResources().getColor(R.color.text_color));
			}
		} catch (IndexOutOfException e) {
			e.printStackTrace();
		}
		
		((ProgressBar) findViewById(R.id.throttle_progress_diagnostic)).setProgress((throttlePercent + 50));
		throttleValueDiagnostic.setText(String.valueOf(Math.max(-1, throttlePercent + 50)));
		////////////////////////////////////////////////////////////////////////////////////////////////////////////

		//SENZOR X Y Z
		((TextView) findViewById(R.id.diagnostic_x)).setText(String.valueOf((int)b[14]));
		((TextView) findViewById(R.id.diagnostic_y)).setText(String.valueOf((int)b[15]));
		((TextView) findViewById(R.id.diagnostic_z)).setText(String.valueOf((int)b[16]));

	}

    /**
     *
     * @param menu
     */
	@Override
	protected void createBanksSubMenu(Menu menu) {
		//v diagonstike nejsou banky povoleny
	}

    /**
     *
     * @param msg
     * @return
     */
	public boolean handleMessage(Message msg)
	{
		switch (msg.what) {
			case DstabiProvider.MESSAGE_SEND_COMAND_ERROR:
				Log.d(TAG, "Prisla chyba");
                if(profileCreator != null && profileCreator.isValid()) {
                    getPositionFromUnit();
                }
				break;
			case PROFILE_CALL_BACK_CODE:
				if (msg.getData().containsKey("data")) {
					initByProfileString(msg.getData().getByteArray("data"));
					sendInSuccessDialog();
				}
                break;
			case DIAGNOSTIC_CALL_BACK_CODE:
				if (msg.getData().containsKey("data")) {
					updateGui(msg.getData().getByteArray("data"));

					getPositionFromUnit();
				}
				break;
			default:
				super.handleMessage(msg);
		}

		return true;
	}

    /**
     *
     */
    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    /**
     *
     */
    @Override
    public void onStop()
    {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}
