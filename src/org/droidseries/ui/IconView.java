package org.droidseries.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class IconView extends ImageView {

	public IconView (Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int height = getMeasuredHeight();
		int width = (int) (height * .75);
		setMeasuredDimension(width + getPaddingRight(), height);
	}
}