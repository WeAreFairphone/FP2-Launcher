package com.fairphone.fplauncher3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PackageChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String packageName = intent.getData().getSchemeSpecificPart();

        if (packageName == null || packageName.isEmpty()) {
            // they sent us a bad intent
            return;
        }
        // in rare cases the receiver races with the application to set up LauncherAppState
        LauncherAppState.setApplicationContext(context.getApplicationContext());
        LauncherAppState app = LauncherAppState.getInstance();
        WidgetPreviewLoader.removePackageFromDb(app.getWidgetPreviewCacheDb(), packageName);

        // If the current icon pack was deleted, we reload the icons.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String iconPackName = sharedPref.getString(SettingsActivity.KEY_PREF_ICON_PACK, "");
        if (packageName.equals(iconPackName)) {
            IconCache iconCache = LauncherAppState.getInstance().getIconCache();

            iconCache.flush();
            iconCache.loadIconPack();
            app.getModel().forceReload();
        }
    }
}
