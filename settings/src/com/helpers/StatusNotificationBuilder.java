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


import com.settings.R;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


/**
 * vytvoreni notifikacni listy na spodu displeje
 * 
 * @author error414
 *
 */
public class StatusNotificationBuilder {
	
	private Context mContext;
	private Window  mWindow;
	private View mDecor;
	
	/**
	 * text ktery je zobrazen v pruhu
	 */
	private String  mText;
	
	/**
	 * je pruh zobrazen?
	 */
	private Boolean isShowing = false;
	
	/**
	 * construktor
	 * 
	 * @param context
	 * @param window
	 */
	public StatusNotificationBuilder(Context context, Window window) {
		this.mContext = context;
		this.mWindow = window;
	}

	
	/**
	 * nastavi text
	 * 
	 * @param text
	 */
	public StatusNotificationBuilder setText(String text) {
		mText = text;
		if(mDecor != null){
			((TextView)mDecor.findViewById(R.id.notify_text)).setText(mText);
		}
		return this;
	}
	
	/**
	 * nastavi text
	 * 
	 * @param text
	 */
	public StatusNotificationBuilder setText(CharSequence text) {
		return setText(text.toString());
	}
	
	/**
	 * nastavi text z ID resource
	 * 
	 * @param text
	 */
	public StatusNotificationBuilder setText(int textId) {
		return setText(mContext.getText(textId));
	}

	/**
	 * je pruh zobrazen
	 * 
	 * @return
	 */
	public boolean isShowing() {
		return isShowing;
	}

	/**
	 * zobrazit pruh
	 */
	public void show() {
		if(!isShowing()){
			isShowing = true;
			if (mDecor != null) {
				((TextView)mDecor.findViewById(R.id.notify_text)).setText(mText);
				mDecor.setVisibility(View.VISIBLE);
				return;
			}
			
			LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        mDecor = inflate.inflate(R.layout.notify, null);
			
			
			WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
	        mParams.gravity = Gravity.BOTTOM;
	        
	        View l1 = mWindow.getDecorView().findViewById(android.R.id.content);
	        ((ViewGroup) l1).addView(mDecor, mParams);
	        
	        TextView text = (TextView) mWindow.getDecorView().findViewById(R.id.notify_text);
	        text.setText(mText);
		}
	}
	
	/**
	 * skryt pruh
	 */
	public void hide() {
		if(mDecor != null){
			mDecor.setVisibility(View.GONE);
			isShowing = false;
		}
	}

}
