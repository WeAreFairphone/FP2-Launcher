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
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.fairphone.fplauncher3.R;
import com.fairphone.fplauncher3.oobe.animation.EdgeSwipeTutorialAnimationView.EdgeSwipeTutorialAnimationViewListener;

public class EdgeSwipeTutorialAnimationHelper implements TutorialAnimationHelper, EdgeSwipeTutorialAnimationViewListener {
    EdgeSwipeTutorialAnimationView animView;
    View rootView;
    Context context;
    TutorialAnimationHelperListener listener;
    TutorialState curState = TutorialState.IdleInvisible;
    TextView editView;

    @Override
    public View setup(Context context) {
        this.context = context;

        rootView = LayoutInflater.from(context).inflate(R.layout.tutorial_edge_swipe_layout, null);
        animView = (EdgeSwipeTutorialAnimationView) rootView.findViewById(R.id.swipeAnimationView);

        TextView titleView = (TextView) animView.findViewById(R.id.swipeAnimationTitle);
        TextView textView = (TextView) animView.findViewById(R.id.swipeAnimationText);
        editView = (TextView) animView.findViewById(R.id.swipeAnimationEditText);

        Animation titleAnimation = new AlphaAnimation(0.0f, 1.0f);
        titleAnimation.setDuration(500);

        Animation textAnimation = new AlphaAnimation(0.0f, 1.0f);
        textAnimation.setDuration(500);
        textAnimation.setStartOffset(500);

        Animation textEditAnimation = new AlphaAnimation(0.0f, 1.0f);
        textEditAnimation.setDuration(200);
        textEditAnimation.setStartOffset(2500);

        Animation textEditAnimationOff = new AlphaAnimation(1.0f, 0.0f);
        textEditAnimationOff.setDuration(850);
        textEditAnimationOff.setStartOffset(3350);
        textEditAnimationOff.setInterpolator(new AccelerateInterpolator());
        //textEditAnimationOff.setFillAfter(true);

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(textEditAnimation);
        animationSet.addAnimation(textEditAnimationOff);
        animationSet.setFillAfter(true);

        titleView.startAnimation(titleAnimation);
        textView.startAnimation(textAnimation);
        editView.startAnimation(animationSet);


        /*titleView.animate().alpha(0f).setDuration(1000).setListener(null);
        titleView.animate().alpha(1f).setDuration(1000).setListener(null);
        textView.animate().alpha(1f).setStartDelay(10000).setDuration(400000).setListener(null);*/

        animView.playSwipeAnimation();
        animView.stopAnimations();

        animView.setEdgeSwipeTutorialAnimationViewListener(this);

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
        animView.playSwipeAnimation();
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
    public void OnAnimationFinished(EdgeSwipeTutorialAnimationView view) {
        Animation textEditAnimation = new AlphaAnimation(0.0f, 1.0f);
        textEditAnimation.setDuration(200);
        textEditAnimation.setStartOffset(2100);

        Animation textEditAnimationOff = new AlphaAnimation(1.0f, 0.0f);
        textEditAnimationOff.setDuration(850);
        textEditAnimationOff.setStartOffset(3150);
        textEditAnimationOff.setInterpolator(new AccelerateInterpolator());

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(textEditAnimation);
        animationSet.addAnimation(textEditAnimationOff);
        animationSet.setFillAfter(true);


        editView.startAnimation(animationSet);
       // editView.startAnimation(textEditAnimation);

        view.playSwipeAnimation();
    }

    private class TutorialViewAnimationListener implements AnimationListener {
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
