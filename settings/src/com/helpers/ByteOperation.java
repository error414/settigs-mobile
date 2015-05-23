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
 * operace s Bytema
 *  
 * @author error414
 */
public class ByteOperation {

	/**
	 * one byte to unsigned int 0 - 255
	 * 
	 * @param b
	 * @return
	 */
	final public static int byteToUnsignedInt(byte b) {
		return 0x00 << 8 | (b & 0xff);
	}
    /**
     * Convert a byte array integer (4 bytes) to its int value nebo 1 byte
     * @param b byte[]
     * @return int
     */
    public static int byteArrayToUnsignedInt(byte[] b) {
        if(b == null){
            return 0;
        }

        if(b.length == 4)
            return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 | (b[3] & 0xff);
        else if(b.length == 2)
            return (b[0] & 0xff) << 8 | (b[1] & 0xff);

        else if(b.length == 1)
            return (b[0] & 0xff);

        return 0;
    }

    public static int byte2ArrayToSigInt(byte[] b){
        if(b.length == 2) {
            return ((b[0] & 0xff) | (b[1] << 8)) << 16 >> 16;
        }else if(b.length == 1){
            return (int)b[0];
        }

        return 0;
    }

    public static int twoByteToSigInt(byte b1, byte b2){
        byte[] b = {b1, b2};
        return byte2ArrayToSigInt(b);

    }

    /**
     *
     * @param data
     * @return
     */
    public static Short byteArrayToShort(byte[] data)
    {
        return (short)((data[0] & 0xFF) | data[1]<<8);
    }

    /**
     *
     * @param value
     * @return
     */
    final public static byte[] intToByteArray(int value)
    {
        if(value < 256){
            byte[] ret = new byte[1];
            ret[0] = (byte) (value & 0xFF);
            return ret;
        }

        byte[] ret = new byte[2];
        ret[0] = (byte) (value & 0xFF);
        ret[1] = (byte) ((value >> 8) & 0xFF);
        return ret;
    }

    /**
     * integer do jednho bytu
     *
     * @param value
     * @return
     */
    final public static byte intToByte(int value)
    {
        return (byte) (value & 0xFF);
    }

    /**
	 * slouceni dvou byte poli
	 * 
	 * @param one
	 * @param two
	 * @return
	 */
	final public static byte[] combineByteArray(byte[] one, byte[] two){
		if(two == null) return one;
		
		
		byte[] combined = new byte[one.length + two.length];

		System.arraycopy(one,0,combined,0         ,one.length);
		System.arraycopy(two,0,combined,one.length,two.length);
		
		return combined;
	}
	
	/**
	 * vraci string cisla z pole bytu
	 * 
	 * @param b
	 * @return
	 */
	final public static String getIntegerStringByByteArray(byte[] b){
		if(b == null || b.length == 0){
			return "";
		}
		
		String buffer = "";
		for(int i = 0; i < b.length; i++){
			buffer += String.valueOf(byteToUnsignedInt(b[i])) + ", ";
		}
		
		return buffer;
	}
	
	/**
	 * vraci hex string cisla z pole bytu
	 * 
	 * @param b
	 * @return
	 */
	final public static String getHexStringByByteArray(byte[] b){
		if(b == null || b.length == 0){
			return "";
		}
		
		String buffer = "";
		for(int i = 0; i < b.length; i++){
			buffer += ByteOperation.byteToHexString(b[i]) + ", ";
		}
		
		return buffer;
	}

	/**
	 * byte to hex string
	 * 
	 * @param array
	 * @return
	 */
	final public static String byteToHexString(byte array) {
		StringBuffer hexString = new StringBuffer();
		int intVal = array & 0xff;
		if (intVal < 0x10){
			hexString.append("0");
		}
		hexString.append(Integer.toHexString(intVal));
		return hexString.toString();    
	}
}
