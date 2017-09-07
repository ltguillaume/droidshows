package nl.asymmetrics.droidshows.ui;

import nl.asymmetrics.droidshows.DroidShows;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class BounceListView extends ListView {
	private static final int MAX_OVERSCROLL_DISTANCE = 70;
	private static final int MIN_OVERSCROLL_DISTANCE = 68;
	private int maxOverscrollDistance;
	private int minOverscrollDistance;
	private Context context;
	private boolean allowOverscroll = false;
	private boolean abortUpdate = false;
	public boolean updating = false;

	public BounceListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		final float density = context.getResources().getDisplayMetrics().density;
		maxOverscrollDistance = (int) (density * MAX_OVERSCROLL_DISTANCE);
		minOverscrollDistance = (int) (density * MIN_OVERSCROLL_DISTANCE);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				allowOverscroll = (getChildCount() > 0 && getChildAt(0).getTop() == 0 && getFirstVisiblePosition() == 0);
				abortUpdate = true;
				break;
			case MotionEvent.ACTION_MOVE:
				if (-getScrollY() < minOverscrollDistance)
					abortUpdate = true;
				break;
			case MotionEvent.ACTION_UP:
				abortUpdate = true;
		}
		
		return super.onTouchEvent(event);
	}
	
	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
//		if (!clampedY)	// Not working for Gingerbread, getScrollY() does
//			overScrollCanceled = true;
		
		if (!updating && -scrollY == maxOverscrollDistance) {
			abortUpdate = false;
			updating = true;
			this.postDelayed(new Runnable() {
				public void run() {
					if (abortUpdate)
						updating = false;
					else
						((DroidShows)context).updateAllSeries();
				}
			}, 1000);
		}
		
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
	}

	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		if (allowOverscroll) {
			return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverscrollDistance, isTouchEvent);
		} else {
			return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
		}
	}

}