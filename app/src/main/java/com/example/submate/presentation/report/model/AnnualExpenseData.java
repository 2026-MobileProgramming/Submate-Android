package com.example.submate.presentation.report.model;

import com.example.submate.domain.model.SubscribeCategory;

import java.util.List;

public class AnnualExpenseData {

    private final int totalExpense;
    private final List<MonthlyExpense> monthlyExpenses;
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
        private final int musicExpense;
        private final int foodExpense;
        private final int shoppingExpense;
        private final int aiExpense;
        private final int digitalContentExpense;
        private final int etcExpense;

        // 기존 더미 데이터와의 호환성을 위한 생성자
        public MonthlyExpense(
                int month,
                int ottExpense,
                int aiExpense,
                int foodExpense
        ) {
            this(month, ottExpense, 0, foodExpense, 0, aiExpense, 0, 0);
        }

        // 7개 카테고리를 사용하는 생성자
        public MonthlyExpense(
                int month,
                int ottExpense,
                int musicExpense,
                int foodExpense,
                int shoppingExpense,
                int aiExpense,
                int digitalContentExpense,
                int etcExpense
        ) {
            this.month = month;
            this.ottExpense = ottExpense;
            this.musicExpense = musicExpense;
            this.foodExpense = foodExpense;
            this.shoppingExpense = shoppingExpense;
            this.aiExpense = aiExpense;
            this.digitalContentExpense = digitalContentExpense;
            this.etcExpense = etcExpense;
        }

        public int getMonth() {
            return month;
        }

        public int getExpense(SubscribeCategory category) {
            switch (category) {
                case OTT: return ottExpense;
                case MUSIC: return musicExpense;
                case FOOD: return foodExpense;
                case SHOPPING: return shoppingExpense;
                case AI: return aiExpense;
                case DIGITAL_CONTENT: return digitalContentExpense;
                case ETC: return etcExpense;
            }
            throw new IllegalArgumentException("Unsupported category: " + category);
        }

        public int getOttExpense() {
            return ottExpense;
        }

        public int getMusicExpense() {
            return musicExpense;
        }

        public int getFoodExpense() {
            return foodExpense;
        }

        public int getShoppingExpense() {
            return shoppingExpense;
        }

        public int getAiExpense() {
            return aiExpense;
        }

        public int getDigitalContentExpense() {
            return digitalContentExpense;
        }

        public int getEtcExpense() {
            return etcExpense;
        }

        public int getTotalExpense() {
            return ottExpense
                    + musicExpense
                    + foodExpense
                    + shoppingExpense
                    + aiExpense
                    + digitalContentExpense
                    + etcExpense;
        }
    }

    // 카테고리별 지출 데이터
    public static class CategoryExpense {

        private final SubscribeCategory category;
        private final int amount;

        public CategoryExpense(SubscribeCategory category, int amount) {
            this.category = category;
            this.amount = amount;
        }

        public SubscribeCategory getCategory() {
            return category;
        }

        public String getCategoryName() {
            return category.getLabel();
        }

        public int getAmount() {
            return amount;
        }
    }
}
