package org.droidseries.ui;

import org.droidseries.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ViewSerie extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_serie);
		
		TextView seriename = (TextView) findViewById(R.id.seriename);
		seriename.setText(getIntent().getStringExtra("seriename"));
		setTitle(getIntent().getStringExtra("seriename") + " - Overview");
		
		TextView serieoverview = (TextView) findViewById(R.id.serieoverview);
		serieoverview.setText(getIntent().getStringExtra("serieoverview"));
		
		//firstaired
		TextView firstaired = (TextView) findViewById(R.id.firstaired);
		String tmpFA = getIntent().getStringExtra("firstaired");
		if(!tmpFA.equals("")) {
			firstaired.setText(getString(R.string.series_first_aired) + " " + tmpFA);
		}
		else {
			firstaired.setText(getString(R.string.series_first_aired) + " " + getString(R.string.messages_unknown));
		}
		
		//airday
		TextView airday = (TextView) findViewById(R.id.airday);
		String tmpAirday = getIntent().getStringExtra("airday");
		if(!tmpAirday.equals("")) {
			airday.setText(getString(R.string.series_air_day) + " " + tmpAirday);
		}
		else {
			airday.setText(getString(R.string.series_air_day) + " " + getString(R.string.messages_unknown));
		}
		
		//airtime
		TextView airtime = (TextView) findViewById(R.id.airtime);
		String tmpAirtime = getIntent().getStringExtra("airtime");
		if(!tmpAirtime.equals("")){
			airtime.setText(getString(R.string.series_air_time) + " " + tmpAirtime);
		}
		else {
			airtime.setText(getString(R.string.series_air_time) + " " + getString(R.string.messages_unknown));
		}
		
		
		//runtime
		TextView runtime = (TextView) findViewById(R.id.runtime);
		String tmpRuntime = getIntent().getStringExtra("runtime");
		if(!tmpRuntime.equals("")) {
			runtime.setText( String.format(getString(R.string.series_runtime_minutes), tmpRuntime) );
		}
		else {
			runtime.setText(getString(R.string.series_runtime) + " " + getString(R.string.messages_unknown));
		}
		
		//network
		TextView network = (TextView) findViewById(R.id.network);
		String tmpNetwork = getIntent().getStringExtra("network");
		if(!tmpNetwork.equals("")) {
			network.setText(getString(R.string.series_network) + " " + tmpNetwork);
		}
		else {
			network.setText(getString(R.string.series_network) + " " + getString(R.string.messages_unknown));
		}
		
		//genre
		TextView genre = (TextView) findViewById(R.id.genre);
		String tmpGenre = getIntent().getStringExtra("genre");
		if(!tmpGenre.equals("")) {
			genre.setText(getString(R.string.series_genre) + " " + tmpGenre);
		}
		else {
			genre.setText(getString(R.string.series_genre) + " " + getString(R.string.messages_unknown));
		}
		
		//rating
		TextView rating = (TextView) findViewById(R.id.rating);
		String tmpRating = getIntent().getStringExtra("rating");
		if(!tmpRating.equals("")) {
			rating.setText(getString(R.string.series_rating) + " " + tmpRating);
		}
		else {
			rating.setText(getString(R.string.series_rating) + " " + getString(R.string.messages_unknown));
		}
		
		//serieactors
		TextView serieactors = (TextView) findViewById(R.id.serieactors);
		String tmpActors = getIntent().getStringExtra("serieactors");
		
		if(!tmpActors.equals("")) {
			serieactors.setText(getString(R.string.series_actors) + " " + tmpActors);
		}
		else {
			serieactors.setText(getString(R.string.series_actors) + " " + getString(R.string.messages_unknown));
		}
		
	}
}