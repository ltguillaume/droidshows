package org.droidseries.ui;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.droidseries.droidseries;
import org.droidseries.R;
import android.app.Activity;
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.overridePendingTransition(R.anim.left_enter, R.anim.left_exit);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_serie);
		View view = findViewById(R.id.viewSerie);
		view.setOnTouchListener(new SwipeDetect());
		serieId = getIntent().getStringExtra("serieId");
	
		String query = "SELECT serieName, posterThumb, poster, fanart, overview, status, firstAired, airsDayOfWeek, "
			+ "airsTime, runtime, network, rating, contentRating, imdbId FROM series WHERE id = '" + serieId + "'";
		Cursor c = droidseries.db.Query(query);
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
					
			TextView networkV = (TextView) findViewById(R.id.network);
			if (!network.equalsIgnoreCase("null")) {
				if (network.endsWith("db")) network = network.substring(0, network.length()-2);
				networkV.setText(network);
			}
	
			TextView contentRatingV = (TextView) findViewById(R.id.contentRating);
			if (!contentRating.equalsIgnoreCase("null"))
				contentRatingV.setText(contentRating);
			
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
			Cursor cgenres = droidseries.db.Query("SELECT genre FROM genres WHERE serieId='"+ serieId + "'");
			cgenres.moveToFirst();
			if (cgenres != null && cgenres.isFirst()) {
				do {
					genres.add(cgenres.getString(0));
				} while (cgenres.moveToNext());
			}
			cgenres.close();
			TextView genreV = (TextView) findViewById(R.id.genre);
			genreV.setText(genres.toString().replace("]", "").replace("[", ""));

			TextView ratingV = (TextView) findViewById(R.id.rating);
			if (!rating.equalsIgnoreCase("null") && !rating.equals(""))
				ratingV.setText("IMDb: "+ rating);
			else
				ratingV.setText("IMDb Info");
					
			TextView firstAiredV = (TextView) findViewById(R.id.firstAired);
			if (!firstAired.equals("")) {
				try {
					SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
					Date epDate = SDF.parse(firstAired);
					Format formatter = SimpleDateFormat.getDateInstance();
					firstAired = formatter.format(epDate);
				} catch (ParseException e) {
					Log.e("DroidSeries", e.getMessage());
				}
			}
			if (!status.equalsIgnoreCase("null"))
				status = " ("+ translateStatus(status) +")";
			else
				status = "";
			firstAiredV.setText(firstAired + status);
	
			TextView airtimeV = (TextView) findViewById(R.id.airtime);
			if (!airday.equalsIgnoreCase("null") && !airday.equals("")) {
				if (airday.equalsIgnoreCase("Daily"))
					airday = getString(R.string.messages_daily);
				if (!airtime.equalsIgnoreCase("null") && !airtime.equals("")) {
					try {
						SimpleDateFormat SDF = new SimpleDateFormat("h:m a");
						Date epDate = SDF.parse(airtime);
						Format formatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
						airtime = formatter.format(epDate);
					} catch (ParseException e) {
						Log.e("DroidSeries", e.getMessage());
					}
					airtimeV.setVisibility(View.VISIBLE);
					airtimeV.setText(airday +" "+ getString(R.string.messages_at) +" "+ airtime);
				}
			}
	
			TextView runtimeV = (TextView) findViewById(R.id.runtime);
			runtimeV.setText(runtime +" "+ getString(R.string.series_runtime_minutes));
			
			TextView serieOverviewV = (TextView) findViewById(R.id.serieOverview);
			serieOverviewV.setText(serieOverview);

			List<String> actors = new ArrayList<String>();
			Cursor cactors = droidseries.db.Query("SELECT actor FROM actors WHERE serieId='"+ serieId + "'");
			cactors.moveToFirst();
			if (cactors != null && cactors.isFirst()) {
				do {
					actors.add(cactors.getString(0));
				} while (cactors.moveToNext());
			}
			cactors.close();
			if (!actors.isEmpty()) {
				TextView serieActorsV = (TextView) findViewById(R.id.serieActors);
				serieActorsV.setText(actors.toString().replace("]", "").replace("[", ""));
				findViewById(R.id.actorsField).setVisibility(View.VISIBLE);
			}
		}
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
		String uri = "imdb:///";
		Intent testForApp = new Intent(Intent.ACTION_VIEW, Uri.parse("imdb:///find"));
    if (getApplicationContext().getPackageManager().resolveActivity(testForApp, 0) == null)
    	uri = "http://m.imdb.com/";
		if (imdbId.indexOf("tt") == 0) {
			uri += "title/"+ imdbId;
		} else {
			uri += "find?q="+ serieName;
		}
		Intent imdb = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		startActivity(imdb);
	}
	
	public void posterView(View v) {
		if (!posterLoaded) {
			posterView = (WebView) findViewById(R.id.posterView);
			if (posterURL.isEmpty() || posterURL.equalsIgnoreCase("null")) {
				if (!fanartURL.isEmpty() && !fanartURL.equalsIgnoreCase("null")) {
					posterURL = fanartURL;
					fanartURL = "#";
				} else {
					return;
				}
			}
			if (fanartURL.isEmpty() || fanartURL.equalsIgnoreCase("null"))
				fanartURL = "#";

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
			if (url.equals("#")) {
				return false;
			} else {
				String img = posterURL, a = "ds:fanart";
				if (url.equals("ds:fanart")) {
					img = fanartURL;
					a = "ds:poster";
				}
				v.loadData(getURL(img, a), "text/html", "UTF-8");
				return true;
			}
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