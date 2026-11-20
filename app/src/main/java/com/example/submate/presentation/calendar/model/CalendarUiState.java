package com.example.submate.presentation.calendar.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 캘린더 화면 한 번 그리는 데 필요한 상태.
 * month 는 java.util.Calendar 와 같이 0(1월) ~ 11(12월).
 * payments 의 billingDay 는 해당 월에 맞게 보정된 실제 결제일.
 */
public final class CalendarUiState {
    private final int year;
    private final int month;
    private final int selectedDay;
    private final int todayYear;
    private final int todayMonth;
    private final int todayDay;
    private final List<CalendarPayment> payments;

    public CalendarUiState(int year, int month, int selectedDay,
                           int todayYear, int todayMonth, int todayDay,
                           List<CalendarPayment> payments) {
        this.year = year;
        this.month = month;
        this.selectedDay = selectedDay;
        this.todayYear = todayYear;
        this.todayMonth = todayMonth;
        this.todayDay = todayDay;
        this.payments = Collections.unmodifiableList(new ArrayList<>(payments));
    }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getSelectedDay() { return selectedDay; }
    public List<CalendarPayment> getPayments() { return payments; }

    public boolean isToday(int day) {
        return year == todayYear && month == todayMonth && day == todayDay;
    }

    public List<CalendarPayment> getPaymentsOf(int day) {
        List<CalendarPayment> result = new ArrayList<>();
        for (CalendarPayment payment : payments) {
            if (payment.getBillingDay() == day) result.add(payment);
        }
        return result;
    }

    public int getTotalOf(int day) {
        int total = 0;
        for (CalendarPayment payment : getPaymentsOf(day)) total += payment.getPrice();
        return total;
    }

    public int getMonthTotal() {
        int total = 0;
        for (CalendarPayment payment : payments) total += payment.getPrice();
        return total;
    }

    public Map<CalendarCategory, Integer> getCategoryTotals() {
        Map<CalendarCategory, Integer> totals = new LinkedHashMap<>();
        for (CalendarPayment payment : payments) {
            if (payment.getPrice() <= 0) continue;
            Integer prev = totals.get(payment.getCategory());
            totals.put(payment.getCategory(), (prev == null ? 0 : prev) + payment.getPrice());
        }
        List<Map.Entry<CalendarCategory, Integer>> entries = new ArrayList<>(totals.entrySet());
        Collections.sort(entries, (a, b) -> Integer.compare(b.getValue(), a.getValue()));
        Map<CalendarCategory, Integer> sorted = new LinkedHashMap<>();
        for (Map.Entry<CalendarCategory, Integer> entry : entries) {
            sorted.put(entry.getKey(), entry.getValue());
        }
        return sorted;
    }
}