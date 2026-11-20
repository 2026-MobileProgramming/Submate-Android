package com.example.submate.presentation.calendar.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.example.submate.R;
import com.example.submate.presentation.calendar.CalendarViewModel;
import com.example.submate.presentation.calendar.model.CalendarUiState;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * 캘린더 상단 "v" 를 눌렀을 때 뜨는 날짜 선택 바텀시트
 * 현재는 "월" 탭만 동작한다. (연 / 전체 탭은 추후 구현)
 * month 는 java.util.Calendar 와 같이 0(1월) ~ 11(12월)
 */
public class CalendarDatePickerSheet {
    private static final int MONTH_COLUMNS = 3;

    private final Context context;
    private final CalendarViewModel viewModel;
    private final BottomSheetDialog dialog;
    private final NumberFormat priceFormat = NumberFormat.getInstance(Locale.KOREA);

    private final TextView yearText;
    private final View prevYearButton;
    private final View nextYearButton;
    private final LinearLayout monthContainer;
    private final TextView moveButton;

    private int pickerYear;
    private int selectedYear;
    private int selectedMonth;

    public CalendarDatePickerSheet(@NonNull Context context, @NonNull CalendarViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;

        CalendarUiState state = viewModel.getUiState().getValue();
        if (state != null) {
            selectedYear = state.getYear();
            selectedMonth = state.getMonth();
        } else {
            selectedYear = viewModel.getTodayYear();
            selectedMonth = viewModel.getTodayMonth();
        }
        pickerYear = selectedYear;

        dialog = new BottomSheetDialog(context);
        View content = LayoutInflater.from(context).inflate(R.layout.calendar_date_picker_sheet, null);
        dialog.setContentView(content);

        yearText = content.findViewById(R.id.tv_picker_year);
        prevYearButton = content.findViewById(R.id.btn_picker_prev_year);
        nextYearButton = content.findViewById(R.id.btn_picker_next_year);
        monthContainer = content.findViewById(R.id.ll_picker_months);
        moveButton = content.findViewById(R.id.btn_picker_move);

        prevYearButton.setOnClickListener(v -> {
            pickerYear--;
            render();
        });
        nextYearButton.setOnClickListener(v -> {
            pickerYear++;
            render();
        });
        moveButton.setOnClickListener(v -> {
            viewModel.showMonth(selectedYear, selectedMonth);
            dialog.dismiss();
        });

        content.findViewById(R.id.tv_picker_tab_year).setOnClickListener(v -> {
            // TODO: 연 탭 구현
        });
        content.findViewById(R.id.tv_picker_tab_all).setOnClickListener(v -> {
            // TODO: 전체 탭 구현
        });

        dialog.setOnShowListener(d -> {
            View sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet != null) {
                sheet.setBackgroundColor(Color.TRANSPARENT);
                BottomSheetBehavior.from(sheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        render();
    }

    public void show() {
        dialog.show();
    }

    private void render() {
        yearText.setText(pickerYear + "년");

        renderMonths();

        String label = (selectedMonth + 1) + "월로 이동";
        if (selectedYear != pickerYear) label = selectedYear + "년 " + label;
        moveButton.setText(label);
    }

    private void renderMonths() {
        monthContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);

        LinearLayout row = null;
        for (int month = 0; month < 12; month++) {
            if (month % MONTH_COLUMNS == 0) {
                row = new LinearLayout(context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                monthContainer.addView(row, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            View cell = inflater.inflate(R.layout.calendar_picker_month, row, false);
            bindMonthCell(cell, month);
            row.addView(cell);
        }
    }

    private void bindMonthCell(View cell, int month) {
        TextView monthText = cell.findViewById(R.id.tv_picker_month);
        TextView totalText = cell.findViewById(R.id.tv_picker_month_total);
        monthText.setText((month + 1) + "월");

        if (viewModel.isFutureMonth(pickerYear, month)) {
            totalText.setText("예정");
        } else {
            totalText.setText("₩" + priceFormat.format(viewModel.getMonthTotal(pickerYear, month)));
        }

        boolean selected = pickerYear == selectedYear && month == selectedMonth;
        cell.setBackgroundResource(selected ? R.drawable.bg_calendar_picker_selected : 0);
        monthText.setTextColor(color(selected ? R.color.pri800 : R.color.gray800));
        totalText.setTextColor(color(selected ? R.color.pri700 : R.color.gray500));

        cell.setOnClickListener(v -> {
            selectedYear = pickerYear;
            selectedMonth = month;
            render();
        });
    }

    private int color(int colorRes) {
        return ContextCompat.getColor(context, colorRes);
    }
}
