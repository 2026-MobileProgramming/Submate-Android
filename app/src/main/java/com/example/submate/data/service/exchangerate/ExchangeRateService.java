package com.example.submate.data.service.exchangerate;

import com.example.submate.domain.model.SubscribeCurrency;
import com.example.submate.domain.repository.ResultCallback;

/** 환율을 가져오는 외부 데이터 소스(환율 API)의 경계입니다. */
public interface ExchangeRateService {
    /** 해당 외화 1 단위가 몇 원인지 가져옵니다. */
    void getKrwRate(SubscribeCurrency currency, ResultCallback<Double> callback);
}
