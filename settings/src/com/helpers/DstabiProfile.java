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

/**
 * 
 */
package com.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.util.Log;

import com.exception.IndexOutOfException;

/**
 * @author error414
 *
 * trida pro praci s profilem
 */
public class DstabiProfile {

	final static String TAG = "DstabiProfile";
	
	private int profileLenght;

	private byte[] mProfile;

	private HashMap<String, ProfileItem> profileMap = new HashMap<String, ProfileItem>();
	
	private ArrayList<String> profileErrors = new ArrayList<String>();

	/**
	 *
	 * @param mProfile
	 */
	public DstabiProfile(byte[] mProfile)
	{
		this.buildProfile(mProfile);
	}

	/**
	 * update profilu, doporucuji nedelat s ostrym profilem
	 *
	 * @param mProfile
	 */
	public void updateProfile(byte[] mProfile){
		this.buildProfile(mProfile);
	}

	/**
	 * vytvoreni profilu
	 *
	 * @param mProfile
	 */
	protected void buildProfile(byte[] mProfile)
	{
		/* MTODO nazvy udelat v konstantach */
		profileMap.put("MAJOR", 	new ProfileItem(1, 0, 255, 		null)); // 'major', INT,
		profileMap.put("MINOR", 	new ProfileItem(2, 0, 255, 		null)); // 'minor', INT

		profileMap.put("POSITION", 	new ProfileItem(3, "A", "C", 	"P")); // position_text, ENUM, position_values
		//profileMap.put("MODEL", 	new ProfileItem(4, "A", "C", 	"M"));
        //profileMap.put("SIGNAL_PROCESSING",	new ProfileItem(4, "0", "1", "7")); // rozsirene zpracovani signalu

		profileMap.put("RECEIVER",	new ProfileItem(5, "A", "E", 	"R"));
		profileMap.put("MIX",	 	new ProfileItem(6, "A", "D", 	"C"));

		profileMap.put("CYCLIC_TYPE",	new ProfileItem(7, "A", "A", 	"ST"));
		profileMap.put("CYCLIC_FREQ",	new ProfileItem(8, "A", "F", 	"SF"));
		profileMap.put("RUDDER_TYPE",	new ProfileItem(9, "A", "C", 	"St"));
		profileMap.put("RUDDER_FREQ",	new ProfileItem(10, "A", "G", 	"Sf"));

		profileMap.put("SUBTRIM_AIL",	new ProfileItem(16, 0, 255, 	"SA"));
		profileMap.put("SUBTRIM_ELE",	new ProfileItem(17, 0, 255, 	"SE"));
		profileMap.put("SUBTRIM_PIT",	new ProfileItem(18, 0, 255, 	"SP"));
		profileMap.put("SUBTRIM_RUD",	new ProfileItem(12, 0, 255, 	"Se"));

		profileMap.put("RANGE_AIL",		new ProfileItem(11, 0, 255, 	"Sa"));	// cyclic ring
		profileMap.put("RANGE_PIT",		new ProfileItem(13, 0, 255, 	"Sp"));	// rozsah kolektivu

		profileMap.put("RUDDER_MIN",	new ProfileItem(14, 0, 255, 	"Sm"));
		profileMap.put("RUDDER_MAX",	new ProfileItem(15, 0, 255, 	"SM"));

		profileMap.put("SENSOR_SENX",	new ProfileItem(19, 0, 80, "x")); 		// procenta
		profileMap.put("GEOMETRY",		new ProfileItem(20, 64, 250, "8"));		// geometrie hlavy - 6��
		profileMap.put("SENSOR_SENZ",	new ProfileItem(21, 50, 100, "z")); 	// nasobic

		profileMap.put("SENSOR_REVX",	new ProfileItem(22, "0", "1", "X"));
		profileMap.put("SENSOR_REVY",	new ProfileItem(23, "0", "1", "Y"));
		profileMap.put("SENSOR_REVZ",	new ProfileItem(24, "0", "1", "Z"));
        //
		profileMap.put("RATE_PITCH",	new ProfileItem(25, 5, 16, 	"a"));		// rychlost rotace cykliky
		profileMap.put("CYCLIC_FF",		new ProfileItem(26, 0, 10, 	"b"));		// pocatecni reakce cykliky
		profileMap.put("RATE_YAW",		new ProfileItem(27, 4, 20, 	"c"));		// rychlost rotace vrtulky

		profileMap.put("PITCHUP",	    new ProfileItem(28, 0, 4, "r")); 	// kompenzace zpinani vyskovky
		profileMap.put("STICK_DB",		new ProfileItem(29, 4, 30, "s"));  // mrtva zona knyplu
		profileMap.put("RUDDER_STOP",	new ProfileItem(30, 3, 10, "p")); 		// dynamika vrtulky
		profileMap.put("ALT_FUNCTION",	new ProfileItem(31, "A", "D", "f")); 	// alternativni funkce
        profileMap.put("CYCLIC_REVERSE",	new ProfileItem(32, "A", "D", 	"v"));
		profileMap.put("RUDDER_REVOMIX",new ProfileItem(33, 118, 138, "m")); //

		profileMap.put("STABI_CTRLDIR", new ProfileItem(34, 1, 5, "0"));  // M��ra zm��ny sm��ru
		profileMap.put("STABI_COL",     new ProfileItem(35, 117, 137, "1")); 		// kolektiv zachranneho rezimu
		//profileMap.put("STABI_ROLL",    new ProfileItem(36, 63, 191, "2")); // stabi, kompenzace pro kridelka
		profileMap.put("STABI_STICK",   new ProfileItem(37, 0, 10, "3")); // priorita knyplu

		profileMap.put("PIROUETTE_CONST",	new ProfileItem(38, 64, 250, "H")); // konzistence piruet

		profileMap.put("CHECKSUM_LO",	new ProfileItem(36, 0, 255, null)); 	// checksum pro kontrolu dat
		profileMap.put("CHECKSUM_HI",	new ProfileItem(39, 0, 255, null)); 	// checksum pro kontrolu dat
		
		profileMap.put("CYCLIC_PHASE",	new ProfileItem(40, -90, 90, "5")); // virtualni pooto��en�� cykliky

		profileMap.put("PIRO_OPT",		new ProfileItem(42, "0", "1", "o"));
		profileMap.put("E_FILTER",		new ProfileItem(43, 0, 4, "4")); 		// kompenzace zpinani vyskovky

		profileMap.put("RUDDER_DELAY",	new ProfileItem(44, 0, 30, "9")); // zpozdeni vrtulky

		profileMap.put("FLIGHT_STYLE",	new ProfileItem(45, 0, 7, "l"));		// letovy projev

		//profileMap.put("STABI_PITCH",new ProfileItem(46, 63, 191, "q")); // stabi, kompenzace pro vyskovku
		profileMap.put("FB_MODE",		new ProfileItem(46, "0", "1", "i")); // flybar mechanic

		profileMap.put("TRAVEL_UAIL",	new ProfileItem(47, 63, 191, "QA"));
		profileMap.put("TRAVEL_UELE",	new ProfileItem(48, 63, 191, "QE"));
		profileMap.put("TRAVEL_UPIT",	new ProfileItem(49, 63, 191, "QP"));
		profileMap.put("TRAVEL_DAIL",	new ProfileItem(50, 63, 191, "Qa"));
		profileMap.put("TRAVEL_DELE",	new ProfileItem(51, 63, 191, "Qe"));
		profileMap.put("TRAVEL_DPIT",	new ProfileItem(52, 63, 191, "Qp"));

		this.mProfile = mProfile;


		Iterator<String> iteration = profileMap.keySet().iterator();
		while(iteration.hasNext()) {
			String key=(String)iteration.next();
			ProfileItem item = (ProfileItem)profileMap.get(key);

			if(mProfile!= null && mProfile.length > item.getPosition()){
				item.setValue(mProfile[item.getPosition()]);
			}

		}

		if(mProfile!= null && mProfile.length > 1){
			profileLenght = mProfile[0];
		}else{
			profileLenght = 0;
		}

		//uprava zavyslosti polozek
		/*if(profileMap.containsKey("MODEL") && profileMap.get("MODEL").isValid() && profileMap.get("MODEL").getValueInteger() != 67 ){ // letadlo
			profileMap.remove("SENSOR_SENZ");
		}*/
	}
	
