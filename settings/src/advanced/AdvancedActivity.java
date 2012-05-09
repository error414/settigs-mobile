package advanced;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.settings.BaseActivity;
import com.settings.R;
import com.settings.servo.ServosActivity;
import com.settings.servo.ServosTypeActivity;

public class AdvancedActivity extends BaseActivity{
	final private String TAG = "AdvancedActivity";
	
	private DstabiProvider stabiProvider;
	/**
	 * zavolani pri vytvoreni instance aktivity servos
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.advanced);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.advanced_button_text)));
        
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
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
	 * 
	 * 
	 * @param v
	 */
	public void openCyclicFeedForwardActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, CyclicFeedForwardActivity.class);
    	startActivity(i);
	}
	
	/**
	 * 
	 * 
	 * @param v
	 */
	public void openStickDeathBandActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, StickDeathBandActivity.class);
    	startActivity(i);
	}
	
	/**
	 * 
	 * 
	 * @param v
	 */
	public void openRudderDynamicActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, StickDeathBandActivity.class);
		startActivity(i);
	}
	
	/**
	 * 
	 * 
	 * @param v
	 */
	public void openRudderMixActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, RudderRevomixActivity.class);
		startActivity(i);
	}
	
	/**
	 * 
	 * 
	 * @param v
	 */
	public void openPIDCyclicRegulationActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, PIDCyclicRegulationActivity.class);
		startActivity(i);
	}
	
	// The Handler that gets information back from the 
	 private final Handler connectionHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	switch(msg.what){
	        		case DstabiProvider.MESSAGE_STATE_CHANGE:
						if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
							sendInError();
							finish();
						}else{
							((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
						}
						break;
	        	}
	        }
	    };
}
