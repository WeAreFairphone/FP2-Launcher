package com.fairphone.fplauncher3.edgeswipe.editor.ui;

import java.util.ArrayList;

import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.fairphone.fplauncher3.AppInfo;
import com.fairphone.fplauncher3.edgeswipe.editor.DragDropItemLayoutListener;
import com.fairphone.fplauncher3.edgeswipe.editor.EditFavoritesActivity;

public class DropDragEventListener implements View.OnDragListener {

	private static final String TAG = DropDragEventListener.class.getSimpleName();
	private final ArrayList<FrameLayout> mFavIcons;
	private final AppInfo[] mSelectedApps;
	private final ArrayList<AppInfo> mAllApps;
	private final DragDropItemLayoutListener mListener;
	/**
	 * True means that a favorite will be removed from the list.
	 */
	private final boolean mIsToRemove;

	public DropDragEventListener(DragDropItemLayoutListener listener,
			ArrayList<FrameLayout> favIcons, AppInfo[] mSelectedApps2,
			ArrayList<AppInfo> allApps, boolean toDeleteFromFavorites) {
		mFavIcons = favIcons;
		mSelectedApps = mSelectedApps2;
		mAllApps = allApps;
		mListener = listener;
		mIsToRemove = toDeleteFromFavorites;
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		// Defines a variable to store the action type for the incoming
		// event
		final int action = event.getAction();

		View rla = v;

		// Handles each of the expected events
		switch (action) {

		case DragEvent.ACTION_DRAG_STARTED: {
			// Moving an icon to an occupied position replaces the
			// current one
			return true;
		}
		case DragEvent.ACTION_DRAG_ENTERED:
			// toggle the red glow when removing favorites
			int idx = mFavIcons.indexOf(rla);
			if (idx == -1) {
				mListener.toggleAllAppRemoveZoneRedGlow(event.getX(),
						event.getY());
			} else {
				mListener.hideAllAppsRemoveZoneRedGlow();
				mListener.applyFavoritePressState((FrameLayout)rla);
			}
			break;
		case DragEvent.ACTION_DRAG_EXITED:
			mListener.hideAllAppsRemoveZoneRedGlow();
			idx = mFavIcons.indexOf(rla);
			if (idx != -1) {
				mListener.applyFavoriteUpState((FrameLayout)rla);
			}
			break;
		case DragEvent.ACTION_DROP:
			int id = mFavIcons.indexOf(rla);

			// get the Item data
			// 0 is the origin
			// 1 is the index
			String[] clipItemData = EditFavoritesActivity
					.deserializeItem(event
							.getClipData()
							.getItemAt(
									event.getClipData().getItemCount() - 1)
							.getText().toString());

			int position = -1;
			AppInfo info = null;

			// obtain the applicationInfo
			switch (Integer.parseInt(clipItemData[0])) {
			case EditFavoritesActivity.SELECTED_APPS_DRAG:
				position = Integer.parseInt(clipItemData[1]);

				// when not removing an icon swap is performed
				if (!mIsToRemove) {
					info = mSelectedApps[position];
					mListener.setupFavoriteIcon(mFavIcons.get(position),
							mSelectedApps[id], position, true);
				} else {
					// remove the favorite
					id = position;
					rla = mFavIcons.get(id);
				}
				break;
			case EditFavoritesActivity.ALL_APPS_DRAG:
				position = Integer.parseInt(clipItemData[1]);
				info = mAllApps.get(position);
				break;
			default:
				Log.e(TAG,
						"Unknown Icon Origin received by OnDragListener.");
				break;
			}

			// only setup the icon if a valid id is obtained
			if (id != -1) {
				mListener.setupFavoriteIcon((FrameLayout) rla, info, id,
						true);
			}

			return true;
		case DragEvent.ACTION_DRAG_ENDED:
			// hide zone remove zone
			mListener.hideAllAppsRemoveZone();
			return true;
		default:
			Log.e(TAG, "Unknown action type received by OnDragListener.");
		}

		return false;
	}
}
