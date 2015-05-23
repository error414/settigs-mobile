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
package com.spirit.governor;

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

import com.google.analytics.tracking.android.EasyTracker;
import com.helpers.ByteOperation;
import com.helpers.DstabiProfile;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.spirit.BaseActivity;
import com.spirit.R;

public class GovernorRpmSenzor extends BaseActivity
{
	@SuppressWarnings("unused")
	final private String TAG = "RpmSenzor";

	final private int PROFILE_CALL_BACK_CODE = 16;
    final private int RPMSENZOR_CALL_BACK_CODE = 21;
    final static public int RPM_SENZOR_LENGTH = 4;

    final private Handler delayHandle = new Handler();

	/**
	 * zavolani pri vytvoreni instance aktivity stabi
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		initSlideMenu(R.layout.bec_rpm_senzor);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.governor), " \u2192 ", getString(R.string.governor_rpm_senzor)));

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
     *
     * @param menu
     */
    @Override
    protected void createBanksSubMenu(Menu menu) {
        //v diagonstike nejsou banky povoleny
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
	 * znovu nacteni aktivity, priradime dstabi svuj handler a zkontrolujeme jestli sme pripojeni
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		if (stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED) {
            ((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
        }else{
            finish();
        }
	}

    /**
     * ziskani informace o poloze kniplu z jednotky
     */
    protected void getRpmValue()
    {
        if(profileCreator != null && profileCreator.isValid() && profileCreator.getProfileItemByName("GOVERNOR_MODE").getValueInteger() > 0) {
            delayHandle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stabiProvider.getGovRpm(RPMSENZOR_CALL_BACK_CODE);
                }
            }, 50); // 125ms
        }
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

        int govMode = profileCreator.getProfileItemByName("GOVERNOR_MODE").getValueInteger();

        if(govMode == 0){
            findViewById(R.id.governor_request_rpm).setEnabled(false);
            findViewById(R.id.governor_current_rpm).setEnabled(false);
        }else{
            getRpmValue();
        }

    }

    /**
     *
     * @param b
     */
    protected void updateGui(byte[] b)
    {
        short value = ByteOperation.byteArrayToShort(b);

        byte[] request = {b[0], b[1]};
        byte[] current = {b[2], b[3]};


        Log.d(TAG, "---");
        Log.d(TAG, String.valueOf(value));
        Log.d(TAG, String.valueOf(b.length));
        Log.d(TAG, "---");
        ((ProgressBar)findViewById(R.id.governor_request_rpm)).setProgress(ByteOperation.byteArrayToShort(request));
        ((ProgressBar)findViewById(R.id.governor_current_rpm)).setProgress(ByteOperation.byteArrayToShort(current));

        ((TextView)findViewById(R.id.governor_value_request_rpm)).setText(String.valueOf(ByteOperation.byteArrayToShort(request)) + " RPM");
        ((TextView)findViewById(R.id.governor_value_current_rpm)).setText(String.valueOf(ByteOperation.byteArrayToShort(current)) + " RPM");
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
                    getRpmValue();
                }
                break;
            case PROFILE_CALL_BACK_CODE:
                if (msg.getData().containsKey("data")) {
                    initByProfileString(msg.getData().getByteArray("data"));
                    sendInSuccessDialog();
                }
                break;
            case RPMSENZOR_CALL_BACK_CODE:
                if (msg.getData().containsKey("data")) {

                    if (msg.getData().getByteArray("data").length > 4) {
                        Log.d(TAG, "Odpoved delsi nez 4");
                    }

                    updateGui(msg.getData().getByteArray("data"));

                    getRpmValue();
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



