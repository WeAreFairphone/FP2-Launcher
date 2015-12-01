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

/**
 * Created by kwamecorp on 7/22/15.
 */
public class CameraTutorialAnimationView extends FrameLayout {

    public interface CameraTutorialAnimationViewListener {
        public void OnAnimationFinished(CameraTutorialAnimationView view);
    }

    private int SCREEN_WIDTH = 540;
    private int SCREEN_HEIGHT = 960;

    Paint spritePaint;

    KWSprite spriteRoot;

    KWSprite spriteArrow;
    KWSprite spriteCircle;
    KWSprite spriteCameraIcon;

    KWAnimationGroup animationGroupCircle;
    KWAnimationGroup animationGroupArrowPoint;
    KWAnimationManager animationManager;

    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
    private AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();

    CameraTutorialAnimationViewListener listener;
    private DisplayMetrics mDisplayMetrics;

    public CameraTutorialAnimationView(Context context) {
        super(context);
        init();
    }

    public CameraTutorialAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraTutorialAnimationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void setCameraTutorialAnimationViewListener(CameraTutorialAnimationViewListener listener) {
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
        spriteCameraIcon = new KWSprite();
        spriteCircle = new KWSprite();

        spriteArrow.drawable = ContextCompat.getDrawable(getContext(), R.drawable.tutorial_hw_button_arrow);
        spriteArrow.applySizeFromDrawable();
        spriteCircle.drawable = ContextCompat.getDrawable(getContext(), R.drawable.tutorial_camera_bg_circle);
        spriteCircle.applySizeFromDrawable();
        spriteCameraIcon.drawable = ContextCompat.getDrawable(getContext(), R.drawable.tutorial_camera_icon);
        spriteCameraIcon.applySizeFromDrawable();

        spriteRoot.addChild(spriteArrow);
        spriteRoot.addChild(spriteCircle);
        spriteRoot.addChild(spriteCameraIcon);

        spriteRoot.alpha = 0;

        spriteCameraIcon.alpha = 0;
        spriteArrow.alpha = 0;


        animationManager = new KWAnimationManager();
        animationGroupCircle = new KWAnimationGroup(animationManager);
        animationGroupArrowPoint = new KWAnimationGroup(animationManager);

        setupCircleAnimation();
        setupArrowAnimation();
    }

    public void playCircleAnimation() {
        animationGroupCircle.start();
    }

    public void playArrowAnimation() {
        animationGroupArrowPoint.start();
    }

    public void stopAnimations() {
        animationGroupCircle.stop();
        animationGroupArrowPoint.stop();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        spriteRoot.resetMatrix();
        spriteArrow.draw(canvas, spritePaint);
        spriteCameraIcon.draw(canvas, spritePaint);
        spriteCircle.draw(canvas, spritePaint);
        animationManager.update();
        postInvalidate();
    }

    private void setupCircleAnimation() {
        KWAnimation rootAnimation = new KWAnimation(spriteRoot);

        KWValueAnimation alphaAnim = rootAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 0, null);
        alphaAnim.addKeyframe(1, 500, decelerateInterpolator);

