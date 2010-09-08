package org.droidseries.ui;

import org.droidseries.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ViewEpisode extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_episode);
		
		TextView episodename = (TextView) findViewById(R.id.episodename);
		episodename.setText(getIntent().getStringExtra("episodename"));
		setTitle(getIntent().getStringExtra("episodename"));
		
		TextView episodeoverview = (TextView) findViewById(R.id.episodeoverview);
		episodeoverview.setText(getIntent().getStringExtra("episodeoverview"));
		
		TextView episodedirector = (TextView) findViewById(R.id.episodedirector);
		String tmpDirector = getIntent().getStringExtra("episodedirector");
		if(!tmpDirector.equals("")){
			episodedirector.setText(getString(R.string.episode_directors) + " " + tmpDirector);
		}
		else {
			episodedirector.setText(getString(R.string.episode_directors) + " " + getString(R.string.messages_unknown));
		}
		
		TextView episodewriter = (TextView) findViewById(R.id.episodewriter);
		String tmpWriters = getIntent().getStringExtra("episodewriter");
		if(!tmpWriters.equals("")) {
			episodewriter.setText(getString(R.string.episode_writers) + " " + tmpWriters);
		}
		else {
			episodewriter.setText(getString(R.string.episode_writers) + " " + getString(R.string.messages_unknown));
		}
		
		TextView episodegueststars = (TextView) findViewById(R.id.episodegueststars);
		String tmpEGS = getIntent().getStringExtra("episodegueststars");
		if (!tmpEGS.equals("")) {
			episodegueststars.setText(getString(R.string.episode_guest_stars) + " " + tmpEGS);
		}
		else {
			episodegueststars.setText(getString(R.string.episode_no_guest_stars)); 
		}
		
		TextView episoderating = (TextView) findViewById(R.id.episoderating);
		episoderating.setText(getIntent().getStringExtra("episoderating"));
		
		TextView episodefirstaired = (TextView) findViewById(R.id.firstaired);
		episodefirstaired.setText(getIntent().getStringExtra("episodefirstaired"));
	}
	
}
