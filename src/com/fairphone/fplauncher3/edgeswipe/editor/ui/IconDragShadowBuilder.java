package com.fairphone.fplauncher3.edgeswipe.editor.ui;

import com.fairphone.fplauncher3.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;

public class IconDragShadowBuilder extends View.DragShadowBuilder {

	private Drawable mIcon;
	private Context mContext;

	public IconDragShadowBuilder(Context context, View v, Drawable icon) {
		super(v);

		// Creates a draggable image that will fill the Canvas provided by
		// the system.
		// shadow = new ColorDrawable(Color.LTGRAY);
		mIcon = icon;
		mContext = context;
	}

	@Override
	public void onProvideShadowMetrics(Point size, Point touch) {
		// Defines local variables
		Resources r = mContext.getResources();
		float px = r.getDimension(R.dimen.edit_favorites_icon_size);

		int width = Math.round(px * 1.15f);
		int height = Math.round(px * 1.15f);

		// The drag shadow is a ColorDrawable. This sets its dimensions to
		// be the same as the
		// Canvas that the system will provide. As a result, the drag shadow
		// will fill the
		// Canvas.
		mIcon.setBounds(0, 0, width, height);

		// Sets the size parameter's width and height values. These get back
		// to the system
		// through the size parameter.
		size.set(width, height);

		// Sets the touch point's position to be in the middle of the drag
		// shadow
		touch.set(width / 2, height);
	}

	// Defines a callback that draws the drag shadow in a Canvas that the
	// system constructs
	// from the dimensions passed in onProvideShadowMetrics().
	@Override
	public void onDrawShadow(Canvas canvas) {

		// Draws the ColorDrawable in the Canvas passed in from the system.
		// shadow.draw(canvas);
		mIcon.draw(canvas);
	}

}
