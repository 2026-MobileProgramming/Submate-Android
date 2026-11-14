package com.example.submate.presentation.subscribelist.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.submate.R;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.model.SubscribeStatus;
import com.example.submate.presentation.subscribelist.util.SubscribeFormatter;

/** 구독 목록 RecyclerView 어댑터 */
public class SubscribeListAdapter extends ListAdapter<SubscribeItem, SubscribeListAdapter.SubscribeViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(SubscribeItem item);
    }

    private final OnItemClickListener listener;

    public SubscribeListAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubscribeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subscribe_list_item, parent, false);
        return new SubscribeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscribeViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class SubscribeViewHolder extends RecyclerView.ViewHolder {
        private final View viewCategoryDot;
        private final TextView tvName;
        private final TextView tvSchedule;
        private final TextView tvPrice;
        private final TextView tvConvertedPrice;
        private final TextView tvStatus;

        SubscribeViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCategoryDot = itemView.findViewById(R.id.view_category_dot);
            tvName = itemView.findViewById(R.id.tv_subscribe_name);
            tvSchedule = itemView.findViewById(R.id.tv_subscribe_schedule);
            tvPrice = itemView.findViewById(R.id.tv_subscribe_price);
            tvConvertedPrice = itemView.findViewById(R.id.tv_subscribe_converted_price);
            tvStatus = itemView.findViewById(R.id.tv_subscribe_status);
        }

        void bind(SubscribeItem item, OnItemClickListener listener) {
            viewCategoryDot.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                    itemView.getContext(), SubscribeFormatter.categoryColor(item.getCategory()))));
            tvName.setText(item.getServiceName());
            tvSchedule.setText(SubscribeFormatter.formatSchedule(item));
            tvPrice.setText(SubscribeFormatter.formatPrice(item));

            String converted = SubscribeFormatter.formatConvertedPrice(item);
            tvConvertedPrice.setText(converted);
            tvConvertedPrice.setVisibility(converted.isEmpty() ? View.GONE : View.VISIBLE);

            bindStatus(item.getStatus());
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        private void bindStatus(SubscribeStatus status) {
            tvStatus.setText(status.getLabel());
            if (status == SubscribeStatus.ACTIVE) {
                tvStatus.setBackgroundResource(R.drawable.bg_subscribe_tag_active);
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.pri700));
            } else if (status == SubscribeStatus.CANCEL_SCHEDULED) {
                tvStatus.setBackgroundResource(R.drawable.bg_subscribe_tag_scheduled);
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.error500));
            } else {
                tvStatus.setBackgroundResource(R.drawable.bg_subscribe_tag_canceled);
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.gray600));
            }
        }
    }

    private static final DiffUtil.ItemCallback<SubscribeItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SubscribeItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull SubscribeItem oldItem, @NonNull SubscribeItem newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull SubscribeItem oldItem, @NonNull SubscribeItem newItem) {
                    // 수정 시 항상 새 객체가 만들어지므로 같은 객체인지로 비교합니다.
                    return oldItem == newItem;
                }
            };
}
