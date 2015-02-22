package com.lib;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.helpers.Globals;
import com.spirit.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/**
 * Activity para escolha de arquivos/diretorios.
 * 
 * @author android
 * 
 */
public class FileDialog extends ListActivity{

	/**
	 * Chave de um item da lista de paths.
	 */
	private static final String ITEM_KEY = "key";

	/**
	 * Imagem de um item da lista de paths (diretorio ou arquivo).
	 */
	private static final String ITEM_IMAGE = "image";
	
	/**
	 * 
	 */
	private static final String DATE_KEY = "date";

	/**
	 * Diretorio raiz.
	 */
	private static final String ROOT = "/";

	/**
	 * Parametro de entrada da Activity: path inicial. Padrao: ROOT.
	 */
	public static final String START_PATH = "START_PATH";

	/**
	 * Parametro de entrada da Activity: filtro de formatos de arquivos. Padrao:
	 * null.
	 */
	public static final String FORMAT_FILTER = "FORMAT_FILTER";

	/**
	 * Parametro de saida da Activity: path escolhido. Padrao: null.
	 */
	public static final String RESULT_PATH = "RESULT_PATH";

	/**
	 * Parametro de entrada da Activity: tipo de selecao: pode criar novos paths
	 * ou nao. Padrao: nao permite.
	 * 
	 * @see {@link SelectionMode}
	 */
	public static final String SELECTION_MODE = "SELECTION_MODE";

	/**
	 * Parametro de entrada da Activity: se e permitido escolher diretorios.
	 * Padrao: falso.
	 */
	public static final String CAN_SELECT_DIR = "CAN_SELECT_DIR";

	private List<String> path = null;
	private TextView myPath;
	private EditText mFileName;
	private ArrayList<HashMap<String, Object>> mList;

	private Button selectButton;

	private LinearLayout layoutSelect;
	private LinearLayout layoutCreate;
	private InputMethodManager inputManager;
	private String parentPath;
	private String currentPath = ROOT;

	private int selectionMode = SelectionMode.MODE_CREATE;

	private String[] formatFilter = null;

	private boolean canSelectDir = false;

	private File selectedFile;
	private HashMap<String, Integer> lastPositions = new HashMap<String, Integer>();

	private DstabiProvider stabiProvider;
	
	/**
	 * Called when the activity is first created. Configura todos os parametros
	 * de entrada e das VIEWS..
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		
		setContentView(R.layout.file_dialog_main);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView)findViewById(R.id.title)).setText(TextUtils.concat(getTitle() , " \u2192 " , getString(R.string.file_manager)));
		
		stabiProvider =  DstabiProvider.getInstance(connectionHandler);
		
		setResult(RESULT_CANCELED, getIntent());
		
		myPath = (TextView) findViewById(R.id.path);
		mFileName = (EditText) findViewById(R.id.fdEditTextFile);

		inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		selectButton = (Button) findViewById(R.id.fdButtonSelect);
		selectButton.setEnabled(false);
		selectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (selectedFile != null) {
					getIntent().putExtra(RESULT_PATH, selectedFile.getPath());
					setResult(RESULT_OK, getIntent());
					finish();
				}
			}
		});

		final Button newButton = (Button) findViewById(R.id.fdButtonNew);
		newButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setCreateVisible(v);

				mFileName.setText("");
				mFileName.requestFocus();
			}
		});

		selectionMode = getIntent().getIntExtra(SELECTION_MODE, SelectionMode.MODE_CREATE);

		formatFilter = getIntent().getStringArrayExtra(FORMAT_FILTER);

		canSelectDir = getIntent().getBooleanExtra(CAN_SELECT_DIR, false);

		if (selectionMode == SelectionMode.MODE_OPEN) {
			newButton.setEnabled(false);
		}

		layoutSelect = (LinearLayout) findViewById(R.id.fdLinearLayoutSelect);
		layoutCreate = (LinearLayout) findViewById(R.id.fdLinearLayoutCreate);
		layoutCreate.setVisibility(View.GONE);

		final Button cancelButton = (Button) findViewById(R.id.fdButtonCancel);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setSelectVisible(v);
			}

		});
		final Button createButton = (Button) findViewById(R.id.fdButtonCreate);
		createButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mFileName.getText().length() > 0) {
					getIntent().putExtra(RESULT_PATH, currentPath + "/" + mFileName.getText());
					setResult(RESULT_OK, getIntent());
					finish();
				}
			}
		});

		String startPath = getIntent().getStringExtra(START_PATH);
		startPath = startPath != null ? startPath : ROOT;
		if (canSelectDir) {
			File file = new File(startPath);
			selectedFile = file;
			selectButton.setEnabled(true);
		}
		getDir(startPath);


	}

    /**
     *
     * @param v
     */
    public void openOptionsMenu(View v) {
        //openOptionsMenu();
    }
	
