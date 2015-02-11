package com.fairphone.fplauncher3.edgeswipe.editor.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class FavoriteItemView extends TextView {

	public interface OnFavouriteItemDraggedListener {
		
		public void OnFavouriteItemDragged(View view);
		public void OnFavouriteItemDragEnded();
	}

	private static final String TAG = FavoriteItemView.class.getSimpleName();
	private Context mContext;
	private OnFavouriteItemDraggedListener listener;
	private float touchStartX;
	private float touchStartY;
	private boolean hasStartedDraggingOut;
	private final float minMoveDistance = 25;

	public FavoriteItemView(Context context) {
		super(context);
		init(context);
	}

	public FavoriteItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public FavoriteItemView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@SuppressLint("NewApi")
	public FavoriteItemView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	protected void init(Context context) {
		mContext = context;
	}

	public void setOnFavouriteItemDraggedListener(
			OnFavouriteItemDraggedListener listener) {
		this.listener = listener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			touchStartX = ev.getX();
			touchStartY = ev.getY();
			hasStartedDraggingOut = true;
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			if (hasStartedDraggingOut) {
				float xDif = ev.getX() - touchStartX;
				float yDif = ev.getY() - touchStartY;

				float absYDif = Math.abs(yDif);
				float absXDif = Math.abs(xDif);

				if (absXDif > minMoveDistance || absYDif > minMoveDistance) {
					hasStartedDraggingOut = false;
					MotionEvent cancelEvent = MotionEvent.obtain(
							ev.getDownTime(), ev.getEventTime(),
							MotionEvent.ACTION_CANCEL, ev.getX(), ev.getY(),
							ev.getMetaState());
					super.onTouchEvent(cancelEvent);
					if (listener != null) {
						listener.OnFavouriteItemDragged(this);
					}
				}
			}
			break;
		}

		case MotionEvent.ACTION_UP: {
			hasStartedDraggingOut = false;
			if (listener != null) {
				listener.OnFavouriteItemDragEnded();
			}
			break;
		}

		}

        return hasStartedDraggingOut || super.onTouchEvent(ev);
	}
}
