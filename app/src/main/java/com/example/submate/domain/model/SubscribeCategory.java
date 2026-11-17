package com.example.submate.domain.model;

/** 구독 카테고리 */
public enum SubscribeCategory {
    OTT("OTT"),
    MUSIC("음악"),
    FOOD("외식"),
    SHOPPING("쇼핑"),
    AI("AI"),
    DIGITAL_CONTENT("디지털 콘텐츠"),
    ETC("기타");

    private final String label;

    SubscribeCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
