package nl.asymmetrics.droidshows.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class IconView extends ImageView {

	public IconView (Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int newWidth = (int) (.64 * ((View) getParent()).getMeasuredHeight());
		super.onMeasure(MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
	}
}