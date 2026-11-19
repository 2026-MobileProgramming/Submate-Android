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

import com.example.submate.R;

public class ReportFragment extends Fragment {

    private ReportViewModel viewModel;

    // 리포트 종류 버튼
    private TextView btnUsageReport;
    private TextView btnExpenseReport;

    // 기간 선택 버튼
    private TextView btnPeriod10Days;
    private TextView btnPeriodMonth;
    private TextView btnPeriodAll;

    // 리포트 콘텐츠
    private View layoutUsageReport;
    private View layoutExpenseReport;

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
        setupReportTypeButtons();
        setupPeriodButtons();

        // 초기 화면 설정
        selectReportType(true);
        selectPeriod(btnPeriod10Days);
    }

    // XML에 있는 View 연결
    private void initViews(View view) {
        btnUsageReport = view.findViewById(R.id.btnUsageReport);
        btnExpenseReport = view.findViewById(R.id.btnExpenseReport);

        btnPeriod10Days = view.findViewById(R.id.btnPeriod10Days);
        btnPeriodMonth = view.findViewById(R.id.btnPeriodMonth);
        btnPeriodAll = view.findViewById(R.id.btnPeriodAll);

        layoutUsageReport = view.findViewById(R.id.layoutUsageReport);
        layoutExpenseReport = view.findViewById(R.id.layoutExpenseReport);
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
                isUsageReport ? View.GONE : View.VISIBLE
        );

        // 선택된 버튼 스타일
        btnUsageReport.setSelected(isUsageReport);
        btnExpenseReport.setSelected(!isUsageReport);

        // 선택된 버튼 색상
        btnUsageReport.setBackgroundColor(
                isUsageReport ? 0xFFFFFFFF : 0x00FFFFFF
        );

        btnExpenseReport.setBackgroundColor(
                isUsageReport ? 0x00FFFFFF : 0xFFFFFFFF
        );

        btnUsageReport.setTextColor(
                isUsageReport ? 0xFF333333 : 0xFF888888
        );

        btnExpenseReport.setTextColor(
                isUsageReport ? 0xFF888888 : 0xFF333333
        );
    }

    // 기간 선택 버튼 설정
    private void setupPeriodButtons() {
        btnPeriod10Days.setOnClickListener(v ->
                selectPeriod(btnPeriod10Days)
        );

        btnPeriodMonth.setOnClickListener(v ->
                selectPeriod(btnPeriodMonth)
        );

        btnPeriodAll.setOnClickListener(v ->
                selectPeriod(btnPeriodAll)
        );
    }

    // 기간 버튼 선택 상태 변경
    private void selectPeriod(TextView selectedButton) {

        TextView[] periodButtons = {
                btnPeriod10Days,
                btnPeriodMonth,
                btnPeriodAll
        };

        for (TextView button : periodButtons) {
            button.setSelected(button == selectedButton);
        }
    }
}