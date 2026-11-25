package com.example.submate.presentation.report.model;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class UsageReportItemTest {
    @Test
    public void formatsUsageAtMinuteAndHourBoundaries() {
        assertEquals("1분 미만", UsageReportItem.formatUsageTime(0));
        assertEquals("1분 미만", UsageReportItem.formatUsageTime(TimeUnit.SECONDS.toMillis(59)));
        assertEquals("1분", UsageReportItem.formatUsageTime(TimeUnit.MINUTES.toMillis(1)));
        assertEquals("59분", UsageReportItem.formatUsageTime(TimeUnit.HOURS.toMillis(1) - 1));
        assertEquals("1시간", UsageReportItem.formatUsageTime(TimeUnit.HOURS.toMillis(1)));
    }
}
