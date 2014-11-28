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
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.fairphone.fplauncher3.AppInfo;
import com.fairphone.fplauncher3.DragController;
import com.fairphone.fplauncher3.Launcher;
import com.fairphone.fplauncher3.R;
import com.fairphone.fplauncher3.edgeswipe.editor.FavoritesStorageHelper;
import com.fairphone.fplauncher3.edgeswipe.editor.ui.EdgeSwipeInterceptorViewListener;

public class EdgeSwipeMenu implements EdgeSwipeInterceptorViewListener{
	
	private static final String TAG = EdgeSwipeMenu.class.getSimpleName();
	
	protected final ViewGroup mParentView;
	protected final ViewGroup mMenuView;
	private Context mContext;
    private View mMenuBackgroundView;
    private FrameLayout mEdgeSwipeGroup;
    private LinearLayout mEdgeSwipeHolder;
    private View mEditButton;

    private int mPreviousItem;
    private boolean isAnimatingItem;
	private Launcher mLauncher;
	private DragController mDragController;
	private int mEdgeSwipeMenuMaxItems;
	private long mEditMenuButtonStartTime;
	private MenuSide mSide;
    
	private static final int MAX_FAVORITE_APPS = 4;
	public static enum MenuSide
    {
        LEFT_SIDE, RIGHT_SIDE
    };

	public EdgeSwipeMenu(Context context, Launcher launcher, DragController dragController, ViewGroup parent) {
		mContext = context;
		mLauncher = launcher;
		mDragController = dragController;
		mParentView = parent;
		
		mPreviousItem = -1;
	    isAnimatingItem = false;
	    
		mMenuView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.edge_swipe_layout, null);
		mParentView.addView(mMenuView);
		mDragController.setEdgeSwipeInterceptorViewListener(this);
		
		mEdgeSwipeMenuMaxItems = mContext.getResources().getInteger(R.integer.edge_swipe_items_ammount);
		
