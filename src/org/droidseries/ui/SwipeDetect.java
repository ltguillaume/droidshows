package org.droidseries.ui;

import org.droidseries.droidseries;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;

public class SwipeDetect implements View.OnTouchListener {
	private float onDownX;
	private boolean swipeDetected = false;
	
	public boolean detected() {
		return swipeDetected;
	}

	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				onDownX = event.getX();
				swipeDetected = false;
				break;
			case MotionEvent.ACTION_MOVE:
				float deltaX = onDownX - event.getX();
				if (deltaX > v.getWidth() / 3) {	// > 0 = right-to-left
					if (v.getContext() instanceof droidseries)
						swipeDetected = true;	// mark next episode seen
					else if (droidseries.switchSwipeDirection || v.getContext() instanceof ViewSerie)
						((Activity)v.getContext()).onBackPressed();
					return true;
				} else if (!droidseries.switchSwipeDirection && -deltaX > v.getWidth() / 3) {
						((Activity)v.getContext()).onBackPressed();	// left-to-right: go back
				}
				break;
		}
		return false;
	}
}