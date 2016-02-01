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
package com.spirit.heli.diagnostic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.helpers.ByteOperation;
import com.helpers.LogListAdapter;
import com.lib.BluetoothCommandService;
import com.lib.ChangeInProfile;
import com.lib.LogPdf;
import com.spirit.BaseActivity;
import com.spirit.PrefsActivity;
import com.spirit.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@SuppressLint("SdCardPath")
public class LogActivity extends BaseActivity
{

	final private String TAG = "LogActivity";

	//KODY PRO CALLBACKY //////////////////////////////
	final private int LOG_CALL_BACK_CODE = 20;

	public static Integer TITLE_FOR_LOG = 4;
	public static Integer ICO_RESOURCE_LOG = 5;

	////////////////////////////////////////////////////
	final static Integer LOG_EVENT_OK = 0x0;
	final static Integer LOG_EVENT_GOV_OUT_OF_RANGE = 0x1;
	final static Integer LOG_EVENT_CYCRING = 0x2;
	final static Integer LOG_EVENT_RUDLIM = 0x4;
	final static Integer LOG_EVENT_VIBES = 0x8;
	final static Integer LOG_EVENT_HANG = 0x10;
	final static Integer LOG_EVENT_RXLOSS = 0x20;
	final static Integer LOG_EVENT_LOWVOLT = 0x40;
	final static Integer LOG_EVENT_GOVENGAGED = 0x80;
	final static Integer LOG_EVENT_RXCORRUPT = 0x100;
	final static Integer LOG_EVENT_GOVSIGNAL = 0x200;
	final static Integer LOG_EVENT_FAILED = 0xffff;
	////////////////////////////////////////////////////

	final protected int GROUP_LOG = 4;
	final protected int LOG_REFRESH = 2;

	final static String FILE_LOG_EXT = "pdf";
	@SuppressLint("SimpleDateFormat")
	protected SimpleDateFormat sdf = new SimpleDateFormat("yy_MM_dd_HHmmss");

	private ListView logList;

	private HashMap<Integer, ArrayList<HashMap<Integer, Integer>>> logListData;

	private boolean prewLog = false;

	/**
	 * zavolani pri vytvoreni instance aktivity servos
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		initSlideMenu(R.layout.log);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat("... \u2192 ", getString(R.string.diagnostic_button_text), " \u2192 ", getString(R.string.log_button_text)));

		logList = (ListView) findViewById(R.id.logList);
		LogListAdapter adapter = new LogListAdapter(this, new HashMap<Integer, ArrayList<HashMap<Integer, Integer>>>());
		logList.setAdapter(adapter);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(LogActivity.this);
		if(!sharedPrefs.contains(PrefsActivity.PREF_APP_DIR)){
			Toast.makeText(getApplicationContext(), R.string.first_choose_directory, Toast.LENGTH_SHORT).show();
			Intent i = new Intent(LogActivity.this, PrefsActivity.class);
			startActivity(i);
			finish();
			return;
		}

		initConfiguration();
	}

	/**
	 *
	 * @return
	 */
	public boolean isEnableChangeBank()
	{
		return false;
	}

