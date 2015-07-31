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
package com.fairphone.fplauncher3.oobe.animation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.fairphone.fplauncher3.DynamicGrid;
import com.fairphone.fplauncher3.R;
import com.fairphone.fplauncher3.oobe.utils.KWAnimation;
import com.fairphone.fplauncher3.oobe.utils.KWAnimation.KWValueAnimation;
import com.fairphone.fplauncher3.oobe.utils.KWAnimation.KWValueType;
import com.fairphone.fplauncher3.oobe.utils.KWAnimationGroup;
import com.fairphone.fplauncher3.oobe.utils.KWAnimationGroup.KWAnimationGroupListener;
import com.fairphone.fplauncher3.oobe.utils.KWAnimationManager;
import com.fairphone.fplauncher3.oobe.utils.KWSprite;

public class MenuTutorialAnimationView extends FrameLayout {

    private int mHandStopPointXinDP;
    private int mArrowStopPointXinDP;

    public interface MenuTutorialAnimationViewListener {
        public void OnAnimationFinished(MenuTutorialAnimationView view);
    }

    private int SCREEN_WIDTH = 540;
    private int SCREEN_HEIGHT = 960;

    KWSprite spriteRoot;

    KWSprite spriteArrow;
    KWSprite spriteHand;
    KWSprite spriteHandShadow;
    KWSprite spriteMenu;
    KWSprite spriteAppIcon;
    KWSprite spriteAppIconSelected;

    Paint spritePaint;

    KWAnimationGroup animationGroupSwipe;
    KWAnimationGroup animationGroupSelectApp;
    KWAnimationManager animationManager;

    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
    private AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
    long prevFrame = 0;

    MenuTutorialAnimationViewListener listener;
    private DisplayMetrics mDisplayMetrics;

    public MenuTutorialAnimationView(Context context) {
        super(context);
        init();
    }

    public MenuTutorialAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MenuTutorialAnimationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void setMenuTutorialAnimationViewListener(MenuTutorialAnimationViewListener listener) {
        this.listener = listener;
    }

    private void init() {

        WindowManager windowMananger = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        mDisplayMetrics = new DisplayMetrics();
        windowMananger.getDefaultDisplay().getMetrics(mDisplayMetrics);

        SCREEN_HEIGHT=mDisplayMetrics.heightPixels;
        SCREEN_WIDTH=mDisplayMetrics.widthPixels;

        if (getBackground() == null) {
            setBackgroundColor(0x00000000);
        }

        spritePaint = new Paint();
        spritePaint.setColor(0xffffffff);

        spriteRoot = new KWSprite();
        spriteArrow = new KWSprite();
        spriteHand = new KWSprite();
        spriteHandShadow = new KWSprite();
        spriteMenu = new KWSprite();
        spriteAppIcon = new KWSprite();
        spriteAppIconSelected = new KWSprite();

        spriteArrow.drawable = ContextCompat.getDrawable(getContext(), R.drawable.arrow_gradient_oobe);
        spriteArrow.applySizeFromDrawable();
        spriteHand.drawable = ContextCompat.getDrawable(getContext(), R.drawable.icn_hand_oobe);
        spriteHand.applySizeFromDrawable();
        spriteHandShadow.drawable = ContextCompat.getDrawable(getContext(), R.drawable.oobe_hand_shadow);
        spriteHandShadow.applySizeFromDrawable();
        spriteMenu.drawable = ContextCompat.getDrawable(getContext(), R.drawable.oobe_menu);
        spriteMenu.applySizeFromDrawable();
        spriteAppIcon.drawable = ContextCompat.getDrawable(getContext(), R.drawable.icn_circle_oobe);
        spriteAppIcon.applySizeFromDrawable();
        spriteAppIconSelected.drawable = ContextCompat.getDrawable(getContext(), R.drawable.icn_circle_glow_oobe);
        spriteAppIconSelected.applySizeFromDrawable();

        spriteRoot.addChild(spriteArrow);
        spriteRoot.addChild(spriteHand);
        //spriteRoot.addChild(spriteHandShadow);
        spriteRoot.addChild(spriteMenu);
        spriteMenu.addChild(spriteAppIcon);
        //spriteMenu.addChild(spriteAppIconSelected);

        spriteRoot.alpha = 0;

        spriteAppIconSelected.alpha = 0;

        // Animation setup

        animationManager = new KWAnimationManager();
        animationGroupSwipe = new KWAnimationGroup(animationManager);
        animationGroupSelectApp = new KWAnimationGroup(animationManager);


        mHandStopPointXinDP = SCREEN_WIDTH - DynamicGrid.pxFromDp(100,mDisplayMetrics);
        mArrowStopPointXinDP = SCREEN_WIDTH - DynamicGrid.pxFromDp(120,mDisplayMetrics);

        setupSwipeAnimation();
        setupAppOpenAnimation();
    }

