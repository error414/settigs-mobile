package com.spirit;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.helpers.Globals;
import com.lib.FileDialog;
import com.lib.SelectionMode;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class PrefsActivity extends PreferenceActivity {

	private final String TAG = "PrefsActivity";

	public final static int REQUEST_APP_GRAPH_DIR = 1;
	public final static int REQUEST_APP_LOG_DIR = 2;

	public final static String PREF_APP_GRAPH_DIR = "graf_dir";
	public final static String PREF_APP_LOG_DIR = "log_dir";
	public final static String PREF_APP_LANGUAGE = "prefs_language";
	public final static String PREF_APP_CLOUD = "active_cloud";
	/*public final static String PREF_APP_CLOUD_EMAIL = "cloud_login_email";
	public final static String PREF_APP_CLOUD_PASS = "cloud_login_password";*/


	@Override
	protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
        ((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.settings)));

		final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String languageCurent = sharedPrefs.getString(PrefsActivity.PREF_APP_LANGUAGE, "none");
		if (!languageCurent.equals("none")) {
			Resources res = getResources();
			// Change locale settings in the app.
			DisplayMetrics dm = res.getDisplayMetrics();
			android.content.res.Configuration conf = res.getConfiguration();
			conf.locale = new Locale(languageCurent);
			res.updateConfiguration(conf, dm);
		}

		addPreferencesFromResource(R.xml.prefs);


		//CHOOSE DIR FOR GRAPH
		final Preference grafDir = (Preference) findPreference(PREF_APP_GRAPH_DIR);
		grafDir.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String prefsDir = sharedPrefs.getString(PREF_APP_GRAPH_DIR, "/");
				openFileDialogIndent(prefsDir, REQUEST_APP_GRAPH_DIR);
				return true;
			}
		});
		////////////////////////////

		//CHOOSE DIR FOR LOG
		final Preference logDir = (Preference) findPreference(PREF_APP_LOG_DIR);
		logDir.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String prefsDir = sharedPrefs.getString(PREF_APP_LOG_DIR, "/");
				openFileDialogIndent(prefsDir, REQUEST_APP_LOG_DIR);
				return true;
			}
		});
		////////////////////////////

		//// LANGUAGE 
		ListPreference language = (ListPreference) findPreference(PREF_APP_LANGUAGE);
		if (language.getValue() == null) {
			language.setValueIndex(0);
		}

		//// CLOUD
		/*final Preference aciveCloud = (Preference) findPreference(PREF_APP_CLOUD);
		aciveCloud.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference(PREF_APP_CLOUD_EMAIL).setEnabled((Boolean) newValue);
				findPreference(PREF_APP_CLOUD_PASS).setEnabled((Boolean) newValue);
				return true;
			}
		});

		final Preference cloudEmail = (Preference) findPreference(PREF_APP_CLOUD_EMAIL);
		cloudEmail.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary((String) newValue);
                checkCloudLogin(sharedPrefs.getString(PREF_APP_CLOUD_EMAIL, ""), sharedPrefs.getString(PREF_APP_CLOUD_PASS, ""));
				return true;
			}
		});

		final Preference cloudPassword = (Preference) findPreference(PREF_APP_CLOUD_PASS);
		cloudPassword.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(((String) newValue).length() > 0 ? "........." : "");
                checkCloudLogin(sharedPrefs.getString(PREF_APP_CLOUD_EMAIL, ""), sharedPrefs.getString(PREF_APP_CLOUD_PASS, ""));
				return true;
			}
		});


		//// FIRST INICIALIZE
		cloudEmail.setSummary(sharedPrefs.getString(PREF_APP_CLOUD_EMAIL, ""));
		cloudPassword.setSummary(sharedPrefs.getString(PREF_APP_CLOUD_PASS, "").length() > 0 ? "........." : "");

		cloudEmail.setEnabled(sharedPrefs.getBoolean(PREF_APP_CLOUD, false));
		cloudPassword.setEnabled(sharedPrefs.getBoolean(PREF_APP_CLOUD, false));*/
	}

    /**
     *
     * @param v
    */
    public void openOptionsMenu(View v) {
        //openOptionsMenu();
    }

    /**
     * kontrola jestli jsou prihlasovaci udaje spravne
     *
     * @param email
     * @param password
     * @return
     */
    private void checkCloudLogin(String email, String password){
        AlertDialog.Builder alert = new AlertDialog.Builder(PrefsActivity.this);
        alert.setPositiveButton("OK", null);

        if(email.length() > 0 && password.length() > 0){
            // tu dotaz na spravnost loginu
            alert.setMessage("Login OK");
            alert.show();
        }

    }

	/**
	 *
	 */
	public void onResume() {
		super.onResume();

        if(Globals.getInstance().getUnsaveNotify() != null){
            Globals.getInstance().getUnsaveNotify().cancelAll();
        }
        this.stopActivityTransitionTimer();

        ((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.none);
        ((ImageView)findViewById(R.id.image_title_saved)).setImageResource(R.drawable.none);
        ((ImageView)findViewById(R.id.image_app_basic_mode)).setImageResource(R.drawable.none);
        ((TextView)findViewById(R.id.title_banks)).setText("");
        ((ImageView)findViewById(R.id.option_bar)).setImageResource(R.drawable.none);
	}

    @Override
    public void onPause()
    {
        super.onPause();
        if(Globals.getInstance().isChanged()) {
            this.startActivityTransitionTimer();
        }
    }

    /* ################ PROTECT UNSAVE CHANGE ################ */
    /**
     *
     */
    public void startActivityTransitionTimer() {
        Globals.getInstance().setmActivityTransitionTimer(new Timer());
        Globals.getInstance().setmActivityTransitionTimerTask(new TimerTask() {
            public void run() {
                if(Globals.getInstance().getUnsaveNotify() == null){
                    Globals.getInstance().setUnsaveNotify((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE));
                }

                Globals.getInstance().setUnsaveNotify((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE));
                Notification notify=new Notification(android.R.drawable.stat_notify_more,"Unsave change",System.currentTimeMillis());
                PendingIntent pending=PendingIntent.getActivity(getApplicationContext(), 0, new Intent(),0);
                notify.setLatestEventInfo(getApplicationContext(), "test", "body",pending);

                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Globals.getInstance().getUnsaveNotify().notify(0, notify);
            }
        });

        Globals.getInstance().getmActivityTransitionTimer().schedule( Globals.getInstance().getmActivityTransitionTimerTask(),
                Globals.MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    /**
     *
     */
    public void stopActivityTransitionTimer() {
        if (Globals.getInstance().getmActivityTransitionTimerTask() != null) {
            Globals.getInstance().getmActivityTransitionTimerTask().cancel();
        }

        if (Globals.getInstance().getmActivityTransitionTimer() != null) {
            Globals.getInstance().getmActivityTransitionTimer().cancel();
        }
    }
    /* ################################################ */

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		String newValue = data.getStringExtra(FileDialog.RESULT_PATH);
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		SharedPreferences.Editor editor = sharedPrefs.edit();
		switch (requestCode) {
			case REQUEST_APP_GRAPH_DIR:
				if (resultCode == Activity.RESULT_OK) {
					editor.putString(PREF_APP_GRAPH_DIR, newValue);
					editor.commit();
				}
				break;
			case REQUEST_APP_LOG_DIR:
				if (resultCode == Activity.RESULT_OK) {
					editor.putString(PREF_APP_LOG_DIR, newValue);
					editor.commit();
				}
				break;
		}
	}

	/**
	 * @param defaultDir
	 * @param callBackId
	 */
	protected void openFileDialogIndent(String defaultDir, int callBackId) {
		Intent intent = new Intent(getBaseContext(), FileDialog.class);
		intent.putExtra(FileDialog.START_PATH, defaultDir);
		intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
		intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
		intent.putExtra(FileDialog.FORMAT_FILTER, new String[]{});

		startActivityForResult(intent, callBackId);
	}


}
