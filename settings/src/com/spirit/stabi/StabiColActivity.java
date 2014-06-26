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


package com.spirit.stabi;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.customWidget.picker.ProgresEx;
import com.customWidget.picker.ProgresEx.OnChangedListener;
import com.customWidget.picker.ProgresExViewTranslateInterface;
import com.helpers.DstabiProfile;
import com.helpers.DstabiProfile.ProfileItem;
import com.lib.BluetoothCommandService;
import com.spirit.BaseActivity;
import com.spirit.R;

public class StabiColActivity extends BaseActivity
{

	@SuppressWarnings("unused")
	final private String TAG = "SenzorActivity";

	final private int PROFILE_CALL_BACK_CODE = 16;

	private final String protocolCode[] = {"STABI_COL",};

	private int formItems[] = {R.id.stabi_pitch,};

	private int formItemsTitle[] = {R.string.stabi_col,};

	private DstabiProfile profileCreator;

	/**
	 * zavolani pri vytvoreni instance aktivity stabi
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.stabi_col);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.stabi_button_text), " \u2192 ", getString(R.string.stabi_col)));

		initGui();
		initConfiguration();
		delegateListener();
	}

	/**
	 * znovu nacteni aktivity, priradime dstabi svuj handler a zkontrolujeme jestli sme pripojeni
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		if (stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED)
			((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
		else finish();
	}

	private void initGui()
	{
		for (int i = 0; i < formItems.length; i++) {
			ProgresEx tempPicker = (ProgresEx) findViewById(formItems[i]);

			tempPicker.setTranslate(new StabiPichProgressExTranslate());
			tempPicker.setOffset(-127);             // zobrazujeme od stredu, 127 => 0
			tempPicker.setRange(117, 137, -10, 10); // hack, ble
			tempPicker.setTitle(formItemsTitle[i]); // nastavime titulek
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
			ProfileItem item = profileCreator.getProfileItemByName(protocolCode[i]);

			tempPicker.setCurrentNoNotify(item.getValueInteger());

			if(profileCreator.getProfileItemByName("ALT_FUNCTION").getValueString().equals("A")){
				tempPicker.setEnabled(false);
			}
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

					if (newVal > 127 && newVal < 127 + 4) newVal = 127 - 4;
					if (newVal < 127 && newVal > 127 - 4) newVal = 127 + 4;

					parent.setCurrentNoNotify(newVal);

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

	/**
	 * trida nam prelozi cislo z velikosti stabiPitch na procenta
	 * <p/>
	 * -10 az 10 prelozi na -100% az 100%, vnitrne se ale bude pocitat porad s -10 az 10
	 *
	 * @author petrcada
	 */
	protected class StabiPichProgressExTranslate implements ProgresExViewTranslateInterface
	{

		@Override
		public String translateCurrent(int current)
		{
			return String.valueOf((current * 10)) + " %";
		}

		@Override
		public String translateMin(int min)
		{
			return String.valueOf((min * 10)) + " %";
		}

		@Override
		public String translateMax(int max)
		{
			return String.valueOf((max * 10)) + " %";
		}

	}

}