	///////////////// PUBLIC ////////////////////////
	
	public Boolean exits(String key)
	{
		return profileMap.keySet().contains(key);
	}
	
	/**
	 * geters pro profil
	 * 
	 * @return
	 */
	public byte[] getmProfile()
	{
		return mProfile;
	}
	
	public ProfileItem getProfileItemByName(String name)
	{
		if(profileMap.containsKey(name) && ((ProfileItem) profileMap.get(name)).isValid()){
			return (ProfileItem) profileMap.get(name);
		}
		return null;
	}
	
	public HashMap<String, ProfileItem> getProfileItems()
	{
		return profileMap;
	}
	
	/**
	 * je profil validni?
	 * 
	 * @return
	 */
	public Boolean isValid(){
		Log.d(TAG, "kontroluji delku profilu");
		if(profileLenght == 0){
			Log.d(TAG, "delka profilu 0");
			return false;
		}
		
		if(mProfile.length < profileLenght){
			Log.d(TAG, "profil je moc kratky");
			return false;
		}
		
		int id_lo = profileMap.get("CHECKSUM_LO").positionInConfig;
		int id_hi = profileMap.get("CHECKSUM_HI").positionInConfig;
		int checksum = (mProfile[id_hi] & 0xff) << 8 | (mProfile[id_lo] & 0xff);

		if (getCheckSum() != checksum) {
			Log.d(TAG, "Invalid checksum !");
			return false;
		}

        boolean isValid = true;
        this.profileErrors.clear();
		Iterator<String> iteration = profileMap.keySet().iterator();

        while(iteration.hasNext()) {
			String key=(String)iteration.next();
			ProfileItem item = (ProfileItem)profileMap.get(key);

			if(!item.isValid()){
				Log.d(TAG, "polozka " + key + " neni validni s hodnotou " + item.getValueString());
                this.profileErrors.add("polozka " + key + " neni validni s hodnotou " + item.getValueString());
                isValid = false;
			}
		}

		return isValid;
	}
	
