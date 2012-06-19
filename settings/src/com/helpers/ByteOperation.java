package com.helpers;

import java.nio.ByteBuffer;

/**
 * operace s Bytema
 *  
 * @author error414
 */
public class ByteOperation {

	/**
	 * gyte to int
	 * 
	 * @param b
	 * @return
	 */
	final public static int byteToUnsignedInt(byte b) {
		return 0x00 << 8 | (b & 0xff);
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
     * Convert a byte array integer (4 bytes) to its int value nebo 1 byte
     * @param b byte[]
     * @return int
     */
    public static int byteArrayToInt(byte[] b) {
        if(b.length == 4)
            return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 | (b[3] & 0xff);
        else if(b.length == 2)
            return 0x00 << 24 | 0x00 << 16 | (b[0] & 0xff) << 8 | (b[1] & 0xff);
        
        else if(b.length == 1)
            return 0x00 << 24 | 0x00 << 16 |  0x00 << 8 | (b[0] & 0xff);

        return 0;
    }
	
    /**
     * integer rozparsuje do byte array, podporuje 4,2 a 1 prvkove pole byte
     * 
     * @param value
     * @return
     */
	final public static byte[] intToByteArray(int value)
	{
		byte[] ret = new byte[1];
	    ret[0] = (byte) (value & 0xFF); 

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
	 * byte to hex string
	 * 
	 * @param array
	 * @return
	 */
	final public static String byteArrayToHexString(byte array) {
		StringBuffer hexString = new StringBuffer();
		int intVal = array & 0xff;
		if (intVal < 0x10){
			hexString.append("0");
		}
		hexString.append(Integer.toHexString(intVal));
		return hexString.toString();    
	}
}
