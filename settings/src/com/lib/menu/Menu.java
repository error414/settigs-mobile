package com.lib.menu;

import com.spirit.ConnectionActivity;
import com.spirit.FavouritesActivity;
import com.spirit.GeneralActivity;
import com.spirit.R;
import com.spirit.advanced.AdvancedActivity;
import com.spirit.advanced.CyclicFFActivity;
import com.spirit.advanced.EFilterActivity;
import com.spirit.advanced.GeometryAngleActivity;
import com.spirit.advanced.PiroOptimalizationActivity;
import com.spirit.advanced.PirouetteConsistencyActivity;
import com.spirit.advanced.RudderDelayActivity;
import com.spirit.advanced.RudderDynamicActivity;
import com.spirit.advanced.RudderRevomixActivity;
import com.spirit.advanced.expert.CyclicPhaseActivity;
import com.spirit.advanced.expert.ExpertActivity;
import com.spirit.advanced.expert.PitchpumpActivity;
import com.spirit.advanced.expert.PitchupActivity;
import com.spirit.advanced.expert.SignalProcessingActivity;
import com.spirit.advanced.expert.StickDeadBandActivity;
import com.spirit.diagnostic.BecTesterActivity;
import com.spirit.diagnostic.DiagnosticActivity;
import com.spirit.diagnostic.GraphActivity;
import com.spirit.diagnostic.InputChannelsActivity;
import com.spirit.diagnostic.LogActivity;
import com.spirit.governor.GovernorGainActivity;
import com.spirit.governor.GovernorGearSettingsActivity;
import com.spirit.governor.GovernorModeActivity;
import com.spirit.governor.GovernorRpmMaxActivity;
import com.spirit.governor.GovernorRpmSenzor;
import com.spirit.governor.GovernorThrRangeActivity;
import com.spirit.governor.GovernorThrReverseActivity;
import com.spirit.senzor.SenzorActivity;
import com.spirit.senzor.SenzorReverseActivity;
import com.spirit.senzor.SenzorRotationSpeedActivity;
import com.spirit.senzor.SenzorSenzivityActivity;
import com.spirit.servo.ServoTravelCorrectionActivity;
import com.spirit.servo.ServosActivity;
import com.spirit.servo.ServosCyclickRingRangeActivity;
import com.spirit.servo.ServosLimitActivity;
import com.spirit.servo.ServosReverzActivity;
import com.spirit.servo.ServosRudderEndPointsActivity;
import com.spirit.servo.ServosSubtrimActivity;
import com.spirit.servo.ServosTypeActivity;
import com.spirit.stabi.StabiActivity;
import com.spirit.stabi.StabiColActivity;
import com.spirit.stabi.StabiCtrlDirActivity;
import com.spirit.stabi.StabiFbModeActivity;
import com.spirit.stabi.StabiFunctionActivity;
import com.spirit.stabi.StabiStickActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * trida obsluhujici vytvoreni a praci s menu
 */
public class Menu {

    /**
     * klice pro adapter aby vedel k jakemu prvku jaky klic priradit
     */
    public static Integer TITLE_FOR_MENU    = 1;
    public static Integer ICO_RESOURCE_ID   = 2;

    /**
     * index pro skupinu menu
     */
    public static Integer MENU_INDEX_SETTINGS       = 1;
    public static Integer MENU_INDEX_ADVANCED       = 2;
    public static Integer MENU_INDEX_ADVANCED_EXPERT = 9;
    public static Integer MENU_INDEX_SENZOR         = 3;
    public static Integer MENU_INDEX_SERVO          = 4;
    public static Integer MENU_INDEX_STABI          = 5;
    public static Integer MENU_INDEX_SERVOLIMIT     = 6;
    public static Integer MENU_INDEX_GOVERNOR       = 7;
    public static Integer MENU_INDEX_DIAGNOSTIC     = 8;
    public HashMap<Integer,Integer[]> menuGroups = new HashMap<Integer,Integer[]>();

    /**
     * indexy pro polozky v menu
     */
    public static Integer MENU_CONNECTION       = 1;
    public static Integer MENU_GENERAL          = 2;
    public static Integer MENU_SERVO            = 3;
    public static Integer MENU_SENZOR           = 4;
    public static Integer MENU_STABI            = 5;
    public static Integer MENU_ADVANCED         = 6;
    public static Integer MENU_DIAGNOSTIC       = 7;
    public static Integer MENU_GRAPH            = 8;
    public static Integer MENU_LOG              = 9;
    public static Integer MENU_FAVOURITES       = 35;
    public static Integer MENU_BEC              = 40;

