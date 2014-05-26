package com.lib;

import com.helpers.DstabiProfile;

/**
 * trida pro globalni uloziste persistetnich dat, ne nastaveni
 * 
 * @author petrcada
 *
 */
public class Globals {
	
	static private Globals instance;
	
	/**
	 * byla data v jednotce zmenena
	 */
	public Boolean changed = false;

	private DstabiProfile originalProfile;

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

	/**
	 *
	 * @return
	 */
	public Boolean getChanged()
	{
		return changed;
	}

	public void setChanged(Boolean changed)
	{
		this.changed = changed;
	}

	/**
	 * puvodni profil pro zjisteni rozdilu
	 *
	 * @return
	 */
	public DstabiProfile getOriginalProfile()
	{
		return originalProfile;
	}

	/**
	 *
	 * @param originalProfile
	 */
	public void setOriginalProfile(DstabiProfile originalProfile)
	{
		this.originalProfile = originalProfile;
	}
}
