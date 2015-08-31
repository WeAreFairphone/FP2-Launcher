package com.fairphone.fplauncher3.oobe.animation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.fairphone.fplauncher3.R;

/**
 * Created by kwamecorp on 8/4/15.
 */
public class EdgeGlowTutorialAnimationHelper implements TutorialAnimationHelper, EdgeGlowTutorialAnimationView.EdgeGlowTutorialAnimationViewListener {
    EdgeGlowTutorialAnimationView animView;
    View rootView;
    Context context;
    TutorialAnimationHelperListener listener;
    TutorialState curState = TutorialState.IdleInvisible;

    @Override
    public View setup(Context context) {
        this.context = context;

        rootView = LayoutInflater.from(context).inflate(R.layout.tutorial_edge_glow_layout, null);
        animView = (EdgeGlowTutorialAnimationView) rootView.findViewById(R.id.glowAnimationView);

        animView.playGlowAnimation();
        animView.stopAnimations();

        animView.setEdgeGlowTutorialAnimationViewListener(this);

        return rootView;
    }

    @Override
    public void setTutorialAnimationHelperListener(TutorialAnimationHelperListener listener) {
        this.listener = listener;
    }

    int curAnimationId = 0;
    TutorialViewAnimationListener curStateAnimationListener;

    private TutorialViewAnimationListener getCurStateAnimationListener() {
        return curStateAnimationListener;
    }

    @Override
    public boolean playIntro() {
        if (curState == TutorialState.Intro)
            return false;
        startAnimationState(TutorialState.Intro, null);
        Animation fadeAnim = AnimationUtils.loadAnimation(context, R.anim.tutorial_intro);
        fadeAnim.setAnimationListener(getCurStateAnimationListener());
        rootView.setVisibility(View.VISIBLE);
        rootView.startAnimation(fadeAnim);

        return true;
    }


    @Override
    public boolean playMain() {
        if (curState != TutorialState.IdleVisible)
            return false;

        startAnimationState(TutorialState.Main, null);
        animView.playGlowAnimation();
        return true;
    }

    @Override
    public boolean playOutro() {
        if (curState == TutorialState.Outro)
            return false;

        startAnimationState(TutorialState.Outro, new Runnable() {
            @Override
            public void run() {
                rootView.setVisibility(View.GONE);
            }
        });
        Animation fadeAnim = AnimationUtils.loadAnimation(context, R.anim.tutorial_outro);
        fadeAnim.setAnimationListener(getCurStateAnimationListener());
        rootView.startAnimation(fadeAnim);

        return true;
    }

    private void startState(TutorialState newState) {
        curState = newState;
        curAnimationId++;
    }

    private void startAnimationState(TutorialState newState, Runnable runnable) {
        curState = newState;
        curAnimationId++;
        curStateAnimationListener = new TutorialViewAnimationListener(curAnimationId, runnable);
    }

    private void onAnimationFinished(int animationId) {
        if (animationId != curAnimationId) {
            return;
        }

        if (curState == TutorialState.Intro) {
            startState(TutorialState.IdleVisible);
            playMain();
            if (listener != null) {
                listener.onTutorialAnimationFinished(this, TutorialState.Intro);
            }
        } else if (curState == TutorialState.Main) {
            startState(TutorialState.IdleVisible);
            if (listener != null) {
                listener.onTutorialAnimationFinished(this, TutorialState.Main);
            }
            playMain();
        } else if (curState == TutorialState.Outro) {
            startState(TutorialState.IdleInvisible);
            if (listener != null) {
                listener.onTutorialAnimationFinished(this, TutorialState.Outro);
            }
        }
    }

    @Override
    public void OnAnimationFinished(EdgeGlowTutorialAnimationView view) {
        view.playGlowAnimation();
    }


    private class TutorialViewAnimationListener implements Animation.AnimationListener {
        int startNum = 0;
        int finishNum = 0;
        int animationId;
        Runnable runnable;

        public TutorialViewAnimationListener(int animationid, Runnable runnable) {
            this.animationId = animationid;
            this.runnable = runnable;
        }

        int getAnimationId() {
            return animationId;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            startNum++;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            finishNum++;
            if (finishNum == startNum) {
                if (runnable != null) {
                    runnable.run();
                }
                onAnimationFinished(animationId);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}