		setupLayout();
	}
	
	private void setupLayout()
    {
        mEdgeSwipeGroup = (FrameLayout) mParentView.findViewById(R.id.edge_swipe_group);
        mEdgeSwipeHolder = (LinearLayout) mParentView.findViewById(R.id.edge_swipe_holder);
        mMenuBackgroundView = mEdgeSwipeGroup.findViewById(R.id.background);
        mEditButton = mEdgeSwipeGroup.findViewById(R.id.edit_button);
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
				if (launchIntent != null) {
					mLauncher.startActivity(mParentView, launchIntent, null);
				} else {
					//to avoid the addition of Fairphone home launcher to appSwitcher
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

        AnimatorSet hideEdgeSwipeAnimatorSet = new AnimatorSet();
        ObjectAnimator hideBackground = ObjectAnimator.ofFloat(mMenuBackgroundView, View.ALPHA, 0);
        hideBackground.setStartDelay(resources.getInteger(R.dimen.edge_swipe_translate_delay_duration));
        hideBackground.setDuration(resources.getInteger(R.integer.edge_swipe_animate_out_duration));

        ObjectAnimator hideItems = ObjectAnimator.ofFloat(mEdgeSwipeHolder, View.ALPHA, 0);
        hideItems.setDuration(resources.getInteger(R.integer.edge_swipe_animate_out_duration));

        hideEdgeSwipeAnimatorSet.playTogether(hideBackground, hideItems);
        hideEdgeSwipeAnimatorSet.addListener(new AnimatorListener()
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

        hideEdgeSwipeAnimatorSet.start();
    }
    
	public void showEdgeSwipe() {
		Resources resources = mContext.getResources();

		AppInfo[] selectedApps = FavoritesStorageHelper.loadSelectedApps(
				mContext, MAX_FAVORITE_APPS);

		// CHECKS IF THE ANIMATION IS STARTING FROM THE LEFT OR RIGHT
		mEdgeSwipeGroup.setVisibility(View.VISIBLE);
		AnimatorSet showEdgeSwipeAnimatorSet = new AnimatorSet();

		ObjectAnimator showBackground = ObjectAnimator.ofFloat(
				mMenuBackgroundView, View.ALPHA, 0, 1);
		showBackground.setDuration(resources.getInteger(R.integer.edge_swipe_background_fade_duration));

		showEdgeSwipeAnimatorSet.play(showBackground);
		mEdgeSwipeHolder.setAlpha(1);

		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
		switch (mSide) {
		case LEFT_SIDE:
			setupMenuItems(resources, selectedApps, showEdgeSwipeAnimatorSet,
					params, R.layout.edge_swipe_item_left);
			break;
		case RIGHT_SIDE:

			setupMenuItems(resources, selectedApps, showEdgeSwipeAnimatorSet,
					params, R.layout.edge_swipe_item_right);
			break;
		}
		
		showEdgeSwipeAnimatorSet.start();
	}

	private void setupMenuItems(Resources resources, AppInfo[] selectedApps,
			AnimatorSet showEdgeSwipeAnimatorSet, LayoutParams params,
			int itemLayoutId) {

		float edgeSwipeHolderWidth = (float) mEdgeSwipeHolder.getWidth() / 2f;
		float translateOffset = resources
				.getDimension(R.dimen.edge_swipe_translate_offset);
		long translateDuration = resources
				.getInteger(R.integer.edge_swipe_translate_duration);
		long translateDelayDuration = resources
				.getInteger(R.integer.edge_swipe_translate_delay_duration);
		float translateValue = 0;
		
		switch (mSide) {
			case RIGHT_SIDE:
				translateValue = edgeSwipeHolderWidth + translateOffset;
				break;
	
			case LEFT_SIDE:
				translateValue = -(edgeSwipeHolderWidth - translateOffset);
				break;
		}

		for (int i = 0; i < mEdgeSwipeMenuMaxItems; i++) {
			View item = LayoutInflater.from(mContext).inflate(itemLayoutId,
					null);
			item.setLayoutParams(params);
			mEdgeSwipeHolder.addView(item);
			item.setAlpha(0);
			setItemContent(item, selectedApps[i < 2 ? i : i - 1], i);
			
			ObjectAnimator translate = ObjectAnimator.ofFloat(item,
					View.TRANSLATION_X, translateValue, 0);
			translate.setInterpolator(new DecelerateInterpolator());
			translate.setDuration(translateDuration);
			translate.setStartDelay(translateDelayDuration);

			ObjectAnimator fade = ObjectAnimator
					.ofFloat(item, View.ALPHA, 0, 1);
			fade.setDuration(translateDelayDuration);

			showEdgeSwipeAnimatorSet.playTogether(translate, fade);
		}
	}

    private void setItemContent(final View item, AppInfo applicationInfo, int position)
    {
        ImageView icon = (ImageView) item.findViewById(R.id.icon);
        TextView text = (TextView) item.findViewById(R.id.text);

        //IF ALL APPS ICON
        if (position == 2)
        {
            icon.setImageResource(R.drawable.ic_allapps);
            text.setText("");
            return;
        }
        
        Intent launchIntent = null;
        ComponentName componentName = null;
        Drawable iconDrawable = null;
        String label = "";
        Resources resources = mContext.getResources();
        
        if (applicationInfo == null)
        {
            label = resources.getString(R.string.add_app).toUpperCase();
        }
        else
        {
            componentName = applicationInfo.getComponentName();
            iconDrawable = new BitmapDrawable(mContext.getResources(), applicationInfo.getIconBitmap());
            label = applicationInfo.getApplicationTitle();
            
            // Set the right ComponentName in order to launch Dialer
            // or Contacts correctly
            PackageManager pacManager = mContext.getPackageManager();
            launchIntent = pacManager.getLaunchIntentForPackage(componentName.getPackageName());
            if(launchIntent != null){
                launchIntent.setComponent(componentName);
            }else{
                label = mContext.getString(R.string.add_app).toUpperCase();
            }
        }
        
        icon.setBackground(iconDrawable);
        text.setText(label);
        item.setTag(launchIntent);
    }

    private void animateItemIn(View item)
    {
    	Resources resources = mContext.getResources();
    	
        View background = (View) item.findViewById(R.id.background);
        ImageView icon = (ImageView) item.findViewById(R.id.icon);
        TextView text = (TextView) item.findViewById(R.id.text);
        
        float iconTranslateValue = resources.getDimension(R.dimen.edge_swipe_item_icon_translate);
        float textTranslateValue = resources.getDimension(R.dimen.edge_swipe_item_text_translate);
        long translateDuration = resources.getInteger(R.integer.edge_swipe_translate_duration);

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

        //CHECKS IF THE ANIMATION IS STARTING FROM THE LEFT OR RIGHT
		switch (mSide) {
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

	private void translateViewAnimation(View view, float translateValue,
			long duration) {
		ObjectAnimator translateView = ObjectAnimator.ofFloat(view,
				View.TRANSLATION_X, translateValue);
		translateView.setInterpolator(new DecelerateInterpolator());
		translateView.setDuration(duration);
		translateView.start();
	}

    private void animateItemOut(View item)
    {
    	Resources resources = mContext.getResources();
   
        View background = (View) item.findViewById(R.id.background);
        ImageView icon = (ImageView) item.findViewById(R.id.icon);
        TextView text = (TextView) item.findViewById(R.id.text);
        long translateDuration = resources.getInteger(R.integer.edge_swipe_translate_duration);

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

        translateViewAnimation(icon, 0, translateDuration);
        translateViewAnimation(text, 0, translateDuration);
    }

	@Override
	public void onSelectionStarted(float pointerX, float pointerY) {
		if(pointerX < (mLauncher.getResources().getDisplayMetrics().widthPixels / 2)){
			mSide = MenuSide.LEFT_SIDE;
		}
		else{
			mSide = MenuSide.RIGHT_SIDE;
		}
		setupEditButtonPositionAndTimer();
		showEdgeSwipe();
	}

	@Override
	public void onSelectionUpdate(float pointerX, float pointerY) {
		startEditButtonAnimation();
		
		if (mEdgeSwipeHolder != null) {
			View firstChild = mEdgeSwipeHolder.getChildAt(0);
			float menuHolderY = mEdgeSwipeHolder.getY();
			if (isInActiveZone(pointerX) && firstChild != null && pointerY > menuHolderY) {
				float startingPoint = pointerY - menuHolderY;
				int currentItem = (int) (startingPoint / firstChild.getHeight());
				View item;
				if (mPreviousItem != currentItem && mPreviousItem != -1) {
					item = mEdgeSwipeHolder.getChildAt(mPreviousItem);
					if (item != null) {
						animateItemOut(item);
					}
				}

				if (!isAnimatingItem) {
					item = mEdgeSwipeHolder.getChildAt(currentItem);
					if (item != null) {
						animateItemIn(item);
					}
				}
				mPreviousItem = currentItem;
			}
			else
			{
				View item;
				if (mPreviousItem != -1) {
					item = mEdgeSwipeHolder.getChildAt(mPreviousItem);
					if (item != null) {
						animateItemOut(item);
					}
					mPreviousItem = -1;
				}
			}
		}
	}

	@Override
	public void onSelectionFinished(float pointerX, float pointerY) {
		if (isInEditZone(pointerX, pointerY)) {
			mLauncher.startEditFavorites();
		} else if (isInActiveZone(pointerX)) {
			View item = mEdgeSwipeHolder.getChildAt(mPreviousItem);
			if (item != null) {
				animateItemOut(item);
				Intent launchIntent = (Intent) item.getTag();
				launchMenuItem(launchIntent, mPreviousItem);
			}
		}
		hideEdgeSwipe();
	}
	
	private boolean isInEditZone(float pointerX, float pointerY)
    {   
    	boolean validX = false;
        boolean validY = false;
        
        if(mSide == null){
        	return false;
        }
        
    	switch (mSide)
        {
            case LEFT_SIDE:
                validX = pointerX >= mEditButton.getX();
                break;
            case RIGHT_SIDE:
            	validX = pointerX <= (mEditButton.getX() + mEditButton.getWidth());
                break;
            default:
                break;
        }
    	validY = pointerY >= mEditButton.getY();
    	validY &= pointerY <= mEditButton.getY() + mEditButton.getHeight();
        
        return validX && validY;
    }
	
	private boolean startEditButtonAnimation() {
		boolean isTimeToShow = mEditMenuButtonStartTime < System.currentTimeMillis();
		if(isTimeToShow && (mEditButton.getAlpha() == 0) && mEdgeSwipeHolder.getVisibility() == View.VISIBLE){
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
		}else{
			Log.d(TAG, "Edit button can't be shown");
		}
		return isTimeToShow;
	}

	private void setupEditButtonPositionAndTimer() {
		
		mEditButton.setAlpha(0);
		//set edit menu button timer
        mEditMenuButtonStartTime = System.currentTimeMillis() + mLauncher.getResources().getInteger(R.integer.edge_swipe_show_edit_button_time);
        DisplayMetrics displayMetrics = mLauncher.getResources().getDisplayMetrics();
		// set the X coords
		switch (mSide)
        {
            case LEFT_SIDE:
                mEditButton.setX((int) (displayMetrics.widthPixels - mEditButton.getWidth()));
                break;
            case RIGHT_SIDE:
            	mEditButton.setX(0);
                break;
        }
        
        //set Y coords
		mEditButton.setY(displayMetrics.heightPixels / 2
				- (mEditButton.getHeight() / 2));
	}
	
	private boolean isInActiveZone(float pointerX) {
        boolean isActive = false;
        float deadZoneSize = mLauncher.getResources().getDimension(R.dimen.edge_swipe_dead_zone);
        DisplayMetrics displayMetrics = mLauncher.getResources().getDisplayMetrics();
        
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
        
        return isActive;
	}
}
