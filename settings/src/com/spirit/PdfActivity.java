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

package com.spirit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.helpers.HelpLinks;
import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnPageChangeListener;
import com.lib.BluetoothCommandService;
import com.lib.DstabiProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;

public class PdfActivity extends BaseActivity implements OnPageChangeListener
{

	@SuppressWarnings("unused")
	final private String TAG = "AuthorActivity";

    /////PDF VIEW //////

    PDFView pdfView;

    Integer pageNumber = 1;

    File localFile;
    ////////////////////

    /////// DOWNLOAD MANGER ///////
    ProgressDialog mProgressDialog;

    String url;
    //////////////////////////////

	/**
	 * zavolani pri vytvoreni instance aktivity settings
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		initSlideMenu(R.layout.pdf);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
		((TextView) findViewById(R.id.title)).setText(getText(R.string.manual));

        url = HelpLinks.getPdfUrl(Locale.getDefault().getLanguage());
        File remoteFile = new File(url);
        String baseName = remoteFile.getName();

        localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + baseName);
        if(localFile.exists()){
            displayPdf(localFile);
        }else{

            //must be enable internet connection
            if(!isOnline()){
                Toast.makeText(getBaseContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }else {
                downloadFile(url, baseName);
            }
        }
	}

    /**
     * render PDF
     *
     * @param file
     */
    private void displayPdf(File file){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(file));
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);

        if (activities.size() > 0) { // inbuild viewvew
            startActivity(intent);
            finish();
        } else {
            pdfView = (PDFView)findViewById(R.id.pdfview);
            pdfView.fromFile(file)
                    .defaultPage(pageNumber)
                    .onPageChange(PdfActivity.this)
                    .showMinimap(true)
                    .enableSwipe(true)
                    .load();
        }
    }

    /**
     *
     * @param file
     */
    private void downloadFile(String file, String name){
        mProgressDialog = new ProgressDialog(PdfActivity.this);
        mProgressDialog.setMessage(getText(R.string.downloading) + " " + name);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setProgressDrawable(getResources().getDrawable(R.drawable.download_progressbar));

        final DownloadTask downloadTask = new DownloadTask(PdfActivity.this);
        downloadTask.execute(file, name);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
    }

    /**
     *
     */
	public void onResume()
	{
		super.onResume();
		if (stabiProvider.getState() == BluetoothCommandService.STATE_CONNECTED) {
			((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
		} else {
			((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.red);
		}
	}

    /**
     *
     * @param page
     * @param pageCount
     */
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        ((TextView) findViewById(R.id.title)).setText(getTitle() + " \u2192 " + format("%s / %s", page, pageCount));
    }

    /**
     *
     * @param msg
     * @return
     */
	public boolean handleMessage(Message msg)
	{
		switch (msg.what) {
			case DstabiProvider.MESSAGE_STATE_CHANGE:
				if (stabiProvider.getState() != BluetoothCommandService.STATE_CONNECTED) {
					sendInError(false);
					((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.red);
				} else {
					((ImageView) findViewById(R.id.image_title_status)).setImageResource(R.drawable.green);
				}
				break;
			default:
				super.handleMessage(msg);
		}
		return true;
	}

    /**
     *
     * @return
     */
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * vytvoreni kontextoveho menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return false;
    }

    /**
     * reakce na kliknuti polozky v kontextovem menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return false;
    }

    /**
     *
     */
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + sUrl[1]);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null) {
                Toast.makeText(context, getString(R.string.download_error) + ": " + result, Toast.LENGTH_LONG).show();
            }else {

                displayPdf(localFile);
                Toast.makeText(context, R.string.file_downloaded, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}
