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


import com.helpers.StatusNotificationBuilder;
import com.lib.DstabiProvider;
import com.spirit.R;
import com.spirit.R.id;
import com.spirit.servo.ServosActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

abstract public class BaseActivity extends Activity{
	
	//for debug
	private final String TAG = "BaseActivity";
	
	/**
	 * klice pro adapter aby vedel k jakemu prvku jaky klic priradit
	 */

	public static Integer TITLE_FOR_MENU = 1;  
	public static Integer ICO_RESOURCE_ID = 2;  
	
	// Intent request codes
    private static final int REQUEST_ENABLE_BT = 22;

    final protected int GROUP_GENERAL = 5;
    final protected int OPEN_AUTHOR = 5;
    
    
	final protected int GROUP_HELP = 2;  
	final protected int OPEN_MANUAL = 2;  
	final protected int OPEN_MANUAL_GOOGLE_DOCS = 3;
	
	final protected int GROUP_SAVE = 3;  
	final protected int SAVE_PROFILE_MENU = 4;  
	
	final protected String MANUAL_URL = "http://spirit-system.com/dl/manual/spirit-manual-1.0.18.pdf";  
	final protected String MANUAL_URL_GOOGLE_DOCS = "http://docs.google.com/viewer?url=http%3A%2F%2Fspirit-system.com%2Fdl%2Fmanual%2Fspirit-manual-1.0.18.pdf";  
	
	final protected  BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	ProgressDialog generalDialog;
	StatusNotificationBuilder infoBar;
	
	// TOHLE PUJDE do DSTABI PROFILE ASI :D
	final protected String SAVE_PROFILE = "g";
	
	/**
	 * pocitadlo otevreni dialog boxu
	 */
	public int progressCount = 0;
	
	/**
	 * pocitadlo otevreni info boxu
	 */
	public int progressInfoCount = 0;
	
	@Override
	protected void onStart() {
		super.onStart();
		if(mBluetoothAdapter == null){
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
	public void onStop(){
		closeAllBar();
		super.onStop();
	}
	
	/**
	 * zavreni vsechn notofikaci a dialogu
	 */
	protected void closeAllBar(){
		closeDialog();
		closeInfoBar();
	}
	
	/**
	 * zavreni dialogu
	 */
	protected void closeDialog(){
		progressCount = 0;
		if(generalDialog != null){
			 generalDialog.hide();
			 generalDialog.cancel();
		 }
	}
	
	protected void closeInfoBar(){
		progressInfoCount = 0;
		if(infoBar != null){
			infoBar.hide();
		 }
	}
	
	/**
	 * otevreni dialogu
	 * 
	 * @param text
	 */
	protected void showDialog(String text){
		progressCount++;
		if(generalDialog == null || !generalDialog.isShowing()){
			generalDialog = ProgressDialog.show(BaseActivity.this, "", text, true);
	 	}
	}
	
	protected void showInfoBar(String text){
		progressInfoCount++;
		if(infoBar == null || !infoBar.isShowing()){
			if(infoBar == null){
				infoBar = new StatusNotificationBuilder(getApplicationContext(), getWindow());
			}
			infoBar.setText(text);
			infoBar.show();
	 	}
	}
	
	/**
	 * zobrazeni dialogu pri cteni dat z jednotky
	 */
	protected void showDialogRead(){
		showDialog(getString(R.string.read_please_wait));
	}
	
	/**
	 * zobrazeni dialogu pri zapisovani dat z jednotky
	 */
	protected void showDialogWrite(){
		showDialog(getString(R.string.write_please_wait));
	}
	
	/**
	 * zobrazeni dialogu pri cteni dat z jednotky
	 */
	protected void showInfoBarRead(){
		showInfoBar(getString(R.string.read_data));
	}
	
	/**
	 * zobrazeni dialogu pri zapisovani dat z jednotky
	 */
	protected void showInfoBarWrite(){
		showInfoBar(getString(R.string.write_data));
	}
	
	 
	/**
	 * pri dokonceni odesilani dat
	 * aby zmenila gui tak aby slo znova odeslat dasli pozadavek
	 */
	protected void sendInSuccessDialog(){
		progressCount--;
		if(progressCount <= 0){
			closeDialog();
			progressCount = 0;
		}
	 }
	
	/**
	 * pri dokonceni odesilani dat
	 * aby zmenila gui tak aby slo znova odeslat dasli pozadavek
	 */
	protected void sendInSuccessInfo(){
		progressInfoCount--;
		if(progressInfoCount <= 0){
			closeInfoBar();
			progressInfoCount = 0;
		}
	 }
	 
	 /**
	  * pri chybe pozadavku 
	  * 
	  * @param finishActivity
	  */
	 protected void sendInError(){
		 sendInError(true);
	 }
	 
	 /**
	  * pri chybe pozadavku 
	  * 
	  * @param finishActivity
	  */
	 protected void sendInError(Boolean finishActivity){
		Toast.makeText(getApplicationContext(), R.string.connection_error, Toast.LENGTH_SHORT).show();
		closeAllBar();
		if(finishActivity){
			finish();
		}
	 }
	 
	 protected void errorInActivity(int idText){
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
		 stabiProvider.sendDataForResponce(SAVE_PROFILE, call_back_code);
	 }
	
	 /**
	  * otevreni napovedy, dialog box
	  * 
	  * @param resourceTextId
	  * @param resourceTitleId
	  */
	 protected void openHelp(int resourceTextId)
	 {
		 Intent i = new Intent(this, HelpActivity.class);
		 i.putExtra("content", resourceTextId);

		 startActivity(i);
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
	    return true;
    }
    
    /**
     * reakce na kliknuti polozky v kontextovem menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	//otevrit manual google docs/primo
    	if(item.getGroupId() == GROUP_HELP && (item.getItemId() == OPEN_MANUAL || item.getItemId() == OPEN_MANUAL_GOOGLE_DOCS)){
    		
    		String url = "";
    		if(item.getItemId() == OPEN_MANUAL_GOOGLE_DOCS){
    			url = this.MANUAL_URL_GOOGLE_DOCS;
    		}else{
    			url = this.MANUAL_URL;
    		}
    	      
    	    Intent i = new Intent(Intent.ACTION_VIEW);  
    	    i.setData(Uri.parse(url));  
    	    startActivity(i);  
    	}
    	
    	//otevreni informace o autorovi
    	if(item.getGroupId() == GROUP_GENERAL && item.getItemId() == OPEN_AUTHOR){
    		Intent i = new Intent(this, AuthorActivity.class);
			startActivity(i);
    	}
    	
    	
    	return false;
    }
    
    /**
     * zachytavani vysledku z aktivit
     * 
     */
    public synchronized void onActivityResult(final int requestCode,
        int resultCode, final Intent data) {
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
	  * otevreni napovedy pro position
	  * 
	  * @param V
	  */
	 public void showHelp(View v)
	 {
		 
		 /*switch(v.getId()){
		 	case R.id.position_help: 
		 		this.openHelp(R.layout.help_test);	
		 		break;
		 	case R.id.model_help: 
		 		this.openHelp(R.layout.help_test);	
		 		break;
		 	default:
		 		this.openHelp(R.layout.help_test);	
		 		break;
		 }*/
	 }
	 
	 protected void showProfileSavedDialog()
	 {
		 AlertDialog.Builder alert = new AlertDialog.Builder(BaseActivity.this);
		 alert.setPositiveButton("OK", null);
		
		 alert.setView(getLayoutInflater().inflate(R.layout.alert_done, null));
		 
		 alert.show();
		
	 }
    
}
