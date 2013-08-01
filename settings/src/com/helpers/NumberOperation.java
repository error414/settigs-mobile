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
		
		return (double)(((double)num * 100) / (double)base);
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