    public void playSwipeAnimation() {
        animationGroupSwipe.start();
    }

    public void playAppOpenAnimation() {
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
        spriteMenu.draw(canvas, spritePaint);
        spriteAppIcon.draw(canvas, spritePaint);
        spriteAppIconSelected.draw(canvas, spritePaint);
        spriteHandShadow.draw(canvas, spritePaint);
        spriteHand.draw(canvas, spritePaint);
        animationManager.update();
        postInvalidate();
    }

    private void setupSwipeAnimation() {
        KWAnimation rootSwipeAnimation = new KWAnimation(spriteRoot);
        KWValueAnimation alphaAnim = rootSwipeAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(1, 1700, null);
        alphaAnim.addKeyframe(0, 2000, accelerateInterpolator);

        KWAnimation fingerSwipeAnimation = new KWAnimation(spriteHand);
        KWValueAnimation xAnim = fingerSwipeAnimation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe(SCREEN_WIDTH, 350, null);
        xAnim.addKeyframe(mHandStopPointXinDP, 700, decelerateInterpolator);
        xAnim.addKeyframe(mHandStopPointXinDP, 1700, null);

        KWAnimation arrowSwipeAnimation = new KWAnimation(spriteArrow);
        xAnim = arrowSwipeAnimation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe(SCREEN_WIDTH, 0, null);
        xAnim.addKeyframe(mArrowStopPointXinDP, 500, decelerateInterpolator);

        KWAnimation menuSwipeAnimation = new KWAnimation(spriteMenu);
        KWValueAnimation scaleAnim = menuSwipeAnimation.addValueAnimation(KWValueType.Scale);
        scaleAnim.addKeyframe(0.8f, 600, null);
        scaleAnim.addKeyframe(1.0f, 800, decelerateInterpolator);
        alphaAnim = menuSwipeAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 600, null);
        alphaAnim.addKeyframe(1.0f, 750, decelerateInterpolator);

        animationGroupSwipe.addAnimation(fingerSwipeAnimation);
        animationGroupSwipe.addAnimation(arrowSwipeAnimation);
        animationGroupSwipe.addAnimation(rootSwipeAnimation);
        animationGroupSwipe.addAnimation(menuSwipeAnimation);

