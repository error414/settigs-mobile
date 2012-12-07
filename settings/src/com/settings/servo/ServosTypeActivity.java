package com.settings.servo;

import com.exception.IndexOutOfException;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.settings.BaseActivity;
import com.settings.R;


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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class ServosTypeActivity extends BaseActivity{
	final private String TAG = "ServosTypeActivity";
	
	final private int PROFILE_CALL_BACK_CODE = 16;
	final private int PROFILE_SAVE_CALL_BACK_CODE = 17;
	
	private final String protocolCode[] = {
			"CYCLIC_TYPE",
			"CYCLIC_FREQ",
			"RUDDER_TYPE",
			"RUDDER_FREQ"
	};
	
	private int formItems[] = {
			R.id.cyclic_pulse,
			R.id.cyclic_frequency,
			R.id.rudder_pulse,
			R.id.rudder_frequency
		};
	
	private int lock = formItems.length;
	
	private DstabiProvider stabiProvider;
	
	private DstabiProfile profileCreator;
	
	/**
	 * zavolani pri vytvoreni instance aktivity servo type
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.servos_type);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.servos_button_text), " \u2192 " , getString(R.string.type)));
        
        stabiProvider =  DstabiProvider.getInstance(connectionHandler);
        
        initConfiguration();
		delegateListener();
    }
	
	/**
	 * znovu nacteni aktovity, priradime dstabi svuj handler a zkontrolujeme jestli sme pripojeni
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
	  * prvotni konfigurace view
	  */
	 private void initConfiguration()
	 {
		 showDialogRead();
		 // ziskani konfigurace z jednotky
		 stabiProvider.getProfile(PROFILE_CALL_BACK_CODE);
	 }
	 
	 /**
	  * pri zmenene rudder pulse se podvame jestli neni potreba zmenit i select
	  * rudder frequency
	  * 
	  * @param pos
	  */
	 private void updateItemRudderFrequency(int pos)
	 {
		 ArrayAdapter<?> adapter;
		 Spinner rudderFrequency = (Spinner) findViewById(R.id.rudder_frequency);
		 int freqPos = (int) rudderFrequency.getSelectedItemPosition(); 
		 if(pos == 2){
			 adapter = ArrayAdapter.createFromResource(
		                this, R.array.rudder_frequency_value_extend, android.R.layout.simple_spinner_item);
		 }else{
			 adapter = ArrayAdapter.createFromResource(
		                this, R.array.rudder_frequency_value, android.R.layout.simple_spinner_item);
		 }
		 
		 adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		 rudderFrequency.setAdapter(adapter);
		 rudderFrequency.setSelection(Math.min(freqPos, adapter.getCount() - 1 ));
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
				
				 //TOHLE MUSIM VYRESIT LIP
				 if(tempSpinner.getId() == formItems[2]){
					 updateItemRudderFrequency(profileCreator.getProfileItemByName(protocolCode[i]).getValueForSpinner(tempSpinner.getCount()));
				 }
				 
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
						ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
						item.setValueFromSpinner(pos);
						stabiProvider.sendDataNoWaitForResponce(item);
						
						showInfoBarWrite();
						
						//pro prvrk rudder pulse volame jeste obsluhu zmeny seznamu rudder frequency
						if(parent.getId() == formItems[2]){
							updateItemRudderFrequency(pos);
						}
						
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
			        		Log.d(TAG, "error");
							sendInError();
							break;
						case DstabiProvider.MESSAGE_SEND_COMPLETE:
							sendInSuccessInfo();
							break;
		        		case DstabiProvider.MESSAGE_STATE_CHANGE:
							if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
								sendInError();
							}
							break;
		        		case PROFILE_CALL_BACK_CODE:
		        			if(msg.getData().containsKey("data")){
		        				initGuiByProfileString(msg.getData().getByteArray("data"));
		        				sendInSuccessDialog();
		        			}
		        			break;
		        		case PROFILE_SAVE_CALL_BACK_CODE:
		        			sendInSuccessDialog();
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
