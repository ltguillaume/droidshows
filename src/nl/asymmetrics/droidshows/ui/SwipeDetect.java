package nl.asymmetrics.droidshows.ui;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import nl.asymmetrics.droidshows.DroidShows;

public class SwipeDetect implements View.OnTouchListener {
	private int onDownX, onDownY;
	private boolean swipeDetected = false;
	
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				onDownX = (int) event.getX();
				onDownY = (int) event.getY();
				swipeDetected = false;
				break;
			case MotionEvent.ACTION_MOVE:
				int deltaX = onDownX - (int) event.getX();
				int deltaY = onDownY - (int) event.getY();
				if ((((deltaX < 0) ? -deltaX : deltaX) / 3) > ((deltaY < 0) ? -deltaY : deltaY)) {
					if (deltaX > v.getWidth() / 3) {	// > 0 = right-to-left
						if (v.getContext() instanceof DroidShows) {
							swipeDetected = true;	// mark next episode seen
						} else if (DroidShows.switchSwipeDirection || v.getContext() instanceof ViewSerie) {
							((Activity)v.getContext()).onBackPressed();
						}
						return true;
					} else if (!DroidShows.switchSwipeDirection && -deltaX > v.getWidth() / 3) {
						((Activity)v.getContext()).onBackPressed();	// left-to-right: go back
						return true;
					} else {
						v.performClick();
						return true;
					}
				}
		}
		return false;
	}
	
	public boolean detected() {
		return swipeDetected;
	}
}