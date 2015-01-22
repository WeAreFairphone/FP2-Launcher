package com.fairphone.fplauncher3.edgeswipe;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.fairphone.fplauncher3.AppInfo;
import com.fairphone.fplauncher3.DragController;
import com.fairphone.fplauncher3.Launcher;
import com.fairphone.fplauncher3.R;
import com.fairphone.fplauncher3.edgeswipe.editor.EditFavoritesActivity;
import com.fairphone.fplauncher3.edgeswipe.editor.EditFavoritesActivity.Themes;
import com.fairphone.fplauncher3.edgeswipe.editor.FavoritesStorageHelper;
import com.fairphone.fplauncher3.edgeswipe.editor.ui.EdgeSwipeInterceptorViewListener;

public class EdgeSwipeMenu implements EdgeSwipeInterceptorViewListener
{

    private static final int EDIT_BUTTON_ITEM = -2;

    private static final String TAG = EdgeSwipeMenu.class.getSimpleName();

    protected final ViewGroup mParentView;
    protected final ViewGroup mMenuView;
    private Context mContext;
    private View mMenuBackgroundView;
    private FrameLayout mEdgeSwipeGroup;
    private LinearLayout mEdgeSwipeHolder;
    private TextView mEditButton;

    private int mPreviousItem;
    boolean isAnimatingToEditMode;
    private boolean isAnimatingItem;
    private Launcher mLauncher;
    private DragController mDragController;
    private int mEdgeSwipeMenuMaxItems;
    private long mEditMenuButtonStartTime;
    private MenuSide mSide;

    private Themes mCurrentTheme;

    private Runnable mShowEditRunnable;

    private boolean mIsMenuVisible;

    private AnimatorSet mShowEdgeSwipeAnimatorSet;

    private AnimatorSet mHideEdgeSwipeAnimatorSet;

    private static final int MAX_FAVORITE_APPS = 4;

    public static enum MenuSide
    {
        LEFT_SIDE, RIGHT_SIDE
    };

    public EdgeSwipeMenu(Context context, Launcher launcher, DragController dragController, ViewGroup parent)
    {
        mContext = context;
        mLauncher = launcher;
        mDragController = dragController;
        mParentView = parent;

        mPreviousItem = -1;
        isAnimatingItem = false;
        mIsMenuVisible = false;
        mHideEdgeSwipeAnimatorSet = null;
        mShowEdgeSwipeAnimatorSet = null;

        mMenuView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.edge_swipe_layout, null);
        mParentView.addView(mMenuView);
        mDragController.setEdgeSwipeInterceptorViewListener(this);

        mEdgeSwipeMenuMaxItems = mContext.getResources().getInteger(R.integer.edge_swipe_items_ammount);

        mCurrentTheme = EditFavoritesActivity.getCurrentTheme(mContext);

