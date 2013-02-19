package com.settings.diagnostic;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.helpers.ByteOperation;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.settings.BaseActivity;
import com.settings.R;
import com.androidplot.xy.*;


public class GraphActivity extends BaseActivity{
	final private String TAG = "GraphActivity";
	
	
	//KODY PRO CALLBACKY //////////////////////////////
	final private int GRAPH_CALL_BACK_CODE = 21;
	
	final private int GROUP_AXIS 	= 222;
	final private int AXIS_X 		= 2222;
	final private int AXIS_Y 		= 2223;
	final private int AXIS_Z 		= 2224;
	
	final private int MESSAGE_FFT 	= 100;
	////////////////////////////////////////////////////
	
	//provider pro pripojeni k zarizeni ////////////////
	private DstabiProvider stabiProvider;
	////////////////////////////////////////////////////
	
	//pro graf ////////////////////////////////////////
	private XYPlot aprLevelsPlot = null;
	private SimpleXYSeries aprLevelsSeries = null;
	///////////////////////////////////////////////////
	
	////
	private byte[] dataBuffer;
	
	private long startTime;
	
	
	
	/**
	 * zavolani pri vytvoreni instance aktivity servos
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.graph);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.graph_button_text)));
        
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		
		//inicializujeme graf
		//inicializeGraph();
		
		startGraph();
    }
	
	/**
	 * inicializace a nastaveni zobrazeni grafu
	 * 
	 */
	private void inicializeGraph(){
		Number[] seriesX = {0, 25, 55, 2, 80, 30, 99, 0, 44, 6};
		Number[] seriesY = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
		aprLevelsSeries = new SimpleXYSeries(Arrays.asList(seriesY), Arrays.asList(seriesX), "");
		
		// setup the APR Levels plot:
        aprLevelsPlot = (XYPlot) findViewById(R.id.vibration);
        aprLevelsPlot.addSeries(aprLevelsSeries, LineAndPointRenderer.class, new LineAndPointFormatter(Color.rgb(0, 200, 0), null, null));
        aprLevelsPlot.disableAllMarkup();
        
        aprLevelsPlot.setRangeBoundaries(0, 220, BoundaryMode.FIXED);
        aprLevelsPlot.setRangeLabel("osa X");
        aprLevelsPlot.setRangeStepValue(11);
        
        aprLevelsPlot.setDomainBoundaries(0, 10, BoundaryMode.FIXED);
        aprLevelsPlot.setDomainLabel("hz");
        aprLevelsPlot.setDomainStepValue(5);
        
        aprLevelsPlot.getLayoutManager()
        .remove(aprLevelsPlot.getLegendWidget());
	}
	
	/**
	 * update grafu po vypocteni novych dat
	 * 
	 * @param seriesX
	 */
	private void updateGraph(Number[] seriesX){
		aprLevelsSeries.setModel(Arrays.asList(seriesX), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		aprLevelsPlot.redraw();
	}
	
	/**
	 * stopnuti aktovity, posle pozadavek na ukonceni streamu
	 */
	@Override
    public void onStop() 
	{
        super.onStop();
        stabiProvider.stopGraph();
    }
	
	
	/**
	 * zapnem stream
	 * 
	 */
    public void startGraph(){
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				stabiProvider.getGraph(GRAPH_CALL_BACK_CODE);
			}
		};
		
