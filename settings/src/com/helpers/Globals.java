package com.helpers;

import android.app.NotificationManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * trida pro globalni uloziste persistetnich dat, ne nastaveni
 * 
 * @author petrcada
 *
 */
public class Globals {
	
	public final static int BANK_NULL = -1;
	public final static int BANK_0 = 0;
	public final static int BANK_1 = 1;
	public final static int BANK_2 = 2;
	
	static private Globals instance;
	
	/**
	 * byla data v jednotce zmenena
	 */
	private Boolean changed = false;
	
	/**
	 * byla data v jednotce zmenena
	 */
	private int activeBank;

    /**
     * priznak jestli volat init ktery se ma volat jen po pripojeni
     */
    private boolean callInitAfterConnect = true;

    /* for protect unsave change */
    public static final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;

    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;
    private NotificationManager unsaveNotify;
    /* **************************** */

	/**
	 *
	 */
	private Globals(){
	}

	static public Globals getInstance(){
		if(instance == null){
			instance = new Globals();
		}
		
		return instance;
	}
	
	
	///// CHANGED ///////////////////////////////////
	/**
	 *
	 * @return
	 */
	public Boolean isChanged()
	{
		return changed;
	}

	public void setChanged(Boolean changed)
	{
		this.changed = changed;
	}
	///////////////////////////////////
	
	///// BANK ///////////////////////////////////
	public int getActiveBank() {
		return activeBank;
	}

	public void setActiveBank(int activeBank) {
		this.activeBank = activeBank;
	}
	///////////////////////////////////


    public boolean isCallInitAfterConnect() {
        return callInitAfterConnect;
    }

    public void setCallInitAfterConnect(boolean callInitAfterConnect) {
        this.callInitAfterConnect = callInitAfterConnect;
    }

    ///// PROTECT unchange ///////////////////////////////////
    public Timer getmActivityTransitionTimer() {
        return mActivityTransitionTimer;
    }

    public void setmActivityTransitionTimer(Timer mActivityTransitionTimer) {
        this.mActivityTransitionTimer = mActivityTransitionTimer;
    }

    public TimerTask getmActivityTransitionTimerTask() {
        return mActivityTransitionTimerTask;
    }

    public void setmActivityTransitionTimerTask(TimerTask mActivityTransitionTimerTask) {
        this.mActivityTransitionTimerTask = mActivityTransitionTimerTask;
    }

    public NotificationManager getUnsaveNotify() {
        return unsaveNotify;
    }

    public void setUnsaveNotify(NotificationManager unsaveNotify) {
        this.unsaveNotify = unsaveNotify;
    }




}
