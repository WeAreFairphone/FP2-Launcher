package com.fairphone.fplauncher3;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

public class IconPackPreference extends ListPreference {

    private final PackageManager mPackageManager;
    private final Resources mResources;
    private final IconCache mIconCache;

    public IconPackPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mPackageManager = context.getPackageManager();
        mResources = context.getResources();
        mIconCache = LauncherAppState.getInstance().getIconCache();
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder)
    {
        // We use the same intents as Lawnchair.
        List<ResolveInfo> list = mPackageManager.queryIntentActivities(new Intent("com.novalauncher.THEME"), 0);
        list.addAll(mPackageManager.queryIntentActivities(new Intent("org.adw.launcher.icons.ACTION_PICK_ICON"), 0));
        list.addAll(mPackageManager.queryIntentActivities(new Intent("com.dlto.atom.launcher.THEME"), 0));
        list.addAll(mPackageManager.queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory("com.anddoes.launcher.THEME"), 0));

        List<CharSequence> entryList = new ArrayList<CharSequence>();
        List<CharSequence> entryValueList = new ArrayList<CharSequence>();

        entryList.add(mResources.getString(R.string.icon_pack_preference_none));
        entryValueList.add("");

        for (ResolveInfo info : list) {
            if (!entryValueList.contains(info.activityInfo.packageName)) {
                entryList.add(info.loadLabel(mPackageManager));
                entryValueList.add(info.activityInfo.packageName);
            }
        }

        CharSequence[] entries = new CharSequence[entryList.size()];
        CharSequence[] entryValues  = new CharSequence[entryValueList.size()];
        entries = entryList.toArray(entries);
        entryValues = entryValueList.toArray(entryValues);

        setEntries(entries);
        setEntryValues(entryValues);

        super.onPrepareDialogBuilder(builder);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            mIconCache.flush();
            mIconCache.loadIconPack();
        }
    }
}
