package com.example.submate.data.network;

import com.example.submate.data.dto.exchangerate.ExchangeRateResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/** 환율 API (Frankfurter: 무료, 인증키 없음, 유럽중앙은행 기준 환율을 하루 한 번 갱신) */
public interface ExchangeRateApi {
    /** ex) base=USD, symbols=KRW -> 1달러가 몇 원인지 */
    @GET("v1/latest")
    Call<ExchangeRateResponse> getLatestRates(@Query("base") String base,
                                              @Query("symbols") String symbols);
}
