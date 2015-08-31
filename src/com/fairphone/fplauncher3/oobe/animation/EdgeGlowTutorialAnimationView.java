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
import com.fairphone.fplauncher3.oobe.utils.KWAnimationGroup;
import com.fairphone.fplauncher3.oobe.utils.KWAnimationManager;
import com.fairphone.fplauncher3.oobe.utils.KWSprite;

/**
 * Created by kwamecorp on 8/4/15.
 */
public class EdgeGlowTutorialAnimationView extends FrameLayout {


    public interface EdgeGlowTutorialAnimationViewListener {
        public void OnAnimationFinished(EdgeGlowTutorialAnimationView view);
    }

    private int SCREEN_WIDTH = 540;
    private int SCREEN_HEIGHT = 960;

    KWSprite spriteRoot;

    KWSprite spriteLeftGlow;
    KWSprite spriteRightGlow;

    Paint spritePaint;

    KWAnimationGroup animationGroupGlow;
    KWAnimationManager animationManager;

    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
    private AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();

    EdgeGlowTutorialAnimationViewListener listener;
    private DisplayMetrics mDisplayMetrics;

    public EdgeGlowTutorialAnimationView(Context context) {
        super(context);
        init();
    }

    public EdgeGlowTutorialAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EdgeGlowTutorialAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void setEdgeGlowTutorialAnimationViewListener(EdgeGlowTutorialAnimationViewListener listener) {
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
        spriteLeftGlow = new KWSprite();
        spriteRightGlow = new KWSprite();

        spritePaint = new Paint();
        spritePaint.setColor(0xffffffff);

        spriteLeftGlow.drawable = ContextCompat.getDrawable(getContext(), R.drawable.tutorial_edge_glow_left);
        spriteLeftGlow.applySizeFromDrawable();
        spriteRightGlow.drawable = ContextCompat.getDrawable(getContext(), R.drawable.tutorial_edge_glow_right);
        spriteRightGlow.applySizeFromDrawable();

        spriteRoot.addChild(spriteLeftGlow);
        spriteRoot.addChild(spriteRightGlow);

        spriteRoot.alpha = 0;

        animationManager = new KWAnimationManager();
        animationGroupGlow = new KWAnimationGroup(animationManager);


        setupGlowAnimation();
    }

    public void playGlowAnimation(){
        animationGroupGlow.start();
    }

    public void stopAnimations() {
        animationGroupGlow.stop();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        spriteRoot.resetMatrix();
        spriteLeftGlow.draw(canvas, spritePaint);
        spriteRightGlow.draw(canvas, spritePaint);
        animationManager.update();
        postInvalidate();
    }

    private void setupGlowAnimation() {

        KWAnimation rootSwipeAnimation = new KWAnimation(spriteRoot);
        KWAnimation.KWValueAnimation alphaAnim = rootSwipeAnimation.addValueAnimation(KWAnimation.KWValueType.Alpha);

        KWAnimation leftGlowAnimation = new KWAnimation(spriteLeftGlow);
        alphaAnim = leftGlowAnimation.addValueAnimation(KWAnimation.KWValueType.Alpha);
        alphaAnim.addKeyframe(0f, 0, null);
        alphaAnim.addKeyframe(1f, 1500, accelerateDecelerateInterpolator);
        alphaAnim.addKeyframe(1f, 1501, null);
        alphaAnim.addKeyframe(0f, 3000, accelerateDecelerateInterpolator);

        KWAnimation.KWValueAnimation xAnim = leftGlowAnimation.addValueAnimation(KWAnimation.KWValueType.Width);
        xAnim.addKeyframe(0, 0, null);
        xAnim.addKeyframe(120, 1500, accelerateDecelerateInterpolator);
        xAnim.addKeyframe(120, 1501, null);
        xAnim.addKeyframe(0, 3000, accelerateDecelerateInterpolator);

        KWAnimation rightGlowAnimation = new KWAnimation(spriteRightGlow);
        alphaAnim = rightGlowAnimation.addValueAnimation(KWAnimation.KWValueType.Alpha);
        alphaAnim.addKeyframe(0f, 0, null);
        alphaAnim.addKeyframe(1f, 1500, accelerateDecelerateInterpolator);
        alphaAnim.addKeyframe(1f, 1501, null);
        alphaAnim.addKeyframe(0f, 3000, accelerateDecelerateInterpolator);

        xAnim = rightGlowAnimation.addValueAnimation(KWAnimation.KWValueType.Width);
        xAnim.addKeyframe(0, 0, null);
        xAnim.addKeyframe(120, 1500, accelerateDecelerateInterpolator);
        xAnim.addKeyframe(120, 1501, null);
        xAnim.addKeyframe(0, 3000, accelerateDecelerateInterpolator);

        animationGroupGlow.addAnimation(leftGlowAnimation);
        animationGroupGlow.addAnimation(rightGlowAnimation);

        animationGroupGlow.setAnimationGroupListener(new KWAnimationGroup.KWAnimationGroupListener() {
            @Override
            public void onAnimationGroupStarted(KWAnimationGroup group) {
                spriteRoot.clearTransform(true);
                spriteRoot.alpha = 1f;
                spriteRightGlow.alpha = 1f;
                spriteLeftGlow.alpha = 1f;

                spriteLeftGlow.x = 0f;
                spriteLeftGlow.y = 0f;
                spriteLeftGlow.pivotX = 0f;
                spriteLeftGlow.pivotY = 0f;

                spriteRightGlow.x = SCREEN_WIDTH;
                spriteRightGlow.y = 0f;
                spriteRightGlow.pivotX = 1f;
                spriteRightGlow.pivotY = 0f;
            }

            @Override
            public void onAnimationGroupFinished(KWAnimationGroup group) {
                if (listener != null) {
                    listener.OnAnimationFinished(EdgeGlowTutorialAnimationView.this);
                }
            }
        });

    }
}