	/**
	 * spocita a vrati checksum ze zakladniho profilu, pri chybe vraci -1
	 * 
	 * @return
	 */
	
	public int getCheckSum(){
		if(mProfile != null){
			int id_lo = profileMap.get("CHECKSUM_LO").positionInConfig;
			int id_hi = profileMap.get("CHECKSUM_HI").positionInConfig;
			
			int bytes = mProfile.length-1;

			int sum1 = 0xff;
			int sum2 = 0xff;    
			int i = 1;

			while (bytes != 0) {
			  	int tlen = bytes > 20 ? 20 : bytes;
				
			  	bytes -= tlen;
					
			    do {
			     	int d = 0;

			       	if (i != id_lo && i != id_hi)
			       		d = mProfile[i ++] & 0xff;
			       	else
			       		i ++;

			       	sum2 += sum1 += d;
			    } while ((-- tlen) != 0);
			       
			    sum1 = (sum1 & 0xff) + (sum1 >> 8);
			    sum2 = (sum2 & 0xff) + (sum2 >> 8);
			}

			sum1 = (sum1 & 0xff) + (sum1 >> 8);
			sum2 = (sum2 & 0xff) + (sum2 >> 8);

			return (sum2 << 8 | sum1);
		}
		
		return -1;
	}
	
	/**
	 * spocita a vrati checsum aktualniho profilu, pri chybe vraci -1
	 * 
	 * @return
	 */
	public int getCheckSumFromKnowItem(){
		if(mProfile != null){		
			int id_lo = profileMap.get("CHECKSUM_LO").positionInConfig;
			int id_hi = profileMap.get("CHECKSUM_HI").positionInConfig;
			
			int bytes = profileMap.size()-1;

			int sum1 = 0xff;
			int sum2 = 0xff;    
			int i = 1;

			Iterator<ProfileItem> it = profileMap.values().iterator();
			
			while (bytes != 0) {
			  	int tlen = bytes > 20 ? 20 : bytes;
				
			  	bytes -= tlen;
					
			    do {
			     	int d = 0;

			       	if (i != id_lo && i != id_hi) {
			       		ProfileItem item = it.next();
			       		d = item.getValueInteger() & 0xff;
			       	} else
			       		it.next();
			       		
			       	i ++;

			       	sum2 += sum1 += d;
			    } while ((-- tlen) != 0);
			       
			    sum1 = (sum1 & 0xff) + (sum1 >> 8);
			    sum2 = (sum2 & 0xff) + (sum2 >> 8);
			}

			sum1 = (sum1 & 0xff) + (sum1 >> 8);
			sum2 = (sum2 & 0xff) + (sum2 >> 8);

			return (sum2 << 8 | sum1);
		}
		
		return -1;
	}

    /**
     * po metode isValid vrati chyby profilu
     *
     * @return
     */
    public ArrayList<String> getErrors(){
        return this.profileErrors;
    }

	///////////////// PRIVATE ////////////////////////
	
	/**
	 * trida pro ulozeni jedne hodnoty konfigurace
	 * 
	 * @author error414
	 *
	 */
	public class ProfileItem{
		private Integer positionInConfig;
		private Byte value;
		private Integer min;
		private Integer max;
		private String sendCode;
		
		public ProfileItem(Integer positionInConfig, Integer min, Integer max, String sendCode)
		{
			this.positionInConfig = positionInConfig;
			this.sendCode = sendCode;
			this.min = min;
			this.max = max;
		}
				
		public ProfileItem(Integer positionInConfig, String min, String max, String sendCode)
		{
			this.positionInConfig = positionInConfig;
			this.sendCode = sendCode;
			this.min = ByteOperation.byteArrayToInt(min.getBytes());
			this.max = ByteOperation.byteArrayToInt(max.getBytes());
		}
		
