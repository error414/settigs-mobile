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
import android.widget.TextView;

import com.spirit.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * trida pro naplneni listview hodnotama
 *
 * @author error414
 *
 */
@SuppressLint("UseSparseArrays")
public class DiffListAdapter extends BaseAdapter
{

	public static Integer NAME      = 1;
	public static Integer FROM      = 2;
	public static Integer TO        = 3;

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
	protected ArrayList<HashMap<Integer, String>> data;

	/**
	 *
	 * @param a
	 * @param d
	 */
	public DiffListAdapter(Activity a, ArrayList<HashMap<Integer, String>> d) {
		activity = a;
		data = d;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.diff_list_item, null);
 
        TextView name   = (TextView)vi.findViewById(R.id.name); // title
        TextView from   = (TextView)vi.findViewById(R.id.from);
        TextView to     = (TextView)vi.findViewById(R.id.to);

        HashMap<Integer, String> row = new HashMap<Integer, String>();
        row = data.get(position);
 
        // Setting all values in listview
		name.setText(row.get(DiffListAdapter.NAME));
		from.setText(row.get(DiffListAdapter.FROM));
		to.setText(row.get(DiffListAdapter.TO));
        return vi;
    }

	public void setData(ArrayList<HashMap<Integer, String>> data) {
		this.data = data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
