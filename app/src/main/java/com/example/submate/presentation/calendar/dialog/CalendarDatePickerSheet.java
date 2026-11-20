package com.example.submate.presentation.calendar.dialog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * month 는 java.util.Calendar 와 같이 0(1월) ~ 11(12월)
 */
public class CalendarDatePickerSheet {
    private static final int MONTH_COLUMNS = 3;
    private static final int YEAR_COLUMNS = 2;
    private static final int YEAR_PAGE_SIZE = 4;

    private enum Tab { MONTH, YEAR, ALL }

    private final Context context;
    private final CalendarViewModel viewModel;
    private final BottomSheetDialog dialog;
    private final NumberFormat priceFormat = NumberFormat.getInstance(Locale.KOREA);

    private final TextView monthTab;
    private final TextView yearTab;
    private final TextView allTab;
    private final TextView yearText;
    private final View prevYearButton;
    private final View nextYearButton;
    private final LinearLayout monthContainer;
    private final TextView moveButton;
    private final View content;
    private final View overviewScroll;
    private final TextView overviewTitle;
    private final TextView overviewTotal;
    private final TextView overviewSub;
    private final LinearLayout overviewMonths;

    private Tab tab = Tab.MONTH;

    private int pickerYear;
    private int selectedYear;
    private int selectedMonth;

    private int yearPageStart;
    private int yearTabSelected;

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
        yearPageStart = selectedYear - 2;
        yearTabSelected = selectedYear;

        dialog = new BottomSheetDialog(context);
        content = LayoutInflater.from(context).inflate(R.layout.calendar_date_picker_sheet, null);
        dialog.setContentView(content);

        monthTab = content.findViewById(R.id.tv_picker_tab_month);
        yearTab = content.findViewById(R.id.tv_picker_tab_year);
        allTab = content.findViewById(R.id.tv_picker_tab_all);
        yearText = content.findViewById(R.id.tv_picker_year);
        prevYearButton = content.findViewById(R.id.btn_picker_prev_year);
        nextYearButton = content.findViewById(R.id.btn_picker_next_year);
        monthContainer = content.findViewById(R.id.ll_picker_months);
        moveButton = content.findViewById(R.id.btn_picker_move);
        overviewScroll = content.findViewById(R.id.sv_picker_overview);
        overviewTitle = content.findViewById(R.id.tv_overview_title);
        overviewTotal = content.findViewById(R.id.tv_overview_total);
        overviewSub = content.findViewById(R.id.tv_overview_sub);
        overviewMonths = content.findViewById(R.id.ll_overview_months);

        prevYearButton.setOnClickListener(v -> movePage(-1));
        nextYearButton.setOnClickListener(v -> movePage(1));
        moveButton.setOnClickListener(v -> {
            move();
            dialog.dismiss();
        });

        monthTab.setOnClickListener(v -> selectTab(Tab.MONTH));
        yearTab.setOnClickListener(v -> selectTab(Tab.YEAR));
        allTab.setOnClickListener(v -> selectTab(Tab.ALL));

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

    private void selectTab(Tab tab) {
        this.tab = tab;
        render();
    }

    private void movePage(int direction) {
        if (tab == Tab.YEAR) {
            yearPageStart += direction * YEAR_PAGE_SIZE;
        } else {
            pickerYear += direction;
        }
        render();
    }

    private void move() {
        switch (tab) {
            case MONTH:
                viewModel.showMonth(selectedYear, selectedMonth);
                break;
            case YEAR:
                viewModel.showMonth(yearTabSelected, 0);
                break;
        }
    }

    private void render() {
        styleTab(monthTab, tab == Tab.MONTH);
        styleTab(yearTab, tab == Tab.YEAR);
        styleTab(allTab, tab == Tab.ALL);

        boolean all = tab == Tab.ALL;
        setFullScreen(all);
        monthContainer.setVisibility(all ? View.GONE : View.VISIBLE);
        moveButton.setVisibility(all ? View.GONE : View.VISIBLE);
        overviewScroll.setVisibility(all ? View.VISIBLE : View.GONE);

        switch (tab) {
            case MONTH:
                renderMonthTab();
                break;
            case YEAR:
                renderYearTab();
                break;
            case ALL:
                renderAllTab();
                break;
        }
    }

