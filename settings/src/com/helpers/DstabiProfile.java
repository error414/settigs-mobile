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

import android.util.Log;

import com.exception.IndexOutOfException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author error414
 *
 * trida pro praci s profilem
 */
public class DstabiProfile {

	final static String TAG = "DstabiProfile";
	
	private int profileLenght;

	private byte[] mProfile;

	final static public int CHECK_ALL = 0;
	final static public int DONT_CHECK_CHECKSUM = 1;
	
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
		profileMap.put("MAJOR", 	new ProfileItem(1, 0, 255, 		null,	true)); // 'major', INT,
		profileMap.put("MINOR2", 	new ProfileItem(2, 0, 255, 		null,	true)); // 'minor', INT

		profileMap.put("POSITION", 	new ProfileItem(3, "A", "C", 	"P",	true)); // position_text, ENUM, position_values
		profileMap.put("BANKS", 		new ProfileItem(4,  0, 	2, 		"M", 	true));
        //profileMap.put("SIGNAL_PROCESSING",	new ProfileItem(4, "0", "1", "7")); // rozsirene zpracovani signalu

		profileMap.put("RECEIVER",	new ProfileItem(5, "A", "F", 	"R",	true));
		profileMap.put("MIX",	 	new ProfileItem(6, "A", "D", 	"C",	true));

		profileMap.put("CYCLIC_TYPE",	new ProfileItem(7, "A", "A", 	"ST",	true));
		profileMap.put("CYCLIC_FREQ",	new ProfileItem(8, "A", "F", 	"SF",	true));
		profileMap.put("RUDDER_TYPE",	new ProfileItem(9, "A", "C", 	"St",	true));
		profileMap.put("RUDDER_FREQ",	new ProfileItem(10, "A", "G", 	"Sf",	true));

		profileMap.put("SUBTRIM_AIL",	new ProfileItem(16, 0, 254, 	"SA",	true));
		profileMap.put("SUBTRIM_ELE",	new ProfileItem(17, 0, 254, 	"SE",	true));
		profileMap.put("SUBTRIM_PIT",	new ProfileItem(18, 0, 254, 	"SP",	true));
		profileMap.put("SUBTRIM_RUD",	new ProfileItem(12, 0, 254, 	"Se",	true));

		profileMap.put("RANGE_AIL",		new ProfileItem(11, 32, 255, 	"Sa",	true));	// cyclic ring
		profileMap.put("RANGE_PIT",		new ProfileItem(13, 32, 255, 	"Sp",	false));	// rozsah kolektivu

		profileMap.put("RUDDER_MIN",	new ProfileItem(14, 32, 255, 	"Sm",	true));
		profileMap.put("RUDDER_MAX",	new ProfileItem(15, 32, 255, 	"SM",	true));

		profileMap.put("SENSOR_SENX",	new ProfileItem(19, 0, 80, "x",	false)); 		// zisk cyklikt
		profileMap.put("GEOMETRY",		new ProfileItem(20, 64, 250, "8",	true));		// geometrie hlavy - 6
		profileMap.put("SENSOR_SENZ",	new ProfileItem(21, 50, 100, "z",	false)); 	// nasobic

		profileMap.put("SENSOR_REVX",	new ProfileItem(22, "0", "1", "X",	true));
		profileMap.put("SENSOR_REVY",	new ProfileItem(23, "0", "1", "Y",	true));
		profileMap.put("SENSOR_REVZ",	new ProfileItem(24, "0", "1", "Z",	true));
        //
		profileMap.put("RATE_PITCH",	new ProfileItem(25, 5, 16, 	"a",	false));		// rychlost rotace cykliky
		profileMap.put("CYCLIC_FF",		new ProfileItem(26, 1, 12, 	"b",	false));		// pocatecni reakce cykliky
		profileMap.put("RATE_YAW",		new ProfileItem(27, 5, 20, 	"c",	false));		// rychlost rotace vrtulky

		profileMap.put("PITCHUP",	    new ProfileItem(28, 0, 4, "r",	false)); 	// kompenzace zpinani vyskovky
		profileMap.put("STICK_DB",		new ProfileItem(29, 4, 30, "s",	false));  // mrtva zona knyplu
		profileMap.put("RUDDER_STOP",	new ProfileItem(30, 3, 10, "p",	false)); 		// dynamika vrtulky
		profileMap.put("ALT_FUNCTION",	new ProfileItem(31, "A", "E", "f",	false)); 	// stabi mode
        profileMap.put("CYCLIC_REVERSE",	new ProfileItem(32, "A", "D", 	"v",	true));
		profileMap.put("RUDDER_REVOMIX",new ProfileItem(33, 118, 138, "m",	false)); //

		profileMap.put("STABI_CTRLDIR", new ProfileItem(34, 1, 5, "0",	false));  // Mira zmeny smeru
		profileMap.put("STABI_COL",     new ProfileItem(35, 117, 137, "1",	false)); 		// kolektiv zachranneho rezimu
		//profileMap.put("STABI_ROLL",    new ProfileItem(36, 63, 191, "2")); // stabi, kompenzace pro kridelka
		profileMap.put("STABI_STICK",   new ProfileItem(37, 0, 16, "3",	false)); // priorita knyplu

