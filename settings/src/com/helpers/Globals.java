package com.helpers;

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
	private Boolean changed = false;

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
}
