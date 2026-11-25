package com.example.submate.presentation.report;

import android.content.res.ColorStateList;
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
import java.util.Collections;
import java.util.Locale;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;

import androidx.core.widget.TextViewCompat;
import com.example.submate.R;
import com.google.android.material.textview.MaterialTextView;
import com.example.submate.presentation.subscribelist.activity.MainActivity;

@AndroidEntryPoint
public class ReportFragment extends Fragment {
    private static final long LOW_USAGE_THRESHOLD_MILLIS = TimeUnit.HOURS.toMillis(3);

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
    private TextView tvExpenseComparison;
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

    // 연간 지출 카테고리 그래프
    private AnnualExpenseDonutView annualDonutChart;
    private LinearLayout layoutAnnualCategoryLegend;
    private TextView tvReportDescription;
    // 연간 월별 지출 그래프
    private LinearLayout layoutAnnualMonthlyChart;

    private TextView tvAnnualMonthlyEmpty;
    private TextView tvAnnualCategoryEmpty;

    private TextView tvTotalUsageTime;
    private TextView tvUsageEmpty;
    private View layoutUsageSummaryCard;
    private View layoutUsageWarningCard;
    private TextView tvUsageWarningMessage;

    private View scrollAnnualMonthlyChart;
    private View layoutAnnualCategoryChart;

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

        setupUsageReportRecyclerView();
        setupExpenseReportRecyclerView();
        viewModel.getMonthlyUsage().observe(getViewLifecycleOwner(), this::renderUsageReport);

        // 실제 구독 목록 변경 감지
        viewModel.getSubscribeList().observe(
                getViewLifecycleOwner(),
                subscribeItems -> {
                    updateMonthlyExpenseReport(subscribeItems);
                    updateAnnualExpenseReport(subscribeItems);
                    setupAllYearlyChart(subscribeItems);
                }
        );

        // 연간 지출 리포트
        updateAnnualExpenseReport(
                viewModel.getSubscribeList().getValue()
        );

        // 전체 지출 리포트
        setupAllServiceCount();
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

        // 공용 헤더 제목 설정
        TextView tvHeaderTitle = view.findViewById(R.id.tv_subscribe_title);
        tvHeaderTitle.setTextAppearance(R.style.ReportText_Heading1);
        TextViewCompat.setLineHeight(tvHeaderTitle,
                getResources().getDimensionPixelSize(R.dimen.report_heading1_line_height));
        tvHeaderTitle.setText("리포트");
        ViewGroup.MarginLayoutParams titleParams =
                (ViewGroup.MarginLayoutParams) tvHeaderTitle.getLayoutParams();
        titleParams.setMarginStart(getResources().getDimensionPixelSize(
                R.dimen.report_content_horizontal_inset));
        tvHeaderTitle.setLayoutParams(titleParams);

