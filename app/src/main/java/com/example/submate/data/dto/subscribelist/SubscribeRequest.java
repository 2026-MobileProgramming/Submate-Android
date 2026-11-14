package com.example.submate.data.dto.subscribelist;

import com.google.gson.annotations.SerializedName;

/** 구독 추가/수정 요청 Body */
public class SubscribeRequest {
    @SerializedName("category")
    public String category;
    @SerializedName("serviceName")
    public String serviceName;
    @SerializedName("status")
    public String status;
    @SerializedName("billingCycle")
    public String billingCycle;
    @SerializedName("paymentMonth")
    public int paymentMonth;
    @SerializedName("paymentDay")
    public int paymentDay;
    @SerializedName("currency")
    public String currency;
    @SerializedName("amount")
    public double amount;
    @SerializedName("memo")
    public String memo;
}
