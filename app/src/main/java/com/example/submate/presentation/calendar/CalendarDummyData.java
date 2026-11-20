package com.example.submate.presentation.calendar;

import com.example.submate.presentation.calendar.model.CalendarCategory;
import com.example.submate.presentation.calendar.model.CalendarPayment;
import java.util.ArrayList;
import java.util.List;

/**
 * API 연동 전까지 캘린더 화면 확인용으로 쓰는 임시 구독 데이터
 * API 연동 후 이 파일 지우기.
 */
final class CalendarDummyData {
    private CalendarDummyData() {}

    private static final int YEAR = 2026;
    private static final int MONTH = 10;
    private static final int REGISTERED_ON = CalendarPayment.dateKey(YEAR, MONTH, 1);

    static List<CalendarPayment> getSubscriptions() {
        List<CalendarPayment> list = new ArrayList<>();
        list.add(CalendarPayment.monthly("넷플릭스", CalendarCategory.OTT, 13500, 3));
        list.add(CalendarPayment.monthly("유튜브 프리미엄", CalendarCategory.MUSIC, 14900, 8));
        list.add(CalendarPayment.monthly("라프텔", CalendarCategory.OTT, 9900, 15));
        list.add(CalendarPayment.monthly("배달의민족", CalendarCategory.FOOD, 3990, 15));
        list.add(CalendarPayment.yearly("밀리의 서재", CalendarCategory.DIGITAL_CONTENT, 99000, MONTH, 20));
        list.add(CalendarPayment.freeTrial("왓챠", CalendarCategory.OTT, YEAR, MONTH, 22));
        list.add(CalendarPayment.monthly("쿠팡 와우", CalendarCategory.SHOPPING, 7890, 27));
        list.add(CalendarPayment.monthly("ChatGPT", CalendarCategory.AI, 29000, 27));

        List<CalendarPayment> registered = new ArrayList<>();
        for (CalendarPayment item : list) registered.add(item.withRegisteredOn(REGISTERED_ON));
        return registered;
    }
}