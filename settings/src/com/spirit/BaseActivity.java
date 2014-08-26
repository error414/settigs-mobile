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


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.helpers.Globals;
import com.helpers.SlideMenuListAdapter;
import com.helpers.StatusNotificationBuilder;
import com.lib.BluetoothCommandService;
import com.lib.ChangeInProfile;
import com.lib.DstabiProvider;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.Locale;

@SuppressLint("InflateParams")
abstract public class BaseActivity extends Activity implements Handler.Callback
{

    /*#############################################*/
	/* ZDE SE MUSI NASTAVIT VERZE APLIKACE          */
	/*#############################################*/
    final protected String APLICATION_MAJOR_VERSION = "1";
    final protected String APLICATION_MINOR1_VERSION = "0";
    final protected String APLICATION_MINOR2_VERSION = "25";

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
	final protected int BANK_CHANGE_CALL_BACK_CODE  = 150;
	final protected int PROFILE_FOR_UPDATE_ORIGINAL = 100;

	final protected int GROUP_GENERAL = 5;
	final protected int OPEN_AUTHOR = 5;
    final protected int OPEN_DIFF = 55;

	final protected int GROUP_HELP = 2;
	final protected int OPEN_MANUAL = 2;
	final protected int OPEN_MANUAL_GOOGLE_DOCS = 3;

	final protected int GROUP_SAVE = 3;
	final protected int SAVE_PROFILE_MENU = 4;

	final protected String MANUAL_URL = "http://spirit-system.com/dl/manual/spirit-manual-"+ APLICATION_MAJOR_VERSION + "." + APLICATION_MINOR1_VERSION + "." + APLICATION_MINOR2_VERSION +"_cz.pdf";
	final protected String MANUAL_URL_GOOGLE_DOCS = "http://docs.google.com/viewer?url=http%3A%2F%2Fspirit-system.com%2Fdl%2Fmanual%2Fspirit-manual-" + APLICATION_MAJOR_VERSION + "." + APLICATION_MINOR1_VERSION + "." + APLICATION_MINOR2_VERSION +"_cz.pdf";
	final protected String DONATE_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=error414%40error414%2ecom&lc=CZ&item_name=spirit%20settings&item_number=spirit%2dsettings&currency_code=CZK&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted";

	final protected BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	ProgressDialog generalDialog;
	StatusNotificationBuilder infoBar;

	final protected String PREF_BASIC_MODE 		= "pref_basic_mode";

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
	 *
	 */
	protected DstabiProfile profileCreator;

    /**
     *
     */
    protected MenuDrawer mDrawer;

    protected SlideMenuListAdapter slideMenuListAdapter;

