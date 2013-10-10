package com.customWidget.picker;

/**
 * interface pro preklad zobrazeni cisel v progressEx
 * 
 * @author petrcada
 *
 */
public abstract interface ProgresExViewTranslateInterface {

	/**
	 * 
	 * @param current
	 * @return
	 */
	public String translateCurrent(int current);
	
	/**
	 * 
	 * @param min
	 * @return
	 */
	public String translateMin(int min);
	
	/**
	 * 
	 * @param max
	 * @return
	 */
	public String translateMax(int max);
	
}
