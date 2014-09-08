package com.spirit;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.lib.FileDialog;
import com.lib.SelectionMode;

import java.util.Locale;

public class PrefsActivity extends PreferenceActivity{
	
	private final String TAG = "PrefsActivity";
	
	public final static int REQUEST_APP_GRAPH_DIR 	= 1;
	public final static int REQUEST_APP_LOG_DIR 	= 2;
	
	public final static String PREF_APP 			= "pref_app";
	public final static String PREF_APP_GRAPH_DIR 	= "pref_app_graph_dir";
	public final static String PREF_APP_LOG_DIR 	= "pref_app_log_dir";
	public final static String PREF_APP_LANGUAGE 	= "prefs_language";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String languageCurent = sharedPrefs.getString(PrefsActivity.PREF_APP_LANGUAGE, "none");
        if(!languageCurent.equals("none")){
            Resources res = getResources();
            // Change locale settings in the app.
            DisplayMetrics dm = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            conf.locale = new Locale(languageCurent);
            res.updateConfiguration(conf, dm);
        }

	   addPreferencesFromResource(R.xml.prefs);
	    
	   final SharedPreferences preferences = getSharedPreferences(PREF_APP, Context.MODE_PRIVATE);
	    
	    //CHOOSE DIR FOR GRAPH
	    final Preference grafDir = (Preference) findPreference("grafDir");
	    grafDir.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
	        @Override
	        public boolean onPreferenceClick(Preference preference) {
				String prefsDir = preferences.getString(PREF_APP_GRAPH_DIR, "/");
				openFileDialogIndent(prefsDir, REQUEST_APP_GRAPH_DIR);
	            return true;
	        }
	    });
	    ////////////////////////////
	    
	    //CHOOSE DIR FOR LOG
	    final Preference logDir = (Preference) findPreference("logDir");
	    logDir.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
	        @Override
	        public boolean onPreferenceClick(Preference preference) {
				String prefsDir = preferences.getString(PREF_APP_LOG_DIR, "/");
				openFileDialogIndent(prefsDir, REQUEST_APP_LOG_DIR);
	            return true;
	        }
	    });
	    ////////////////////////////
	    
	    //// LANGUAGE 
		ListPreference language = (ListPreference) findPreference (PREF_APP_LANGUAGE);
		if(language.getValue() == null){
			language.setValueIndex(0);
		}
	}

    /**
     *
     */
    public void onResume(){
        super.onResume();
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		String newValue = data.getStringExtra(FileDialog.RESULT_PATH);
		SharedPreferences preferences = getSharedPreferences(PREF_APP, Context.MODE_PRIVATE);
		
		SharedPreferences.Editor editor = preferences.edit();
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
	 * 
	 * @param defaultDir
	 * @param callBackId
	 */
	protected void openFileDialogIndent(String defaultDir, int callBackId){
		Intent intent = new Intent(getBaseContext(), FileDialog.class);
    	intent.putExtra(FileDialog.START_PATH, defaultDir);
		intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
		intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
		intent.putExtra(FileDialog.FORMAT_FILTER, new String[]{});
		
		startActivityForResult(intent, callBackId);
	}
	
	

}
