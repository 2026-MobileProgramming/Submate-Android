package com.example.submate.data.dto.exchangerate;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * 환율 API 응답
 * ex) {"amount":1.0,"base":"USD","date":"2026-11-13","rates":{"KRW":1357.2}}
 */
public class ExchangeRateResponse {
    @SerializedName("base")
    public String base;
    @SerializedName("date")
    public String date;
    @SerializedName("rates")
    public Map<String, Double> rates;
}
