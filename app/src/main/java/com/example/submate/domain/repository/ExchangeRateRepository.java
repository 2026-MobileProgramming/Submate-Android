package com.example.submate.domain.repository;

import androidx.lifecycle.LiveData;

/** 외화(USD, JPY)의 원화 환율을 관리합니다. 받아온 환율은 SubscribeCurrency에 반영됩니다. */
public interface ExchangeRateRepository {
    /** 환율이 새로 반영된 시각(ms). 값이 바뀌면 원화 환산 금액을 다시 계산해야 합니다. */
    LiveData<Long> observeUpdatedAt();

    /** 최신 환율을 받아옵니다. 받아온 지 얼마 안 됐으면 다시 요청하지 않습니다. */
    void refresh();
}
