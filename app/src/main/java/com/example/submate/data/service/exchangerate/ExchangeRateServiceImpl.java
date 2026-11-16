package com.example.submate.data.service.exchangerate;

import com.example.submate.data.dto.exchangerate.ExchangeRateResponse;
import com.example.submate.data.network.ExchangeRateApi;
import com.example.submate.domain.model.SubscribeCurrency;
import com.example.submate.domain.repository.ResultCallback;
import javax.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Retrofit으로 환율 API와 통신하는 구현체입니다. */
public class ExchangeRateServiceImpl implements ExchangeRateService {
    private static final String KRW = SubscribeCurrency.KRW.name();
    private static final String ERROR = "환율 정보를 불러오지 못했습니다.";

    private final ExchangeRateApi api;

    @Inject
    public ExchangeRateServiceImpl(ExchangeRateApi api) {
        this.api = api;
    }

    @Override
    public void getKrwRate(SubscribeCurrency currency, ResultCallback<Double> callback) {
        api.getLatestRates(currency.name(), KRW).enqueue(new Callback<ExchangeRateResponse>() {
            @Override
            public void onResponse(Call<ExchangeRateResponse> call,
                                   Response<ExchangeRateResponse> response) {
                ExchangeRateResponse body = response.body();
                Double rate = body == null || body.rates == null ? null : body.rates.get(KRW);
                if (response.isSuccessful() && rate != null && rate > 0) {
                    callback.onSuccess(rate);
                } else {
                    callback.onError(ERROR);
                }
            }

            @Override
            public void onFailure(Call<ExchangeRateResponse> call, Throwable t) {
                callback.onError(ERROR);
            }
        });
    }
}
