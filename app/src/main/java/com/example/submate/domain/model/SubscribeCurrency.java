package com.example.submate.domain.model;

/** 결제 통화 (무료 체험은 금액 없이 종료일만 기록) */
public enum SubscribeCurrency {
    KRW("KRW", "₩", 1.0),
    JPY("JPY", "¥", 9.1),
    USD("USD", "$", 1357.0),
    FREE_TRIAL("무료 체험", "", 0.0);

    private final String label;
    private final String symbol;
    // 1 단위당 원화 환율 (TODO: 환율 API 연동 시 실시간 값으로 교체)
    private final double krwRate;

    SubscribeCurrency(String label, String symbol, double krwRate) {
        this.label = label;
        this.symbol = symbol;
        this.krwRate = krwRate;
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

    public boolean isForeign() {
        return this == JPY || this == USD;
    }
}
