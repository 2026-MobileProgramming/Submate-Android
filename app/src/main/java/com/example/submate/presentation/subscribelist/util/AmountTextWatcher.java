package com.example.submate.presentation.subscribelist.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/** 금액 입력 시 자동으로 천 단위 콤마를 넣어줍니다. ex) 7450 -> 7,450 */
public class AmountTextWatcher implements TextWatcher {
    private static final int MAX_INTEGER_LENGTH = 10;
    private static final int MAX_DECIMAL_LENGTH = 2;

    private final EditText editText;
    private boolean allowDecimal = false;
    private boolean formatting = false;

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
        // setText() 때문에 이 메서드가 다시 불리는 것을 막습니다.
        if (formatting || s == null) {
            return;
        }
        String text = s.toString();
        String formatted = format(text);
        if (formatted.equals(text)) {
            return;
        }
        // 콤마가 다시 찍혀도 커서가 같은 숫자 앞에 있도록, 커서 뒤의 글자 수를 기억합니다.
        int cursor = Math.max(editText.getSelectionEnd(), 0);
        int charsAfterCursor = countKept(text.substring(Math.min(cursor, text.length())));

        formatting = true;
        editText.setText(formatted);
        formatting = false;

        int length = editText.length();
        editText.setSelection(Math.min(findCursor(formatted, charsAfterCursor), length));
    }

    /** 숫자와 소수점만 남기고, 정수 부분에 천 단위 콤마를 넣습니다. (KRW·JPY는 소수점 제거) */
    String format(String text) {
        // 붙여넣기 등으로 숫자가 아닌 글자가 들어와도 앱이 멈추지 않게 숫자와 소수점만 남깁니다.
        String raw = text.replaceAll("[^0-9.]", "");
        String integerPart = raw;
        String decimalPart = null;

        int dot = raw.indexOf('.');
        if (dot >= 0) {
            integerPart = raw.substring(0, dot);
            if (allowDecimal) {
                decimalPart = raw.substring(dot + 1).replace(".", "");
                if (decimalPart.length() > MAX_DECIMAL_LENGTH) {
                    decimalPart = decimalPart.substring(0, MAX_DECIMAL_LENGTH);
                }
            }
        }
        if (integerPart.length() > MAX_INTEGER_LENGTH) {
            integerPart = integerPart.substring(0, MAX_INTEGER_LENGTH);
        }

        String result = integerPart.isEmpty() ? "" : SubscribeFormatter.formatNumber(Long.parseLong(integerPart));
        if (decimalPart != null) {
            result = (result.isEmpty() ? "0" : result) + "." + decimalPart;
        }
        return result;
    }

    /** format()에서 남는 글자(숫자, 소수점) 수 */
    private static int countKept(String text) {
        return text.replaceAll("[^0-9.]", "").length();
    }

    /** 뒤에서부터 세어, 콤마를 뺀 글자가 charsAfterCursor개 남는 위치를 찾습니다. */
    static int findCursor(String formatted, int charsAfterCursor) {
        int position = formatted.length();
        int remaining = charsAfterCursor;
        while (position > 0 && remaining > 0) {
            position--;
            if (formatted.charAt(position) != ',') {
                remaining--;
            }
        }
        // 커서가 콤마 바로 뒤에 놓이면, 지우기를 눌렀을 때 콤마만 지워졌다 다시 생기므로 앞으로 옮깁니다.
        if (position > 0 && formatted.charAt(position - 1) == ',') {
            position--;
        }
        return position;
    }
}
