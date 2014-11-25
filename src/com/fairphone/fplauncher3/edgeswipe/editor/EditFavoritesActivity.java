/*
 * Copyright (C) 2013 Fairphone Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fairphone.fplauncher3.edgeswipe.editor;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.fairphone.fplauncher3.AppInfo;
import com.fairphone.fplauncher3.R;
import com.fairphone.fplauncher3.edgeswipe.editor.ui.DropDragEventListener;
import com.fairphone.fplauncher3.edgeswipe.editor.ui.EditFavoritesGridView;
import com.fairphone.fplauncher3.edgeswipe.editor.ui.IconDragShadowBuilder;
import com.fairphone.fplauncher3.edgeswipe.editor.ui.EditFavoritesGridView.OnEditFavouritesIconDraggedListener;

/**
 * Edit favorites activity implements functionality to edit your favorite apps
 * that will appear with the edge swipe.
 */
public class EditFavoritesActivity extends Activity implements View.OnDragListener, DragDropItemLayoutListener
{
    private static final String TAG = EditFavoritesActivity.class.getSimpleName();

    // This is used to differentiate a drag from the all apps to favorites
    // from a drag between two favorites to perform a swap
    public static final int SELECTED_APPS_DRAG = 0;
    public static final int ALL_APPS_DRAG = 1;

    private AllAppsListAdapter mAllAppsListAdapter;
    private ArrayList<AppInfo> mAllApps;

    private ArrayList<FrameLayout> mFavIcons;

    private AppInfo[] mSelectedApps;

    private EditFavoritesGridView mAllAppsGridView;

    private int mDragOrigin;

    private View mRemoveFavouriteOverlay;