	/**
	 */
	@Override
	public void onResume(){
		super.onResume();

         /* ################ PROTECT UNSAVE CHANGE ################ */
        if(Globals.getInstance().getUnsaveNotify() != null){
            Globals.getInstance().getUnsaveNotify().cancelAll();
        }
        this.stopActivityTransitionTimer();
        /* ################################################ */

        ((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.none);
        ((ImageView)findViewById(R.id.image_title_saved)).setImageResource(R.drawable.none);
        ((ImageView)findViewById(R.id.image_app_basic_mode)).setImageResource(R.drawable.none);
        ((TextView)findViewById(R.id.title_banks)).setText("");
        ((ImageView)findViewById(R.id.option_bar)).setImageResource(R.drawable.none);
	}

    @Override
    public void onPause()
    {
        super.onPause();
        if(Globals.getInstance().isChanged()) {
            this.startActivityTransitionTimer();
        }
    }

     /* ################ PROTECT UNSAVE CHANGE ################ */
    /**
     *
     */
    public void startActivityTransitionTimer() {
        Globals.getInstance().setmActivityTransitionTimer(new Timer());
        Globals.getInstance().setmActivityTransitionTimerTask(new TimerTask() {
            public void run() {
                if(Globals.getInstance().getUnsaveNotify() == null){
                    Globals.getInstance().setUnsaveNotify((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE));
                }

                Globals.getInstance().setUnsaveNotify((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE));
                Notification notify     = new Notification(R.drawable.notify_ico, getString(R.string.unsaved_changes), System.currentTimeMillis());
                PendingIntent pending   = PendingIntent.getActivity(getApplicationContext(), 0, getIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
                notify.setLatestEventInfo(getApplicationContext(), getString(R.string.unsaved_changes), getString(R.string.unsaved_changes_description), pending);

                try {
                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alert);
                    mediaPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Globals.getInstance().getUnsaveNotify().notify(0, notify);
            }
        });

        Globals.getInstance().getmActivityTransitionTimer().schedule( Globals.getInstance().getmActivityTransitionTimerTask(),
                Globals.MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    /**
     *
     */
    public void stopActivityTransitionTimer() {
        if (Globals.getInstance().getmActivityTransitionTimerTask() != null) {
            Globals.getInstance().getmActivityTransitionTimerTask().cancel();
        }

        if (Globals.getInstance().getmActivityTransitionTimer() != null) {
            Globals.getInstance().getmActivityTransitionTimer().cancel();
        }
    }
    /* ################################################ */

	private void getDir(String dirPath) {

		boolean useAutoSelection = dirPath.length() < currentPath.length();

		Integer position = lastPositions.get(parentPath);

		getDirImpl(dirPath);

		if (position != null && useAutoSelection) {
			getListView().setSelection(position);
		}

	}

	/**
	 * Monta a estrutura de arquivos e diretorios filhos do diretorio fornecido.
	 * 
	 * @param dirPath
	 *            Diretorio pai.
	 */
	private void getDirImpl(final String dirPath) {

		currentPath = dirPath;

		final List<String> item = new ArrayList<String>();
		path = new ArrayList<String>();
		mList = new ArrayList<HashMap<String, Object>>();

		File f = new File(currentPath);
		File[] files = f.listFiles();
		if (files == null) {
			currentPath = ROOT;
			f = new File(currentPath);
			files = f.listFiles();
		}
		myPath.setText(getText(R.string.location) + ": " + currentPath);

		if (!currentPath.equals(ROOT)) {

			File rootFile = new File(ROOT); 
			item.add(ROOT);
			addItem(ROOT, R.drawable.folder,  new Date(rootFile.lastModified()));
			path.add(ROOT);

			item.add("../");
			addItem("../", R.drawable.folder, new Date(f.lastModified()));
			path.add(f.getParent());
			parentPath = f.getParent();

		}

		TreeMap<String, String> dirsMap = new TreeMap<String, String>();
		TreeMap<String, String> dirsPathMap = new TreeMap<String, String>();
		TreeMap<String, String> filesMap = new TreeMap<String, String>();
		TreeMap<String, String> filesPathMap = new TreeMap<String, String>();
		
		TreeMap<String, Date> dateMap = new TreeMap<String, Date>();
		
		for (File file : files) {
			if (file.isDirectory()) {
				String dirName = file.getName();
				dirsMap.put(dirName, dirName);
				dirsPathMap.put(dirName, file.getPath());
				dateMap.put(dirName, new Date(file.lastModified()));
			} else {
				final String fileName = file.getName();
				final String fileNameLwr = fileName.toLowerCase(Locale.getDefault());
				// se ha um filtro de formatos, utiliza-o
				if (formatFilter != null) {
					boolean contains = false;
					for (int i = 0; i < formatFilter.length; i++) {
						final String formatLwr = formatFilter[i].toLowerCase(Locale.getDefault());
						if (fileNameLwr.endsWith(formatLwr)) {
							contains = true;
							break;
						}
					}
					if (contains) {
						dateMap.put(fileName, new Date(file.lastModified()));
						filesMap.put(fileName, fileName);
						filesPathMap.put(fileName, file.getPath());
					}
					// senao, adiciona todos os arquivos
				} else {
					dateMap.put(fileName, new Date(file.lastModified()));
					filesMap.put(fileName, fileName);
					filesPathMap.put(fileName, file.getPath());
				}
			}
		}
		item.addAll(dirsMap.tailMap("").values());
		item.addAll(filesMap.tailMap("").values());
		path.addAll(dirsPathMap.tailMap("").values());
		path.addAll(filesPathMap.tailMap("").values());

		SimpleAdapter fileList = new SimpleAdapter(this, mList, R.layout.file_dialog_row, new String[] {
				ITEM_KEY, ITEM_IMAGE, DATE_KEY }, new int[] { R.id.fdrowtext, R.id.fdrowimage, R.id.file_dialog_date });

		for (String dir : dirsMap.tailMap("").values()) {
			addItem(dir, R.drawable.folder, dateMap.get(dir));
		}

		for (String file : filesMap.tailMap("").values()) {
			addItem(file, R.drawable.file_icon_4dstabi, dateMap.get(file));
		}

		fileList.notifyDataSetChanged();

		setListAdapter(fileList);

	}

	private void addItem(String fileName, int imageId, Date date) {
		HashMap<String, Object> item = new HashMap<String, Object>();
		item.put(ITEM_KEY, fileName);
		item.put(ITEM_IMAGE, imageId);
		item.put(DATE_KEY, date);
		mList.add(item);
	}
	

	/**
	 * Quando clica no item da lista, deve-se: 1) Se for diretorio, abre seus
	 * arquivos filhos; 2) Se puder escolher diretorio, define-o como sendo o
	 * path escolhido. 3) Se for arquivo, define-o como path escolhido. 4) Ativa
	 * botao de selecao.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		File file = new File(path.get(position));

		setSelectVisible(v);

		if (file.isDirectory()) {
			selectButton.setEnabled(false);
			if (file.canRead()) {
				lastPositions.put(currentPath, position);
				getDir(path.get(position));
				if (canSelectDir) {
					selectedFile = file;
					v.setSelected(true);
					selectButton.setEnabled(true);
				}
			} else {
				new AlertDialog.Builder(this).setIcon(R.drawable.file_icon_4dstabi)
						.setTitle("[" + file.getName() + "] " + getText(R.string.cant_read_folder))
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						}).show();
			}
		} else {
			selectedFile = file;
			v.setSelected(true);
			selectButton.setEnabled(true);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			selectButton.setEnabled(false);

			if (layoutCreate.getVisibility() == View.VISIBLE) {
				layoutCreate.setVisibility(View.GONE);
				layoutSelect.setVisibility(View.VISIBLE);
			} else {
				if (!currentPath.equals(ROOT)) {
					getDir(parentPath);
				} else {
					return super.onKeyDown(keyCode, event);
				}
			}

			return true;
		} else {*/
			return super.onKeyDown(keyCode, event);
		/*}*/
	}

	/**
	 * Define se o botao de CREATE e visivel.
	 * 
	 * @param v
	 */
	private void setCreateVisible(View v) {
		layoutCreate.setVisibility(View.VISIBLE);
		layoutSelect.setVisibility(View.GONE);

		inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
		selectButton.setEnabled(false);
	}

	/**
	 * Define se o botao de SELECT e visivel.
	 * 
	 * @param v
	 */
	private void setSelectVisible(View v) {
		layoutCreate.setVisibility(View.GONE);
		layoutSelect.setVisibility(View.VISIBLE);

		inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
		selectButton.setEnabled(false);
	}
	
	
	// The Handler that gets information back from the 
    private final Handler connectionHandler = new Handler(new Handler.Callback() {
	    @Override
	    public boolean handleMessage(Message msg) {
        	switch(msg.what){
    			case DstabiProvider.MESSAGE_STATE_CHANGE:
    				if(stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED){
						finish();
					}else{
						((ImageView)findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
					}
    				break;
        	}
        	return true;
        }
    });
}
