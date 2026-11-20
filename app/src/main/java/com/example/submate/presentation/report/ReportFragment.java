package com.example.submate.presentation.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submate.presentation.report.adapter.UsageReportAdapter;
import com.example.submate.presentation.report.data.ReportDummyData;
import com.example.submate.presentation.report.model.UsageReportItem;
import com.example.submate.presentation.report.adapter.ExpenseReportAdapter;
import com.example.submate.presentation.report.model.ExpenseReportItem;
import com.example.submate.presentation.report.model.AnnualExpenseData;
import com.example.submate.presentation.report.view.AnnualExpenseDonutView;

import java.util.Locale;
import java.util.List;

import com.example.submate.R;

public class ReportFragment extends Fragment {

    private ReportViewModel viewModel;


    // 리포트 종류 버튼
    private TextView btnUsageReport;
    private TextView btnExpenseReport;

    private RecyclerView rvUsageReport;
    private UsageReportAdapter usageReportAdapter;
    private RecyclerView rvExpenseReport;
    private ExpenseReportAdapter expenseReportAdapter;

    // 기간 선택 버튼
    private TextView btnPeriodMonth;
    private TextView btnPeriodYear;
    private TextView btnPeriodAll;

    // 기간 선택 영역
    private View layoutPeriod;

    // 현재 선택된 리포트 상태
    private boolean isUsageReportSelected = true;
    private TextView selectedPeriodButton;

    // 리포트 콘텐츠
    private View layoutUsageReport;
    private View layoutExpenseReport;
    private View layoutAnnualExpenseReport;

    // 연간 리포트 데이터 표시
    private TextView tvAnnualTotalExpense;
    private TextView tvAnnualAverageExpense;

    // 연간 그래프 색상
    private int colorOtt;
    private int colorAi;
    private int colorFood;

