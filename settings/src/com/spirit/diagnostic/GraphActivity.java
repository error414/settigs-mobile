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

package com.spirit.diagnostic;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
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
import android.widget.Toast;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;
import com.lib.FFT;
import com.lib.FileDialog;
import com.lib.SelectionMode;
import com.spirit.R;
import com.spirit.BaseActivity;
import com.androidplot.xy.*;
import com.androidplot.Plot;

public class GraphActivity extends BaseActivity{
	final private String TAG = "GraphActivity";
	
	
	//KODY PRO CALLBACKY //////////////////////////////
	final private int GRAPH_CALL_BACK_CODE = 21;
	
	final private int GROUP_AXIS 	= 222;
	final private int AXIS_X 		= 2222;
	final private int AXIS_Y 		= 2223;
	final private int AXIS_Z 		= 2224;
	
	
	final private int GROUP_SCREENSHOT 			= 333;
	final private int CHOOSE_DIRECOTORY 		= 444;
	
	
	@SuppressWarnings("unused")
	final private int MESSAGE_FFT 	= 100;
	////////////////////////////////////////////////////
	
	//provider pro pripojeni k zarizeni ////////////////
	private DstabiProvider stabiProvider;
	////////////////////////////////////////////////////
	
	//pro graf ////////////////////////////////////////
	private XYPlot aprLevelsPlot = null;
	private SimpleXYSeries aprLevelsSeries = null;
	///////////////////////////////////////////////////
	
	//jestli pri kliku na graf se ulozi screenshot
	private Boolean tapToScreenShot = false;
	
	////
	private byte[] dataBuffer;
	private int dataBuffer_len = 0;
	final private int DATABUFFER_SIZE = 3000;
	
	Number[] seriesX = null;
	Number[] seriesY = null;
	
	// FFT stuff
	final private int FFT_N = 1024;
	final private int FFT_NYQUIST = (FFT_N / 2) + 1;
	
	FFT fft = null;
	
	private double[] input_xr = null;
	private double[] input_xi = null;
	
	@SuppressLint("SdCardPath")
	final static protected String DEFAULT_SCREEN_SHOT_PATH = "/sdcard/";
	final static protected int REQUEST_DIR_FOR_SCREEN_SHOT = 15;
	
	private String dir_for_save_screen_shot;
	
