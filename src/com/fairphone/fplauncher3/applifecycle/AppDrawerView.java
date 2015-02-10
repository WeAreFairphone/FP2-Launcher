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
import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ScrollView;
import android.widget.TextView;

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

/**
 * Edit favorites activity implements functionality to edit your favorite apps
 * that will appear with the edge swipe.
 */
public class AppDrawerView extends FrameLayout implements DragSource, LauncherTransitionable, OnLongClickListener
{
    private static final String TAG = AppDrawerView.class.getSimpleName();
    public static final int DRAGGING_DELAY_MILLIS = 150;

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

        Pair<ArrayList<AppInfo>, ArrayList<AppInfo>> appLists = AppDiscoverer.getInstance().getUsedAndUnusedApps(mContext);
        mUsedApps = appLists.first;
        mUnusedApps = appLists.second;

        setupAllAppsList(view);

        addView(view);
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

        setupListAdapter(mAllAppsGridView, mAllAppsListAdapter, mUsedApps);
        setupListAdapter(mUnusedAppsGridView, mUnusedAppsListAdapter, mUnusedApps);

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

    public void setupListAdapter(GridView listView, AgingAppsListAdapter appsListAdapter, ArrayList<AppInfo> appList)
    {
        appsListAdapter = new AgingAppsListAdapter(mContext, mLauncher, this);

        appsListAdapter.setAllApps(appList);

        listView.setAdapter(appsListAdapter);
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
                    layout.calculateSpans(itemInfo);
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