	/**
	 * zavolani pri vytvoreni instance aktivity servo type
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		stabiProvider = DstabiProvider.getInstance(connectionHandler);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String language = sharedPrefs.getString(PrefsActivity.PREF_APP_LANGUAGE, "none");

		if(!language.equals("none")){
			Resources res = getResources();
		    // Change locale settings in the app.
		    DisplayMetrics dm = res.getDisplayMetrics();
		    android.content.res.Configuration conf = res.getConfiguration();
		    conf.locale = new Locale(language);
		    res.updateConfiguration(conf, dm);
		}
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
		((ImageView) findViewById(R.id.image_title_saved)).setImageResource(Globals.getInstance().isChanged() ? R.drawable.not_equal : R.drawable.equals);

		// check BANKS
		if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED && Globals.getInstance().getActiveBank() != Globals.BANK_NULL ){
			((TextView) findViewById(R.id.title_banks)).setText(TextUtils.concat(getString(R.string.bank_short_code), String.valueOf(Globals.getInstance().getActiveBank())));
            if(slideMenuListAdapter != null){
                slideMenuListAdapter.setActivePosition(Globals.getInstance().getActiveBank());
                slideMenuListAdapter.setDisabledAll(false);
                slideMenuListAdapter.notifyDataSetChanged();
            }
		}else{
			((TextView) findViewById(R.id.title_banks)).setText("");
            if(slideMenuListAdapter != null){
                slideMenuListAdapter.setActivePosition(-1);
                slideMenuListAdapter.setDisabledAll(true);
                slideMenuListAdapter.notifyDataSetChanged();
            }
		}
	}

    /**
     * inicializace slide menu
     *
     * @param layout
     */
    protected void initSlideMenu(int layout){
        mDrawer = MenuDrawer.attach(this);
        mDrawer.setContentView(layout);
        mDrawer.setMenuView(R.layout.left_menu);

        slideMenuListAdapter = new SlideMenuListAdapter(this, getResources().getStringArray(R.array.bank_values));

        ListView leftMenuList = (ListView) findViewById(R.id.leftMenu);
        leftMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED && Globals.getInstance().getActiveBank() != Globals.BANK_NULL ) {
                    if (Globals.getInstance().isChanged()) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(BaseActivity.this);

                        //dont save profile
                        alert.setNegativeButton(R.string.no, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                return;
                            }

                        });

                        //save profile
                        alert.setPositiveButton(R.string.yes, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                saveProfileToUnit(stabiProvider, PROFILE_SAVE_CALL_BACK_CODE);
                            }

                        });

                        alert.setCancelable(true);
                        alert.setMessage(R.string.want_save_item);
                        alert.show();

                        return;
                    }


                    // change bank
                    ProfileItem profileItem;
                    DstabiProfile localProfileCreator = profileCreator;
                    if (profileCreator == null) {
                        localProfileCreator = new DstabiProfile(null);
                        profileItem = localProfileCreator.getProfileItemByName("BANKS");
                    } else {
                        profileItem = profileCreator.getProfileItemByName("BANKS");
                    }

                    profileItem.setValueFromSpinner(position);
                    stabiProvider.sendDataForResponce(profileItem, BANK_CHANGE_CALL_BACK_CODE);
                    checkBankNumber(localProfileCreator);
                    showInfoBarWrite();
                    slideMenuListAdapter.setActivePosition(position);
                    slideMenuListAdapter.notifyDataSetChanged();
                    mDrawer.closeMenu();
                }
            }
        });

        leftMenuList.setAdapter(slideMenuListAdapter);


    }

	/**
	 * check if profile was changed and save to GLobal storage
	 */
	public void checkChange(DstabiProfile profile){
		if(profile == null){
			Globals.getInstance().setChanged(false);
			((ImageView) findViewById(R.id.image_title_saved)).setImageResource(R.drawable.equals);
			return;
		}

		DstabiProfile originalProfile = ChangeInProfile.getInstance().getOriginalProfile();

		Globals.getInstance().setChanged(originalProfile.getCheckSumFromKnowItem() != profile.getCheckSumFromKnowItem());
		((ImageView) findViewById(R.id.image_title_saved)).setImageResource(Globals.getInstance().isChanged() ? R.drawable.not_equal : R.drawable.equals);
	}

	/**
	 *
	 */
	public void checkBankNumber(DstabiProfile profile){
		if(profile == null){
			Globals.getInstance().setActiveBank(Globals.BANK_NULL);
			((TextView) findViewById(R.id.title_banks)).setText("");

            if(slideMenuListAdapter != null){
                slideMenuListAdapter.setActivePosition(-1);
                slideMenuListAdapter.setDisabledAll(true);
                slideMenuListAdapter.notifyDataSetChanged();
            }

			return;
		}

		if(profile.getProfileItemByName("CHANNELS_BANK").getValueInteger() == 7){ // 7 = unbind bank
			((TextView) findViewById(R.id.title_banks)).setText("");
            Globals.getInstance().setActiveBank(Globals.BANK_NULL);
            if(slideMenuListAdapter != null){
                slideMenuListAdapter.setActivePosition(-1);
                slideMenuListAdapter.setDisabledAll(true);
                slideMenuListAdapter.notifyDataSetChanged();
            }

			return;
		}

		Globals.getInstance().setActiveBank(profile.getProfileItemByName("BANKS").getValueInteger());
		((TextView) findViewById(R.id.title_banks)).setText(TextUtils.concat(getString(R.string.bank_short_code), String.valueOf(Globals.getInstance().getActiveBank())));
		((ImageView) findViewById(R.id.image_app_basic_mode)).setImageResource(getAppBasicMode() ? R.drawable.app_basic_mode_on : R.drawable.none);

        if(slideMenuListAdapter != null){
            slideMenuListAdapter.setActivePosition(Globals.getInstance().getActiveBank());
            slideMenuListAdapter.setDisabledAll(false);
            slideMenuListAdapter.notifyDataSetChanged();
        }

	}

	/**
	 * is application in basic mode?
	 *
	 * @return
	 */
	public boolean getAppBasicMode()
	{
		SharedPreferences settings = getSharedPreferences(PREF_BASIC_MODE, Context.MODE_PRIVATE);
		return settings.getBoolean(PREF_BASIC_MODE, false) || Globals.getInstance().getActiveBank() > 0;
	}

	/**
	 * basic mode set on
	 * @param state
	 */
	public void setAppBasicMode(boolean state)
	{
		SharedPreferences settings = getSharedPreferences(PREF_BASIC_MODE, Context.MODE_PRIVATE);

		settings.edit().putBoolean(PREF_BASIC_MODE, state).commit();
		((ImageView) findViewById(R.id.image_app_basic_mode)).setImageResource(getAppBasicMode() ? R.drawable.app_basic_mode_on : R.drawable.none);


		if(state){
			Toast.makeText(getApplicationContext(), R.string.app_basic_mode_on, Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(getApplicationContext(), R.string.app_basic_mode_off, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * cloase all dialog notification
	 */
	protected void closeAllBar()
	{
		closeDialog();
		closeInfoBar();
	}

	/**
	 * close dialog
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
	 * open dialog
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

	/**
	 *
	 * @param text
	 */
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

	/**
	 *
	 * @param profile
	 */
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
		showConfirmDialog(textId, null);
	}

	/**
	 * @param textId
	 */
	protected void showConfirmDialog(int textId, OnClickListener handlerOk)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(BaseActivity.this);
		alert.setPositiveButton("OK", handlerOk);

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
	 * @param graphWarn
	 */
	protected void showConfirmDialogWithCancel(int graphWarn, OnClickListener handlerOk, OnClickListener handlerCancel)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(BaseActivity.this);
		alert.setPositiveButton(R.string.ok, handlerOk);
		alert.setNegativeButton(R.string.cancel, handlerCancel);
		alert.setCancelable(false);

		alert.setMessage(graphWarn);

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
				if(profileCreator != null){
					checkChange(profileCreator);
				}
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

				DstabiProfile profile = new DstabiProfile(msg.getData().getByteArray("data"));

				setOriginalProfileProfile(profile);
				checkChange(profile);

				break;

			case BANK_CHANGE_CALL_BACK_CODE:
				sendInSuccessInfo();
				reloadOriginalProfile();
				break;
		}
		return true;
	}

}
