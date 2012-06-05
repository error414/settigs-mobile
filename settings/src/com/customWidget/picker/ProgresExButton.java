package com.customWidget.picker;

import com.settings.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Button;


public class ProgresExButton extends Button{
	private ProgresEx mProgresEx;
    
    public ProgresExButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ProgresExButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProgresExButton(Context context) {
        super(context);
    }
    
    public void setProgresEx(ProgresEx progresEx) {
    	mProgresEx = progresEx;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        cancelLongpressIfRequired(event);
        return super.onTouchEvent(event);
    }
    
    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        cancelLongpressIfRequired(event);
        return super.onTrackballEvent(event);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                || (keyCode == KeyEvent.KEYCODE_ENTER)) {
            cancelLongpress();
        }
        return super.onKeyUp(keyCode, event);
    }
    
    private void cancelLongpressIfRequired(MotionEvent event) {
        if ((event.getAction() == MotionEvent.ACTION_CANCEL)
                || (event.getAction() == MotionEvent.ACTION_UP)) {
            cancelLongpress();
        }
    }

    private void cancelLongpress() {
        if (R.id.progres_plus == getId()) {
        	mProgresEx.cancelIncrement();
        } else if (R.id.progres_minus == getId()) {
        	mProgresEx.cancelDecrement();
        }
    }
}
