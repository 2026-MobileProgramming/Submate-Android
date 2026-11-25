
package com.example.submate.presentation.report.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.example.submate.R;
import com.example.submate.presentation.report.ReportCategoryColors;
import com.example.submate.presentation.report.model.AnnualExpenseData;

import java.util.List;

public class AnnualExpenseDonutView extends View {

    private final Paint chartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<AnnualExpenseData.CategoryExpense> categoryList;

    public AnnualExpenseDonutView(Context context) {
        super(context);
        init();
    }

    public AnnualExpenseDonutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnnualExpenseDonutView(
            Context context,
            AttributeSet attrs,
            int defStyleAttr
    ) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        chartPaint.setStyle(Paint.Style.STROKE);
        chartPaint.setStrokeCap(Paint.Cap.BUTT);

        textPaint.setColor(Color.rgb(160, 160, 170));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        10,
                        getResources().getDisplayMetrics()
                )
        );
    }

    // 카테고리별 지출 데이터 전달
    public void setData(
            List<AnnualExpenseData.CategoryExpense> categoryList
    ) {
        this.categoryList = categoryList;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (categoryList == null || categoryList.isEmpty()) {
            return;
        }

        float width = getWidth();
        float height = getHeight();

        float centerX = width / 2f;
        float centerY = height / 2f;

        float strokeWidth = dpToPx(13);
        float radius = Math.min(width, height) / 2f - strokeWidth;

        RectF rect = new RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        int totalExpense = 0;

        for (AnnualExpenseData.CategoryExpense item : categoryList) {
            totalExpense += item.getAmount();
        }

        if (totalExpense <= 0) {
            return;
        }

        float startAngle = -90f;

        // 카테고리별 비율만큼 원형 그래프 그리기
        for (int i = 0; i < categoryList.size(); i++) {

            int amount = categoryList.get(i).getAmount();

            if (amount <= 0) {
                continue;
            }

            float sweepAngle = amount * 360f / totalExpense;

            chartPaint.setColor(
                    ReportCategoryColors.getColor(getContext(), categoryList.get(i).getCategory())
            );
            chartPaint.setStrokeWidth(strokeWidth);

            canvas.drawArc(
                    rect,
                    startAngle,
                    sweepAngle,
                    false,
                    chartPaint
            );

            startAngle += sweepAngle;
        }

        // 도넛 중앙 텍스트
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();

        float textY = centerY
                - (fontMetrics.ascent + fontMetrics.descent) / 2f;

        canvas.drawText("비율", centerX, textY, textPaint);
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}
