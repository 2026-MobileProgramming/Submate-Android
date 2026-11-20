package com.example.submate.presentation.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submate.presentation.report.adapter.UsageReportAdapter;
import com.example.submate.presentation.report.data.ReportDummyData;
import com.example.submate.presentation.report.model.AllExpenseData;
import com.example.submate.presentation.report.model.UsageReportItem;
import com.example.submate.presentation.report.adapter.ExpenseReportAdapter;
import com.example.submate.presentation.report.model.ExpenseReportItem;
import com.example.submate.presentation.report.model.AnnualExpenseData;
import com.example.submate.presentation.report.view.AnnualExpenseDonutView;
import com.example.submate.presentation.report.view.AnnualMonthlyChartRenderer;
import com.example.submate.presentation.report.view.AllYearlyChartRenderer;
import com.example.submate.presentation.report.view.AllCategoryChartRenderer;
import com.example.submate.presentation.report.view.AnnualCategoryLegendRenderer;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.model.SubscribeCategory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

import com.example.submate.R;

@AndroidEntryPoint
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

    // 월/연도 이동 영역
    private TextView btnPreviousMonth;
    private TextView btnNextMonth;
    private TextView tvReportMonth;

    private TextView tvTotalExpense;
    private TextView tvExpenseEmpty;

    // 현재 선택된 리포트 날짜
    private Calendar selectedReportDate;

    // 기간 선택 영역
    private View layoutPeriod;

    // 현재 선택된 리포트 상태
    private boolean isUsageReportSelected = true;
    private TextView selectedPeriodButton;

    // 리포트 콘텐츠
    private View layoutUsageReport;
    private View layoutExpenseReport;
    private View layoutAnnualExpenseReport;
    // 월간 지출 카테고리 그래프
    private LinearLayout layoutMonthlyCategoryBar;
    private LinearLayout layoutMonthlyCategoryLegend;

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
    // 연간 월별 지출 그래프
    private LinearLayout layoutAnnualMonthlyChart;

    // 전체 누적 지출 리포트
    private View layoutAllExpenseReport;

    private TextView tvAllExpensePeriod;
    private TextView tvAllTotalExpense;
    private TextView tvAllServiceCount;

    private LinearLayout layoutAllYearlyChart;
    private LinearLayout layoutAllCategoryBar;
    private LinearLayout layoutAllCategoryList;
    private LinearLayout layoutReportPeriodNavigation;

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
        selectedReportDate = Calendar.getInstance();
        setupChartColors();

        setupUsageReportRecyclerView();
        setupExpenseReportRecyclerView();

        // 실제 구독 목록 변경 감지
        viewModel.getSubscribeList().observe(
                getViewLifecycleOwner(),
                subscribeItems -> updateMonthlyExpenseReport(subscribeItems)
        );

        // 연간 지출 리포트
        setupAnnualExpenseSummary();
        setupAnnualMonthlyChart();
        setupAnnualCategoryChart();

        // 전체 지출 리포트
        setupAllExpenseSummary();
        setupAllYearlyChart();
        setupAllCategoryChart();

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

        // 월/연도 이동 영역
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        tvReportMonth = view.findViewById(R.id.tvReportMonth);


        layoutUsageReport = view.findViewById(R.id.layoutUsageReport);
        layoutExpenseReport = view.findViewById(R.id.layoutExpenseReport);

        layoutAnnualMonthlyChart =
                view.findViewById(R.id.layoutAnnualMonthlyChart);

        layoutAnnualExpenseReport =
                view.findViewById(R.id.layoutAnnualExpenseReport);

        tvAnnualTotalExpense =
                view.findViewById(R.id.tvAnnualTotalExpense);

        tvAnnualAverageExpense =
                view.findViewById(R.id.tvAnnualAverageExpense);

        layoutMonthlyCategoryBar =
                view.findViewById(R.id.layoutMonthlyCategoryBar);

        layoutMonthlyCategoryBar =
                view.findViewById(R.id.layoutMonthlyCategoryBar);

        layoutMonthlyCategoryLegend =
                view.findViewById(R.id.layoutMonthlyCategoryLegend);


        tvTotalExpense = view.findViewById(R.id.tvTotalExpense);
        tvExpenseEmpty = view.findViewById(R.id.tvExpenseEmpty);

        rvUsageReport = view.findViewById(R.id.rvUsageReport);
        rvExpenseReport = view.findViewById(R.id.rvExpenseReport);

        tvReportDescription = view.findViewById(R.id.tvReportDescription);

        annualDonutChart = view.findViewById(R.id.annualDonutChart);

        layoutAnnualCategoryLegend =
                view.findViewById(R.id.layoutAnnualCategoryLegend);

        // 전체 누적 지출 화면
        layoutAllExpenseReport =
                view.findViewById(R.id.layoutAllExpenseReport);

        tvAllExpensePeriod =
                view.findViewById(R.id.tvAllExpensePeriod);

        tvAllTotalExpense =
                view.findViewById(R.id.tvAllTotalExpense);

        tvAllServiceCount =
                view.findViewById(R.id.tvAllServiceCount);

        layoutAllYearlyChart =
                view.findViewById(R.id.layoutAllYearlyChart);

        layoutAllCategoryBar =
                view.findViewById(R.id.layoutAllCategoryBar);

        layoutAllCategoryList =
                view.findViewById(R.id.layoutAllCategoryList);

        layoutReportPeriodNavigation =
                view.findViewById(R.id.layoutReportPeriodNavigation);
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
        boolean isAll = selectedPeriodButton == btnPeriodAll;

        // 사용량 리포트
        layoutUsageReport.setVisibility(
                isUsageReportSelected ? View.VISIBLE : View.GONE
        );

        // 월별 지출 리포트
        layoutExpenseReport.setVisibility(
                !isUsageReportSelected && isMonth
                        ? View.VISIBLE : View.GONE
        );

        // 연간 지출 리포트
        layoutAnnualExpenseReport.setVisibility(
                !isUsageReportSelected && isYear
                        ? View.VISIBLE : View.GONE
        );

        // 전체 누적 지출 리포트
        layoutAllExpenseReport.setVisibility(
                !isUsageReportSelected && isAll
                        ? View.VISIBLE : View.GONE
        );
        // 전체 탭에서는 기존 월간 이동 영역 숨기기
        layoutReportPeriodNavigation.setVisibility(
                !isUsageReportSelected && isAll
                        ? View.GONE
                        : View.VISIBLE
        );

        // 화면 설명
        if (isUsageReportSelected) {
            tvReportDescription.setText("앱 사용 시간 (월)");
        } else if (isMonth) {
            tvReportDescription.setText("월별 구독료");
        } else if (isYear) {
            tvReportDescription.setText("연간 구독료");
        } else {
            tvReportDescription.setText("전체 구독료");
        }
        // 월/연도 이동 표시 갱신
        updatePeriodNavigation();
        // 월별 지출 화면으로 돌아왔을 때 데이터 갱신
        if (!isUsageReportSelected && isMonth) {
            updateMonthlyExpenseReport(viewModel.getSubscribeList().getValue());
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

        // 월/연도 이동 버튼 설정
        setupPeriodNavigation();
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
    // 월별 지출 RecyclerView 설정
    private void setupExpenseReportRecyclerView() {

        rvExpenseReport.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        // 처음에는 빈 목록으로 시작
        expenseReportAdapter = new ExpenseReportAdapter(new ArrayList<>());

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

    // 연간 월별 지출 그래프 설정
    private void setupAnnualMonthlyChart() {

        AnnualExpenseData annualData =
                ReportDummyData.getAnnualExpenseData();

        AnnualMonthlyChartRenderer.render(
                requireContext(),
                layoutAnnualMonthlyChart,
                annualData.getMonthlyExpenses(),
                colorOtt,
                colorAi,
                colorFood
        );
    }

    // 연간 카테고리 그래프 설정
    private void setupAnnualCategoryChart() {

        AnnualExpenseData annualData =
                ReportDummyData.getAnnualExpenseData();

        List<AnnualExpenseData.CategoryExpense> categoryList =
                annualData.getCategoryExpenses();

        // 도넛 그래프에 데이터 전달
        annualDonutChart.setData(categoryList);

        // 도넛 그래프 옆 카테고리 목록 생성
        AnnualCategoryLegendRenderer.render(
                requireContext(),
                layoutAnnualCategoryLegend,
                categoryList,
                annualData.getTotalExpense(),
                colorOtt,
                colorAi,
                colorFood
        );
    }

    private void setupAllYearlyChart() {

        AllExpenseData allData =
                ReportDummyData.getAllExpenseData();

        AllYearlyChartRenderer.render(
                requireContext(),
                layoutAllYearlyChart,
                allData.getYearlyExpenses()
        );
    }

    private void setupAllCategoryChart() {

        AllExpenseData allData =
                ReportDummyData.getAllExpenseData();

        AllCategoryChartRenderer.render(
                requireContext(),
                layoutAllCategoryBar,
                layoutAllCategoryList,
                allData.getCategoryExpenses(),
                allData.getTotalExpense(),
                colorOtt,
                colorAi,
                colorFood
        );
    }

    private void setupAllExpenseSummary() {

        AllExpenseData allData =
                ReportDummyData.getAllExpenseData();

        tvAllTotalExpense.setText(
                String.format(
                        Locale.KOREA,
                        "₩%,d",
                        allData.getTotalExpense()
                )
        );

        tvAllServiceCount.setText(
                "구독 서비스 " + allData.getServiceCount() + "개"
        );
    }
    // 이전/다음 월 또는 연도 이동 설정
    private void setupPeriodNavigation() {

        btnPreviousMonth.setOnClickListener(v -> moveReportPeriod(-1));

        btnNextMonth.setOnClickListener(v -> moveReportPeriod(1));
    }

    // 선택된 기간에 따라 월 또는 연도 이동
    private void moveReportPeriod(int direction) {

        if (selectedPeriodButton == btnPeriodMonth) {
            selectedReportDate.add(Calendar.MONTH, direction);

        } else if (selectedPeriodButton == btnPeriodYear) {
            selectedReportDate.add(Calendar.YEAR, direction);
        }

        // 날짜 표시 갱신
        updatePeriodNavigation();

        // 월별 지출 탭을 보고 있을 때 데이터도 즉시 갱신
        if (!isUsageReportSelected
                && selectedPeriodButton == btnPeriodMonth) {

            updateMonthlyExpenseReport(
                    viewModel.getSubscribeList().getValue()
            );
        }
    }


    // 현재 선택된 기간 표시
    private void updatePeriodNavigation() {

        Calendar today = Calendar.getInstance();

        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH);

        int selectedYear = selectedReportDate.get(Calendar.YEAR);
        int selectedMonth = selectedReportDate.get(Calendar.MONTH);

        // 연간 탭: 연도만 표시
        if (selectedPeriodButton == btnPeriodYear) {
            tvReportMonth.setText(selectedYear + "년");

            // 현재 연도보다 이전이면 다음 버튼 표시
            btnNextMonth.setVisibility(
                    selectedYear < currentYear ? View.VISIBLE : View.GONE
            );

        } else {
            // 월간 탭: 연도와 월 표시
            tvReportMonth.setText(
                    selectedYear + "년 " + (selectedMonth + 1) + "월"
            );

            // 현재 연월보다 이전이면 다음 버튼 표시
            boolean isBeforeCurrentMonth =
                    selectedYear < currentYear ||
                            (selectedYear == currentYear && selectedMonth < currentMonth);

            btnNextMonth.setVisibility(
                    isBeforeCurrentMonth ? View.VISIBLE : View.GONE
            );
        }
    }


    private void setupChartColors() {
        colorOtt = getResources().getColor(R.color.pastel_red);
        colorAi = getResources().getColor(R.color.pastel_blue);
        colorFood = getResources().getColor(R.color.pastel_yellow);
    }

    // 월간 지출 범례의 동그라미에만 카테고리 색상 적용
    private void setLegendColor(TextView textView, int color) {
        String text = textView.getText().toString();

        SpannableString spannable = new SpannableString(text);

        spannable.setSpan(
                new ForegroundColorSpan(color),
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        textView.setText(spannable);
    }

    // 실제 구독 데이터로 월별 지출 리포트 갱신
    private void updateMonthlyExpenseReport(
            List<SubscribeItem> subscribeItems
    ) {

        // 현재 날짜
        Calendar today = Calendar.getInstance();

        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH) + 1;
        // 선택한 월의 마지막 날짜를 기준으로 결제 여부 판단
        int lastDayOfMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 선택한 날짜
        int selectedYear = selectedReportDate.get(Calendar.YEAR);
        int selectedMonth = selectedReportDate.get(Calendar.MONTH) + 1;

        // 현재 월이 아니면 빈 목록 표시
        if (selectedYear != currentYear || selectedMonth != currentMonth) {
            showMonthlyExpenseEmpty();
            return;
        }

        // 결제일이 지난 구독만 가져오기
        List<SubscribeItem> paidList =
                viewModel.getPaidSubscriptionsForMonth(
                        subscribeItems,
                        selectedYear,
                        selectedMonth,
                        lastDayOfMonth
                );

        // 총 지출 금액 먼저 계산
        long totalExpense = 0;

        for (SubscribeItem item : paidList) {
            totalExpense += item.getKrwAmount();
        }
        // 카테고리별 지출 그래프 갱신
        updateMonthlyCategoryChart(paidList, totalExpense);

// 서비스별 지출 데이터 생성
        List<ExpenseReportItem> expenseList = new ArrayList<>();

        for (SubscribeItem item : paidList) {

            int price = (int) item.getKrwAmount();

            // 전체 지출에서 해당 서비스가 차지하는 비율
            int expensePercent = totalExpense > 0
                    ? (int) Math.round(price * 100.0 / totalExpense)
                    : 0;

            int categoryColor;

            switch (item.getCategory()) {
                case OTT:
                    categoryColor = colorOtt;
                    break;

                case AI:
                    categoryColor = colorAi;
                    break;

                case FOOD:
                    categoryColor = colorFood;
                    break;

                default:
                    categoryColor = getResources().getColor(R.color.gray500);
                    break;
            }

            expenseList.add(
                    new ExpenseReportItem(
                            item.getServiceName(),
                            item.getServiceName().isEmpty()
                                    ? "?"
                                    : item.getServiceName().substring(0, 1),
                            price,
                            expensePercent,
                            categoryColor,
                            categoryColor
                    )
            );
        }

        // 총 구독료 표시
        tvTotalExpense.setText(
                String.format(Locale.KOREA, "₩%,d", totalExpense)
        );

        // RecyclerView 목록 갱신
        expenseReportAdapter.setExpenseList(expenseList);

        // 데이터 유무에 따른 화면 전환
        if (expenseList.isEmpty()) {
            showMonthlyExpenseEmpty();
        } else {
            rvExpenseReport.setVisibility(View.VISIBLE);
            tvExpenseEmpty.setVisibility(View.GONE);
        }
    }

    private void showMonthlyExpenseEmpty() {
        tvTotalExpense.setText("₩0");
        expenseReportAdapter.setExpenseList(new ArrayList<>());

        // 카테고리 그래프와 범례 초기화
        layoutMonthlyCategoryBar.removeAllViews();
        layoutMonthlyCategoryLegend.removeAllViews();

        rvExpenseReport.setVisibility(View.GONE);
        tvExpenseEmpty.setVisibility(View.VISIBLE);
    }
    // 카테고리별 그래프 색상 반환
    private int getCategoryColor(SubscribeCategory category) {
        switch (category) {
            case OTT:
                return getResources().getColor(R.color.pastel_red);

            case MUSIC:
                return getResources().getColor(R.color.pastel_purple);

            case FOOD:
                return getResources().getColor(R.color.pastel_yellow);

            case SHOPPING:
                return getResources().getColor(R.color.pastel_green);

            case AI:
                return getResources().getColor(R.color.pastel_blue);

            case DIGITAL_CONTENT:
                return getResources().getColor(R.color.pastel_orange);

            case ETC:
            default:
                return getResources().getColor(R.color.gray500);
        }
    }
    // 월간 카테고리별 지출 그래프 및 범례 생성
    private void updateMonthlyCategoryChart(
            List<SubscribeItem> paidList,
            long totalExpense
    ) {
        // 기존 그래프와 범례 초기화
        layoutMonthlyCategoryBar.removeAllViews();
        layoutMonthlyCategoryLegend.removeAllViews();

        if (totalExpense <= 0 || paidList.isEmpty()) {
            return;
        }

        // 카테고리별 금액 초기화
        Map<SubscribeCategory, Long> categoryAmounts =
                new LinkedHashMap<>();

        for (SubscribeCategory category : SubscribeCategory.values()) {
            categoryAmounts.put(category, 0L);
        }

        // 실제 결제 금액을 카테고리별로 합산
        for (SubscribeItem item : paidList) {
            SubscribeCategory category = item.getCategory();

            if (category == null) {
                category = SubscribeCategory.ETC;
            }

            long amount = item.getKrwAmount();

            categoryAmounts.put(
                    category,
                    categoryAmounts.get(category) + amount
            );
        }

        // 5% 미만 카테고리는 기타로 합산
        long etcAmount = categoryAmounts.get(SubscribeCategory.ETC);

        for (SubscribeCategory category : SubscribeCategory.values()) {
            if (category == SubscribeCategory.ETC) {
                continue;
            }

            long amount = categoryAmounts.get(category);

            double percent = amount * 100.0 / totalExpense;

            if (amount > 0 && percent < 5.0) {
                etcAmount += amount;
                categoryAmounts.put(category, 0L);
            }
        }

        categoryAmounts.put(SubscribeCategory.ETC, etcAmount);

        // 그래프에 표시할 카테고리 순서대로 생성
        for (SubscribeCategory category : SubscribeCategory.values()) {
            long amount = categoryAmounts.get(category);

            if (amount <= 0) {
                continue;
            }

            int color = getCategoryColor(category);

            // 막대 그래프 생성
            View bar = new View(requireContext());

            LinearLayout.LayoutParams barParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            (float) amount
                    );

            bar.setLayoutParams(barParams);
            bar.setBackgroundColor(color);

            layoutMonthlyCategoryBar.addView(bar);

            // 범례 생성
            int percent = (int) Math.round(
                    amount * 100.0 / totalExpense
            );

            TextView legend = new TextView(requireContext());

            legend.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    )
            );

            legend.setText(
                    "● " + category.getLabel() + " " + percent + "%"
            );

            legend.setTextSize(9);
            legend.setTextColor(
                    getResources().getColor(R.color.gray500)
            );

            setLegendColor(legend, color);

            layoutMonthlyCategoryLegend.addView(legend);
        }
    }

}