package nl.asymmetrics.droidshows.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nl.asymmetrics.droidshows.R;
import nl.asymmetrics.droidshows.utils.SQLiteStore;
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
import android.widget.CheckBox;
import android.widget.TextView;

public class ViewEpisode extends Activity
{
	private String episodeName = "",
			serieName = "",
			serieId = "",
			episodeId = "",
			imdbId = "",
			uri = "imdb:///";
	private int seen = 0;
	private List<String> writers = new ArrayList<String>();
	private List<String> directors = new ArrayList<String>();
	private List<String> guestStars = new ArrayList<String>();
	private SQLiteStore db;
	private SimpleDateFormat sdfseen = new SimpleDateFormat("yyyyMMdd");
	private SwipeDetect swipeDetect = new SwipeDetect();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.overridePendingTransition(R.anim.right_enter, R.anim.right_exit);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_episode);
		db = SQLiteStore.getInstance(this);
		View view = findViewById(R.id.viewEpisodes);
		view.setOnTouchListener(swipeDetect);
		serieId = getIntent().getStringExtra("serieId");
		serieName = getIntent().getStringExtra("serieName");
		episodeId = getIntent().getStringExtra("episodeId");
		
		String query = "SELECT seasonNumber, episodeNumber, episodeName, overview, rating, firstAired, imdbId, seen FROM episodes "
			+ "WHERE id = '"+ episodeId +"' AND serieId='"+ serieId +"'";
		Cursor c = db.Query(query);
		c.moveToFirst();
		if (c != null && c.isFirst()) {
			int seasonNumberCol = c.getColumnIndex("seasonNumber");
			int episodeNumberCol = c.getColumnIndex("episodeNumber");
			int enameCol = c.getColumnIndex("episodeName");
			int overviewCol = c.getColumnIndex("overview");
			int ratingCol = c.getColumnIndex("rating");
			int airedCol = c.getColumnIndex("firstAired");
			int imdbIdCol = c.getColumnIndex("imdbId");
			int seenCol = c.getColumnIndex("seen");
	
			String firstAired = c.getString(airedCol);
			if (!firstAired.equals("") && !firstAired.equals("null")) {
				try {
					Date epDate = SQLiteStore.dateFormat.parse(firstAired);
					firstAired = SimpleDateFormat.getDateInstance().format(epDate);
				} catch (ParseException e) {
					Log.e(SQLiteStore.TAG, e.getMessage());
				}
			} else {
				firstAired = "";
			}
	
			int seasonNumber = c.getInt(seasonNumberCol);
			int episodeNumber = c.getInt(episodeNumberCol);
			episodeName = c.getString(enameCol);
			String overview = c.getString(overviewCol);
			String rating = c.getString(ratingCol);
			imdbId = c.getString(imdbIdCol);
			seen = c.getInt(seenCol);
			c.close();
			
			setTitle(serieName +" - "
					+ (getString(R.string.messages_ep).isEmpty() ? "" : getString(R.string.messages_ep) +" ")
					+ seasonNumber +"x"+ (episodeNumber < 10 ? "0" : "") + episodeNumber);				
			
			TextView episodeNameV = (TextView) findViewById(R.id.episodeName);
			episodeNameV.setText(episodeName);
			
			TextView ratingV = (TextView) findViewById(R.id.rating);
			if (!rating.equalsIgnoreCase("null") && !rating.equals(""))
				ratingV.setText("IMDb: "+ rating);
			else
				ratingV.setText("IMDb Info");
			ratingV.setOnTouchListener(swipeDetect);
			
			CheckBox seenCheckBox = (CheckBox) findViewById(R.id.seen);
			TextView seenDate = (TextView) findViewById(R.id.seendate);
			seenCheckBox.setChecked(seen > 0);
			if (seen > 1)	// If seen value is a date
				try {
					seenDate.setTextColor(seenDate.getTextColors().getDefaultColor());
					seenDate.setText(SimpleDateFormat.getDateInstance().format(sdfseen.parse(seen +"")));
				} catch (ParseException e) { Log.e(SQLiteStore.TAG, e.getMessage()); }
			else
				seenDate.setText("");

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
			
			Cursor cwriters = db.Query("SELECT writer FROM writers WHERE episodeId='" + episodeId
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
			
			Cursor cdirectors = db.Query("SELECT director FROM directors WHERE episodeId='"+ episodeId
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
	
			Cursor cgs = db.Query("SELECT guestStar FROM guestStars WHERE episodeId='"+ episodeId
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

		Intent testForApp = new Intent(Intent.ACTION_VIEW, Uri.parse("imdb:///find"));
		if (getApplicationContext().getPackageManager().resolveActivity(testForApp, 0) == null)
			uri = "http://m.imdb.com/";
	}
	
	public void IMDbDetails(View v) {
		if (swipeDetect.value != 0) return;
		String uri = this.uri;
		if (imdbId.startsWith("tt")) {
			uri += "title/"+ imdbId;
		} else {
			uri += "find?q="+ serieName.replaceAll(" \\(....\\)", "") +" "+ episodeName;
		}
		Intent imdb = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		startActivity(imdb);
	}

	public void IMDbNames(View v) {
		if (swipeDetect.value != 0) return;
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

		new AlertDialog.Builder(this)
			.setTitle(R.string.menu_context_view_imdb)
			.setItems(names.toArray(new CharSequence[names.size()]), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					Intent imdb = new Intent(Intent.ACTION_VIEW, Uri.parse(uri +"find?q="+ names.get(item)));
					startActivity(imdb);
				}
			})
			.show();
	}
	
	public void check(View v) {
		if (v != null) {
			CheckBox c = (CheckBox) findViewById(R.id.seen);
			TextView d = (TextView) findViewById(R.id.seendate);
			if (c.isChecked()) {
				d.setTextColor(getResources().getColor(android.R.color.white));
				Calendar cal = Calendar.getInstance();
				seen = 10000 * cal.get(Calendar.YEAR) + 100 * (cal.get(Calendar.MONTH) +1) + cal.get(Calendar.DAY_OF_MONTH);
				try { d.setText(SimpleDateFormat.getDateInstance().format(sdfseen.parse(seen +"")));
				} catch (ParseException e) { e.printStackTrace(); }
			} else {
				d.setText("");
				seen = 0;
			}
		}
		db.updateUnwatchedEpisode(serieId, episodeId, seen);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.left_enter, R.anim.left_exit);
	}
}