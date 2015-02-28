package com.spirit.general;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.exception.IndexOutOfException;
import com.google.analytics.tracking.android.EasyTracker;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.spirit.BaseActivity;
import com.spirit.R;

import java.util.ArrayList;

public class ChannelsActivity extends BaseActivity{
	
	@SuppressWarnings("unused")
	final private String TAG = "ChannelsActivity";
	
	final private int PROFILE_CALL_BACK_CODE = 16;
	
	private final String protocolCode[] = {"CHANNELS_THT", "CHANNELS_AIL", "CHANNELS_ELE", "CHANNELS_RUD", "CHANNELS_GAIN", "CHANNELS_PITH", "CHANNELS_BANK"};

	// gui prvky ktere sou v teto aktivite aktivni
	private int formItems[] = {R.id.tht_select_id, R.id.aile_select_id, R.id.ele_select_id, R.id.run_select_id, R.id.gain_select_id, R.id.pitch_select_id, R.id.bank_select_id};
	
	private int lock = formItems.length;
	
	/**
	 * zavolani pri vytvoreni instance aktivity settings
	 */
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.channels);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.general_button_text), " \u2192 ", getString(R.string.channels)));

		initConfiguration();
		delegateListener();
	}

    /**
     *
     * @return
     */
    public int[] getFormItems() {
        return formItems;
    }

    /**
     *
     * @return
     */
    public String[] getProtocolCode() {
        return protocolCode;
    }

    /**
     *
     */
    protected int getDefaultValueType(){
        return DEFAULT_VALUE_TYPE_SPINNER;
    }
	
	/**
	 * prvotni konfigurace view
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		if (stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED) {
			((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
            initDefaultValue();
		} else {
			finish();
		}
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
           if(!checkColision()){
               return false;
           }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * kontrola kolizi v kanalech
     *
     * @return
     */
    public boolean checkColision(){
        //zkontrolujme konzistenci kanalu
        ArrayList<Integer> positions = new ArrayList<Integer>();
        for(int a = 0; a < formItems.length; a++){
            Spinner sp =  (Spinner) findViewById(formItems[a]);
            positions.add(sp.getSelectedItemPosition());
        }


        for(int a = 0; a < positions.size(); a++){
            for(int sub_a = 0; sub_a < positions.size(); sub_a++) {
                if (positions.get(a) == positions.get(sub_a) && a != sub_a && positions.get(sub_a) != 7) {
                    showConfirmDialog(String.format(getResources().getString(R.string.channelColision), positions.get(sub_a) + 1 ));
                    return false;
                }
            }
        }
        return true;
    }

	/**
	 * disablovani prvku v bezpecnem rezimu
	 */
	protected void initBasicMode()
	{
		for (int i = 0; i < formItems.length; i++) {
			Spinner spinner = (Spinner) findViewById(formItems[i]);
			ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
			
			spinner.setEnabled(!(getAppBasicMode() && item.isDeactiveInBasicMode()));
		}
	}
	
	/**
	 * disablovani prvku v zavislosti na vyberu typu prijmace
	 */
	protected void initByTypeReceiver()
	{
		if(profileCreator.getProfileItemByName("RECEIVER").getValueInteger() == 65){ // 65 je A coz je PWM prijmac
			int ppmDisallow[] = {R.id.tht_select_id, R.id.aile_select_id, R.id.ele_select_id, R.id.run_select_id, R.id.pitch_select_id};
			
			//nepovolime menit kanaly
			for (int item : ppmDisallow) {
				Spinner spinner = (Spinner) findViewById(item);
				spinner.setEnabled(false);
			}
		}
	}
	
	/**
	 * prirazeni udalosti k prvkum
	 */
	private void delegateListener()
	{
		//nastaveni posluchacu pro formularove prvky
		for (int i = 0; i < formItems.length; i++) {
			((Spinner) findViewById(formItems[i])).setOnItemSelectedListener(spinnerListener);
		}
	}
	
	/**
	 * ziskani profilu z jednotky
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
	private void initGuiByProfileString(byte[] profile)
	{
		profileCreator = new DstabiProfile(profile);
			
		if (!profileCreator.isValid()) {
			errorInActivity(R.string.damage_profile);
			return;
		}
		
		initByTypeReceiver();
		checkBankNumber(profileCreator);
		initBasicMode();
		
		try {
			for (int i = 0; i < formItems.length; i++) {
				Spinner tempSpinner = (Spinner) findViewById(formItems[i]);

				int pos = profileCreator.getProfileItemByName(protocolCode[i]).getValueForSpinner(tempSpinner.getCount());
				if (pos != tempSpinner.getSelectedItemPosition()) lock = lock + 1;
				tempSpinner.setSelection(pos);

                if(profileCreator.getProfileItemByName("RECEIVER").getValueInteger() == 65){// 65 je A coz je PWM prijmac,
                    switch (i){
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 5: ((Spinner) findViewById(formItems[i])).setEnabled(false); break;
                    }
                }

			}
		} catch (IndexOutOfException e) {
			errorInActivity(R.string.damage_profile);
			return;
		}
	}
	
	protected OnItemSelectedListener spinnerListener = new OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			if (lock != 0) {
				lock -= 1;
				return;
			}
			lock = Math.max(lock - 1, 0);

			// prohledani jestli udalost vyvolal znamy prvek
			// pokud prvek najdeme vyhledame si k prvku jeho protkolovy kod a odesleme
			for (int i = 0; i < formItems.length; i++) {
				if (parent.getId() == formItems[i]) {

					//pro PWM jsou povoleny jen kanaly 5 a 7
					if(profileCreator.getProfileItemByName("RECEIVER").getValueInteger() == 65 && (pos != 4 && pos != 7)){ // 65 je A coz je PWM prijmac, hodnoty 4 = kanal 5, 7 = neprirazeno
						Spinner sp =  (Spinner) findViewById(formItems[i]);
						sp.setSelection(7); // neprirazeno
						return;
					}

                    //pro DSM musi byt prirazen kanal
                    if(profileCreator.getProfileItemByName("RECEIVER").getValueInteger() == 67 && pos == 7 && i == 0){ // 67 je A coz je DXM prijmac,  7 = neprirazeno
                        Spinner sp =  (Spinner) findViewById(formItems[i]);
                        sp.setSelection(0); // kanal 1.
                        return;
                    }
					
					ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
					item.setValueFromSpinner(pos);
					stabiProvider.sendDataNoWaitForResponce(item);
					
					if(protocolCode[i] == "CHANNELS_BANK"){
						checkBankNumber(profileCreator); // na tomto banky zavisi takze po zmene musime volat
					}
					
					showInfoBarWrite();
						
				}
			}
            initDefaultValue();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0)
		{
			// TODO Auto-generated method stub

		}
	};

    /**
     * reakce na kliknuti polozky v kontextovem menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        //otevreni informace o autorovi
        if (item.getGroupId() == GROUP_GENERAL && item.getItemId() == OPEN_AUTHOR) {
            if(!checkColision()){
                return false;
            }
        }

        //ulozit do jednotky
        if (item.getGroupId() == GROUP_SAVE && item.getItemId() == SAVE_PROFILE_MENU) {
            if(!checkColision()){
                return false;
            }
        }


        return super.onOptionsItemSelected(item);
    }
	
	/**
	 * obsluha callbacku
	 *
	 * @param msg
	 * @return
	 */
	public boolean handleMessage(Message msg)
	{
		switch (msg.what) {
			case PROFILE_CALL_BACK_CODE:
				if (msg.getData().containsKey("data")) {
					initGuiByProfileString(msg.getData().getByteArray("data"));
					sendInSuccessDialog();
                    initDefaultValue();
				}
				break;
			case BANK_CHANGE_CALL_BACK_CODE:
				initConfiguration();
				super.handleMessage(msg);
				break;
			default:
				super.handleMessage(msg);
		}
		return true;
	}

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
	
}
