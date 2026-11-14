package com.example.submate.presentation.subscribelist.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.text.DecimalFormat;

/** 금액 입력 시 자동으로 천 단위 콤마를 넣어줍니다. ex) 7450 -> 7,450 */
public class AmountTextWatcher implements TextWatcher {
    private static final DecimalFormat NUMBER = new DecimalFormat("#,##0");
    private static final int MAX_INTEGER_LENGTH = 10;

    private final EditText editText;
    private boolean allowDecimal = false;
    private boolean editing = false;

    public AmountTextWatcher(EditText editText) {
        this.editText = editText;
    }

    /** 달러처럼 소수점(센트)이 필요한 통화일 때 true */
    public void setAllowDecimal(boolean allowDecimal) {
        this.allowDecimal = allowDecimal;
        afterTextChanged(editText.getText());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (editing || s == null) {
            return;
        }
        editing = true;
        String formatted = format(s.toString());
        if (!formatted.equals(s.toString())) {
            editText.setText(formatted);
            editText.setSelection(formatted.length());
        }
        editing = false;
    }

    private String format(String text) {
        String raw = text.replace(",", "");
        String integerPart = raw;
        String decimalPart = null;

        int dot = raw.indexOf('.');
        if (dot >= 0) {
            integerPart = raw.substring(0, dot);
            if (allowDecimal) {
                decimalPart = raw.substring(dot + 1).replace(".", "");
                if (decimalPart.length() > 2) {
                    decimalPart = decimalPart.substring(0, 2);
                }
            }
        }
        if (integerPart.length() > MAX_INTEGER_LENGTH) {
            integerPart = integerPart.substring(0, MAX_INTEGER_LENGTH);
        }

        String result = integerPart.isEmpty() ? "" : NUMBER.format(Long.parseLong(integerPart));
        if (decimalPart != null) {
            result = (result.isEmpty() ? "0" : result) + "." + decimalPart;
        }
        return result;
    }
}
