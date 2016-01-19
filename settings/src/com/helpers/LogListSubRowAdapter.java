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

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.spirit.R;
import com.spirit.heli.diagnostic.LogActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class LogListSubRowAdapter extends BaseStandartListAdapter {

    public LogListSubRowAdapter(Activity a, ArrayList<HashMap<Integer, Integer>> d) {
        super(a, d);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;

        HashMap<Integer, Integer> row = data.get(position);
        if(convertView==null) {
            vi = inflater.inflate(R.layout.log_list_item_sub_row, null);
        }
        vi.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 80));
        vi.setOnTouchListener(new View.OnTouchListener() {

              @Override
              public boolean onTouch(View v, MotionEvent event) {
                  if (event.getAction() == MotionEvent.ACTION_MOVE) {
                      return true; // Indicates that this has been handled by you and will not be forwarded further.
                  }
                  return false;
              }
          });

        TextView title 		= (TextView)vi.findViewById(R.id.title); // title
        ImageView ico_image	= (ImageView)vi.findViewById(R.id.list_image); // thumb image


        // Setting all values in listview
        title.setText(row.get(LogActivity.TITLE_FOR_LOG));
        ico_image.setImageResource(row.get(LogActivity.ICO_RESOURCE_LOG));
        return vi;
    }
}