    private TextView mRemoveFavouriteText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fp_edit_favorites);

        mAllApps = AppDiscoverer.getInstance().getPackages();

        mSelectedApps = FavoritesStorageHelper.loadSelectedApps(this, 4);

        mFavIcons = new ArrayList<FrameLayout>();

        mFavIcons.add((FrameLayout) findViewById(R.id.favourite_group_1));
        mFavIcons.add((FrameLayout) findViewById(R.id.favourite_group_2));
        mFavIcons.add((FrameLayout) findViewById(R.id.favourite_group_3));
        mFavIcons.add((FrameLayout) findViewById(R.id.favourite_group_4));

        mRemoveFavouriteOverlay = findViewById(R.id.remove_favourite_overlay);
        mRemoveFavouriteText = (TextView) findViewById(R.id.remove_favourite_text);

        setupAllAppsList();
        setupSelectedAppsList();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    /**
     * Setup the list with all the apps installed on the device.
     */
    private void setupAllAppsList()
    {
        mAllAppsGridView = (EditFavoritesGridView) findViewById(R.id.all_apps_gridview);

        mAllAppsListAdapter = new AllAppsListAdapter(this);

        mAllAppsListAdapter.setAllApps(mAllApps);

        mAllAppsGridView.setLongClickable(true);

        mAllAppsGridView.setOnItemLongClickListener(new OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
            {
                startDraggingIcon(v, position);

                return true;
            }
        });

        mAllAppsGridView.setOnEditFavouritesIconDraggedListener(new OnEditFavouritesIconDraggedListener()
        {

            @Override
            public void OnEditFavouritesIconDragged(AdapterView<?> parent, View view, int position, long id)
            {
                startDraggingIcon(view, position);
            }
        });

        mAllAppsGridView.setAdapter(mAllAppsListAdapter);

        // set the drag listener to enable favorite icon removal
        mRemoveFavouriteOverlay.setOnDragListener(new DropDragEventListener(this, mFavIcons, mSelectedApps, mAllApps, true));

    }

    private void startDraggingIcon(View view, int position)
    {
        // display a circle around the possible destinations
        toggleFavoriteBackground(-1, true);

        View mainView = this.getWindow().getDecorView();
        AppInfo applicationInfo = mAllApps.get(position);

        // set the item with the origin of the drag and the index of the dragged
        // view
        mDragOrigin = EditFavoritesActivity.ALL_APPS_DRAG;
        String selectedItem = serializeItem(mDragOrigin, position);
        ClipData.Item item = new ClipData.Item(selectedItem);
        ClipData dragData = ClipData.newPlainText(applicationInfo.getApplicationTitle(), applicationInfo.getApplicationTitle());
        dragData.addItem(item);

        mainView.startDrag(dragData,
                new IconDragShadowBuilder(EditFavoritesActivity.this, view, new BitmapDrawable(getResources(), applicationInfo.getIconBitmap())), view, 0);

        mainView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    /**
     * Setup the list with the selected app list, which is the holder of the
     * apps that has been selected to become the favorite apps.
     */
    private void setupSelectedAppsList()
    {
        for (int i = 0; i < 4; i++)
        {
            setupFavoriteIcon(mFavIcons.get(i), mSelectedApps[i], i, false);

            // set the listeners
            // The last argument is set to false since this hasn't the ability
            // to remove icons, it only switches them
            mFavIcons.get(i).setOnDragListener(new DropDragEventListener(this, mFavIcons, mSelectedApps, mAllApps, false));
        }
    }

    @Override
    public void setupFavoriteIcon(FrameLayout rla, AppInfo applicationInfo, int idx, boolean performAnimation)
    {

        if (applicationInfo == null)
        {
            final View dragPlaceholderView = rla.getChildAt(0);
            final View iconView = rla.getChildAt(1);

            if (performAnimation)
            {
                startViewFadeOutFadeInAnimation(dragPlaceholderView, iconView, null);
            }
            else
            {
                dragPlaceholderView.setVisibility(View.VISIBLE);
                iconView.setVisibility(View.INVISIBLE);
            }

            rla.setOnLongClickListener(null);
            mSelectedApps[idx] = null;
        }
        else
        {
            final View dragPlaceholderView = rla.getChildAt(0);

            final TextView iconView = (TextView) rla.getChildAt(1);

            // Log.d(TAG, "Adding app : " +
            // applicationInfo.getApplicationTitle());

            if (mSelectedApps[idx] == null)
            {
                updateFavoriteIcon(applicationInfo, iconView);
                if (performAnimation)
                {
                    startViewFadeOutFadeInAnimation(iconView, dragPlaceholderView, null);
                }
                else
                {
                    dragPlaceholderView.setVisibility(View.INVISIBLE);
                    iconView.setVisibility(View.VISIBLE);
                }
            }
            else
            {
                if (performAnimation)
                {
                    startViewFadeOutFadeInAnimation(null, iconView, applicationInfo);
                }
                else
                {
                    updateFavoriteIcon(applicationInfo, iconView);
                    dragPlaceholderView.setVisibility(View.INVISIBLE);
                    iconView.setVisibility(View.VISIBLE);
                }
            }
            mSelectedApps[idx] = applicationInfo;

            // Set the listener
            // pass the main view and the instance setup the drag and visibility
            // of some views
            final View mainView = this.getWindow().getDecorView();
            rla.setOnLongClickListener(new IdLongClickListener(idx, mainView, this));
        }

        FavoritesStorageHelper.storeSelectedApps(this, mSelectedApps);
    }

    /**
     * Update the icon and label of a favorite
     * 
     * @param applicationInfo
     *            App information that contains the icon and label
     * @param icon
     *            the icon to update
     */
    private void updateFavoriteIcon(AppInfo applicationInfo, final TextView icon)
    {
        Drawable drawable = new BitmapDrawable(getResources(), applicationInfo.getIconBitmap());

        Resources r = getResources();
        float px = r.getDimension(R.dimen.edit_favorites_icon_size);
        drawable.setBounds(0, 0, Math.round(px), Math.round(px));

        icon.setCompoundDrawables(null, drawable, null, null);
    }

    /**
     * Performs the animation when replacing one favorite
     * 
     * @param viewToFadeIn
     *            view that will appear. When null it means that we are swapping
     *            two favorites
     * @param viewToFadeOut
     *            view that will disappear.
     * @param applicationInfo
     *            app info that is used to swap two favorites
     */
    private void startViewFadeOutFadeInAnimation(final View viewToFadeIn, final View viewToFadeOut, final AppInfo applicationInfo)
    {
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out_fast);
        fadeOutAnimation.setAnimationListener(new AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                viewToFadeOut.setVisibility(View.INVISIBLE);

                if (applicationInfo == null && viewToFadeIn != null)
                {
                    if (viewToFadeIn.getVisibility() != View.VISIBLE)
                    {
                        viewToFadeIn.setVisibility(View.VISIBLE);
                        Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_fast);
                        viewToFadeIn.startAnimation(fadeInAnimation);
                    }
                }
                else if (applicationInfo != null)
                {
                    // get the new icon
                    updateFavoriteIcon(applicationInfo, (TextView) viewToFadeOut);

                    viewToFadeOut.setVisibility(View.VISIBLE);
                    Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_fast);
                    viewToFadeOut.startAnimation(fadeInAnimation);
                }
            }
        });
        if (viewToFadeOut.getVisibility() == View.VISIBLE)
        {
            viewToFadeOut.startAnimation(fadeOutAnimation);
        }
    }

    /**
     * Capture the back button press, to make sure we save the selected apps
     * before exiting.
     */
    @Override
    public void onBackPressed()
    {

        FavoritesStorageHelper.storeSelectedApps(this, mSelectedApps);

        Intent intent = getIntent();
        setResult(RESULT_OK, intent);

        finish();
    }

    /**
     * Serializes the item id and origin that is being dragged.
     * 
     * @param appOrigin
     *            the origin: can be SELECT_APPS_DRAG or ALL_APPS_DRAG
     * @param appIndex
     *            the item index
     * @return the serialized item info
     */
    public static String serializeItem(int appOrigin, int appIndex)
    {
        String selectedItem = appOrigin + ";" + appIndex;
        return selectedItem;
    }

    /**
     * Deserializes the item id and origin that is being dragged.
     * 
     * @param toDeserialize
     *            string to deserialize
     * @return an array containing {appOrign, appIndex}
     */
    public static String[] deserializeItem(String toDeserialize)
    {
        String[] selectedItem = toDeserialize.split(";");
        return selectedItem;
    }

    class IdLongClickListener implements View.OnLongClickListener
    {
        private int mId;
        private View mMainView;
        private DragDropItemLayoutListener mListener;

        public IdLongClickListener(int id, View mainView, DragDropItemLayoutListener listener)
        {
            super();

            mId = id;
            mMainView = mainView;
            mListener = listener;
        }

        @Override
        public boolean onLongClick(View v)
        {
            // Show the zone where favorites can be removed
            mListener.showAllAppsRemoveZone();

            // display a circle around the possible destinations
            toggleFavoriteBackground(-1, true);

            // set the drag info
            AppInfo applicationInfo = mSelectedApps[mId];

            // set the item with the origin of the drag and the index of the
            // dragged view
            mDragOrigin = EditFavoritesActivity.SELECTED_APPS_DRAG;
            String selectedItem = serializeItem(mDragOrigin, mId);
            ClipData.Item item = new ClipData.Item(selectedItem);
            ClipData dragData = ClipData.newPlainText(applicationInfo.getApplicationTitle(), applicationInfo.getApplicationTitle());
            dragData.addItem(item);

            mMainView.startDrag(dragData,
                    new IconDragShadowBuilder(EditFavoritesActivity.this, v, new BitmapDrawable(getResources(), applicationInfo.getIconBitmap())), v, 0);

            mMainView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

            return true;
        }
    }

    @Override
    public boolean onDrag(View v, DragEvent event)
    {
        return false;
    }

    @Override
    public void hideAllAppsRemoveZone()
    {
        hideAllAppsRemoveZoneRedGlow();

        mRemoveFavouriteOverlay.setVisibility(View.GONE);
        mRemoveFavouriteText.setVisibility(View.GONE);

        // hide the circle around the possible destinations
        toggleFavoriteBackground(-1, false);
    }

    @Override
    public void showAllAppsRemoveZone()
    {
        mRemoveFavouriteOverlay.setVisibility(View.VISIBLE);
        mRemoveFavouriteText.setVisibility(View.VISIBLE);
    }

    @Override
    public void showAllAppsRemoveZoneRedGlow()
    {
    	Resources resources = getResources();
        mRemoveFavouriteOverlay.setBackgroundResource(R.drawable.edit_favourites_background_red);
        mRemoveFavouriteText.setText(R.string.edit_favourites_remove);
        mRemoveFavouriteText.setTextSize(resources.getInteger(R.integer.edit_favorites_remove_text_size));
        mRemoveFavouriteText.setTextColor(resources.getColor(R.color.edit_favourites_text_red_dark));
    }

    @Override
    public void hideAllAppsRemoveZoneRedGlow()
    {
    	Resources resources = getResources();
        mRemoveFavouriteOverlay.setBackgroundResource(R.drawable.background_edit_favourites_stripe_grey);
        mRemoveFavouriteText.setText(R.string.drag_here_to_remove);
        mRemoveFavouriteText.setTextSize(resources.getInteger(R.integer.edit_favorites_drag_here_to_remove_text_size));
        mRemoveFavouriteText.setTextColor(resources.getColor(R.color.edit_favourites_text_white));
    }

    @Override
    public void toggleAllAppRemoveZoneRedGlow(float pointerX, float pointerY)
    {
        // the red glow only makes sense when removing favorites
        if (mDragOrigin == SELECTED_APPS_DRAG)
        {
            if (isInRemoveZone(pointerX, pointerY))
            {
                showAllAppsRemoveZoneRedGlow();
            }
            else
            {
                hideAllAppsRemoveZoneRedGlow();
            }
        }
    }

    private boolean isInRemoveZone(float pointerX, float pointerY)
    {

        boolean validX = false;
        boolean validY = false;
        View allAppsGroupView = findViewById(R.id.remove_favourite_overlay);

        validX = pointerX <= (allAppsGroupView.getX() + allAppsGroupView.getWidth());
        validY = pointerY >= allAppsGroupView.getY();

        return validX && validY;
    }

    /**
     * Displays a background on the favorite possible positions when configuring
     * it
     * 
     * @param selectedFavorite
     *            the favorite position where the background will not be shown.
     *            -1 means that all backgrounds will be shown.
     * @param showBackground
     *            true: displays the background; false: removes the background
     */
    private void toggleFavoriteBackground(int selectedFavorite, boolean showBackground)
    {
        for (int i = 0; i < mFavIcons.size(); i++)
        {
            if (i != selectedFavorite && showBackground)
            {
                mFavIcons.get(i).setBackgroundResource(R.drawable.background_edit_favourites_stripe_grey_light);
            }
            else
            {
                mFavIcons.get(i).setBackground(null);
            }
        }
    }

    @Override
    public void showFavoriteBlueHighlight(FrameLayout view)
    {
        showFavoriteHighlight(view, R.drawable.background_edit_favourites_stripe_blue, R.color.edit_favourites_text_blue_light, getResources().getInteger(R.integer.edit_favorites_drag_here_to_add_highlight_text_size));
    }

    @Override
    public void showFavoriteGreyHighlight(FrameLayout view)
    {
        showFavoriteHighlight(view, R.drawable.background_edit_favourites_stripe_grey_light, R.color.edit_favourites_text_grey_dark, getResources().getInteger(R.integer.edit_favorites_drag_here_to_add_normal_text_size));
    }

    private void showFavoriteHighlight(FrameLayout view, int backgroundResourceId, int textColorResourceId, int textSize)
    {
        if (view != null)
        {
	        TextView emptyFavorites = (TextView) view.getChildAt(0);
	        
	        view.setBackgroundResource(backgroundResourceId);
	        if (emptyFavorites != null)
	        {
	        	int color = getResources().getColor(textColorResourceId);
	        	
	        	emptyFavorites.setBackgroundResource(backgroundResourceId);
	            emptyFavorites.setTextColor(color);
	            emptyFavorites.setTextSize(textSize);
	        }
        }
    }

}
