package org.droidseries.ui;

import org.droidseries.R;
import org.droidseries.droidseries;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ViewEpisode extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_episode);
		View view = findViewById(R.id.viewEpisodes);
		view.setOnTouchListener(new SwipeDetect());
		
		String serieName =  getIntent().getStringExtra("serieName");
		int seasonNumber = getIntent().getIntExtra("seasonNumber", 0);
		int episodeNumber = getIntent().getIntExtra("episodeNumber", 0);
		setTitle(serieName +" "+ seasonNumber +"x"+ episodeNumber);

		TextView episodeName = (TextView) findViewById(R.id.episodeName);
		episodeName.setText(getIntent().getStringExtra("episodeName"));
		
		TextView rating = (TextView) findViewById(R.id.rating);
		String ratingValue = getIntent().getStringExtra("rating");
		if (!ratingValue.equalsIgnoreCase("null") && !ratingValue.equals("")) {
			rating.setText("IMDb: "+ ratingValue);
			rating.setVisibility(View.VISIBLE);
		}
		
		TextView firstAired = (TextView) findViewById(R.id.firstAired);
		String firstAiredValue = getIntent().getStringExtra("firstAired");
		if (!firstAiredValue.equalsIgnoreCase("null") && !firstAiredValue.equals("")) {
			firstAired.setText(firstAiredValue);
			firstAired.setVisibility(View.VISIBLE);
		}

		TextView overview = (TextView) findViewById(R.id.overview);
		String overviewText = getIntent().getStringExtra("overview");
		if (!overviewText.equalsIgnoreCase("null") && !overviewText.equals("")) {
			overview.setText(overviewText);
			findViewById(R.id.overviewField).setVisibility(LinearLayout.VISIBLE);
		}
		
		TextView writer = (TextView) findViewById(R.id.writer);
		String writerText = getIntent().getStringExtra("writer");
		if (!writerText.equalsIgnoreCase("null") && !writerText.equals("")) {
			writer.setText(writerText);
			findViewById(R.id.writerField).setVisibility(LinearLayout.VISIBLE);
		}

		TextView director = (TextView) findViewById(R.id.director);
		String directorText = getIntent().getStringExtra("director");
		if (!directorText.equalsIgnoreCase("null") && !directorText.equals("")) {
			director.setText(directorText);
			findViewById(R.id.directorField).setVisibility(LinearLayout.VISIBLE);
		}
		
		TextView guestStars = (TextView) findViewById(R.id.guestStars);
		String guestStarsText = getIntent().getStringExtra("guestStars");
		if (!guestStarsText.equalsIgnoreCase("null") && !guestStarsText.equals("")) {
			guestStars.setText(guestStarsText);
			findViewById(R.id.guestStarsField).setVisibility(LinearLayout.VISIBLE);
		}
	}
}