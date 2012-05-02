package com.settings;


import com.lib.DstabiProvider;
import com.settings.R.id;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
	
	// Intent request codes
    private static final int REQUEST_ENABLE_BT = 22;

	final protected int GROUP_HELP = 2;  
	final protected int OPEN_MANUAL = 2;  
	final protected int OPEN_MANUAL_GOOGLE_DOCS = 3;
	
	final protected int GROUP_SAVE = 3;  
	final protected int SAVE_PROFILE_MENU = 4;  
	
	final protected String MANUAL_URL = "http://4dstabi.com/dl/manual/4dstabi-manual-1.0.pdf";  
	final protected String MANUAL_URL_GOOGLE_DOCS = "http://docs.google.com/viewer?url=http%3A%2F%2F4dstabi.com%2Fdl%2Fmanual%2F4dstabi-manual-1.0.pdf";  
	
	final protected  BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	ProgressDialog generalDialog;
	
	// TOHLE PUJDE do DSTABI PROFILE ASI :D
	final protected String SAVE_PROFILE = "g";
	
	private int progressCount = 0;
	
	@Override
	protected void onStart() {
		super.onStart();
		if(mBluetoothAdapter == null){
			Toast.makeText(getApplicationContext(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
			finish();
		}
		
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
	}
	
	@Override
	 public void onStop(){
		closeDialog();
		 super.onStop();
	 }
	
	protected void closeDialog(){
		progressCount = 0;
		if(generalDialog != null){
			 generalDialog.hide();
			 generalDialog.cancel();
		 }
	}
	
	
	protected void sendInProgressRead(){
		sendInProgress(getString(R.string.read_please_wait));
	}
	
	/**
	  * pri odesilani dat se zavolata tato fukce 
	  * aby zmenila gui tak aby neslo odeslat dasli pozadavek
	  */
	 protected void sendInProgress(){
		 sendInProgress(getString(R.string.write_please_wait));
	 }
	 
	 /**
	  * pri odesilani dat se zavolata tato fukce 
	  * aby zmenila gui tak aby neslo odeslat dasli pozadavek
	  */
	 protected void sendInProgress(int text){
		 sendInProgress(getString(text));
	 }
	 
	 /**
	  * pri odesilani dat se zavolata tato fukce 
	  * aby zmenila gui tak aby neslo odeslat dasli pozadavek
	  */
	 private void sendInProgress(String text){
		 progressCount++;
		 if(generalDialog == null || !generalDialog.isShowing()){
			 generalDialog = ProgressDialog.show(BaseActivity.this, "", text, true);
	 	 }
	 }
	 
	 /**
	  * pri dokonceni odesilani dat
	  * aby zmenila gui tak aby slo znova odeslat dasli pozadavek
	  */
	 protected void sendInSuccess(){
		 progressCount--;
		 if(progressCount <= 0){
			 closeDialog();
			 progressCount = 0;
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
		closeDialog();
		if(finishActivity){
			finish();
		}
	 }
	 
	 protected void errorInActivity(int idText){
		 Toast.makeText(getApplicationContext(), idText, Toast.LENGTH_SHORT).show();
		 closeDialog();
		 finish();
	 }
	 
	 /**
	  * save profile to unit
	  */
	 protected void saveProfileToUnit(DstabiProvider stabiProvider, int call_back_code)
	 {
		 sendInProgress();
		 // ziskani konfigurace z jednotky
		 stabiProvider.sendDataForResponce(SAVE_PROFILE, call_back_code);
	 }
	
	 /**
	  * otevreni napovedy, dialog box
	  * 
	  * @param resourceTextId
	  * @param resourceTitleId
	  */
	 protected void openHelp(int resourceTextId, int resourceTitleId)
	 {
		 final Dialog dialog = new Dialog(BaseActivity.this);
         dialog.setContentView(R.layout.help_dialog);
         dialog.setTitle(resourceTitleId);
         dialog.setCancelable(true);
         
         TextView textDialog = (TextView) dialog.findViewById(R.id.help_text_area);
         textDialog.setText(resourceTextId); 
         
         //nastaveni zavreni dialogu na button
         Button helpCancelbutton = (Button) dialog.findViewById(R.id.cancel_help);
         helpCancelbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
         
         dialog.show();
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
		 
		 switch(v.getId()){
		 	case R.id.position_help: 
		 		this.openHelp(R.string.position_help, R.string.position_text);	
		 		break;
		 	case R.id.model_help: 
		 		this.openHelp(R.string.model_help, R.string.model_text);	
		 		break;
		 	default:
		 		this.openHelp(R.string.no_help, R.string.no_help_text);	
		 		break;
		 }
	 }
	 
	 protected void showProfileSavedDialog()
	 {
		 AlertDialog.Builder alert = new AlertDialog.Builder(BaseActivity.this);
		 alert.setPositiveButton("OK", null);
		
		 alert.setView(getLayoutInflater().inflate(R.layout.alert_done, null));
		 
		 alert.show();
		
	 }
    
}
