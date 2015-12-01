package com.fairphone.fplauncher3.oobe.animation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.fairphone.fplauncher3.DynamicGrid;
import com.fairphone.fplauncher3.R;
import com.fairphone.fplauncher3.oobe.utils.KWAnimation;
import com.fairphone.fplauncher3.oobe.utils.KWAnimation.KWValueAnimation;
import com.fairphone.fplauncher3.oobe.utils.KWAnimation.KWValueType;
import com.fairphone.fplauncher3.oobe.utils.KWAnimationGroup;
import com.fairphone.fplauncher3.oobe.utils.KWAnimationGroup.KWAnimationGroupListener;
import com.fairphone.fplauncher3.oobe.utils.KWAnimationManager;
import com.fairphone.fplauncher3.oobe.utils.KWSprite;

/**
 * Created by kwamecorp on 7/28/15.
 */
public class EdgeSwipeTutorialAnimationView extends FrameLayout {

    private int mHandStopPointXinDP;
    private int mArrowStopPointXinDP;
    int mMarginTop;


    public interface EdgeSwipeTutorialAnimationViewListener {
        public void OnAnimationFinished(EdgeSwipeTutorialAnimationView view);
    }

    int SCREEN_WIDTH = 540;
    int SCREEN_HEIGHT = 960;

    KWSprite spriteRoot;

    KWSprite spriteArrow;
    KWSprite spriteHand;
    KWSprite spriteAppIconSelectedBlue;
    KWSprite spriteAppIconSelected;
    KWSprite spriteAllAppsIcon;
    KWSprite spriteAppIconWithName1;
    KWSprite spriteAppIconWithName2;
    KWSprite spriteAppIconWithName3;
    KWSprite spriteAppIconWithName4;
    KWSprite spriteAppSelectionShadow;

    Paint spritePaint;

    KWAnimationGroup animationGroupSwipe;
    KWAnimationGroup animationGroupSelectApp;
    KWAnimationManager animationManager;

    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
    private AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();

    EdgeSwipeTutorialAnimationViewListener listener;
    private DisplayMetrics mDisplayMetrics;

    public EdgeSwipeTutorialAnimationView(Context context) {
        super(context);
        init();
    }

    public EdgeSwipeTutorialAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EdgeSwipeTutorialAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void setEdgeSwipeTutorialAnimationViewListener(EdgeSwipeTutorialAnimationViewListener listener) {
        this.listener = listener;
    }

    private void init(){
        WindowManager windowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        mDisplayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);

        SCREEN_HEIGHT=mDisplayMetrics.heightPixels;
        SCREEN_WIDTH=mDisplayMetrics.widthPixels;

        if (getBackground() == null) {
            setBackgroundColor(0x00000000);
        }

        spriteRoot = new KWSprite();
        spriteArrow = new KWSprite();
        spriteHand = new KWSprite();
        spriteAppIconSelectedBlue = new KWSprite();
        spriteAppIconSelected = new KWSprite();
        spriteAllAppsIcon = new KWSprite();
        spriteAppIconWithName1 = new KWSprite();
        spriteAppIconWithName2 = new KWSprite();
        spriteAppIconWithName3 = new KWSprite();
        spriteAppIconWithName4 = new KWSprite();
        spriteAppSelectionShadow = new KWSprite();

        spritePaint = new Paint();
        spritePaint.setColor(0xffffffff);