		/**
		 * pozice v profilu se zacatecnim bytem ktery urcuje delku
		 * 
		 * @return
		 */
		public Integer getPosition()
		{
			return positionInConfig;
		}
		
		/**
		 * pozice v profilu bez zacatecniho bytu ktery urcuje delku
		 * 
		 * @return
		 */
		public Integer getPositionWithoutFirstByte()
		{
			return positionInConfig - 1;
		}
		
		/**
		 * prikaz pro zaslani do jenotky
		 * 
		 * @return
		 */
		public String getCommand()
		{
			return sendCode;
		}
		
		/**
		 * nastavime hodnotu
		 * 
		 * @param value
		 */
		public void setValue(Byte value)
		{
			this.value = value;
		}
		
		/**
		 * nastavime hodnotu
		 * 
		 * @param value
		 */
		public void setValue(Integer value)
		{
			this.value = ByteOperation.intToByte(value);
		}
		
		
		/**
		 * nastavime hodnotu
		 * 
		 * @param value
		 */
		public void setValueFromSpinner(Integer value)
		{
			this.value = ByteOperation.intToByte(value+ this.min);
		}
		
		/**
		 * hodnota pro checkBox, 
		 * 
		 * @return
		 * @throws IndexOutOfException
		 */
		public void setValueFromCheckBox(Boolean checked){
			if(checked == true){
				value = 49; // "1"
			}else{
				value = 48; // "0"
			}
		}
		
		/**
		 * vratime hodnotu
		 * 
		 */
		public Byte getValueByte()
		{
			return this.value;
		}
		
		/**
		 * hodnota pro spinner, parametrem je maximalni hodnota spineru pro pripad ze se maximalni hodnota spineru meni
		 * 
		 * @param max
		 * @return
		 * @throws IndexOutOfException
		 */
		public Integer getValueForSpinner(int max) throws IndexOutOfException{
			if(getValueInteger() - this.min > max - 1){
				throw new IndexOutOfException();
			}
			return getValueInteger() - this.min; // this.min = 65 je znak A od toho se odrazime
		}
		
		/**
		 * hodnota pro checkBox, 
		 * 
		 * @return
		 * @throws IndexOutOfException
		 */
		public Boolean getValueForCheckBox(){
			return (getValueInteger() - this.min) == 1; //this.min = 30 je znak 0 (nula) od toho se odrazime
		}
		
		/**
		 * vratime hodnotu
		 * 
		 */
		public byte[] getValueBytesArray()
		{
			byte[] ret = new byte[1];	
			ret[0] = value;
			return ret;
		}
		
		/**
		 * vratime hodnotu
		 * 
		 */
		public Integer getValueInteger()
		{
			return ByteOperation.byteToUnsignedInt(this.value);
		}
		
		/**
		 * vratime hodnotu
		 * 
		 */
		public String getValueString()
		{
			return String.valueOf(getValueInteger());
		}
		
		/**
		 * je hodnota validni validni?
		 * 
		 * @return
		 */
		public Boolean isValid()
		{
			//getValueInteger vraci jen cela cisla, pokud ale mame profil ktery povoluje i zaporna cisla musime vzit puvodni hodnotu ma kontrolu
			if(value != null){
				if(min >= 0){
					return (getValueInteger() >= min) && (getValueInteger() <= max); 
				}else{
					return (value >= min) && (value <= max);
				}
			}
			return true;
		}
		
		/**
		 * vratime hodnotu reprezentujici minimum pro dany item
		 * 
		 */
		public Integer getMinimum()
		{
			return this.min;
		}
		
		/**
		 * vratime hodnotu reprezentujici maximum pro dany item
		 * 
		 */
		public Integer getMaximum()
		{
			return this.max;
		}

	}
	
	
	///////////////// STATICKE METODY ////////////////////////
	/**
	 * nahrani profilu ze souboru
	 * 
	 * @param file
	 * @return
	 */
	@SuppressWarnings("resource")
	final static public byte[] loadProfileFromFile(File file) throws FileNotFoundException, IOException 
	{
		InputStream is = new FileInputStream(file);

	    // Get the size of the file
	    long length = file.length();

	    // Create the byte array to hold the data
	    byte[] bytes = new byte[(int)length];

	    // Read in the bytes
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }

	    // Ensure all the bytes have been read in
	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file "+file.getName());
	    }

	    // Close the input stream and return bytes
	    is.close();
	    return bytes;

	}
	
	/**
	 * ulozeni profilu do souboru
	 * 
	 * @param file
	 * @param data
	 * @throws IOException
	 */
	final static public void saveProfileToFile(File file, byte[] data) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(file);
	    fos.write(data);
	    fos.close();
	}
}
