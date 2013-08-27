package com.lib;

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
	
	private Globals(){
	}
	
	static public Globals getInstance(){
		if(instance == null){
			instance = new Globals();
		}
		
		return instance;
	}
	
}