	/**
	 *
	 * @param savedInstanceState
	 */
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		savedInstanceState.putBoolean("prewLog", prewLog);
	}


	/**
	 *
	 * @param savedInstanceState
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		prewLog = savedInstanceState.getBoolean("prewLog", false);
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
	 */
	private void initConfiguration()
	{
		showDialogRead();
		// ziskani konfigurace z jednotky
		stabiProvider.getLog(LOG_CALL_BACK_CODE);
	}

	/**
	 * nacteni dat z jednotky log
	 *
	 * @param log
	 */
	@SuppressLint("UseSparseArrays")
	protected void updateGuiByLog(byte[] log)
	{
		int len = ByteOperation.byteToUnsignedInt(log[0]) * 2;

		if(len > 1){
			prewLog = ByteOperation.byteToUnsignedInt(log[1]) == 1;
			if(prewLog){
				showConfirmDialog(R.string.log_from_previous_flight);
			}
		}

		//////////////////////////////////////////////////////////////////////

		logListData = new HashMap<Integer, ArrayList<HashMap<Integer, Integer>>>();

		int position = 0;
		for (int i = 2; i <= len + 1; i = i + 2) {

			byte[] codeByte = new byte[2];
			codeByte[0] = log[i];
			codeByte[1] = log[i + 1];

			ArrayList<HashMap<Integer, Integer>> row = new ArrayList<HashMap<Integer, Integer>>();

			int logCode = ByteOperation.byte2ArrayToSigInt(codeByte);

			if (i == 2) {
				HashMap<Integer, Integer> subRow = new HashMap<Integer, Integer>();
				subRow.put(TITLE_FOR_LOG, R.string.log_event_cal);
				subRow.put(ICO_RESOURCE_LOG, R.drawable.ic_info);
				row.add(subRow);
			}

			if ((logCode & LOG_EVENT_GOV_OUT_OF_RANGE) != 0) {
				HashMap<Integer, Integer> subRow = new HashMap<Integer, Integer>();
				subRow.put(TITLE_FOR_LOG, R.string.log_event_gov_out_of_range);
				subRow.put(ICO_RESOURCE_LOG, R.drawable.ic_warn);
				row.add(subRow);
			}

			if ((logCode & LOG_EVENT_CYCRING) != 0) {
				HashMap<Integer, Integer> subRow = new HashMap<Integer, Integer>();
				subRow.put(TITLE_FOR_LOG, R.string.log_event_cycring);
				subRow.put(ICO_RESOURCE_LOG, R.drawable.ic_warn);
				row.add(subRow);
			}

			if ((logCode & LOG_EVENT_RUDLIM) != 0) {
				HashMap<Integer, Integer> subRow = new HashMap<Integer, Integer>();
				subRow.put(TITLE_FOR_LOG, R.string.log_event_rudlim);
				subRow.put(ICO_RESOURCE_LOG, R.drawable.ic_warn);
				row.add(subRow);
			}

			if ((logCode & LOG_EVENT_VIBES) != 0) {
				HashMap<Integer, Integer> subRow = new HashMap<Integer, Integer>();
				subRow.put(TITLE_FOR_LOG, R.string.log_event_vibes);
				subRow.put(ICO_RESOURCE_LOG, R.drawable.ic_warn2);
				row.add(subRow);
			}

			if ((logCode & LOG_EVENT_HANG) != 0) {
				HashMap<Integer, Integer> subRow = new HashMap<Integer, Integer>();
				subRow.put(TITLE_FOR_LOG, R.string.log_event_hang);
				subRow.put(ICO_RESOURCE_LOG, R.drawable.ic_warn2);
				row.add(subRow);
			}

			if ((logCode & LOG_EVENT_RXLOSS) != 0) {
				HashMap<Integer, Integer> subRow = new HashMap<Integer, Integer>();
				subRow.put(TITLE_FOR_LOG, R.string.log_event_rxloss);
				subRow.put(ICO_RESOURCE_LOG, R.drawable.ic_warn2);
				row.add(subRow);
			}

			if ((logCode & LOG_EVENT_LOWVOLT) != 0) {
				HashMap<Integer, Integer> subRow = new HashMap<Integer, Integer>();
				subRow.put(TITLE_FOR_LOG, R.string.log_event_lowvolt);
				subRow.put(ICO_RESOURCE_LOG, R.drawable.ic_warn2);
				row.add(subRow);
			}

			if ((logCode & LOG_EVENT_GOVENGAGED) != 0) {
				HashMap<Integer, Integer> subRow = new HashMap<Integer, Integer>();
				subRow.put(TITLE_FOR_LOG, R.string.log_event_govengaged);
				subRow.put(ICO_RESOURCE_LOG, R.drawable.ic_info);
				row.add(subRow);
			}

			if ((logCode & LOG_EVENT_RXCORRUPT) != 0) {
				HashMap<Integer, Integer> subRow = new HashMap<Integer, Integer>();
				subRow.put(TITLE_FOR_LOG, R.string.log_event_rxcorrupt);
				subRow.put(ICO_RESOURCE_LOG, R.drawable.ic_warn);
				row.add(subRow);
			}

			if ((logCode & LOG_EVENT_GOVSIGNAL) != 0) {
				HashMap<Integer, Integer> subRow = new HashMap<Integer, Integer>();
				subRow.put(TITLE_FOR_LOG, R.string.log_event_govsignal);
				subRow.put(ICO_RESOURCE_LOG, R.drawable.ic_warn);
				row.add(subRow);
			}

			if (logCode == LOG_EVENT_OK && row.size() == 0) {
				HashMap<Integer, Integer> subRow = new HashMap<Integer, Integer>();
				subRow.put(TITLE_FOR_LOG, R.string.log_event_ok);
				subRow.put(ICO_RESOURCE_LOG, R.drawable.ic_ok);
				row.add(subRow);
			}

			if (logCode == LOG_EVENT_FAILED) {
				HashMap<Integer, Integer> subRow = new HashMap<Integer, Integer>();
				subRow.put(TITLE_FOR_LOG, R.string.log_event_save_failed);
				subRow.put(ICO_RESOURCE_LOG, R.drawable.ic_warn);

				row = new ArrayList<HashMap<Integer, Integer>>();
				row.add(subRow);

				logListData = new HashMap<Integer, ArrayList<HashMap<Integer, Integer>>>();
				logListData.put(0, row);
				break;
			}

			logListData.put(position, row);

			position++;
		}

		LogListAdapter adapter = new LogListAdapter(this, logListData);
		logList.setAdapter(adapter);
		logList.invalidate();
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

	public boolean handleMessage(Message msg)
	{
		//Log.d(TAG, "prisla zprava");
		switch (msg.what) {
			case LOG_CALL_BACK_CODE:
				sendInSuccessDialog();
				if (msg.getData().containsKey("data")) {
					updateGuiByLog(msg.getData().getByteArray("data"));


					Thread thread = new Thread() {
						@Override
						public void run() {
							saveLogToFile();
						}
					};

					thread.start();
				}
				break;
			default:
				super.handleMessage(msg);
		}

		return true;
	}

	/**
	 * vytvoreni kontextoveho menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		menu.add(GROUP_LOG, LOG_REFRESH, Menu.NONE, R.string.refresh_log);
		return true;
	}

	/**
	 *
	 * @return
	 */
	private boolean saveLogToFile()
	{
		// musime byt pripojeni k zarizeni
		if (logListData == null) {
			this.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), R.string.not_log_for_save, Toast.LENGTH_SHORT).show();
				}
			});
			return false;
		}

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(LogActivity.this);
		if(!sharedPrefs.contains(PrefsActivity.PREF_APP_DIR)){
			return false;
		}

		String filename = sharedPrefs.getString(PrefsActivity.PREF_APP_DIR, "") + PrefsActivity.PREF_APP_PREFIX + PrefsActivity.PREF_APP_LOG_DIR + "/" + sdf.format(new Date()) + "-log." + FILE_LOG_EXT;

		LogPdf log = new LogPdf(this, logListData, prewLog, ChangeInProfile.getInstance().getOriginalProfile());
		if(log.create(filename)){
			this.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), R.string.save_done, Toast.LENGTH_SHORT).show();
				}
			});

		}else{

			this.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), R.string.not_save, Toast.LENGTH_SHORT).show();
				}
			});
		}

		return true;
	}

	/**
	 * reakce na kliknuti polozky v kontextovem menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		//nahrani / ulozeni profilu
		if(item.getGroupId() == GROUP_LOG && item.getItemId() == LOG_REFRESH){
			initConfiguration();
		}
		return false;
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
	
