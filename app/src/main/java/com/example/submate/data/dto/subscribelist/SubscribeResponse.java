package com.example.submate.data.dto.subscribelist;

import com.google.gson.annotations.SerializedName;

/** 서버가 내려주는 구독 정보 */
public class SubscribeResponse {
    @SerializedName("id")
    public long id;
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
    /** 서비스명과 같은 이름의 설치된 앱 패키지명. 없으면 "" (리포트 사용량 조회용) */
    @SerializedName("packageName")
    public String packageName;
}
