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

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.helpers.ByteOperation;
import com.lib.BluetoothCommandService;
import com.spirit.BaseActivity;
import com.spirit.R;

public class BecTesterActivity extends BaseActivity
{

	final private String TAG = "BecTesterActivity";

    private CountDownTimer countDownTimer;
    private Integer countDownSecond = 20;


	/**
	 * zavolani pri vytvoreni instance aktivity servo type
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		initSlideMenu(R.layout.bec_tester);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
        ((TextView) findViewById(R.id.title)).setText(TextUtils.concat("... \u2192 ", getString(R.string.diagnostic_button_text), " \u2192 ", getString(R.string.bec_tester)));

        countDownTimer = new CountDownTimer(countDownSecond * 1000, 1000 / countDownSecond) {
            public void onTick(long millisUntilFinished) {

                long percent = (((100000L / (countDownSecond * 1000L)) * millisUntilFinished)) / 1000;

                ProgressBar progress = (ProgressBar)findViewById(R.id.progress);
                progress.setProgress(100 - (int)percent);

                TextView progressText = (TextView)findViewById(R.id.state_text);
                progressText.setText(String.valueOf(100 - percent) + "%");
            }

            public void onFinish() {
                stabiProvider.sendDataNoWaitForResponce("0", ByteOperation.intToByteArray(0xff));
                initButton();
            }
        };

        initGui();
        delegateListener();
	}

    public void onStop()
    {
        super.onStop();
        if(countDownTimer != null){
            countDownTimer.cancel();
        }
        stabiProvider.sendDataNoWaitForResponce("0", ByteOperation.intToByteArray(0xff));
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
		} else {
			finish();
		}
	}

    /**
     *
     */
    public void initGui()
    {
        initProgressText();
        initButton();
    }

    /**
     *
     */
    public void initProgressText()
    {
        ProgressBar progress = (ProgressBar)findViewById(R.id.progress);
        progress.setProgress(0);

        TextView progressText = (TextView)findViewById(R.id.state_text);
        progressText.setText("0%");
    }

    /**
     *
     */
    public void initButton()
    {
        Button start = (Button)findViewById(R.id.start);
        start.setEnabled(true);

        Button stop = (Button)findViewById(R.id.stop);
        stop.setEnabled(false);
    }

    /**
     *
     */
    public void initButtonProgress()
    {
        Button start = (Button)findViewById(R.id.start);
        start.setEnabled(false);

        Button stop = (Button)findViewById(R.id.stop);
        stop.setEnabled(true);
    }

    /**
     *
     */
    public void delegateListener()
    {
        Button start = (Button)findViewById(R.id.start);
        start.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                initProgressText();
                initButtonProgress();
                countDownTimer.start();
                stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0x05));

            }
        });

        Button stop = (Button)findViewById(R.id.stop);
        stop.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                initGui();
                stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0xff));
                countDownTimer.cancel();
            }
        });
    }
}
