package com.lib;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.spirit.R;
import com.helpers.StringOperation;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.spirit.BaseActivity;
import com.spirit.diagnostic.LogActivity;

public class LogPdf {
	 private Font catFont 	= new Font(Font.FontFamily.HELVETICA, 18, Font.NORMAL);
	 private Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
	 
	 private BaseActivity activity;
	 
	 private ArrayList<HashMap<Integer, Integer>> data;
	 
	 /**
	  * konstruktor
	  */
	 public LogPdf(BaseActivity activity, ArrayList<HashMap<Integer, Integer>> data) {
		 this.activity 	= activity;
		 this.data 		= data;
	 }
	 
	 public void create(String filePath) {
	     try {
		      Document document = new Document();
		      PdfWriter.getInstance(document, new FileOutputStream(filePath));
		      document.open();
		      
		      addTitlePage(document);
		      addLogPage(document);
		      
		      document.close();
	     } catch (Exception e) {
	    	 e.printStackTrace();
	     }
	 }
	 
	 /**
	  * hlavicka
	  * 
	  * @param document
	  * @throws DocumentException
	  */
	 private void addTitlePage(Document document) throws DocumentException {
		
		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100);
		// LOGO OBRAZEK ##################################################
		try {
			Image image1 = Image.getInstance(getByteArrayFromImageResource("logo_mobile.png"));
			PdfPCell cell = new PdfPCell(image1);
			cell.setBorder(0);
			table.addCell(cell);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// ###############################################################
		
		// HLAVICKA ######################################################
	    Paragraph preface = new Paragraph();
	    preface.add(new Paragraph("\n"));
	    preface.add(new Paragraph("\n"));
		preface.add(new Paragraph(activity.getString(R.string.pdf_unit_version) + ": ????", 	smallFont));
		
		preface.add(new Paragraph("\n"));
		preface.add(new Paragraph(activity.getString(R.string.pdf_time_run) + ": " + getTimeByPosition(data.size()), 		smallFont));
		
		
		Date currentTime = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM.d.yyyy    HH:mm:ss", Locale.US);
		preface.add(new Paragraph("\n"));
		preface.add(new Paragraph(activity.getString(R.string.pdf_time_create) + ": " + sdf.format(currentTime) , 	smallFont));
		
		
		PdfPCell cell = new PdfPCell(preface);
		cell.setBorder(0);
		cell.setVerticalAlignment (Element.ALIGN_CENTER);
		table.addCell(cell);
		// ###############################################################
		
		document.add(table);
		
		document.add(new Paragraph("\n"));
		document.add(new Paragraph(activity.getString(R.string.pdf_title), 	catFont));
		document.add(new Paragraph("\n"));
	 }
	 
	 /**
	  * 
	  * 
	  * @param document
	  * @throws DocumentException
	  */
	 private void addLogPage(Document document) throws DocumentException {

		PdfPTable table = new PdfPTable(3);
		table.setWidthPercentage(100);
		table.setWidths(new int[]{8,8, 92});
		
		for(int i = 0; i < data.size(); i++){
			//cas
			HashMap<Integer, Integer> dataRow = data.get(i);
			PdfPCell time = new PdfPCell(new Paragraph(getTimeByPosition(dataRow.get(LogActivity.POSITION)), 	smallFont));
			time.setFixedHeight(20f);
			table.addCell(time);
			
			//ikona
			try {
				
				HashMap <Integer, String> icoList = new HashMap<Integer, String>();
				icoList.put(R.drawable.ic_ok, "ic_ok.png");
				icoList.put(R.drawable.ic_info, "ic_info.png");
				icoList.put(R.drawable.ic_warn, "ic_warn.png");
				icoList.put(R.drawable.ic_warn2, "ic_warn2.png");
				
				Image imgIco = Image.getInstance(getByteArrayFromImageResource(icoList.get(dataRow.get(LogActivity.ICO_RESOURCE_LOG))));
				imgIco.scaleToFit(12, 12);
				PdfPCell ico = new PdfPCell(imgIco);
				ico.setFixedHeight(20f);
				ico.setBorder(0);
				ico.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
				ico.setVerticalAlignment(Element.ALIGN_MIDDLE);
				ico.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(ico);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//text
			PdfPCell text = new PdfPCell(new Paragraph(StringOperation.removeSpecialChars(activity.getString(dataRow.get(LogActivity.TITLE_FOR_LOG))), 	smallFont));
			text.setBorder(0);
			text.setBorder(Rectangle.TOP | Rectangle.BOTTOM | Rectangle.RIGHT);
			text.setFixedHeight(20f);
			table.addCell(text);
			
		}
		 
		document.add(table);
	 }
	 
	 /**
	  * vytvori z resource ID pole bytu
	  * 
	  * @param resource
	  * @return
	  */
	 private byte[] getByteArrayFromImageResource(String imageName){
		 try {
            // get input stream
           InputStream ims = activity.getAssets().open(imageName);
           Bitmap bmp = BitmapFactory.decodeStream(ims);
           ByteArrayOutputStream stream = new ByteArrayOutputStream();
           bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
           return stream.toByteArray();
        }
        catch(IOException e) {
        	e.printStackTrace();
            return new byte[0];
        }
	 }
	 
	@SuppressLint("DefaultLocale")
	protected String getTimeByPosition(int pos) {
		int sec = (pos - 1) * 10;
		return String.format("%02d:%02d", sec / 60, sec % 60);
	}
}
