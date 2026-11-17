package com.example.submate.presentation.subscribelist.util;

import android.text.InputFilter;
import android.text.Spanned;

/** 최댓값보다 큰 숫자가 입력되지 않게 막습니다. ex) 결제일 칸에 32 이상 입력 방지 */
public class MaxNumberInputFilter implements InputFilter {
    private final int max;

    public MaxNumberInputFilter(int max) {
        this.max = max;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        // 입력이 반영된 뒤의 글자를 미리 만들어 보고, 범위를 넘으면 입력을 무시합니다.
        String result = dest.subSequence(0, dstart).toString()
                + source.subSequence(start, end)
                + dest.subSequence(dend, dest.length());
        if (result.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(result) <= max ? null : "";
        } catch (NumberFormatException e) {
            return "";
        }
    }
}
