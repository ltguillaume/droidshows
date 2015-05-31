package nl.asymmetrics.droidshows.ui;

import nl.asymmetrics.droidshows.DroidShows;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;

public class SwipeDetect implements View.OnTouchListener {
	private float onDownX, onDownY;
	private boolean swipeDetected = false;
	
	public boolean detected() {
		return swipeDetected;
	}

	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				onDownX = event.getX();
				onDownY = event.getY();
				swipeDetected = false;
				break;
			case MotionEvent.ACTION_MOVE:
				float deltaX = onDownX - event.getX();
				float deltaY = onDownY - event.getY();
				if (Math.abs(deltaX / 3) > Math.abs(deltaY)) {
					if (deltaX > v.getWidth() / 3) {	// > 0 = right-to-left
						if (v.getContext() instanceof DroidShows) {
							swipeDetected = true;	// mark next episode seen
							return true;
						}
						else if (DroidShows.switchSwipeDirection || v.getContext() instanceof ViewSerie)
							((Activity)v.getContext()).onBackPressed();
					} else if (!DroidShows.switchSwipeDirection && -deltaX > v.getWidth() / 3) {
							((Activity)v.getContext()).onBackPressed();	// left-to-right: go back
					}
				}
				break;
		}
		return false;
	}
}