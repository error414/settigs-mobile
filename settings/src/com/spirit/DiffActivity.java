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

package com.spirit;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.exception.IndexOutOfException;
import com.exception.ProfileNotValidException;
import com.google.analytics.tracking.android.EasyTracker;
import com.helpers.DiffListAdapter;
import com.helpers.DstabiProfile;
import com.helpers.Globals;
import com.lib.BluetoothCommandService;
import com.lib.ChangeInProfile;
import com.lib.DstabiProvider;
import com.lib.translate.AutorotationBailOutProgressExTranslate;
import com.lib.translate.GovernorRamPupProgressExTranslate;
import com.lib.translate.GovernorRpmMaxProgressExTranslate;
import com.lib.translate.GovernorThrRangeProgressExTranslate;
import com.lib.translate.GovernorgearRatioProgressExTranslate;
import com.lib.translate.ServoCorrectionProgressExTranslate;
import com.lib.translate.ServoCorrectionUpProgressExTranslate;
import com.lib.translate.ServoCyclickRingProgressExTranslate;
import com.lib.translate.ServoSubtrimProgressExTranslate;
import com.lib.translate.StabiAcroDelayProgressExTranslate;
import com.lib.translate.StabiPichProgressExTranslate;
import com.lib.translate.StabiSenzivityXProgressExTranslate;
import com.lib.translate.StabiSenzivityYProgressExTranslate;
import com.lib.translate.StabiSenzivityZProgressExTranslate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * @author error414
 */
public class DiffActivity extends BaseActivity
{
    @SuppressWarnings("unused")
	final private String TAG = "DiffActivity";

    private static final String ARG_BANK = "bank";
    private static final String ARG_ACTIVE_BANK = "activeBank";

    final static private int PROFILE_CALL_BACK_CODE = 101;
    final static private int BANK_TO_COMAPARE_CALL_BACK_CODE = 102;
    final static private int BANK_ACTIVE_CALL_BACK_CODE = 103;
    final static private int PROFILE_TO_COMPARE_CALL_BACK_CODE = 104;
    final static private int ACTIVE_PROFILE_CALL_BACK_CODE = 105;

	private DiffListAdapter adapter;

	private ArrayList<HashMap<Integer, String>> diffListData;

	final private String textSeparator = "\u2192";
    private DstabiProfile profileToCompare;

