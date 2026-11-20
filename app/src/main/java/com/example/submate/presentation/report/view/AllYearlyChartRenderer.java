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
            // 막대 클릭 시 지출 금액 팝업 표시
            bar.setOnClickListener(v -> {
                showExpensePopup(
                        context,
                        bar,
                        item.getAmount()
                );
            });

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

    // 지출 금액 팝업
    private static void showExpensePopup(
            Context context,
            View anchor,
            int expense
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
