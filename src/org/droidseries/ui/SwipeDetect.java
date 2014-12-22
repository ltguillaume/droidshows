package org.droidseries.ui;

import org.droidseries.droidseries;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class SwipeDetect implements View.OnTouchListener {
	private float downX;
	private boolean swipeDetected = false;
	
	public boolean detected() {
		return swipeDetected;
	}

	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				downX = event.getX();
				swipeDetected = false;
      }
			case MotionEvent.ACTION_MOVE: {
				float deltaX = downX - event.getX();
				if (deltaX > v.getWidth() / 3) {	// > 0 = right-to-left, minimum delta might need some tweaking
					if(v.getContext() instanceof droidseries) {
						swipeDetected = true;	// mark next episode seen
						return true;
					} else {
						((Activity)v.getContext()).finish();	// go back
					}
				}
			}
		}
		return false;
	}
}