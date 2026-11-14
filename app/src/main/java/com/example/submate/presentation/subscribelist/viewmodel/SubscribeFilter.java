package com.example.submate.presentation.subscribelist.viewmodel;

import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.model.SubscribeStatus;

/** 구독 목록 상단 필터 칩 */
public enum SubscribeFilter {
    ALL("전체"),
    ACTIVE("구독중"),
    CANCEL_SCHEDULED("해지예정"),
    CANCELED("해지완료");

    private final String label;

    SubscribeFilter(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** 전체 탭에는 해지완료된 구독을 보여주지 않습니다. */
    public boolean matches(SubscribeItem item) {
        switch (this) {
            case ACTIVE:
                return item.getStatus() == SubscribeStatus.ACTIVE;
            case CANCEL_SCHEDULED:
                return item.getStatus() == SubscribeStatus.CANCEL_SCHEDULED;
            case CANCELED:
                return item.getStatus() == SubscribeStatus.CANCELED;
            default:
                return item.getStatus() != SubscribeStatus.CANCELED;
        }
    }
}
