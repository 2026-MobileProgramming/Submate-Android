package com.example.submate.presentation.report.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.submate.R;
import com.example.submate.presentation.report.model.AnnualExpenseData;

import java.util.List;

public class AnnualMonthlyChartRenderer {

    // 연간 월별 지출 그래프 생성
    public static void render(
            Context context,
            LinearLayout chartLayout,
            List<AnnualExpenseData.MonthlyExpense> monthlyList,
            int colorOtt,
            int colorAi,
            int colorFood
    ) {

        chartLayout.removeAllViews();

        // 그래프에서 가장 큰 월 지출 금액 찾기
        int maxExpense = 1;

        for (AnnualExpenseData.MonthlyExpense item : monthlyList) {
            maxExpense = Math.max(maxExpense, item.getTotalExpense());
        }

        // 월별 막대 생성
        for (AnnualExpenseData.MonthlyExpense item : monthlyList) {

            LinearLayout monthColumn = new LinearLayout(context);
            monthColumn.setOrientation(LinearLayout.VERTICAL);
            monthColumn.setGravity(
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
            );

            LinearLayout.LayoutParams columnParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(context, 34),
                            dpToPx(context, 150)
                    );

            columnParams.setMargins(
                    dpToPx(context, 5), 0,
                    dpToPx(context, 5), 0
            );

            monthColumn.setLayoutParams(columnParams);

            // 월별 막대 전체 영역
            LinearLayout bar = new LinearLayout(context);
            bar.setOrientation(LinearLayout.VERTICAL);
            bar.setGravity(Gravity.BOTTOM);

            LinearLayout.LayoutParams barParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(context, 22),
                            0,
                            1
                    );

            bar.setLayoutParams(barParams);

            int total = item.getTotalExpense();

            if (total > 0) {

                int barHeight = Math.max(
                        dpToPx(context, 10),
                        (int) (
                                dpToPx(context, 110)
                                        * total / (float) maxExpense
                        )
                );

                // 전체 막대 높이
                LinearLayout stack = new LinearLayout(context);
                stack.setOrientation(LinearLayout.VERTICAL);
                stack.setGravity(Gravity.BOTTOM);

                LinearLayout.LayoutParams stackParams =
                        new LinearLayout.LayoutParams(
                                dpToPx(context, 22),
                                barHeight
                        );

                stack.setLayoutParams(stackParams);

                // 카테고리별 막대 높이
                addChartSegment(
                        context, stack,
                        item.getOttExpense(), total, colorOtt
                );

                addChartSegment(
                        context, stack,
                        item.getAiExpense(), total, colorAi
                );

                addChartSegment(
                        context, stack,
                        item.getFoodExpense(), total, colorFood
                );

                bar.addView(stack);
            }

            monthColumn.addView(bar);

            // 월 표시
            TextView monthLabel = new TextView(context);
            monthLabel.setText(item.getMonth() + "월");
            monthLabel.setTextSize(8);
            monthLabel.setTextColor(
                    context.getResources().getColor(R.color.gray500)
            );
            monthLabel.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams labelParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            dpToPx(context, 20)
                    );

            monthLabel.setLayoutParams(labelParams);

            monthColumn.addView(monthLabel);

            chartLayout.addView(monthColumn);
        }
    }

    // 카테고리별 막대 조각 생성
    private static void addChartSegment(
            Context context,
            LinearLayout stack,
            int amount,
            int total,
            int color
    ) {

        if (amount <= 0) return;

        int height = Math.max(
                1,
                (int) (
                        amount / (float) total
                                * stack.getLayoutParams().height
                )
        );

        View segment = new View(context);

        segment.setBackgroundColor(color);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        height
                );

        segment.setLayoutParams(params);

        stack.addView(segment);
    }

    // dp를 px로 변환
    private static int dpToPx(Context context, float dp) {
        return (int) (
                dp * context.getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }
}
