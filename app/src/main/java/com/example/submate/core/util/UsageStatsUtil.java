package com.example.submate.core.util;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Process;

import java.util.List;

public final class UsageStatsUtil {
    private UsageStatsUtil() {
    }

    public static boolean hasUsageAccess(Context context) {
        AppOpsManager manager = context.getSystemService(AppOpsManager.class);
        if (manager == null) return false;
        int mode = manager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.getPackageName());
        if (mode == AppOpsManager.MODE_DEFAULT) {
            return context.checkSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static String findPackageNameByLabel(Context context, String serviceName) {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo application : applications) {
            CharSequence label = packageManager.getApplicationLabel(application);
            if (label != null && label.toString().equals(serviceName)) {
                return application.packageName;
            }
        }
        return null;
    }

    public static long getUsageMillis(Context context, String serviceName, long startMillis, long endMillis) {
        String packageName = findPackageNameByLabel(context, serviceName);
        if (packageName == null) return 0L;
        UsageStatsManager manager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (manager == null) return 0L;
        List<UsageStats> stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMillis, endMillis);
        if (stats == null) return 0L;
        for (UsageStats usage : stats) {
            if (packageName.equals(usage.getPackageName())) return usage.getTotalTimeInForeground();
        }
        return 0L;
    }
}
