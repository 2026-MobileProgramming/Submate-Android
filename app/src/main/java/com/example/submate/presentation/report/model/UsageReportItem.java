package com.example.submate.presentation.report.model;

import java.util.concurrent.TimeUnit;

public class UsageReportItem {

    private String serviceName;
    private String iconText;
    private final long usageMillis;
    private int usagePercent;
    private int iconColor;
    private int progressColor;

    public UsageReportItem(
            String serviceName,
            String iconText,
            long usageMillis,
            int usagePercent,
            int iconColor,
            int progressColor
    ) {
        this.serviceName = serviceName;
        this.iconText = iconText;
        this.usageMillis = usageMillis;
        this.usagePercent = usagePercent;
        this.iconColor = iconColor;
        this.progressColor = progressColor;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getIconText() {
        return iconText;
    }

    public String getUsageTime() {
        return TimeUnit.MILLISECONDS.toHours(usageMillis) + "시간";
    }

    public long getUsageMillis() {
        return usageMillis;
    }

    public int getUsagePercent() {
        return usagePercent;
    }

    public int getIconColor() {
        return iconColor;
    }

    public int getProgressColor() {
        return progressColor;
    }
}