    public static Integer MENU_EXPERT               = 49;
    public static Integer MENU_DEADBAND             = 10;
    public static Integer MENU_6DEG                 = 11;
    public static Integer MENU_PIROOPT              = 12;
    public static Integer MENU_RUDDERDELAY          = 13;
    public static Integer MENU_PIROUETTECONSISTENCY = 14;
    public static Integer MENU_RUDDERDYNAMIC        = 15;
    public static Integer MENU_RUDDERREVOMIX        = 16;
    public static Integer MENU_EFILTER              = 17;
    public static Integer MENU_PITCHUP              = 18;
    public static Integer MENU_CYCLICPHASE          = 19;
    public static Integer MENU_CYCLICFF             = 20;
    public static Integer MENU_SIGNALPROCESSING     = 21;
    public static Integer MENU_PITCHPUMP            = 41;

    public static Integer MENU_SENZIVITY            = 22;
    public static Integer MENU_REVERSE              = 23;
    public static Integer MENU_ROTATIONSPEED        = 24;

    public static Integer MENU_SERVOTYPE                = 25;
    public static Integer MENU_SERVOSUBTRIM             = 26;
    public static Integer MENU_SERVOLIMIT               = 27;
    public static Integer MENU_SERVOTRAVELCORRECTION    = 28;
    public static Integer MENU_REVERZ                   = 43;

    public static Integer MENU_STABIFUNCTION        = 29;
    public static Integer MENU_STABICOL             = 30;
    public static Integer MENU_STABISTICK           = 31;
    public static Integer MENU_STABIFBMODE          = 32;
    public static Integer STABI_CTRLDIR             = 36;

    public static Integer MENU_CYCLICRING            = 33;
    public static Integer MENU_ENDPOINTS             = 34;

    public static Integer MENU_GOV_MODE            = 38;
    public static Integer MENU_GOV_GAIN            = 39;
    public static Integer MENU_GOV_RPM_SENZOR      = 44;
    public static Integer MENU_GOV_THR_RANGE       = 45;
    public static Integer MENU_GOV_RPM_MAX         = 46;
    public static Integer MENU_GOV_GEAR_SETTINGS   = 47;
    public static Integer MENU_GOV_THR_REVERSE     = 48;

    public static Integer MENU_DIAGNOSTIC_LIST     = 42;

    protected static Menu instance;

    protected Map<Integer, MenuItem> menuList = new HashMap<Integer, MenuItem>();

    /**
     * singleton
     *
     * @return
     */
    protected Menu()
    {
        createMenu();
    }

    /**
     * singleton
     *
     * @return
     */
    public static Menu getInstance(){
        if(instance == null){
            instance = new Menu();
        }

        return instance;
    }

