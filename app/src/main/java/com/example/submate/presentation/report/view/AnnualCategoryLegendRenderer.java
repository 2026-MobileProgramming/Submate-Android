package com.example.submate.presentation.report.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.submate.R;
import com.example.submate.presentation.report.model.AnnualExpenseData;

import java.util.List;
import java.util.Locale;

public class AnnualCategoryLegendRenderer {

    public static void render(
            Context context,
            LinearLayout legendLayout,
            List<AnnualExpenseData.CategoryExpense> categoryList,
            int totalExpense,
            int colorOtt,
            int colorAi,
            int colorFood
    ) {
        // 기존 목록 초기화
        legendLayout.removeAllViews();

        // 카테고리별 색상
        int[] categoryColors = {
                colorOtt,
                colorAi,
                colorFood
        };

        // 카테고리별 목록 생성
        for (int i = 0; i < categoryList.size(); i++) {

            AnnualExpenseData.CategoryExpense item =
                    categoryList.get(i);

            int amount = item.getAmount();

            int percent = totalExpense > 0
                    ? Math.round(amount * 100f / totalExpense)
                    : 0;

            // 한 줄을 구성하는 레이아웃
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams rowParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            dpToPx(context, 24)
                    );

            rowParams.bottomMargin = dpToPx(context, 4);
            row.setLayoutParams(rowParams);

            // 색상 원형 표시
            View colorDot = new View(context);

            GradientDrawable dotBackground = new GradientDrawable();
            dotBackground.setShape(GradientDrawable.OVAL);
            dotBackground.setColor(
                    categoryColors[i % categoryColors.length]
            );

            colorDot.setBackground(dotBackground);

            LinearLayout.LayoutParams dotParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(context, 7),
                            dpToPx(context, 7)
                    );

            dotParams.rightMargin = dpToPx(context, 8);
            row.addView(colorDot, dotParams);

            // 카테고리 이름
            TextView nameText = new TextView(context);
            nameText.setText(item.getCategoryName());
            nameText.setTextSize(10);
            nameText.setTextColor(
                    context.getResources().getColor(R.color.appblack)
            );

            LinearLayout.LayoutParams nameParams =
                    new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1
                    );

            row.addView(nameText, nameParams);

            // 지출 비율
            TextView percentText = new TextView(context);
            percentText.setText(percent + "%");
            percentText.setTextSize(10);
            percentText.setTextColor(
                    context.getResources().getColor(R.color.gray500)
            );

            LinearLayout.LayoutParams percentParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(context, 36),
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            row.addView(percentText, percentParams);

            // 지출 금액
            TextView amountText = new TextView(context);

            amountText.setText(
                    String.format(Locale.KOREA, "₩%,d", amount)
            );

            amountText.setTextSize(10);
            amountText.setTextColor(
                    context.getResources().getColor(R.color.appblack)
            );
            amountText.setGravity(Gravity.END);

            LinearLayout.LayoutParams amountParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(context, 65),
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            row.addView(amountText, amountParams);

            // 완성된 한 줄을 목록에 추가
            legendLayout.addView(row);
        }
    }

    // dp를 px로 변환
    private static int dpToPx(Context context, float dp) {
        return (int) (
                dp * context.getResources().getDisplayMetrics().density + 0.5f
        );
    }
}
