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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fairphone.fplauncher3.AppInfo;
import com.fairphone.fplauncher3.AppInfo.APP_AGE;
import com.fairphone.fplauncher3.LauncherModel;
import com.fairphone.fplauncher3.R;

public class AgingAppsListAdapter extends BaseAdapter
{
    private Context context;

    private ArrayList<AppInfo> allApps;

    public AgingAppsListAdapter(Context context)
    {
        this.context = context;
    }

    public void setAllApps(ArrayList<AppInfo> allApps)
    {
        this.allApps = allApps;
        Collections.sort(this.allApps, LauncherModel.getAppNameComparator());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View rowView = (View) convertView;

        LayoutInflater inflater = LayoutInflater.from(context);

        AppInfo info = allApps.get(position);
        rowView = (View) inflater.inflate(R.layout.fp_aging_app_drawer_list_item, parent, false);
        //        rowView.applyFromApplicationInfo(info, true, (VerticalAppDrawerActivity) context);

        TextView textView = (TextView) rowView.findViewById(R.id.text_view);
        TextView newTextView = (TextView) rowView.findViewById(R.id.new_text_view);
        TextView updatedTextView = (TextView) rowView.findViewById(R.id.updated_text_view);

        Bitmap mBitmap = info.getIconBitmap();
        Drawable icon = new BitmapDrawable(context.getResources(), mBitmap);
        icon.setBounds(0, 0, (int) context.getResources().getDimension(R.dimen.edit_favorites_icon_size),
                (int) context.getResources().getDimension(R.dimen.edit_favorites_icon_size));
        textView.setCompoundDrawables(null, icon, null, null);

        setAppAge(info.getAge(), newTextView, updatedTextView);

        textView.setText(info.getApplicationTitle());

        rowView.setTag(info);
        
        return rowView;
    }

    private void setAppAge(APP_AGE age, View newApp, View updatedApp)
    {

        switch (age)
        {
            case NEW_APP:
                newApp.setVisibility(View.VISIBLE);

                break;
            case FREQUENT_USE:
            case RARE_USE:
                break;
        }
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
