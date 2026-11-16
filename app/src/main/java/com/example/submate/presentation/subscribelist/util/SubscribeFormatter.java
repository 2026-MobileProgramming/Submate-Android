package com.example.submate.presentation.subscribelist.util;

import com.example.submate.R;
import com.example.submate.domain.model.BillingCycle;
import com.example.submate.domain.model.SubscribeCategory;
import com.example.submate.domain.model.SubscribeCurrency;
import com.example.submate.domain.model.SubscribeItem;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** 구독 정보를 화면에 보여줄 문자열/색상으로 바꿔주는 도우미입니다. */
public final class SubscribeFormatter {
    // 기기 언어와 상관없이 항상 1,234.56 형태로 표시합니다.
    private static final DecimalFormatSymbols SYMBOLS = DecimalFormatSymbols.getInstance(Locale.US);
    private static final DecimalFormat NUMBER = new DecimalFormat("#,##0", SYMBOLS);
    private static final DecimalFormat DECIMAL = new DecimalFormat("#,##0.00", SYMBOLS);
    private static final DecimalFormat RATE = new DecimalFormat("#,##0.##", SYMBOLS);

    private SubscribeFormatter() {
    }

    /** ex) 4,250 */
    public static String formatNumber(long number) {
        return NUMBER.format(number);
    }

    /** ex) ₩4,250 */
    public static String formatKrw(long krw) {
        return "₩" + formatNumber(krw);
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
        return currency.getSymbol() + formatNumber(Math.round(item.getAmount()));
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

    /** 금액 입력칸에 처음 보여줄 문자열. ex) 7,450 / 14.00 */
    public static String formatAmountInput(SubscribeItem item) {
        if (item.isFreeTrial()) {
            return "";
        }
        if (item.getCurrency() == SubscribeCurrency.USD) {
            return DECIMAL.format(item.getAmount());
        }
        return formatNumber(Math.round(item.getAmount()));
    }

    /** 금액 입력칸의 글자를 숫자로 바꿉니다. 비어 있거나 숫자가 아니면 0 */
    public static double parseAmount(String text) {
        if (text == null) {
            return 0;
        }
        try {
            return Double.parseDouble(text.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** 숫자 입력칸(월, 일)의 글자를 정수로 바꿉니다. 비어 있거나 숫자가 아니면 0 */
    public static int parseNumber(String text) {
        if (text == null) {
            return 0;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** 외화 금액 아래에 보여줄 환산 안내. ex) ≈₩19,000 · 오늘 환율 1 USD = ₩1,357 기준 */
    public static String formatExchangePreview(SubscribeCurrency currency, double amount) {
        return "≈" + formatKrw(currency.toKrw(amount))
                + " · 오늘 환율 1 " + currency.name() + " = ₩" + RATE.format(currency.getKrwRate()) + " 기준";
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
