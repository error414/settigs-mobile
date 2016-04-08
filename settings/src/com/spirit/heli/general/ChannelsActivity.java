package com.spirit.heli.general;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.exception.IndexOutOfException;
import com.google.analytics.tracking.android.EasyTracker;
import com.helpers.ByteOperation;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.helpers.SerialNumber;
import com.lib.CommandService;
import com.lib.DstabiProvider;
import com.spirit.BaseActivity;
import com.spirit.R;

import java.util.ArrayList;

public class ChannelsActivity extends BaseActivity{
	
	@SuppressWarnings("unused")
	final private String TAG = "ChannelsActivity";

	final private int GET_SERIAL_NUMBER = 118;

	final private int PROFILE_CALL_BACK_CODE = 16;
	final private int DIAGNOSTIC_CALL_BACK_CODE = 21;
	final private int FAILSAFE_CALL_BACK_CODE = 200;
	
	private final String protocolCode[] = {"CHANNELS_THT", "CHANNELS_AIL", "CHANNELS_ELE", "CHANNELS_RUD", "CHANNELS_GAIN", "CHANNELS_PITH", "CHANNELS_BANK"};

	// gui prvky ktere sou v teto aktivite aktivni
	private int formItems[] = {R.id.tht_select_id, R.id.aile_select_id, R.id.ele_select_id, R.id.run_select_id, R.id.gain_select_id, R.id.pitch_select_id, R.id.bank_select_id};
	
	private int lock = formItems.length;

    private SerialNumber serial;

	private int counter = 0;
	
	/**
	 * zavolani pri vytvoreni instance aktivity settings
	 */
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		initSlideMenu(R.layout.channels);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.general_button_text), " \u2192 ", getString(R.string.channels)));

        showDialogRead();
        stabiProvider.getSerial(GET_SERIAL_NUMBER);

        delegateListener();
	}

	/**
	 *
	 */
	final private Handler delayHandle = new Handler();

	/**
	 *
	 * @return
	 */
	public boolean isEnableChangeBank()
	{
		return false;
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
		if (stabiProvider.getState() == CommandService.STATE_CONNECTED) {
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

		if (profileCreator.getProfileItemByName("RECEIVER").getValueInteger() == 65 || profileCreator.getProfileItemByName("RECEIVER").getValueInteger() == 68) { // 65 je A coz je PWM prijmac / 68 futaba
			Button failsafe = (Button) findViewById(R.id.failsafe);
			failsafe.setEnabled(false);

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
	 *
	 * @param v
	 */
	public void setFailSafe(View v) {
		stabiProvider.setFailSafe(FAILSAFE_CALL_BACK_CODE);
	}

	/**
	 *
	 * @param b
	 */
	protected void updateGui(byte[] b)
	{

		//AILERON
		int aileron = ByteOperation.twoByteToSigInt(b[0], b[1]);
		int aileronPercent = ((100 * aileron) / 340) * -1;
		((ProgressBar) findViewById(R.id.aileron_progress_diagnostic)).setProgress(aileronPercent + 100);
		////////////////////////////////////////////////////////////////////////////////////////////////////////////

		//ELEVATOR
		int elevator = ByteOperation.twoByteToSigInt(b[2], b[3]);
		int elevatorPercent = ((100 * elevator) / 340) * -1;
		((ProgressBar) findViewById(R.id.elevator_progress_diagnostic)).setProgress(elevatorPercent + 100);
		////////////////////////////////////////////////////////////////////////////////////////////////////////////

		//PITCH
		int pitch = ByteOperation.twoByteToSigInt(b[4], b[5]);
		int pitchPercent = ((100 * pitch) / 340);
		((ProgressBar) findViewById(R.id.pitch_progress_diagnostic)).setProgress(pitchPercent + 100);
		////////////////////////////////////////////////////////////////////////////////////////////////////////////

		//RUDDER
		int rudder = ByteOperation.twoByteToSigInt(b[6], b[7]);
		int rudderPercent = ((100 * rudder) / 340);
		((ProgressBar) findViewById(R.id.rudder_progress_diagnostic)).setProgress((rudderPercent + 100));
		////////////////////////////////////////////////////////////////////////////////////////////////////////////

		//GYRO
		int gyro = ByteOperation.twoByteToSigInt(b[8], b[9]);
		int gyroPercent = ((100 * gyro) / 388);
		((ProgressBar) findViewById(R.id.gyro_progress_diagnostic)).setProgress((gyroPercent + 100));
		////////////////////////////////////////////////////////////////////////////////////////////////////////////

		//AUX2  / banks
		int banks = ByteOperation.twoByteToSigInt(b[10], b[11]);
		int banksPercent = ((100 * banks) / 340);
		((ProgressBar) findViewById(R.id.bank_progress_diagnostic)).setProgress((banksPercent + 100));
		////////////////////////////////////////////////////////////////////////////////////////////////////////////

		//AUX1  / throttle
		int throttle = ByteOperation.twoByteToSigInt(b[12], b[13]);
		int throttlePercent = ((50 * throttle) / 340);
		((ProgressBar) findViewById(R.id.throttle_progress_diagnostic)).setProgress((throttlePercent + 50));
		////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

		getPositionFromUnit();
	}

	/**
	 * ziskani informace o poloze kniplu z jednotky
	 */
	protected void getPositionFromUnit()
	{
		delayHandle.postDelayed(new Runnable() {
			@Override
			public void run() {
				stabiProvider.getDiagnostic(DIAGNOSTIC_CALL_BACK_CODE);
			}
		}, 50); // ms

	}

    /**
     *
     * @param serialNumber
     */
    public void initSerialNumber(byte[] serialNumber){
        serial = new SerialNumber(serialNumber);
        initConfiguration();
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
					if(profileCreator.getProfileItemByName("RECEIVER").getValueInteger() == 65 && (pos != 4 && pos != 7) && !serial.isProVersion()){ // 65 je A coz je PWM prijmac, hodnoty 4 = kanal 5, 7 = neprirazeno
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
			case DstabiProvider.MESSAGE_SEND_COMAND_ERROR:

				int callBackCode = msg.getData().containsKey("callBack") ? msg.getData().getInt("callBack") : 0;

				if(profileCreator != null && profileCreator.isValid() && callBackCode == DIAGNOSTIC_CALL_BACK_CODE) {
					if(counter < 1) {
						getPositionFromUnit();
						counter++;
					}else{
						super.handleMessage(msg);
					}

				}else{
					super.handleMessage(msg);
				}
				break;

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
			case DIAGNOSTIC_CALL_BACK_CODE:
				if (msg.getData().containsKey("data")) {
					updateGui(msg.getData().getByteArray("data"));

					getPositionFromUnit();
					counter = 0;
				}
				break;
			case FAILSAFE_CALL_BACK_CODE:
				showConfirmDialog(R.string.failsafe_configured);
				break;
            case GET_SERIAL_NUMBER:
                sendInSuccessDialog();
                if (msg.getData().containsKey("data")) {
                    initSerialNumber(msg.getData().getByteArray("data"));
                }
                break;
			default:
				super.handleMessage(msg);
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		//super.onCreateOptionsMenu(menu);
		return false;
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
