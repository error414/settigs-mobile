package com.customWidget.picker;


import java.awt.font.NumericShaper;

import com.helpers.NumberOperation;
import com.settings.R;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ProgresEx extends LinearLayout implements OnClickListener,  OnLongClickListener {
	private static final String TAG = "ProgresEx";
	
	static private int DEFAULT_MIN = 0;
	static private int DEFAULT_MAX = 100;
	
	
	//Prvky formulare
	private final TextView  mObjTitle;
	private final TextView  mObjMin;
	private final TextView  mObjMax;
	private final TextView  mObjCurrent;
	private final EditText  mObjProgresValue;
	private final RelativeLayout childLayout;
	private final RelativeLayout mainLayout;
	
	private final ProgressBar  mObjProgres;
	
	private int mCurrent = 50;
	private int mMin = DEFAULT_MIN;
	private int mMax = DEFAULT_MAX;
	private boolean mFloor = false;

	private int mRangeMin = DEFAULT_MIN;
	private int mRangeMax = DEFAULT_MAX;
	private String mTitle = "";
	private boolean mShowAsPercent = false;
	
	private int mOffset = 0;
	
	private ProgresExButton mIncrementButton;
	private ProgresExButton mDecrementButton;
	
	private OnChangedListener mListener;
	
	private boolean mIncrement;
    private boolean mDecrement;
	
    private final Handler mHandler;
    private final Runnable mRunnable = new Runnable() {
        public void run() {
            if (mIncrement) {
                changeCurrent(mCurrent + 1);
                mHandler.postDelayed(this, 100);
            } else if (mDecrement) {
                changeCurrent(mCurrent - 1);
                mHandler.postDelayed(this, 100);
            }
        }
    };
    
	public interface OnChangedListener {
		void onChanged(ProgresEx picker, int newVal);
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
	    
	    mObjTitle 	= (TextView) findViewById(R.id.progres_title); 
	    
	    mObjMin 	= (TextView) findViewById(R.id.progres_min); 
	    mObjMax 	= (TextView) findViewById(R.id.progres_max); 
	    mObjCurrent = (TextView) findViewById(R.id.progres_current); 
	    
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
	    childLayout.setOnClickListener(this);;
	    
	    mHandler = new Handler();
	}
	
	/**
	 * zobrazit progres jako procenta, min a max ukazuji 0 a 100, k cislu ve vypisu se pridaji procenta
	 * 
	 * @param showAsPercent
	 */
	public void showAsPercent(boolean showAsPercent){
		this.mShowAsPercent = showAsPercent;
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
	 * @param mMin
	 * @param mMax
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
	
	public void setFloor(boolean floor){
		mFloor = floor;
	}
	
	/**
	 * pri zmene nejakeho prvku updatujem view
	 */
	protected void updateView() {
		mObjTitle.setText(mTitle);
		mObjProgres.setProgress(mCurrent - mMin);
		
		double percent = mMin;
		
		if(mShowAsPercent){
			if(mFloor){
				mObjMin.setText(String.valueOf(Math.floor((int)NumberOperation.numberToPercent(mRangeMax - mRangeMin , mMin))));
				mObjMax.setText(String.valueOf(Math.floor((int)NumberOperation.numberToPercent(mRangeMax - mRangeMin , mMax))));
				percent = Math.max(0, Math.floor(NumberOperation.numberToPercent(mRangeMax - mRangeMin , mCurrent)));
			}else{
				mObjMin.setText(String.valueOf((int)NumberOperation.numberToPercent(mRangeMax - mRangeMin , mMin)));
				mObjMax.setText(String.valueOf((int)NumberOperation.numberToPercent(mRangeMax - mRangeMin  , mMax)));
				percent = Math.max(0, NumberOperation.round(NumberOperation.numberToPercent(mRangeMax - mRangeMin  , mCurrent), 1));
			}
			
			mObjProgresValue.setText(String.valueOf(percent) + "%");
			mObjCurrent.setText(String.valueOf(percent) + "%");
		}else{
			mObjMin.setText(String.valueOf(mRangeMin));
			mObjMax.setText(String.valueOf(mRangeMax));
			mObjProgresValue.setText(String.valueOf(mCurrent + mOffset));
			mObjCurrent.setText(String.valueOf(mCurrent + mOffset));
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
        if (R.id.progres_minus == v.getId()) {
            changeCurrent(mCurrent-1);
        } else if (R.id.progres_plus == v.getId()) {
            changeCurrent(mCurrent+1);
        }else if (R.id.progres_main == v.getId()) {
            toogleInput();
        }
    }
	
	/**
     * We start the long click here but rely on the {@link NumberPickerButton}
     * to inform us when the long click has ended.
     */
    public boolean onLongClick(View v) {
        if (R.id.progres_plus == v.getId()) {
            mIncrement = true;
            mHandler.post(mRunnable);
        } else if (R.id.progres_minus == v.getId()) {
            mDecrement = true;
            mHandler.post(mRunnable);
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
}