    /**
	 * zavolani pri vytvoreni instance aktivity settings
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.profile_diff);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
        Integer bankToCompare = getBankToCompare();
        if (bankToCompare == null) {
            ((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", getString(R.string.profile_diff)));
        }
        else {
            String title = getString(R.string.profile_bank_diff);
            ((TextView) findViewById(R.id.title)).setText(TextUtils.concat(getTitle(), " \u2192 ", title));
            ((TextView) findViewById(R.id.textView2)).setText(TextUtils.concat(title, " B" + bankToCompare + "\u2192B" + Globals.getInstance().getActiveBank()));
        }

		////////////////////////////////////////////////////////////////////////
		ListView diffList = (ListView) findViewById(R.id.listMenu);

		adapter = new DiffListAdapter(this, new ArrayList<HashMap<Integer, String>>());
		diffList.setAdapter(adapter);
	}

    /**
     *
     */
    public void onResume()
    {
        super.onResume();
        if (stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED) {
            ((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
            //dame pozadavek na ziskani profilu z jednotky
            initConfiguration();
        } else {
            finish();
        }
    }

	/**
	 *
	 */
	private void initConfiguration()
	{
		showDialogRead();

        Integer bankToCompare = getBankToCompare();
        if (bankToCompare != null) {
            readBankProfile(bankToCompare);
            return;
        }

        // ziskani konfigurace z jednotky
		stabiProvider.getProfile(PROFILE_CALL_BACK_CODE);
	}

    private void readBankProfile(int bankToCompare) {
        changeBank(bankToCompare, BANK_TO_COMAPARE_CALL_BACK_CODE);
    }

    /**
     *
     * @return
     */
    private Integer getBankToCompare() {
        int bank = getIntent().getIntExtra(ARG_BANK, Globals.BANK_NULL);
        return bank == Globals.BANK_NULL ? null : bank;
    }

	/**
	 *
	 * @param profile
	 */
	protected void updateGui(byte[] profile){
		DstabiProfile changedProfile = new DstabiProfile(profile);
		try {
            updateGui(ChangeInProfile.getInstance().getDiff(changedProfile), ChangeInProfile.getInstance().getOriginalProfile(), changedProfile);
        } catch (ProfileNotValidException e) {
			e.printStackTrace();
		}
	}

    private void updateGui(DstabiProfile originalProfile, DstabiProfile actualProfile, boolean compareOnlyBasicItems) {
        try {
            updateGui(ChangeInProfile.getDiff(originalProfile, actualProfile, compareOnlyBasicItems), originalProfile, actualProfile);
        } catch (ProfileNotValidException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param diffItems
     */
    private void updateGui(Collection<ChangeInProfile.DiffItem> diffItems, DstabiProfile originalProfile, DstabiProfile actualProfile) {
        diffListData = new ArrayList<HashMap<Integer, String>>();

        try {
            for(ChangeInProfile.DiffItem diffItem : diffItems) {
                HashMap<Integer, String> row = new HashMap<Integer, String>();

                diffItem = this.translateDiffItem(diffItem, originalProfile, actualProfile);

                row.put(DiffListAdapter.NAME, diffItem.getLabel());
                row.put(DiffListAdapter.FROM, diffItem.getFrom());
                row.put(DiffListAdapter.TO,   diffItem.getTo());
                diffListData.add(row);
            }

            Collections.sort(diffListData, new Comparator<HashMap<Integer, String>>() {
                @Override
                public int compare(HashMap<Integer, String> lhs, HashMap<Integer, String> rhs) {
                    return lhs.get(DiffListAdapter.NAME).compareTo(rhs.get(DiffListAdapter.NAME));
                }
            });

            adapter.setData(diffListData);
            adapter.notifyDataSetChanged();

        } catch (IndexOutOfException e) {
            e.printStackTrace();
        }
    }


    /**
	 *
	 * @param diffItem
	 * @return
	 * @throws IndexOutOfException
	 */
    private ChangeInProfile.DiffItem translateDiffItem(ChangeInProfile.DiffItem diffItem, DstabiProfile originalProfile, DstabiProfile actualProfile) throws IndexOutOfException {
        Resources res = getResources();
        android.content.res.Configuration conf = res.getConfiguration();


        String from = diffItem.getOriginalValue().getValueString();
		String to   = diffItem.getChangedValue().getValueString();

		// #############################################################################################
		if(diffItem.getLabel().equals("POSITION")){
			diffItem.setLabel(getResources().getString(R.string.position_text));

			String[] values = getResources().getStringArray(R.array.position_values);

			from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
			to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
		}
		// #############################################################################################

		// #############################################################################################
		if(diffItem.getLabel().equals("RECEIVER")){
			diffItem.setLabel(getResources().getString(R.string.receiver_text));

			String[] values = getResources().getStringArray(R.array.receiver_values);

			from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
			to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
		}
		// #############################################################################################

		// #############################################################################################
		if(diffItem.getLabel().equals("MIX")){
			diffItem.setLabel(getResources().getString(R.string.mix_text));

			String[] values = getResources().getStringArray(R.array.mix_values);

			from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
			to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
		}
		// #############################################################################################

		// #############################################################################################
		if(diffItem.getLabel().equals("CYCLIC_TYPE")){
			diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servos_button_text), textSeparator, getResources().getString(R.string.type), textSeparator, getResources().getString(R.string.cyclic), textSeparator, getResources().getString(R.string.pulse)).toString());

			String[] values = getResources().getStringArray(R.array.cyclic_pulse_value);

			from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
			to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
		}
		// #############################################################################################

		// #############################################################################################
		if(diffItem.getLabel().equals("CYCLIC_FREQ")){
			diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servos_button_text), textSeparator, getResources().getString(R.string.type), textSeparator, getResources().getString(R.string.cyclic), textSeparator, getResources().getString(R.string.frequency)).toString());

			String[] values = getResources().getStringArray(R.array.cyclic_frequency_value);

