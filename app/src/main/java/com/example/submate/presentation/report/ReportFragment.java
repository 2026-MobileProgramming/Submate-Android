package com.example.submate.presentation.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        setupChartColors();

        setupUsageReportRecyclerView();
        setupExpenseReportRecyclerView();

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

    private void setupChartColors() {
        colorOtt = getResources().getColor(R.color.pastel_red);
        colorAi = getResources().getColor(R.color.pastel_blue);
        colorFood = getResources().getColor(R.color.pastel_yellow);
    }

}