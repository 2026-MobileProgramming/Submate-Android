package com.example.submate.presentation.report.view;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.PopupWindow;

import com.example.submate.R;
import com.example.submate.presentation.report.model.AllExpenseData;

import java.util.List;

public class AllYearlyChartRenderer {

    public static void render(
            Context context,
            LinearLayout chartLayout,
            List<AllExpenseData.YearlyExpense> yearlyList,
            int currentYear,
            long projectedExpense
    ) {

        chartLayout.removeAllViews();

        long maxExpense = Math.max(1, projectedExpense);

        for (AllExpenseData.YearlyExpense item : yearlyList) {
            maxExpense = Math.max(maxExpense, item.getAmount());
        }

        int[] barColors = {
                context.getResources().getColor(R.color.pri200),
                context.getResources().getColor(R.color.pri400),
                context.getResources().getColor(R.color.pri700)
        };

        for (int i = 0; i < yearlyList.size(); i++) {
            AllExpenseData.YearlyExpense item = yearlyList.get(i);
            boolean isCurrentYear = item.getYear() == currentYear;
            String label = String.valueOf(item.getYear());
            int barColor = isCurrentYear ? barColors[2] : barColors[i % barColors.length];
            int labelColor = context.getColor(isCurrentYear ? R.color.pri700 : R.color.gray600);
            addColumn(context, chartLayout, label, item.getAmount(), maxExpense,
                    barColor, labelColor, isCurrentYear);
        }

        int projectionColor = context.getColor(R.color.pri200);
        addColumn(context, chartLayout, context.getString(R.string.report_year_projection, currentYear),
                projectedExpense, maxExpense, projectionColor, projectionColor, true);
    }

    private static void addColumn(
            Context context,
            LinearLayout chartLayout,
            String label,
            long amount,
            long maxExpense,
            int barColor,
            int labelColor,
            boolean boldLabel
    ) {

        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

        LinearLayout.LayoutParams columnParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1
                );

        column.setLayoutParams(columnParams);

        // 막대그래프 영역
        LinearLayout barArea = new LinearLayout(context);
        barArea.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

        LinearLayout.LayoutParams barAreaParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(context, 90)
                );

        column.addView(barArea, barAreaParams);

        // 지출 금액에 비례한 막대 높이
        int barHeight = Math.max(
                dpToPx(context, 8),
                Math.round(
                        amount / (float) maxExpense
                                * dpToPx(context, 90)
                )
        );

        View bar = new View(context);

        GradientDrawable barBackground = new GradientDrawable();
        barBackground.setColor(barColor);
        barBackground.setCornerRadius(dpToPx(context, 6));

        bar.setBackground(barBackground);

        LinearLayout.LayoutParams barParams =
                new LinearLayout.LayoutParams(
                        dpToPx(context, 38),
                        barHeight
                );

        barArea.addView(bar, barParams);
        // 막대 클릭 시 지출 금액 팝업 표시
        bar.setContentDescription(
                context.getString(R.string.report_year_expense_description, label, amount)
        );
        bar.setOnClickListener(v -> showExpensePopup(context, bar, amount));

        // 연도 표시
        TextView yearText = new TextView(context);
        yearText.setText(label);
        yearText.setTextSize(10);

        yearText.setTextColor(labelColor);

        yearText.setGravity(Gravity.CENTER);

        yearText.setTypeface(
                null,
                boldLabel ? Typeface.BOLD : Typeface.NORMAL
        );

        LinearLayout.LayoutParams yearParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(context, 30)
                );

        column.addView(yearText, yearParams);

        chartLayout.addView(column);
    }

    // 지출 금액 팝업
    private static void showExpensePopup(
            Context context,
            View anchor,
            long expense
    ) {
        TextView popupText = new TextView(context);

        popupText.setText(
                String.format(java.util.Locale.KOREA, "₩%,d", expense)
        );
        popupText.setTextColor(Color.WHITE);
        popupText.setTextSize(12);
        popupText.setTypeface(null, Typeface.BOLD);
        popupText.setGravity(Gravity.CENTER);

        popupText.setPadding(
                dpToPx(context, 12),
                dpToPx(context, 7),
                dpToPx(context, 12),
                dpToPx(context, 7)
        );

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(45, 45, 50));
        background.setCornerRadius(dpToPx(context, 10));
        popupText.setBackground(background);

        PopupWindow popupWindow = new PopupWindow(
                popupText,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                false
        );

        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new GradientDrawable());

        // 팝업 크기 측정
        popupText.measure(
                View.MeasureSpec.makeMeasureSpec(
                        0, View.MeasureSpec.UNSPECIFIED
                ),
                View.MeasureSpec.makeMeasureSpec(
                        0, View.MeasureSpec.UNSPECIFIED
                )
        );

        // 막대 중앙에 팝업 표시
        popupWindow.showAsDropDown(
                anchor,
                (anchor.getWidth() - popupText.getMeasuredWidth()) / 2,
                -anchor.getHeight() - dpToPx(context, 45)
        );

        // 1.5초 후 자동으로 닫기
        popupText.postDelayed(() -> {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        }, 1500);
    }

    private static int dpToPx(Context context, float dp) {
        return (int) (
                dp * context.getResources().getDisplayMetrics().density + 0.5f
        );
    }
}
