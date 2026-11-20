package com.example.submate.presentation.calendar.model;

import androidx.annotation.ColorRes;

import com.example.submate.R;

/** 구독 카테고리. 달력 원, 비율 바, 로고 색이 이 색을 따른다. **/
public enum CalendarCategory {
    OTT("OTT", R.color.pastel_red, R.color.appwhite),
    MUSIC("음악", R.color.pastel_orange, R.color.appwhite),
    FOOD("외식", R.color.pastel_yellow, R.color.gray800),
    SHOPPING("쇼핑", R.color.pastel_green, R.color.gray800),
    AI("AI", R.color.pastel_blue, R.color.gray800),
    DIGITAL_CONTENT("디지털 콘텐츠", R.color.pastel_navy, R.color.appwhite),
    ETC("기타", R.color.pastel_purple, R.color.appwhite);

    private final String label;
    @ColorRes
    private final int colorRes;
    @ColorRes
    private final int textColorRes;

    CalendarCategory(String label, @ColorRes int colorRes, @ColorRes int textColorRes) {
        this.label = label;
        this.colorRes = colorRes;
        this.textColorRes = textColorRes;
    }

    public String getLabel() { return label; }

    @ColorRes
    public int getColorRes() { return colorRes; }

    /** 카테고리 위 글자 색 **/
    @ColorRes
    public int getTextColorRes() { return textColorRes; }
}