		profileMap.put("PIROUETTE_CONST",	new ProfileItem(38, 130, 250, "H",	false)); // konzistence piruet

		profileMap.put("CHECKSUM_LO",	new ProfileItem(36, 0, 255, null,	true)); 	// checksum pro kontrolu dat
		profileMap.put("CHECKSUM_HI",	new ProfileItem(39, 0, 255, null,	true)); 	// checksum pro kontrolu dat
		
		profileMap.put("CYCLIC_PHASE",	new ProfileItem(40, -90, 90, "5",	true)); // virtualni pootoceni cykliky

        profileMap.put("SIGNAL_PROCESSING",		new ProfileItem(41, 0, 1, "6",	true)); // rozisrene zpracovani siganlu

		profileMap.put("PIRO_OPT",		new ProfileItem(42, "0", "1", "o",	true));
		profileMap.put("E_FILTER",		new ProfileItem(43, 0, 4, "4",	false)); 		// kompenzace zpinani vyskovky

		profileMap.put("RUDDER_DELAY",	new ProfileItem(44, 0, 30, "9",	false)); // zpozdeni vrtulky

		profileMap.put("FLIGHT_STYLE",	new ProfileItem(45, 0, 7, "l",	false));		// letovy projev

		//profileMap.put("STABI_PITCH",new ProfileItem(46, 63, 191, "q")); // stabi, kompenzace pro vyskovku
		profileMap.put("FB_MODE",		new ProfileItem(46, "0", "1", "i",	true)); // flybar mechanic

		profileMap.put("TRAVEL_UAIL",	new ProfileItem(47, 63, 191, "QA",	true));
		profileMap.put("TRAVEL_UELE",	new ProfileItem(48, 63, 191, "QE",	true));
		profileMap.put("TRAVEL_UPIT",	new ProfileItem(49, 63, 191, "QP",	true));
		profileMap.put("TRAVEL_DAIL",	new ProfileItem(50, 63, 191, "Qa",	true));
		profileMap.put("TRAVEL_DELE",	new ProfileItem(51, 63, 191, "Qe",	true));
		profileMap.put("TRAVEL_DPIT",	new ProfileItem(52, 63, 191, "Qp",	true));
		
		//prirazeni kanalu
		profileMap.put("CHANNELS_THT",	new ProfileItem(53, 0, 7, "Et",	true));
		profileMap.put("CHANNELS_AIL",	new ProfileItem(54, 0, 7, "Ea",	true));
		profileMap.put("CHANNELS_ELE",	new ProfileItem(55, 0, 7, "Ee",	true));
		profileMap.put("CHANNELS_RUD",	new ProfileItem(56, 0, 7, "Er",	true));
		profileMap.put("CHANNELS_GAIN",	new ProfileItem(57, 0, 7, "Eg",	true));
		profileMap.put("CHANNELS_PITH",	new ProfileItem(58, 0, 7, "Ep",	true));
		profileMap.put("CHANNELS_BANK",	new ProfileItem(59, 0, 7, "Eb",	true));
		
		profileMap.put("SENSOR_GYROGAIN",	new ProfileItem(60, 0, 200, "7",	false));

        profileMap.put("GOVERNOR_MODE",	        new ProfileItem(61, 0, 4, "2",	true));
        profileMap.put("GOVERNOR_PGAIN",	    new ProfileItem(62, 1, 10, "j",	false)); // P Gain
        profileMap.put("MINOR1", 	            new ProfileItem(63, 0, 255, null,	true)); // 'minor', INT
        profileMap.put("PITCH_PUMP",	        new ProfileItem(64, 0, 4, "n",	false)); // pitch pump

        profileMap.put("GOVERNOR_DIVIDER",	    new ProfileItem(65, 1, 8, "u",	true));
        profileMap.put("GOVERNOR_RATIO",	    new ProfileItem(66, 20, 254, "t",	true));
        profileMap.put("GOVERNOR_THR_REVERSE",	new ProfileItem(67, "0", "1", "w",	true));
        profileMap.put("GOVERNOR_THR_MIN",	    new ProfileItem(68, 50, 150, "k",	true));
        profileMap.put("GOVERNOR_THR_MAX",	    new ProfileItem(69, 50, 150, "K",	true));
        profileMap.put("GOVERNOR_RPM_MAX",	    new ProfileItem(70, 0, 250, "W",	true));
        profileMap.put("GOVERNOR_IGAIN",	    new ProfileItem(71, 1, 6, "y",	false)); // mira drzeni otacek

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

        //////////////////////////////////////////////////////////////////
		//oprava hodnot polozek ///
        this.profileErrors.clear();
        Iterator<String> iterationProfile = profileMap.keySet().iterator();

