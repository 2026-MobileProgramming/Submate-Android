package com.example.submate.data.dto.subscribelist;

import com.example.submate.domain.model.BillingCycle;
import com.example.submate.domain.model.SubscribeCategory;
import com.example.submate.domain.model.SubscribeCurrency;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.model.SubscribeStatus;

/** 서버 DTO와 앱 모델(SubscribeItem)을 서로 변환합니다. */
public final class SubscribeMapper {
    private SubscribeMapper() {
    }

    public static SubscribeItem toItem(SubscribeResponse response) {
        return new SubscribeItem(
                response.id,
                parse(SubscribeCategory.class, response.category, SubscribeCategory.ETC),
                response.serviceName,
                parse(SubscribeStatus.class, response.status, SubscribeStatus.ACTIVE),
                parse(BillingCycle.class, response.billingCycle, BillingCycle.MONTHLY),
                response.paymentMonth,
                response.paymentDay,
                parse(SubscribeCurrency.class, response.currency, SubscribeCurrency.KRW),
                response.amount,
                response.memo);
    }

    public static SubscribeRequest toRequest(SubscribeItem item) {
        SubscribeRequest request = new SubscribeRequest();
        request.category = item.getCategory().name();
        request.serviceName = item.getServiceName();
        request.status = item.getStatus().name();
        request.billingCycle = item.getBillingCycle().name();
        request.paymentMonth = item.getPaymentMonth();
        request.paymentDay = item.getPaymentDay();
        request.currency = item.getCurrency().name();
        request.amount = item.getAmount();
        request.memo = item.getMemo();
        return request;
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String value, E defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}
