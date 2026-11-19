
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
import com.example.submate.presentation.report.model.UsageReportItem;

import java.util.ArrayList;
import java.util.List;

public class UsageReportAdapter
        extends RecyclerView.Adapter<UsageReportAdapter.UsageReportViewHolder> {

    private List<UsageReportItem> usageList = new ArrayList<>();

    public UsageReportAdapter(List<UsageReportItem> usageList) {
        this.usageList = usageList;
    }

    @NonNull
    @Override
    public UsageReportViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usage_report, parent, false);

        return new UsageReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull UsageReportViewHolder holder,
            int position
    ) {
        UsageReportItem item = usageList.get(position);

        holder.tvServiceIcon.setText(item.getIconText());
        holder.tvServiceName.setText(item.getServiceName());
        holder.tvServiceUsageTime.setText(item.getUsageTime());

        holder.progressServiceUsage.setProgress(item.getUsagePercent());

        // 서비스 아이콘 배경색
        holder.tvServiceIcon.setBackgroundTintList(
                ColorStateList.valueOf(item.getIconColor())
        );

        // 사용량 막대 색상
        holder.progressServiceUsage.setProgressTintList(
                ColorStateList.valueOf(item.getProgressColor())
        );
    }

    @Override
    public int getItemCount() {
        return usageList.size();
    }

    // 데이터 목록 변경 시 호출
    public void setUsageList(List<UsageReportItem> newUsageList) {
        usageList = new ArrayList<>(newUsageList);
        notifyDataSetChanged();
    }

    static class UsageReportViewHolder extends RecyclerView.ViewHolder {

        TextView tvServiceIcon;
        TextView tvServiceName;
        TextView tvServiceUsageTime;
        ProgressBar progressServiceUsage;

        public UsageReportViewHolder(@NonNull View itemView) {
            super(itemView);

            tvServiceIcon = itemView.findViewById(R.id.tvServiceIcon);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceUsageTime = itemView.findViewById(R.id.tvServiceUsageTime);
            progressServiceUsage = itemView.findViewById(R.id.progressServiceUsage);
        }
    }
}
