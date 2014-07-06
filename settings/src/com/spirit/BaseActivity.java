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


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.helpers.DstabiProfile;
import com.helpers.StatusNotificationBuilder;
import com.lib.BluetoothCommandService;
import com.lib.ChangeInProfile;
import com.lib.DstabiProvider;

abstract public class BaseActivity extends Activity implements Handler.Callback
{

	//for debug
	private final String TAG = "BaseActivity";

	// klice pro nastaveni
	final protected String PREF_BT_ADRESS = "pref_bt_adress";
	final protected String PREF_FAVOURITES = "pref_favourites";

	// Intent request codes
	private static final int REQUEST_ENABLE_BT = 22;

	/**
	 * ulozeni profilu do jednotky
 	 */
	final protected int PROFILE_SAVE_CALL_BACK_CODE = 17;

	final protected int PROFILE_FOR_UPDATE_ORIGINAL = 100;

	final protected int GROUP_GENERAL = 5;
	final protected int OPEN_AUTHOR = 5;
    final protected int OPEN_DIFF = 55;

	final protected int GROUP_HELP = 2;
	final protected int OPEN_MANUAL = 2;
	final protected int OPEN_MANUAL_GOOGLE_DOCS = 3;

	final protected int GROUP_SAVE = 3;
	final protected int SAVE_PROFILE_MENU = 4;

	final protected String MANUAL_URL = "http://spirit-system.com/dl/manual/spirit-manual-1.0.22_cz.pdf";
	final protected String MANUAL_URL_GOOGLE_DOCS = "http://docs.google.com/viewer?url=http%3A%2F%2Fspirit-system.com%2Fdl%2Fmanual%2Fspirit-manual-1.0.22_cz.pdf";

	final protected BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	ProgressDialog generalDialog;
	StatusNotificationBuilder infoBar;

	final protected String PREF_BASIC_MODE = "pref_basic_mode";

	// The Handler that gets information back from the
	protected final Handler connectionHandler = new Handler(this);

	/**
	 *
	 */
	protected DstabiProvider stabiProvider;

	/**
	 * pocitadlo otevreni dialog boxu
	 */
	public int progressCount = 0;

	/**
	 * pocitadlo otevreni info boxu
	 */
	public int progressInfoCount = 0;

	/**
	 * zavolani pri vytvoreni instance aktivity servo type
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		stabiProvider = DstabiProvider.getInstance(connectionHandler);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		if (mBluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
	}

	@Override
	public void onStop()
	{
		closeAllBar();
		super.onStop();
	}

	/**
	 *
	 */
	public void onResume()
	{
		super.onResume();
		stabiProvider = DstabiProvider.getInstance(connectionHandler);
		((ImageView) findViewById(R.id.image_app_basic_mode)).setImageResource(getAppBasicMode() ? R.drawable.app_basic_mode_on : R.drawable.none);
	}

	/**
	 * nachazi se aplikace v basic modu ?
	 *
	 * @return
	 */
	public boolean getAppBasicMode()
	{
		SharedPreferences settings = getSharedPreferences(PREF_BASIC_MODE, Context.MODE_PRIVATE);
		return settings.getBoolean(PREF_BASIC_MODE, false);
	}

	/**
	 * zavreni vsechn notofikaci a dialogu
	 */
	protected void closeAllBar()
	{
		closeDialog();
		closeInfoBar();
	}

	/**
	 * zavreni dialogu
	 */
	protected void closeDialog()
	{
		progressCount = 0;
		if (generalDialog != null) {
			generalDialog.hide();
			generalDialog.cancel();
		}
	}

	protected void closeInfoBar()
	{
		progressInfoCount = 0;
		if (infoBar != null) {
			infoBar.hide();
		}
	}

	/**
	 * otevreni dialogu
	 *
	 * @param text
	 */
	protected void showDialog(String text)
	{
		progressCount++;
		if (generalDialog == null || !generalDialog.isShowing()) {
			generalDialog = ProgressDialog.show(BaseActivity.this, "", text, true);
		}
	}

