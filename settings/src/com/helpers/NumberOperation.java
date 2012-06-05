package com.helpers;

public class NumberOperation {

	//val*100/255
	final static public int numberToPercent(int base, int num)
	{
		if(base == 0) return 0;
		
		return (int)(((float)100 / (float)base) * (float)num);
	}
	
	final static public int percentToNumber(int base, int percent)
	{
		if(base == 0) return 0;
		if(percent == 0) return 0;
		
		return (int) ((float)base / ((float)100 / (float)percent)); 
	}
	
}
