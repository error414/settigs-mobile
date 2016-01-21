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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spirit.R;
import com.spirit.heli.diagnostic.LogActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * trida pro naplneni listview hodnotama
 * 
 * @author error414
 *
 */
@SuppressLint("UseSparseArrays")
public class LogListAdapter extends BaseAdapter {

    /**
     *
     */
    protected LayoutInflater inflater;

    /**
     * aktivata kde zobrazujeme listview
     */
    protected Activity activity;

    /**
     */
    protected HashMap<Integer, ArrayList<HashMap<Integer, Integer>>> data;

	public LogListAdapter(Activity a, HashMap<Integer, ArrayList<HashMap<Integer, Integer>>> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

    public void setData(HashMap<Integer, ArrayList<HashMap<Integer, Integer>>> data) {
        this.data = data;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        ArrayList<HashMap<Integer, Integer>> row = data.get(position);
        View vi = inflater.inflate(R.layout.log_list_item, null);

        LinearLayout layout = (LinearLayout)vi.findViewById(R.id.log_list_item_layount);

        for(int i = 0; i < row.size(); i++){
            View subRowView = inflater.inflate(R.layout.log_list_item_sub_row, (ViewGroup) vi, false);
            TextView title 		= (TextView)subRowView.findViewById(R.id.title); // title
            ImageView ico_image	= (ImageView)subRowView.findViewById(R.id.list_image); // thumb image


            // Setting all values in listview
            title.setText(row.get(i).get(LogActivity.TITLE_FOR_LOG));
            ico_image.setImageResource(row.get(i).get(LogActivity.ICO_RESOURCE_LOG));

            layout.addView(subRowView);
        }

        TextView time 		= (TextView)vi.findViewById(R.id.time); // time
        time.setText(getTimeByPosition(position));

        return vi;
    }

    @SuppressLint("DefaultLocale")
    protected String getTimeByPosition(int pos) {
        int sec = (pos) * 10;
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }
	
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
}
