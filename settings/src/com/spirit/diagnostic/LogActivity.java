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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.helpers.LogListAdapter;
import com.lib.BluetoothCommandService;
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
	public static Integer POSITION = 6;

	////////////////////////////////////////////////////
	final static Integer LOG_EVENT_OK = 0x0;
	final static Integer LOG_EVENT_CAL = 0x1;
	final static Integer LOG_EVENT_CYCRING = 0x2;
	final static Integer LOG_EVENT_RUDLIM = 0x4;
	final static Integer LOG_EVENT_VIBES = 0x8;
	final static Integer LOG_EVENT_HANG = 0x10;
	final static Integer LOG_EVENT_RXLOSS = 0x20;
	final static Integer LOG_EVENT_LOWVOLT = 0x40;
	////////////////////////////////////////////////////

	final protected int GROUP_LOG = 4;
	final protected int LOG_SAVE = 1;
	final protected int LOG_REFRESH = 2;

	final static String FILE_LOG_EXT = "pdf";
	@SuppressLint("SimpleDateFormat")
	protected SimpleDateFormat sdf = new SimpleDateFormat("yy_MM_dd_HHmmss");

	private ListView logList;

	private ArrayList<HashMap<Integer, Integer>> logListData;

	/**
	 * zavolani pri vytvoreni instance aktivity servos
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.log);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.log_button_text)));

		logList = (ListView) findViewById(R.id.logList);
		LogListAdapter adapter = new LogListAdapter(this, new ArrayList<HashMap<Integer, Integer>>());
		logList.setAdapter(adapter);
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
		int len = log[0] & 0xff;
		// kontrola jestli je log z pameti
		if (len == 120) {
			len /= 2;
	
	        int i;
	        for (i = len-1; i >= 0; i --) {
	            if (((log[i+1] & 0xff) & LOG_EVENT_LOWVOLT) != 0 && (log[i+1] & 0xff) != 0xff){
	                break;
	            }
	        }
	
	        len = i+1;
	        showConfirmDialog(R.string.log_from_previous_flight);
	    }
		//////////
		
		logListData = new ArrayList<HashMap<Integer, Integer>>();
		
		for (int i = 1; i <= len; i++) {
			if ((log[i] & 0xff) == LOG_EVENT_OK) {
				HashMap<Integer, Integer> row = new HashMap<Integer, Integer>();
				row.put(TITLE_FOR_LOG, R.string.log_event_ok);
				row.put(ICO_RESOURCE_LOG, R.drawable.ic_ok);
				row.put(POSITION, i);
				logListData.add(row);
			}

			if (((log[i] & 0xff) & LOG_EVENT_CAL) != 0) {
				HashMap<Integer, Integer> row = new HashMap<Integer, Integer>();
				row.put(TITLE_FOR_LOG, R.string.log_event_cal);
				row.put(ICO_RESOURCE_LOG, R.drawable.ic_info);
				row.put(POSITION, i);
				logListData.add(row);
			}

			if (((log[i] & 0xff) & LOG_EVENT_CYCRING) != 0) {
				HashMap<Integer, Integer> row = new HashMap<Integer, Integer>();
				row.put(TITLE_FOR_LOG, R.string.log_event_cycring);
				row.put(ICO_RESOURCE_LOG, R.drawable.ic_warn);
				row.put(POSITION, i);
				logListData.add(row);
			}

			if (((log[i] & 0xff) & LOG_EVENT_RUDLIM) != 0) {
				HashMap<Integer, Integer> row = new HashMap<Integer, Integer>();
				row.put(TITLE_FOR_LOG, R.string.log_event_rudlim);
				row.put(ICO_RESOURCE_LOG, R.drawable.ic_warn);
				row.put(POSITION, i);
				logListData.add(row);
			}

			if (((log[i] & 0xff) & LOG_EVENT_VIBES) != 0) {
				HashMap<Integer, Integer> row = new HashMap<Integer, Integer>();
				row.put(TITLE_FOR_LOG, R.string.log_event_vibes);
				row.put(ICO_RESOURCE_LOG, R.drawable.ic_warn);
				row.put(POSITION, i);
				logListData.add(row);
			}

			if (((log[i] & 0xff) & LOG_EVENT_HANG) != 0) {
				HashMap<Integer, Integer> row = new HashMap<Integer, Integer>();
				row.put(TITLE_FOR_LOG, R.string.log_event_hang);
				row.put(ICO_RESOURCE_LOG, R.drawable.ic_warn2);
				row.put(POSITION, i);
				logListData.add(row);
			}

			if (((log[i] & 0xff) & LOG_EVENT_RXLOSS) != 0) {
				HashMap<Integer, Integer> row = new HashMap<Integer, Integer>();
				row.put(TITLE_FOR_LOG, R.string.log_event_rxloss);
				row.put(ICO_RESOURCE_LOG, R.drawable.ic_warn2);
				row.put(POSITION, i);
				logListData.add(row);
			}
			
			if (((log[i] & 0xff) & LOG_EVENT_LOWVOLT) != 0) {
				HashMap<Integer, Integer> row = new HashMap<Integer, Integer>();
				row.put(TITLE_FOR_LOG, R.string.log_event_lowvolt);
				row.put(ICO_RESOURCE_LOG, R.drawable.ic_warn2);
				row.put(POSITION, i);
				logListData.add(row);
			}
		}

		LogListAdapter adapter = new LogListAdapter(this, logListData);
		logList.setAdapter(adapter);
		logList.invalidate();
	}

	/**
	 * stopnuti aktovity, posle pozadavek na ukonceni streamu
	 */
	@Override
	public void onStop()
	{
		super.onStop();
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
		menu.add(GROUP_LOG, LOG_SAVE, Menu.NONE, R.string.save_log);
		menu.add(GROUP_LOG, LOG_REFRESH, Menu.NONE, R.string.refresh_log);
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
		if (item.getGroupId() == GROUP_LOG && item.getItemId() == LOG_SAVE) {
			// musime byt pripojeni k zarizeni
			if (logListData == null) {
				Toast.makeText(getApplicationContext(), R.string.not_log_for_save, Toast.LENGTH_SHORT).show();
				return false;
			}
			
			SharedPreferences preferences = getSharedPreferences(PrefsActivity.PREF_APP, Context.MODE_PRIVATE);
			if(!preferences.contains(PrefsActivity.PREF_APP_LOG_DIR)){
				Toast.makeText(getApplicationContext(), R.string.first_choose_directory, Toast.LENGTH_SHORT).show();
				Intent i = new Intent(LogActivity.this, PrefsActivity.class);
				startActivity(i);
				return false;
			}
			
			String filename = preferences.getString(PrefsActivity.PREF_APP_LOG_DIR, "") + "/" + sdf.format(new Date()) + "-log." + FILE_LOG_EXT;

			LogPdf log = new LogPdf(this, logListData);
			log.create(filename);
			
			Toast.makeText(getApplicationContext(), R.string.save_done, Toast.LENGTH_SHORT).show();
			
			
		}else if(item.getGroupId() == GROUP_LOG && item.getItemId() == LOG_REFRESH){
			initConfiguration();
		}
		return false;
	}
}
	
