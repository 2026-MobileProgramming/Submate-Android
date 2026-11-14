package com.example.submate.domain.model;

/**
 * 구독 한 건의 정보입니다.
 * 연간 결제와 무료 체험은 paymentMonth(월)까지 사용하고, 월간 결제는 paymentDay(일)만 사용합니다.
 * 무료 체험의 결제일은 체험 종료일을 의미합니다.
 */
public final class SubscribeItem {
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

    public SubscribeItem(long id, SubscribeCategory category, String serviceName,
                         SubscribeStatus status, BillingCycle billingCycle,
                         int paymentMonth, int paymentDay,
                         SubscribeCurrency currency, double amount, String memo) {
        this.id = id;
        this.category = category;
        this.serviceName = serviceName;
        this.status = status;
        this.billingCycle = billingCycle;
        this.paymentMonth = paymentMonth;
        this.paymentDay = paymentDay;
        this.currency = currency;
        this.amount = amount;
        this.memo = memo;
    }

    /** id만 바꾼 복사본을 만듭니다. (서버/저장소에서 새 id를 받았을 때 사용) */
    public SubscribeItem withId(long newId) {
        return new SubscribeItem(newId, category, serviceName, status, billingCycle,
                paymentMonth, paymentDay, currency, amount, memo);
    }

    public long getId() { return id; }
    public SubscribeCategory getCategory() { return category; }
    public String getServiceName() { return serviceName; }
    public SubscribeStatus getStatus() { return status; }
    public BillingCycle getBillingCycle() { return billingCycle; }
    public int getPaymentMonth() { return paymentMonth; }
    public int getPaymentDay() { return paymentDay; }
    public SubscribeCurrency getCurrency() { return currency; }
    public double getAmount() { return amount; }
    public String getMemo() { return memo; }

    public boolean isFreeTrial() {
        return currency == SubscribeCurrency.FREE_TRIAL;
    }

    /** 원화로 환산한 1회 결제 금액 (100원 단위 반올림, 원화는 그대로) */
    public long getKrwAmount() {
        if (isFreeTrial()) {
            return 0L;
        }
        if (currency == SubscribeCurrency.KRW) {
            return Math.round(amount);
        }
        return Math.round(amount * currency.getKrwRate() / 100.0) * 100L;
    }

    /** 월 환산 금액. 연간 요금제는 1/12로 나누어 계산합니다. */
    public long getMonthlyKrwAmount() {
        long krw = getKrwAmount();
        if (billingCycle == BillingCycle.YEARLY) {
            return Math.round(krw / 12.0);
        }
        return krw;
    }
}
