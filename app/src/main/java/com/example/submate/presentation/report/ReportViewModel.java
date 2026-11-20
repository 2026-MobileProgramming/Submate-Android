
package com.example.submate.presentation.report;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.repository.ResultCallback;
import com.example.submate.domain.repository.SubscribeRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ReportViewModel extends ViewModel {

    private final SubscribeRepository repository;

    // 실제 구독 목록
    private final LiveData<List<SubscribeItem>> subscribeList;

    @Inject
    public ReportViewModel(SubscribeRepository repository) {
        this.repository = repository;

        // Repository의 구독 목록을 관찰
        subscribeList = repository.observeSubscribeList();

        // 실제 구독 데이터 불러오기
        refresh();
    }

    // 구독 데이터 새로고침
    public void refresh() {
        repository.refresh(new ResultCallback<List<SubscribeItem>>() {
            @Override
            public void onSuccess(List<SubscribeItem> result) {
                // Repository가 LiveData를 갱신하므로 별도 처리 불필요
            }

            @Override
            public void onError(String message) {
                // 데이터 조회 실패 시 현재 목록 유지
            }
        });
    }

    // 구독 목록 관찰
    public LiveData<List<SubscribeItem>> getSubscribeList() {
        return subscribeList;
    }

    // 선택한 월에 결제일이 지난 구독 목록 반환
    public List<SubscribeItem> getPaidSubscriptionsForMonth(
            List<SubscribeItem> items,
            int year,
            int month,
            int todayDay
    ) {
        List<SubscribeItem> paidList = new ArrayList<>();

        if (items == null) {
            return paidList;
        }

        for (SubscribeItem item : items) {

            // 무료 체험 및 해지 완료 제외
            if (item.isFreeTrial() || item.isCanceled()) {
                continue;
            }

            // 해당 연월의 실제 결제일 확인
            int paymentDay = item.getPaymentDayIn(year, month);

            // 결제일이 없거나 아직 지나지 않았다면 제외
            if (paymentDay == 0 || paymentDay > todayDay) {
                continue;
            }

            paidList.add(item);
        }

        return paidList;
    }
}
