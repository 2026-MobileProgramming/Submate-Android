
package com.example.submate.presentation.report.model;

import java.util.List;

public class AnnualExpenseData {

    // 연간 총 지출
    private final int totalExpense;

    // 월별 지출 금액 (1월~12월)
    private final List<MonthlyExpense> monthlyExpenses;

    // 카테고리별 연간 지출
    private final List<CategoryExpense> categoryExpenses;

    public AnnualExpenseData(
            int totalExpense,
            List<MonthlyExpense> monthlyExpenses,
            List<CategoryExpense> categoryExpenses
    ) {
        this.totalExpense = totalExpense;
        this.monthlyExpenses = monthlyExpenses;
        this.categoryExpenses = categoryExpenses;
    }

    public int getTotalExpense() {
        return totalExpense;
    }

    public List<MonthlyExpense> getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public List<CategoryExpense> getCategoryExpenses() {
        return categoryExpenses;
    }

    // 월별 지출 데이터
    public static class MonthlyExpense {

        private final int month;
        private final int ottExpense;
        private final int aiExpense;
        private final int foodExpense;

        public MonthlyExpense(
                int month,
                int ottExpense,
                int aiExpense,
                int foodExpense
        ) {
            this.month = month;
            this.ottExpense = ottExpense;
            this.aiExpense = aiExpense;
            this.foodExpense = foodExpense;
        }

        public int getMonth() {
            return month;
        }

        public int getOttExpense() {
            return ottExpense;
        }

        public int getAiExpense() {
            return aiExpense;
        }

        public int getFoodExpense() {
            return foodExpense;
        }

        public int getTotalExpense() {
            return ottExpense + aiExpense + foodExpense;
        }
    }

    // 카테고리별 지출 데이터
    public static class CategoryExpense {

        private final String categoryName;
        private final int amount;

        public CategoryExpense(String categoryName, int amount) {
            this.categoryName = categoryName;
            this.amount = amount;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public int getAmount() {
            return amount;
        }
    }
}