    // 연간 지출 카테고리 그래프
    private AnnualExpenseDonutView annualDonutChart;
    private LinearLayout layoutAnnualCategoryLegend;
    private TextView tvReportDescription;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.screen_report, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);

        initViews(view);
        setupChartColors();
        setupUsageReportRecyclerView();
        setupExpenseReportRecyclerView();
        setupAnnualExpenseSummary();
        setupAnnualMonthlyChart();
        setupAnnualCategoryChart();

        setupReportTypeButtons();
        setupPeriodButtons();

        // 초기 화면 설정
        selectedPeriodButton = btnPeriodMonth;

        selectReportType(true);
        selectPeriod(btnPeriodMonth);
    }

    // XML에 있는 View 연결
    private void initViews(View view) {
        btnUsageReport = view.findViewById(R.id.btnUsageReport);
        btnExpenseReport = view.findViewById(R.id.btnExpenseReport);

        layoutPeriod = view.findViewById(R.id.layoutPeriod);

        btnPeriodMonth = view.findViewById(R.id.btnPeriodMonth);
        btnPeriodYear = view.findViewById(R.id.btnPeriodYear);
        btnPeriodAll = view.findViewById(R.id.btnPeriodAll);

        layoutUsageReport = view.findViewById(R.id.layoutUsageReport);
        layoutExpenseReport = view.findViewById(R.id.layoutExpenseReport);

        layoutAnnualExpenseReport =
                view.findViewById(R.id.layoutAnnualExpenseReport);

        tvAnnualTotalExpense =
                view.findViewById(R.id.tvAnnualTotalExpense);

        tvAnnualAverageExpense =
                view.findViewById(R.id.tvAnnualAverageExpense);

        rvUsageReport = view.findViewById(R.id.rvUsageReport);
        rvExpenseReport = view.findViewById(R.id.rvExpenseReport);

        tvReportDescription = view.findViewById(R.id.tvReportDescription);

        annualDonutChart = view.findViewById(R.id.annualDonutChart);

        layoutAnnualCategoryLegend =
                view.findViewById(R.id.layoutAnnualCategoryLegend);
    }

    // 사용량 / 누적 지출 버튼 설정
    private void setupReportTypeButtons() {
        btnUsageReport.setOnClickListener(v -> selectReportType(true));

        btnExpenseReport.setOnClickListener(v -> selectReportType(false));
    }


    // 리포트 종류 변경
    private void selectReportType(boolean isUsageReport) {

        // 현재 선택된 리포트 종류 저장
        isUsageReportSelected = isUsageReport;

        // 버튼 선택 상태
        btnUsageReport.setSelected(isUsageReport);
        btnExpenseReport.setSelected(!isUsageReport);

        // 버튼 배경
        btnUsageReport.setBackgroundResource(
                isUsageReport
                        ? R.drawable.bg_report_type_selected
                        : android.R.color.transparent
        );

        btnExpenseReport.setBackgroundResource(
                isUsageReport
                        ? android.R.color.transparent
                        : R.drawable.bg_report_type_selected
        );

        // 버튼 글자색
        btnUsageReport.setTextColor(
                getResources().getColor(
                        isUsageReport ? R.color.appblack : R.color.gray500
                )
        );

        btnExpenseReport.setTextColor(
                getResources().getColor(
                        isUsageReport ? R.color.gray500 : R.color.appblack
                )
        );

        // 기간 선택 영역 표시
        layoutPeriod.setVisibility(
                isUsageReport ? View.GONE : View.VISIBLE
        );

        // 화면 갱신
        updateReportContentVisibility();
    }

    // 현재 탭과 기간에 맞는 리포트 화면 표시
    private void updateReportContentVisibility() {

        boolean isMonth = selectedPeriodButton == btnPeriodMonth;
        boolean isYear = selectedPeriodButton == btnPeriodYear;

        // 사용량 화면
        layoutUsageReport.setVisibility(
                isUsageReportSelected ? View.VISIBLE : View.GONE
        );

        // 월간 지출 화면
        layoutExpenseReport.setVisibility(
                !isUsageReportSelected && isMonth
                        ? View.VISIBLE : View.GONE
        );

        // 연간 지출 화면
        layoutAnnualExpenseReport.setVisibility(
                !isUsageReportSelected && isYear
                        ? View.VISIBLE : View.GONE
        );

        // 설명 문구
        if (isUsageReportSelected) {
            tvReportDescription.setText("앱 사용 시간 (월)");
        } else if (isMonth) {
            tvReportDescription.setText("월별 구독료");
        } else if (isYear) {
            tvReportDescription.setText("연간 구독료");
        } else {
            tvReportDescription.setText("전체 구독료");
        }
    }


    // 기간 선택 버튼 설정
    private void setupPeriodButtons() {
        btnPeriodMonth.setOnClickListener(v ->
                selectPeriod(btnPeriodMonth)
        );

        btnPeriodYear.setOnClickListener(v ->
                selectPeriod(btnPeriodYear)
        );

        btnPeriodAll.setOnClickListener(v ->
                selectPeriod(btnPeriodAll)
        );
    }

    // 기간 버튼 선택 상태 변경
    private void selectPeriod(TextView selectedButton) {

        // 현재 선택된 기간 저장
        selectedPeriodButton = selectedButton;

        TextView[] periodButtons = {
                btnPeriodMonth,
                btnPeriodYear,
                btnPeriodAll
        };

        // 버튼 선택 스타일 변경
        for (TextView button : periodButtons) {
            button.setSelected(button == selectedButton);
        }

        // 화면 갱신
        updateReportContentVisibility();
    }


    private void setupUsageReportRecyclerView() {

        List<UsageReportItem> usageList =
                ReportDummyData.getUsageReportData(requireContext());

        // RecyclerView 설정
        rvUsageReport.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        usageReportAdapter = new UsageReportAdapter(usageList);

        rvUsageReport.setAdapter(usageReportAdapter);
    }

    // 누적 지출 RecyclerView 설정
    private void setupExpenseReportRecyclerView() {

        // 임시 지출 데이터
        List<ExpenseReportItem> expenseList =
                ReportDummyData.getExpenseReportData(requireContext());

        // RecyclerView 설정
        rvExpenseReport.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        expenseReportAdapter = new ExpenseReportAdapter(expenseList);

        rvExpenseReport.setAdapter(expenseReportAdapter);
    }

    // 연간 지출 요약 정보 설정
    private void setupAnnualExpenseSummary() {

        AnnualExpenseData annualData =
                ReportDummyData.getAnnualExpenseData();

        int totalExpense = annualData.getTotalExpense();

        // 12개월 기준 월평균
        int averageExpense = totalExpense / 12;

        tvAnnualTotalExpense.setText(
                String.format(Locale.KOREA, "₩%,d", totalExpense)
        );

        tvAnnualAverageExpense.setText(
                String.format(Locale.KOREA, "월평균 ₩%,d", averageExpense)
        );
    }

    private void setupAnnualMonthlyChart() {

        LinearLayout chartLayout =
                getView().findViewById(R.id.layoutAnnualMonthlyChart);

        chartLayout.removeAllViews();

        AnnualExpenseData annualData =
                ReportDummyData.getAnnualExpenseData();

        List<AnnualExpenseData.MonthlyExpense> monthlyList =
                annualData.getMonthlyExpenses();

        // 그래프에서 가장 큰 월 지출 금액 찾기
        int maxExpense = 1;

        for (AnnualExpenseData.MonthlyExpense item : monthlyList) {
            maxExpense = Math.max(maxExpense, item.getTotalExpense());
        }

        // 월별 막대 생성
        for (AnnualExpenseData.MonthlyExpense item : monthlyList) {

            LinearLayout monthColumn = new LinearLayout(requireContext());
            monthColumn.setOrientation(LinearLayout.VERTICAL);
            monthColumn.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

            LinearLayout.LayoutParams columnParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(34),
                            dpToPx(150)
                    );

            columnParams.setMargins(dpToPx(5), 0, dpToPx(5), 0);
            monthColumn.setLayoutParams(columnParams);

            // 월별 막대 전체 영역
            LinearLayout bar = new LinearLayout(requireContext());
            bar.setOrientation(LinearLayout.VERTICAL);
            bar.setGravity(Gravity.BOTTOM);

            LinearLayout.LayoutParams barParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(22),
                            0,
                            1
                    );

            bar.setLayoutParams(barParams);

            int total = item.getTotalExpense();

            if (total > 0) {

                int barHeight = Math.max(
                        dpToPx(10),
                        (int) (dpToPx(110) * total / (float) maxExpense)
                );

                // 전체 막대 높이
                LinearLayout stack = new LinearLayout(requireContext());
                stack.setOrientation(LinearLayout.VERTICAL);
                stack.setGravity(Gravity.BOTTOM);

                LinearLayout.LayoutParams stackParams =
                        new LinearLayout.LayoutParams(
                                dpToPx(22),
                                barHeight
                        );

                stack.setLayoutParams(stackParams);

                // 카테고리별 막대 높이
                addChartSegment(stack, item.getOttExpense(), total, colorOtt);
                addChartSegment(stack, item.getAiExpense(), total, colorAi);
                addChartSegment(stack, item.getFoodExpense(), total, colorFood);

                bar.addView(stack);
            }

            monthColumn.addView(bar);

            // 월 표시
            TextView monthLabel = new TextView(requireContext());
            monthLabel.setText(item.getMonth() + "월");
            monthLabel.setTextSize(8);
            monthLabel.setTextColor(
                    getResources().getColor(R.color.gray500)
            );
            monthLabel.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams labelParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            dpToPx(20)
                    );

            monthLabel.setLayoutParams(labelParams);

            monthColumn.addView(monthLabel);

            chartLayout.addView(monthColumn);
        }
    }

    // 카테고리별 막대 조각 생성
    private void addChartSegment(
            LinearLayout stack,
            int amount,
            int total,
            int color
    ) {

        if (amount <= 0) return;

        int height = Math.max(
                1,
                (int) (amount / (float) total * stack.getLayoutParams().height)
        );

        View segment = new View(requireContext());

        segment.setBackgroundColor(color);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        height
                );

        segment.setLayoutParams(params);

        stack.addView(segment);
    }

    private void setupAnnualCategoryChart() {

        // 기존 목록 초기화
        layoutAnnualCategoryLegend.removeAllViews();

        // 연간 지출 데이터 가져오기
        AnnualExpenseData annualData =
                ReportDummyData.getAnnualExpenseData();

        List<AnnualExpenseData.CategoryExpense> categoryList =
                annualData.getCategoryExpenses();

        int totalExpense = annualData.getTotalExpense();

        // 도넛 그래프에 데이터 전달
        annualDonutChart.setData(categoryList);

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
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams rowParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            dpToPx(24)
                    );

            rowParams.bottomMargin = dpToPx(4);
            row.setLayoutParams(rowParams);

            // 색상 원형 표시
            View colorDot = new View(requireContext());

            GradientDrawable dotBackground = new GradientDrawable();
            dotBackground.setShape(GradientDrawable.OVAL);
            dotBackground.setColor(
                    categoryColors[i % categoryColors.length]
            );

            colorDot.setBackground(dotBackground);

            LinearLayout.LayoutParams dotParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(7),
                            dpToPx(7)
                    );

            dotParams.rightMargin = dpToPx(8);
            row.addView(colorDot, dotParams);

            // 카테고리 이름
            TextView nameText = new TextView(requireContext());
            nameText.setText(item.getCategoryName());
            nameText.setTextSize(10);
            nameText.setTextColor(
                    getResources().getColor(R.color.appblack)
            );

            LinearLayout.LayoutParams nameParams =
                    new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1
                    );

            row.addView(nameText, nameParams);

            // 지출 비율
            TextView percentText = new TextView(requireContext());
            percentText.setText(percent + "%");
            percentText.setTextSize(10);
            percentText.setTextColor(
                    getResources().getColor(R.color.gray500)
            );

            LinearLayout.LayoutParams percentParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(36),
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            row.addView(percentText, percentParams);

            // 지출 금액
            TextView amountText = new TextView(requireContext());

            amountText.setText(
                    String.format(Locale.KOREA, "₩%,d", amount)
            );

            amountText.setTextSize(10);
            amountText.setTextColor(
                    getResources().getColor(R.color.appblack)
            );
            amountText.setGravity(Gravity.END);

            LinearLayout.LayoutParams amountParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(65),
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            row.addView(amountText, amountParams);

            // 완성된 한 줄을 목록에 추가
            layoutAnnualCategoryLegend.addView(row);
        }
    }private void setupChartColors() {
        colorOtt = getResources().getColor(R.color.pastel_red);
        colorAi = getResources().getColor(R.color.pastel_blue);
        colorFood = getResources().getColor(R.color.pastel_yellow);
    }

    // dp를 px로 변환
    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

}