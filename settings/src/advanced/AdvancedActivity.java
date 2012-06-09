package advanced;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.helpers.MenuListAdapter;
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
		
		ListView menuList = (ListView) findViewById(R.id.listMenu);
		MenuListAdapter adapter = new MenuListAdapter(this, createArrayForMenuList());
		menuList.setAdapter(adapter);
		menuList.setOnItemClickListener(new OnItemClickListener() {
			 
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	
            	switch(position){
            		case 0://feed forward
            			openCyclicFeedForwardActivity(view);
            			break;
            		case 1://deathband
            			openStickDeathBandActivity(view);
            			break;
            		case 2://rudder dynamic
            			openRudderDynamicActivity(view);
            			break;
            		case 3://rudder mix
            			openRudderMixActivity(view);
            			break;
            		case 4://PID cyclic
            			openPIDCyclicRegulationActivity(view);
            			break;
            		case 5://PID cyclic
            			openPiroOptimalizationActivity(view);
            			break;
            	}
 
            }
        });
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
	 * vytvoreni pole pro adapter menu listu
	 * 
	 * tohle se bude vytvaret dynamicky z pole
	 * 
	 * @return
	 */
	public ArrayList<HashMap<Integer, Integer>> createArrayForMenuList(){
		ArrayList<HashMap<Integer, Integer>> menuListData = new ArrayList<HashMap<Integer, Integer>>();
		//feed forward
		HashMap<Integer, Integer> forward = new HashMap<Integer, Integer>();
		forward.put(TITLE_FOR_MENU, R.string.cyclick_feed_forward);
		forward.put(ICO_RESOURCE_ID, R.drawable.i21);
		menuListData.add(forward);
		
		//deathband
		HashMap<Integer, Integer> deathband = new HashMap<Integer, Integer>();
		deathband.put(TITLE_FOR_MENU, R.string.stick_deathband);
		deathband.put(ICO_RESOURCE_ID, R.drawable.i22);
		menuListData.add(deathband);
		
		//rudder dynamic
		HashMap<Integer, Integer> rudderdynamic = new HashMap<Integer, Integer>();
		rudderdynamic.put(TITLE_FOR_MENU, R.string.rudder_dynamic);
		rudderdynamic.put(ICO_RESOURCE_ID, R.drawable.i23);
		menuListData.add(rudderdynamic);
		
		//rudder mix
		HashMap<Integer, Integer> ruddermix = new HashMap<Integer, Integer>();
		ruddermix.put(TITLE_FOR_MENU, R.string.rudder_revomix);
		ruddermix.put(ICO_RESOURCE_ID, R.drawable.i24);
		menuListData.add(ruddermix);
		
		//PID cyclic
		HashMap<Integer, Integer> pid = new HashMap<Integer, Integer>();
		pid.put(TITLE_FOR_MENU, R.string.pid_cyclic_regulation);
		pid.put(ICO_RESOURCE_ID, R.drawable.i25);
		menuListData.add(pid);
		
		//PID cyclic
		HashMap<Integer, Integer> piro = new HashMap<Integer, Integer>();
		piro.put(TITLE_FOR_MENU, R.string.piro_opt);
		piro.put(ICO_RESOURCE_ID, R.drawable.i26);
		menuListData.add(piro);
		
		return menuListData;
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
		Intent i = new Intent(AdvancedActivity.this, RudderDynamicActivity.class);
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

	/**
	 * 
	 * 
	 * @param v
	 */
	public void openPiroOptimalizationActivity(View v)
	{
		Intent i = new Intent(AdvancedActivity.this, PiroOptimalizationActivity.class);
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
