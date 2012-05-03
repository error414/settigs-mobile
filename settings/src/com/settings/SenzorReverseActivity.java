package com.settings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.helpers.DstabiProfile;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;

public class SenzorReverseActivity extends BaseActivity{

final private String TAG = "SenzorReverseActivity";
	
	final private int PROFILE_CALL_BACK_CODE = 16;
	final private int PROFILE_SAVE_CALL_BACK_CODE = 17;
	
	private final String protocolCode[] = {
	};
	
	private int formItems[] = {
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
        setContentView(R.layout.senzor_reverse);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.senzor_button_text), " \u2192 " , getString(R.string.reverse)));
        
        //stabiProvider =  DstabiProvider.getInstance(connectionHandler);
        
       /* initGui();
        initConfiguration();
		delegateListener();*/
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
	        				//initGuiByProfileString(msg.getData().getByteArray("data"));
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