	protected void showInfoBar(String text)
	{
		progressInfoCount++;
		if (infoBar == null || !infoBar.isShowing()) {
			if (infoBar == null) {
				infoBar = new StatusNotificationBuilder(getApplicationContext(), getWindow());
			}
			infoBar.setText(text);
			infoBar.show();
		}
	}

	/**
	 * zobrazeni dialogu pri cteni dat z jednotky
	 */
	protected void showDialogRead()
	{
		showDialog(getString(R.string.read_please_wait));
	}

	/**
	 * zobrazeni dialogu pri zapisovani dat do jednotky
	 */
	protected void showDialogWrite()
	{
		showDialog(getString(R.string.write_please_wait));
	}

	/**
	 * zobrazeni dialogu pri cteni dat z jednotky
	 */
	protected void showInfoBarRead()
	{
		showInfoBar(getString(R.string.read_data));
	}

	/**
	 * zobrazeni dialogu pri zapisovani dat z jednotky
	 */
	protected void showInfoBarWrite()
	{
		Log.i(TAG, "zapisuji");
		showInfoBar(getString(R.string.write_data));
	}


	/**
	 * pri dokonceni odesilani dat
	 * aby zmenila gui tak aby slo znova odeslat dasli pozadavek
	 */
	protected void sendInSuccessDialog()
	{
		progressCount--;
		if (progressCount <= 0) {
			closeDialog();
			progressCount = 0;
		}
	}

	/**
	 * pri dokonceni odesilani dat
	 * aby zmenila gui tak aby slo znova odeslat dasli pozadavek
	 */
	protected void sendInSuccessInfo()
	{
		progressInfoCount--;
		if (progressInfoCount <= 0) {
			closeInfoBar();
			progressInfoCount = 0;
		}
	}

	/**
	 * pri chybe pozadavku
	 */
	protected void sendInError()
	{
		sendInError(true);
	}

	/**
	 * pri chybe pozadavku
	 *
	 * @param finishActivity
	 */
	protected void sendInError(Boolean finishActivity)
	{
		Toast.makeText(getApplicationContext(), R.string.connection_error, Toast.LENGTH_SHORT).show();
		closeAllBar();
		if (finishActivity) {
			finish();
		}
	}

	protected void errorInActivity(int idText)
	{
		Toast.makeText(getApplicationContext(), idText, Toast.LENGTH_SHORT).show();
		closeAllBar();
		finish();
	}

	/**
	 * save profile to unit
	 */
	protected void saveProfileToUnit(DstabiProvider stabiProvider, int call_back_code)
	{
		showDialogWrite();
		// ziskani konfigurace z jednotky
		stabiProvider.sendDataForResponce(stabiProvider.SAVE_PROFILE, call_back_code);
	}

	protected void setOriginalProfileProfile(DstabiProfile profile)
	{
		ChangeInProfile.getInstance().setOriginalProfile(profile);
	}

	/**
	 * pokud se napriklad ulozi profil tak prenactem profil
	 */
	protected void reloadOriginalProfile()
	{
		if(stabiProvider != null){
			showInfoBarRead();
			stabiProvider.getProfile(PROFILE_FOR_UPDATE_ORIGINAL);
		}
	}

