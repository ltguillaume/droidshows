package nl.asymmetrics.droidshows.ui;

import nl.asymmetrics.droidshows.DroidShows;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class BounceListView extends ListView {
	private static final int MAX_OVERSCROLL_DISTANCE = 70;
	private static final int MIN_OVERSCROLL_DISTANCE = 60;
	private int maxOverScrollDistance;
	private int minOverScrollDistance;
	private Context context;
	private float startY;
	private boolean allowOverScroll = false;
	private boolean abortUpdate = true;
	public boolean updating = false;
	public boolean gettingNextLogged = false;

	public BounceListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		final float density = context.getResources().getDisplayMetrics().density;
		maxOverScrollDistance = (int) density * MAX_OVERSCROLL_DISTANCE;
		minOverScrollDistance = (int) density * MIN_OVERSCROLL_DISTANCE;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				abortUpdate = true;
				startY = event.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				if (DroidShows.logMode)
					allowOverScroll = event.getY() < startY && getAdapter().getCount() - 1 == getLastVisiblePosition();
				else
					allowOverScroll = event.getY() > startY && getChildCount() > 0 && getChildAt(0).getTop() == 0 && getFirstVisiblePosition() == 0;
				break;
			case MotionEvent.ACTION_UP:
				allowOverScroll = false;
				abortUpdate = true;
		}
		return super.onTouchEvent(event);
	}
	
	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
		if (DroidShows.logMode && !gettingNextLogged && allowOverScroll) {
			gettingNextLogged = true;
			((DroidShows)context).getNextLogged();
		} else {
			if (!updating && -scrollY > minOverScrollDistance) {
				abortUpdate = false;
				updating = true;
				this.postDelayed(startUpdate, 500);
			}
		}
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
	}

	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		if (-scrollY <= minOverScrollDistance)
			abortUpdate = true;
		if (allowOverScroll)
			maxOverScrollY = maxOverScrollDistance;
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
	}

	private Runnable startUpdate = new Runnable() {
		public void run() {
			if (!abortUpdate)
				((DroidShows)context).updateAllSeries(DroidShows.showArchive);
			updating = false;
		}
	};
}