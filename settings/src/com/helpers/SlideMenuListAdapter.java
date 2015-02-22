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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.spirit.R;

/**
 * trida pro naplneni listview hodnotama
 * 
 * @author error414
 *
 */
@SuppressLint("UseSparseArrays")
public class SlideMenuListAdapter extends BaseAdapter {

    /**
     *
     */
    protected LayoutInflater inflater;

    /**
     * aktivita kde zobrazujeme listview
     */
    protected Activity activity;

    /**
     */
    protected String[] data;

    /**
     * pozice ktera je vybrana
     */
    protected int activePosition = -1;

    /**
     * disablovat cely vyber
     */
    protected boolean disabledAll = true;

	public SlideMenuListAdapter(Activity a, String[] d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.slide_menu_list_item, null);
 
        TextView title      = (TextView)vi.findViewById(R.id.title); // title
        RelativeLayout item = (RelativeLayout)vi.findViewById(R.id.item); // item
        if(!isEnabled(position)){
            title.setTextColor(activity.getResources().getColor(R.color.dashed_dark));
        }else if(!disabledAll){
            title.setTextColor(activity.getResources().getColor(R.color.text_color));
        }

        if(!disabledAll && activePosition == position){
            item.setBackgroundColor(activity.getResources().getColor(R.color.dashed_dark));
            title.setTextColor(activity.getResources().getColor(R.color.text_color_white));
        }else if(!disabledAll){
            item.setBackgroundResource(R.drawable.slide_menu_list_selector);
            title.setTextColor(activity.getResources().getColor(R.color.text_color));
        }else{
            title.setTextColor(activity.getResources().getColor(R.color.dashed_dark));
            item.setBackgroundColor(activity.getResources().getColor(R.color.text_color_white));
        }



        // Setting all values in listview

        title.setText(((String)getItem(position)));
        return vi;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return !disabledAll && activePosition != position;
    }

    public int getActivePosition() {
        return activePosition;
    }

    public void setActivePosition(int activePosition) {
        this.activePosition = activePosition;
    }

    public boolean isDisabledAll() {
        return disabledAll;
    }

    public void setDisabledAll(boolean disabledAll) {
        this.disabledAll = disabledAll;
    }
}
