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

package com.fairphone.fplauncher3.applifecycle;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.fairphone.fplauncher3.AppInfo;
import com.fairphone.fplauncher3.BubbleTextView;
import com.fairphone.fplauncher3.CellLayout;
import com.fairphone.fplauncher3.DeleteDropTarget;
import com.fairphone.fplauncher3.DeviceProfile;
import com.fairphone.fplauncher3.DragSource;
import com.fairphone.fplauncher3.DropTarget.DragObject;
import com.fairphone.fplauncher3.Folder;
import com.fairphone.fplauncher3.ItemInfo;
import com.fairphone.fplauncher3.Launcher;
import com.fairphone.fplauncher3.LauncherAppState;
import com.fairphone.fplauncher3.LauncherTransitionable;
import com.fairphone.fplauncher3.R;
import com.fairphone.fplauncher3.Workspace;
import com.fairphone.fplauncher3.edgeswipe.editor.AppDiscoverer;
import com.fairphone.fplauncher3.widgets.appswitcher.ApplicationRunInformation;

/**
 * Edit favorites activity implements functionality to edit your favorite apps
 * that will appear with the edge swipe.
 */
public class AppDrawerView extends FrameLayout implements DragSource, LauncherTransitionable, OnLongClickListener
{
    private static final String TAG = AppDrawerView.class.getSimpleName();
    public static final int DRAGGING_DELAY_MILLIS = 150;
    public static final String APP_LIFECYCLE_PREFERENCES = "APP_LIFECYCLE_PREFERENCES";
    public static final String APP_AGE_LIMIT_IN_DAYS = "APP_AGE_LIMIT_IN_DAYS";
    public static final String CURENT_APP_AGE_LIMIT_IN_DAYS = "APP_AGE_LIMIT_IN_DAYS";
    public static final String APP_AGE_LIMIT_CHOISE_RESOURCE = "APP_AGE_LIMIT_CHOISE_RESOURCE";

    private AgingAppsListAdapter mAllAppsListAdapter;

    private AgingAppsListAdapter mUnusedAppsListAdapter;

    private ArrayList<AppInfo> mUsedApps;

    private ArrayList<AppInfo> mUnusedApps;

    private ExpandedGridview mAllAppsGridView;

    private ExpandedGridview mUnusedAppsGridView;

    private TextView activeAppsDescription;

    private TextView unusedAppsDescription;

    private ScrollView mScroll;

    private Context mContext;

    private Launcher mLauncher;

    private boolean mInTransition;
    private ImageView app_drawer_settings;
    private PopupMenu mAppSettingsPopup;

    public AppDrawerView(Context context)
    {
        super(context);
        init(context);
    }

    public AppDrawerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public AppDrawerView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("NewApi")
    public AppDrawerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected void init(Context context)
    {
        mContext = context;
        this.removeAllViews();
        View view = inflate(mContext, R.layout.fp_aging_app_drawer, null);

        refreshAppLists();

        setupAllAppsList(view);

        addView(view);
    }

    private void refreshAppLists() {
        Pair<ArrayList<AppInfo>, ArrayList<AppInfo>> appLists = AppDiscoverer.getInstance().getUsedAndUnusedApps(mContext);
        mUsedApps = appLists.first;
        mUnusedApps = appLists.second;
    }

    public void refreshView(Context context, Launcher launcher)
    {
        mLauncher = launcher;
        init(context);
    }

