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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.fairphone.fplauncher3.AppInfo;
import com.fairphone.fplauncher3.Launcher;
import com.fairphone.fplauncher3.R;
import com.fairphone.fplauncher3.edgeswipe.editor.FavoritesStorageHelper;

public class EdgeSwipeMenu {
	
	protected final ViewGroup parentView;
	protected final ViewGroup menu_view;
	private Context mContext;
    private View background;
    private FrameLayout edgeSwipeGroup;
    private LinearLayout edgeSwipeHolder;

    private int previousItem = -1;
    private boolean isAnimatingItem;
    
	private static final int MAX_FAVORITE_APPS = 4;

	public EdgeSwipeMenu(Context context, ViewGroup parent) {
		mContext = context;
		parentView = parent;

		menu_view = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.edge_swipe_layout, null);
		parentView.addView(menu_view);
		
		setupLayout();
	}
	
	private void setupLayout()
    {
        edgeSwipeGroup = (FrameLayout) parentView.findViewById(R.id.edge_swipe_group);
        edgeSwipeHolder = (LinearLayout) parentView.findViewById(R.id.edge_swipe_holder);
        background = (View) edgeSwipeGroup.findViewById(R.id.background);

        edgeSwipeHolder.setOnTouchListener(new OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int currentItem = (int) (event.getY() / edgeSwipeHolder.getChildAt(0).getHeight());

                switch (MotionEventCompat.getActionMasked(event))
                {
                    case MotionEvent.ACTION_UP:
                    	View item = edgeSwipeHolder.getChildAt(previousItem);
                        animateItemOut(item, false);
                        Intent launchIntent = (Intent) item.getTag();
                        launchMenuItem(launchIntent, previousItem);
                        hideEdgeSwipe();

                        break;

                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        if (previousItem != currentItem && previousItem != -1)
                            animateItemOut(edgeSwipeHolder.getChildAt(previousItem), false);

                        if (!isAnimatingItem)
                            animateItemIn(edgeSwipeHolder.getChildAt(currentItem), false);

                        previousItem = currentItem;

                        break;

                    default:
                        break;
                }

                return true;
            }
        });
    }
        
    private void launchMenuItem(Intent launchIntent, int itemPosition)
    {
        try
        {
            if (itemPosition == 2)
            {
            	((Launcher)mContext).showAllAppsDrawer();
            }
            else
            {
				if (launchIntent != null) {
					((Launcher)mContext).startActivity(parentView, launchIntent, null);
				} else {
					//to avoid the addition of Fairphone home launcher to appSwitcher
					((Launcher)mContext).startEditFavorites();
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
        ObjectAnimator hideBackground = ObjectAnimator.ofFloat(background, View.ALPHA, 0);
        hideBackground.setStartDelay(resources.getInteger(R.dimen.edge_swipe_translate_delay_duration));
        hideBackground.setDuration(resources.getInteger(R.integer.edge_swipe_animate_out_duration));

        ObjectAnimator hideItems = ObjectAnimator.ofFloat(edgeSwipeHolder, View.ALPHA, 0);
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
                edgeSwipeHolder.removeAllViews();
                edgeSwipeGroup.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }
        });

        hideEdgeSwipeAnimatorSet.start();
    }

	public void updateIcons()
    {
        AppInfo[] selectedApps = FavoritesStorageHelper.loadSelectedApps(mContext, MAX_FAVORITE_APPS);

    }
    
    public void showEdgeSwipe(boolean fromLeft)
    {
        Resources resources = mContext.getResources();

        AppInfo[] selectedApps = FavoritesStorageHelper.loadSelectedApps(mContext, MAX_FAVORITE_APPS);
        
        //CHECKS IF THE ANIMATION IS STARTING FROM THE LEFT OR RIGHT
        if (fromLeft)
        {
            edgeSwipeGroup.setVisibility(View.VISIBLE);
            AnimatorSet showEdgeSwipeAnimatorSet = new AnimatorSet();

            ObjectAnimator showBackground = ObjectAnimator.ofFloat(background, View.ALPHA, 0, 1);
            showBackground.setDuration(resources.getInteger(R.integer.edge_swipe_background_fade_duration));

            showEdgeSwipeAnimatorSet.play(showBackground);
            edgeSwipeHolder.setAlpha(1);

            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
            //INFLATES 5 ITEMS ON THE HOLDER LAYOUT
            for (int i = 0; i < resources.getInteger(R.integer.edge_swipe_items_ammount); i++)
            {
                View item = LayoutInflater.from(mContext).inflate(R.layout.edge_swipe_item_left, null);
                item.setLayoutParams(params);
                edgeSwipeHolder.addView(item);
                item.setAlpha(0);
                setItemContent(item, selectedApps[i<2?i:i-1], i);
                ObjectAnimator translate =
                        ObjectAnimator.ofFloat(item, View.TRANSLATION_X,
                                edgeSwipeHolder.getWidth() / 2 + resources.getDimension(R.dimen.edge_swipe_translate_offset), 0);
                translate.setInterpolator(new DecelerateInterpolator());
                translate.setDuration(resources.getInteger(R.integer.edge_swipe_translate_duration));
                translate.setStartDelay(resources.getInteger(R.integer.edge_swipe_translate_delay_duration));
                ObjectAnimator fade = ObjectAnimator.ofFloat(item, View.ALPHA, 0, 1);
                fade.setDuration(resources.getInteger(R.integer.edge_swipe_translate_delay_duration));

                showEdgeSwipeAnimatorSet.playTogether(translate, fade);
            }

            showEdgeSwipeAnimatorSet.start();
        }
        else
        {
            edgeSwipeGroup.setVisibility(View.VISIBLE);
            AnimatorSet showEdgeSwipeAnimatorSet = new AnimatorSet();

            ObjectAnimator showBackground = ObjectAnimator.ofFloat(background, View.ALPHA, 0, 1);
            showBackground.setDuration(resources.getInteger(R.integer.edge_swipe_background_fade_duration));

            showEdgeSwipeAnimatorSet.play(showBackground);
            edgeSwipeHolder.setAlpha(1);

            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);

            //INFLATES 5 ITEMS ON THE HOLDER LAYOUT
            for (int i = 0; i < resources.getInteger(R.integer.edge_swipe_items_ammount); i++)
            {
                View item = LayoutInflater.from(mContext).inflate(R.layout.edge_swipe_item_right, null);
                item.setLayoutParams(params);
                edgeSwipeHolder.addView(item);
                item.setAlpha(0);
                setItemContent(item, selectedApps[i<2?i:i-1], i);
                ObjectAnimator translate =
                        ObjectAnimator.ofFloat(item, View.TRANSLATION_X,
                                -((edgeSwipeHolder.getWidth() / 2) - resources.getDimension(R.dimen.edge_swipe_translate_offset)), 0);
                translate.setInterpolator(new DecelerateInterpolator());
                translate.setDuration(resources.getInteger(R.integer.edge_swipe_translate_duration));
                translate.setStartDelay(resources.getInteger(R.integer.edge_swipe_translate_delay_duration));
                ObjectAnimator fade = ObjectAnimator.ofFloat(item, View.ALPHA, 0, 1);
                fade.setDuration(resources.getInteger(R.integer.edge_swipe_translate_duration));

                showEdgeSwipeAnimatorSet.playTogether(translate, fade);

            }

            showEdgeSwipeAnimatorSet.start();
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
            label = resources.getString(R.string.edit);
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
                label = mContext.getString(R.string.edit);
            }
        }
        
        icon.setBackground(iconDrawable);
        text.setText(label);
        item.setTag(launchIntent);
    }

    private void animateItemIn(View item, boolean fromLeft)
    {
    	Resources resources = mContext.getResources();
    	
        View background = (View) item.findViewById(R.id.background);
        ImageView icon = (ImageView) item.findViewById(R.id.icon);
        TextView text = (TextView) item.findViewById(R.id.text);

        ObjectAnimator fadeBackground = ObjectAnimator.ofFloat(background, View.ALPHA, 0, 1);
        fadeBackground.setDuration(resources.getInteger(R.integer.edge_swipe_translate_duration));
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
        if (fromLeft)
        {
            ObjectAnimator translateIcon =
                    ObjectAnimator.ofFloat(icon, View.TRANSLATION_X, resources.getDimension(R.dimen.edge_swipe_item_icon_translate));
            translateIcon.setInterpolator(new DecelerateInterpolator());
            translateIcon.setDuration(resources.getInteger(R.integer.edge_swipe_translate_duration));
            translateIcon.start();
            ObjectAnimator translateText =
                    ObjectAnimator.ofFloat(text, View.TRANSLATION_X, resources.getDimension(R.dimen.edge_swipe_item_text_translate));
            translateText.setInterpolator(new DecelerateInterpolator());
            translateText.setDuration(resources.getInteger(R.integer.edge_swipe_translate_duration));
            translateText.start();

        }
        else
        {
            ObjectAnimator translateIcon =
                    ObjectAnimator.ofFloat(icon, View.TRANSLATION_X, -(resources.getDimension(R.dimen.edge_swipe_item_icon_translate)));
            translateIcon.setInterpolator(new DecelerateInterpolator());
            translateIcon.setDuration(resources.getInteger(R.integer.edge_swipe_translate_duration));
            translateIcon.start();
            ObjectAnimator translateText =
                    ObjectAnimator.ofFloat(text, View.TRANSLATION_X, -(resources.getDimension(R.dimen.edge_swipe_item_text_translate)));
            translateText.setInterpolator(new DecelerateInterpolator());
            translateText.setDuration(resources.getInteger(R.integer.edge_swipe_translate_duration));
            translateText.start();
        }
    }

    private void animateItemOut(View item, boolean fromLeft)
    {
    	Resources resources = mContext.getResources();
    	
        View background = (View) item.findViewById(R.id.background);
        ImageView icon = (ImageView) item.findViewById(R.id.icon);
        TextView text = (TextView) item.findViewById(R.id.text);

        ObjectAnimator fadeBackground = ObjectAnimator.ofFloat(background, View.ALPHA, 1, 0);
        fadeBackground.setDuration(resources.getInteger(R.integer.edge_swipe_translate_duration));
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

        ObjectAnimator translateIcon = ObjectAnimator.ofFloat(icon, View.TRANSLATION_X, 0);
        translateIcon.setInterpolator(new DecelerateInterpolator());
        translateIcon.setDuration(resources.getInteger(R.integer.edge_swipe_translate_duration));
        translateIcon.start();
        ObjectAnimator translateText = ObjectAnimator.ofFloat(text, View.TRANSLATION_X, 0);
        translateText.setInterpolator(new DecelerateInterpolator());
        translateText.setDuration(resources.getInteger(R.integer.edge_swipe_translate_duration));
        translateText.start();
    }
}