	/**
	 * vytvoreni kontextoveho menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		SubMenu manual = menu.addSubMenu(R.string.manual);
		manual.add(GROUP_HELP, OPEN_MANUAL, Menu.NONE, R.string.open_manual);
		manual.add(GROUP_HELP, OPEN_MANUAL_GOOGLE_DOCS, Menu.NONE, R.string.open_manual_google_docs);

		menu.add(GROUP_GENERAL, OPEN_AUTHOR, Menu.NONE, R.string.credits);
        menu.add(GROUP_GENERAL, OPEN_DIFF, Menu.NONE, R.string.profile_diff);

		menu.add(GROUP_SAVE, SAVE_PROFILE_MENU, Menu.NONE, R.string.save_profile_to_unit);
		return true;
	}

	/**
	 * reakce na kliknuti polozky v kontextovem menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//otevrit manual google docs/primo
		if (item.getGroupId() == GROUP_HELP && (item.getItemId() == OPEN_MANUAL || item.getItemId() == OPEN_MANUAL_GOOGLE_DOCS)) {

			String url = "";
			if (item.getItemId() == OPEN_MANUAL_GOOGLE_DOCS) {
				url = this.MANUAL_URL_GOOGLE_DOCS;
			} else {
				url = this.MANUAL_URL;
			}

			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
		}

		//otevreni informace o autorovi
		if (item.getGroupId() == GROUP_GENERAL && item.getItemId() == OPEN_AUTHOR) {
			Intent i = new Intent(this, AuthorActivity.class);
			startActivity(i);
		}

        //otevreni diffu profilu
        if (item.getGroupId() == GROUP_GENERAL && item.getItemId() == OPEN_DIFF) {
            if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED) {
                Intent i = new Intent(this, DiffActivity.class);
                startActivity(i);
            }else{
                Toast.makeText(this, R.string.must_first_connect_to_device, Toast.LENGTH_SHORT).show();
            }
        }

		//ulozit do jednotky
		if (item.getGroupId() == GROUP_SAVE && item.getItemId() == SAVE_PROFILE_MENU) {
			if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED) {
				saveProfileToUnit(stabiProvider, PROFILE_SAVE_CALL_BACK_CODE);
			}else{
				Toast.makeText(this, R.string.must_first_connect_to_device, Toast.LENGTH_SHORT).show();
			}
		}


		return false;
	}

	/**
	 * zachytavani vysledku z aktivit
	 */
	public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data)
	{
		switch (requestCode) {
			case REQUEST_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {
					// Bluetooth is now enabled, so set up a chat session
					// setupCommand();
				} else {
					// User did not enable Bluetooth or an error occured
					Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
					finish();
				}
				break;
		}
	}

	/**
	 *
	 */
	protected void showProfileSavedDialog()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(BaseActivity.this);
		alert.setPositiveButton("OK", null);

		alert.setView(getLayoutInflater().inflate(R.layout.alert_done, null));

		alert.show();
	}

	/**
	 * @param textId
	 */
	protected void showConfirmDialog(int textId)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(BaseActivity.this);
		alert.setPositiveButton("OK", null);

		alert.setMessage(getText(textId));

		alert.show();


	}

	/**
	 * @param text
	 */
	protected void showConfirmDialog(String text)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(BaseActivity.this);
		alert.setPositiveButton("OK", null);

		alert.setMessage(text);

		alert.show();
	}

	/**
	 * obsluha callbacku
	 *
	 * @param msg
	 * @return
	 */
	public boolean handleMessage(Message msg)
	{
		switch (msg.what) {
			case DstabiProvider.MESSAGE_SEND_COMAND_ERROR:
				sendInError();
				break;
			case DstabiProvider.MESSAGE_SEND_COMPLETE:
				sendInSuccessInfo();
				break;
			case DstabiProvider.MESSAGE_STATE_CHANGE:
				if (stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED) {
					sendInError();
				} else {
					((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
				}
				break;
			case PROFILE_SAVE_CALL_BACK_CODE:
				sendInSuccessDialog();
				showProfileSavedDialog();
				//po ulozeni profilu nacteme novy original profile
				reloadOriginalProfile();
				break;

			case PROFILE_FOR_UPDATE_ORIGINAL:
				sendInSuccessInfo();
				setOriginalProfileProfile(new DstabiProfile(msg.getData().getByteArray("data")));
				break;
		}
		return true;
	}

}
