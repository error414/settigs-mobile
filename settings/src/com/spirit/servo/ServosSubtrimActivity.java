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

package com.spirit.servo;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.customWidget.picker.ProgresEx;
import com.customWidget.picker.ProgresEx.OnChangedListener;
import com.helpers.ByteOperation;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.spirit.BaseActivity;
import com.spirit.R;

public class ServosSubtrimActivity extends BaseActivity
{

	@SuppressWarnings("unused")
	final private String TAG = "ServosSubtrimActivity";

	final private int PROFILE_CALL_BACK_CODE = 16;

	private final String protocolCode[] = {"SUBTRIM_AIL", "SUBTRIM_ELE", "SUBTRIM_PIT", "SUBTRIM_RUD",};

	private int formItems[] = {R.id.aileron_picker, R.id.elevator_picker, R.id.pitch_picker, R.id.rudder_picker,};

	// gui prvky ktere jsou pri basic mode disablovane
	private int formItemsNotInBasicMode[] = {R.id.aileron_picker, R.id.elevator_picker, R.id.pitch_picker, R.id.rudder_picker,};

	private int formItemsTitle[] = {R.string.aileron, R.string.elevator, R.string.pitch, R.string.rudder,};

	private DstabiProfile profileCreator;

	/**
	 * zavolani pri vytvoreni instance aktivity servo type
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.servos_subtrim);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.servos_button_text), " \u2192 ", getString(R.string.subtrim)));

		initGui();
		initConfiguration();
		delegateListener();
	}

	/**
	 * znovu nacteni aktovity, priradime dstabi svuj handler a zkontrolujeme jestli sme pripojeni
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		if (stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED) {
			((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);

			if (!getAppBasicMode()) {
				stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0x00)); //povoleni subtrimu
			}
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
			ProgresEx progressEx = (ProgresEx) findViewById(item);
			progressEx.setEnabled(!getAppBasicMode());
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (!getAppBasicMode()) {
			stabiProvider.sendDataNoWaitForResponce("O", ByteOperation.intToByteArray(0xff)); //zakazani subtrimu
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();
	}

	private void initGui()
	{
		for (int i = 0; i < formItems.length; i++) {
			ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
			tempPicker.setRange(0, 255); // tohle rozmezi asi brat ze stabi profilu
			tempPicker.setTitle(formItemsTitle[i]);
		}
	}

	/**
	 * prirazeni udalosti k prvkum
	 */
	private void delegateListener()
	{
		//nastaveni posluchacu pro formularove prvky
		for (int i = 0; i < formItems.length; i++) {
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
	private void initGuiByProfileString(byte[] profile)
	{
		profileCreator = new DstabiProfile(profile);

		if (!profileCreator.isValid()) {
			errorInActivity(R.string.damage_profile);
			return;
		}

		for (int i = 0; i < formItems.length; i++) {
			ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);
			int size = profileCreator.getProfileItemByName(protocolCode[i]).getValueInteger();

			tempPicker.setCurrentNoNotify(size);
		}

	}

	protected OnChangedListener numberPicekrListener = new OnChangedListener()
	{


		@Override
		public void onChanged(ProgresEx parent, int newVal)
		{
			// TODO Auto-generated method stub
			// prohledani jestli udalost vyvolal znamy prvek
			// pokud prvek najdeme vyhledame si k prvku jeho protkolovy kod a odesleme
			for (int i = 0; i < formItems.length; i++) {
				if (parent.getId() == formItems[i]) {
					showInfoBarWrite();
					ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);
					item.setValue(newVal);
					stabiProvider.sendDataNoWaitForResponce(item);
				}
			}
		}

	};


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
