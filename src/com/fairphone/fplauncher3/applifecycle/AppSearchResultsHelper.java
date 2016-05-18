package com.fairphone.fplauncher3.applifecycle;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.fairphone.fplauncher3.AppInfo;
import com.fairphone.fplauncher3.Launcher;
import com.fairphone.fplauncher3.R;
import com.fairphone.fplauncher3.edgeswipe.editor.AppDiscoverer;

import java.util.ArrayList;


public class AppSearchResultsHelper {

    private final static String TAG = AppSearchResultsHelper.class.getName();
    private AppDrawerView mDrawerView;
    private SearchView mSearchView;
    private Launcher mLauncher;
    private ArrayList<AppInfo> mActiveApps;
    private ArrayList<AppInfo> mIdleApps;
    private AgingAppsListAdapter mAdapterActive;
    private AgingAppsListAdapter mAdapterIdle;

    private boolean mIsKeyboardShowing = false;
    private boolean mActive = false;
    private boolean mAnimating = false;

    public boolean isActive() {
        return mActive;
    }

    public AppSearchResultsHelper(AppDrawerView drawerView, Launcher launcher) {
        mDrawerView = drawerView;
        mSearchView = (SearchView) mDrawerView.findViewById(R.id.searchView);
        mLauncher = launcher;

        init();
        setupSearchView();
    }

