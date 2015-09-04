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
import java.util.Collections;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fairphone.fplauncher3.AppInfo;
import com.fairphone.fplauncher3.BubbleTextView;
import com.fairphone.fplauncher3.Launcher;
import com.fairphone.fplauncher3.LauncherModel;
import com.fairphone.fplauncher3.R;
import com.fairphone.fplauncher3.edgeswipe.editor.AppDiscoverer;
import com.fairphone.fplauncher3.widgets.appswitcher.ApplicationRunInformation;

public class AgingAppsListAdapter extends BaseAdapter
{
    private final Context mContext;

    private ArrayList<AppInfo> allApps;

	private final LayoutInflater mInflater;

	private final Launcher mLauncher;

	private final OnLongClickListener mLongClickListener;

    private final boolean isUnusedApp;

    public AgingAppsListAdapter(Context context, Launcher launcher, OnLongClickListener longClickListener, boolean isUnused)
    {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mLauncher = launcher;
        mLongClickListener = longClickListener;
        isUnusedApp = isUnused;
    }

    public void setAllApps(ArrayList<AppInfo> allApps)
    {
        this.allApps = allApps;
        Collections.sort(this.allApps, LauncherModel.getAppNameComparator());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        AppInfo info = allApps.get(position);
        
        RelativeLayout fullIcon = (RelativeLayout) mInflater.inflate(
                R.layout.fp_aging_apps_item, parent, false);
        
        BubbleTextView icon = (BubbleTextView) fullIcon.findViewById(R.id.application_icon);
        View pinLabel = fullIcon.findViewById(R.id.label_icon_pinned);
        View newLabel = fullIcon.findViewById(R.id.label_icon_new);
        View updatedLabel = fullIcon.findViewById(R.id.label_icon_updated);
        
        icon.applyFromApplicationInfo(info);
        icon.setOnClickListener(mLauncher);
        icon.setOnLongClickListener(mLongClickListener);
        icon.setOnTouchListener(null);
        icon.setOnKeyListener(null);
        icon.setOnFocusChangeListener(null);

        ApplicationRunInformation appRunInfo = AppDiscoverer.getInstance().getApplicationRunInformation(mContext, info.getComponentName());
        if(appRunInfo != null) {
            if (appRunInfo.isPinnedApp()) {
                pinLabel.setVisibility(View.VISIBLE);
            }
            if (appRunInfo.isUpdatedApp()){
                updatedLabel.setVisibility(View.VISIBLE);
            } else if (appRunInfo.isNewApp()) {
                newLabel.setVisibility(View.VISIBLE);
            }
        }

        if(isUnusedApp) {
            Drawable icd = icon.getCompoundDrawables()[1]; // 1 is top
            if(icd != null){
                int alpha = mContext.getResources().getInteger(R.integer.color_alpha_70_percent);
                icd.setAlpha(alpha);
                updatedLabel.setAlpha(alpha);
                newLabel.setAlpha(alpha);
            }
        }
        
        return fullIcon;
    }

    @Override
    public int getCount()
    {
        return allApps.size();
    }

    @Override
    public Object getItem(int position)
    {
        return allApps.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }
}
