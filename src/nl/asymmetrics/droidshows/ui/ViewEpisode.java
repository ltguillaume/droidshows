package nl.asymmetrics.droidshows.ui;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.asymmetrics.droidshows.R;
import nl.asymmetrics.droidshows.DroidShows;
import nl.asymmetrics.droidshows.utils.SwipeDetect;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ViewEpisode extends Activity
{
	private String uri = "imdb:///";
	private List<String> writers = new ArrayList<String>();
	private List<String> directors = new ArrayList<String>();
	private List<String> guestStars = new ArrayList<String>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.overridePendingTransition(R.anim.right_enter, R.anim.right_exit);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_episode);
		View view = findViewById(R.id.viewEpisodes);
		SwipeDetect swipeDetect = new SwipeDetect();
		view.setOnTouchListener(swipeDetect);
		String serieId = getIntent().getStringExtra("serieId");
		final String serieName = getIntent().getStringExtra("serieName");
		String episodeId = getIntent().getStringExtra("episodeId");
		
		String query = "SELECT seasonNumber, episodeNumber, episodeName, overview, rating, firstAired, imdbId FROM episodes "
			+ "WHERE id = '"+ episodeId +"' AND serieId='"+ serieId +"'";
		Cursor c = DroidShows.db.Query(query);
		c.moveToFirst();
		if (c != null && c.isFirst()) {
			int seasonNumberCol = c.getColumnIndex("seasonNumber");
			int episodeNumberCol = c.getColumnIndex("episodeNumber");
			int enameCol = c.getColumnIndex("episodeName");
			int overviewCol = c.getColumnIndex("overview");
			int ratingCol = c.getColumnIndex("rating");
			int airedCol = c.getColumnIndex("firstAired");
			int imdbIdCol = c.getColumnIndex("imdbId");
	
			String firstAired = c.getString(airedCol);
			if (!firstAired.equals("") && !firstAired.equals("null")) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date epDate = sdf.parse(firstAired);
					Format formatter = SimpleDateFormat.getDateInstance();
					firstAired = formatter.format(epDate);
				} catch (ParseException e) {
					Log.e(DroidShows.TAG, e.getMessage());
				}
			} else {
				firstAired = "";
			}
	
			int seasonNumber = c.getInt(seasonNumberCol);
			int episodeNumber = c.getInt(episodeNumberCol);
			final String episodeName = c.getString(enameCol);
			String overview = c.getString(overviewCol);
			String rating = c.getString(ratingCol);
			final String imdbId = c.getString(imdbIdCol);
			c.close();
	
			setTitle(serieName +" "+ seasonNumber +"x"+ episodeNumber);				
			
			TextView episodeNameV = (TextView) findViewById(R.id.episodeName);
			episodeNameV.setText(episodeName);
			
			TextView ratingV = (TextView) findViewById(R.id.rating);
			if (!rating.equalsIgnoreCase("null") && !rating.equals(""))
				ratingV.setText("IMDb: "+ rating);
			else
				ratingV.setText("IMDb Info");
			ratingV.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String uri = "imdb:///";
					Intent testForApp = new Intent(Intent.ACTION_VIEW, Uri.parse("imdb:///find"));
					if (getApplicationContext().getPackageManager().resolveActivity(testForApp, 0) == null)
						uri = "http://m.imdb.com/";
					if (imdbId.indexOf("tt") == 0) {
						uri += "title/"+ imdbId;
					} else {
						uri += "find?q="+ serieName.replaceAll(" \\(....\\)", "") +" "+ episodeName;
					}
					Intent imdb = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					startActivity(imdb);
				}
			});
			
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

			Cursor cwriters = DroidShows.db.Query("SELECT writer FROM writers WHERE episodeId='" + episodeId
				+"' AND serieId='"+ serieId +"'");
			cwriters.moveToFirst();
			if (cwriters != null && cwriters.isFirst()) {
				do {
					writers.add(cwriters.getString(0));
				} while (cwriters.moveToNext());
			}
			cwriters.close();
			if (!writers.isEmpty()) {
				TextView writersV = (TextView) findViewById(R.id.writer);
				writersV.setText(writers.toString().replace("]", "").replace("[", ""));
				writersV.setOnTouchListener(swipeDetect);
				View writerField = (View) findViewById(R.id.writerField);
				writerField.setOnTouchListener(swipeDetect);
				writerField.setVisibility(View.VISIBLE);
			}
			
			Cursor cdirectors = DroidShows.db.Query("SELECT director FROM directors WHERE episodeId='"+ episodeId
				+"' AND serieId='"+ serieId +"'");
			cdirectors.moveToFirst();
			if (cdirectors != null && cdirectors.isFirst()) {
				do {
					directors.add(cdirectors.getString(0));
				} while (cdirectors.moveToNext());
			}
			cdirectors.close();
			if (!directors.isEmpty()) {
				TextView directorsV = (TextView) findViewById(R.id.director);
				directorsV.setText(directors.toString().replace("]", "").replace("[", ""));
				directorsV.setOnTouchListener(swipeDetect);
				View directorField = (View) findViewById(R.id.directorField);
				directorField.setOnTouchListener(swipeDetect);
				directorField.setVisibility(View.VISIBLE);
			}
	
			Cursor cgs = DroidShows.db.Query("SELECT guestStar FROM guestStars WHERE episodeId='"+ episodeId
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
				guestStarsV.setOnTouchListener(swipeDetect);
				View guestStarsField = (View) findViewById(R.id.guestStarsField);
				guestStarsField.setOnTouchListener(swipeDetect);
				guestStarsField.setVisibility(View.VISIBLE);
			}
		}
	}
	
	public void IMDbNames(View v) {
		final List<String> names;
		int id = v.getId();
		if (id == R.id.writer || id == R.id.writerField)
			names = writers;
		else if (id == R.id.director || id == R.id.directorField)
			names = directors;
		else if (id == R.id.guestStars || id == R.id.guestStarsField)
			names = guestStars;
		else
			return;

		AlertDialog namesList = new AlertDialog.Builder(this)
			.setTitle(R.string.menu_context_view_imdb)
			.setItems(names.toArray(new CharSequence[names.size()]), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					Intent imdb = new Intent(Intent.ACTION_VIEW, Uri.parse(uri +"find?q="+ names.get(item)));
					startActivity(imdb);
				}
			})
			.show();
		namesList.setCanceledOnTouchOutside(true);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.left_enter, R.anim.left_exit);
	}
}