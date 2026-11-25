package com.example.submate.presentation.report.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.submate.R;
import com.google.android.material.textview.MaterialTextView;
import com.example.submate.presentation.report.ReportCategoryColors;
import com.example.submate.presentation.report.model.AllExpenseData;

import java.util.List;
import java.util.Locale;

public class AllCategoryChartRenderer {

    public static void render(
            Context context,
            LinearLayout categoryBar,
            LinearLayout categoryListLayout,
            List<AllExpenseData.CategoryExpense> categoryList,
            int totalExpense
    ) {

        categoryBar.removeAllViews();
        categoryListLayout.removeAllViews();

        if (totalExpense <= 0) {
            return;
        }

        for (int i = 0; i < categoryList.size(); i++) {

            AllExpenseData.CategoryExpense item = categoryList.get(i);

            int amount = item.getAmount();

            if (amount <= 0) {
                continue;
            }

            int percent = Math.round(amount * 100f / totalExpense);

            // 1. 위쪽 비율 막대
            View segment = new View(context);

            segment.setBackgroundResource(R.drawable.bg_report_category_bar_segment);
            segment.setBackgroundTintList(ColorStateList.valueOf(
                    ReportCategoryColors.getColor(context, item.getCategory())));

            LinearLayout.LayoutParams segmentParams =
                    new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            amount
                    );

            if (categoryBar.getChildCount() > 0) {
                segmentParams.setMarginStart(context.getResources().getDimensionPixelSize(
                        R.dimen.report_category_bar_segment_gap));
            }

            categoryBar.addView(segment, segmentParams);

            // 2. 카테고리별 목록 한 줄
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams rowParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            dpToPx(context, 38)
                    );

            row.setLayoutParams(rowParams);

            // 카테고리 색상 표시
            TextView categoryBadge = new MaterialTextView(context);
            categoryBadge.setText(item.getCategoryName());
            categoryBadge.setTextAppearance(R.style.ReportText_Caption);
            categoryBadge.setTextColor(
                    item.getCategoryName().equals("OTT")
                            ? Color.WHITE
                            : Color.BLACK
            );
            categoryBadge.setGravity(Gravity.CENTER);

            GradientDrawable badgeBackground = new GradientDrawable();
            badgeBackground.setColor(
                    ReportCategoryColors.getColor(context, item.getCategory())
            );
            badgeBackground.setCornerRadius(dpToPx(context, 4));

            categoryBadge.setBackground(badgeBackground);

            LinearLayout.LayoutParams badgeParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(context, 34),
                            dpToPx(context, 30)
                    );

            row.addView(categoryBadge, badgeParams);

            // 카테고리 이름
            TextView nameText = new MaterialTextView(context);
            nameText.setText(item.getCategoryName());
            nameText.setTextAppearance(R.style.ReportText_Body4);
            nameText.setTextColor(
                    context.getResources().getColor(R.color.appblack)
            );

            LinearLayout.LayoutParams nameParams =
                    new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1
                    );

            nameParams.setMarginStart(dpToPx(context, 8));
            row.addView(nameText, nameParams);

            // 지출 비율
            TextView percentText = new MaterialTextView(context);
            percentText.setText(percent + "%");
            percentText.setTextAppearance(R.style.ReportText_Caption);
            percentText.setTextColor(
                    context.getResources().getColor(R.color.gray500)
            );

            LinearLayout.LayoutParams percentParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(context, 40),
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            row.addView(percentText, percentParams);

            // 지출 금액
            TextView amountText = new MaterialTextView(context);
            amountText.setText(
                    String.format(Locale.KOREA, "₩%,d", amount)
            );
            amountText.setTextAppearance(R.style.ReportText_Caption);
            amountText.setTextColor(
                    context.getResources().getColor(R.color.appblack)
            );
            amountText.setGravity(Gravity.END);

            LinearLayout.LayoutParams amountParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(context, 75),
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            row.addView(amountText, amountParams);

            categoryListLayout.addView(row);
        }
    }

    private static int dpToPx(Context context, float dp) {
        return (int) (
                dp * context.getResources().getDisplayMetrics().density + 0.5f
        );
    }
}
