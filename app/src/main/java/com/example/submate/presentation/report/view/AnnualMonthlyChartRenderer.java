
package com.example.submate.presentation.report.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.submate.R;
import com.example.submate.domain.model.SubscribeCategory;
import com.example.submate.presentation.report.model.AnnualExpenseData;

import java.util.List;

public class AnnualMonthlyChartRenderer {

    // 기존 3개 카테고리 호출과의 호환성을 위한 메서드
    public static void render(
            Context context,
            LinearLayout chartLayout,
            List<AnnualExpenseData.MonthlyExpense> monthlyList,
            int colorOtt,
            int colorAi,
            int colorFood
    ) {
        int gray = context.getResources().getColor(R.color.gray500);

        int[] categoryColors = {
                colorOtt,
                gray,
                colorFood,
                gray,
                colorAi,
                gray,
                gray
        };

        render(context, chartLayout, monthlyList, categoryColors);
    }

    // 7개 카테고리를 사용하는 연간 월별 지출 그래프
    public static void render(
            Context context,
            LinearLayout chartLayout,
            List<AnnualExpenseData.MonthlyExpense> monthlyList,
            int[] categoryColors
    ) {

        chartLayout.removeAllViews();

        if (monthlyList == null || monthlyList.isEmpty()) {
            return;
        }

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

                // 막대 전체 모서리 둥글게
                GradientDrawable stackBackground = new GradientDrawable();
                stackBackground.setColor(Color.TRANSPARENT);
                stackBackground.setCornerRadius(dpToPx(context, 10));

                stack.setBackground(stackBackground);
                stack.setClipToOutline(true);

                // 카테고리별 금액과 색상
                int[] categoryAmounts = {
                        item.getOttExpense(),
                        item.getMusicExpense(),
                        item.getFoodExpense(),
                        item.getShoppingExpense(),
                        item.getAiExpense(),
                        item.getDigitalContentExpense(),
                        item.getEtcExpense()
                };

                // 7개 카테고리 막대 조각 생성
                for (int i = 0; i < categoryAmounts.length; i++) {

                    if (i >= categoryColors.length) {
                        break;
                    }

                    addChartSegment(
                            context,
                            stack,
                            categoryAmounts[i],
                            total,
                            categoryColors[i]
                    );
                }

                bar.addView(stack);

                // 막대를 누르면 해당 월의 지출 금액 표시
                stack.setOnClickListener(v -> {
                    showExpensePopup(
                            context,
                            stack,
                            item.getTotalExpense()
                    );
                });
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

        GradientDrawable segmentBackground = new GradientDrawable();
        segmentBackground.setColor(color);
        segmentBackground.setCornerRadius(0);

        segment.setBackground(segmentBackground);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        height
                );

        segment.setLayoutParams(params);

        stack.addView(segment);
    }

    // 선택한 월의 지출 금액 팝업 표시
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

        // 팝업 배경 둥글게
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
        popupWindow.setBackgroundDrawable(
                new GradientDrawable()
        );

        // 팝업 크기 측정
        popupText.measure(
                View.MeasureSpec.makeMeasureSpec(
                        0, View.MeasureSpec.UNSPECIFIED
                ),
                View.MeasureSpec.makeMeasureSpec(
                        0, View.MeasureSpec.UNSPECIFIED
                )
        );

        // 팝업을 막대의 정중앙에 맞춰 표시
        popupWindow.showAsDropDown(
                anchor,
                (anchor.getWidth() - popupText.getMeasuredWidth()) / 2,
                -anchor.getHeight() - dpToPx(context, 45)
        );

        // 약 1.5초 후 자동으로 닫기
        popupText.postDelayed(() -> {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        }, 1500);
    }

    // dp를 px로 변환
    private static int dpToPx(Context context, float dp) {
        return (int) (
                dp * context.getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }
}
