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
package com.customWidget.picker;


import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.spirit.R;

public class ProgresEx extends LinearLayout implements OnClickListener,  OnLongClickListener {
	@SuppressWarnings("unused")
	private static final String TAG = "ProgresEx";
	
	static private int DEFAULT_MIN = 0;
	static private int DEFAULT_MAX = 100;
	
	
	//Prvky formulare
	private final TextView  mObjTitle;
    private final TextView mObjLeft;
    private final TextView mObjRight;
    private final TextView  mObjCurrent;
    private final TextView  mObjOriginal;
	private final EditText  mObjProgresValue;
	private final RelativeLayout childLayout;
	private final RelativeLayout mainLayout;
	
	private final ProgressBar  mObjProgres;

    private int mCurrent = DEFAULT_MIN;
    private String mCurrentString = null;
    private int mMin = DEFAULT_MIN;
    private int mMax = DEFAULT_MAX;

	private int mRangeMin = DEFAULT_MIN;
	private int mRangeMax = DEFAULT_MAX;
	private String mTitle = "";

	private int mOffset = 0;

    private int originalValue = DEFAULT_MIN;

	private ProgresExButton mIncrementButton;
	private ProgresExButton mDecrementButton;
	
	private OnChangedListener mListener;

    private OnLongClickListener mListenerLongClick;

	private boolean mIncrement;
    private boolean mDecrement;

    private int stepLongPress = 1;
    private int stepPress = 1;
    private boolean inverted = false;

    private boolean enabledDefaultValue = true;

    private ProgresExViewTranslateInterface translate;
    private ProgresExViewTranslateInterface translateOriginalValue;
    
    private final Handler mHandler;
    private final Runnable mRunnable = new Runnable() {
        public void run() {
            if (mIncrement) {
                changeCurrent(mCurrent + stepLongPress);
                mHandler.postDelayed(this, 100);
            } else if (mDecrement) {
                changeCurrent(mCurrent - stepLongPress);
                mHandler.postDelayed(this, 100);
            }
        }
    };

	public interface OnChangedListener {
		void onChanged(ProgresEx picker, int newVal);
	}

    public interface setOnLongClickListener {
        void OnLongClickListener(ProgresEx picker);
    }

	public ProgresEx(Context context) {
	    this(context, null);
	}
	
	public ProgresEx(Context context, AttributeSet attrs) {
	    this(context, attrs, 0);
	}
	
	public ProgresEx(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs);
	    
	    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    inflater.inflate(R.layout.progres_ex, this, true);
	    
	    mainLayout = (RelativeLayout) findViewById(R.id.progres_main);
        mainLayout.setOnClickListener(this);
        mainLayout.setOnLongClickListener(this);

	    mObjTitle 	= (TextView) findViewById(R.id.progres_title);

        mObjLeft = (TextView) findViewById(R.id.progres_left);
        mObjRight = (TextView) findViewById(R.id.progres_right);
        mObjCurrent = (TextView) findViewById(R.id.progres_current);
        mObjOriginal = (TextView) findViewById(R.id.original_value);

        mObjProgres = (ProgressBar) findViewById(R.id.progres_bar);

	    mObjProgresValue = (EditText) findViewById(R.id.progres_value); 
	    mObjProgresValue.setFocusable(false);

	    mDecrementButton = (ProgresExButton) findViewById(R.id.progres_minus);
	    mDecrementButton.setOnClickListener(this);
	    mDecrementButton.setOnLongClickListener(this);
	    mDecrementButton.setProgresEx(this);

	    mIncrementButton = (ProgresExButton) findViewById(R.id.progres_plus);
	    mIncrementButton.setOnClickListener(this);
	    mIncrementButton.setOnLongClickListener(this);
	    mIncrementButton.setProgresEx(this);

	    childLayout = (RelativeLayout) findViewById(R.id.progres_child);
	    childLayout.setVisibility(View.GONE);

        childLayout.setOnClickListener(this);

