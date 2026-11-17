package com.example.submate.domain.model;

import java.util.List;

/**
 * 구독 목록 전체를 요약한 값입니다. (설정 탭 '내 구독 현황', 리포트 등에서 사용)
 * 사용 예) SubscribeSummary summary = SubscribeSummary.from(list);
 * 피그마 예시 데이터 기준: 구독중 5 / 무료체험 1 / 해지예정 1 / 해지완료 2 / 월 42,283원
 */
public final class SubscribeSummary {
    private final int subscribingCount;
    private final int freeTrialCount;
    private final int cancelScheduledCount;
    private final int canceledCount;
    private final long monthlyKrwAmount;

    private SubscribeSummary(int subscribingCount, int freeTrialCount, int cancelScheduledCount,
                             int canceledCount, long monthlyKrwAmount) {
        this.subscribingCount = subscribingCount;
        this.freeTrialCount = freeTrialCount;
        this.cancelScheduledCount = cancelScheduledCount;
        this.canceledCount = canceledCount;
        this.monthlyKrwAmount = monthlyKrwAmount;
    }

    public static SubscribeSummary from(List<SubscribeItem> items) {
        int subscribing = 0;
        int freeTrial = 0;
        int cancelScheduled = 0;
        int canceled = 0;
        long monthly = 0L;
        if (items != null) {
            for (SubscribeItem item : items) {
                if (item.isCanceled()) {
                    canceled++;
                    continue;
                }
                subscribing++;
                if (item.isFreeTrial()) {
                    freeTrial++;
                    continue;
                }
                if (item.getStatus() == SubscribeStatus.CANCEL_SCHEDULED) {
                    cancelScheduled++;
                }
                monthly += item.getMonthlyKrwAmount();
            }
        }
        return new SubscribeSummary(subscribing, freeTrial, cancelScheduled, canceled, monthly);
    }

    /** 해지완료를 뺀 구독 수 (구독 목록 '전체' 칩의 숫자와 같습니다. 무료 체험·해지예정 포함) */
    public int getSubscribingCount() {
        return subscribingCount;
    }

    /** 해지완료를 뺀 구독 중 무료 체험 수 */
    public int getFreeTrialCount() {
        return freeTrialCount;
    }

    /** 해지예정 상태인 구독 수 (무료 체험은 '무료체험'으로만 세고 여기에는 넣지 않습니다) */
    public int getCancelScheduledCount() {
        return cancelScheduledCount;
    }

    /** 해지완료 상태인 구독 수 */
    public int getCanceledCount() {
        return canceledCount;
    }

    /** 한 달 구독료 합계(원). 해지완료와 무료 체험은 빼고, 연간 요금은 1/12로 계산합니다. */
    public long getMonthlyKrwAmount() {
        return monthlyKrwAmount;
    }
}
