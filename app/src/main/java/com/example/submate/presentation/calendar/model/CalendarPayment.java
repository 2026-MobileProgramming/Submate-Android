package com.example.submate.presentation.calendar.model;

/**
 * 캘린더에 표시할 구독 결제 1건.
 * billingYear / billingMonth 가 ANY 이면 매년 / 매월 결제된다.
 * (월간: 둘 다 ANY, 연간: 월만 지정, 무료체험: 연·월 모두 지정)
 * month 는 java.util.Calendar 와 같이 0(1월) ~ 11(12월).
 */
public final class CalendarPayment {
    public static final int ANY = -1;

    private final String name;
    private final CalendarCategory category;
    private final CalendarBillingType billingType;
    private final int price;
    private final int billingDay;
    private final int billingYear;
    private final int billingMonth;

    public CalendarPayment(String name, CalendarCategory category, CalendarBillingType billingType,
                           int price, int billingDay, int billingYear, int billingMonth) {
        this.name = name;
        this.category = category;
        this.billingType = billingType;
        this.price = price;
        this.billingDay = billingDay;
        this.billingYear = billingYear;
        this.billingMonth = billingMonth;
    }

    public static CalendarPayment monthly(String name, CalendarCategory category, int price, int day) {
        return new CalendarPayment(name, category, CalendarBillingType.MONTHLY, price, day, ANY, ANY);
    }

    public static CalendarPayment yearly(String name, CalendarCategory category, int price, int month, int day) {
        return new CalendarPayment(name, category, CalendarBillingType.YEARLY, price, day, ANY, month);
    }

    public static CalendarPayment freeTrial(String name, CalendarCategory category, int year, int month, int day) {
        return new CalendarPayment(name, category, CalendarBillingType.FREE_TRIAL, 0, day, year, month);
    }

    public boolean occursIn(int year, int month) {
        if (billingYear != ANY && billingYear != year) return false;
        return billingMonth == ANY || billingMonth == month;
    }

    public CalendarPayment withBillingDay(int day) {
        return new CalendarPayment(name, category, billingType, price, day, billingYear, billingMonth);
    }

    public boolean isFreeTrial() { return billingType == CalendarBillingType.FREE_TRIAL; }

    public String getName() { return name; }
    public CalendarCategory getCategory() { return category; }
    public CalendarBillingType getBillingType() { return billingType; }
    public int getPrice() { return price; }
    public int getBillingDay() { return billingDay; }
}