	    mHandler = new Handler();
	}

    /**
     *
     * @param step
     */
    public void setStepLongPress(int step){
        this.stepLongPress = step;
    }

    /**
     *
     * @param step
     */
    public void setStepPress(int step){
        this.stepPress = step;
    }

    /**
     *
     * @param status
     */
    public void setEnabled(boolean status)
    {
        super.setEnabled(status);
        if(!status){
            mObjProgres.setProgressDrawable(getResources().getDrawable(R.drawable.my_disable_custom_pb));
            mainLayout.setOnClickListener(null);
            mainLayout.setBackgroundResource(R.drawable.disabled_list_selector);
            hideInput();
        }else{
            mObjProgres.setProgressDrawable(getResources().getDrawable(R.drawable.my_custom_pb));
            mainLayout.setOnClickListener(this);
            mainLayout.setBackgroundResource(R.drawable.list_selector);
        }
    }

	/**
	 * nastaveni aktialni hodnoty
	 * 
	 * @param mCurrent
	 */
	public void setCurrent(int mCurrent) {
		if(mCurrent < mMin && mCurrent > mMax){
			return;
		}
		this.mCurrent = mCurrent;
        this.mCurrentString = null;
		notifyChange();		
		updateView();
	}
	
	/**
	 * nastaveni aktialni hodnoty bez zavolani onchange
	 * 
	 * @param mCurrent
	 */
	public void setCurrentNoNotify(int mCurrent) {
		this.mCurrent = mCurrent;
        this.mCurrentString = null;
		updateView();
	}

    /**
     * nastaveni aktialni hodnoty bez zavolani onchange
     *
     * @param mCurrent
     */
    public void setCurrentNoNotify(String mCurrent) {
        this.mCurrentString = mCurrent;
        updateView();
    }
	
	/**
	 * nastaveni posluchace na zmenu hodnoty
	 * 
	 * @param listener
	 */
	public void setOnChangeListener(OnChangedListener listener) {
        mListener = listener;
    }

    /**
     * nastaveni posluchace na dlouhy klik
     *
     * @param listener
     */
    public void setOnLongClickListener(OnLongClickListener listener) {
        mListenerLongClick = listener;
    }
	
	/**
	 * zmeni se hodnota curent
	 */
	protected void notifyChange() {
        if (mListener != null) {
            mListener.onChanged(this, mCurrent);
        }
    }
	
	/**
	 * nastaveni rozsahu
	 * 
	 * @param mMin
	 * @param mMax
	 */
	public void setRange(int mMin, int mMax) {
		setRange(mMin, mMax, mMin, mMax);
	}
	
	/**
	 * nastaveni rozsahu
	 * 
	 * @param mMin
	 * @param mMax
	 */
	public void setRange(int mMin, int mMax, int mRangeMin, int mRangeMax) {
		this.mMin = mMin;
		this.mMax = mMax;
		
		this.mRangeMin = mRangeMin;
		this.mRangeMax = mRangeMax;
		
		mObjProgres.setMax(mMax - mMin);
		
		mCurrent = mMin;
        updateView();
	}
	
	/**
	 * nastaveni offsetu
	 * 
	 * @param mOffset
	 */
	public void setOffset(int mOffset) {
		this.mOffset = mOffset;
	}
	
	/**
	 * nastaveni titilku
	 * 
	 * @param mTitle
	 */
	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
		updateView();
	}
	
	/**
	 * nastaveni titilku
	 * 
	 * @param mTitle
	 */
	public void setTitle(int mTitle) {
		setTitle(getContext().getString(mTitle));
	}
	
	/**
	 * pri zmene nejakeho prvku updatujem view
	 */
	protected void updateView() {
		mObjTitle.setText(mTitle);

        if (isInverted()) {
            mObjProgres.setProgress((mMax - mMin) - (mCurrent - mMin));
        } else {
            mObjProgres.setProgress(mCurrent - mMin);
        }

        int leftValue = isInverted() ? mRangeMax : mRangeMin;
        int rightValue = isInverted() ? mRangeMin : mRangeMax;

        if (getTranslate() == null) {
            mObjLeft.setText(String.valueOf(leftValue));
            mObjRight.setText(String.valueOf(rightValue));
            mObjProgresValue.setText(String.valueOf(mCurrent + mOffset));
            mObjCurrent.setText(String.valueOf(mCurrent + mOffset));
            mObjOriginal.setText(mCurrent != originalValue && enabledDefaultValue ? String.valueOf(originalValue + mOffset) : "");
		}else{
            mObjLeft.setText(getTranslate().translateMin(leftValue));
            mObjRight.setText(getTranslate().translateMax(rightValue));
            mObjProgresValue.setText(getTranslate().translateCurrent(mCurrent + mOffset));
            mObjCurrent.setText(getTranslate().translateCurrent(mCurrent + mOffset));
            mObjOriginal.setText(mCurrent != originalValue && enabledDefaultValue ? getTranslateOriginalValue().translateCurrent(originalValue + mOffset) : "");
        }


        if(mCurrentString != null) {
            mObjCurrent.setText(mCurrentString);
        }
		
    }
	
	/**
	 * 
	 * 
	 * @param current
	 */
	protected void changeCurrent(int current) {
        // Wrap around the values if we go past the start or end
        if (current > mMax) {
            current = mMax;
        } else if (current < mMin) {
            current = mMin;
        }
        
        mCurrent = current;

        notifyChange();
        updateView();
    }

	@Override
	public void onClick(View v) {
        //validateInput(mText);
        //if (!mText.hasFocus()) mText.requestFocus();

        // now perform the increment/decrement
        if ((R.id.progres_minus == v.getId() && !isInverted()) || (R.id.progres_plus == v.getId() && isInverted())) {
            changeCurrent(mCurrent-stepPress);
        } else if ((R.id.progres_plus == v.getId() && !isInverted()) || (R.id.progres_minus == v.getId() && isInverted())) {
            changeCurrent(mCurrent+stepPress);
        }else if (R.id.progres_main == v.getId()) {
            toogleInput();
        }
    }
	
	/**
     * We start the long click here but rely on the {@link ProgresEx}
     * to inform us when the long click has ended.
     */
    public boolean onLongClick(View v) {
        if ((R.id.progres_plus == v.getId() && !isInverted()) || (R.id.progres_minus == v.getId() && isInverted())) {
            mIncrement = true;
            mHandler.post(mRunnable);
        } else if ((R.id.progres_minus == v.getId() && !isInverted()) || (R.id.progres_plus == v.getId() && isInverted())) {
            mDecrement = true;
            mHandler.post(mRunnable);
        }else if(R.id.progres_main == v.getId()){
            if(mListenerLongClick != null) {
                mListenerLongClick.onLongClick(this);
            }
        }
        return true;
    }
    
    public void cancelIncrement() {
        mIncrement = false;
    }

    public void cancelDecrement() {
        mDecrement = false;
    }
        
    /**
     * zobrazeni/skryvani zadavaciho pole
     */
    private void toogleInput(){
    	if(childLayout.getVisibility() == View.GONE){
            showInput();
    	}else{
    		hideInput();
    	}
    }
    
    public void showInput(){
    	if(childLayout.getVisibility() == View.GONE){
    		childLayout.setVisibility(View.VISIBLE);
    	}
    }
    
    public void hideInput(){
    	if(childLayout.getVisibility() == View.VISIBLE){
    		childLayout.setVisibility(View.GONE);
    	}
    }
    
    public void setVisibility(int visibility){
    	if(mainLayout != null){
    		mainLayout.setVisibility(visibility);
    	}
    }

	public ProgresExViewTranslateInterface getTranslate() {
		return translate;
	}

	public void setTranslate(ProgresExViewTranslateInterface translate) {
		this.translate = translate;
	}

    public ProgresExViewTranslateInterface getTranslateOriginalValue() {
        return (translateOriginalValue != null) ? translateOriginalValue : getTranslate();
    }

    public void setTranslateOriginalValue(ProgresExViewTranslateInterface translateOriginalValue) {
        this.translateOriginalValue = translateOriginalValue;
    }

    public int getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(int originalValue) {
        this.originalValue = originalValue;
        updateView();
    }

    public boolean isEnabledDefaultValue() {
        return enabledDefaultValue;
    }

    public void setEnabledDefaultValue(boolean enabledDefaultValue) {
        this.enabledDefaultValue = enabledDefaultValue;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
        updateView();
    }
}
