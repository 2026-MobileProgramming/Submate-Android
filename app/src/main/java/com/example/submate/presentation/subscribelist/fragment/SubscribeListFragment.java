package com.example.submate.presentation.subscribelist.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.submate.R;
import com.example.submate.presentation.common.view.SubmateToast;
import com.example.submate.presentation.subscribelist.activity.SubscribeDetailActivity;
import com.example.submate.presentation.subscribelist.adapter.SubscribeListAdapter;
import com.example.submate.presentation.subscribelist.viewmodel.SubscribeFilter;
import com.example.submate.presentation.subscribelist.viewmodel.SubscribeListViewModel;
import dagger.hilt.android.AndroidEntryPoint;

/** 하단 탭 '구독 목록' 화면 */
@AndroidEntryPoint
public class SubscribeListFragment extends Fragment {
    private SubscribeListViewModel viewModel;
    private SubscribeListAdapter adapter;
    private TextView[] filterChips;

    // 상세 화면에서 저장/삭제 후 돌아오면 결과 메시지를 띄웁니다.
    private final ActivityResultLauncher<Intent> detailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (result.getResultCode() == Activity.RESULT_OK && data != null) {
                    String message = data.getStringExtra(SubscribeDetailActivity.EXTRA_RESULT_MESSAGE);
                    if (message != null) {
                        SubmateToast.show(requireActivity(), message);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.screen_subscribe_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SubscribeListViewModel.class);

        setupList(view);
        setupFilterChips(view);

        TextView tvSummary = view.findViewById(R.id.tv_subscribe_summary);
        View emptyView = view.findViewById(R.id.tv_subscribe_empty);

        viewModel.getFilteredList().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            emptyView.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getSummary().observe(getViewLifecycleOwner(), tvSummary::setText);
        viewModel.getAllCount().observe(getViewLifecycleOwner(), count ->
                filterChips[0].setText(SubscribeFilter.ALL.getLabel() + " " + count));
        viewModel.getSelectedFilter().observe(getViewLifecycleOwner(), this::renderFilter);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                SubmateToast.show(requireActivity(), message);
                viewModel.clearErrorMessage();
            }
        });

        view.findViewById(R.id.fab_subscribe_add).setOnClickListener(v ->
                detailLauncher.launch(SubscribeDetailActivity.newIntent(requireContext(),
                        SubscribeDetailActivity.NO_ID)));
        view.findViewById(R.id.iv_subscribe_notification).setOnClickListener(v -> {
            // TODO: 알림 창 화면이 완성되면 연결 (설정 탭 담당)
        });
    }

    private void setupList(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rv_subscribe_list);
        adapter = new SubscribeListAdapter(item ->
                detailLauncher.launch(SubscribeDetailActivity.newIntent(requireContext(), item.getId())));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFilterChips(View view) {
        filterChips = new TextView[]{
                view.findViewById(R.id.tv_filter_all),
                view.findViewById(R.id.tv_filter_active),
                view.findViewById(R.id.tv_filter_scheduled),
                view.findViewById(R.id.tv_filter_canceled)
        };
        SubscribeFilter[] filters = SubscribeFilter.values();
        for (int i = 0; i < filterChips.length; i++) {
            SubscribeFilter filter = filters[i];
            filterChips[i].setText(filter.getLabel());
            filterChips[i].setOnClickListener(v -> viewModel.selectFilter(filter));
        }
    }

    private void renderFilter(SubscribeFilter selected) {
        for (int i = 0; i < filterChips.length; i++) {
            filterChips[i].setSelected(SubscribeFilter.values()[i] == selected);
        }
    }
}
