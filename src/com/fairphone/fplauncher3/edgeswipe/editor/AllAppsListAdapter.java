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
package com.fairphone.fplauncher3.edgeswipe.editor;

import java.util.ArrayList;

import com.fairphone.fplauncher3.AppInfo;
import com.fairphone.fplauncher3.R;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AllAppsListAdapter extends BaseAdapter
{
    public static final int COMPOUND_DRAWABLE_PADDING = 20;
    private final Activity context;
    private ArrayList<AppInfo> allApps;
    
    static class ViewHolder
    {
        public TextView app_item;
    }

    public AllAppsListAdapter(Activity context)
    {
        this.context = context;
    }

    public void setAllApps(ArrayList<AppInfo> allApps)
    {
        this.allApps = allApps;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View rowView = convertView;
        if (rowView == null)
        {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.fp_edit_favorites_all_apps_list_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.app_item = (TextView) rowView.findViewById(R.id.app_list_item);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        AppInfo applicationInfo = allApps.get(position);
        holder.app_item.setText(applicationInfo.getApplicationTitle());
        
        Resources r = context.getResources();
		float px = r.getDimension(R.dimen.edit_favorites_icon_size);
		Drawable drawable = new BitmapDrawable(context.getResources(), applicationInfo.getIconBitmap());
		drawable.setBounds(0, 0, Math.round(px), Math.round(px));
		holder.app_item.setCompoundDrawablePadding(COMPOUND_DRAWABLE_PADDING);
		
		holder.app_item.setCompoundDrawables(null, drawable, null, null);
        
        return rowView;
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