    private void setFullScreen(boolean full) {
        int height = full ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT;
        ViewGroup.LayoutParams contentParams = content.getLayoutParams();
        if (contentParams != null) {
            contentParams.height = height;
            content.setLayoutParams(contentParams);
        }
        View sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet != null) {
            ViewGroup.LayoutParams sheetParams = sheet.getLayoutParams();
            sheetParams.height = height;
            sheet.setLayoutParams(sheetParams);
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void styleTab(TextView tabView, boolean selected) {
        tabView.setBackgroundResource(selected ? R.drawable.bg_calendar_tab_selected : 0);
        tabView.setTextAppearance(selected ? R.style.typo_body_b3 : R.style.typo_body_r1);
        tabView.setTextColor(color(selected ? R.color.gray800 : R.color.gray500));
    }

    private void renderMonthTab() {
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

        if (viewModel.isBeforeFirstRegistration(pickerYear, month)) {
            totalText.setText("없음");
        } else if (viewModel.isFutureMonth(pickerYear, month)) {
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

    private void renderYearTab() {
        yearText.setText(yearPageStart + " - " + (yearPageStart + YEAR_PAGE_SIZE - 1));

        monthContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);

        LinearLayout row = null;
        for (int i = 0; i < YEAR_PAGE_SIZE; i++) {
            if (i % YEAR_COLUMNS == 0) {
                row = new LinearLayout(context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                monthContainer.addView(row, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            View cell = inflater.inflate(R.layout.calendar_picker_month, row, false);
            bindYearCell(cell, yearPageStart + i);
            row.addView(cell);
        }

        moveButton.setText(yearTabSelected + "년으로 이동");
    }

    private void bindYearCell(View cell, int year) {
        TextView yearLabel = cell.findViewById(R.id.tv_picker_month);
        TextView totalText = cell.findViewById(R.id.tv_picker_month_total);
        yearLabel.setText(year + "년");

        if (viewModel.isBeforeFirstRegistration(year, 11)) {
            totalText.setText("없음");
        } else if (viewModel.isFutureYear(year)) {
            totalText.setText("예정");
        } else {
            totalText.setText("₩" + priceFormat.format(viewModel.getYearTotal(year)));
        }

        boolean selected = year == yearTabSelected;
        cell.setBackgroundResource(selected ? R.drawable.bg_calendar_picker_selected : 0);
        yearLabel.setTextColor(color(selected ? R.color.pri800 : R.color.gray800));
        totalText.setTextColor(color(selected ? R.color.pri700 : R.color.gray500));

        cell.setOnClickListener(v -> {
            yearTabSelected = year;
            render();
        });
    }

    private void renderAllTab() {
        yearText.setText(pickerYear + "년");

        int[] monthTotals = new int[12];
        int maxMonthTotal = 0;
        int paidMonths = 0;
        for (int month = 0; month < 12; month++) {
            monthTotals[month] = viewModel.getMonthTotal(pickerYear, month);
            maxMonthTotal = Math.max(maxMonthTotal, monthTotals[month]);
            if (monthTotals[month] > 0) paidMonths++;
        }
        int yearTotal = viewModel.getYearTotal(pickerYear);
        int average = paidMonths == 0 ? 0 : yearTotal / paidMonths;

        overviewTitle.setText(pickerYear + "년 결제 합계 (예정 포함)");
        overviewTotal.setText(formatPrice(yearTotal));
        overviewSub.setText("월 평균 " + formatPrice(average) + " · 구독 "
                + viewModel.getYearSubscriptionCount(pickerYear) + "개");

        overviewMonths.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);
        for (int month = 0; month < 12; month++) {
            View row = inflater.inflate(R.layout.calendar_overview_month, overviewMonths, false);
            bindOverviewMonth(row, month, monthTotals[month], maxMonthTotal);
            overviewMonths.addView(row);
        }
    }

    private void bindOverviewMonth(View row, int month, int total, int maxTotal) {
        ((TextView) row.findViewById(R.id.tv_overview_month)).setText((month + 1) + "월");
        TextView amountText = row.findViewById(R.id.tv_overview_amount);
        LinearLayout bar = row.findViewById(R.id.ll_overview_bar);
        boolean future = viewModel.isFutureMonth(pickerYear, month);

        bar.removeAllViews();
        if (viewModel.isBeforeFirstRegistration(pickerYear, month)) {
            amountText.setText("없음");
            amountText.setTextColor(color(R.color.gray400));
            bar.setBackground(null);
            return;
        }

        if (future) {
            amountText.setText("예정");
            amountText.setTextColor(color(R.color.gray400));
        } else {
            amountText.setText(formatPrice(total));
            amountText.setTextColor(color(R.color.appblack));
        }

        bar.setBackgroundResource(R.drawable.bg_calendar_bar_segment);
        bar.setBackgroundTintList(ColorStateList.valueOf(color(R.color.gray100)));
        if (total > 0) {
            View fill = new View(context);
            fill.setBackgroundResource(R.drawable.bg_calendar_bar_segment);
            fill.setBackgroundTintList(ColorStateList.valueOf(color(future ? R.color.pri200 : R.color.pri700)));
            bar.addView(fill, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, total));
        }
        View rest = new View(context);
        bar.addView(rest, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, maxTotal - total));
    }

    private String formatPrice(int price) {
        return "₩" + priceFormat.format(price);
    }

    private int color(int colorRes) {
        return ContextCompat.getColor(context, colorRes);
    }
}