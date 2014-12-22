package org.droidseries.ui;

import org.droidseries.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

public class SerieOverview extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv = new TextView(this);
		final int padding = (int) (9 * (getApplicationContext().getResources().getDisplayMetrics().densityDpi / 160f));
		tv.setPadding(padding, padding, padding, padding);
		ScrollView sv = new ScrollView(this);
		sv.setOnTouchListener(new SwipeDetect());
		try {
			setTitle(getIntent().getStringExtra("name") + " - " + getString(R.string.messages_overview));
			tv.setText(getIntent().getStringExtra("overview"));
		} catch (Exception e) {
		}
		sv.addView(tv);
		setContentView(sv);
	}
}