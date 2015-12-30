package com.lib.menu;

import com.helpers.DstabiProfile;
import com.helpers.Globals;
import com.spirit.ConnectionActivity;
import com.spirit.FavouritesActivity;
import com.spirit.R;
import com.spirit.aero.advanced.FFActivity;
import com.spirit.aero.limit.LimitAilRangeActivity;
import com.spirit.aero.limit.LimitEleRangeActivity;
import com.spirit.aero.limit.LimitRudRangeActivity;
import com.spirit.heli.GeneralActivity;
import com.spirit.heli.advanced.AdvancedActivity;
import com.spirit.heli.advanced.CyclicFFActivity;
import com.spirit.heli.advanced.EFilterActivity;
import com.spirit.heli.advanced.GeometryAngleActivity;
import com.spirit.heli.advanced.PirouetteConsistencyActivity;
import com.spirit.heli.advanced.RotorRotationActivity;
import com.spirit.heli.advanced.RudderDelayActivity;
import com.spirit.heli.advanced.RudderDynamicActivity;
import com.spirit.heli.advanced.RudderRevomixActivity;
import com.spirit.heli.advanced.expert.CyclicPhaseActivity;
import com.spirit.heli.advanced.expert.ExpertActivity;
import com.spirit.heli.advanced.expert.PitchpumpActivity;
import com.spirit.heli.advanced.expert.PitchupActivity;
import com.spirit.heli.advanced.expert.RpmSenzorFilterActivity;
import com.spirit.heli.advanced.expert.SignalProcessingActivity;
import com.spirit.heli.advanced.expert.StickDeadBandActivity;
import com.spirit.heli.diagnostic.BecTesterActivity;
import com.spirit.heli.diagnostic.DiagnosticActivity;
import com.spirit.heli.diagnostic.GraphActivity;
import com.spirit.heli.diagnostic.InputChannelsActivity;
import com.spirit.heli.diagnostic.LogActivity;
import com.spirit.heli.governorthr.GovernorFreqActivity;
import com.spirit.heli.governorthr.GovernorThrRangeActivity;
import com.spirit.heli.governorthr.GovernorThrReverseActivity;
import com.spirit.heli.governorthr.governor.GovernorActivity;
import com.spirit.heli.governorthr.governor.GovernorFineTuningActivity;
import com.spirit.heli.governorthr.governor.GovernorGearSettingsActivity;
import com.spirit.heli.governorthr.governor.GovernorOnActivity;
import com.spirit.heli.governorthr.governor.GovernorRamPupActivity;
import com.spirit.heli.governorthr.governor.GovernorRpmMaxActivity;
import com.spirit.heli.governorthr.governor.GovernorRpmSenzor;
import com.spirit.heli.governorthr.governor.GovernorSpoolUpActivity;
import com.spirit.heli.limit.CollectivePitchActivity;
import com.spirit.heli.limit.ServosCyclickRingRangeActivity;
import com.spirit.heli.limit.ServosLimitActivity;
import com.spirit.heli.limit.ServosRudderEndPointsActivity;
import com.spirit.heli.senzor.SenzorActivity;
import com.spirit.heli.senzor.SenzorRotationSpeedActivity;
import com.spirit.heli.senzor.SenzorSenzivityActivity;
import com.spirit.heli.servo.ServoReverseActivity;
import com.spirit.heli.servo.ServoTravelCorrectionActivity;
import com.spirit.heli.servo.ServosActivity;
import com.spirit.heli.servo.ServosSubtrimActivity;
import com.spirit.heli.servo.ServosTypeActivity;
import com.spirit.heli.stabi.StabiAcroDelayActivity;
import com.spirit.heli.stabi.StabiActivity;
import com.spirit.heli.stabi.StabiColActivity;
import com.spirit.heli.stabi.StabiCtrlDirActivity;
import com.spirit.heli.stabi.StabiFbModeActivity;
import com.spirit.heli.stabi.StabiFunctionActivity;
import com.spirit.heli.stabi.StabiStickActivity;

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
    public static Integer MENU_INDEX_GOVERNOR_THR   = 7;
    public static Integer MENU_INDEX_GOVERNOR       = 10;
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
    //public static Integer MENU_REVERSE              = 23;
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
    public static Integer STABI_ACRO_DELAY          = 58;

    public static Integer MENU_CYCLICRING            = 33;
    public static Integer MENU_ENDPOINTS             = 34;
    public static Integer MENU_PITCHRANGE            = 53;


    public static Integer MENU_GOV_ON              = 52;
    public static Integer MENU_GOV_GOV             = 51;
    public static Integer MENU_GOV_FREQ            = 38;
    public static Integer MENU_GOV_FINE_TUNING     = 39;
    public static Integer MENU_GOV_RPM_SENZOR      = 44;
    public static Integer MENU_GOV_THR_RANGE       = 45;
    public static Integer MENU_GOV_RPM_MAX         = 46;
    public static Integer MENU_GOV_GEAR_SETTINGS   = 47;
    public static Integer MENU_GOV_THR_REVERSE     = 48;
    public static Integer MENU_GOV_SPOOLUP         = 50;
    public static Integer MENU_GOV_RAMPUP = 62;

    public static Integer MENU_FF                  = 57;

    public static Integer MENU_LIMIT_AIL           = 54;
    public static Integer MENU_LIMIT_ELE           = 55;
    public static Integer MENU_LIMIT_RUD           = 56;

    public static Integer MENU_RPM_SENZOR_FILTER   = 60;
    //public static Integer MENU_AUTOROTATION_BAILOUT = 61;

    public static Integer MENU_DIAGNOSTIC_LIST     = 42;

    protected static Map<Integer, Menu> instances = new HashMap<Integer, Menu>();

    protected Map<Integer, MenuItem> menuList = new HashMap<Integer, MenuItem>();

    /**
     * singleton
     *
     * @return
     */
    protected Menu(){
    }

    /**
     * singleton
     *
     * @return
     */
    public static Menu getInstance(){

        if(!instances.containsKey(Globals.getInstance().getAppMode()))
        {
            Menu menu = new Menu();

            switch(Globals.getInstance().getAppMode()) {
                default:
                case DstabiProfile.HELI:
                    menu.createMenuHeli();
                    break;
                case DstabiProfile.AERO:
                    menu.createMenuAero();
                    break;
            }

            instances.put(Globals.getInstance().getAppMode(), menu);
        }

        return instances.get(Globals.getInstance().getAppMode());
    }

    /**
     * vytovreni seznamu menu
     *
     */
    private void createMenuHeli() {

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

        //add to groups
        menuGroups.put(MENU_INDEX_SETTINGS, new Integer[]{MENU_CONNECTION, MENU_FAVOURITES, MENU_GENERAL, MENU_DIAGNOSTIC_LIST, MENU_SERVO, MENU_SERVOLIMIT, MENU_SENZOR, MENU_STABI, MENU_ADVANCED});



        //DIAGNOSTIC ACTIVITY
        //diagnostic
        menuList.put(MENU_DIAGNOSTIC,   new MenuItem(R.drawable.i37,     R.string.input_channels, InputChannelsActivity.class));

        //graph
        menuList.put(MENU_GRAPH,        new MenuItem(R.drawable.i38,     R.string.graph_button_text, GraphActivity.class));

        //log
        menuList.put(MENU_LOG,          new MenuItem(R.drawable.i40,     R.string.log_button_text, LogActivity.class));

        //BEC Tester
        menuList.put(MENU_BEC,     new MenuItem(R.drawable.na,     R.string.bec_tester, BecTesterActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_DIAGNOSTIC, new Integer[]{MENU_DIAGNOSTIC, MENU_GRAPH, MENU_LOG, MENU_BEC});



        //ADVANCED ACTIVITY

        //geometry 6deg
        menuList.put(MENU_6DEG,   new MenuItem(R.drawable.i48,     R.string.geom_6deg, GeometryAngleActivity.class));

        //piruette opt
        menuList.put(MENU_PIROOPT, new MenuItem(R.drawable.i26, R.string.rotor_rotation, RotorRotationActivity.class));

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
                         MENU_PIROOPT, MENU_6DEG, MENU_EFILTER, MENU_CYCLICFF,
                         MENU_RUDDERDELAY, MENU_RUDDERDYNAMIC, MENU_RUDDERREVOMIX,
                         MENU_EXPERT, MENU_PIROUETTECONSISTENCY,
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


        //rpm senzor filetr
        menuList.put(MENU_RPM_SENZOR_FILTER,   new MenuItem(R.drawable.na,     R.string.rpm_senzor_filter, RpmSenzorFilterActivity.class));

        //add to groups
        menuGroups.put(
            MENU_INDEX_ADVANCED_EXPERT,
                new Integer[]{
                        MENU_DEADBAND, MENU_PITCHUP, MENU_CYCLICPHASE, MENU_PITCHPUMP, MENU_SIGNALPROCESSING, MENU_RPM_SENZOR_FILTER,
                }
        );


        //SENZOR ACTIVITY
        //senzivity
        menuList.put(MENU_SENZIVITY,   new MenuItem(R.drawable.i16,     R.string.senzivity, SenzorSenzivityActivity.class));

        //rotation speed
        menuList.put(MENU_ROTATIONSPEED,   new MenuItem(R.drawable.i18,     R.string.rotation_speed, SenzorRotationSpeedActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_SENZOR, new Integer[]{MENU_SENZIVITY, MENU_ROTATIONSPEED});


        //SERVO ACTIVITY
        //type
        menuList.put(MENU_SERVOTYPE,   new MenuItem(R.drawable.i9,     R.string.type, ServosTypeActivity.class));

        //subtrim
        menuList.put(MENU_SERVOSUBTRIM,   new MenuItem(R.drawable.i10,     R.string.subtrim,  ServosSubtrimActivity.class));

        //korekce drahy serv
        menuList.put(MENU_SERVOTRAVELCORRECTION,   new MenuItem(R.drawable.i41,     R.string.servo_travel_correction, ServoTravelCorrectionActivity.class));

        //reverz
        menuList.put(MENU_REVERZ, new MenuItem(R.drawable.i17, R.string.reverse, ServoReverseActivity.class));

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

        //STABI_ACRO_DELAY
        menuList.put(STABI_ACRO_DELAY,   new MenuItem(R.drawable.na,     R.string.acro_delay, StabiAcroDelayActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_STABI, new Integer[]{MENU_STABIFUNCTION, MENU_STABICOL, MENU_STABISTICK, MENU_STABIFBMODE, STABI_CTRLDIR, STABI_ACRO_DELAY});


        //SERVO LIMIT ACTIVITY
        //cyclic ring
        menuList.put(MENU_CYCLICRING,   new MenuItem(R.drawable.i12,     R.string.cyclic_ring_range_no_break, ServosCyclickRingRangeActivity.class));

        //pitch range
        menuList.put(MENU_PITCHRANGE,   new MenuItem(R.drawable.na,     R.string.limit_pitch, CollectivePitchActivity.class));

        //rudder endpoints
        menuList.put(MENU_ENDPOINTS,   new MenuItem(R.drawable.i13,     R.string.rudder_end_points_no_break, ServosRudderEndPointsActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_SERVOLIMIT, new Integer[]{MENU_CYCLICRING, MENU_PITCHRANGE, MENU_ENDPOINTS});


        //GOVERNOR / THR
        menuList.put(MENU_GOV_GOV,   new MenuItem(R.drawable.na,     R.string.governor, GovernorActivity.class));

        menuList.put(MENU_GOV_FREQ,   new MenuItem(R.drawable.i43,     R.string.governor_freq, GovernorFreqActivity.class));

        menuList.put(MENU_GOV_THR_RANGE,   new MenuItem(R.drawable.i39,     R.string.governor_thr_range, GovernorThrRangeActivity.class));

        menuList.put(MENU_GOV_THR_REVERSE,   new MenuItem(R.drawable.i53,     R.string.governor_thr_reverse, GovernorThrReverseActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_GOVERNOR_THR, new Integer[]{MENU_GOV_GOV, MENU_GOV_FREQ, MENU_GOV_THR_RANGE, MENU_GOV_THR_REVERSE});

        //GOVERNOR
        menuList.put(MENU_GOV_ON,   new MenuItem(R.drawable.na,     R.string.governor_mode, GovernorOnActivity.class));

        menuList.put(MENU_GOV_FINE_TUNING,   new MenuItem(R.drawable.i44,     R.string.governor_gain, GovernorFineTuningActivity.class));

        menuList.put(MENU_GOV_RPM_MAX,   new MenuItem(R.drawable.i55,     R.string.governor_rpm_max, GovernorRpmMaxActivity.class));

        menuList.put(MENU_GOV_GEAR_SETTINGS,   new MenuItem(R.drawable.i60,     R.string.governor_gear_settings, GovernorGearSettingsActivity.class));

        menuList.put(MENU_GOV_SPOOLUP,   new MenuItem(R.drawable.na,     R.string.governor_spoolup, GovernorSpoolUpActivity.class));

        menuList.put(MENU_GOV_RPM_SENZOR,   new MenuItem(R.drawable.i51,     R.string.governor_rpm_senzor, GovernorRpmSenzor.class));

        menuList.put(MENU_GOV_RAMPUP, new MenuItem(R.drawable.na, R.string.governor_rampup, GovernorRamPupActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_GOVERNOR, new Integer[]{MENU_GOV_ON, MENU_GOV_GEAR_SETTINGS, MENU_GOV_RPM_MAX, MENU_GOV_SPOOLUP, MENU_GOV_FINE_TUNING, MENU_GOV_RPM_SENZOR, MENU_GOV_RAMPUP,});
    }

    /**
     * vytovreni seznamu menu
     *
     */
    private void createMenuAero() {

        //SETTINGS ACTIVITY

        //connection
        menuList.put(MENU_FAVOURITES,   new MenuItem(R.drawable.i49,     R.string.favourites_button_text, FavouritesActivity.class));

        //connection
        menuList.put(MENU_CONNECTION,   new MenuItem(R.drawable.i4,     R.string.connection_button_text, ConnectionActivity.class));

        //general
        menuList.put(MENU_GENERAL,      new MenuItem(R.drawable.i6,     R.string.general_button_text, com.spirit.aero.GeneralActivity.class));

        //servo
        menuList.put(MENU_SERVO,        new MenuItem(R.drawable.i8,     R.string.servos_button_text, ServosActivity.class));

        //servo
        menuList.put(MENU_SERVOLIMIT,   new MenuItem(R.drawable.i11,     R.string.limits, ServosLimitActivity.class));

        //senzor
        menuList.put(MENU_SENZOR,       new MenuItem(R.drawable.i15,     R.string.senzor_button_text, SenzorActivity.class));

        //stabi
        menuList.put(MENU_STABI,        new MenuItem(R.drawable.i50,     R.string.stabi_button_text, StabiActivity.class));

        //advanced
        menuList.put(MENU_ADVANCED,     new MenuItem(R.drawable.i20,     R.string.advanced_button_text, AdvancedActivity.class));

        //diagnostic
        menuList.put(MENU_DIAGNOSTIC_LIST,   new MenuItem(R.drawable.i37,     R.string.diagnostic_button_text, DiagnosticActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_SETTINGS, new Integer[]{MENU_CONNECTION, MENU_FAVOURITES, MENU_GENERAL, MENU_DIAGNOSTIC_LIST, MENU_SERVO, MENU_SERVOLIMIT, MENU_SENZOR, MENU_STABI, MENU_ADVANCED, });


        //SERVO ACTIVITY
        //type
        menuList.put(MENU_SERVOTYPE,   new MenuItem(R.drawable.i9,     R.string.type, com.spirit.aero.servo.ServosTypeActivity.class));

        //subtrim
        menuList.put(MENU_SERVOSUBTRIM,   new MenuItem(R.drawable.i10,     R.string.subtrim,  ServosSubtrimActivity.class));

        //reverz
        menuList.put(MENU_REVERZ,   new MenuItem(R.drawable.na,     R.string.reverse, com.spirit.aero.servo.ServoReverseActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_SERVO, new Integer[]{MENU_SERVOTYPE, MENU_SERVOSUBTRIM, MENU_REVERZ, });


        //SERVO LIMIT ACTIVITY
        //cyclic ring
        menuList.put(MENU_LIMIT_AIL,   new MenuItem(R.drawable.na,     R.string.limit_range_ail, LimitAilRangeActivity.class));

        //pitch range
        menuList.put(MENU_LIMIT_ELE,   new MenuItem(R.drawable.na,     R.string.limit_range_ele, LimitEleRangeActivity.class));

        //rudder endpoints
        menuList.put(MENU_LIMIT_RUD,   new MenuItem(R.drawable.na,     R.string.limit_range_rud, LimitRudRangeActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_SERVOLIMIT, new Integer[]{MENU_LIMIT_AIL, MENU_LIMIT_ELE, MENU_LIMIT_RUD});


        //SENZOR ACTIVITY
        //senzivity
        menuList.put(MENU_SENZIVITY,   new MenuItem(R.drawable.i16,     R.string.senzivity, com.spirit.aero.senzor.SenzorSenzivityActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_SENZOR, new Integer[]{MENU_SENZIVITY,});


        //STABI ACTIVITY
        //function
        menuList.put(MENU_STABIFUNCTION,   new MenuItem(R.drawable.i50,     R.string.stabi_function, com.spirit.aero.stabi.StabiFunctionActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_STABI, new Integer[]{MENU_STABIFUNCTION,});


        //ADVANCED ACTIVITY

        //cyclic ff
        menuList.put(MENU_FF,   new MenuItem(R.drawable.na,     R.string.cyclic_ff, FFActivity.class));

        //stick deadband
        menuList.put(MENU_DEADBAND,   new MenuItem(R.drawable.i22,     R.string.stick_deadband, com.spirit.aero.advanced.StickDeadBandActivity.class));


        //add to groups
        menuGroups.put(MENU_INDEX_ADVANCED, new Integer[]{MENU_FF, MENU_DEADBAND, });

        //DIAGNOSTIC ACTIVITY
        //diagnostic
        menuList.put(MENU_DIAGNOSTIC,   new MenuItem(R.drawable.i37,     R.string.input_channels, com.spirit.aero.diagnostic.InputChannelsActivity.class));

        //graph
        menuList.put(MENU_GRAPH,        new MenuItem(R.drawable.i38,     R.string.graph_button_text, GraphActivity.class));

        //log
        menuList.put(MENU_LOG,          new MenuItem(R.drawable.i40,     R.string.log_button_text, LogActivity.class));


        //BEC Tester
        menuList.put(MENU_BEC,     new MenuItem(R.drawable.na,     R.string.bec_tester, BecTesterActivity.class));

        //add to groups
        menuGroups.put(MENU_INDEX_DIAGNOSTIC, new Integer[]{MENU_DIAGNOSTIC, MENU_GRAPH, MENU_LOG, MENU_BEC});



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

    /**
     *
     * @param itemId
     * @return
     */
    public boolean hasItem(int itemId)
    {
        return menuList.containsKey(itemId);
    }
}
