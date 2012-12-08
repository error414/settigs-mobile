package com.settings.diagnostic;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import android.R.integer;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.helpers.ByteOperation;
import com.helpers.MenuListAdapter;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.settings.BaseActivity;
import com.settings.R;
import com.settings.servo.ServosActivity;
import com.settings.servo.ServosTypeActivity;

public class DiagnosticActivity extends BaseActivity{
	final private String TAG = "DiagnosticActivity";
	
	final private int DIAGNOSTIC_CALL_BACK_CODE = 21;
	final private int PROFILE_SAVE_CALL_BACK_CODE = 22;
	
	private DstabiProvider stabiProvider;
	/**
	 * zavolani pri vytvoreni instance aktivity servos
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.diagnostic);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.advanced_button_text)));
        
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		
		getPositionFromUnit();
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
	 * ziskani informace o poloze kniplu z jednotky
	 */
	protected void getPositionFromUnit(){
		stabiProvider.getDiagnostic(DIAGNOSTIC_CALL_BACK_CODE);
	}
	
	protected void updateGui(byte[] b){
		
		//AILERON
		int aileron = ByteOperation.twoByteToSigInt(b[0], b[1]);
		int aileronPercent = Math.round((100f / 340f) *  aileron);
		((ProgressBar)findViewById(R.id.aileron_progress_diagnostic)).setProgress(Math.round(aileronPercent + 100));
		((TextView)findViewById(R.id.aileron_value_diagnostic)).setText(String.valueOf(aileronPercent));
		
		//ELEVATOR
		int elevator = ByteOperation.twoByteToSigInt(b[2], b[3]);
		int elevatorPercent = Math.round((100f / 340f) *  elevator);
		((ProgressBar)findViewById(R.id.elevator_progress_diagnostic)).setProgress(Math.round(elevatorPercent + 100));
		((TextView)findViewById(R.id.elevator_value_diagnostic)).setText(String.valueOf(elevatorPercent));
		
		//RUDDER
		int rudder = ByteOperation.twoByteToSigInt(b[6], b[7]);
		int rudderPercent = Math.round((100f / 340f) *  rudder);
		((ProgressBar)findViewById(R.id.rudder_progress_diagnostic)).setProgress(Math.round(rudderPercent + 100));
		((TextView)findViewById(R.id.rudder_value_diagnostic)).setText(String.valueOf(rudderPercent));
		
		//PITCH
		int pitch = ByteOperation.twoByteToSigInt(b[4], b[5 ]);
		int pitchPercent = Math.round((100f / 340f) *  pitch);
		((ProgressBar)findViewById(R.id.pitch_progress_diagnostic)).setProgress(Math.round(pitchPercent + 100));
		((TextView)findViewById(R.id.pitch_value_diagnostic)).setText(String.valueOf(pitchPercent));
	}
	
	
	
	// The Handler that gets information back from the 
	 private final Handler connectionHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	switch(msg.what){
		        	case DstabiProvider.MESSAGE_SEND_COMAND_ERROR:
		        		Log.d(TAG, "Prisla chyba");
		        		getPositionFromUnit();
		        		
		        		//sendInError();
						break;
					case DstabiProvider.MESSAGE_SEND_COMPLETE:
						sendInSuccessInfo();
						break;
	        		case DstabiProvider.MESSAGE_STATE_CHANGE:
						if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
							sendInError();
						}
						break;
	        		case DIAGNOSTIC_CALL_BACK_CODE:
	        			if(msg.getData().containsKey("data")){
	        				
	        				if(msg.getData().getByteArray("data").length > 16){
	        					Log.d(TAG, "Odpoved delsi nez 16");
	        				}
	        				
	        				updateGui(msg.getData().getByteArray("data"));
	        				
	        				getPositionFromUnit();
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
