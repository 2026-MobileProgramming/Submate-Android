
package com.example.submate.presentation.report.model;

public class ExpenseReportItem {

    // 서비스 정보
    private String serviceName;
    private String iconText;

    // 구독 금액
    private int price;

    // 지출 비율
    private int expensePercent;

    // 아이콘 및 프로그레스바 색상
    private int iconColor;
    private int progressColor;

    // 생성자
    public ExpenseReportItem(
            String serviceName,
            String iconText,
            int price,
            int expensePercent,
            int iconColor,
            int progressColor
    ) {
        this.serviceName = serviceName;
        this.iconText = iconText;
        this.price = price;
        this.expensePercent = expensePercent;
        this.iconColor = iconColor;
        this.progressColor = progressColor;
    }

    // Getter
    public String getServiceName() {
        return serviceName;
    }

    public String getIconText() {
        return iconText;
    }

    public int getPrice() {
        return price;
    }

    public int getExpensePercent() {
        return expensePercent;
    }

    public int getIconColor() {
        return iconColor;
    }

    public int getProgressColor() {
        return progressColor;
    }
}
