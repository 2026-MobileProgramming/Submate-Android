package com.example.submate.core.util;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /** Returns recorded foreground time by package, or null when stats are unavailable. */
    @WorkerThread
    @Nullable
    public static Map<String, Long> queryMonthlyUsageMillis(
            @NonNull Context context,
            @NonNull Calendar selectedMonth,
            @NonNull Set<String> packageNames
    ) {
        Map<String, Long> usageByPackage = new HashMap<>();
        if (packageNames.isEmpty()) return usageByPackage;

        Calendar monthStart = (Calendar) selectedMonth.clone();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        monthStart.set(Calendar.HOUR_OF_DAY, 0);
        monthStart.set(Calendar.MINUTE, 0);
        monthStart.set(Calendar.SECOND, 0);
        monthStart.set(Calendar.MILLISECOND, 0);

        Calendar nextMonthStart = (Calendar) monthStart.clone();
        nextMonthStart.add(Calendar.MONTH, 1);
        nextMonthStart.set(Calendar.HOUR_OF_DAY, 0);
        long startMillis = monthStart.getTimeInMillis();
        long endMillis = Math.min(nextMonthStart.getTimeInMillis(), System.currentTimeMillis());
        if (endMillis <= startMillis) return usageByPackage;

        UsageStatsManager manager = context.getSystemService(UsageStatsManager.class);
        if (manager == null || !hasUsageAccess(context)) return null;

        List<UsageStats> stats;
        try {
            stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMillis, endMillis);
        } catch (SecurityException exception) {
            return null;
        }
        if (stats == null || !hasUsageAccess(context)) return null;

        for (UsageStats usage : stats) {
            String packageName = usage.getPackageName();
            if (packageNames.contains(packageName)) {
                usageByPackage.merge(packageName, usage.getTotalTimeInForeground(), Long::sum);
            }
        }
        return usageByPackage;
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
