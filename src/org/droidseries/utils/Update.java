package org.droidseries.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.droidseries.droidseries;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.view.Display;

public class Update {
	
	private Utils utils = new Utils();
	
	//TODO: change this method from void to boolean to check if all updates go well
	public void updateDroidSeries(Context context, Display display) {
		droidseries.db.execQuery("CREATE TABLE IF NOT EXISTS droidseries (version VARCHAR)");
		
		String version = "";
		try {
			Cursor c = droidseries.db.Query("SELECT version FROM droidseries");
			try {
				c.moveToFirst();
				if (c != null && c.isFirst()) {
					if (c.getCount() != 0) {
						//get the version
						version = c.getString(0);
					}
				}
			} catch (Exception e) {
				//does nothing
			}
			c.close();
			
			//runs the updates if the db version isn't the current version
			if(version.equals("") || !version.equals(droidseries.VERSION)) {
				u0141To0142(context, display);
				u0142To0143(context, display);
			}
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
	}
	
	private boolean u0141To0142(Context context, Display display) {
		String version = "";
		try {
			Cursor c = droidseries.db.Query("SELECT version FROM droidseries");
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				if (c.getCount() != 0) {
					//get the version
					version = c.getString(0);
				}
			}
			c.close();
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
		
		if(version.equals("")) {
			//insert sql statment with the current version
			droidseries.db.execQuery("INSERT INTO droidseries (version) VALUES ('" + droidseries.VERSION + "')");
			
			//do the changes you have to do
			try {
				List<String> serieIds = droidseries.db.getSeries();
				
				for(int i=0; i<serieIds.size(); i++) {
					try {
						Cursor c = droidseries.db.Query("SELECT posterInCache, posterThumb FROM series WHERE id='" + serieIds.get(i)  + "'");
						c.moveToFirst();
						if (c != null && c.isFirst()) {
							File thumbImage = new File(c.getString(1));
							thumbImage.delete();
							
							//create the new thumb
							Bitmap posterThumb = BitmapFactory.decodeFile(c.getString(0));
		                	int width = posterThumb.getWidth();
		                	int height = posterThumb.getHeight();

		                	//TODO: check this for other resolutions
    	                	//int newWidth = 42;
    	                	//int newHeight = 64;
		                	//int newWidth = 128;
    	                	//int newHeight = 180;
    	                	
    	                	//Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
    	                	int Vwidth = display.getWidth();
    	                	int Vheight = display.getHeight();
    	                 	int[] viewSize = utils.getViewSize(Vwidth, Vheight);
    	                 	
    	                 	int newWidth = 0;
    	                 	int newHeight = 0;
    	                 	if(viewSize[0] <= 350) {
    	                 		newWidth =   (int) (viewSize[0] * 0.16);
        	                	newHeight =  (int) (viewSize[1] * 0.156);
    	                 	}
    	                 	else {
    	                 		newWidth =   (int) (viewSize[0] * 0.26);
        	                	newHeight =  (int) (viewSize[1] * 0.211);    	 
    	                 	}
		                	
		                	float scaleWidth = ((float) newWidth) / width;
		                	float scaleHeight = ((float) newHeight) / height;

		                	Matrix matrix = new Matrix();
		                	matrix.postScale(scaleWidth, scaleHeight);
		                	
		                	Bitmap resizedBitmap = Bitmap.createBitmap(posterThumb, 0, 0, width, height, matrix, true);
		                	
		                	File dirTmp = new File(context.getFilesDir().getAbsolutePath() + "/thumbs/banners/posters");
		                	if(!dirTmp.isDirectory()) {
		                		dirTmp.mkdirs();
		                	}
		                	
		                	OutputStream fOut = null;
		                	File thumFile = new File(c.getString(1));
		                    fOut = new FileOutputStream(thumFile);
		                	resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
		                	fOut.flush();
		                    fOut.close();
		                    
		                    posterThumb.recycle();
		                    resizedBitmap.recycle();
		                    System.gc();
		                    posterThumb = null;
		                    resizedBitmap = null;
						}
						c.close();
					} catch (SQLiteException e) {
						Log.e("DroidSeries", e.getMessage());
					}
				}
			} catch (Exception e) {
				
			}
		}
		
		return true;
	}
	
	private boolean u0142To0143(Context context, Display display) {
		String version = "";
		try {
			Cursor c = droidseries.db.Query("SELECT version FROM droidseries");
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				if (c.getCount() != 0) {
					//get the version
					version = c.getString(0);
				}
			}
			c.close();
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
		
		if(version.equals("0.1.4-2")) {
			//insert sql statment with the current version
			droidseries.db.execQuery("UPDATE droidseries SET version='" + droidseries.VERSION + "'");
			
			//do the changes you have to do
			try {
				List<String> serieIds = droidseries.db.getSeries();
				
				for(int i=0; i<serieIds.size(); i++) {
					try {
						Cursor c = droidseries.db.Query("SELECT posterInCache, posterThumb FROM series WHERE id='" + serieIds.get(i)  + "'");
						c.moveToFirst();
						if (c != null && c.isFirst()) {
							File thumbImage = new File(c.getString(1));
							thumbImage.delete();
							
							//create the new thumb
							Bitmap posterThumb = BitmapFactory.decodeFile(c.getString(0));
		                	int width = posterThumb.getWidth();
		                	int height = posterThumb.getHeight();

		                	//TODO: check this for other resolutions
    	                	//int newWidth = 42;
    	                	//int newHeight = 64;
		                	//int newWidth = 128;
    	                	//int newHeight = 180;
    	                	
    	                	//Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
    	                	int Vwidth = display.getWidth();
    	                	int Vheight = display.getHeight();
    	                	int[] viewSize = utils.getViewSize(Vwidth, Vheight);
    	                 	
    	                 	int newWidth = 0;
    	                 	int newHeight = 0;
    	                 	if(viewSize[0] <= 350) {
    	                 		newWidth =   (int) (viewSize[0] * 0.16);
        	                	newHeight =  (int) (viewSize[1] * 0.156);
    	                 	}
    	                 	else {
    	                 		newWidth =   (int) (viewSize[0] * 0.26);
        	                	newHeight =  (int) (viewSize[1] * 0.211);    	 
    	                 	}
		                	
		                	float scaleWidth = ((float) newWidth) / width;
		                	float scaleHeight = ((float) newHeight) / height;

		                	Matrix matrix = new Matrix();
		                	matrix.postScale(scaleWidth, scaleHeight);
		                	
		                	Bitmap resizedBitmap = Bitmap.createBitmap(posterThumb, 0, 0, width, height, matrix, true);
		                	
		                	File dirTmp = new File(context.getFilesDir().getAbsolutePath() + "/thumbs/banners/posters");
		                	if(!dirTmp.isDirectory()) {
		                		dirTmp.mkdirs();
		                	}
		                	
		                	OutputStream fOut = null;
		                	File thumFile = new File(c.getString(1));
		                    fOut = new FileOutputStream(thumFile);
		                	resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
		                	fOut.flush();
		                    fOut.close();
		                    
		                    posterThumb.recycle();
		                    resizedBitmap.recycle();
		                    System.gc();
		                    posterThumb = null;
		                    resizedBitmap = null;
						}
						c.close();
					} catch (SQLiteException e) {
						Log.e("DroidSeries", e.getMessage());
					}
				}
			} catch (Exception e) {
				//does nothings
			}
		}
		
		return true;
	}
	
}