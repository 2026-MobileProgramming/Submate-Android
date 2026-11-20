package com.example.submate.presentation.report.data;

import android.content.Context;

import com.example.submate.R;
import com.example.submate.presentation.report.model.UsageReportItem;
import com.example.submate.presentation.report.model.ExpenseReportItem;
import com.example.submate.presentation.report.model.AnnualExpenseData;
import com.example.submate.presentation.report.model.AllExpenseData;

import java.util.ArrayList;
import java.util.List;

public class ReportDummyData {

    // 월별 사용량 임시 데이터
    public static List<UsageReportItem> getUsageReportData(Context context) {

        List<UsageReportItem> usageList = new ArrayList<>();

        usageList.add(new UsageReportItem(
                "넷플릭스",
                "넷",
                "18시간",
                100,
                context.getResources().getColor(R.color.pastel_red),
                context.getResources().getColor(R.color.pastel_red)
        ));

        usageList.add(new UsageReportItem(
                "Gemini",
                "G",
                "7시간",
                39,
                context.getResources().getColor(R.color.pastel_blue),
                context.getResources().getColor(R.color.pastel_blue)
        ));

        usageList.add(new UsageReportItem(
                "라프텔",
                "라",
                "1시간",
                6,
                context.getResources().getColor(R.color.pastel_red),
                context.getResources().getColor(R.color.pastel_red)
        ));

        return usageList;
    }


    // 월별 지출 임시 데이터
    public static List<ExpenseReportItem> getExpenseReportData(Context context) {

        List<ExpenseReportItem> expenseList = new ArrayList<>();

        expenseList.add(new ExpenseReportItem(
                "Claude",
                "C",
                5000,
                100,
                context.getResources().getColor(R.color.pastel_blue),
                context.getResources().getColor(R.color.pastel_blue)
        ));

        expenseList.add(new ExpenseReportItem(
                "Gemini",
                "G",
                4881,
                98,
                context.getResources().getColor(R.color.pastel_blue),
                context.getResources().getColor(R.color.pastel_blue)
        ));

        expenseList.add(new ExpenseReportItem(
                "넷플릭스",
                "넷",
                4269,
                85,
                context.getResources().getColor(R.color.pastel_red),
                context.getResources().getColor(R.color.pastel_red)
        ));

        expenseList.add(new ExpenseReportItem(
                "라프텔",
                "라",
                3725,
                75,
                context.getResources().getColor(R.color.pastel_red),
                context.getResources().getColor(R.color.pastel_red)
        ));

        expenseList.add(new ExpenseReportItem(
                "배달의민족",
                "배",
                1500,
                30,
                context.getResources().getColor(R.color.pastel_yellow),
                context.getResources().getColor(R.color.pastel_yellow)
        ));

        return expenseList;
    }

    // 연간 지출 임시 데이터
    public static AnnualExpenseData getAnnualExpenseData() {

        List<AnnualExpenseData.MonthlyExpense> monthlyList =
                new ArrayList<>();

        // 월별 임시 지출 데이터
        monthlyList.add(new AnnualExpenseData.MonthlyExpense(1, 10000, 8000, 2000));
        monthlyList.add(new AnnualExpenseData.MonthlyExpense(2, 12000, 8000, 1000));
        monthlyList.add(new AnnualExpenseData.MonthlyExpense(3, 11000, 9000, 2000));
        monthlyList.add(new AnnualExpenseData.MonthlyExpense(4, 13000, 9000, 1500));
        monthlyList.add(new AnnualExpenseData.MonthlyExpense(5, 14000, 10000, 2000));
        monthlyList.add(new AnnualExpenseData.MonthlyExpense(6, 15000, 10000, 2500));
        monthlyList.add(new AnnualExpenseData.MonthlyExpense(7, 16000, 11000, 2000));
        monthlyList.add(new AnnualExpenseData.MonthlyExpense(8, 15000, 10000, 2000));
        monthlyList.add(new AnnualExpenseData.MonthlyExpense(9, 14000, 10000, 1500));
        monthlyList.add(new AnnualExpenseData.MonthlyExpense(10, 12000, 9000, 1500));
        monthlyList.add(new AnnualExpenseData.MonthlyExpense(11, 0, 0, 0));
        monthlyList.add(new AnnualExpenseData.MonthlyExpense(12, 0, 0, 0));

        // 카테고리별 지출 계산
        int ottTotal = 0;
        int aiTotal = 0;
        int foodTotal = 0;

        for (AnnualExpenseData.MonthlyExpense item : monthlyList) {
            ottTotal += item.getOttExpense();
            aiTotal += item.getAiExpense();
            foodTotal += item.getFoodExpense();
        }

        List<AnnualExpenseData.CategoryExpense> categoryList =
                new ArrayList<>();

        categoryList.add(
                new AnnualExpenseData.CategoryExpense("OTT", ottTotal)
        );

        categoryList.add(
                new AnnualExpenseData.CategoryExpense("AI", aiTotal)
        );

        categoryList.add(
                new AnnualExpenseData.CategoryExpense("음식", foodTotal)
        );

        int totalExpense = ottTotal + aiTotal + foodTotal;

        return new AnnualExpenseData(
                totalExpense,
                monthlyList,
                categoryList
        );
    }
    // 전체 누적 지출 리포트 임시 데이터
    public static AllExpenseData getAllExpenseData() {

        // 연도별 지출
        List<AllExpenseData.YearlyExpense> yearlyList =
                new ArrayList<>();

        yearlyList.add(new AllExpenseData.YearlyExpense(2024, 90000));
        yearlyList.add(new AllExpenseData.YearlyExpense(2025, 145000));
        yearlyList.add(new AllExpenseData.YearlyExpense(2026, 177300));

        // 카테고리별 누적 지출
        List<AllExpenseData.CategoryExpense> categoryList =
                new ArrayList<>();

        categoryList.add(
                new AllExpenseData.CategoryExpense("OTT", 197900)
        );

        categoryList.add(
                new AllExpenseData.CategoryExpense("AI", 181400)
        );

        categoryList.add(
                new AllExpenseData.CategoryExpense("음식", 33000)
        );

        // 전체 누적 지출 데이터 반환
        return new AllExpenseData(
                412300,
                6,
                yearlyList,
                categoryList
        );
    }

}
