package com.settings;


import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;

import exception.IndexOutOfException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * aktivita na zobrazeni general moznosti nastaveni
 * 
 * @author error414
 *
 */
public class GeneralActivity extends BaseActivity{
	final private String TAG = "GeneralActivity";
	
	//kod pro handle pri ziskani profilu z jednotky
	final private int PROFILE_CALL_BACK_CODE = 16;
	final private int PROFILE_SAVE_CALL_BACK_CODE = 17;
	
	
	private final String protocolCode[] = {
			"POSITION",
			"MODEL",
			"MIX",
			"RECEIVER",
			"CYCLIC_REVERSE",
	};
	
	// gui prvky ktere sou v teto aktivite aktivni
	private int formItems[] = {
		R.id.position_select_id,
		R.id.model_select_id,
		R.id.mix_select_id,
		R.id.receiver_select_id,
		R.id.cyclic_servo_reverse_select_id
	};
	
	private int lock = formItems.length;

	private DstabiProvider stabiProvider;
	
	private DstabiProfile profileCreator;
	
	/**
	 * zavolani pri vytvoreni instance aktivity settings
	 */
	 public void onCreate(Bundle savedInstanceState) 
	 {
		 super.onCreate(savedInstanceState);
		 requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		 setContentView(R.layout.general);
		 
		 getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		 ((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.general_button_text)));
		 
		 stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		 
		 initConfiguration();
		 delegateListener();
	 }
	 
	 /**
	  * prvotni konfigurace view
	  */
	 @Override
	 public void onResume(){
		super.onResume();
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED){
			((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
		}else{
			finish();
		}
	 }
	 
	 /**
	  * prirazeni udalosti k prvkum
	  */
	 private void delegateListener(){
		//nastaveni posluchacu pro formularove prvky
		 for(int i = 0; i < formItems.length; i++){
			 ((Spinner) findViewById(formItems[i])).setOnItemSelectedListener(spinnerListener);
		 }
	 }
	 
	 /**
	  * ziskani profilu z jednotky
	  */
	 private void initConfiguration()
	 {
		 sendInProgressRead();
		 // ziskani konfigurace z jednotky
		 stabiProvider.getProfile(PROFILE_CALL_BACK_CODE);
	 }
	 
	 /**
	  * naplneni formulare
	  * 
	  * @param profile
	  */
	 private void initGuiByProfileString(byte[] profile){
		 profileCreator = new DstabiProfile(profile);
		 
		 if(!profileCreator.isValid()){
			 errorInActivity(R.string.damage_profile);
			 return;
		 }
		 try{
			 for(int i = 0; i < formItems.length; i++){
				Spinner tempSpinner = (Spinner) findViewById(formItems[i]);
				
				int pos = profileCreator.getProfileItemByName(protocolCode[i]).getValueForSpinner(tempSpinner.getCount());
				
				if(pos != 0)lock = lock + 1;
				tempSpinner.setSelection(pos);
			 }
		 }catch(IndexOutOfException e){
				errorInActivity(R.string.damage_profile);
				return;
		 }
	 }
	 
	 protected OnItemSelectedListener spinnerListener = new OnItemSelectedListener(){
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			
			
			if(lock != 0){
				lock -= 1;
				return;
			}
			lock = Math.max(lock - 1, 0);
			
			// prohledani jestli udalost vyvolal znamy prvek
			// pokud prvek najdeme vyhledame si k prvku jeho protkolovy kod a odesleme
			for(int i = 0; i < formItems.length; i++){
				if(parent.getId() == formItems[i]){
					Log.d(TAG, profileCreator.getProfileItemByName(protocolCode[i]).getCommand());
					
					
					ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
					item.setValueFromSpinner(pos);
					stabiProvider.sendDataNoWaitForResponce(item);
					
					sendInProgress();
				}
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	 };
	 
	 
	// The Handler that gets information back from the 
	 private final Handler connectionHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	switch(msg.what){
		        	case DstabiProvider.MESSAGE_SEND_COMAND_ERROR:
						sendInError();
						break;
					case DstabiProvider.MESSAGE_SEND_COMPLETE:
						sendInSuccess();
						break;
	        		case DstabiProvider.MESSAGE_STATE_CHANGE:
						if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
							sendInError();
						}
						break;
	        		case PROFILE_CALL_BACK_CODE:
	        			if(msg.getData().containsKey("data")){
	        				initGuiByProfileString(msg.getData().getByteArray("data"));
	        				sendInSuccess();
	        			}
	        			break;
	        		case PROFILE_SAVE_CALL_BACK_CODE:
	        			sendInSuccess();
	        			showProfileSavedDialog();
	        			break;
	        	}
	        }
	    };
	    
    /**
     * vytvoreni kontextoveho menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	    super.onCreateOptionsMenu(menu);
	    
	    menu.add(GROUP_SAVE, SAVE_PROFILE_MENU, Menu.NONE, R.string.save_profile_to_unit);
	    return true;
    }
    
    /**
     * reakce na kliknuti polozky v kontextovem menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	 super.onOptionsItemSelected(item);
    	//ulozit do jednotky
    	if(item.getGroupId() == GROUP_SAVE && item.getItemId() == SAVE_PROFILE_MENU){
    		saveProfileToUnit(stabiProvider, PROFILE_SAVE_CALL_BACK_CODE);
    	}
    	return false;
    }
}