			from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
			to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
		}
		// #############################################################################################

		// #############################################################################################
		if(diffItem.getLabel().equals("RUDDER_TYPE")){
			diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servos_button_text),  textSeparator , getResources().getString(R.string.type), textSeparator, getResources().getString(R.string.rudder), textSeparator, getResources().getString(R.string.pulse)).toString());

			String[] values = getResources().getStringArray(R.array.rudder_pulse_value);

			from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
			to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
		}
		// #############################################################################################

		// #############################################################################################
		if(diffItem.getLabel().equals("RUDDER_FREQ")){
			diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servos_button_text),  textSeparator , getResources().getString(R.string.type), textSeparator, getResources().getString(R.string.rudder), textSeparator, getResources().getString(R.string.frequency)).toString());

			String[] values = getResources().getStringArray(R.array.rudder_frequency_value_extend);

			from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
			to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
		}
		// #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("SUBTRIM_AIL")){
            ServoSubtrimProgressExTranslate translate = new ServoSubtrimProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servos_button_text),  textSeparator ,getResources().getString(R.string.subtrim),  textSeparator , getResources().getString(actualProfile.getProfileItemByName("MIX").getValueInteger()  % 2 == 1 ? R.string.servo_ch1 : R.string.servo_ch1_inverted)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("SUBTRIM_ELE")){
            ServoSubtrimProgressExTranslate translate = new ServoSubtrimProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servos_button_text),  textSeparator ,getResources().getString(R.string.subtrim),  textSeparator , getResources().getString(R.string.servo_ch2)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("SUBTRIM_PIT")){
            ServoSubtrimProgressExTranslate translate = new ServoSubtrimProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servos_button_text),  textSeparator ,getResources().getString(R.string.subtrim),  textSeparator , getResources().getString(actualProfile.getProfileItemByName("MIX").getValueInteger()  % 2 == 1 ? R.string.servo_ch3 : R.string.servo_ch3_inverted)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("SUBTRIM_RUD")){
            ServoSubtrimProgressExTranslate translate = new ServoSubtrimProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servos_button_text),  textSeparator ,getResources().getString(R.string.subtrim),  textSeparator , getResources().getString(R.string.servo_rudder)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("RANGE_AIL")){
            ServoCyclickRingProgressExTranslate translateFrom = new ServoCyclickRingProgressExTranslate(originalProfile.getProfileItemByName("GEOMETRY").getValueInteger());
            ServoCyclickRingProgressExTranslate translateTo = new ServoCyclickRingProgressExTranslate(actualProfile.getProfileItemByName("GEOMETRY").getValueInteger());

            diffItem.setLabel(TextUtils.concat( getResources().getString(R.string.limits), textSeparator,  getResources().getString(R.string.cyclic_ring_range),  textSeparator , getResources().getString(R.string.ail_ele)).toString());

            from = String.valueOf(translateFrom.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to = String.valueOf(translateTo.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("RANGE_PIT")){
            diffItem.setLabel(TextUtils.concat( getResources().getString(R.string.limits),  textSeparator , getResources().getString(R.string.pitch_range)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("RUDDER_MIN")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.limits), textSeparator, getResources().getString(R.string.rudder_end_points_no_break), textSeparator, getResources().getString(R.string.right_limit)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("RUDDER_MAX")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.limits), textSeparator, getResources().getString(R.string.rudder_end_points_no_break), textSeparator, getResources().getString(R.string.left_limit)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("SENSOR_SENX")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.senzor_button_text),  textSeparator , getResources().getString(R.string.senzivity),  textSeparator , getResources().getString(R.string.x_cyclic)).toString());

            StabiSenzivityXProgressExTranslate translate = new StabiSenzivityXProgressExTranslate();

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("GEOMETRY")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text),  textSeparator , getResources().getString(R.string.geom_6deg)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("SENSOR_RUDDER_COMMON_GAIN")){

            StabiSenzivityZProgressExTranslate translate = new StabiSenzivityZProgressExTranslate(conf.locale);

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.senzor_button_text),  textSeparator , getResources().getString(R.string.senzivity),  textSeparator , getResources().getString(R.string.rudder_common_gain)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("SENSOR_GYROGAIN")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.senzor_button_text),  textSeparator , getResources().getString(R.string.senzivity),  textSeparator , getResources().getString(R.string.gyro_gain)).toString());

            StabiSenzivityYProgressExTranslate translate = new StabiSenzivityYProgressExTranslate();

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("RATE_PITCH")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.senzor_button_text),  textSeparator , getResources().getString(R.string.rotation_speed),  textSeparator , getResources().getString(R.string.cyc_rate)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("RATE_YAW")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.senzor_button_text),  textSeparator , getResources().getString(R.string.rotation_speed),  textSeparator , getResources().getString(R.string.rud_rate)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("CYCLIC_FF")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text),  textSeparator , getResources().getString(R.string.cyclic_ff)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("RUDDER_STOP")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text),  textSeparator , getResources().getString(R.string.rudder_dynamic)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("ALT_FUNCTION")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.stabi_button_text),  textSeparator , getResources().getString(R.string.stabi_function)).toString());

            String[] values = getResources().getStringArray(R.array.function_values);

            from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
            to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("RUDDER_REVOMIX")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text), textSeparator , getResources().getString(R.string.rudder_revomix)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger() - 128);
            to = String.valueOf(diffItem.getChangedValue().getValueInteger() - 128);
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("STABI_CTRLDIR")){

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.stabi_button_text), textSeparator , getResources().getString(R.string.stabi_ctrldir)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("STABI_COL")){

            StabiPichProgressExTranslate translate = new StabiPichProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.stabi_button_text), textSeparator , getResources().getString(R.string.stabi_col)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger() - 127));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger() - 127));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("STABI_STICK")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.stabi_button_text), textSeparator , getResources().getString(R.string.stabi_stick)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // ###################################PITCH_PUMP##########################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("STABI_ACRO_DELAY")){

            StabiAcroDelayProgressExTranslate translate = new StabiAcroDelayProgressExTranslate(conf.locale);

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.stabi_button_text), textSeparator , getResources().getString(R.string.acro_delay)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // ###################################PITCH_PUMP##########################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("PIROUETTE_CONST")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text), textSeparator , getResources().getString(R.string.pirouette_consistency)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if (diffItem.getLabel().equals("ROTOR_ROTATION")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text), textSeparator, getResources().getString(R.string.rotor_rotation), textSeparator, getResources().getString(R.string.rotor_rotation_title)).toString());

            from = diffItem.getOriginalValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
            to   = diffItem.getChangedValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("E_FILTER")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text), textSeparator , getResources().getString(R.string.e_filter)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("RUDDER_DELAY")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text), textSeparator , getResources().getString(R.string.rudder_delay)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("FLIGHT_STYLE")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.flight_style_text)).toString());

            String[] values = getResources().getStringArray(R.array.flight_style_values);

            from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
            to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("FB_MODE")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.stabi_button_text), textSeparator , getResources().getString(R.string.stabi_fbmode)).toString());

            from = diffItem.getOriginalValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
            to   = diffItem.getChangedValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("TRAVEL_UAIL")){

            ServoCorrectionUpProgressExTranslate translate = new ServoCorrectionUpProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servo_travel_correction), textSeparator , getResources().getString(actualProfile.getProfileItemByName("MIX").getValueInteger()  % 2 == 1 ? R.string.servo_ch1 : R.string.servo_ch1_inverted), textSeparator, getResources().getString(R.string.max)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("TRAVEL_UELE")){

            ServoCorrectionUpProgressExTranslate translate = new ServoCorrectionUpProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servo_travel_correction), textSeparator , getResources().getString(R.string.servo_ch2), textSeparator, getResources().getString(R.string.max)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("TRAVEL_UPIT")){

            ServoCorrectionUpProgressExTranslate translate = new ServoCorrectionUpProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servo_travel_correction), textSeparator , getResources().getString(actualProfile.getProfileItemByName("MIX").getValueInteger()  % 2 == 1 ? R.string.servo_ch3 : R.string.servo_ch3_inverted), textSeparator, getResources().getString(R.string.max)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("TRAVEL_DAIL")){

            ServoCorrectionProgressExTranslate translate = new ServoCorrectionProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servo_travel_correction), textSeparator , getResources().getString(actualProfile.getProfileItemByName("MIX").getValueInteger()  % 2 == 1 ? R.string.servo_ch1 : R.string.servo_ch1_inverted), textSeparator, getResources().getString(R.string.min)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("TRAVEL_DELE")){

            ServoCorrectionProgressExTranslate translate = new ServoCorrectionProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servo_travel_correction), textSeparator , getResources().getString(R.string.servo_ch2), textSeparator, getResources().getString(R.string.min)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("TRAVEL_DPIT")){

            ServoCorrectionProgressExTranslate translate = new ServoCorrectionProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servo_travel_correction), textSeparator , getResources().getString(actualProfile.getProfileItemByName("MIX").getValueInteger()  % 2 == 1 ? R.string.servo_ch3 : R.string.servo_ch3_inverted), textSeparator, getResources().getString(R.string.min)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################
        
        // #############################################################################################
        if(diffItem.getLabel().equals("CHANNELS_THT")){
        	diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.channels), textSeparator , getResources().getString(R.string.throttle)).toString());

			String[] values = getResources().getStringArray(R.array.channels_values);

			from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
			to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
        }
        // #############################################################################################
        
        // #############################################################################################
        if(diffItem.getLabel().equals("CHANNELS_AIL")){
        	diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.channels), textSeparator , getResources().getString(R.string.diag_aileron)).toString());

			String[] values = getResources().getStringArray(R.array.channels_values);

			from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
			to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
        }
        // #############################################################################################
        
        // #############################################################################################
        if(diffItem.getLabel().equals("CHANNELS_ELE")){
        	diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.channels), textSeparator , getResources().getString(R.string.diag_elevator)).toString());

			String[] values = getResources().getStringArray(R.array.channels_values);

			from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
			to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
        }
        // #############################################################################################
        
        // #############################################################################################
        if(diffItem.getLabel().equals("CHANNELS_RUD")){
        	diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.channels), textSeparator , getResources().getString(R.string.diag_rudder)).toString());

			String[] values = getResources().getStringArray(R.array.channels_values);

			from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
			to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
        }
        // #############################################################################################
        
        // #############################################################################################
        if(diffItem.getLabel().equals("CHANNELS_GAIN")){
        	diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.channels), textSeparator , getResources().getString(R.string.gyro_gain)).toString());

			String[] values = getResources().getStringArray(R.array.channels_values);

			from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
			to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
        }
        // #############################################################################################
        
        // #############################################################################################
        if(diffItem.getLabel().equals("CHANNELS_PITH")){
        	diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.channels), textSeparator , getResources().getString(R.string.diag_pitch)).toString());

			String[] values = getResources().getStringArray(R.array.channels_values);

			from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
			to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
        }
        // #############################################################################################
        
        // #############################################################################################
        if(diffItem.getLabel().equals("CHANNELS_BANK")){
        	diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.channels), textSeparator , getResources().getString(R.string.banks)).toString());

			String[] values = getResources().getStringArray(R.array.channels_values);

			from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
			to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("GOVERNOR_ON")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.governor_thr), textSeparator, getResources().getString(R.string.governor), textSeparator, getResources().getString(R.string.governor_on)).toString());

            from = diffItem.getOriginalValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
            to   = diffItem.getChangedValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("GOVERNOR_FREQ")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.governor_thr), textSeparator , getResources().getString(R.string.governor_freq)).toString());

            String[] values = getResources().getStringArray(R.array.governor_freq_values);

            from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
            to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("GOVERNOR_SPOOLUP")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.governor_thr), textSeparator, getResources().getString(R.string.governor), textSeparator , getResources().getString(R.string.governor_spoolup)).toString());

            String[] values = getResources().getStringArray(R.array.governor_spoolup_values);

            from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
            to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("GOVERNOR_PGAIN")){

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.governor_thr), textSeparator, getResources().getString(R.string.governor), textSeparator , getResources().getString(R.string.governor_pgain)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("GOVERNOR_IGAIN")){

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.governor_thr), textSeparator, getResources().getString(R.string.governor), textSeparator , getResources().getString(R.string.governor_igain)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("GOVERNOR_THR_MIN")){
            GovernorThrRangeProgressExTranslate translate = new GovernorThrRangeProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.governor_thr), textSeparator , getResources().getString(R.string.governor_thr_min)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("GOVERNOR_THR_MAX")){
            GovernorThrRangeProgressExTranslate translate = new GovernorThrRangeProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.governor_thr), textSeparator , getResources().getString(R.string.governor_thr_max)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if (diffItem.getLabel().equals("GOVERNOR_RAMPUP")){
            GovernorRamPupProgressExTranslate translate = new GovernorRamPupProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.governor_thr), textSeparator, getResources().getString(R.string.governor_rampup)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("GOVERNOR_DIVIDER")){

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.governor_thr), textSeparator, getResources().getString(R.string.governor), textSeparator , getResources().getString(R.string.governor_divider)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("GOVERNOR_RATIO")){
            GovernorgearRatioProgressExTranslate translate = new GovernorgearRatioProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.governor_thr), textSeparator, getResources().getString(R.string.governor), textSeparator , getResources().getString(R.string.governor_ratio)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("GOVERNOR_RPM_MAX")){
            GovernorRpmMaxProgressExTranslate translate = new GovernorRpmMaxProgressExTranslate();

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.governor_thr), textSeparator, getResources().getString(R.string.governor), textSeparator , getResources().getString(R.string.governor_rpm_max)).toString());

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("GOVERNOR_THR_REVERSE")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.governor_thr),  textSeparator , getResources().getString(R.string.governor_thr_reverse)).toString());

            from = diffItem.getOriginalValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
            to   = diffItem.getChangedValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
        }
        // #############################################################################################


        // #############################################################################################
        if(diffItem.getLabel().equals("PITCH_PUMP")){

            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text), textSeparator , getString(R.string.advanced_expert), textSeparator , getResources().getString(R.string.pitch_pump)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("CYCLIC_PHASE")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text), textSeparator , getString(R.string.advanced_expert), textSeparator, getResources().getString(R.string.cyclic_phase)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("SIGNAL_PROCESSING")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text),  textSeparator ,  getString(R.string.advanced_expert), textSeparator, getResources().getString(R.string.signal_processing)).toString());

            from = diffItem.getOriginalValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
            to   = diffItem.getChangedValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("PITCHUP")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text),  textSeparator , getString(R.string.advanced_expert), textSeparator, getResources().getString(R.string.pitchup)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("STICK_DB")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text),  textSeparator , getString(R.string.advanced_expert), textSeparator, getResources().getString(R.string.stick_deadband)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        ////////////////////////////////////////////
        ////////////// AERO ////////////////////////
        ////////////////////////////////////////////

        // #############################################################################################
        if(diffItem.getLabel().equals("LIMIT_RANGE_AILE_U")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.limits),  textSeparator , getString(R.string.limit_range_ail), textSeparator, getResources().getString(R.string.left)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("LIMIT_RANGE_AILE_D")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.limits),  textSeparator , getString(R.string.limit_range_ail), textSeparator, getResources().getString(R.string.right)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("LIMIT_RANGE_ELE_U")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.limits),  textSeparator , getString(R.string.limit_range_ele), textSeparator, getResources().getString(R.string.left)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("LIMIT_RANGE_ELE_D")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.limits),  textSeparator , getString(R.string.limit_range_ele), textSeparator, getResources().getString(R.string.right)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("LIMIT_RANGE_RUD_U")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.limits),  textSeparator , getString(R.string.limit_range_rud), textSeparator, getResources().getString(R.string.left)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("LIMIT_RANGE_RUD_D")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.limits),  textSeparator , getString(R.string.limit_range_rud), textSeparator, getResources().getString(R.string.right)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("SERVO_REV_CH1")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servos_button_text),  textSeparator , getString(R.string.ser_reverse), textSeparator, getResources().getString(R.string.ch1)).toString());

            from = diffItem.getOriginalValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
            to   = diffItem.getChangedValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("SERVO_REV_CH2")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servos_button_text),  textSeparator , getString(R.string.ser_reverse), textSeparator, getResources().getString(R.string.ch2)).toString());

            from = diffItem.getOriginalValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
            to   = diffItem.getChangedValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("SERVO_REV_CH3")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servos_button_text),  textSeparator , getString(R.string.ser_reverse), textSeparator, getResources().getString(R.string.ch3)).toString());

            from = diffItem.getOriginalValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
            to   = diffItem.getChangedValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("SERVO_REV_CH4")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servos_button_text),  textSeparator , getString(R.string.ser_reverse), textSeparator, getResources().getString(R.string.ch4)).toString());

            from = diffItem.getOriginalValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
            to   = diffItem.getChangedValue().getValueForCheckBox() ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("AERO_POSITION")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.general_button_text),  textSeparator , getResources().getString(R.string.position_text)).toString());

            String[] values = getResources().getStringArray(R.array.aero_position_values);

            from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
            to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("AERO_ALT_FUNCTION")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.stabi_button_text),  textSeparator , getResources().getString(R.string.stabi_function)).toString());

            String[] values = getResources().getStringArray(R.array.aero_function_values);

            from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
            to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("FF")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text),  textSeparator , getResources().getString(R.string.ff)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("AERO_STICK_DB")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text),  textSeparator , getResources().getString(R.string.stick_deadband)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("RPM_SENZOR_FILTER")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text),  textSeparator , getString(R.string.advanced_expert), textSeparator , getResources().getString(R.string.rpm_senzor_filter)).toString());

            from = String.valueOf(diffItem.getOriginalValue().getValueInteger());
            to   = String.valueOf(diffItem.getChangedValue().getValueInteger());
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("AUTOROTATION_BAILOUT")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.advanced_button_text),  textSeparator , getString(R.string.advanced_expert), textSeparator , getResources().getString(R.string.autorotation_bailout)).toString());

            AutorotationBailOutProgressExTranslate translate = new AutorotationBailOutProgressExTranslate();

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("AERO_SENSOR_SENX")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.senzor_button_text),  textSeparator , getResources().getString(R.string.senzivity),  textSeparator , getResources().getString(R.string.diag_aileron)).toString());

            StabiSenzivityXProgressExTranslate translate = new StabiSenzivityXProgressExTranslate();

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("AERO_SENSOR_SENY")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.senzor_button_text),  textSeparator , getResources().getString(R.string.senzivity),  textSeparator , getResources().getString(R.string.diag_elevator)).toString());

            StabiSenzivityXProgressExTranslate translate = new StabiSenzivityXProgressExTranslate();

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("AERO_SENSOR_SENZ")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.senzor_button_text),  textSeparator , getResources().getString(R.string.senzivity),  textSeparator , getResources().getString(R.string.diag_rudder)).toString());

            StabiSenzivityXProgressExTranslate translate = new StabiSenzivityXProgressExTranslate();

            from = String.valueOf(translate.translateCurrent(diffItem.getOriginalValue().getValueInteger()));
            to   = String.valueOf(translate.translateCurrent(diffItem.getChangedValue().getValueInteger()));
        }
        // #############################################################################################

        // #############################################################################################
        if(diffItem.getLabel().equals("SERVO_FREQ")){
            diffItem.setLabel(TextUtils.concat(getResources().getString(R.string.servos_button_text), textSeparator, getResources().getString(R.string.type), textSeparator, getResources().getString(R.string.frequency)).toString());

            String[] values = getResources().getStringArray(R.array.cyclic_frequency_value);

            from = values[diffItem.getOriginalValue().getValueForSpinner(values.length)];
            to   = values[diffItem.getChangedValue().getValueForSpinner(values.length)];
        }
        // #############################################################################################


        ////////////////////////////////////////////
        ////////////////////////////////////////////

        diffItem.setFrom(from);
		diffItem.setTo(to);


		return diffItem;
	}

	/**
	 *
	 * @param msg
	 * @return
	 */
	public boolean handleMessage(Message msg)
	{
		switch (msg.what) {
			case DstabiProvider.MESSAGE_STATE_CHANGE:
				if (stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED) {
					sendInError();
					((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.red);
				} else {
					((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
				}
				break;
			case DiffActivity.PROFILE_CALL_BACK_CODE:
				sendInSuccessDialog();
				if (msg.getData().containsKey("data")) {
					updateGui(msg.getData().getByteArray("data"));
				}
				break;
            case PROFILE_FOR_UPDATE_ORIGINAL:
                super.handleMessage(msg);
                initConfiguration();
                break;
            case BANK_TO_COMAPARE_CALL_BACK_CODE:
                stabiProvider.getProfile(PROFILE_TO_COMPARE_CALL_BACK_CODE);
                break;
            case BANK_ACTIVE_CALL_BACK_CODE:
                stabiProvider.getProfile(ACTIVE_PROFILE_CALL_BACK_CODE);
                break;
            case PROFILE_TO_COMPARE_CALL_BACK_CODE:
                if (msg.getData().containsKey("data")) {
                    profileToCompare = new DstabiProfile(msg.getData().getByteArray("data"));
                    changeBank(getIntent().getIntExtra(ARG_ACTIVE_BANK, 0), BANK_ACTIVE_CALL_BACK_CODE);
                }
                else {
                    sendInSuccessDialog();
                }
                break;
            case ACTIVE_PROFILE_CALL_BACK_CODE:
                sendInSuccessDialog();
                if (msg.getData().containsKey("data")) {
                    DstabiProfile activeProfile = new DstabiProfile(msg.getData().getByteArray("data"));
                    updateGui(profileToCompare, activeProfile, true);
                    profileToCompare = null;
                }
                break;
			default:
				super.handleMessage(msg);
		}
		return true;
	}

    protected void createBanksSubMenu(Menu menu) {
    }

    public static Intent createBankCompareIntent(Context context, int bank) {
        Intent intent = new Intent(context, DiffActivity.class);
        intent.putExtra(ARG_BANK, bank);
        intent.putExtra(ARG_ACTIVE_BANK, Globals.getInstance().getActiveBank());
        return intent;
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}