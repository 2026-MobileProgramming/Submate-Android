package com.example.submate.presentation.subscribelist.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.repository.ResultCallback;
import com.example.submate.domain.repository.SubscribeRepository;
import com.example.submate.presentation.subscribelist.util.SubscribeFormatter;
import java.util.ArrayList;
import java.util.List;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@HiltViewModel
public class SubscribeListViewModel extends ViewModel {
    private final SubscribeRepository repository;
    private final LiveData<List<SubscribeItem>> subscribeList;
    private final MutableLiveData<SubscribeFilter> selectedFilter =
            new MutableLiveData<>(SubscribeFilter.ALL);
    private final MediatorLiveData<List<SubscribeItem>> filteredList = new MediatorLiveData<>();
    private final MediatorLiveData<String> summary = new MediatorLiveData<>();
    private final MediatorLiveData<Integer> allCount = new MediatorLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public SubscribeListViewModel(SubscribeRepository repository) {
        this.repository = repository;
        subscribeList = repository.observeSubscribeList();

        filteredList.addSource(subscribeList, list -> update());
        filteredList.addSource(selectedFilter, filter -> update());
        summary.addSource(filteredList, list -> summary.setValue(makeSummary(list)));
        allCount.addSource(subscribeList, list -> allCount.setValue(count(list)));
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

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

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
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    private void update() {
        SubscribeFilter filter = selectedFilter.getValue();
        List<SubscribeItem> source = subscribeList.getValue();
        List<SubscribeItem> result = new ArrayList<>();
        if (source != null && filter != null) {
            for (SubscribeItem item : source) {
                if (filter.matches(item)) {
                    result.add(item);
                }
            }
        }
        filteredList.setValue(result);
    }

    private int count(List<SubscribeItem> list) {
        int count = 0;
        if (list != null) {
            for (SubscribeItem item : list) {
                if (SubscribeFilter.ALL.matches(item)) {
                    count++;
                }
            }
        }
        return count;
    }

    /** 무료체험은 개수와 금액에서 빼고, 연간 요금은 1/12로 나누어 월 금액을 계산합니다. */
    private String makeSummary(List<SubscribeItem> list) {
        int count = 0;
        long monthly = 0L;
        if (list != null) {
            for (SubscribeItem item : list) {
                if (item.isFreeTrial()) {
                    continue;
                }
                count++;
                monthly += item.getMonthlyKrwAmount();
            }
        }
        String prefix = selectedFilter.getValue() == SubscribeFilter.CANCELED ? "비활성 구독 " : "활성 구독 ";
        return prefix + count + "개 · 월 " + SubscribeFormatter.formatKrw(monthly);
    }
}