    private void init() {
        Pair<ArrayList<AppInfo>, ArrayList<AppInfo>> appLists = AppDiscoverer.getInstance().getUsedAndUnusedApps(mLauncher.getApplicationContext());
        mActiveApps = new ArrayList<>();
        mIdleApps = new ArrayList<>();
        mActiveApps.addAll(appLists.first);
        mIdleApps.addAll(appLists.second);

        ExpandedGridview allAppsGridview = (ExpandedGridview) mDrawerView.findViewById(R.id.usedAppsGridView);
        ExpandedGridview idleAppsGridview = (ExpandedGridview) mDrawerView.findViewById(R.id.unusedAppsGridView);

        mAdapterActive = new AgingAppsListAdapter(mLauncher.getApplicationContext(), mLauncher, mDrawerView, false);
        mAdapterActive.setAllApps(mActiveApps);

        mAdapterIdle = new AgingAppsListAdapter(mLauncher.getApplicationContext(), mLauncher, mDrawerView, true);
        mAdapterIdle.setAllApps(mIdleApps);

        checkSearchResultSizes(mActiveApps, mIdleApps);

        allAppsGridview.setAdapter(mAdapterActive);
        allAppsGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                AppInfo appInfo = (AppInfo) adapterView.getItemAtPosition(position);
                startNewActivity(mLauncher.getApplicationContext(), appInfo.getComponentName().getPackageName());
            }
        });

        idleAppsGridview.setAdapter(mAdapterIdle);
        idleAppsGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                AppInfo appInfo = (AppInfo) adapterView.getItemAtPosition(position);
                startNewActivity(mLauncher.getApplicationContext(), appInfo.getComponentName().getPackageName());
            }
        });
    }

    private void startNewActivity(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void setupSearchView() {
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!mIsKeyboardShowing) {
                        showKeyboard(v);
                    } else {
                        mIsKeyboardShowing = true;
                    }
                } else if (mIsKeyboardShowing) {
                    hideKeyboard();
                }
            }
        });
        mSearchView.setIconifiedByDefault(false);

        colorSearchArea();

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                executeQuery(query);
                hideKeyboard();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                executeQuery(newText);
                return true;
            }
        });
    }

    private void showKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) mLauncher.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            boolean success = false;
            if (v != null) {
                View focused = v.findFocus();
                success = imm.showSoftInput(focused, InputMethodManager.SHOW_FORCED, new ResultReceiver(focused.getHandler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        super.onReceiveResult(resultCode, resultData);
                        mIsKeyboardShowing = (resultCode == InputMethodManager.RESULT_UNCHANGED_SHOWN) || (resultCode == InputMethodManager.RESULT_SHOWN);
                    }
                });
            } else {
                Log.d(TAG, "showKeyboard: no focused view");
            }
            if (!success) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                mIsKeyboardShowing = true;
            }
        } else {
            Log.d(TAG, "showKeyboard: no soft input service");
        }
    }

    protected void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) mLauncher.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchView.getApplicationWindowToken(), 0);
        mIsKeyboardShowing = false;
        mSearchView.clearFocus();
        mDrawerView.findViewById(R.id.agingDrawerScroll).requestFocusFromTouch();
    }

    public void smartHide() {
        if (mActive && !mAnimating) {
            if (mIsKeyboardShowing) {
                hideKeyboard();
            } else if (!TextUtils.isEmpty(mSearchView.getQuery())) {
                mSearchView.setQuery("", false);
            } else {
                hideSearchView();
            }
        }
    }

    public void showSearchView() {
        if (!mAnimating) {
            mAnimating = true;

            // otherwise it may crash
            View focused = mLauncher.getCurrentFocus();
            if (focused != null) {
                focused.clearFocus();
            }

            View agingAppsMenuIcon = mDrawerView.findViewById(R.id.aging_drawer_menu_btn);
            View searchButton = mDrawerView.findViewById(R.id.all_apps_search_btn);

            //Open the searchview bar
            Animation fadeOut = AnimationUtils.loadAnimation(mLauncher.getApplicationContext(), R.anim.fade_out_fast);
            fadeOut.setFillAfter(true);
            searchButton.startAnimation(fadeOut);
            agingAppsMenuIcon.startAnimation(fadeOut);

            mDrawerView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            mSearchView.setVisibility(View.VISIBLE);
            mSearchView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            Animation searchAppear = AnimationUtils.loadAnimation(mLauncher.getApplicationContext(), R.anim.search_appear);
            searchAppear.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mSearchView.setLayerType(View.LAYER_TYPE_NONE, null);
                    mDrawerView.setLayerType(View.LAYER_TYPE_NONE, null);
                    mAnimating = false;
                    mActive = true;
                    mSearchView.requestFocusFromTouch();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mSearchView.startAnimation(searchAppear);
        }
    }

    private void hideSearchView() {
        if (!mAnimating) {
            mAnimating = true;
            mDrawerView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            mSearchView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            // otherwise it may crash
            View focused = mLauncher.getCurrentFocus();
            if (focused != null) {
                focused.clearFocus();
            }

            Animation searchVanish = AnimationUtils.loadAnimation(mLauncher.getApplicationContext(), R.anim.search_vanish);
            searchVanish.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mSearchView.setLayerType(View.LAYER_TYPE_NONE, null);
                    mSearchView.setVisibility(View.GONE);

                    View scrollView = mDrawerView.findViewById(R.id.agingDrawerScroll);
                    scrollView.scrollTo(0, 0);

                    Animation fadeOut = AnimationUtils.loadAnimation(mLauncher.getApplicationContext(), R.anim.fade_in_slow);
                    fadeOut.setFillAfter(true);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mDrawerView.setLayerType(View.LAYER_TYPE_NONE, null);
                            mActive = false;
                            mAnimating = false;
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    View agingAppsMenuIcon = mDrawerView.findViewById(R.id.aging_drawer_menu_btn);
                    View searchButton = mDrawerView.findViewById(R.id.all_apps_search_btn);
                    agingAppsMenuIcon.startAnimation(fadeOut);
                    searchButton.startAnimation(fadeOut);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });


            mSearchView.startAnimation(searchVanish);
        }
    }

    private void colorSearchArea() {
        mSearchView.setBackgroundColor(Color.WHITE);
        int searchAreaId = mSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) mSearchView.findViewById(searchAreaId);
        textView.setTextColor(mLauncher.getResources().getColor(R.color.blue));
        textView.setHintTextColor(mLauncher.getResources().getColor(R.color.blue_alpha_50));
        textView.setBackgroundColor(Color.WHITE);
        int searchGlassId = mLauncher.getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView searchGlassIcon = (ImageView) mSearchView.findViewById(searchGlassId);
        if (searchGlassIcon != null) {
            searchGlassIcon.setScaleType(ImageView.ScaleType.CENTER);
            searchGlassIcon.setColorFilter(Color.rgb(42, 168, 224));
            searchGlassIcon.setImageResource(R.drawable.ic_all_apps_search);
        }
        int closeId = mLauncher.getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeBtn = (ImageView) mSearchView.findViewById(closeId);
        if (closeBtn != null) {
            closeBtn.setColorFilter(mLauncher.getResources().getColor(R.color.blue));
            closeBtn.setBackgroundColor(Color.WHITE);
        }
        int searchPlateId = mLauncher.getResources().getIdentifier("android:id/search_plate", null, null);
        LinearLayout searchPlateIcon = (LinearLayout) mSearchView.findViewById(searchPlateId);
        if (searchPlateIcon != null)
            searchPlateIcon.setBackgroundColor(mLauncher.getResources().getColor(R.color.blue));

    }


    private void executeQuery(String query) {

        ArrayList<AppInfo> searchResultsActive = new ArrayList<>();
        ArrayList<AppInfo> searchResultsIdle = new ArrayList<>();
        for (AppInfo appInfo : mActiveApps) {
            String app = appInfo.getApplicationTitle();
            if (app.toLowerCase().startsWith(query.toLowerCase())) {
                searchResultsActive.add(appInfo);
            }
        }
        for (AppInfo appInfo : mIdleApps) {
            String app = appInfo.getApplicationTitle();
            if (app.toLowerCase().startsWith(query.toLowerCase())) {
                searchResultsIdle.add(appInfo);
            }
        }

        checkSearchResultSizes(searchResultsActive, searchResultsIdle);

        mAdapterActive.setAllApps(searchResultsActive);
        mAdapterActive.notifyDataSetChanged();
        mAdapterIdle.setAllApps(searchResultsIdle);
        mAdapterIdle.notifyDataSetChanged();
    }

    private void checkSearchResultSizes(ArrayList<AppInfo> activeApps, ArrayList<AppInfo> idleApps) {
        if (activeApps.size() == 0) {
            TextView activeAppsDescription = (TextView) mDrawerView.findViewById(R.id.activeAppsDescription);
            activeAppsDescription.setText(R.string.no_active_app_found);
            activeAppsDescription.setVisibility(View.VISIBLE);
        } else {
            TextView activeAppsDescription = (TextView) mDrawerView.findViewById(R.id.activeAppsDescription);
            activeAppsDescription.setVisibility(View.GONE);
        }

        if (idleApps.size() == 0) {
            TextView activeAppsDescription = (TextView) mDrawerView.findViewById(R.id.unusedAppsDescription);
            activeAppsDescription.setText(R.string.no_idle_app_found);
            activeAppsDescription.setVisibility(View.VISIBLE);
        } else {
            TextView activeAppsDescription = (TextView) mDrawerView.findViewById(R.id.unusedAppsDescription);
            activeAppsDescription.setVisibility(View.GONE);
        }
    }


}