    /**
     * Setup the list with all the apps installed on the device.
     * 
     * @param view
     */
    private void setupAllAppsList(View view)
    {
        mScroll = (ScrollView) view.findViewById(R.id.agingDrawerScroll);
        mScroll.setSmoothScrollingEnabled(true);

        mAllAppsGridView = (ExpandedGridview) view.findViewById(R.id.usedAppsGridView);
        mUnusedAppsGridView = (ExpandedGridview) view.findViewById(R.id.unusedAppsGridView);

        activeAppsDescription = (TextView) view.findViewById(R.id.activeAppsDescription);
        unusedAppsDescription = (TextView) view.findViewById(R.id.unusedAppsDescription);

        setupListAdapters();

        app_drawer_settings = (ImageView)view.findViewById(R.id.aging_drawer_menu_btn);
        setupAppSettingsMenu();

        app_drawer_settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v.getAlpha() == 0f) {
                    //fire the easter egg
                    Toast.makeText(mContext, R.string.lifecyle_time_chooser_easter_egg, Toast.LENGTH_LONG).show();
                    v.setAlpha(1f);
                } else {

                    mAppSettingsPopup.show();
                }
            }
        });
    }

    private void setupListAdapters() {
        setupListAdapter(mAllAppsGridView, mAllAppsListAdapter, mUsedApps, false);
        setupListAdapter(mUnusedAppsGridView, mUnusedAppsListAdapter, mUnusedApps, true);

        if (mUsedApps.isEmpty())
        {
            activeAppsDescription.setVisibility(View.VISIBLE);
        }
        else
        {
            activeAppsDescription.setVisibility(View.GONE);
        }

        if (mUnusedApps.isEmpty())
        {
            unusedAppsDescription.setVisibility(View.VISIBLE);
        }
        else
        {
            unusedAppsDescription.setVisibility(View.GONE);
        }
    }

    private void setupAppSettingsMenu() {
        mAppSettingsPopup = new PopupMenu(mContext, app_drawer_settings);
        mAppSettingsPopup.getMenuInflater()
                .inflate(R.menu.aging_drawer_menu, mAppSettingsPopup.getMenu());

        MenuItem item = mAppSettingsPopup.getMenu().findItem(getAppIdleChoice(mContext));
        if (item == null) {
            setAppIdleChoice(mContext, R.id.one_month_to_idle);
            item = mAppSettingsPopup.getMenu().findItem(getAppIdleChoice(mContext));
        }
        item.setChecked(true);

        mAppSettingsPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Resources resources = mContext.getResources();
                int itemId = item.getItemId();
                switch (itemId) {
                    case R.id.one_week_to_idle:
                        ApplicationRunInformation.setAppIdleLimitInDays(mContext, resources.getInteger(R.integer.app_frequent_use_one_week));
                        break;
                    case R.id.two_weeks_to_idle:
                        ApplicationRunInformation.setAppIdleLimitInDays(mContext, resources.getInteger(R.integer.app_frequent_use_two_weeks));
                        break;
                    case R.id.one_month_to_idle:
                        ApplicationRunInformation.setAppIdleLimitInDays(mContext, resources.getInteger(R.integer.app_frequent_use_one_month));
                    default:
                        ApplicationRunInformation.setAppIdleLimitInDays(mContext, resources.getInteger(R.integer.app_frequent_use_default));
                        break;
                }

                setAppIdleChoice(mContext, itemId);

                if (item.isChecked()){
                    item.setChecked(false);
                }
                else {
                    item.setChecked(true);
                }

                //Redraw the drawer
                refreshListAdapters();
                return true;
            }
        });
    }

    public static int getAppIdleChoice(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_LIFECYCLE_PREFERENCES, Activity.MODE_PRIVATE);
        return sharedPreferences.getInt(APP_AGE_LIMIT_CHOISE_RESOURCE, R.id.one_month_to_idle);
    }

    public static void setAppIdleChoice(Context context, int choiceId){
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_LIFECYCLE_PREFERENCES, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(APP_AGE_LIMIT_CHOISE_RESOURCE, choiceId);
        editor.commit();
    }

    public void setupListAdapter(GridView listView, AgingAppsListAdapter appsListAdapter, ArrayList<AppInfo> appList, boolean isUnused)
    {
        appsListAdapter = new AgingAppsListAdapter(mContext, mLauncher, this, isUnused);

        appsListAdapter.setAllApps(appList);

        listView.setAdapter(appsListAdapter);
    }

    public void refreshListAdapters()
    {
        refreshAppLists();
        mAllAppsGridView.removeAllViewsInLayout();
        mUnusedAppsGridView.removeAllViewsInLayout();
        setupListAdapters();
    }

    private void beginDraggingApplication(View v)
    {
        mLauncher.getWorkspace().beginDragShared(v, this);
    }

    /**
     * Clean up after dragging.
     *
     * @param target
     *            where the item was dragged to (can be null if the item was
     *            flung)
     */
    private void endDragging(View target, boolean isFlingToDelete, boolean success)
    {
        if (isFlingToDelete || !success || (target != mLauncher.getWorkspace() && !(target instanceof DeleteDropTarget) && !(target instanceof Folder)))
        {
            // Exit spring loaded mode if we have not successfully dropped or
            // have not handled the
            // drop in Workspace
            mLauncher.exitSpringLoadedDragMode();
            mLauncher.unlockScreenOrientation(false);
        }
        else
        {
            mLauncher.unlockScreenOrientation(false);
        }
    }

    protected boolean beginDragging(final View v)
    {

        if (v instanceof BubbleTextView)
        {
            beginDraggingApplication(v);
        }

        // We delay entering spring-loaded mode slightly to make sure the UI
        // thready is free of any work.
        postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                // We don't enter spring-loaded mode if the drag has been
                // cancelled
                if (mLauncher.getDragController().isDragging())
                {
                    // Go into spring loaded mode (must happen before we
                    // startDrag())
                    mLauncher.enterSpringLoadedDragMode();
                }
            }
        }, DRAGGING_DELAY_MILLIS);

        return true;
    }

    @Override
    public View getContent()
    {
        if (getChildCount() > 0)
        {
            return getChildAt(0);
        }
        return null;
    }

    @Override
    public void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean toWorkspace)
    {
        mInTransition = true;
    }

    @Override
    public void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace)
    {
    }

    @Override
    public void onLauncherTransitionStep(Launcher l, float t)
    {
    }

    @Override
    public void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace)
    {
        mInTransition = false;
        // mForceDrawAllChildrenNextFrame = !toWorkspace;
    }

    @Override
    public boolean onLongClick(View v)
    {
        // Return early if this is not initiated from a touch
        // if (!v.isInTouchMode()) return false;
        // When we have exited all apps or are in transition, disregard long
        // clicks
        // if (!mLauncher.isAgingAppDrawerVisible() ||
        // !mLauncher.isAllAppsVisible() ||
        // mLauncher.getWorkspace().isSwitchingState()) return false;
        // // Return if global dragging is not enabled
        // if (!mLauncher.isDraggingEnabled()) return false;

        mLauncher.exitOverviewMode();
        mLauncher.hideAgingAppDrawer();

        return beginDragging(v);
    }

    @Override
    public void onDropCompleted(View target, DragObject d, boolean isFlingToDelete, boolean success)
    {
        // Return early and wait for onFlingToDeleteCompleted if this was the
        // result of a fling
        if (isFlingToDelete) {
            return;
        }

        endDragging(target, false, success);

        // Display an error message if the drag failed due to there not being
        // enough space on the
        // target layout we were dropping on.
        if (!success)
        {
            boolean showOutOfSpaceMessage = false;
            if (target instanceof Workspace)
            {
                int currentScreen = mLauncher.getCurrentWorkspaceScreen();
                Workspace workspace = (Workspace) target;
                CellLayout layout = (CellLayout) workspace.getChildAt(currentScreen);
                ItemInfo itemInfo = (ItemInfo) d.dragInfo;
                if (layout != null)
                {
                    CellLayout.calculateSpans(itemInfo);
                    showOutOfSpaceMessage = !layout.findCellForSpan(null, itemInfo.spanX, itemInfo.spanY);
                }
            }
            if (showOutOfSpaceMessage)
            {
                mLauncher.showOutOfSpaceMessage();
            }

            d.deferDragViewCleanupPostAnimation = false;
        }
    }

    @Override
    public void onFlingToDeleteCompleted()
    {
        // We just dismiss the drag when we fling, so cleanup here
        endDragging(null, true, true);
    }

    @Override
    public boolean supportsFlingToDelete()
    {
        return true;
    }

    @Override
    public boolean supportsAppInfoDropTarget()
    {
        return true;
    }

    @Override
    public boolean supportsDeleteDropTarget()
    {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor()
    {
        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
        return (float) grid.allAppsIconSizePx / grid.iconSizePx;
    }
}
