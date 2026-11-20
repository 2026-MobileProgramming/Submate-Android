package com.example.submate.core.util;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.List;

public final class UsageStatsUtil {
    private UsageStatsUtil() {
    }

    public static boolean hasUsageAccess(Context context) {
        UsageStatsManager manager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (manager == null) return false;
        long now = System.currentTimeMillis();
        List<UsageStats> stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 60_000L, now);
        return stats != null && !stats.isEmpty();
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