        animationGroupSwipe.setAnimationGroupListener(new KWAnimationGroupListener() {
            @Override
            public void onAnimationGroupStarted(KWAnimationGroup group) {
                spriteRoot.clearTransform(true);
                spriteRoot.alpha = 0;
                spriteArrow.alpha = 1;
                spriteHand.alpha = 1;
                spriteHandShadow.alpha = 0;
                spriteMenu.alpha = 1;
                spriteAppIcon.alpha = 1;
                spriteAppIconSelected.alpha = 0;

                spriteHand.y = SCREEN_HEIGHT / 2;
                spriteArrow.y = SCREEN_HEIGHT / 2;
                spriteArrow.pivotY = 0.5f;
                spriteMenu.y = SCREEN_HEIGHT / 2;
                spriteMenu.x = SCREEN_WIDTH - DynamicGrid.pxFromDp(67,mDisplayMetrics);
                spriteMenu.pivotX = 1.0f;
                spriteMenu.pivotY = 0.5f;
                spriteAppIcon.x = -spriteMenu.pivotX * spriteMenu.width + DynamicGrid.pxFromDp(35,mDisplayMetrics);
                spriteAppIcon.y = -spriteMenu.pivotY * spriteMenu.height + DynamicGrid.pxFromDp(45,mDisplayMetrics);
                spriteAppIconSelected.x = -spriteMenu.pivotX * spriteMenu.width + DynamicGrid.pxFromDp(35,mDisplayMetrics);
                spriteAppIconSelected.y = -spriteMenu.pivotY * spriteMenu.height + DynamicGrid.pxFromDp(45,mDisplayMetrics);
            }

            @Override
            public void onAnimationGroupFinished(KWAnimationGroup group) {
                if (listener != null) {
                    listener.OnAnimationFinished(MenuTutorialAnimationView.this);
                }
            }
        });
    }

    private void setupAppOpenAnimation() {
        KWValueAnimation alphaAnim;
        KWValueAnimation xAnim;
        KWValueAnimation yAnim;
        KWValueAnimation scaleAnim;

        KWAnimation rootAnimation = new KWAnimation(spriteRoot);
        alphaAnim = rootAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 0, null);
        alphaAnim.addKeyframe(1, 300, decelerateInterpolator);
        alphaAnim.addKeyframe(1, 1950, null);
        alphaAnim.addKeyframe(0, 2250, decelerateInterpolator);

        KWAnimation handAnimation = new KWAnimation(spriteHand);
        xAnim = handAnimation.addValueAnimation(KWValueType.X);
        yAnim = handAnimation.addValueAnimation(KWValueType.Y);
        scaleAnim = handAnimation.addValueAnimation(KWValueType.Scale);
        xAnim.addKeyframe(mHandStopPointXinDP, 300, null);
        yAnim.addKeyframe(SCREEN_HEIGHT / 2, 300, null);

        xAnim.addKeyframe(DynamicGrid.pxFromDp(191,mDisplayMetrics), 700, accelerateDecelerateInterpolator);
        yAnim.addKeyframe(DynamicGrid.pxFromDp(233,mDisplayMetrics), 700, accelerateDecelerateInterpolator);

        xAnim.addKeyframe(DynamicGrid.pxFromDp(191,mDisplayMetrics), 1300, accelerateDecelerateInterpolator);
        yAnim.addKeyframe(DynamicGrid.pxFromDp(233,mDisplayMetrics), 1300, accelerateDecelerateInterpolator);
        scaleAnim.addKeyframe(1.0f, 1300, null);

        scaleAnim.addKeyframe(1.3f, 1700, accelerateDecelerateInterpolator);
        xAnim.addKeyframe(DynamicGrid.pxFromDp(207,mDisplayMetrics), 1700, accelerateDecelerateInterpolator);
        yAnim.addKeyframe(DynamicGrid.pxFromDp(215,mDisplayMetrics), 1700, accelerateDecelerateInterpolator);

        KWAnimation handShadowAnimation = new KWAnimation(spriteHandShadow);
        alphaAnim = handShadowAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 1300, null);
        alphaAnim.addKeyframe(1.0f, 1700, accelerateInterpolator);

        KWAnimation appIconAnimation = new KWAnimation(spriteAppIcon);
        alphaAnim = appIconAnimation.addValueAnimation(KWValueType.Alpha);
        scaleAnim = appIconAnimation.addValueAnimation(KWValueType.Scale);
        alphaAnim.addKeyframe(1.0f, 0, null);
        scaleAnim.addKeyframe(1.0f, 0, null);
        alphaAnim.addKeyframe(1.0f, 650, null);
        scaleAnim.addKeyframe(1.0f, 650, null);
        alphaAnim.addKeyframe(0, 650, null);
        scaleAnim.addKeyframe(0.9f, 650, null);
        alphaAnim.addKeyframe(1.0f, 830, decelerateInterpolator);
        scaleAnim.addKeyframe(1.4f, 1000, decelerateInterpolator);
        scaleAnim.addKeyframe(1.4f, 1550, null);
        alphaAnim.addKeyframe(1.0f, 1550, null);
        scaleAnim.addKeyframe(3.5f, 1900, decelerateInterpolator);
        alphaAnim.addKeyframe(0.0f, 1850, accelerateInterpolator);

        KWAnimation appIconSelectedAnimation = new KWAnimation(spriteAppIconSelected);
        alphaAnim = appIconSelectedAnimation.addValueAnimation(KWValueType.Alpha);
        scaleAnim = appIconSelectedAnimation.addValueAnimation(KWValueType.Scale);
        alphaAnim.addKeyframe(0, 650, null);
        scaleAnim.addKeyframe(0.9f, 650, null);
        alphaAnim.addKeyframe(1.0f, 730, accelerateInterpolator);
        scaleAnim.addKeyframe(1.0f, 750, decelerateInterpolator);

        KWAnimation menuAnimation = new KWAnimation(spriteMenu);
        alphaAnim = menuAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0.0f, 200, accelerateInterpolator);
        alphaAnim.addKeyframe(1, 400, null);
        alphaAnim.addKeyframe(1, 1900, null);
        alphaAnim.addKeyframe(0.0f, 2000, decelerateInterpolator);

        animationGroupSelectApp.addAnimation(rootAnimation);
        animationGroupSelectApp.addAnimation(handAnimation);
        animationGroupSelectApp.addAnimation(handShadowAnimation);
        animationGroupSelectApp.addAnimation(appIconAnimation);
        animationGroupSelectApp.addAnimation(appIconSelectedAnimation);
        animationGroupSelectApp.addAnimation(menuAnimation);

        animationGroupSelectApp.setAnimationGroupListener(new KWAnimationGroupListener() {
            @Override
            public void onAnimationGroupStarted(KWAnimationGroup group) {
                spriteRoot.clearTransform(true);
                spriteRoot.alpha = 0;
                spriteArrow.alpha = 0;
                spriteHand.alpha = 1;
                spriteHandShadow.alpha = 0;
                spriteMenu.alpha = 1;
                spriteAppIcon.alpha = 1;
                spriteAppIconSelected.alpha = 0;

                spriteHand.x = mHandStopPointXinDP;
                spriteHand.y = SCREEN_HEIGHT / 2;

                spriteHand.pivotX = 18.0f / spriteHand.width;
                spriteHand.pivotY = 21.0f / spriteHand.height;

                spriteHandShadow.x = DynamicGrid.pxFromDp(191,mDisplayMetrics);
                spriteHandShadow.y = DynamicGrid.pxFromDp(233,mDisplayMetrics);
                spriteHandShadow.pivotX = 54.0f / spriteHandShadow.width;
                spriteHandShadow.pivotY = 53.0f / spriteHandShadow.height;

                spriteArrow.x = mArrowStopPointXinDP;
                spriteArrow.y = SCREEN_HEIGHT / 2;

                spriteArrow.pivotX = 0.0f;
                spriteArrow.pivotY = 0.5f;

                spriteMenu.y = SCREEN_HEIGHT / 2;
                spriteMenu.x = SCREEN_WIDTH - DynamicGrid.pxFromDp(67,mDisplayMetrics);

                spriteMenu.pivotX = 1.0f;
                spriteMenu.pivotY = 0.5f;

                spriteAppIcon.pivotX = 0.5f;
                spriteAppIcon.pivotY = 0.5f;
                spriteAppIcon.x = -spriteMenu.pivotX * spriteMenu.width + DynamicGrid.pxFromDp(35,mDisplayMetrics) + spriteAppIcon.pivotX * spriteAppIcon.width;
                spriteAppIcon.y = -spriteMenu.pivotY * spriteMenu.height + DynamicGrid.pxFromDp(45,mDisplayMetrics) + spriteAppIcon.pivotX * spriteAppIcon.width;

                spriteAppIconSelected.pivotX = 0.5f;
                spriteAppIconSelected.pivotY = 0.5f;
                spriteAppIconSelected.x = -spriteMenu.pivotX * spriteMenu.width + DynamicGrid.pxFromDp(35,mDisplayMetrics) + spriteAppIconSelected.pivotX * spriteAppIconSelected.width;
                spriteAppIconSelected.y = -spriteMenu.pivotY * spriteMenu.height + DynamicGrid.pxFromDp(45,mDisplayMetrics) + spriteAppIconSelected.pivotX * spriteAppIconSelected.width;
            }

            @Override
            public void onAnimationGroupFinished(KWAnimationGroup group) {
                if (listener != null) {
                    listener.OnAnimationFinished(MenuTutorialAnimationView.this);
                }
            }
        });
    }
}
