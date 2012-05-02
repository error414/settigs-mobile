package com.settings;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

public class ServosLimitActivity extends Activity{
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.servos_limit);
        
        setTitle(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.servos_button_text), " \u2192 " , getString(R.string.limit)));

    }
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
}
