package com.example.submate.domain.model;

/**
 * 결제 통화 (무료 체험은 금액 없이 종료일만 기록)
 * 외화의 환율은 환율 API에서 받아온 값으로 갱신되고, 받아오기 전에는 아래 기본값을 사용합니다.
 */
public enum SubscribeCurrency {
    KRW("KRW", "₩", 1.0),
    JPY("JPY", "¥", 9.1),
    USD("USD", "$", 1357.0),
    FREE_TRIAL("무료 체험", "", 0.0);

    private static final long KRW_ROUND_UNIT = 100L;

    private final String label;
    private final String symbol;
    // 1 단위당 원화 환율
    private volatile double krwRate;

    SubscribeCurrency(String label, String symbol, double defaultKrwRate) {
        this.label = label;
        this.symbol = symbol;
        this.krwRate = defaultKrwRate;
    }

    public String getLabel() {
        return label;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getKrwRate() {
        return krwRate;
    }

    /** 환율 API에서 받은 최신 환율을 반영합니다. (외화만 해당) */
    public void setKrwRate(double krwRate) {
        if (isForeign() && krwRate > 0) {
            this.krwRate = krwRate;
        }
    }

    public boolean isForeign() {
        return this == JPY || this == USD;
    }

    /** 금액을 원화로 환산합니다. 외화는 100원 단위로 반올림하고, 무료 체험은 0원입니다. */
    public long toKrw(double amount) {
        if (this == FREE_TRIAL) {
            return 0L;
        }
        if (this == KRW) {
            return Math.round(amount);
        }
        return Math.round(amount * krwRate / KRW_ROUND_UNIT) * KRW_ROUND_UNIT;
    }
}
