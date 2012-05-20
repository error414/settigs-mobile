package com.helpers;

import java.util.ArrayList;
import java.util.HashMap;

import com.settings.BaseActivity;
import com.settings.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * trida pro naplneni listview hodnotama
 * 
 * @author error414
 *
 */
public class MenuListAdapter extends BaseAdapter {

	/**
	 * 
	 */
	private LayoutInflater inflater;
	
	/**
	 * aktivata kde zobrazujeme listview
	 */
	private Activity activity;
	
	/**
	 * pole [title ID resource, ID obrazku (resource)]
	 */
	private ArrayList<HashMap<Integer, Integer>> data;

	public MenuListAdapter(Activity a, ArrayList<HashMap<Integer, Integer>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.menu_list_item, null);
 
        TextView title = (TextView)vi.findViewById(R.id.title); // title
        ImageView ico_image=(ImageView)vi.findViewById(R.id.list_image); // thumb image
 
        HashMap<Integer, Integer> row = new HashMap<Integer, Integer>();
        row = data.get(position);
 
        // Setting all values in listview
        title.setText(row.get(BaseActivity.TITLE_FOR_MENU));
        ico_image.setImageResource(row.get(BaseActivity.ICO_RESOURCE_ID));
        return vi;
    }

}