        spriteArrow.drawable = ContextCompat.getDrawable(getContext(), R.drawable.tutorial_swipe_arrow);
        spriteArrow.applySizeFromDrawable();
        spriteHand.drawable = ContextCompat.getDrawable(getContext(), R.drawable.tutorial_hand);
        spriteHand.applySizeFromDrawable();
        spriteAllAppsIcon.drawable = ContextCompat.getDrawable(getContext(), R.drawable.tutorial_all_apps);
        spriteAllAppsIcon.applySizeFromDrawable();
        spriteAppIconSelected.drawable = ContextCompat.getDrawable(getContext(), R.drawable.tutorial_app_selected);
        spriteAppIconSelected.applySizeFromDrawable();
        spriteAppIconSelectedBlue.drawable = ContextCompat.getDrawable(getContext(), R.drawable.tutorial_app_selected_copy);
        spriteAppIconSelectedBlue.applySizeFromDrawable();
        spriteAppIconWithName1.drawable = ContextCompat.getDrawable(getContext(), R.drawable.circle_appname_together);
        spriteAppIconWithName1.applySizeFromDrawable();
        spriteAppIconWithName2.drawable = ContextCompat.getDrawable(getContext(), R.drawable.circle_appname_together);
        spriteAppIconWithName2.applySizeFromDrawable();
        spriteAppIconWithName3.drawable = ContextCompat.getDrawable(getContext(), R.drawable.circle_appname_together);
        spriteAppIconWithName3.applySizeFromDrawable();
        spriteAppIconWithName4.drawable = ContextCompat.getDrawable(getContext(), R.drawable.circle_appname_together);
        spriteAppIconWithName4.applySizeFromDrawable();
        spriteAppSelectionShadow.drawable = ContextCompat.getDrawable(getContext(), R.drawable.tutorial_app_selection_shader);
        spriteAppSelectionShadow.applySizeFromDrawable();
        spriteAppSelectionShadow.width = SCREEN_WIDTH;

        spriteRoot.addChild(spriteArrow);
        spriteRoot.addChild(spriteAllAppsIcon);
        spriteRoot.addChild(spriteAppIconSelected);
        spriteRoot.addChild(spriteAppIconSelectedBlue);
        spriteRoot.addChild(spriteHand);
        spriteRoot.addChild(spriteAppIconWithName1);
        spriteRoot.addChild(spriteAppIconWithName2);
        spriteRoot.addChild(spriteAppIconWithName3);
        spriteRoot.addChild(spriteAppIconWithName4);
        spriteRoot.addChild(spriteAppSelectionShadow);

        spriteRoot.alpha = 0;

        animationManager = new KWAnimationManager();
        animationGroupSwipe = new KWAnimationGroup(animationManager);
        animationGroupSelectApp = new KWAnimationGroup(animationManager);

        mArrowStopPointXinDP = SCREEN_WIDTH - DynamicGrid.pxFromDp(120, mDisplayMetrics);
        mHandStopPointXinDP = SCREEN_WIDTH - DynamicGrid.pxFromDp(100,mDisplayMetrics);
        mMarginTop = DynamicGrid.pxFromDp(55f, mDisplayMetrics);

