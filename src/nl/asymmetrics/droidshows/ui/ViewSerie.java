package nl.asymmetrics.droidshows.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;

import java.text.ParseException;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewSerie extends Activity
{
	private String serieId = null,
		serieName = "",
		posterURL = "#",
		fanartURL = "#",
		imdbId = "";
	private WebView posterView = null;
	private boolean posterLoaded = false;
	private String uri = "imdb:///";
	private List<String> actors = new ArrayList<String>();
	private SQLiteStore db;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.overridePendingTransition(R.anim.left_enter, R.anim.left_exit);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_serie);
		db = SQLiteStore.getInstance(this);
		View view = findViewById(R.id.viewSerie);
		SwipeDetect swipeDetect = new SwipeDetect();
		view.setOnTouchListener(swipeDetect);
		serieId = getIntent().getStringExtra("serieId");
	
		String query = "SELECT serieName, posterThumb, poster, fanart, overview, status, firstAired, airsDayOfWeek, "
			+ "airsTime, runtime, network, rating, contentRating, imdbId FROM series WHERE id = '" + serieId + "'";
		Cursor c = db.Query(query);
		c.moveToFirst();
		if (c != null && c.isFirst()) {
			int snameCol = c.getColumnIndex("serieName");
			int posterThumbCol = c.getColumnIndex("posterThumb");
			int posterCol = c.getColumnIndex("poster");
			int fanartCol = c.getColumnIndex("fanart");
			int overviewCol = c.getColumnIndex("overview");
			int statusCol = c.getColumnIndex("status");
			int firstAiredCol = c.getColumnIndex("firstAired");
			int airsdayofweekCol = c.getColumnIndex("airsDayOfWeek");
			int airstimeCol = c.getColumnIndex("airsTime");
			int runtimeCol = c.getColumnIndex("runtime");
			int networkCol = c.getColumnIndex("network");
			int ratingCol = c.getColumnIndex("rating");
			int contentRatingCol = c.getColumnIndex("contentRating");
			int imdbIdCol = c.getColumnIndex("imdbId");
			serieName = c.getString(snameCol);
			String posterThumb = c.getString(posterThumbCol);
			posterURL = c.getString(posterCol);
			fanartURL = c.getString(fanartCol);
			String serieOverview = c.getString(overviewCol);
			String status = c.getString(statusCol);
			String firstAired = c.getString(firstAiredCol);
			String airday = c.getString(airsdayofweekCol);
			String airtime = c.getString(airstimeCol);
			String runtime = c.getString(runtimeCol);
			String network = c.getString(networkCol);
			String rating = c.getString(ratingCol);
			String contentRating = c.getString(contentRatingCol);
			imdbId = c.getString(imdbIdCol);
			c.close();
					
			if (!network.equalsIgnoreCase("null")) {
				TextView networkV = (TextView) findViewById(R.id.network);
				if (network.endsWith("db")) network = network.substring(0, network.length()-2);
				networkV.setText(network);
			}
	
			if (!contentRating.equalsIgnoreCase("null")) {
				TextView contentRatingV = (TextView) findViewById(R.id.contentRating);
				contentRatingV.setText(contentRating);
			}
			
			TextView serieNameV = (TextView) findViewById(R.id.serieName);
			serieNameV.setText(serieName);
	
			ImageView posterThumbV = (ImageView) findViewById(R.id.posterThumb);
			try {
				BitmapDrawable posterThumbD = (BitmapDrawable) BitmapDrawable.createFromPath(posterThumb);
				posterThumbD.setTargetDensity(getResources().getDisplayMetrics().densityDpi);	// Don't auto-resize to screen density
				posterThumbV.setImageDrawable(posterThumbD);
			}
			catch (Exception e) {}
					
			List<String> genres = new ArrayList<String>();
			Cursor cgenres = db.Query("SELECT genre FROM genres WHERE serieId='"+ serieId + "'");
			cgenres.moveToFirst();
			if (cgenres != null && cgenres.isFirst()) {
				do {
					genres.add(cgenres.getString(0));
				} while (cgenres.moveToNext());
			}
			cgenres.close();
			if (!genres.isEmpty()) {
				TextView genreV = (TextView) findViewById(R.id.genre);
				genreV.setText(genres.toString().replace("]", "").replace("[", ""));
				genreV.setVisibility(View.VISIBLE);
			}

			TextView ratingV = (TextView) findViewById(R.id.rating);
			if (!rating.equalsIgnoreCase("null") && !rating.equals(""))
				ratingV.setText("IMDb: "+ rating);
			else
				ratingV.setText("IMDb Info");
					
			if (!firstAired.equals("null") && !firstAired.equals("")) {
				TextView firstAiredV = (TextView) findViewById(R.id.firstAired);
				try {
					Date epDate = SQLiteStore.dateFormat.parse(firstAired);
					firstAired = SimpleDateFormat.getDateInstance().format(epDate);
				} catch (ParseException e) {
					Log.e(SQLiteStore.TAG, e.getMessage());
				}
				if (!status.equalsIgnoreCase("null") && !status.equalsIgnoreCase(""))
					status = " ("+ translateStatus(status) +")";
				else
					status = "";
				firstAiredV.setText(firstAired + status);
				firstAiredV.setVisibility(View.VISIBLE);
			}
	
			if (!airday.equalsIgnoreCase("null") && !airday.equals("")) {
				TextView airtimeV = (TextView) findViewById(R.id.airtime);
				if (airday.equalsIgnoreCase("Daily"))
					airday = getString(R.string.messages_daily);
				if (!airtime.equalsIgnoreCase("null") && !airtime.equals("")) {
					try {
						Date epDate = SQLiteStore.dateFormat.parse(airtime);
						airtime = SimpleDateFormat.getDateInstance().format(epDate);
					} catch (ParseException e) {
						Log.e(SQLiteStore.TAG, e.getMessage());
					}
					airtimeV.setText(airday +" "+ getString(R.string.messages_at) +" "+ airtime);
					airtimeV.setVisibility(View.VISIBLE);
				}
			}
	
			if (!runtime.equalsIgnoreCase("null") && !runtime.equals("")) {
				TextView runtimeV = (TextView) findViewById(R.id.runtime);
				runtimeV.setText(runtime +" "+ getString(R.string.series_runtime_minutes));
				runtimeV.setVisibility(View.VISIBLE);
			}
			
			TextView serieOverviewV = (TextView) findViewById(R.id.serieOverview);
			serieOverviewV.setText(serieOverview);

			Cursor cactors = db.Query("SELECT actor FROM actors WHERE serieId='"+ serieId + "'");
			cactors.moveToFirst();
			if (cactors != null && cactors.isFirst()) {
				do {
					actors.add(cactors.getString(0));
				} while (cactors.moveToNext());
			}
			cactors.close();
			if (!actors.isEmpty()) {
				TextView serieActorsV = (TextView) findViewById(R.id.actors);
				serieActorsV.setText(actors.toString().replace("]", "").replace("[", ""));
				serieActorsV.setOnTouchListener(swipeDetect);
				View actorsField = (View) findViewById(R.id.actorsField);
				actorsField.setOnTouchListener(swipeDetect);
				actorsField.setVisibility(View.VISIBLE);
			}
		}
		
		Intent testForApp = new Intent(Intent.ACTION_VIEW, Uri.parse("imdb:///find"));
		if (getApplicationContext().getPackageManager().resolveActivity(testForApp, 0) == null)
			uri = "http://m.imdb.com/";

	}
	
	private String translateStatus(String statusValue) {
		if (statusValue.equalsIgnoreCase("Continuing")) {
			return getString(R.string.showstatus_continuing);
		} else if (statusValue.equalsIgnoreCase("Ended")) {
			return getString(R.string.showstatus_ended);
		} else {
			return statusValue;
		}
	}
	
	public void IMDbDetails(View v) {
		String uri = this.uri;
		if (imdbId.indexOf("tt") == 0) {
			uri += "title/"+ imdbId;
		} else {
			uri += "find?q="+ serieName;
		}
		Intent imdb = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		startActivity(imdb);
	}
	
	public void IMDbNames(View v) {
		AlertDialog namesList = new AlertDialog.Builder(this)
			.setTitle(R.string.menu_context_view_imdb)
			.setItems(actors.toArray(new CharSequence[actors.size()]), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					Intent imdb = new Intent(Intent.ACTION_VIEW, Uri.parse(uri +"find?q="+ actors.get(item)));
					startActivity(imdb);
				}
			})
			.show();
		namesList.setCanceledOnTouchOutside(true);
	}
	
	public void posterView(View v) {
		if (!posterLoaded) {
			posterView = (WebView) findViewById(R.id.posterView);
			if (posterURL.isEmpty() || posterURL.equalsIgnoreCase("null")) {
				if (!fanartURL.isEmpty() && !fanartURL.equalsIgnoreCase("null")) {
					posterURL = fanartURL;
				} else {
					return;
				}
			}
			if (fanartURL.isEmpty() || fanartURL.equalsIgnoreCase("null"))
				fanartURL = posterURL;

			posterView.getSettings().setBuiltInZoomControls(true);
			posterView.getSettings().setLoadWithOverviewMode(true);
			posterView.getSettings().setUseWideViewPort(true);
			posterView.loadData(getURL(posterURL, "ds:fanart"), "text/html", "UTF-8");
			posterView.setBackgroundColor(Color.BLACK);
			posterView.setInitialScale(1);
			posterView.setOverScrollMode(View.OVER_SCROLL_NEVER);
			posterView.setWebViewClient(new WebViewHandler());
			posterLoaded = true;
		}
		posterView.setVisibility(View.VISIBLE);
	}
	
	private class WebViewHandler extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView v, String url) {
			String img = posterURL, a = "ds:fanart";
			if (url.equals("ds:fanart")) {
				img = fanartURL;
				a = "ds:poster";
			}
			v.loadData(getURL(img, a), "text/html", "UTF-8");
			return true;
		}
	}
	
	private String getURL(String img, String a) {
		return "<html><head><meta name=\"viewport\" content=\"width=device-width,user-scalable=1\">"
			+ "<style>*{margin:0;padding:0}</style></head><body><a href=\""+ a +"\"><img src=\""+ img +"\"/></a></body></html>";
	}
	
	@Override
	public void onBackPressed() {
		if (posterView != null && posterView.getVisibility() == View.VISIBLE)
			posterView.setVisibility(View.GONE);
		else {
			super.onBackPressed();
			overridePendingTransition(R.anim.right_enter, R.anim.right_exit);
		}
	}
}