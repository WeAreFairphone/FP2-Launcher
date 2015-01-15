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
    private Context mContext;

    private ArrayList<AppInfo> allApps;

	private LayoutInflater mInflater;

	private Launcher mLauncher;

	private OnLongClickListener mLongClickListener;

    public AgingAppsListAdapter(Context context, Launcher launcher, OnLongClickListener longClickListener)
    {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mLauncher = launcher;
        mLongClickListener = longClickListener;
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
        TextView newLabel = (TextView) fullIcon.findViewById(R.id.label_icon_new);
        TextView updatedLabel = (TextView) fullIcon.findViewById(R.id.label_icon_updated);
        
        icon.applyFromApplicationInfo(info);
        icon.setOnClickListener(mLauncher);
        icon.setOnLongClickListener(mLongClickListener);
        icon.setOnTouchListener(null);
        icon.setOnKeyListener(null);
        icon.setOnFocusChangeListener(null);
        
        ApplicationRunInformation appRunInfo = AppDiscoverer.getInstance().getApplicationRunInformation(mContext, info.getComponentName());
        if(appRunInfo != null) {
	        updatedLabel.setVisibility(appRunInfo.isUpdatedApp() ? View.VISIBLE : View.GONE);
	        newLabel.setVisibility(appRunInfo.isNewApp() ? View.VISIBLE : View.GONE);
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
