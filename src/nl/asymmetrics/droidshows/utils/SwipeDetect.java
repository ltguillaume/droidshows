package nl.asymmetrics.droidshows.utils;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import nl.asymmetrics.droidshows.DroidShows;
import nl.asymmetrics.droidshows.ui.ViewSerie;

public class SwipeDetect implements View.OnTouchListener {
	private int vWidth, onDownX, onDownY;
	public int value = 0;
	
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				vWidth = v.getWidth();
				onDownX = (int) event.getX();
				onDownY = (int) event.getY();
				value = 0;
				break;
			case MotionEvent.ACTION_MOVE:
				int deltaX = onDownX - (int) event.getX(),
					deltaY = onDownY - (int) event.getY();
				int deltaXabs = deltaX > 0 ? deltaX : -deltaX,
					deltaYabs = deltaY > 0 ? deltaY : -deltaY;
				
				if (value >= 0 && onDownX != 0 && deltaXabs > deltaYabs * 2) {
					if (deltaXabs < vWidth / 4)
						return true;
					
					if (deltaX > 0) {	// > 0 = RTL
						if (v.getContext() instanceof DroidShows) {
							value = 1;	// mark next episode seen
						} else if (DroidShows.switchSwipeDirection || v.getContext() instanceof ViewSerie) {
							((Activity)v.getContext()).onBackPressed();
							value = -1;
						}
//						value = -1;	// Don't fire more than once
					} else if (!DroidShows.switchSwipeDirection) {	// < 0 = LTR
						((Activity)v.getContext()).onBackPressed();
						value = -1;
					}
					
					return true;
				}
		}
		return false;
	}
}