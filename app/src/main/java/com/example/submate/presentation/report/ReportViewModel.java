package com.example.submate.presentation.report;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.submate.core.util.UsageStatsUtil;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.repository.ResultCallback;
import com.example.submate.domain.repository.SubscribeRepository;
import com.example.submate.domain.repository.InstalledAppRepository;
import com.example.submate.presentation.report.model.UsageReportItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class ReportViewModel extends ViewModel {

    private final SubscribeRepository repository;
    private final InstalledAppRepository installedAppRepository;
    private final Context appContext;
    private final MediatorLiveData<MonthlyUsageReport> monthlyUsage = new MediatorLiveData<>();
    private final ExecutorService usageExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Calendar usageMonth;
    private Future<?> usageQuery;
    private int usageRequestId;

    // 실제 구독 목록
    private final LiveData<List<SubscribeItem>> subscribeList;

    @Inject
    public ReportViewModel(
            @ApplicationContext Context appContext,
            SubscribeRepository repository,
            InstalledAppRepository installedAppRepository
    ) {
        this.appContext = appContext;
        this.repository = repository;
        this.installedAppRepository = installedAppRepository;

        // 구독 목록 관찰
        subscribeList = repository.observeSubscribeList();
        monthlyUsage.addSource(subscribeList, items -> refreshMonthlyUsage());
        monthlyUsage.addSource(installedAppRepository.observeInstalledApps(), apps -> refreshMonthlyUsage());

        // 설치된 앱 목록 갱신
        installedAppRepository.refresh();

        // 실제 구독 데이터 불러오기
        refresh();
    }

    // 구독 데이터 새로고침
    public void refresh() {
        repository.refresh(new ResultCallback<List<SubscribeItem>>() {
            @Override
            public void onSuccess(List<SubscribeItem> result) {
                // Repository가 LiveData를 갱신하므로 별도 처리 불필요
            }

            @Override
            public void onError(String message) {
                // 데이터 조회 실패 시 현재 목록 유지
            }
        });
    }

    // 구독 목록 관찰
    public LiveData<List<SubscribeItem>> getSubscribeList() {
        return subscribeList;
    }

    public LiveData<MonthlyUsageReport> getMonthlyUsage() {
        return monthlyUsage;
    }

    public void loadMonthlyUsage(Calendar month) {
        usageMonth = (Calendar) month.clone();
        refreshMonthlyUsage();
    }

    private void refreshMonthlyUsage() {
        List<SubscribeItem> items = subscribeList.getValue();
        if (usageMonth == null || items == null) return;

        int requestId = ++usageRequestId;
        if (usageQuery != null) usageQuery.cancel(true);
        Calendar month = (Calendar) usageMonth.clone();
        List<SubscribeItem> subscriptions = new ArrayList<>(items);
        usageQuery = usageExecutor.submit(() -> {
            MonthlyUsageReport result = buildMonthlyUsage(month, subscriptions);
            mainHandler.post(() -> {
                if (requestId == usageRequestId) monthlyUsage.setValue(result);
            });
        });
        monthlyUsage.setValue(null);
    }

    private MonthlyUsageReport buildMonthlyUsage(Calendar month, List<SubscribeItem> subscriptions) {
        Map<String, SubscribeItem> subscriptionsByPackage = new LinkedHashMap<>();
        int unlinkedCount = 0;
        for (SubscribeItem subscription : subscriptions) {
            String packageName = installedAppRepository.resolvePackageName(subscription);
            if (packageName.isEmpty()) {
                unlinkedCount++;
            } else {
                subscriptionsByPackage.putIfAbsent(packageName, subscription);
            }
        }

        Map<String, Long> usage = UsageStatsUtil.queryMonthlyUsageMillis(
                appContext, month, subscriptionsByPackage.keySet());
        if (usage == null) {
            return new MonthlyUsageReport(Collections.emptyList(), unlinkedCount, 0, true);
        }

        long maxUsage = 0;
        for (long duration : usage.values()) {
            maxUsage = Math.max(maxUsage, duration);
        }

        List<UsageReportItem> rows = new ArrayList<>();
        for (Map.Entry<String, SubscribeItem> entry : subscriptionsByPackage.entrySet()) {
            Long duration = usage.get(entry.getKey());
            if (duration == null) continue;
            SubscribeItem subscription = entry.getValue();
            String name = subscription.getServiceName();
            String icon = name.isEmpty() ? "" : name.substring(0, name.offsetByCodePoints(0, 1));
            int color = ReportCategoryColors.getColor(appContext, subscription.getCategory());
            int percent = maxUsage == 0 ? 0 : (int) Math.round(100.0 * duration / maxUsage);
            rows.add(new UsageReportItem(name, icon, duration, percent, color, color));
        }
        rows.sort(Comparator.comparingLong(UsageReportItem::getUsageMillis).reversed());
        return new MonthlyUsageReport(rows, unlinkedCount, subscriptionsByPackage.size() - rows.size(), false);
    }

    @Override
    protected void onCleared() {
        usageRequestId++;
        usageExecutor.shutdownNow();
        super.onCleared();
    }

    public static final class MonthlyUsageReport {
        public final List<UsageReportItem> items;
        public final int unlinkedSubscriptionCount;
        public final int packagesWithoutRecords;
        public final boolean queryFailed;

        private MonthlyUsageReport(List<UsageReportItem> items, int unlinkedSubscriptionCount,
                                   int packagesWithoutRecords, boolean queryFailed) {
            this.items = Collections.unmodifiableList(items);
            this.unlinkedSubscriptionCount = unlinkedSubscriptionCount;
            this.packagesWithoutRecords = packagesWithoutRecords;
            this.queryFailed = queryFailed;
        }
    }

    public long getExpenseThrough(List<SubscribeItem> items, Calendar endDate) {
        int year = endDate.get(Calendar.YEAR);
        int endMonth = endDate.get(Calendar.MONTH) + 1;
        long total = 0;

        for (int month = 1; month <= endMonth; month++) {
            int cutoffDay = month == endMonth ? endDate.get(Calendar.DAY_OF_MONTH) : 31;
            for (SubscribeItem item : getPaidSubscriptionsForMonth(items, year, month, cutoffDay)) {
                total += item.getKrwAmount();
            }
        }

        return total;
    }

    // 선택한 월에 결제일이 지난 구독 목록 반환
    public List<SubscribeItem> getPaidSubscriptionsForMonth(
            List<SubscribeItem> items,
            int year,
            int month,
            int todayDay
    ) {
        List<SubscribeItem> paidList = new ArrayList<>();

        if (items == null) {
            return paidList;
        }

        for (SubscribeItem item : items) {

            // 무료 체험 및 해지 완료 제외
            if (item.isFreeTrial() || item.isCanceled()) {
                continue;
            }

            // 해당 연월의 실제 결제일 확인
            int paymentDay = item.getPaymentDayIn(year, month);

            // 결제일이 없거나 아직 지나지 않았다면 제외
            if (paymentDay == 0 || paymentDay > todayDay) {
                continue;
            }

            paidList.add(item);
        }

        return paidList;
    }
}