		startTime = System.nanoTime();//START
		thread.run();
	}
	
    /**
     * tuhle metodu potrebuje graf, asi ho psal blbec
     * 
     * @param v
     */
	public void getData(View v){
	}
	
	/**
	 * znovu nacteni aktivity, priradime dstabi svuj handler a zkontrolujeme jestli sme pripojeni
	 */
	@Override
	public void onResume(){
		super.onResume();
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		if(stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED){
			((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
		}else{
			finish();
		}
	}
	
	 // The Handler that gets information back from the 
	 private final Handler connectionHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	Log.d(TAG, "prisla zprava");
	        	switch(msg.what){
		        	case DstabiProvider.MESSAGE_SEND_COMAND_ERROR:
		        		
		        		sendInError();
						break;
					case DstabiProvider.MESSAGE_SEND_COMPLETE:
						sendInSuccessInfo();
						break;
	        		case DstabiProvider.MESSAGE_STATE_CHANGE:
						if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
							sendInError();
						}
						break;
	        		case GRAPH_CALL_BACK_CODE:
	        			if(msg.getData().containsKey("data")){
	        				Log.d(TAG, "Odpoved: " + (msg.getData().getByteArray("data").length));
	        				
	        				
	        				/*byte[] outArray;
	        				if(dataBuffer != null ){
	        					int lenA = msg.getData().getByteArray("data").length;
	        					int lenB = dataBuffer.length;
	        					outArray = new byte[lenA + lenB];
	        					
	        					System.arraycopy (msg.getData().getByteArray("data"), 0, outArray, 0, lenA);
		        				System.arraycopy (dataBuffer, lenA, outArray, lenA, lenB);
	        					
	        				}else{
	        					outArray = new byte[0];
	        				}

	        				// mame dostatek dat
	        				if(outArray.length > 2000){
	        					Log.d(TAG, "mame delku dat " + outArray.length);
	        					Log.d(TAG, "cas " + (System.nanoTime() - startTime));//for seconds;
	        					
	        					dataBuffer = null;
	        				}*/
	        				
	        				
	        				
	        				//sem prisla jedna sada dat ze zarizeni, pokud neni dost dlouha je mozne sadu dat ulozit do tem promene a pockat az prijde dalsi sada dat
	        				// pokud delka uz vyhovuje vypocteme hodnoty grafu
	        				// vypocet se musi spustit v novem vlakne
	        				
	        				Runnable thread = new Runnable() {
	        					@Override
	        					public void run() {
	        						// zde spustime vypocet FFT
	        						// nejak takto FFT.getForX(handler, int caalback);
	        						// handler FFThandler pak zachyti dokonceni vypoctu
	        					}
	        				};
	        				
	        				thread.run();
	        			}
	        			break;
	        	}
	        }
	    };
	    
	    
	    // handler pro vypocet FFT
		 private final Handler FFThandler = new Handler() {
		        @Override
		        public void handleMessage(Message msg) {
		        	switch(msg.what){
			        	case MESSAGE_FFT:
			        		if(msg.getData().containsKey("data")){
		    				Log.d(TAG, "Odpoved: " + (msg.getData().getByteArray("data").length));
		    				//prisli vypocitane data
		    				// zavolame updateGraph(Numbers[]);
		    			}
						break;
		    	}
		    }
		};
		    
		    
		/**
	     * vytvoreni kontextoveho menu
	     */
	    @Override
	    public boolean onCreateOptionsMenu(Menu menu)
	    {
		    super.onCreateOptionsMenu(menu);
		    
		    menu.add(GROUP_AXIS, AXIS_X, Menu.NONE, R.string.axis_X);
		    menu.add(GROUP_AXIS, AXIS_Y, Menu.NONE, R.string.axis_Y);
		    menu.add(GROUP_AXIS, AXIS_Z, Menu.NONE, R.string.axis_Z);
		    return true;
	    }
	    
	    /**
	     * reakce na kliknuti polozky v kontextovem menu
	     */
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) 
	    {
	    	super.onOptionsItemSelected(item);
	    	//zobrazeni osy X 
	    	if(item.getGroupId() == GROUP_AXIS && item.getItemId() == AXIS_X){

	    	}
	    	
	    	//zobrazeni osy Y 
	    	if(item.getGroupId() == GROUP_AXIS && item.getItemId() == AXIS_Y){
	    		
	    	}
	    	
	    	//zobrazeni osy Z 
	    	if(item.getGroupId() == GROUP_AXIS && item.getItemId() == AXIS_Z){
	    		
	    	}
	    	return false;
	    }
	
}
