package com.example.submate.presentation.subscribelist.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.repository.ExchangeRateRepository;
import com.example.submate.domain.repository.ResultCallback;
import com.example.submate.domain.repository.SubscribeRepository;
import com.example.submate.presentation.subscribelist.util.SubscribeFormatter;
import java.util.ArrayList;
import java.util.List;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

/** 구독 목록 화면의 상태(필터, 걸러진 목록, 요약 문구)를 관리합니다. */
@HiltViewModel
public class SubscribeListViewModel extends ViewModel {
    private final SubscribeRepository repository;
    private final LiveData<List<SubscribeItem>> subscribeList;
    private final LiveData<Long> exchangeRateUpdatedAt;
    private final MutableLiveData<SubscribeFilter> selectedFilter =
            new MutableLiveData<>(SubscribeFilter.ALL);
    private final MediatorLiveData<List<SubscribeItem>> filteredList = new MediatorLiveData<>();
    private final MediatorLiveData<String> summary = new MediatorLiveData<>();
    private final MediatorLiveData<Integer> allCount = new MediatorLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public SubscribeListViewModel(SubscribeRepository repository,
                                  ExchangeRateRepository exchangeRateRepository) {
        this.repository = repository;
        subscribeList = repository.observeSubscribeList();
        exchangeRateUpdatedAt = exchangeRateRepository.observeUpdatedAt();

        filteredList.addSource(subscribeList, list -> updateFilteredList());
        filteredList.addSource(selectedFilter, filter -> updateFilteredList());
        summary.addSource(filteredList, list -> summary.setValue(makeSummary(list)));
        allCount.addSource(subscribeList, list -> allCount.setValue(filter(list, SubscribeFilter.ALL).size()));
        refresh();
    }

    public LiveData<List<SubscribeItem>> getFilteredList() {
        return filteredList;
    }

    public LiveData<SubscribeFilter> getSelectedFilter() {
        return selectedFilter;
    }

    /** "전체 5" 칩에 들어갈 개수 */
    public LiveData<Integer> getAllCount() {
        return allCount;
    }

    /** ex) 활성 구독 4개 · 월 ₩42,283 */
    public LiveData<String> getSummary() {
        return summary;
    }

    /** 환율이 바뀐 시각. 값이 바뀌면 목록의 원화 환산 금액을 다시 그려야 합니다. */
    public LiveData<Long> getExchangeRateUpdatedAt() {
        return exchangeRateUpdatedAt;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /** 메시지를 보여준 뒤 호출해서 화면 회전 시 다시 뜨지 않게 합니다. */
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    public void selectFilter(SubscribeFilter filter) {
        selectedFilter.setValue(filter);
    }

    public void refresh() {
        repository.refresh(new ResultCallback<List<SubscribeItem>>() {
            @Override
            public void onSuccess(List<SubscribeItem> result) {
                // 목록은 subscribeList(LiveData)로 전달됩니다.
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    private void updateFilteredList() {
        filteredList.setValue(filter(subscribeList.getValue(), selectedFilter.getValue()));
    }

    private List<SubscribeItem> filter(List<SubscribeItem> source, SubscribeFilter filter) {
        List<SubscribeItem> result = new ArrayList<>();
        if (source != null && filter != null) {
            for (SubscribeItem item : source) {
                if (filter.matches(item)) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    /** 무료체험은 개수와 금액에서 빼고, 연간 요금은 1/12로 나누어 월 금액을 계산합니다. */
    private String makeSummary(List<SubscribeItem> list) {
        int count = 0;
        long monthly = 0L;
        for (SubscribeItem item : list) {
            if (item.isFreeTrial()) {
                continue;
            }
            count++;
            monthly += item.getMonthlyKrwAmount();
        }
        String prefix = selectedFilter.getValue() == SubscribeFilter.CANCELED ? "비활성 구독 " : "활성 구독 ";
        return prefix + count + "개 · 월 " + SubscribeFormatter.formatKrw(monthly);
    }
}