        KWAnimation circleAnimation = new KWAnimation(spriteCircle);
        KWValueAnimation scaleAnim = circleAnimation.addValueAnimation(KWValueType.Scale);
        scaleAnim.addKeyframe(0f, 700, null);
        scaleAnim.addKeyframe(1.1f, 1000, decelerateInterpolator);
        scaleAnim.addKeyframe(1.0f, 1150, decelerateInterpolator);
        alphaAnim = circleAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 0, null);
        alphaAnim.addKeyframe(1.0f, 650, null);

        KWAnimation cameraIconAnimation = new KWAnimation(spriteCameraIcon);
        scaleAnim = cameraIconAnimation.addValueAnimation(KWValueType.Scale);
        scaleAnim.addKeyframe(0, 1200, null);
        scaleAnim.addKeyframe(1.1f, 1400, decelerateInterpolator);
        scaleAnim.addKeyframe(1.0f, 1550, decelerateInterpolator);
        alphaAnim = cameraIconAnimation.addValueAnimation(KWValueType.Alpha);
        //alphaAnim.addKeyframe(0, 1600, null);
        alphaAnim.addKeyframe(1.0f, 1200, null);

        KWAnimation arrowSwipeAnimation = new KWAnimation(spriteArrow);
        KWValueAnimation widthAnim = arrowSwipeAnimation.addValueAnimation(KWValueType.Width);
        alphaAnim = arrowSwipeAnimation.addValueAnimation(KWValueType.Alpha);
        //alphaAnim.addKeyframe(0, 2000, null);
        alphaAnim.addKeyframe(1.0f, 1700, null);

        widthAnim.addKeyframe((0), 1800, null);
        widthAnim.addKeyframe((spriteArrow.width + 10), 2200, accelerateDecelerateInterpolator);
        widthAnim.addKeyframe((spriteArrow.width - 110), 2500, accelerateDecelerateInterpolator);
        widthAnim.addKeyframe((spriteArrow.width - 100), 2600, accelerateDecelerateInterpolator);


        animationGroupCircle.addAnimation(rootAnimation);
        animationGroupCircle.addAnimation(circleAnimation);
        animationGroupCircle.addAnimation(cameraIconAnimation);
        animationGroupCircle.addAnimation(arrowSwipeAnimation);

        animationGroupCircle.setAnimationGroupListener(new KWAnimationGroupListener() {
            @Override
            public void onAnimationGroupStarted(KWAnimationGroup group) {
                spriteRoot.clearTransform(true);
                spriteRoot.alpha = 0;
                spriteArrow.alpha = 0;
                spriteCircle.alpha = 0;
                spriteCameraIcon.alpha = 0;

                spriteArrow.y = (SCREEN_HEIGHT - 650);
                spriteArrow.x = (SCREEN_WIDTH / 2) + (spriteCircle.drawable.getIntrinsicWidth()/2);
                spriteArrow.pivotY = 0.5f;

                spriteCircle.y = (SCREEN_HEIGHT - 650);
                spriteCircle.x = SCREEN_WIDTH / 2;
                spriteCircle.pivotX = 0.5f;
                spriteCircle.pivotY = 0.5f;

                spriteCameraIcon.y = (SCREEN_HEIGHT - 650);
                spriteCameraIcon.x = SCREEN_WIDTH / 2;
                spriteCameraIcon.pivotX = 0.5f;
                spriteCameraIcon.pivotY = 0.5f;
            }

            @Override
            public void onAnimationGroupFinished(KWAnimationGroup group) {
                if (listener != null) {
                    listener.OnAnimationFinished(CameraTutorialAnimationView.this);
                }
            }
        });

    }

    private void setupArrowAnimation() {

        KWAnimation arrowSwipeAnimation = new KWAnimation(spriteArrow);
        KWValueAnimation widthAnim = arrowSwipeAnimation.addValueAnimation(KWValueType.Width);
        widthAnim.addKeyframe((spriteArrow.width - 100), 2100, null);
        widthAnim.addKeyframe((spriteArrow.width + 20), 2400, accelerateInterpolator);
        widthAnim.addKeyframe((spriteArrow.width - 110), 2700, decelerateInterpolator);
        widthAnim.addKeyframe((spriteArrow.width - 100), 2800, accelerateInterpolator);

        animationGroupArrowPoint.addAnimation(arrowSwipeAnimation);

        animationGroupArrowPoint.setAnimationGroupListener(new KWAnimationGroupListener() {
            @Override
            public void onAnimationGroupStarted(KWAnimationGroup group) {

                spriteArrow.y = (SCREEN_HEIGHT - 650);
                spriteArrow.x = (SCREEN_WIDTH / 2) + (spriteCircle.drawable.getIntrinsicWidth()/2);// - DynamicGrid.pxFromDp(110, mDisplayMetrics);
                spriteArrow.pivotY = 0.5f;
            }

            @Override
            public void onAnimationGroupFinished(KWAnimationGroup group) {
                if (listener != null) {
                    listener.OnAnimationFinished(CameraTutorialAnimationView.this);
                }
            }
        });
    }

}
