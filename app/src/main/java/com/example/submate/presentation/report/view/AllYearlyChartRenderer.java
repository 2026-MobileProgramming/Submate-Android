package com.example.submate.presentation.report.view;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.submate.R;
import com.example.submate.presentation.report.model.AllExpenseData;

import java.util.List;

public class AllYearlyChartRenderer {

    public static void render(
            Context context,
            LinearLayout chartLayout,
            List<AllExpenseData.YearlyExpense> yearlyList
    ) {

        chartLayout.removeAllViews();

        int maxExpense = 0;

        for (AllExpenseData.YearlyExpense item : yearlyList) {
            maxExpense = Math.max(maxExpense, item.getAmount());
        }

        if (maxExpense <= 0) {
            return;
        }

        int[] barColors = {
                context.getResources().getColor(R.color.pri200),
                context.getResources().getColor(R.color.pri400),
                context.getResources().getColor(R.color.pri700)
        };

        for (int i = 0; i < yearlyList.size(); i++) {

            AllExpenseData.YearlyExpense item = yearlyList.get(i);

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
                            item.getAmount() / (float) maxExpense
                                    * dpToPx(context, 90)
                    )
            );

            View bar = new View(context);

            GradientDrawable barBackground = new GradientDrawable();
            barBackground.setColor(barColors[i % barColors.length]);
            barBackground.setCornerRadius(dpToPx(context, 6));

            bar.setBackground(barBackground);

            LinearLayout.LayoutParams barParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(context, 38),
                            barHeight
                    );

            barArea.addView(bar, barParams);

            // 연도 표시
            TextView yearText = new TextView(context);
            yearText.setText(String.valueOf(item.getYear()));
            yearText.setTextSize(10);

            yearText.setTextColor(
                    context.getResources().getColor(
                            i == yearlyList.size() - 1
                                    ? R.color.pri700
                                    : R.color.gray400
                    )
            );

            yearText.setGravity(Gravity.CENTER);

            yearText.setTypeface(
                    null,
                    i == yearlyList.size() - 1
                            ? Typeface.BOLD
                            : Typeface.NORMAL
            );

            LinearLayout.LayoutParams yearParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            dpToPx(context, 22)
                    );

            column.addView(yearText, yearParams);

            chartLayout.addView(column);
        }
    }

    private static int dpToPx(Context context, float dp) {
        return (int) (
                dp * context.getResources().getDisplayMetrics().density + 0.5f
        );
    }
}