        while(iterationProfile.hasNext()) {
            String key=(String)iterationProfile.next();
            ProfileItem item = (ProfileItem)profileMap.get(key);

            if(!item.isValid()){
                Log.d(TAG, "polozka " + key + " neni validni s hodnotou " + item.getValueString() + " byla opravena na " + String.valueOf(item.getMinimum()));
                this.profileErrors.add("polozka " + key + " neni validni s hodnotou " + item.getValueString() + " byla opravena na " + String.valueOf(item.getMinimum()));
                item.setValue(item.getMinimum());
            }
        }
        ///////////////////////////////////////////////////////////////////

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
		return isValid(CHECK_ALL);
	}
	
	/**
	 * je profil validni?
	 * 
	 * @return
	 */
	public Boolean isValid(int mode){
		if(profileLenght == 0){
			Log.d(TAG, "delka profilu 0");
			return false;
		}
		
		if(mProfile.length < profileLenght){
			Log.d(TAG, "profil je moc kratky");
			return false;
		}
		
		if(mode != DONT_CHECK_CHECKSUM){
			int id_lo = profileMap.get("CHECKSUM_LO").positionInConfig;
			int id_hi = profileMap.get("CHECKSUM_HI").positionInConfig;
			int checksum = (mProfile[id_hi] & 0xff) << 8 | (mProfile[id_lo] & 0xff);
			if (getCheckSum() != checksum) {
				Log.d(TAG, "Invalid checksum !");
				return false;
			}
		}
		
       return true;
	}

    /**
     *
     * @return
     */
    public String getFormatedVersion()
    {
        String buffer = getProfileItemByName("MAJOR").getValueString() + "." + getProfileItemByName("MINOR1").getValueString();

        int minor2Int = getProfileItemByName("MINOR2").getValueInteger();

        if(minor2Int < 128){
            buffer = buffer + "." + String.valueOf(minor2Int);
        }else if(minor2Int < 220){
            buffer = buffer + "-beta" + String.valueOf(minor2Int - 128);
        }else {
            buffer = buffer + "-rc" + String.valueOf(minor2Int - 220);
        }

        return buffer;
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

			       	if (i != id_lo && i != id_hi){
			       		d = mProfile[i ++] & 0xff;
			       	}else{
			       		i ++;
			       	}

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
			
			int bytes = profileMap.size()-1;

			int sum1 = 0xff;
			int sum2 = 0xff;    

			Iterator<ProfileItem> it = profileMap.values().iterator();
			
			while (bytes != 0) {
			  	int tlen = bytes > 20 ? 20 : bytes;
				
			  	bytes -= tlen;
					
			    do {
			     	int d = 0;
			     	
			     	ProfileItem item = it.next();
			       	if (item.getCommand() != null) {
			       		d = item.getValueInteger() & 0xff;
			       	}
			       	
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
		private byte[] value = new byte[2];
		private Integer min;
		private Integer max;
		private String sendCode;
		private boolean deactiveInBasicMode;
		
		

		public ProfileItem(Integer positionInConfig, Integer min, Integer max, String sendCode, boolean deactiveInBasicMode)
		{
			this.positionInConfig = positionInConfig;
			this.sendCode = sendCode;
			this.min = min;
			this.max = max;
			this.deactiveInBasicMode = deactiveInBasicMode;
		}
				
		public ProfileItem(Integer positionInConfig, String min, String max, String sendCode, boolean deactiveInBasicMode)
		{
			this.positionInConfig = positionInConfig;
			this.sendCode = sendCode;
			this.min = ByteOperation.byte2ArrayToSigInt(min.getBytes());
			this.max = ByteOperation.byte2ArrayToSigInt(max.getBytes());
			this.deactiveInBasicMode = deactiveInBasicMode;
		}
		
		public boolean isDeactiveInBasicMode() {
			return deactiveInBasicMode;
		}
		
		public void setDeactiveInBasicMode(boolean deactiveInBasicMode) {
			this.deactiveInBasicMode = deactiveInBasicMode;
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
		public void setValue(byte value)
		{
			this.value = new byte[1];
			this.value[0] = value;
		}

        /**
         * nastavime hodnotu
         *
         * @param value
         */
        public void setValue(byte[] value)
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
			this.value = ByteOperation.intToByteArray(value);
		}
		
		
		/**
		 * nastavime hodnotu
		 * 
		 * @param value
		 */
		public void setValueFromSpinner(Integer value)
		{
			this.value = ByteOperation.intToByteArray(value + this.min);
		}

		/**
		 * hodnota pro checkBox, 
		 * 
		 * @return
		 * @throws IndexOutOfException
		 */
		public void setValueFromCheckBox(Boolean checked){
			if(checked == true){
				value = ByteOperation.intToByteArray(this.getMaximum()); // "1"
			}else{
				value = ByteOperation.intToByteArray(this.getMinimum()); // "0"
			}
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
			return value;
		}
		
		/**
		 * vratime hodnotu
		 * 
		 */
		public Integer getValueInteger()
		{
			if(this.value == null){
				return 0;
			}

            if(min >= 0){
                return ByteOperation.byteArrayToUnsignedInt(this.value);
            }else{
                return ByteOperation.byte2ArrayToSigInt(this.value);
            }

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
					return (ByteOperation.byte2ArrayToSigInt(value) >= min) && (ByteOperation.byte2ArrayToSigInt(value) <= max);
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
