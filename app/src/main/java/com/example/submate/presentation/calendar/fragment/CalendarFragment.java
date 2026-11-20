package com.example.submate.presentation.calendar.fragment;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.submate.R;
import com.example.submate.presentation.calendar.model.CalendarCategory;
import com.example.submate.presentation.calendar.model.CalendarPayment;
import com.example.submate.presentation.calendar.model.CalendarUiState;
import com.example.submate.presentation.calendar.CalendarViewModel;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CalendarFragment extends Fragment {
    private static final int MAX_BADGES_PER_DAY = 2;
    private static final String[] DAY_OF_WEEK_NAMES =
            {"일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일"};

    private final NumberFormat priceFormat = NumberFormat.getInstance(Locale.KOREA);

    private CalendarViewModel viewModel;

    private TextView yearText;
    private TextView monthText;
    private LinearLayout weekContainer;
    private TextView monthTotalText;
    private LinearLayout legendContainer;
    private LinearLayout ratioBar;
    private TextView selectedDateText;
    private TextView selectedTotalText;
    private LinearLayout paymentContainer;
    private TextView paymentEmptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.screen_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        yearText = view.findViewById(R.id.tv_calendar_year);
        monthText = view.findViewById(R.id.tv_calendar_month);
        weekContainer = view.findViewById(R.id.ll_calendar_weeks);
        monthTotalText = view.findViewById(R.id.tv_calendar_month_total);
        legendContainer = view.findViewById(R.id.ll_calendar_legend);
        ratioBar = view.findViewById(R.id.ll_calendar_ratio_bar);
        selectedDateText = view.findViewById(R.id.tv_calendar_selected_date);
        selectedTotalText = view.findViewById(R.id.tv_calendar_selected_total);
        paymentContainer = view.findViewById(R.id.ll_calendar_payments);
        paymentEmptyText = view.findViewById(R.id.tv_calendar_payment_empty);

        view.findViewById(R.id.btn_calendar_prev).setOnClickListener(v -> viewModel.showPreviousMonth());
        view.findViewById(R.id.btn_calendar_next).setOnClickListener(v -> viewModel.showNextMonth());
        view.findViewById(R.id.ll_calendar_month_picker).setOnClickListener(v -> {
            // TODO: 날짜 선택 바텀시트(월/연/전체) 구현 후 연결
        });
        view.findViewById(R.id.btn_calendar_notification).setOnClickListener(v -> {
            // TODO: 알림 화면 연결
        });

        viewModel.getUiState().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(@Nullable CalendarUiState state) {
        if (state == null) return;
        yearText.setText(state.getYear() + "년");
        monthText.setText((state.getMonth() + 1) + "월");

        renderCalendar(state);
        renderSummary(state);
        renderSelectedDay(state);
    }

    private void renderCalendar(CalendarUiState state) {
        weekContainer.removeAllViews();

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(state.getYear(), state.getMonth(), 1);
        int firstDayOffset = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int weekCount = (firstDayOffset + daysInMonth + 6) / 7;

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int week = 0; week < weekCount; week++) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            for (int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                View cell = inflater.inflate(R.layout.calendar_day, row, false);
                row.addView(cell);

                int day = week * 7 + dayOfWeek - firstDayOffset + 1;
                if (day < 1 || day > daysInMonth) {
                    cell.setVisibility(View.INVISIBLE);
                } else {
                    bindDayCell(cell, day, dayOfWeek, state);
                }
            }
            weekContainer.addView(row);
        }
    }

    private void bindDayCell(View cell, int day, int dayOfWeek, CalendarUiState state) {
        TextView dayText = cell.findViewById(R.id.tv_calendar_day);
        LinearLayout badgeContainer = cell.findViewById(R.id.ll_calendar_day_badges);

        dayText.setText(String.valueOf(day));
        if (state.isToday(day)) {
            dayText.setBackgroundResource(R.drawable.bg_calendar_today);
            dayText.setTextColor(color(R.color.appwhite));
        } else {
            dayText.setBackground(null);
            dayText.setTextColor(color(dayOfWeek == 0 ? R.color.error500 : R.color.gray800));
        }

        if (day == state.getSelectedDay()) {
            cell.setBackgroundResource(R.drawable.bg_calendar_selected);
        } else {
            cell.setBackground(null);
        }

        // 결제마다 카테고리 색 원 + 서비스 이름 첫 글자 (최대 2개, 넘치면 +N)
        badgeContainer.removeAllViews();
        List<CalendarPayment> payments = state.getPaymentsOf(day);
        boolean overflow = payments.size() > MAX_BADGES_PER_DAY;
        int badgeCount = overflow ? MAX_BADGES_PER_DAY - 1 : payments.size();
        for (int i = 0; i < badgeCount; i++) {
            CalendarPayment payment = payments.get(i);
            CalendarCategory category = payment.getCategory();
            badgeContainer.addView(createDayBadge(firstLetter(payment.getName()),
                    category.getColorRes(), category.getTextColorRes()));
        }
        if (overflow) {
            badgeContainer.addView(createDayBadge("+" + (payments.size() - badgeCount),
                    R.color.gray200, R.color.gray700));
        }

        cell.setOnClickListener(v -> viewModel.selectDay(day));
    }

    private void renderSummary(CalendarUiState state) {
        int monthTotal = state.getMonthTotal();
        monthTotalText.setText(formatPrice(monthTotal));

        legendContainer.removeAllViews();
        ratioBar.removeAllViews();

        if (monthTotal == 0) {
            addRatioSegment(R.color.gray200, 1, false);
            return;
        }

        boolean first = true;
        int legendCount = 0;
        for (Map.Entry<CalendarCategory, Integer> entry : state.getCategoryTotals().entrySet()) {
            CalendarCategory category = entry.getKey();
            int amount = entry.getValue();
            int percent = Math.round(amount * 100f / monthTotal);

            // 카테고리 이름은 3개까지만 (막대 그래프에는 전부 표시)
            if (legendCount < 3) {
                legendContainer.addView(createLegendRow(category, percent));
                legendCount++;
            }
            addRatioSegment(category.getColorRes(), amount, !first);
            first = false;
        }
    }

    private View createLegendRow(CalendarCategory category, int percent) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = dp(2);
        row.setLayoutParams(rowParams);

        row.addView(createDot(category, 6, 0));

        TextView name = new TextView(requireContext());
        name.setTextAppearance(R.style.typo_cap_r1);
        name.setTextColor(color(R.color.gray600));
        name.setText(category.getLabel());
        name.setMinWidth(dp(36));
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nameParams.leftMargin = dp(6);
        row.addView(name, nameParams);

        TextView percentText = new TextView(requireContext());
        percentText.setTextAppearance(R.style.typo_cap_r1);
        percentText.setTextColor(color(R.color.gray800));
        percentText.setText(percent + "%");
        percentText.setGravity(Gravity.END);
        percentText.setMinWidth(dp(32));
        row.addView(percentText);

        return row;
    }

    private void addRatioSegment(int colorRes, int weight, boolean withGap) {
        View segment = new View(requireContext());
        segment.setBackgroundResource(R.drawable.bg_calendar_bar_segment);
        segment.setBackgroundTintList(ColorStateList.valueOf(color(colorRes)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, weight);
        if (withGap) params.leftMargin = dp(2);
        ratioBar.addView(segment, params);
    }

    private void renderSelectedDay(CalendarUiState state) {
        int day = state.getSelectedDay();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(state.getYear(), state.getMonth(), day);
        String dayOfWeek = DAY_OF_WEEK_NAMES[calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY];

        selectedDateText.setText((state.getMonth() + 1) + "월 " + day + "일 " + dayOfWeek);
        selectedTotalText.setText(formatPrice(state.getTotalOf(day)));

        paymentContainer.removeAllViews();
        List<CalendarPayment> payments = state.getPaymentsOf(day);
        paymentEmptyText.setVisibility(payments.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (CalendarPayment payment : payments) {
            View item = inflater.inflate(R.layout.calendar_payment_item, paymentContainer, false);

            TextView logo = item.findViewById(R.id.tv_calendar_payment_logo);
            logo.setText(firstLetter(payment.getName()));
            logo.setTextColor(color(payment.getCategory().getTextColorRes()));
            logo.setBackgroundTintList(ColorStateList.valueOf(color(payment.getCategory().getColorRes())));

            ((TextView) item.findViewById(R.id.tv_calendar_payment_name)).setText(payment.getName());

            ((TextView) item.findViewById(R.id.tv_calendar_payment_category)).setText(
                    payment.getCategory().getLabel() + " · " + payment.getBillingType().getLabel());

            TextView price = item.findViewById(R.id.tv_calendar_payment_price);
            if (payment.isFreeTrial()) {
                price.setText("무료체험");
                price.setTextColor(color(R.color.pri700));
            } else {
                price.setText(formatPrice(payment.getPrice()));
                price.setTextColor(color(R.color.appblack));
            }

            paymentContainer.addView(item);
        }
    }

    private TextView createDayBadge(String text, int backgroundColorRes, int textColorRes) {
        TextView badge = new TextView(requireContext());
        badge.setText(text);
        badge.setGravity(Gravity.CENTER);
        badge.setIncludeFontPadding(false);
        badge.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold));
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
        badge.setTextColor(color(textColorRes));
        badge.setBackgroundResource(R.drawable.bg_calendar_day_logo);
        badge.setBackgroundTintList(ColorStateList.valueOf(color(backgroundColorRes)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(18), dp(18));
        params.leftMargin = dp(1);
        params.rightMargin = dp(1);
        badge.setLayoutParams(params);
        return badge;
    }

    private static String firstLetter(String name) {
        return name.isEmpty() ? "" : name.substring(0, 1).toUpperCase(Locale.ROOT);
    }

    private View createDot(CalendarCategory category, int sizeDp, int horizontalMarginDp) {
        View dot = new View(requireContext());
        dot.setBackgroundResource(R.drawable.bg_calendar_category_dot);
        dot.setBackgroundTintList(ColorStateList.valueOf(color(category.getColorRes())));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp));
        params.leftMargin = dp(horizontalMarginDp);
        params.rightMargin = dp(horizontalMarginDp);
        dot.setLayoutParams(params);
        return dot;
    }

    private String formatPrice(int price) {
        return "₩" + priceFormat.format(price);
    }

    private int color(int colorRes) {
        return ContextCompat.getColor(requireContext(), colorRes);
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }

}
