package com.example.submate.domain.model;

/** 구독 상태 */
public enum SubscribeStatus {
    ACTIVE("구독중"),
    CANCEL_SCHEDULED("해지예정"),
    CANCELED("해지완료");

    private final String label;

    SubscribeStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