        // 알림 버튼 클릭 시 알림 화면으로 이동
        View notificationButton = view.findViewById(R.id.iv_subscribe_notification);
        ViewGroup.MarginLayoutParams notificationParams =
                (ViewGroup.MarginLayoutParams) notificationButton.getLayoutParams();
        notificationParams.setMarginEnd(getResources().getDimensionPixelSize(
                R.dimen.report_header_notification_margin));
        notificationButton.setLayoutParams(notificationParams);
        notificationButton.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).openNotifications();
        });

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

        tvAnnualMonthlyEmpty = view.findViewById(R.id.tvAnnualMonthlyEmpty);
        tvAnnualCategoryEmpty = view.findViewById(R.id.tvAnnualCategoryEmpty);

        tvTotalUsageTime = view.findViewById(R.id.tvTotalUsageTime);
        tvUsageEmpty = view.findViewById(R.id.tvUsageEmpty);
        layoutUsageSummaryCard = view.findViewById(R.id.layoutUsageSummaryCard);
        layoutUsageWarningCard = view.findViewById(R.id.layoutUsageWarningCard);
        tvUsageWarningMessage = view.findViewById(R.id.tvUsageWarningMessage);

        scrollAnnualMonthlyChart = view.findViewById(R.id.scrollAnnualMonthlyChart);
        layoutAnnualCategoryChart = view.findViewById(R.id.layoutAnnualCategoryChart);

        layoutMonthlyCategoryBar =
                view.findViewById(R.id.layoutMonthlyCategoryBar);

        layoutMonthlyCategoryBar =
                view.findViewById(R.id.layoutMonthlyCategoryBar);

        layoutMonthlyCategoryLegend =
                view.findViewById(R.id.layoutMonthlyCategoryLegend);


        tvTotalExpense = view.findViewById(R.id.tvTotalExpense);
        tvExpenseComparison = view.findViewById(R.id.tvExpenseComparison);
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

        // 사용량 탭으로 이동하면 기간을 항상 월간으로 초기화
        if (isUsageReport) {
            selectedPeriodButton = btnPeriodMonth;

            // 기간 선택 버튼의 선택 상태도 초기화
            btnPeriodMonth.setSelected(true);
            btnPeriodYear.setSelected(false);
            btnPeriodAll.setSelected(false);
        }

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
        if (!isUsageReportSelected && isAll) {
            setupAllYearlyChart(viewModel.getSubscribeList().getValue());
        }
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

        if (isUsageReportSelected) {
            // 사용량 탭이면 선택한 월의 사용량 갱신
            viewModel.loadMonthlyUsage(selectedReportDate);

        } else {
            if (selectedPeriodButton == btnPeriodMonth) {
                updateMonthlyExpenseReport(
                        viewModel.getSubscribeList().getValue()
                );

            } else if (selectedPeriodButton == btnPeriodYear) {
                updateAnnualExpenseReport(
                        viewModel.getSubscribeList().getValue()
                );
            }
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
        // RecyclerView 설정
        rvUsageReport.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        usageReportAdapter = new UsageReportAdapter(Collections.emptyList());

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


    // 연간 리포트 전체 갱신
    private void updateAnnualExpenseReport(
            List<SubscribeItem> subscribeItems
    ) {
        AnnualExpenseData annualData =
                calculateAnnualExpenseData(subscribeItems);

        int totalExpense = annualData.getTotalExpense();

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;

        // 연간 총지출
        tvAnnualTotalExpense.setText(
                String.format(Locale.KOREA, "₩%,d", totalExpense)
        );

        // 1월부터 현재 월까지의 월평균
        int averageExpense = currentMonth > 0
                ? totalExpense / currentMonth
                : 0;

        tvAnnualAverageExpense.setText(
                String.format(
                        Locale.KOREA,
                        "월평균 ₩%,d",
                        averageExpense
                )
        );

        boolean hasData = totalExpense > 0;

        // 월별 지출
        tvAnnualMonthlyEmpty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        scrollAnnualMonthlyChart.setVisibility(hasData ? View.VISIBLE : View.GONE);

        // 카테고리별 지출
        tvAnnualCategoryEmpty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        layoutAnnualCategoryChart.setVisibility(hasData ? View.VISIBLE : View.GONE);

        // 월별 지출 그래프
        AnnualMonthlyChartRenderer.render(
                requireContext(),
                layoutAnnualMonthlyChart,
                annualData.getMonthlyExpenses()
        );

        // 카테고리 도넛 그래프
        annualDonutChart.setData(
                annualData.getCategoryExpenses()
        );

        // 카테고리 범례
        AnnualCategoryLegendRenderer.render(
                requireContext(),
                layoutAnnualCategoryLegend,
                annualData.getCategoryExpenses(),
                totalExpense
        );
    }

    private void setupAllYearlyChart(List<SubscribeItem> subscribeItems) {

        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        List<AllExpenseData.YearlyExpense> yearlyExpenses = new ArrayList<>();
        for (AllExpenseData.YearlyExpense expense : ReportDummyData.getAllExpenseData().getYearlyExpenses()) {
            if (expense.getYear() < currentYear) {
                yearlyExpenses.add(expense);
            }
        }
        yearlyExpenses.add(new AllExpenseData.YearlyExpense(
                currentYear, viewModel.getExpenseThrough(subscribeItems, today)
        ));

        long totalExpense = 0;
        for (AllExpenseData.YearlyExpense expense : yearlyExpenses) {
            totalExpense += expense.getAmount();
        }
        tvAllTotalExpense.setText(String.format(Locale.KOREA, "₩%,d", totalExpense));

        Calendar yearEnd = (Calendar) today.clone();
        yearEnd.set(currentYear, Calendar.DECEMBER, 31);

        AllYearlyChartRenderer.render(
                requireContext(),
                layoutAllYearlyChart,
                yearlyExpenses,
                currentYear,
                viewModel.getExpenseThrough(subscribeItems, yearEnd)
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
                allData.getTotalExpense()
        );
    }

    private void setupAllServiceCount() {

        AllExpenseData allData =
                ReportDummyData.getAllExpenseData();

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

        // 날짜 변경 후 현재 탭과 기간에 맞게 화면 갱신
        updateReportContentVisibility();
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


    // 실제 구독 데이터로 연간 지출 데이터 생성
    private AnnualExpenseData calculateAnnualExpenseData(
            List<SubscribeItem> subscribeItems
    ) {
        Calendar today = Calendar.getInstance();

        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH) + 1;
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        int selectedYear = selectedReportDate.get(Calendar.YEAR);

        // 과거 연도는 실제 결제 기록이 없으므로 빈 데이터 반환
        if (selectedYear != currentYear) {
            return new AnnualExpenseData(
                    0,
                    new ArrayList<>(),
                    new ArrayList<>()
            );
        }

        List<AnnualExpenseData.MonthlyExpense> monthlyExpenses =
                new ArrayList<>();

        // 카테고리별 연간 누적 금액 초기화
        Map<SubscribeCategory, Long> categoryTotals =
                new LinkedHashMap<>();

        for (SubscribeCategory category : SubscribeCategory.values()) {
            categoryTotals.put(category, 0L);
        }

        long annualTotal = 0;

        // 1월부터 현재 월까지 계산
        for (int month = 1; month <= currentMonth; month++) {

            Calendar monthCalendar = Calendar.getInstance();
            monthCalendar.set(selectedYear, month - 1, 1);

            // 과거 월은 마지막 날, 현재 월은 오늘 날짜까지 계산
            int todayDay = (month == currentMonth)
                    ? currentDay
                    : monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            List<SubscribeItem> paidList =
                    viewModel.getPaidSubscriptionsForMonth(
                            subscribeItems,
                            selectedYear,
                            month,
                            todayDay
                    );

            // 해당 월의 카테고리별 지출 초기화
            Map<SubscribeCategory, Long> monthlyCategoryTotals =
                    new LinkedHashMap<>();

            for (SubscribeCategory category : SubscribeCategory.values()) {
                monthlyCategoryTotals.put(category, 0L);
            }

            // 결제 대상 구독을 카테고리별로 합산
            for (SubscribeItem item : paidList) {

                SubscribeCategory category = item.getCategory();

                if (category == null) {
                    category = SubscribeCategory.ETC;
                }

                long amount = item.getKrwAmount();

                monthlyCategoryTotals.put(
                        category,
                        monthlyCategoryTotals.get(category) + amount
                );

                categoryTotals.put(
                        category,
                        categoryTotals.get(category) + amount
                );
            }

            // 월별 카테고리 금액
            int ott = monthlyCategoryTotals.get(SubscribeCategory.OTT).intValue();
            int music = monthlyCategoryTotals.get(SubscribeCategory.MUSIC).intValue();
            int food = monthlyCategoryTotals.get(SubscribeCategory.FOOD).intValue();
            int shopping = monthlyCategoryTotals.get(SubscribeCategory.SHOPPING).intValue();
            int ai = monthlyCategoryTotals.get(SubscribeCategory.AI).intValue();
            int digitalContent = monthlyCategoryTotals.get(SubscribeCategory.DIGITAL_CONTENT).intValue();
            int etc = monthlyCategoryTotals.get(SubscribeCategory.ETC).intValue();

            AnnualExpenseData.MonthlyExpense monthlyExpense =
                    new AnnualExpenseData.MonthlyExpense(
                            month,
                            ott,
                            music,
                            food,
                            shopping,
                            ai,
                            digitalContent,
                            etc
                    );

            monthlyExpenses.add(monthlyExpense);

            annualTotal += monthlyExpense.getTotalExpense();
        }

        // 금액이 있는 카테고리만 도넛 차트와 범례에 전달
        List<AnnualExpenseData.CategoryExpense> categoryExpenses =
                new ArrayList<>();

        for (SubscribeCategory category : SubscribeCategory.values()) {

            long amount = categoryTotals.get(category);

            if (amount <= 0) {
                continue;
            }

            categoryExpenses.add(
                    new AnnualExpenseData.CategoryExpense(
                            category,
                            (int) amount
                    )
            );
        }

        return new AnnualExpenseData(
                (int) annualTotal,
                monthlyExpenses,
                categoryExpenses
        );
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
        // 지난달 날짜 계산
        Calendar previousMonth = (Calendar) selectedReportDate.clone();
        previousMonth.add(Calendar.MONTH, -1);

        int previousYear = previousMonth.get(Calendar.YEAR);
        int previousMonthNumber = previousMonth.get(Calendar.MONTH) + 1;
        int previousMonthLastDay =
                previousMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

// 지난달 결제 대상 구독 목록 가져오기
        List<SubscribeItem> previousPaidList =
                viewModel.getPaidSubscriptionsForMonth(
                        subscribeItems,
                        previousYear,
                        previousMonthNumber,
                        previousMonthLastDay
                );

// 지난달 총 구독료 계산
        long previousTotalExpense = 0;

        for (SubscribeItem item : previousPaidList) {
            previousTotalExpense += item.getKrwAmount();
        }

// 이번 달과 지난달의 차이
        long difference = totalExpense - previousTotalExpense;

// 증감 문구 표시
        if (difference > 0) {
            tvExpenseComparison.setText(
                    String.format(
                            Locale.KOREA,
                            "지난달보다 ▲ ₩%,d 증가했어요",
                            difference
                    )
            );
            tvExpenseComparison.setTextColor(
                    getResources().getColor(R.color.error800)
            );

        } else if (difference < 0) {
            tvExpenseComparison.setText(
                    String.format(
                            Locale.KOREA,
                            "지난달보다 ▼ ₩%,d 감소했어요",
                            Math.abs(difference)
                    )
            );
            tvExpenseComparison.setTextColor(
                    getResources().getColor(R.color.pastel_blue)
            );

        } else {
            tvExpenseComparison.setText("지난달과 동일해요");
            tvExpenseComparison.setTextColor(
                    getResources().getColor(R.color.gray500)
            );
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

            int categoryColor = ReportCategoryColors.getColor(requireContext(), item.getCategory());

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
    private void renderUsageReport(@Nullable ReportViewModel.MonthlyUsageReport report) {
        List<UsageReportItem> usageList = report == null ? Collections.emptyList() : report.items;
        usageReportAdapter.setUsageList(usageList);

        // 데이터가 없는 경우
        if (usageList.isEmpty()) {

            // 요약 카드의 기본 틀은 유지
            layoutUsageSummaryCard.setVisibility(View.VISIBLE);

            // 총 사용 시간은 - 시간으로 표시
            tvTotalUsageTime.setText("- 시간");

            // 사용량 목록 대신 데이터 없음 안내 표시
            rvUsageReport.setVisibility(View.GONE);
            tvUsageEmpty.setVisibility(View.VISIBLE);
            int message = R.string.report_usage_no_records;
            if (report == null) {
                message = R.string.report_usage_loading;
            } else if (report.queryFailed) {
                message = R.string.report_usage_unavailable;
            } else if (report.unlinkedSubscriptionCount > 0 && report.packagesWithoutRecords == 0) {
                message = R.string.report_usage_no_linked_apps;
            }
            tvUsageEmpty.setText(message);

            // 경고 카드는 숨김
            layoutUsageWarningCard.setVisibility(View.GONE);

            return;
        }

        long totalUsageMillis = 0;
        List<String> lowUsageServiceNames = new ArrayList<>();

        for (UsageReportItem item : usageList) {
            totalUsageMillis += item.getUsageMillis();
            if (item.getUsageMillis() <= LOW_USAGE_THRESHOLD_MILLIS) {
                lowUsageServiceNames.add(item.getServiceName());
            }
        }

        tvTotalUsageTime.setText(TimeUnit.MILLISECONDS.toHours(totalUsageMillis) + "시간");

        layoutUsageSummaryCard.setVisibility(View.VISIBLE);
        boolean hasLowUsage = !lowUsageServiceNames.isEmpty();
        layoutUsageWarningCard.setVisibility(hasLowUsage ? View.VISIBLE : View.GONE);
        if (hasLowUsage) {
            String lastServiceName = lowUsageServiceNames.get(lowUsageServiceNames.size() - 1);
            tvUsageWarningMessage.setText(getString(
                    R.string.report_low_usage_message,
                    android.text.TextUtils.join(", ", lowUsageServiceNames)
                            + getObjectParticle(lastServiceName)
            ));
        }
        rvUsageReport.setVisibility(View.VISIBLE);
        tvUsageEmpty.setVisibility(View.GONE);
    }

    private static String getObjectParticle(String serviceName) {
        String name = serviceName.trim();
        if (name.isEmpty()) {
            return "을(를)";
        }
        char lastCharacter = name.charAt(name.length() - 1);
        if (lastCharacter < '가' || lastCharacter > '힣') {
            return "을(를)";
        }
        return (lastCharacter - '가') % 28 == 0 ? "를" : "을";
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

        // 그래프에 표시할 카테고리 순서대로 생성
        for (SubscribeCategory category : SubscribeCategory.values()) {
            long amount = categoryAmounts.get(category);

            if (amount <= 0) {
                continue;
            }

            int color = ReportCategoryColors.getColor(requireContext(), category);

            // 막대 그래프 생성
            View bar = new View(requireContext());

            LinearLayout.LayoutParams barParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            (float) amount
                    );

            if (layoutMonthlyCategoryBar.getChildCount() > 0) {
                barParams.setMarginStart(getResources().getDimensionPixelSize(
                        R.dimen.report_category_bar_segment_gap));
            }
            bar.setBackgroundResource(R.drawable.bg_report_category_bar_segment);
            bar.setBackgroundTintList(ColorStateList.valueOf(color));
            layoutMonthlyCategoryBar.addView(bar, barParams);

            // 범례 생성
            int percent = (int) Math.round(
                    amount * 100.0 / totalExpense
            );
            String percentLabel = percent == 0 ? "1% 미만" : percent + "%";

            TextView legend = new MaterialTextView(requireContext());

            legend.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    )
            );

            legend.setText(
                    "● " + category.getLabel() + " " + percentLabel
            );

            legend.setTextAppearance(R.style.ReportText_Caption);
            legend.setTextColor(
                    getResources().getColor(R.color.gray500)
            );

            setLegendColor(legend, color);

            layoutMonthlyCategoryLegend.addView(legend);
        }
    }

}
