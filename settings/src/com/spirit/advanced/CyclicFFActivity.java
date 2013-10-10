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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.customWidget.picker.ProgresEx;
import com.customWidget.picker.ProgresEx.OnChangedListener;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.spirit.R;
import com.spirit.BaseActivity;

public class CyclicFFActivity extends BaseActivity{

	final private String TAG = "CyclicFFActivity";
	
	final private int PROFILE_CALL_BACK_CODE = 16;
	final private int PROFILE_SAVE_CALL_BACK_CODE = 17;
	
	private final String protocolCode[] = {
			"CYCLIC_FF",
	};
	
	private int formItems[] = {
			R.id.cyclicff,
		};
	
	private int formItemsTitle[] = {
			R.string.cyclic_ff,
		};
	
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
        setContentView(R.layout.advanced_cyclic_ff);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat("... \u2192 " , getString(R.string.advanced_button_text), " \u2192 "));
        
        stabiProvider =  DstabiProvider.getInstance(connectionHandler);
        
        initGui();
        initConfiguration();
		delegateListener();
    }
	
	/**
	 * znovu nacteni aktivity, priradime dstabi svuj handler a zkontrolujeme jestli sme pripojeni
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
	
	private void initGui()
	{
		for(int i = 0; i < formItems.length; i++){
			 ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
			 tempPicker.setTitle(formItemsTitle[i]); // nastavime titulek
		 }
	}
	
	/**
	  * prirazeni udalosti k prvkum
	  */
	 private void delegateListener(){
		//nastaveni posluchacu pro formularove prvky
		 for(int i = 0; i < formItems.length; i++){
			 ((ProgresEx) findViewById(formItems[i])).setOnChangeListener(numberPicekrListener);
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
			 ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
			 ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
			 
			 tempPicker.setRange(item.getMinimum(), item.getMaximum()); // nastavuji rozmezi prvku z profilu
			 tempPicker.setCurrentNoNotify(item.getValueInteger());
		 }
				
	 }
	 
	 protected OnChangedListener numberPicekrListener = new OnChangedListener(){


			@Override
			public void onChanged(ProgresEx parent, int newVal) {
				// TODO Auto-generated method stub
				// prohledani jestli udalost vyvolal znamy prvek
				// pokud prvek najdeme vyhledame si k prvku jeho protkolovy kod a odesleme
				for(int i = 0; i < formItems.length; i++){
					if(parent.getId() == formItems[i]){
						showInfoBarWrite();
						ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
						item.setValue(newVal);
						Log.d(TAG, String.valueOf(newVal));
						stabiProvider.sendDataNoWaitForResponce(item);
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
