package com.example.submate.presentation.report.model;

import com.example.submate.domain.model.SubscribeCategory;

import java.util.List;

public class AllExpenseData {

    // 전체 누적 지출 금액
    private final int totalExpense;

    // 구독 서비스 개수
    private final int serviceCount;

    // 연도별 지출 목록
    private final List<YearlyExpense> yearlyExpenses;

    // 카테고리별 누적 지출 목록
    private final List<CategoryExpense> categoryExpenses;

    public AllExpenseData(
            int totalExpense,
            int serviceCount,
            List<YearlyExpense> yearlyExpenses,
            List<CategoryExpense> categoryExpenses
    ) {
        this.totalExpense = totalExpense;
        this.serviceCount = serviceCount;
        this.yearlyExpenses = yearlyExpenses;
        this.categoryExpenses = categoryExpenses;
    }

    public int getTotalExpense() {
        return totalExpense;
    }

    public int getServiceCount() {
        return serviceCount;
    }

    public List<YearlyExpense> getYearlyExpenses() {
        return yearlyExpenses;
    }

    public List<CategoryExpense> getCategoryExpenses() {
        return categoryExpenses;
    }

    // 연도별 지출 데이터
    public static class YearlyExpense {

        private final int year;
        private final long amount;

        public YearlyExpense(int year, long amount) {
            this.year = year;
            this.amount = amount;
        }

        public int getYear() {
            return year;
        }

        public long getAmount() {
            return amount;
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
