package com.example.submate.presentation.report.data;

import android.content.Context;

import com.example.submate.R;
import com.example.submate.presentation.report.model.UsageReportItem;
import com.example.submate.presentation.report.model.ExpenseReportItem;

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
}
