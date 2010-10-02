package org.droidseries.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.droidseries.droidseries;
import org.droidseries.thetvdb.TheTVDB;
import org.droidseries.thetvdb.model.Serie;
import org.droidseries.thetvdb.model.TVShowItem;
import org.droidseries.utils.Utils;

import org.droidseries.R;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
//import android.view.Window;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SerieOverview extends Activity {
	private TheTVDB theTVDB;
	private Utils utils = new Utils();
	
	private final String TAG = "DroidSeries";
	private ProgressDialog m_ProgressDialog = null; 
	private String serieid;
	
	private CharSequence text;
	private int duration;
	
	/* Option Menus */
	private static final int ADD_SERIE_MENU_ITEM = Menu.FIRST;

	@Override
	  public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TextView tv = new TextView(this);
		ScrollView sv = new ScrollView(this);
		
		try {
			serieid = getIntent().getStringExtra("serieid");
			setTitle(getIntent().getStringExtra("name") + " - " + getString(R.string.messages_overview));
			tv.setText(getIntent().getStringExtra("overview"));
		} catch (Exception e) {
			Log.e(TAG, "Error getting the intent extra value.");
		}
		
		sv.addView(tv);
	    setContentView(sv);
	  }
	
	/* Options Menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, ADD_SERIE_MENU_ITEM, 0, getString(R.string.menu_context_add_serie)).setIcon(android.R.drawable.ic_menu_add);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()) {
    		case ADD_SERIE_MENU_ITEM:
    			//TODO: adicionar uma verificacao para saber se ha net
    			theTVDB = new TheTVDB("8AC675886350B3C3");
    			if(theTVDB.getMirror() != null) {
					Runnable addnewserie = new Runnable(){
			            @Override
			            public void run() {
			            	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			            	PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
			            	wl.acquire();
			            	Serie sToAdd = theTVDB.getSerie(serieid, "en");
			            	if (sToAdd == null) {
			            		m_ProgressDialog.dismiss();
			            		
			            		if(utils.isNetworkAvailable(SerieOverview.this)) {
			            			text = getText(R.string.messages_thetvdb_con_error);
									duration = Toast.LENGTH_LONG;
			            		} else {
			            			text = getText(R.string.messages_no_internet);
									duration = Toast.LENGTH_LONG;
			            		}			            	
								
								Looper.prepare();
								Toast toast = Toast.makeText(getApplicationContext(), text, duration);
								toast.show();
								Looper.loop();
			            	}
			            	else {
				            	URL imageUrl;
								try {
									imageUrl = new URL( sToAdd.getPoster() );
									URLConnection uc = imageUrl.openConnection();
					                String contentType = uc.getContentType();
					                //Log.i("DroidSeries", contentType);
					                int contentLength = uc.getContentLength();
					                if(!TextUtils.isEmpty(contentType)) {
						                if (!contentType.startsWith("image/") || contentLength == -1) {
						                    //throw new IOException("This is not a binary file.");
						                	Log.e(TAG, "This is not a image.");
						                }
					                }
					                try {
					                	File cacheImage = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/" + imageUrl.getFile().toString());
					                	FileUtils.copyURLToFile(imageUrl, cacheImage);
		
					                	Bitmap posterThumb = BitmapFactory.decodeFile(getApplicationContext().getFilesDir().getAbsolutePath() + imageUrl.getFile().toString());
					                	int width = posterThumb.getWidth();
					                	int height = posterThumb.getHeight();
					                	
					                	//TODO: check this for other resolutions
			    	                	//int newWidth = 42;
			    	                	//int newHeight = 64;
			    	                	//int newWidth = 128;
			    	                	//int newHeight = 180;
			    	                	Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
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
					                	
					                	File dirTmp = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/thumbs/banners/posters");
					                	if(!dirTmp.isDirectory()) {
					                		dirTmp.mkdirs();
					                	}
					                	
					                	OutputStream fOut = null;
					                	File thumFile = new File(getApplicationContext().getFilesDir().getAbsolutePath(), "thumbs" + imageUrl.getFile().toString());
					                	//fOut = openFileOutput(getApplicationContext().getFilesDir().getAbsolutePath() + "/thumbs" + imageUrl.getFile().toString(), Context.MODE_PRIVATE);
					                    fOut = new FileOutputStream(thumFile);
					                	resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
					                	fOut.flush();
					                    fOut.close();
					                	
					                    sToAdd.setPosterThumb(getApplicationContext().getFilesDir().getAbsolutePath() + "/thumbs" + imageUrl.getFile().toString());
					                	sToAdd.setPosterInCache(getApplicationContext().getFilesDir().getAbsolutePath() + "/" + imageUrl.getFile().toString());
					                	wl.release();
					                } catch (Exception e) {
					                	sToAdd.setPosterInCache("");
					                	Log.e(TAG, "Error copying the poster to cache.");
					                	wl.release();
					                }
								} catch (MalformedURLException e) {
									//e.printStackTrace();
								} catch (IOException e) {
									//e.printStackTrace();
									wl.release();
								}
				                
								boolean sucesso = false;
				            	try {            		
				            		sToAdd.saveToDB(droidseries.db);
		
				            		int nseasons = droidseries.db.getSeasonCount(sToAdd.getId());
				            		String nextEpisode = droidseries.db.getNextEpisode(sToAdd.getId(), -1);
				            		Date nextAir= droidseries.db.getNextAir(sToAdd.getId(), -1);
				            		int unwatched = droidseries.db.getEPUnwatched(sToAdd.getId());
				            		Drawable d = Drawable.createFromPath(sToAdd.getPosterThumb());
									TVShowItem tvsi = new TVShowItem(sToAdd.getId(), sToAdd.getPosterThumb(), d, sToAdd.getSerieName(), nseasons, nextEpisode, nextAir, unwatched, false);
						            droidseries.series.add(tvsi);
						            
									runOnUiThread(droidseries.updateListView);
									
									sucesso = true;
				            	} catch (Exception e) {
				            		//does nothings
				            	}
				            	
								m_ProgressDialog.dismiss();
								
								if(sucesso) {
									CharSequence text = String.format(getString(R.string.messages_series_success) ,sToAdd.getSerieName());
									int duration = Toast.LENGTH_LONG;
									
									Looper.prepare();
									Toast toast = Toast.makeText(getApplicationContext(), text, duration);
									toast.show();
									Looper.loop();
								}
			            	}
			            }
			        };
			         
					boolean alreadyExists = false;
		            for(int i=0; i < droidseries.series.size(); i++) {
		            	if( droidseries.series.get(i).getSerieId().equals(serieid) ) {
		            		alreadyExists = true;
		            		CharSequence text = String.format(getString(R.string.messages_show_exists), droidseries.series.get(i).getName());
							int duration = Toast.LENGTH_LONG;
							
							Toast toast = Toast.makeText(getApplicationContext(), text, duration);
							toast.show();
							break;
		            	}
		            }
	            	
	            	if(!alreadyExists) {
	            		Thread thread =  new Thread(null, addnewserie, "MagentoBackground");
	    		        thread.start();
	    		        m_ProgressDialog = ProgressDialog.show(SerieOverview.this,    
	    		        		getString(R.string.messages_title_adding_serie), getString(R.string.messages_adding_serie), true);
	            	}
    			}
    			else {
    				
    			}
    			break;
    	}
    	return super.onOptionsItemSelected(item);
    }
}