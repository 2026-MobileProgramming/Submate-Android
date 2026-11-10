package com.example.submate.presentation.subscribe;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.repository.SubscribeRepository;
import java.util.List;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@HiltViewModel
public class SubscribeListViewModel extends ViewModel {
    private final LiveData<List<SubscribeItem>> subscribeList;

    @Inject
    public SubscribeListViewModel(SubscribeRepository repository) {
        subscribeList = repository.observeSubscribeList();
    }

    public LiveData<List<SubscribeItem>> getSubscribeList() {
        return subscribeList;
    }
}
