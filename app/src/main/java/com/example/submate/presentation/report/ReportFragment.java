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
import com.example.submate.presentation.report.model.UsageReportItem;
import com.example.submate.presentation.report.adapter.ExpenseReportAdapter;
import com.example.submate.presentation.report.model.ExpenseReportItem;

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

    // 리포트 콘텐츠
    private View layoutUsageReport;
    private View layoutExpenseReport;

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
        setupUsageReportRecyclerView();
        setupExpenseReportRecyclerView();

        setupReportTypeButtons();
        setupPeriodButtons();

        // 초기 화면 설정
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

        rvUsageReport = view.findViewById(R.id.rvUsageReport);
        rvExpenseReport = view.findViewById(R.id.rvExpenseReport);

        tvReportDescription = view.findViewById(R.id.tvReportDescription);
    }

    // 사용량 / 누적 지출 버튼 설정
    private void setupReportTypeButtons() {
        btnUsageReport.setOnClickListener(v -> selectReportType(true));

        btnExpenseReport.setOnClickListener(v -> selectReportType(false));
    }

    // 리포트 종류 변경
    private void selectReportType(boolean isUsageReport) {

        // 콘텐츠 화면 변경
        layoutUsageReport.setVisibility(
                isUsageReport ? View.VISIBLE : View.GONE
        );

        layoutExpenseReport.setVisibility(
                (!isUsageReport && btnPeriodMonth.isSelected())
                        ? View.VISIBLE
                        : View.GONE
        );
        tvReportDescription.setText(
                isUsageReport ? "앱 사용 시간 (월)" : "월별 구독료"
        );

        // 누적 지출 탭에서만 기간 선택 표시
        layoutPeriod.setVisibility(
                isUsageReport ? View.GONE : View.VISIBLE
        );

        // 선택된 버튼 스타일
        btnUsageReport.setSelected(isUsageReport);
        btnExpenseReport.setSelected(!isUsageReport);

        // 선택된 버튼 배경
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

        // 선택된 버튼 색상
        btnUsageReport.setTextColor(
                isUsageReport
                        ? getResources().getColor(R.color.appblack)
                        : getResources().getColor(R.color.gray500)
        );

        btnExpenseReport.setTextColor(
                isUsageReport
                        ? getResources().getColor(R.color.gray500)
                        : getResources().getColor(R.color.appblack)
        );
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
        TextView[] periodButtons = {
                btnPeriodMonth,
                btnPeriodYear,
                btnPeriodAll
        };

        for (TextView button : periodButtons) {
            button.setSelected(button == selectedButton);
        }

        // 누적 지출 탭에서는 '월'을 선택했을 때만 리포트 표시
        if (btnExpenseReport.isSelected()) {
            layoutExpenseReport.setVisibility(
                    selectedButton == btnPeriodMonth ? View.VISIBLE : View.GONE
            );
        }
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
}