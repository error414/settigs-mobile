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

package com.lib;

import com.exception.ProfileNotValidException;
import com.helpers.DstabiProfile;

import java.util.ArrayList;
import java.util.HashMap;


public class ChangeInProfile
{
	static private ChangeInProfile instance;

	protected DstabiProfile originalProfile;

	private ChangeInProfile(){}

	/**
	 * pristupovat jen pres singleton
	 *
	 * @return
	 */
	static public ChangeInProfile getInstance(){
		if(instance == null){
			instance = new ChangeInProfile();
		}
		return instance;
	}

	public void setOriginalProfile(DstabiProfile originalProfile)
	{
		this.originalProfile = originalProfile;
	}


	public ArrayList<DiffItem> getDiff(DstabiProfile changedProfile) throws ProfileNotValidException
	{
		ArrayList<DiffItem> resultDiff = new ArrayList<DiffItem>();
		if(changedProfile.isValid() && originalProfile.isValid()){
			for(String itemName : changedProfile.getProfileItems().keySet()){
				if(changedProfile.getProfileItemByName(itemName).getValueInteger() != originalProfile.getProfileItemByName(itemName).getValueInteger()){
					resultDiff.add(new DiffItem(originalProfile.getProfileItemByName(itemName), changedProfile.getProfileItemByName(itemName), itemName));
				}
			}
			return resultDiff;
		}

		throw new ProfileNotValidException();
	}

	/**
	 * polozka diffu
	 */
	public class DiffItem{
		private DstabiProfile.ProfileItem originalValue;
		private DstabiProfile.ProfileItem changedValue;
		private String label;

		DiffItem(DstabiProfile.ProfileItem originalValue, DstabiProfile.ProfileItem changedValue, String label)
		{
			this.originalValue = originalValue;
			this.changedValue = changedValue;
			this.label = label;
		}

		public DstabiProfile.ProfileItem getOriginalValue()
		{
			return originalValue;
		}

		public DstabiProfile.ProfileItem getChangedValue()
		{
			return changedValue;
		}

		public String getLabel()
		{
			return label;
		}
	}


}


