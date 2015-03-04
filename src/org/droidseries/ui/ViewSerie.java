package org.droidseries.ui;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.droidseries.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;

import java.text.ParseException;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewSerie extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_serie);
		View view = findViewById(R.id.viewSerie);
		view.setOnTouchListener(new SwipeDetect());
		
		TextView network = (TextView) findViewById(R.id.network);
		String networkText = getIntent().getStringExtra("network");
		if (!networkText.equalsIgnoreCase("null")) {
			if (networkText.endsWith("db")) networkText = networkText.substring(0, networkText.length()-2);
			network.setText(networkText);
		}

		TextView contentRating = (TextView) findViewById(R.id.contentRating);
		String contentRatingText = getIntent().getStringExtra("contentRating");
		if (!contentRatingText.equalsIgnoreCase("null"))
			contentRating.setText(contentRatingText);
		
		TextView serieName = (TextView) findViewById(R.id.serieName);
		serieName.setText(getIntent().getStringExtra("serieName"));

		ImageView poster = (ImageView) findViewById(R.id.poster);
		
		try {
			BitmapDrawable posterThumb = (BitmapDrawable) BitmapDrawable.createFromPath(getIntent().getStringExtra("poster"));
			posterThumb.setTargetDensity(getResources().getDisplayMetrics().densityDpi);	// Don't auto-resize from mdpi to screen density
			poster.setImageDrawable(posterThumb);
		}
		catch (Exception e) {
		}
		
		TextView genre = (TextView) findViewById(R.id.genre);
		genre.setText(getIntent().getStringExtra("genre"));
		
		TextView rating = (TextView) findViewById(R.id.rating);
		rating.setText("IMDb: "+ getIntent().getStringExtra("rating"));
				
		TextView firstAired = (TextView) findViewById(R.id.firstAired);
		String firstAiredValue = getIntent().getStringExtra("firstAired");
		if (!firstAiredValue.equals("")) {
			try {
				SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
				Date epDate = SDF.parse(firstAiredValue);
				Format formatter = SimpleDateFormat.getDateInstance();
				firstAiredValue = formatter.format(epDate);
			} catch (ParseException e) {
				Log.e("DroidSeries", e.getMessage());
			}
		}
		String statusValue = translateStatus(getIntent().getStringExtra("status"));
		if (!statusValue.isEmpty()) statusValue = " ("+ statusValue +")";
		firstAired.setText(firstAiredValue + statusValue);

		TextView airtime = (TextView) findViewById(R.id.airtime);
		String airdayValue = getIntent().getStringExtra("airday");
		String airtimeValue = getIntent().getStringExtra("airtime");
		if (!airdayValue.equalsIgnoreCase("null") && !airdayValue.equals("")) {
			if (airdayValue.equalsIgnoreCase("Daily"))
				airdayValue = getString(R.string.messages_daily);
			if (!airtimeValue.equalsIgnoreCase("null") && !airtimeValue.equals("")) {
				try {
					SimpleDateFormat SDF = new SimpleDateFormat("h:m a");
					Date epDate = SDF.parse(airtimeValue);
					Format formatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
					airtimeValue = formatter.format(epDate);
				} catch (ParseException e) {
					Log.e("DroidSeries", e.getMessage());
				}
				airtime.setVisibility(View.VISIBLE);
				airtime.setText(airdayValue +" "+ getString(R.string.messages_at) +" "+ airtimeValue);
			}
		}

		TextView runtime = (TextView) findViewById(R.id.runtime);
		runtime.setText(getIntent().getStringExtra("runtime") +" "+ getString(R.string.series_runtime_minutes));

		TextView serieActors = (TextView) findViewById(R.id.serieActors);
		serieActors.setText(getIntent().getStringExtra("serieActors"));
		
		TextView serieOverview = (TextView) findViewById(R.id.serieOverview);
		serieOverview.setText(getIntent().getStringExtra("serieOverview"));
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
		String imdbId = getIntent().getStringExtra("imdbId");
		String serieName = getIntent().getStringExtra("serieName");
		Intent imdb;
		if (imdbId.equalsIgnoreCase("null") || imdbId.equals("")) {
			imdb = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.imdb.com/find?q=" + serieName));
		} else {
			imdb = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.imdb.com/title/"+ imdbId));
		}
		startActivity(imdb);
	}
	
	public void posterView(View v) {
		Toast.makeText(getApplicationContext(), "Not yet implemented", Toast.LENGTH_SHORT).show();
	}

}