        setupSwipeAnimation();
        setupOpenAppAnimation();

    }


    public void playSwipeAnimation(){
        animationGroupSwipe.start();
    }

    public void playOpenAppAnimation(){
        animationGroupSelectApp.start();
    }

    public void stopAnimations() {
        animationGroupSwipe.stop();
        animationGroupSelectApp.stop();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        spriteRoot.resetMatrix();
        spriteArrow.draw(canvas, spritePaint);
        spriteAppIconSelectedBlue.draw(canvas, spritePaint);
        spriteAppIconSelected.draw(canvas, spritePaint);
        spriteAllAppsIcon.draw(canvas, spritePaint);
        spriteHand.draw(canvas, spritePaint);
        spriteAppIconWithName1.draw(canvas, spritePaint);
        spriteAppIconWithName2.draw(canvas, spritePaint);
        spriteAppIconWithName3.draw(canvas, spritePaint);
        spriteAppIconWithName4.draw(canvas, spritePaint);
        spriteAppSelectionShadow.draw(canvas,spritePaint);
        animationManager.update();
        postInvalidate();
    }

    private void setupSwipeAnimation(){
        KWAnimation rootSwipeAnimation = new KWAnimation(spriteRoot);
        KWValueAnimation alphaAnim = rootSwipeAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(1, 1700, null);

        KWAnimation fingerSwipeAnimation = new KWAnimation(spriteHand);
        alphaAnim = fingerSwipeAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 0, null);
        alphaAnim.addKeyframe(1.0f, 1300, null);
        KWValueAnimation xAnim = fingerSwipeAnimation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe(SCREEN_WIDTH, 1050, null);
        xAnim.addKeyframe(mHandStopPointXinDP, 1400, decelerateInterpolator);
        xAnim.addKeyframe(mHandStopPointXinDP, 2600, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(35f,mDisplayMetrics)), 3000, accelerateDecelerateInterpolator);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(35f,mDisplayMetrics)), 3300, null);
        xAnim.addKeyframe(((SCREEN_WIDTH) + DynamicGrid.pxFromDp(70f,mDisplayMetrics)), 4400, accelerateInterpolator);
        KWValueAnimation yAnim = fingerSwipeAnimation.addValueAnimation(KWValueType.Y);
        yAnim.addKeyframe((SCREEN_HEIGHT / 2) + mMarginTop, 2600, null);
        yAnim.addKeyframe((SCREEN_HEIGHT / 2) - DynamicGrid.pxFromDp(55f,mDisplayMetrics) + mMarginTop, 3000, accelerateDecelerateInterpolator);
        yAnim.addKeyframe((SCREEN_HEIGHT / 2) - DynamicGrid.pxFromDp(55f,mDisplayMetrics)+ mMarginTop, 3300, null);
        yAnim.addKeyframe((SCREEN_HEIGHT / 2) - DynamicGrid.pxFromDp(80f,mDisplayMetrics)+ mMarginTop, 4400, decelerateInterpolator);
        KWValueAnimation scaleAnim = fingerSwipeAnimation.addValueAnimation(KWValueType.Scale);
        scaleAnim.addKeyframe(1.0f, 3300, null);
        scaleAnim.addKeyframe(15.0f, 4300, accelerateDecelerateInterpolator);
        alphaAnim = fingerSwipeAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(1.0f, 3300, null);
        alphaAnim.addKeyframe(0f, 3900, accelerateDecelerateInterpolator);

        KWAnimation arrowSwipeAnimation = new KWAnimation(spriteArrow);
        xAnim = arrowSwipeAnimation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe(SCREEN_WIDTH, 700, null);
        xAnim.addKeyframe(mArrowStopPointXinDP, 1300, decelerateInterpolator);
        xAnim.addKeyframe(mArrowStopPointXinDP, 2400, null);
        alphaAnim = arrowSwipeAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(1.0f, 3300, null);
        alphaAnim.addKeyframe(0f, 3700, accelerateInterpolator);

        KWAnimation allAppsIconAnimation = new KWAnimation(spriteAllAppsIcon);
        alphaAnim = allAppsIconAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 1300, null);
        alphaAnim.addKeyframe(1, 1800, accelerateInterpolator);
        alphaAnim.addKeyframe(1.0f, 3300, null);
        alphaAnim.addKeyframe(0f, 3700, accelerateInterpolator);
        xAnim = allAppsIconAnimation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(100f,mDisplayMetrics), 1200, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(50f,mDisplayMetrics)), 1800, decelerateInterpolator);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(50f,mDisplayMetrics)), 2200, null);

        KWAnimation appIcon1Animation = new KWAnimation(spriteAppIconWithName1);
        alphaAnim = appIcon1Animation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 1300, null);
        alphaAnim.addKeyframe(1, 1800, accelerateInterpolator);
        alphaAnim.addKeyframe(1.0f, 3300, null);
        alphaAnim.addKeyframe(0f, 3700, accelerateInterpolator);
        xAnim = appIcon1Animation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(100f,mDisplayMetrics), 1200, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(50f,mDisplayMetrics)), 1800, decelerateInterpolator);

        KWAnimation appIcon2Animation = new KWAnimation(spriteAppIconWithName2);
        alphaAnim = appIcon2Animation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 1300, null);
        alphaAnim.addKeyframe(1, 1800, accelerateInterpolator);
        alphaAnim.addKeyframe(1.0f, 3300, null);
        alphaAnim.addKeyframe(0f, 3700, accelerateInterpolator);
        alphaAnim.addKeyframe(0, 3900, null);
        //alphaAnim.addKeyframe(1, 4250, null);
        xAnim = appIcon2Animation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(100f,mDisplayMetrics), 1200, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(50f,mDisplayMetrics)), 1800, decelerateInterpolator);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(50f,mDisplayMetrics)), 2800, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(35f,mDisplayMetrics)), 3300, decelerateInterpolator);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(35f,mDisplayMetrics)), 3700, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(50f,mDisplayMetrics)), 4000, null);

        KWAnimation appIcon3Animation = new KWAnimation(spriteAppIconWithName3);
        alphaAnim = appIcon3Animation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 1300, null);
        alphaAnim.addKeyframe(1, 1800, accelerateInterpolator);
        alphaAnim.addKeyframe(1.0f, 3300, null);
        alphaAnim.addKeyframe(0f, 3700, accelerateInterpolator);
        xAnim = appIcon3Animation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(100f,mDisplayMetrics), 1200, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(50f,mDisplayMetrics)), 1800, decelerateInterpolator);
        //xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(50f,mDisplayMetrics)), 1500, null);

        KWAnimation appIcon4Animation = new KWAnimation(spriteAppIconWithName4);
        alphaAnim = appIcon4Animation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 1300, null);
        alphaAnim.addKeyframe(1, 1800, accelerateInterpolator);
        alphaAnim.addKeyframe(1.0f, 3300, null);
        alphaAnim.addKeyframe(0f, 3700, accelerateInterpolator);
        xAnim = appIcon4Animation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(100f,mDisplayMetrics), 1200, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(50f,mDisplayMetrics)), 1800, decelerateInterpolator);
        //xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(50f,mDisplayMetrics)), 1500, null);

        KWAnimation appShadowAnimation = new KWAnimation(spriteAppSelectionShadow);
        alphaAnim = appShadowAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 2700, null);
        alphaAnim.addKeyframe(1, 2800, accelerateInterpolator);
        alphaAnim.addKeyframe(1.0f, 3300, null);
        alphaAnim.addKeyframe(0f, 3700, accelerateInterpolator);

        KWAnimation appIconSelectedAnimation = new KWAnimation(spriteAppIconSelected);
        alphaAnim = appIconSelectedAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 2700, null);
        alphaAnim.addKeyframe(1, 2800, accelerateInterpolator);
        alphaAnim.addKeyframe(1, 3300, null);
        alphaAnim.addKeyframe(0f, 3301, null);
        xAnim = appIconSelectedAnimation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(31.3f,mDisplayMetrics), 2800, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(16.3f,mDisplayMetrics)), 3300, decelerateInterpolator);
        scaleAnim = appIconSelectedAnimation.addValueAnimation(KWValueType.Scale);
        scaleAnim.addKeyframe(1f, 2800, null);
       /* scaleAnim.addKeyframe(1f, 2600, null);
        scaleAnim.addKeyframe(20.0f, 4000, decelerateInterpolator);*/

        KWAnimation appIconSelectedBlueAnimation = new KWAnimation(spriteAppIconSelectedBlue);
        alphaAnim = appIconSelectedBlueAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 2700, null);
        alphaAnim.addKeyframe(1, 2800, accelerateInterpolator);
        alphaAnim.addKeyframe(1, 3300, null);
        alphaAnim.addKeyframe(0f, 4400, accelerateDecelerateInterpolator);
        xAnim = appIconSelectedBlueAnimation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(31.3f,mDisplayMetrics), 2800, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(16.3f,mDisplayMetrics)), 3300, decelerateInterpolator);
        scaleAnim = appIconSelectedBlueAnimation.addValueAnimation(KWValueType.Scale);
        scaleAnim.addKeyframe(1f, 2800, null);
        scaleAnim.addKeyframe(1f, 3300, null);
        scaleAnim.addKeyframe(20.0f, 4400, accelerateDecelerateInterpolator);

        animationGroupSwipe.addAnimation(fingerSwipeAnimation);
        animationGroupSwipe.addAnimation(arrowSwipeAnimation);
        animationGroupSwipe.addAnimation(rootSwipeAnimation);
        animationGroupSwipe.addAnimation(allAppsIconAnimation);
        animationGroupSwipe.addAnimation(appIcon1Animation);
        animationGroupSwipe.addAnimation(appIcon2Animation);
        animationGroupSwipe.addAnimation(appIcon3Animation);
        animationGroupSwipe.addAnimation(appIcon4Animation);
        animationGroupSwipe.addAnimation(appShadowAnimation);
        animationGroupSwipe.addAnimation(appIconSelectedAnimation);
        animationGroupSwipe.addAnimation(appIconSelectedBlueAnimation);

        animationGroupSwipe.setAnimationGroupListener(new KWAnimationGroupListener() {
            @Override
            public void onAnimationGroupStarted(KWAnimationGroup group) {
                spriteRoot.clearTransform(true);
                spriteRoot.alpha = 0;
                spriteAppSelectionShadow.alpha = 0;
                spriteAppIconSelected.alpha = 0;
                spriteArrow.alpha = 1;
                spriteHand.alpha = 0;
                spriteAllAppsIcon.alpha = 1;
                spriteAppIconWithName1.alpha = 1;
                spriteAppIconWithName2.alpha = 1;
                spriteAppIconWithName3.alpha = 1;
                spriteAppIconWithName4.alpha = 1;
                spriteAppIconSelectedBlue.alpha = 0;

                spriteHand.y = (SCREEN_HEIGHT / 2) + mMarginTop;
                spriteArrow.y = (SCREEN_HEIGHT / 2) + mMarginTop;
                spriteArrow.pivotY = 0.5f;
                spriteAllAppsIcon.y = (SCREEN_HEIGHT / 2) + mMarginTop;
                spriteAllAppsIcon.x = SCREEN_WIDTH / 2;
                spriteAllAppsIcon.pivotX = 1f;
                spriteAllAppsIcon.pivotY = 0.5f;

                spriteAppIconWithName1.y = (SCREEN_HEIGHT / 2) - DynamicGrid.pxFromDp(83.3f,mDisplayMetrics) + mMarginTop;
                spriteAppIconWithName1.x = (SCREEN_WIDTH / 2);
                spriteAppIconWithName1.pivotX = 1f;
                spriteAppIconWithName1.pivotY = 1f;

                spriteAppIconWithName2.y = (SCREEN_HEIGHT / 2) - DynamicGrid.pxFromDp(33.4f,mDisplayMetrics) + mMarginTop;
                spriteAppIconWithName2.x = (SCREEN_WIDTH / 2);
                spriteAppIconWithName2.pivotX = 1f;
                spriteAppIconWithName2.pivotY = 1f;

                spriteAppIconWithName3.y = (SCREEN_HEIGHT / 2) + DynamicGrid.pxFromDp(33.4f,mDisplayMetrics) + mMarginTop;
                spriteAppIconWithName3.x = (SCREEN_WIDTH / 2);
                spriteAppIconWithName3.pivotX = 1f;
                spriteAppIconWithName3.pivotY = 0f;

                spriteAppIconWithName4.y = (SCREEN_HEIGHT / 2) + DynamicGrid.pxFromDp(83.3f,mDisplayMetrics) + mMarginTop;
                spriteAppIconWithName4.x = (SCREEN_WIDTH / 2);
                spriteAppIconWithName4.pivotX = 1f;
                spriteAppIconWithName4.pivotY = 0f;

                spriteAppSelectionShadow.y = (SCREEN_HEIGHT / 2) - DynamicGrid.pxFromDp(52f,mDisplayMetrics) + mMarginTop;
                spriteAppSelectionShadow.x = (SCREEN_WIDTH / 2) + DynamicGrid.pxFromDp(50f,mDisplayMetrics);
                spriteAppSelectionShadow.pivotX = 1f;
                spriteAppSelectionShadow.pivotY = 0.5f;

                spriteAppIconSelected.y = (SCREEN_HEIGHT / 2) - DynamicGrid.pxFromDp(51.9f,mDisplayMetrics) + mMarginTop;
                spriteAppIconSelected.x = (SCREEN_WIDTH / 2) + DynamicGrid.pxFromDp(33.3f,mDisplayMetrics);
                spriteAppIconSelected.pivotX = 0.5f;
                spriteAppIconSelected.pivotY = 0.5f;

                spriteAppIconSelectedBlue.y = (SCREEN_HEIGHT / 2) - DynamicGrid.pxFromDp(51.9f,mDisplayMetrics) + mMarginTop;
                spriteAppIconSelectedBlue.x = (SCREEN_WIDTH / 2) + DynamicGrid.pxFromDp(33.3f,mDisplayMetrics);
                spriteAppIconSelectedBlue.pivotX = 0.5f;
                spriteAppIconSelectedBlue.pivotY = 0.5f;
            }

            @Override
            public void onAnimationGroupFinished(KWAnimationGroup group) {
                if (listener != null) {
                    listener.OnAnimationFinished(EdgeSwipeTutorialAnimationView.this);
                }
            }
        });
    }

    private void setupOpenAppAnimation(){

        KWAnimation rootSwipeAnimation = new KWAnimation(spriteRoot);
        KWValueAnimation alphaAnim = rootSwipeAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(1, 1700, null);

        KWAnimation fingerSwipeAnimation = new KWAnimation(spriteHand);
        KWValueAnimation xAnim = fingerSwipeAnimation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe(mHandStopPointXinDP, 0, null);
        xAnim.addKeyframe(mHandStopPointXinDP, 1700, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(35f,mDisplayMetrics)), 2100, accelerateDecelerateInterpolator);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(35f,mDisplayMetrics)), 2100, null);
        xAnim.addKeyframe(((SCREEN_WIDTH) + DynamicGrid.pxFromDp(70f,mDisplayMetrics)), 5000, accelerateInterpolator);
        KWValueAnimation yAnim = fingerSwipeAnimation.addValueAnimation(KWValueType.Y);
        yAnim.addKeyframe((SCREEN_HEIGHT / 2) + mMarginTop, 1700, null);
        yAnim.addKeyframe((SCREEN_HEIGHT / 2) - DynamicGrid.pxFromDp(55f,mDisplayMetrics) + mMarginTop, 2100, accelerateDecelerateInterpolator);
        yAnim.addKeyframe((SCREEN_HEIGHT / 2) - DynamicGrid.pxFromDp(55f,mDisplayMetrics) + mMarginTop, 2100, null);
        yAnim.addKeyframe((SCREEN_HEIGHT / 2) - DynamicGrid.pxFromDp(80f,mDisplayMetrics) + mMarginTop, 5000, decelerateInterpolator);
        KWValueAnimation scaleAnim = fingerSwipeAnimation.addValueAnimation(KWValueType.Scale);
        scaleAnim.addKeyframe(0.1f, 0, null);
        scaleAnim.addKeyframe(1.15f, 300, null);
        scaleAnim.addKeyframe(1.0f, 600, null);
        scaleAnim.addKeyframe(1.0f, 2100, null);
        scaleAnim.addKeyframe(15.0f, 3500, accelerateDecelerateInterpolator);
        alphaAnim = fingerSwipeAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(1.0f, 1700, null);
        alphaAnim.addKeyframe(1.0f, 2100, null);
        alphaAnim.addKeyframe(0f, 3100, accelerateDecelerateInterpolator);

        KWAnimation appIcon2Animation = new KWAnimation(spriteAppIconWithName2);
        alphaAnim = appIcon2Animation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(1, 0, null);
        alphaAnim.addKeyframe(1, 2100, null);
        alphaAnim.addKeyframe(0, 2500, null);
        alphaAnim.addKeyframe(0, 2900, null);
        alphaAnim.addKeyframe(1, 3000, null);
        xAnim = appIcon2Animation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(50f,mDisplayMetrics), 0, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(50f,mDisplayMetrics)), 1900, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(35f,mDisplayMetrics)), 2100, decelerateInterpolator);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(35f,mDisplayMetrics)), 2400, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(50f,mDisplayMetrics)), 2700, null);

        KWAnimation appIconSelectedAnimation = new KWAnimation(spriteAppIconSelected);
        alphaAnim = appIconSelectedAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 1900, null);
        alphaAnim.addKeyframe(1, 2100, accelerateInterpolator);
        alphaAnim.addKeyframe(0f, 2301, accelerateInterpolator);
        xAnim = appIconSelectedAnimation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(31.3f,mDisplayMetrics), 1900, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(16.3f,mDisplayMetrics)), 2100, decelerateInterpolator);
        scaleAnim = appIconSelectedAnimation.addValueAnimation(KWValueType.Scale);
        scaleAnim.addKeyframe(1f, 1700, null);
        scaleAnim.addKeyframe(1f, 2300, null);
        scaleAnim.addKeyframe(0.0f, 2350, decelerateInterpolator);

        KWAnimation appIconSelectedBlueAnimation = new KWAnimation(spriteAppIconSelectedBlue);
        alphaAnim = appIconSelectedBlueAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 2100, null);
        alphaAnim.addKeyframe(1, 2200, accelerateInterpolator);
        alphaAnim.addKeyframe(0f, 4300, accelerateInterpolator);
        xAnim = appIconSelectedBlueAnimation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(31.3f,mDisplayMetrics), 1900, null);
        xAnim.addKeyframe(((SCREEN_WIDTH/2) + DynamicGrid.pxFromDp(16.3f,mDisplayMetrics)), 2100, decelerateInterpolator);
        scaleAnim = appIconSelectedBlueAnimation.addValueAnimation(KWValueType.Scale);
        scaleAnim.addKeyframe(0f, 1700, null);
        scaleAnim.addKeyframe(1f, 2100, null);
        scaleAnim.addKeyframe(1f, 2300, null);
        scaleAnim.addKeyframe(20.0f, 3500, accelerateDecelerateInterpolator);

        KWAnimation appShadowAnimation = new KWAnimation(spriteAppSelectionShadow);
        alphaAnim = appShadowAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 1900, null);
        alphaAnim.addKeyframe(1, 2100, accelerateInterpolator);
        alphaAnim.addKeyframe(0, 2900, decelerateInterpolator);

        animationGroupSelectApp.addAnimation(fingerSwipeAnimation);
        animationGroupSelectApp.addAnimation(appIcon2Animation);
        animationGroupSelectApp.addAnimation(appShadowAnimation);
        animationGroupSelectApp.addAnimation(appIconSelectedAnimation);
        animationGroupSelectApp.addAnimation(appIconSelectedBlueAnimation);

        animationGroupSelectApp.setAnimationGroupListener(new KWAnimationGroupListener() {
            @Override
            public void onAnimationGroupStarted(KWAnimationGroup group) {

                spriteAppSelectionShadow.alpha = 0;
                spriteAppIconSelected.alpha = 0;
                spriteArrow.alpha = 0;
                spriteHand.alpha = 1;
                spriteAllAppsIcon.alpha = 1;
                spriteAppIconWithName1.alpha = 1;
                spriteAppIconWithName2.alpha = 1;
                spriteAppIconWithName3.alpha = 1;
                spriteAppIconWithName4.alpha = 1;
                spriteAppIconSelectedBlue.alpha = 0;

                spriteHand.y = SCREEN_HEIGHT / 2;
                spriteHand.x = mHandStopPointXinDP;

                spriteAppIconWithName2.y = (SCREEN_HEIGHT / 2) - DynamicGrid.pxFromDp(33.4f,mDisplayMetrics) + mMarginTop;
                spriteAppIconWithName2.x = (SCREEN_WIDTH / 2);// + DynamicGrid.pxFromDp(77f,mDisplayMetrics);
                spriteAppIconWithName2.pivotX = 1f;
                spriteAppIconWithName2.pivotY = 1f;

                spriteAppSelectionShadow.y = (SCREEN_HEIGHT / 2) - DynamicGrid.pxFromDp(52f,mDisplayMetrics) + mMarginTop;
                spriteAppSelectionShadow.x = (SCREEN_WIDTH / 2) + DynamicGrid.pxFromDp(50f,mDisplayMetrics);
                spriteAppSelectionShadow.pivotX = 1f;
                spriteAppSelectionShadow.pivotY = 0.5f;

                spriteAppIconSelected.y = (SCREEN_HEIGHT / 2) - DynamicGrid.pxFromDp(51.9f,mDisplayMetrics) + mMarginTop;
                spriteAppIconSelected.x = (SCREEN_WIDTH / 2) + DynamicGrid.pxFromDp(33.3f,mDisplayMetrics);
                spriteAppIconSelected.pivotX = 0.5f;
                spriteAppIconSelected.pivotY = 0.5f;

                spriteAppIconSelectedBlue.y = (SCREEN_HEIGHT / 2) - DynamicGrid.pxFromDp(51.9f,mDisplayMetrics) + mMarginTop;
                spriteAppIconSelectedBlue.x = (SCREEN_WIDTH / 2) + DynamicGrid.pxFromDp(33.3f,mDisplayMetrics);
                spriteAppIconSelectedBlue.pivotX = 0.5f;
                spriteAppIconSelectedBlue.pivotY = 0.5f;
            }

            @Override
            public void onAnimationGroupFinished(KWAnimationGroup group) {
                if (listener != null) {
                    listener.OnAnimationFinished(EdgeSwipeTutorialAnimationView.this);
                }
            }
        });

    }
}
