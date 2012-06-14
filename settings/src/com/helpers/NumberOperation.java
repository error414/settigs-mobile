package com.helpers;

/**
 * operace s cisly
 * 
 * @author error414
 *
 */
public class NumberOperation {

	/**
	 * cislo na procenta
	 * 
	 * zaklad = zaklad je cislo ktere odpovida 100%
	 * cislo
	 * 
	 * @param base
	 * @param num
	 * @return
	 */
	final static public double numberToPercent(int base, int num)
	{
		if(base == 0) return 0;
		
		return NumberOperation.round((double)(((float)num * 100) / (float)base), 1);
	}
	
	/**
	 * procenta na cislo
	 * 
	 * zaklad = zaklad je cislo ktere odpovida 100%
	 * procne
	 * 
	 * @param base
	 * @param num
	 * @return
	 */
	final static public double percenToNumber(int base, int percent)
	{
		if(base == 0) return 0;
		
		return (double)((double)((double)base / 100) * (double)percent);
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}

	
}