package org.droidseries.ui;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.droidseries.R;
import org.droidseries.droidseries;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ViewEpisode extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_episode);
		View view = findViewById(R.id.viewEpisodes);
		view.setOnTouchListener(new SwipeDetect());
		String serieId = getIntent().getStringExtra("serieId");
		String serieName = getIntent().getStringExtra("serieName");
		int seasonNumber = getIntent().getIntExtra("seasonNumber", 0);
		String episodeId = getIntent().getStringExtra("episodeId");
		
		String query = "SELECT episodeNumber, episodeName, overview, rating, firstAired FROM episodes "
			+ "WHERE id = '"+ episodeId +"' AND serieId='"+ serieId +"'";
		Cursor c = droidseries.db.Query(query);
		c.moveToFirst();
		if (c != null && c.isFirst()) {
			int episodeNumberCol = c.getColumnIndex("episodeNumber");
			int enameCol = c.getColumnIndex("episodeName");
			int overviewCol = c.getColumnIndex("overview");
			int ratingCol = c.getColumnIndex("rating");
			int airedCol = c.getColumnIndex("firstAired");
	
			String firstAired = c.getString(airedCol);
			if (!firstAired.equals("") && !firstAired.equals("null")) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date epDate = sdf.parse(firstAired);
					Format formatter = SimpleDateFormat.getDateInstance();
					firstAired = formatter.format(epDate);
				} catch (ParseException e) {
					Log.e("DroidSeries", e.getMessage());
				}
			} else {
				firstAired = "";
			}
	
			int episodeNumber = c.getInt(episodeNumberCol);
			String episodeName = c.getString(enameCol);
			String overview = c.getString(overviewCol);
			String rating = c.getString(ratingCol);
			c.close();
	
			setTitle(serieName +" "+ seasonNumber +"x"+ episodeNumber);				
			
			TextView episodeNameV = (TextView) findViewById(R.id.episodeName);
			episodeNameV.setText(episodeName);
			
			if (!rating.equalsIgnoreCase("null") && !rating.equals("")) {
				TextView ratingV = (TextView) findViewById(R.id.rating);
				ratingV.setText("IMDb: "+ rating);
				ratingV.setVisibility(View.VISIBLE);
			}
			
			if (!firstAired.equalsIgnoreCase("null") && !firstAired.equals("")) {
				TextView firstAiredV = (TextView) findViewById(R.id.firstAired);
				firstAiredV.setText(firstAired);
				firstAiredV.setVisibility(View.VISIBLE);
			}
	
			if (!overview.equalsIgnoreCase("null") && !overview.equals("")) {
				TextView overviewV = (TextView) findViewById(R.id.overview);
				overviewV.setText(overview);
				findViewById(R.id.overviewField).setVisibility(View.VISIBLE);
			}

			List<String> writers = new ArrayList<String>();
			Cursor cwriters = droidseries.db.Query("SELECT writer FROM writers WHERE episodeId='" + episodeId
				+"' AND serieId='"+ serieId +"'");
			cwriters.moveToFirst();
			if (cwriters != null && cwriters.isFirst()) {
				do {
					writers.add(cwriters.getString(0));
				} while (cwriters.moveToNext());
			}
			cwriters.close();
			if (!writers.isEmpty()) {
				TextView writer = (TextView) findViewById(R.id.writer);
				writer.setText(writers.toString().replace("]", "").replace("[", ""));
				findViewById(R.id.writerField).setVisibility(View.VISIBLE);
			}
			
			List<String> directors = new ArrayList<String>();
			Cursor cdirectors = droidseries.db.Query("SELECT director FROM directors WHERE episodeId='"+ episodeId
				+"' AND serieId='"+ serieId +"'");
			cdirectors.moveToFirst();
			if (cdirectors != null && cdirectors.isFirst()) {
				do {
					directors.add(cdirectors.getString(0));
				} while (cdirectors.moveToNext());
			}
			cdirectors.close();
			if (!directors.isEmpty()) {
				TextView director = (TextView) findViewById(R.id.director);
				director.setText(directors.toString().replace("]", "").replace("[", ""));
				findViewById(R.id.directorField).setVisibility(View.VISIBLE);
			}
	
			List<String> guestStars = new ArrayList<String>();
			Cursor cgs = droidseries.db.Query("SELECT guestStar FROM guestStars WHERE episodeId='"+ episodeId
				+"' AND serieId='"+ serieId +"'");
			cgs.moveToFirst();
			if (cgs != null && cgs.isFirst()) {
				do {
					guestStars.add(cgs.getString(0));
				} while (cgs.moveToNext());
			}
			cgs.close();
			if (!guestStars.isEmpty()) {
				TextView guestStarsV = (TextView) findViewById(R.id.guestStars);
				guestStarsV.setText(guestStars.toString().replace("]", "").replace("[", ""));
				findViewById(R.id.guestStarsField).setVisibility(View.VISIBLE);
			}
		}
	}
}