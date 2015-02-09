package org.droidseries.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class IconView extends ImageView {

	public IconView (Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Drawable d = getDrawable();
		int width = widthMeasureSpec;
		if (d != null) {
			int height = MeasureSpec.getSize(heightMeasureSpec);
			width = (int) Math.ceil((float) height / d.getIntrinsicHeight() * d.getIntrinsicWidth());
			setMeasuredDimension(width, height);
		}
		super.onMeasure(width, heightMeasureSpec);
	}
}