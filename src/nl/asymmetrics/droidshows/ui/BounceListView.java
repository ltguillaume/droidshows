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
	private float startY;

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
				abortUpdate = true;
				startY = event.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				if (DroidShows.logMode) {
					allowOverscroll = (event.getY() > startY && getChildCount() > 0 && getChildAt(getChildCount() - 1).getBottom() == getBottom());
				} else {
					if (-getScrollY() < minOverscrollDistance)
					abortUpdate = true;
					allowOverscroll = (event.getY() > startY && getChildCount() > 0 && getChildAt(0).getTop() == 0 && getFirstVisiblePosition() == 0);
				}

				break;
			case MotionEvent.ACTION_UP:
				abortUpdate = true;
		}
		
		return super.onTouchEvent(event);
	}
	
	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
		if (DroidShows.logMode) {
			((DroidShows)context).getNextLogged();
		} else {
			if (!updating && -scrollY == maxOverscrollDistance) {
				abortUpdate = false;
				updating = true;
				this.postDelayed(startUpdate, 1000);
			}
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

	private Runnable startUpdate = new Runnable() {
		public void run() {
			if (abortUpdate)
				updating = false;
			else
				((DroidShows)context).updateAllSeries();
		}
	};
}