    /**
     * vytovreni seznamu menu
     *
     */
    private void createMenu(){

        //SETTINGS ACTIVITY

        //connection
        menuList.put(MENU_FAVOURITES,   new MenuItem(R.drawable.i49,     R.string.favourites_button_text, FavouritesActivity.class));

        //connection
        menuList.put(MENU_CONNECTION,   new MenuItem(R.drawable.i4,     R.string.connection_button_text, ConnectionActivity.class));

        //general
        menuList.put(MENU_GENERAL,      new MenuItem(R.drawable.i6,     R.string.general_button_text, GeneralActivity.class));

        //servo
        menuList.put(MENU_SERVO,        new MenuItem(R.drawable.i8,     R.string.servos_button_text, ServosActivity.class));

        //servo
        menuList.put(MENU_SERVOLIMIT,   new MenuItem(R.drawable.i11,     R.string.limits, ServosLimitActivity.class));

        //senzor
        menuList.put(MENU_SENZOR,       new MenuItem(R.drawable.i15,     R.string.senzor_button_text, SenzorActivity.class));

        //diagnostic
        menuList.put(MENU_DIAGNOSTIC_LIST,   new MenuItem(R.drawable.i37,     R.string.diagnostic_button_text, DiagnosticActivity.class));

        //stabi
        menuList.put(MENU_STABI,        new MenuItem(R.drawable.i50,     R.string.stabi_button_text, StabiActivity.class));

        //advanced
        menuList.put(MENU_ADVANCED,     new MenuItem(R.drawable.i20,     R.string.advanced_button_text, AdvancedActivity.class));

        //BEC Tester
	    menuList.put(MENU_BEC,     new MenuItem(R.drawable.na,     R.string.bec_tester, BecTesterActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_SETTINGS, new Integer[]{MENU_CONNECTION, MENU_FAVOURITES, MENU_GENERAL, MENU_DIAGNOSTIC_LIST, MENU_SERVO, MENU_SERVOLIMIT, MENU_SENZOR, MENU_STABI, MENU_ADVANCED});



        //DIAGNOSTIC ACTIVITY
        //diagnostic
        menuList.put(MENU_DIAGNOSTIC,   new MenuItem(R.drawable.i37,     R.string.input_channels, InputChannelsActivity.class));

        //graph
        menuList.put(MENU_GRAPH,        new MenuItem(R.drawable.i38,     R.string.graph_button_text, GraphActivity.class));

        //log
        menuList.put(MENU_LOG,          new MenuItem(R.drawable.i40,     R.string.log_button_text, LogActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_DIAGNOSTIC, new Integer[]{MENU_DIAGNOSTIC, MENU_GRAPH, MENU_LOG, MENU_BEC});



        //ADVANCED ACTIVITY

        //geometry 6deg
        menuList.put(MENU_6DEG,   new MenuItem(R.drawable.i48,     R.string.geom_6deg, GeometryAngleActivity.class));

        //piruette opt
        menuList.put(MENU_PIROOPT,   new MenuItem(R.drawable.i26,     R.string.piro_opt, PiroOptimalizationActivity.class));

        //rudder delay
        menuList.put(MENU_RUDDERDELAY,   new MenuItem(R.drawable.na,     R.string.rudder_delay, RudderDelayActivity.class));

        //piruette const
        menuList.put(MENU_PIROUETTECONSISTENCY,   new MenuItem(R.drawable.i36,     R.string.pirouette_consistency, PirouetteConsistencyActivity.class));

        //rudder dynamic
        menuList.put(MENU_RUDDERDYNAMIC,   new MenuItem(R.drawable.i23,     R.string.rudder_dynamic, RudderDynamicActivity.class));

        //rudder dynamic
        menuList.put(MENU_RUDDERREVOMIX,   new MenuItem(R.drawable.i24,     R.string.rudder_revomix, RudderRevomixActivity.class));

        //elevator filter
        menuList.put(MENU_EFILTER,   new MenuItem(R.drawable.i33,     R.string.e_filter, EFilterActivity.class));

        //cyclic ff
        menuList.put(MENU_CYCLICFF,   new MenuItem(R.drawable.na,     R.string.cyclic_ff, CyclicFFActivity.class));

        //cyclic ff
        menuList.put(MENU_EXPERT,   new MenuItem(R.drawable.na,     R.string.advanced_expert, ExpertActivity.class));

        //add to groups
        menuGroups.put(
             MENU_INDEX_ADVANCED,
                 new Integer[]{
                         MENU_6DEG, MENU_PIROOPT, MENU_RUDDERDELAY,
                         MENU_PIROUETTECONSISTENCY, MENU_RUDDERDYNAMIC, MENU_RUDDERREVOMIX, MENU_EFILTER,
                         MENU_CYCLICFF, MENU_EXPERT
                 }
        );

        //ADVANCED EXPERT ACTIVITY

        //cyclic phase
        menuList.put(MENU_CYCLICPHASE,   new MenuItem(R.drawable.i56,     R.string.cyclic_phase, CyclicPhaseActivity.class));

        //elevator pitchup
        menuList.put(MENU_PITCHUP,   new MenuItem(R.drawable.i54,     R.string.pitchup, PitchupActivity.class));

        //pitch pump
        menuList.put(MENU_PITCHPUMP,   new MenuItem(R.drawable.i54,     R.string.pitch_pump, PitchpumpActivity.class));

        //signal procesing
        menuList.put(MENU_SIGNALPROCESSING,   new MenuItem(R.drawable.i58,     R.string.signal_processing, SignalProcessingActivity.class));

        //stick deadband
        menuList.put(MENU_DEADBAND,   new MenuItem(R.drawable.i22,     R.string.stick_deadband, StickDeadBandActivity.class));


        //add to groups
        menuGroups.put(
            MENU_INDEX_ADVANCED_EXPERT,
                new Integer[]{
                        MENU_DEADBAND, MENU_PITCHUP, MENU_CYCLICPHASE, MENU_PITCHPUMP, MENU_SIGNALPROCESSING,
                }
        );


        //SENZOR ACTIVITY
        //senzivity
        menuList.put(MENU_SENZIVITY,   new MenuItem(R.drawable.i16,     R.string.senzivity, SenzorSenzivityActivity.class));

        //reverse
        menuList.put(MENU_REVERSE,   new MenuItem(R.drawable.i17,     R.string.reverse, SenzorReverseActivity.class));

        //rotation speed
        menuList.put(MENU_ROTATIONSPEED,   new MenuItem(R.drawable.i18,     R.string.rotation_speed, SenzorRotationSpeedActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_SENZOR, new Integer[]{MENU_SENZIVITY, MENU_REVERSE, MENU_ROTATIONSPEED});


        //SERVO ACTIVITY
        //type
        menuList.put(MENU_SERVOTYPE,   new MenuItem(R.drawable.i9,     R.string.type, ServosTypeActivity.class));

        //subtrim
        menuList.put(MENU_SERVOSUBTRIM,   new MenuItem(R.drawable.i10,     R.string.subtrim,  ServosSubtrimActivity.class));

        //korekce drahy serv
        menuList.put(MENU_SERVOTRAVELCORRECTION,   new MenuItem(R.drawable.i41,     R.string.servo_travel_correction, ServoTravelCorrectionActivity.class));

        //reverz
        menuList.put(MENU_REVERZ,   new MenuItem(R.drawable.i46,     R.string.cyclic_servo_reverse_text, ServosReverzActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_SERVO, new Integer[]{MENU_SERVOTYPE, MENU_REVERZ, MENU_SERVOSUBTRIM, MENU_SERVOTRAVELCORRECTION});


        //STABI ACTIVITY
        //function
        menuList.put(MENU_STABIFUNCTION,   new MenuItem(R.drawable.i50,     R.string.stabi_function, StabiFunctionActivity.class));

        //kolektiv zachraneho rezimu
        menuList.put(MENU_STABICOL,   new MenuItem(R.drawable.i42,     R.string.stabi_col, StabiColActivity.class));

        //priorita knyplu
        menuList.put(MENU_STABISTICK,   new MenuItem(R.drawable.i47,     R.string.stabi_stick, StabiStickActivity.class));

        //flybar mechanic
        menuList.put(MENU_STABIFBMODE,   new MenuItem(R.drawable.i3,     R.string.stabi_fbmode, StabiFbModeActivity.class));

	    //STABI_CTRLDIR
	    menuList.put(STABI_CTRLDIR,   new MenuItem(R.drawable.i45,     R.string.stabi_ctrldir, StabiCtrlDirActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_STABI, new Integer[]{MENU_STABIFUNCTION, MENU_STABICOL, MENU_STABISTICK, MENU_STABIFBMODE, STABI_CTRLDIR});

        //SERVO LIMIT ACTIVITY
        //cyclic ring
        menuList.put(MENU_CYCLICRING,   new MenuItem(R.drawable.i12,     R.string.cyclic_ring_range_no_break, ServosCyclickRingRangeActivity.class));

        //rudder endpoints
        menuList.put(MENU_ENDPOINTS,   new MenuItem(R.drawable.i13,     R.string.rudder_end_points_no_break, ServosRudderEndPointsActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_SERVOLIMIT, new Integer[]{MENU_CYCLICRING, MENU_ENDPOINTS});


        //GOVERNOR
        menuList.put(MENU_GOV_MODE,   new MenuItem(R.drawable.i43,     R.string.governor_mode, GovernorModeActivity.class));

        menuList.put(MENU_GOV_GAIN,   new MenuItem(R.drawable.i44,     R.string.governor_gain, GovernorGainActivity.class));

        menuList.put(MENU_GOV_THR_RANGE,   new MenuItem(R.drawable.i39,     R.string.governor_thr_range, GovernorThrRangeActivity.class));

        menuList.put(MENU_GOV_RPM_MAX,   new MenuItem(R.drawable.i55,     R.string.governor_rpm_max, GovernorRpmMaxActivity.class));

        menuList.put(MENU_GOV_GEAR_SETTINGS,   new MenuItem(R.drawable.i60,     R.string.governor_gear_settings, GovernorGearSettingsActivity.class));

        menuList.put(MENU_GOV_RPM_SENZOR,   new MenuItem(R.drawable.i51,     R.string.governor_rpm_senzor, GovernorRpmSenzor.class));

        menuList.put(MENU_GOV_THR_REVERSE,   new MenuItem(R.drawable.i53,     R.string.governor_thr_reverse, GovernorThrReverseActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_GOVERNOR, new Integer[]{MENU_GOV_MODE, MENU_GOV_THR_RANGE, MENU_GOV_THR_REVERSE, MENU_GOV_GEAR_SETTINGS, MENU_GOV_RPM_MAX, MENU_GOV_GAIN, MENU_GOV_RPM_SENZOR});

    }

    /**
     * vrati pole polozek menu pro skupinu menu
     *
     * @param group
     * @return
     */
    public Integer[] getItemForGroup(int group)
    {
        if(menuGroups.containsKey(group)){
            return menuGroups.get(group);
        }

        return new Integer[0];
    }

    /**
     * vrati polozku menu
     *
     * @param itemId
     * @return
     */
    public MenuItem getItem(int itemId)
    {
        if(menuList.containsKey(itemId)){
            return menuList.get(itemId);
        }

        throw new IndexOutOfBoundsException();
    }
}
