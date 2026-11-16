package com.example.submate.domain.model;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * 구독 한 건의 정보입니다.
 * 연간 결제와 무료 체험은 paymentMonth(월)까지 사용하고, 월간 결제는 paymentDay(일)만 사용합니다.
 * 무료 체험의 결제일은 체험 종료일을 의미합니다.
 * packageName은 서비스명과 같은 이름의 앱이 기기에 설치되어 있을 때 그 앱의 패키지명이고, 없으면 빈 문자열입니다.
 * (리포트 탭에서 UsageStatsManager로 사용 시간을 조회할 때 사용)
 */
public final class SubscribeItem {
    private static final int MONTHS_PER_YEAR = 12;

    private final long id;
    private final SubscribeCategory category;
    private final String serviceName;
    private final SubscribeStatus status;
    private final BillingCycle billingCycle;
    private final int paymentMonth;
    private final int paymentDay;
    private final SubscribeCurrency currency;
    private final double amount;
    private final String memo;
    private final String packageName;

    public SubscribeItem(long id, SubscribeCategory category, String serviceName,
                         SubscribeStatus status, BillingCycle billingCycle,
                         int paymentMonth, int paymentDay,
                         SubscribeCurrency currency, double amount, String memo) {
        this(id, category, serviceName, status, billingCycle, paymentMonth, paymentDay,
                currency, amount, memo, "");
    }

    public SubscribeItem(long id, SubscribeCategory category, String serviceName,
                         SubscribeStatus status, BillingCycle billingCycle,
                         int paymentMonth, int paymentDay,
                         SubscribeCurrency currency, double amount, String memo,
                         String packageName) {
        this.id = id;
        this.category = category;
        this.serviceName = serviceName;
        this.status = status;
        this.billingCycle = billingCycle;
        this.paymentMonth = paymentMonth;
        this.paymentDay = paymentDay;
        this.currency = currency;
        this.amount = amount;
        this.memo = memo == null ? "" : memo;
        this.packageName = packageName == null ? "" : packageName;
    }

    /** id만 바꾼 복사본을 만듭니다. (서버/저장소에서 새 id를 받았을 때 사용) */
    public SubscribeItem withId(long newId) {
        return new SubscribeItem(newId, category, serviceName, status, billingCycle,
                paymentMonth, paymentDay, currency, amount, memo, packageName);
    }

    /** 연결된 앱의 패키지명만 바꾼 복사본을 만듭니다. */
    public SubscribeItem withPackageName(String newPackageName) {
        return new SubscribeItem(id, category, serviceName, status, billingCycle,
                paymentMonth, paymentDay, currency, amount, memo, newPackageName);
    }

    public long getId() {
        return id;
    }

    public SubscribeCategory getCategory() {
        return category;
    }

    public String getServiceName() {
        return serviceName;
    }

    public SubscribeStatus getStatus() {
        return status;
    }

    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public int getPaymentMonth() {
        return paymentMonth;
    }

    public int getPaymentDay() {
        return paymentDay;
    }

    public SubscribeCurrency getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }

    public String getMemo() {
        return memo;
    }

    /** 서비스명과 같은 이름의 앱이 설치되어 있을 때 그 앱의 패키지명. 없으면 "" */
    public String getPackageName() {
        return packageName;
    }

    /** 설치된 앱과 연결되어 사용량 조회가 가능한지 */
    public boolean isLinkedToApp() {
        return !packageName.isEmpty();
    }

    public boolean isFreeTrial() {
        return currency == SubscribeCurrency.FREE_TRIAL;
    }

    public boolean isCanceled() {
        return status == SubscribeStatus.CANCELED;
    }

    /** 원화로 환산한 1회 결제 금액 (외화는 현재 환율 기준, 무료 체험은 0원) */
    public long getKrwAmount() {
        return currency.toKrw(amount);
    }

    /** 월 환산 금액. 연간 요금제는 1/12로 나누어 계산합니다. */
    public long getMonthlyKrwAmount() {
        long krw = getKrwAmount();
        if (billingCycle == BillingCycle.YEARLY) {
            return Math.round(krw / (double) MONTHS_PER_YEAR);
        }
        return krw;
    }

    /**
     * 해당 연/월에 결제되는 날짜(일)를 돌려줍니다. 그 달에 결제가 없으면 0입니다. (캘린더용)
     * 31일 결제처럼 그 달에 없는 날짜는 말일로 맞춥니다. 무료 체험은 결제가 없으므로 항상 0입니다.
     *
     * @param month 1~12
     */
    public int getPaymentDayIn(int year, int month) {
        if (isFreeTrial()) {
            return 0;
        }
        if (billingCycle == BillingCycle.YEARLY && paymentMonth != month) {
            return 0;
        }
        int lastDay = new GregorianCalendar(year, month - 1, 1)
                .getActualMaximum(Calendar.DAY_OF_MONTH);
        return Math.min(paymentDay, lastDay);
    }
}
