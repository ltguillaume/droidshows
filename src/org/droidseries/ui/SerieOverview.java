package org.droidseries.ui;

import org.droidseries.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
//import android.view.Window;
import android.widget.ScrollView;
import android.widget.TextView;

public class SerieOverview extends Activity
{
	private final String TAG = "DroidSeries";
	private String serieid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv = new TextView(this);
		tv.setPadding(9, 9, 9, 9); // Guillaume (HDPI 6dip = 9px)
		ScrollView sv = new ScrollView(this);
		try {
			serieid = getIntent().getStringExtra("serieid");
			setTitle(getIntent().getStringExtra("name") + " - " + getString(R.string.messages_overview));
			tv.setText(getIntent().getStringExtra("overview"));
		} catch (Exception e) {
			Log.e(TAG, "Error getting the intent extra value.");
		}
		sv.addView(tv);
		setContentView(sv);
	}
}