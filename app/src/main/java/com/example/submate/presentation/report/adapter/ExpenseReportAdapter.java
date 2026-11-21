
package com.example.submate.presentation.report.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submate.R;
import com.example.submate.presentation.report.model.ExpenseReportItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseReportAdapter
        extends RecyclerView.Adapter<ExpenseReportAdapter.ExpenseViewHolder> {

    private List<ExpenseReportItem> expenseList;

    // 생성자
    public ExpenseReportAdapter(List<ExpenseReportItem> expenseList) {
        this.expenseList = new ArrayList<>(expenseList);
    }

    // ViewHolder 생성
    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_report, parent, false);

        return new ExpenseViewHolder(view);
    }

    // 데이터 연결
    @Override
    public void onBindViewHolder(
            @NonNull ExpenseViewHolder holder,
            int position
    ) {
        ExpenseReportItem item = expenseList.get(position);

        // 서비스명
        holder.tvExpenseServiceName.setText(item.getServiceName());

        // 서비스 아이콘
        holder.tvExpenseServiceIcon.setText(item.getIconText());

        // 구독 금액 (예: ₩5,000)
        holder.tvExpenseServicePrice.setText(
                String.format(Locale.KOREA, "₩%,d", item.getPrice())
        );

        // 아이콘 배경색
        holder.tvExpenseServiceIcon.setBackgroundTintList(
                ColorStateList.valueOf(item.getIconColor())
        );

        // 지출 비율
        holder.progressExpenseAmount.setMax(100);
        holder.progressExpenseAmount.setProgress(
                item.getExpensePercent()
        );

        // 프로그레스바 색상
        holder.progressExpenseAmount.setProgressTintList(
                ColorStateList.valueOf(item.getProgressColor())
        );
    }

    // 항목 개수
    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    // 지출 목록 갱신
    public void setExpenseList(List<ExpenseReportItem> newList) {
        expenseList.clear();
        expenseList.addAll(newList);
        notifyDataSetChanged();
    }

    // ViewHolder
    static class ExpenseViewHolder extends RecyclerView.ViewHolder {

        TextView tvExpenseServiceIcon;
        TextView tvExpenseServiceName;
        TextView tvExpenseServicePrice;
        ProgressBar progressExpenseAmount;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);

            tvExpenseServiceIcon =
                    itemView.findViewById(R.id.tvExpenseServiceIcon);

            tvExpenseServiceName =
                    itemView.findViewById(R.id.tvExpenseServiceName);

            tvExpenseServicePrice =
                    itemView.findViewById(R.id.tvExpenseServicePrice);

            progressExpenseAmount =
                    itemView.findViewById(R.id.progressExpenseAmount);
        }
    }
}
