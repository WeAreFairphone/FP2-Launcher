package com.fairphone.fplauncher3.edgeswipe.editor.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.fairphone.fplauncher3.R;
import com.fairphone.fplauncher3.edgeswipe.editor.EditFavoritesActivity;

public class IconDragShadowBuilder extends View.DragShadowBuilder {

	private Drawable mIcon;
	private Context mContext;
	private int mOrigin;

	public IconDragShadowBuilder(Context context, View v, Drawable icon, int origin) {
		super(v);

		// Creates a draggable image that will fill the Canvas provided by
		// the system.
		// shadow = new ColorDrawable(Color.LTGRAY);
		mIcon = icon;
		mContext = context;
		mOrigin = origin;
	}

	@Override
	public void onProvideShadowMetrics(Point size, Point touch) {
		// Defines local variables
		Resources r = mContext.getResources();
		float px = r.getDimension(R.dimen.edit_favorites_icon_size);

		int iconWidth = Math.round(px * 1.25f);
		int iconHeight = Math.round(px * 1.25f);

		//Calculate the canvas size 
		int canvasSize = (int) Math.ceil(Math.hypot(iconWidth, iconHeight));
		int centerIconDiff = ((canvasSize - iconWidth) / 2) + 5; 
		
		// The drag shadow is a ColorDrawable. This sets its dimensions to
		// be the same as the
		// Canvas that the system will provide. As a result, the drag shadow
		// will fill the
		// Canvas.
		mIcon.setBounds(centerIconDiff, centerIconDiff, canvasSize-centerIconDiff, canvasSize-centerIconDiff);

		// Sets the size parameter's width and height values. These get back
		// to the system
		// through the size parameter.
		size.set(canvasSize, canvasSize);

		// Sets the touch point's position to be in the middle of the drag
		// shadow
		touch.set(canvasSize / 2, (int) (canvasSize / 1.5));
	}

	// Defines a callback that draws the drag shadow in a Canvas that the
	// system constructs
	// from the dimensions passed in onProvideShadowMetrics().
	@Override
	public void onDrawShadow(Canvas canvas) {

		// Draws the ColorDrawable in the Canvas passed in from the system.
		// shadow.draw(canvas);
		canvas.save();
		Rect bounds = mIcon.getBounds();
		canvas.rotate(mOrigin == EditFavoritesActivity.SELECTED_APPS_DRAG ? 15f
				: -15f, bounds.centerX(), bounds.centerY());
		int cX = canvas.getWidth() / 2;
		int cY = canvas.getHeight() / 2;
		int glowColor = mContext.getResources().getColor(R.color.edit_favourites_glow_blue_light) - 0xFF000000;

		RadialGradient gradient = new RadialGradient(cX, cY, cX, new int[] {
				(glowColor +0xFF000000) , (glowColor + 0xAA000000), glowColor }, new float[] { 0.0f, 0.5f,
				1.0f }, android.graphics.Shader.TileMode.CLAMP);
		Paint paint = new Paint();
		paint.setShader(gradient);

		canvas.drawCircle(cX, cY, cX, paint);
		mIcon.draw(canvas);
		canvas.restore();
	}

}
