package nl.asymmetrics.droidshows.ui;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import nl.asymmetrics.droidshows.DroidShows;

public class SwipeDetect implements View.OnTouchListener {
	private int onDownX, onDownY;
	public int value = 0;
	
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				onDownX = (int) event.getX();
				onDownY = (int) event.getY();
				value = 0;
				break;
			case MotionEvent.ACTION_MOVE:
				int deltaX = onDownX - (int) event.getX();
				int deltaY = onDownY - (int) event.getY();
				if (value < 99 && (((deltaX < 0) ? -deltaX : deltaX) / 3) > ((deltaY < 0) ? -deltaY : deltaY)) {
					if (deltaX > v.getWidth() / 3) {	// > 0 = RTL
						if (v.getContext() instanceof DroidShows) {
							value = 1;	// mark next episode seen
							return true;
						} else if (DroidShows.switchSwipeDirection || v.getContext() instanceof ViewSerie) {
							value = -1;	// no ListItemClick
							((Activity)v.getContext()).onBackPressed();
						}
						value = 99;	// Don't fire more than once
					} else if (!DroidShows.switchSwipeDirection && -deltaX > v.getWidth() / 3) {	// < 0 = LTR
						value = -1;	// no ListItemClick
						((Activity)v.getContext()).onBackPressed();
						value = 99;
					}
				}
		}
		return false;
	}
}