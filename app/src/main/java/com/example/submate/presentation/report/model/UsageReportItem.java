package com.example.submate.presentation.report.model;
public class UsageReportItem {

    private String serviceName;
    private String iconText;
    private String usageTime;
    private int usagePercent;
    private int iconColor;
    private int progressColor;

    public UsageReportItem(
            String serviceName,
            String iconText,
            String usageTime,
            int usagePercent,
            int iconColor,
            int progressColor
    ) {
        this.serviceName = serviceName;
        this.iconText = iconText;
        this.usageTime = usageTime;
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
        return usageTime;
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
