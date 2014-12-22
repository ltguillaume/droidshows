package org.droidseries.ui;

import org.droidseries.R;
import android.app.Activity;
import android.os.Bundle;
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
		TextView episodename = (TextView) findViewById(R.id.episodename);
		episodename.setText(getIntent().getStringExtra("episodename"));
		setTitle(getIntent().getStringExtra("episodename"));
		TextView episodeoverview = (TextView) findViewById(R.id.episodeoverview);
		episodeoverview.setText((getIntent().getStringExtra("episodeoverview").equals("null") ? "" : getIntent().getStringExtra("episodeoverview")));
		TextView episodedirector = (TextView) findViewById(R.id.episodedirector);
		String tmpDirector = getIntent().getStringExtra("episodedirector");
		episodedirector.setText(getString(R.string.episode_directors) + " " + tmpDirector);
		TextView episodewriter = (TextView) findViewById(R.id.episodewriter);
		String tmpWriters = getIntent().getStringExtra("episodewriter");
		episodewriter.setText(getString(R.string.episode_writers) + " " + tmpWriters);
		TextView episodegueststars = (TextView) findViewById(R.id.episodegueststars);
		String tmpEGS = getIntent().getStringExtra("episodegueststars");
		episodegueststars.setText(getString(R.string.episode_guest_stars) + " " + tmpEGS);
		TextView episoderating = (TextView) findViewById(R.id.episoderating);
		episoderating.setText(getString(R.string.series_rating) + " " + (getIntent().getStringExtra("episoderating").equals("null") ? "" : getIntent().getStringExtra("episoderating")));
		TextView episodefirstaired = (TextView) findViewById(R.id.firstaired);
		episodefirstaired.setText(getString(R.string.messages_aired) + " " + (getIntent().getStringExtra("episodefirstaired").equals("null") ? "" : getIntent().getStringExtra("episodefirstaired")));
	}
}