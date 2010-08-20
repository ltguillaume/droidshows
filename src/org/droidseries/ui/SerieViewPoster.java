package org.droidseries.ui;

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.InputStream;

import android.app.ListActivity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class SerieViewPoster extends ListActivity {
	
	private final String MY_DEBUG_TAG = "DroidSeries";
	
	private String poster;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		poster = getIntent().getStringExtra("poster");
		setTitle(getIntent().getStringExtra("seriename") + " Poster");
		
		try {
			Drawable image = Drawable.createFromPath(poster);
			ImageView iv = new ImageView(this);
			iv.setImageDrawable(image);
			setContentView(iv);
		} catch (Exception e) {
			Log.e(MY_DEBUG_TAG, e.getMessage());
		}
	}
}