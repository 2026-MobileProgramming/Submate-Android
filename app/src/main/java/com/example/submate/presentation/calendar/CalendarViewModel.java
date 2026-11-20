package com.example.submate.presentation.calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.submate.presentation.calendar.model.CalendarPayment;
import com.example.submate.presentation.calendar.model.CalendarUiState;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** 캘린더 화면의 상태(보고 있는 연/월, 선택한 날짜, 결제 목록)를 관리한다. **/
@HiltViewModel
public class CalendarViewModel extends ViewModel {
    private final MutableLiveData<CalendarUiState> uiState = new MutableLiveData<>();

    private final int todayYear;
    private final int todayMonth;
    private final int todayDay;

    private int year;
    private int month;
    private int selectedDay;

    @Inject
    public CalendarViewModel() {
        Calendar today = Calendar.getInstance();
        todayYear = today.get(Calendar.YEAR);
        todayMonth = today.get(Calendar.MONTH);
        todayDay = today.get(Calendar.DAY_OF_MONTH);

        year = todayYear;
        month = todayMonth;
        selectedDay = todayDay;
        publish();
    }

    public LiveData<CalendarUiState> getUiState() {
        return uiState;
    }

    public void showPreviousMonth() {
        month--;
        if (month < 0) {
            month = 11;
            year--;
        }
        selectedDay = defaultSelectedDay();
        publish();
    }

    public void showNextMonth() {
        month++;
        if (month > 11) {
            month = 0;
            year++;
        }
        selectedDay = defaultSelectedDay();
        publish();
    }

    public void showMonth(int year, int month) {
        this.year = year;
        this.month = month;
        selectedDay = defaultSelectedDay();
        publish();
    }

    public void selectDay(int day) {
        if (day < 1 || day > daysInMonth()) return;
        selectedDay = day;
        publish();
    }

    private int defaultSelectedDay() {
        return (year == todayYear && month == todayMonth) ? todayDay : 1;
    }

    private int daysInMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private void publish() {
        // TODO: API 연동 시 Repository 에서 해당 월 결제 내역을 받아오도록 변경
        int lastDay = daysInMonth();
        List<CalendarPayment> monthPayments = new ArrayList<>();
        for (CalendarPayment item : CalendarDummyData.getSubscriptions(todayYear, todayMonth)) {
            if (!item.occursIn(year, month)) continue;
            monthPayments.add(item.withBillingDay(Math.min(item.getBillingDay(), lastDay)));
        }
        uiState.setValue(new CalendarUiState(year, month, selectedDay,
                todayYear, todayMonth, todayDay, monthPayments));
    }
}