        setupLayout();
    }

    private void setupLayout()
    {
        mEdgeSwipeGroup = (FrameLayout) mParentView.findViewById(R.id.edge_swipe_group);
        mEdgeSwipeHolder = (LinearLayout) mParentView.findViewById(R.id.edge_swipe_holder);
        mMenuBackgroundView = mEdgeSwipeGroup.findViewById(R.id.background);
        mEditButton = null;
    }

    private void launchMenuItem(Intent launchIntent, int itemPosition)
    {
        try
        {
            if (itemPosition == 2)
            {
                mLauncher.showAllAppsDrawer();
            }
            else
            {
                if (launchIntent != null)
                {
                    mLauncher.startActivity(mParentView, launchIntent, null);
                }
                else
                {
                    // to avoid the addition of Fairphone home launcher to
                    // appSwitcher
                    mLauncher.startEditFavorites();
                }
            }

        } catch (ActivityNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void hideEdgeSwipe()
    {
        Resources resources = mContext.getResources();

        mHideEdgeSwipeAnimatorSet = new AnimatorSet();
        ObjectAnimator hideBackground = ObjectAnimator.ofFloat(mMenuBackgroundView, View.ALPHA, 0);
        hideBackground.setStartDelay(resources.getInteger(R.dimen.edge_swipe_translate_delay_duration));
        hideBackground.setDuration(resources.getInteger(R.integer.edge_swipe_animate_out_duration));

        ObjectAnimator hideItems = ObjectAnimator.ofFloat(mEdgeSwipeHolder, View.ALPHA, 0);
        hideItems.setDuration(resources.getInteger(R.integer.edge_swipe_animate_out_duration));

        mHideEdgeSwipeAnimatorSet.playTogether(hideBackground, hideItems);
        mHideEdgeSwipeAnimatorSet.addListener(new AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                mEdgeSwipeHolder.removeAllViews();
                mEdgeSwipeGroup.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }
        });

        mHideEdgeSwipeAnimatorSet.addListener(new AnimatorListener()
        {

            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                mIsMenuVisible = false;
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }
        });
        mHideEdgeSwipeAnimatorSet.start();
    }

    public void showEdgeSwipe(float pointerY)
    {
        mCurrentTheme = EditFavoritesActivity.getCurrentTheme(mContext);
        Resources resources = mContext.getResources();

        AppInfo[] selectedApps = FavoritesStorageHelper.loadSelectedApps(mContext, MAX_FAVORITE_APPS);

        // CHECKS IF THE ANIMATION IS STARTING FROM THE LEFT OR RIGHT
        mEdgeSwipeGroup.setVisibility(View.VISIBLE);
        mShowEdgeSwipeAnimatorSet = new AnimatorSet();

        ObjectAnimator showBackground = ObjectAnimator.ofFloat(mMenuBackgroundView, View.ALPHA, 0, 0.97f);
        showBackground.setDuration(resources.getInteger(R.integer.edge_swipe_background_fade_duration));

        mShowEdgeSwipeAnimatorSet.play(showBackground);
        mEdgeSwipeHolder.setAlpha(1);

        mEdgeSwipeHolder.setY(pointerY - (getHolderSize() / 2));

        Log.i(TAG, "PointerY : " + pointerY + " PoE: " + (pointerY - (getHolderSize() / 2)));

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, (int) resources.getDimension(R.dimen.edge_swipe_height));
        params.setMargins(0, 0, 0, (int) resources.getDimension(R.dimen.edge_swipe_item_margin_bottom));
        switch (mSide)
        {
            case LEFT_SIDE:
                setupMenuItems(resources, selectedApps, mShowEdgeSwipeAnimatorSet, params, R.layout.edge_swipe_item_left);
                break;
            case RIGHT_SIDE:
                setupMenuItems(resources, selectedApps, mShowEdgeSwipeAnimatorSet, params, R.layout.edge_swipe_item_right);
                break;
        }
        switch (mCurrentTheme)
        {
            case LIGHT:
                mMenuBackgroundView.setBackgroundResource(R.color.background_grey_light);
                break;

            case DARK:
            default:
                mMenuBackgroundView.setBackgroundResource(R.color.background_blue_dark);
                break;
        }

        mShowEdgeSwipeAnimatorSet.addListener(new AnimatorListener()
        {

            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                setupEditButtonPositionAndTimer();
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }
        });
        mShowEdgeSwipeAnimatorSet.start();
    }

    private float getHolderSize()
    {
        float result = mEdgeSwipeHolder.getHeight();

        if (result == 0)
        {
            // TODO: CAtch the onMeasure and setup the correct sizes after that,
            // for now just calculate the initial size
            Resources r = mContext.getResources();

            float dimenHeight = r.getDimensionPixelSize(R.dimen.edge_swipe_height);
            float dimenBottom = r.getDimensionPixelSize(R.dimen.edge_swipe_item_margin_bottom);

            float size = dimenBottom + dimenHeight;

            result = mEdgeSwipeMenuMaxItems * size;
        }

        return result;
    }

    private void setupMenuItems(Resources resources, AppInfo[] selectedApps, AnimatorSet showEdgeSwipeAnimatorSet, LayoutParams params, int itemLayoutId)
    {

        float edgeSwipeHolderWidth = (float) mEdgeSwipeHolder.getWidth() / 2f;
        float translateOffset = resources.getDimension(R.dimen.edge_swipe_translate_offset);
        long translateDuration = resources.getInteger(R.integer.edge_swipe_translate_duration);
        long translateDelayDuration = resources.getInteger(R.integer.edge_swipe_translate_delay_duration);
        float translateValue = 0;

        switch (mSide)
        {
            case RIGHT_SIDE:
                translateValue = edgeSwipeHolderWidth / 2;
                break;

            case LEFT_SIDE:
                translateValue = -(edgeSwipeHolderWidth / 2);
                break;
        }

        for (int i = 0; i < mEdgeSwipeMenuMaxItems; i++)
        {
            View item = LayoutInflater.from(mContext).inflate(itemLayoutId, null);
            item.setLayoutParams(params);
            mEdgeSwipeHolder.addView(item);
            item.setAlpha(0);
            setItemContent(item, selectedApps[i < 2 ? i : i - 1], i);

            ObjectAnimator translate = ObjectAnimator.ofFloat(item, View.TRANSLATION_X, translateValue, 0);
            translate.setInterpolator(new DecelerateInterpolator());
            translate.setDuration(translateDuration);
            translate.setStartDelay(translateDelayDuration);

            ObjectAnimator fade = ObjectAnimator.ofFloat(item, View.ALPHA, 0, 1);
            fade.setDuration(translateDelayDuration);

            if (showEdgeSwipeAnimatorSet != null)
            {
                showEdgeSwipeAnimatorSet.playTogether(translate, fade);
                showEdgeSwipeAnimatorSet.start();
            }
        }
    }

    private void setItemContent(final View item, AppInfo applicationInfo, int position)
    {
        Resources resources = mContext.getResources();

        ImageView icon = (ImageView) item.findViewById(R.id.icon);
        TextView text = (TextView) item.findViewById(R.id.text);
        TextView editButton = (TextView) item.findViewById(R.id.edit_button);

        switch (mCurrentTheme)
        {
            case LIGHT:
                text.setTextColor(resources.getColor(R.color.blue));
                break;

            case DARK:
            default:
                text.setTextColor(resources.getColor(R.color.blue_light));
                break;
        }

        if (editButton != null)
        {
            editButton.setAlpha(0);
        }

        // IF ALL APPS ICON
        if (position == 2)
        {
            switch (mCurrentTheme)
            {
                case LIGHT:
                    icon.setImageResource(R.drawable.icon_allapps_blue);
                    break;

                case DARK:
                default:
                    icon.setImageResource(R.drawable.icon_allapps_blue_light);
                    break;
            }

            text.setText(resources.getString(R.string.edge_swipe_all_apps));
            mEditButton = editButton;
            return;
        }

        Intent launchIntent = null;
        ComponentName componentName = null;
        Drawable iconDrawable = null;
        String label = "";

        if (applicationInfo == null)
        {
            icon.setImageResource(R.drawable.icon_edge_swipe_add_blue_light);

            label = resources.getString(R.string.add_app).toUpperCase();
            text.setTextSize(16);
            switch (mSide)
            {
                case RIGHT_SIDE:
                    switch (mCurrentTheme)
                    {
                        case LIGHT:
                            item.setBackgroundResource(R.drawable.edge_swipe_right_light_stripe_up);
                            break;

                        case DARK:
                        default:
                            item.setBackgroundResource(R.drawable.edge_swipe_right_dark_stripe_up);
                            break;
                    }
                    break;

                case LEFT_SIDE:
                    switch (mCurrentTheme)
                    {
                        case LIGHT:
                            item.setBackgroundResource(R.drawable.edge_swipe_left_light_stripe_up);
                            break;

                        case DARK:
                        default:
                            item.setBackgroundResource(R.drawable.edge_swipe_left_dark_stripe_up);
                            break;
                    }
                    break;
            }
        }
        else
        {
            componentName = applicationInfo.getComponentName();
            iconDrawable = new BitmapDrawable(mContext.getResources(), applicationInfo.getIconBitmap());
            label = applicationInfo.getApplicationTitle();
            text.setTextSize(20);

            // Set the right ComponentName in order to launch Dialer
            // or Contacts correctly
            PackageManager pacManager = mContext.getPackageManager();
            launchIntent = pacManager.getLaunchIntentForPackage(componentName.getPackageName());
            if (launchIntent != null)
            {
                launchIntent.setComponent(componentName);
            }
            else
            {
                label = mContext.getString(R.string.add_app).toUpperCase();
            }
        }

        icon.setBackground(iconDrawable);
        text.setText(label);
        item.setTag(launchIntent);
    }

    // WHEN THE USER SETS THE FINGER ON AN ITEM
    private void animateItemIn(View item, int currentItem, boolean skipFade)
    {
        Resources resources = mContext.getResources();

        View background = (View) item.findViewById(R.id.background);
        ImageView icon = (ImageView) item.findViewById(R.id.icon);
        TextView text = (TextView) item.findViewById(R.id.text);

        switch (mCurrentTheme)
        {
            case LIGHT:
                text.setTextColor(resources.getColor(R.color.blue_dark));

                switch (mSide)
                {
                    case RIGHT_SIDE:
                        background.setBackgroundResource(R.drawable.edge_swipe_right_light_press);
                        break;
                    case LEFT_SIDE:
                        background.setBackgroundResource(R.drawable.edge_swipe_left_light_press);
                        break;
                }

                break;

            case DARK:
            default:
                text.setTextColor(resources.getColor(R.color.white));

                switch (mSide)
                {
                    case RIGHT_SIDE:
                        background.setBackgroundResource(R.drawable.edge_swipe_right_dark_press);
                        break;
                    case LEFT_SIDE:
                        background.setBackgroundResource(R.drawable.edge_swipe_left_dark_press);
                        break;
                }
                break;
        }

        // IF ALL APPS ICON
        if (currentItem == 2)
        {
            switch (mCurrentTheme)
            {
                case LIGHT:
                    icon.setImageResource(R.drawable.icon_allapps_blue_dark);
                    break;

                case DARK:
                default:
                    icon.setImageResource(R.drawable.icon_allapps_white);
                    break;
            }
        }

        if (item.getTag() == null && currentItem != 2 && currentItem != EDIT_BUTTON_ITEM)
        {

            switch (mCurrentTheme)
            {
                case LIGHT:
                    icon.setImageResource(R.drawable.icon_edge_swipe_add_blue_dark);
                    break;

                case DARK:
                default:
                    icon.setImageResource(R.drawable.icon_edge_swipe_add_white);
                    break;
            }
        }

        float iconTranslateValue = resources.getDimension(R.dimen.edge_swipe_item_icon_translate);
        float textTranslateValue = resources.getDimension(R.dimen.edge_swipe_item_text_translate);
        long translateDuration = resources.getInteger(R.integer.edge_swipe_translate_duration);

        if(!skipFade)
        {
	        ObjectAnimator fadeBackground = ObjectAnimator.ofFloat(background, View.ALPHA, 0, 1);
	        fadeBackground.setDuration(translateDuration);
	        fadeBackground.addListener(new AnimatorListener()
	        {
	            @Override
	            public void onAnimationStart(Animator animation)
	            {
	                isAnimatingItem = true;
	            }
	
	            @Override
	            public void onAnimationRepeat(Animator animation)
	            {
	            }
	
	            @Override
	            public void onAnimationEnd(Animator animation)
	            {
	            }
	
	            @Override
	            public void onAnimationCancel(Animator animation)
	            {
	            }
	        });
	        fadeBackground.start();
        }

        // CHECKS IF THE ANIMATION IS STARTING FROM THE LEFT OR RIGHT
        switch (mSide)
        {
            case LEFT_SIDE:
                translateViewAnimation(icon, iconTranslateValue, translateDuration);
                translateViewAnimation(text, textTranslateValue, translateDuration);
                break;

            case RIGHT_SIDE:
                translateViewAnimation(icon, -iconTranslateValue, translateDuration);
                translateViewAnimation(text, -textTranslateValue, translateDuration);
                break;
        }
    }

    private void translateViewAnimation(View view, float translateValue, long duration)
    {
        ObjectAnimator translateView = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, translateValue);
        translateView.setInterpolator(new DecelerateInterpolator());
        translateView.setDuration(duration);
        translateView.start();
    }

    private void animateItemOut(View item, int currentItem, boolean skipFade)
    {
        Resources resources = mContext.getResources();

        View background = (View) item.findViewById(R.id.background);
        ImageView icon = (ImageView) item.findViewById(R.id.icon);
        TextView text = (TextView) item.findViewById(R.id.text);
        long translateDuration = resources.getInteger(R.integer.edge_swipe_translate_duration);

        switch (mCurrentTheme)
        {
            case LIGHT:
                text.setTextColor(resources.getColor(R.color.blue));
                break;

            case DARK:
            default:
                text.setTextColor(resources.getColor(R.color.blue_light));
                break;
        }

        // IF ALL APPS ICON
        if (currentItem == 2)
        {

            switch (mCurrentTheme)
            {
                case LIGHT:
                    icon.setImageResource(R.drawable.icon_allapps_blue);
                    break;

                case DARK:
                default:
                    icon.setImageResource(R.drawable.icon_allapps_blue_light);
                    break;
            }
        }

        if (item.getTag() == null && currentItem != 2 && currentItem != EDIT_BUTTON_ITEM)
        {
            icon.setImageResource(R.drawable.icon_edge_swipe_add_blue_light);
        }

		if (!skipFade)
		{
	        ObjectAnimator fadeBackground = ObjectAnimator.ofFloat(background, View.ALPHA, 1, 0);
	        fadeBackground.setDuration(translateDuration);
	        fadeBackground.addListener(new AnimatorListener()
	        {
	
	            @Override
	            public void onAnimationStart(Animator animation)
	            {
	                isAnimatingItem = false;
	            }
	
	            @Override
	            public void onAnimationRepeat(Animator animation)
	            {
	            }
	
	            @Override
	            public void onAnimationEnd(Animator animation)
	            {
	            }
	
	            @Override
	            public void onAnimationCancel(Animator animation)
	            {
	            }
	        });
	        fadeBackground.start();
        }

        translateViewAnimation(icon, 0, translateDuration);
        translateViewAnimation(text, 0, translateDuration);
    }

    @Override
    public void onSelectionStarted(float pointerX, float pointerY)
    {
        if (!mIsMenuVisible)
        {
            if (mHideEdgeSwipeAnimatorSet != null)
            {
                mHideEdgeSwipeAnimatorSet.cancel();
            }

            mIsMenuVisible = true;
            if (pointerX < (mLauncher.getResources().getDisplayMetrics().widthPixels / 2))
            {
                mSide = MenuSide.LEFT_SIDE;
            }
            else
            {
                mSide = MenuSide.RIGHT_SIDE;
            }
            showEdgeSwipe(pointerY);
        }
    }

    @Override
    public void onSelectionUpdate(float pointerX, float pointerY)
    {
        if (mEdgeSwipeHolder != null)
        {
            View firstChild = mEdgeSwipeHolder.getChildAt(0);

            float menuHolderY = mEdgeSwipeHolder.getY();

            float itemSize = mEdgeSwipeHolder.getHeight() / mEdgeSwipeMenuMaxItems;

            if (isInActiveZone(pointerX) && firstChild != null && pointerY > menuHolderY)
            {
                float startingPoint = pointerY - menuHolderY;
                int currentItem = (isInEditZone(pointerX, pointerY)&& isTimeToShowEdit()) ? EDIT_BUTTON_ITEM : (int) (startingPoint / itemSize);
                ViewGroup item = null;
                if (mPreviousItem != currentItem && mPreviousItem != -1)
                {
                    item = (ViewGroup) mEdgeSwipeHolder.getChildAt(mPreviousItem);
                    if (item != null)
                    {
                        if (mPreviousItem != 2)
                        {
                        	animateItemOut(item, mPreviousItem, false);
                        }
                        else
                        {
                            if (mPreviousItem == 2)
                            {
                                if (currentItem != EDIT_BUTTON_ITEM)
                                {
                                    animateItemOut(item, mPreviousItem, false);
                                }
                                else
                                {
                                	animateItemOut(item, mPreviousItem, true);
                                	
                                    if (item != null && !isAnimatingToEditMode)
                                    {
                                        switch (mSide)
                                        {
                                            case LEFT_SIDE:
                                                translateViewAnimation(mEditButton, 60, 100);
                                                item.getChildAt(0).animate().translationX(500).setDuration(220);
                                                isAnimatingToEditMode = true;
                                                break;
                                            case RIGHT_SIDE:

                                            	translateViewAnimation(mEditButton, -60, 100);
                                                item.getChildAt(0).animate().translationX(-500).setDuration(220);
                                                isAnimatingToEditMode = true;
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                }
                            }  
                        }
                    }
                    else if(mPreviousItem == EDIT_BUTTON_ITEM)
                    {
                    	if (currentItem == 2)
                        {
                    		item = (ViewGroup) mEdgeSwipeHolder.getChildAt(currentItem);
                            if (item != null && isAnimatingToEditMode)
                            {
                            	animateItemIn(item, currentItem, true);
                            	translateViewAnimation(mEditButton, 0, 100);
                                item.getChildAt(0).animate().translationX(0).setDuration(220);
                                isAnimatingToEditMode = false;
                            }
                        }
                        else
                        {
                        	item = (ViewGroup) mEdgeSwipeHolder.getChildAt(2);
                            
                        	if (item != null)
                        	{
                        		translateViewAnimation(mEditButton, 0, 100);
                        		item.getChildAt(0).setAlpha(0);
                        		item.getChildAt(0).animate().translationX(0).setDuration(220);
                                isAnimatingToEditMode = false;
                        		animateItemOut(item, mPreviousItem, false);	
                        	}
                        }
                    }
                }

                if (!isAnimatingItem)
                {
                    item = (ViewGroup) mEdgeSwipeHolder.getChildAt(currentItem);
                    if (item != null)
                    {
                        animateItemIn(item, currentItem, false);
                    }
                }
                mPreviousItem = currentItem;
            }
            else
            {
                View item = null;
                if (mPreviousItem != -1)
                {
                    item = mEdgeSwipeHolder.getChildAt(mPreviousItem);
                    if (item != null)
                    {
                        animateItemOut(item, mPreviousItem, false);
                    }
                    mPreviousItem = -1;
                }
            }
        }
    }

    @Override
    public void onSelectionFinished(float pointerX, float pointerY)
    {
        if (mIsMenuVisible)
        {
            if (mShowEdgeSwipeAnimatorSet != null)
            {
                mShowEdgeSwipeAnimatorSet.cancel();
            }

            if (isInActiveZone(pointerX))
            {
                if (isInEditZone(pointerX, pointerY) && isTimeToShowEdit())
                {
                    mLauncher.startEditFavorites();
                }
                else
                {
                    View item = mEdgeSwipeHolder.getChildAt(mPreviousItem);
                    if (item != null)
                    {
                        animateItemOut(item, mPreviousItem, false);
                        Intent launchIntent = (Intent) item.getTag();
                        launchMenuItem(launchIntent, mPreviousItem);
                    }
                }
            }
            if (mMenuView != null)
            {
                if (mShowEditRunnable != null)
                {
                    mMenuView.removeCallbacks(mShowEditRunnable);
                }
                mShowEditRunnable = null;
            }
            hideEdgeSwipe();
        }
    }

    private boolean isInEditZone(float pointerX, float pointerY)
    {
        boolean validX = false;
        boolean validY = false;
        ViewGroup item = (ViewGroup) mEdgeSwipeHolder.getChildAt(2);

        if (mSide == null)
        {
            return false;
        }

        int[] coord = new int[2];
        mEditButton.getLocationInWindow(coord);
        switch (mSide)
        {
            case LEFT_SIDE:
                validX = pointerX >= coord[0];
                break;
            case RIGHT_SIDE:
                validX = pointerX <= (coord[0] + mEditButton.getWidth());

                break;
            default:
                break;
        }

        validY = pointerY >= coord[1];
        validY &= pointerY <= coord[1] + mEditButton.getHeight();

        if (validX && validY)
        {
            switch (mCurrentTheme)
            {
                case LIGHT:
                    mEditButton.setTextColor(mContext.getResources().getColor(R.color.edge_swipe_text_blue_selected));
                    break;

                case DARK:
                default:
                    mEditButton.setTextColor(mContext.getResources().getColor(R.color.white));
                    break;
            }
        }
        else
        {
            switch (mCurrentTheme)
            {
                case LIGHT:
                    mEditButton.setTextColor(mContext.getResources().getColor(R.color.edge_swipe_text_blue));
                    break;

                case DARK:
                default:
                    mEditButton.setTextColor(mContext.getResources().getColor(R.color.blue_light));
                    break;
            }
        }
        return validX && validY;
    }

    private boolean startEditButtonAnimation()
    {
        boolean isTimeToShow = isTimeToShowEdit();
        if (isTimeToShow && mEditButton != null && (mEditButton.getAlpha() == 0) && mEdgeSwipeHolder.getVisibility() == View.VISIBLE)
        {
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mEditButton, View.ALPHA, 0, 1);
            fadeIn.setDuration(mContext.getResources().getInteger(R.integer.edge_swipe_translate_delay_duration));
            fadeIn.addListener(new AnimatorListener()
            {

                @Override
                public void onAnimationStart(Animator animation)
                {
                }

                @Override
                public void onAnimationRepeat(Animator animation)
                {
                }

                @Override
                public void onAnimationEnd(Animator animation)
                {
                    mEditButton.setAlpha(1f);
                }

                @Override
                public void onAnimationCancel(Animator animation)
                {
                }
            });
            fadeIn.start();
        }
        else
        {
            Log.d(TAG, "Edit button can't be shown");
        }
        return isTimeToShow;
    }

    private boolean isTimeToShowEdit()
    {
        boolean isTimeToShow = mEditMenuButtonStartTime < System.currentTimeMillis();
        return isTimeToShow;
    }

    private void setupEditButtonPositionAndTimer()
    {
        // set edit menu button timer
        mEditMenuButtonStartTime = (System.currentTimeMillis() - 10) + mLauncher.getResources().getInteger(R.integer.edge_swipe_show_edit_button_time);
        if (mMenuView != null)
        {
            if (mShowEditRunnable == null)
            {
                mShowEditRunnable = new Runnable()
                {

                    @Override
                    public void run()
                    {
                        startEditButtonAnimation();
                    }
                };
                mMenuView.postDelayed(mShowEditRunnable, mLauncher.getResources().getInteger(R.integer.edge_swipe_show_edit_button_time));
            }
        }
        else
        {
            mShowEditRunnable = null;
        }
    }

    private boolean isInActiveZone(float pointerX)
    {
        boolean isActive = false;
        float deadZoneSize = mLauncher.getResources().getDimension(R.dimen.edge_swipe_dead_zone);
        DisplayMetrics displayMetrics = mLauncher.getResources().getDisplayMetrics();

        if (mSide != null)
        {
            // set the X coords
            switch (mSide)
            {
                case LEFT_SIDE:
                    isActive = pointerX > deadZoneSize;
                    break;
                case RIGHT_SIDE:
                    isActive = pointerX < (displayMetrics.widthPixels - deadZoneSize);
                    break;
            }
        }

        return isActive;
    }
}
