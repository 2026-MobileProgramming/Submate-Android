package com.example.submate.presentation.subscribelist.util;

import com.example.submate.R;
import com.example.submate.domain.model.BillingCycle;
import com.example.submate.domain.model.SubscribeCategory;
import com.example.submate.domain.model.SubscribeCurrency;
import com.example.submate.domain.model.SubscribeItem;
import java.text.DecimalFormat;
import java.util.Locale;

/** 구독 정보를 화면에 보여줄 문자열/색상으로 바꿔주는 도우미입니다. */
public final class SubscribeFormatter {
    private static final DecimalFormat NUMBER = new DecimalFormat("#,##0");
    private static final DecimalFormat DECIMAL = new DecimalFormat("#,##0.00");

    private SubscribeFormatter() {
    }

    /** ex) ₩4,250 */
    public static String formatKrw(long krw) {
        return "₩" + NUMBER.format(krw);
    }

    /** ex) ₩4,250 / $14.00 / ¥1,000 / 무료체험 */
    public static String formatPrice(SubscribeItem item) {
        if (item.isFreeTrial()) {
            return "무료체험";
        }
        SubscribeCurrency currency = item.getCurrency();
        if (currency == SubscribeCurrency.USD) {
            return currency.getSymbol() + DECIMAL.format(item.getAmount());
        }
        return currency.getSymbol() + NUMBER.format(Math.round(item.getAmount()));
    }

    /** 외화일 때만 원화 환산 금액을 돌려줍니다. ex) ≈₩19,000 */
    public static String formatConvertedPrice(SubscribeItem item) {
        if (!item.getCurrency().isForeign()) {
            return "";
        }
        return "≈" + formatKrw(item.getKrwAmount());
    }

    /** ex) 월간 · 매월 3일 / 연간 · 매년 1월 21일 / 월간 · 무료체험 10/11 종료 */
    public static String formatSchedule(SubscribeItem item) {
        String cycle = item.getBillingCycle().getLabel();
        if (item.isFreeTrial()) {
            return cycle + " · 무료체험 " + item.getPaymentMonth() + "/" + item.getPaymentDay() + " 종료";
        }
        if (item.getBillingCycle() == BillingCycle.YEARLY) {
            return cycle + " · 매년 " + item.getPaymentMonth() + "월 " + item.getPaymentDay() + "일";
        }
        return cycle + " · 매월 " + item.getPaymentDay() + "일";
    }

    /** 상세 화면 결제일 칸에 들어갈 문자열. ex) 15일 / 1월 21일 */
    public static String formatPaymentDate(boolean needMonth, int month, int day) {
        if (day <= 0) {
            return "";
        }
        if (needMonth) {
            return month + "월 " + day + "일";
        }
        return day + "일";
    }

    /** 금액 입력칸에 처음 보여줄 문자열. ex) 7,450 / 14.00 */
    public static String formatAmountInput(SubscribeItem item) {
        if (item.isFreeTrial()) {
            return "";
        }
        if (item.getCurrency() == SubscribeCurrency.USD) {
            return String.format(Locale.US, "%,.2f", item.getAmount());
        }
        return NUMBER.format(Math.round(item.getAmount()));
    }

    public static int categoryColor(SubscribeCategory category) {
        switch (category) {
            case OTT:
                return R.color.pastel_red;
            case MUSIC:
                return R.color.pastel_orange;
            case FOOD:
                return R.color.pastel_yellow;
            case SHOPPING:
                return R.color.pastel_green;
            case AI:
                return R.color.pastel_blue;
            case DIGITAL_CONTENT:
                return R.color.pastel_navy;
            default:
                return R.color.pastel_purple;
        }
    }
}