	@SuppressLint("SimpleDateFormat")
	SimpleDateFormat sdf = new SimpleDateFormat("yy_MM_dd_HHmmss");

	
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
    }
	
	/**
	 * inicializace a nastaveni FFT
	 */
	private void initFFT(){
		fft = new FFT(10);	// nastavime rozliseni na 2^10
		
		// realna + imaginarni slozka pro vypocet FFT
		input_xr = new double[FFT_N];
		input_xi = new double[FFT_N];
	}
	
	/**
	 * inicializace a nastaveni zobrazeni grafu
	 */
	private void inicializeGraph(){
		seriesX = new Number[FFT_NYQUIST];
		seriesY = new Number[FFT_NYQUIST];
		
		aprLevelsSeries = new SimpleXYSeries(Arrays.asList(seriesX), Arrays.asList(seriesY), "");
		
		// setup the APR Levels plot:
        aprLevelsPlot = (XYPlot) findViewById(R.id.vibration);
        aprLevelsPlot.addSeries(aprLevelsSeries, LineAndPointRenderer.class, new LineAndPointFormatter(Color.rgb(0, 200, 0), null, null));
        aprLevelsPlot.disableAllMarkup();
        aprLevelsPlot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
        
        aprLevelsPlot.setRangeBoundaries(0, 100, BoundaryMode.FIXED);
        aprLevelsPlot.setRangeLabel("Amplitude");
        aprLevelsPlot.setRangeStepValue(50);
        
        aprLevelsPlot.setDomainBoundaries(0, 500, BoundaryMode.FIXED);
        aprLevelsPlot.setDomainLabel("Frequency [Hz]");
        aprLevelsPlot.setRangeStepValue(10);
        aprLevelsPlot.setOnClickListener(saveScreenShotListener);
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
		
		//startTime = System.nanoTime();//START
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
			
			// inicializace FFT
			initFFT();
			// inicializujeme graf
			inicializeGraph();
			
			startGraph();
			
		}else{
			finish();
		}
	}
	
	 // The Handler that gets information back from the 
	 private final Handler connectionHandler = new Handler(new Handler.Callback() {
		    @Override
		    public boolean handleMessage(Message msg) {
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
	        				int len = msg.getData().getByteArray("data").length;
	        				byte[] data = msg.getData().getByteArray("data");
	        					        				
	        				if(dataBuffer == null)
	        					dataBuffer = new byte[DATABUFFER_SIZE+72];	// 3byte*1024

	        				if ((dataBuffer_len+len) > DATABUFFER_SIZE)		// osetreni preteceni
	        					len = DATABUFFER_SIZE - dataBuffer_len;
	        					
	        				// rychlejsi konkatenace
	        				System.arraycopy(data, 0, dataBuffer, dataBuffer_len, len);
	        				dataBuffer_len += len;
	        					
	        				// buffer je plny
	        				if (dataBuffer_len == DATABUFFER_SIZE) {
	        					dataBuffer_len = 0;
	        					
	    	        			for (int i = 0; i < DATABUFFER_SIZE; ) {
		        					if ((int)dataBuffer[i] == -5) {	// nasli jsme magic byte	-5 & 0xFF = 251 = 0xFB
		        						short val = (short) ((dataBuffer[i+1] & 0xFF) | ((dataBuffer[i+2] & 0xFF) << 8));

		        						input_xr[i/3] = val;
		        						input_xi[i/3] = 0;

		        						i += 3;
		        					} else		        					
		        						i ++;
	        					}

	    	        			// aplikujeme Hamming okno
	    	        			for (int i = 0; i < FFT_N; i ++)
	    	        				input_xr[i] = (double) ((input_xr[i]))  * (0.54f - (0.46f * Math.cos ((2*Math.PI*i) / (FFT_N-1))));
	    	        			
	    	        			// fft pocitam zatim primo tady - je to docela rychle
	    	        			// TODO udelat v samostatnem vlaknu
								fft.doFFT(input_xr, input_xi, false);
							
								// vypocet amplitud celeho spektra
								for (int i = 0; i < FFT_NYQUIST; i ++) {
									seriesX[i] = (double) Math.sqrt ((input_xr[i]*input_xr[i]) + (input_xi[i]*input_xi[i])) * 5.33;
									
									// nezobrazujeme prvnich par Hz - je to urcite pohyb heli
									if (i < 5)
										seriesX[i] = 0;
								}
	    	        			
								// prekreslime graf - TODO udelat v samostatnem vlaknu
	    	        			updateGraph (seriesX);
	        				}
	        			}
	        			break;
	        	}
	        	return true;
	        }
	    });
	    
		View.OnClickListener saveScreenShotListener = new View.OnClickListener() {
			public void onClick(View v) {
				if(tapToScreenShot){
					
					String filename = dir_for_save_screen_shot + "/" + sdf.format(new Date()) + "-log.png";
					
					try {
						aprLevelsPlot.setDrawingCacheEnabled(true);
				        int width = aprLevelsPlot.getWidth();
				        int height = aprLevelsPlot.getHeight();
				        aprLevelsPlot.measure(width, height);
				        Bitmap bmp = Bitmap.createBitmap(aprLevelsPlot.getDrawingCache());
				        aprLevelsPlot.setDrawingCacheEnabled(false);
				        FileOutputStream fos;
						
						fos = new FileOutputStream(filename, true);
						bmp.compress(CompressFormat.PNG, 100, fos);
						
						Toast.makeText(getApplicationContext(), R.string.save_done, Toast.LENGTH_SHORT).show();
					} catch (FileNotFoundException e) {
						Toast.makeText(getApplicationContext(), R.string.not_log_for_save, Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
			        
				}
			}
		};
	 
		/**
	     * vytvoreni kontextoveho menu
	     */
	    @Override
	    public boolean onPrepareOptionsMenu(Menu menu)
	    {	
	    	menu.clear();
		   
		    
		    menu.add(GROUP_AXIS, 		AXIS_X, Menu.NONE, R.string.axis_X);
		    menu.add(GROUP_AXIS, 		AXIS_Y, Menu.NONE, R.string.axis_Y);
		    menu.add(GROUP_AXIS, 		AXIS_Z, Menu.NONE, R.string.axis_Z);
		    if(tapToScreenShot){
		    	menu.add(GROUP_SCREENSHOT, 	CHOOSE_DIRECOTORY, Menu.NONE, R.string.tap_to_screenshot_off);
	    	}else{
	    		menu.add(GROUP_SCREENSHOT, 	CHOOSE_DIRECOTORY, Menu.NONE, R.string.tap_to_screenshot_on);
	    	}
		    
		    return super.onPrepareOptionsMenu(menu);
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
                Toast.makeText(getApplicationContext(), getString(R.string.graph_change_axis) + getString(R.string.axis_X), Toast.LENGTH_SHORT).show();
	    		stabiProvider.sendDataImmediately("4DA\1".getBytes());			// HACK, chtelo by to vylepsit :)
	    	}
	    	
	    	//zobrazeni osy Y 
	    	if(item.getGroupId() == GROUP_AXIS && item.getItemId() == AXIS_Y){
                Toast.makeText(getApplicationContext(), getString(R.string.graph_change_axis) + getString(R.string.axis_Y), Toast.LENGTH_SHORT).show();
	    		stabiProvider.sendDataImmediately("4DA\2".getBytes());			// HACK, chtelo by to vylepsit :)
	    	}
	    	
	    	//zobrazeni osy Z 
	    	if(item.getGroupId() == GROUP_AXIS && item.getItemId() == AXIS_Z){
                Toast.makeText(getApplicationContext(), getString(R.string.graph_change_axis) + getString(R.string.axis_Z), Toast.LENGTH_SHORT).show();
	    		stabiProvider.sendDataImmediately("4DA\3".getBytes());			// HACK, chtelo by to vylepsit :)
	    	}
	    	
	    	//ulozeni obrazku grafu
	    	if(item.getGroupId() == GROUP_SCREENSHOT && item.getItemId() == CHOOSE_DIRECOTORY){
	    		if(tapToScreenShot){
	    			tapToScreenShot = false;
	    		}else{
	    			Intent intent = new Intent(getBaseContext(), FileDialog.class);
	                intent.putExtra(FileDialog.START_PATH, DEFAULT_SCREEN_SHOT_PATH);
	                intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
	                intent.putExtra(FileDialog.SELECTION_MODE,  SelectionMode.MODE_OPEN);
	                intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { });
	            	startActivityForResult(intent, REQUEST_DIR_FOR_SCREEN_SHOT);
	    		}
	    	}
	    	return false;
	    }
	    
	    /**
	     * zachytavani vysledku z aktivit
	     * 
	     */
	    public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {
	    	switch (requestCode) {
	    		case REQUEST_DIR_FOR_SCREEN_SHOT:
			        if (resultCode == Activity.RESULT_OK) {
			        	tapToScreenShot = true;
			        	dir_for_save_screen_shot = data.getStringExtra(FileDialog.RESULT_PATH);
			        }
	    	}
	    }
	
}
