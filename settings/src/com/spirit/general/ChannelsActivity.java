package com.spirit.general;

import com.exception.IndexOutOfException;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.spirit.BaseActivity;
import com.spirit.R;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class ChannelsActivity extends BaseActivity{
	
	@SuppressWarnings("unused")
	final private String TAG = "ChannelsActivity";
	
	final private int PROFILE_CALL_BACK_CODE = 16;
	
	private final String protocolCode[] = {"CHANNELS_THT", "CHANNELS_AIL", "CHANNELS_ELE", "CHANNELS_RUD", "CHANNELS_GAIN", "CHANNELS_PITH", "CHANNELS_BANK"};

	// gui prvky ktere sou v teto aktivite aktivni
	private int formItems[] = {R.id.tht_select_id, R.id.aile_select_id, R.id.ele_select_id, R.id.run_select_id, R.id.gain_select_id, R.id.pitch_select_id, R.id.bank_select_id};
	
	// gui prvky ktere jsou pri basic mode disablovane
	private int formItemsNotInBasicMode[] = formItems;
	
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
	 * prvotni konfigurace view
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		if (stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED) {
			((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
			initBasicMode();
		} else {
			finish();
		}
	}
	
	/**
	 * disablovani prvku v bezpecnem rezimu
	 */
	protected void initBasicMode()
	{
		for (int item : formItemsNotInBasicMode) {
			Spinner spinner = (Spinner) findViewById(item);
			spinner.setEnabled(!getAppBasicMode());
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
		
		try {
			for (int i = 0; i < formItems.length; i++) {
				Spinner tempSpinner = (Spinner) findViewById(formItems[i]);

				int pos = profileCreator.getProfileItemByName(protocolCode[i]).getValueForSpinner(tempSpinner.getCount());
				if (pos != 0) lock = lock + 1;
				tempSpinner.setSelection(pos);
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
					
					//nejprve zkontrolujeme jestli uz nekde hodnota neni pouzita
					for(int a = 0; a < formItems.length; a++){
						Spinner sp =  (Spinner) findViewById(formItems[a]);
						if(sp.getSelectedItemPosition() == pos && i != a && pos != 7){ // neprirazeno muze mit kolizi, 7 = neprirazeno
							showConfirmDialog(String.format(getResources().getString(R.string.channelColision), i));
							return;
						}
					}
					
					//tady bude kontrola integrity
					ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
					item.setValueFromSpinner(pos);
					stabiProvider.sendDataNoWaitForResponce(item);
					
					showInfoBarWrite();
						
				}
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0)
		{
			// TODO Auto-generated method stub

		}
	};
	
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
				}
				break;
			default:
				super.handleMessage(msg);
		}
		return true;
	}
	
}
