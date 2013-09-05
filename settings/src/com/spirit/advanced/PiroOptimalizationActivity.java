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

package com.spirit.advanced;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.helpers.ByteOperation;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.spirit.R;
import com.spirit.BaseActivity;

public class PiroOptimalizationActivity extends BaseActivity{

	@SuppressWarnings("unused")
	final private String TAG = "PiroOptimalizationActivity";
	
	final private int PROFILE_CALL_BACK_CODE = 16;
	final private int PROFILE_SAVE_CALL_BACK_CODE = 17;
	
	private final String protocolCode[] = {
			"PIRO_OPT",
	};
	
	private int formItems[] = {
			R.id.piro_opt,
		};
	
	private DstabiProvider stabiProvider;
	
	private DstabiProfile profileCreator;
	
	private int lock = 0;
	/**
	 * zavolani pri vytvoreni instance aktivity servo type
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.advanced_piro_opt);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat("... \u2192 " , getString(R.string.advanced_button_text), " \u2192 " , getString(R.string.piro_opt)));
        
        stabiProvider =  DstabiProvider.getInstance(connectionHandler);
        
       initConfiguration();
       delegateListener();
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
	  * prirazeni udalosti k prvkum
	  */
	 private void delegateListener(){
		//nastaveni posluchacu pro formularove prvky
		 for(int i = 0; i < formItems.length; i++){
			 ((CheckBox) findViewById(formItems[i])).setOnCheckedChangeListener(checkboxListener);
		 }
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
			stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0x02)); //povoleni nastaveni optimalizace
		}else{
			finish();
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
		stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0xff)); //zakazani nastaveni optimalizace
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
		
		for(int i = 0; i < formItems.length; i++){
			CheckBox tempCheckbox = (CheckBox) findViewById(formItems[i]);
			
			Boolean checked = profileCreator.getProfileItemByName(protocolCode[i]).getValueForCheckBox();
			if(checked)lock = lock + 1;
			tempCheckbox.setChecked(checked);
		}
	 }
	 
	 
	 private OnCheckedChangeListener checkboxListener = new OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			
			if(lock != 0){
				lock -= 1;
				return;
			}
			lock = Math.max(lock - 1, 0);
			
			// TODO Auto-generated method stub
			// prohledani jestli udalost vyvolal znamy prvek
			// pokud prvek najdeme vyhledame si k prvku jeho protkolovy kod a odesleme
			for(int i = 0; i < formItems.length; i++){
				if(buttonView.getId() == formItems[i]){
					ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
					item.setValueFromCheckBox(isChecked);
					stabiProvider.sendDataNoWaitForResponce(item);
					
					showInfoBarWrite();
				}
			}
			
		}
		
	};
	
	// The Handler that gets information back from the 
	 private final Handler connectionHandler = new Handler(new Handler.Callback() {
		    @Override
		    public boolean handleMessage(Message msg) {
	        	switch(msg.what){
		        	case DstabiProvider.MESSAGE_SEND_COMAND_ERROR:
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
	        
	        return true;
	    }
	});
    
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
