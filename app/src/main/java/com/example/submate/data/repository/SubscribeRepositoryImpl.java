package com.example.submate.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.repository.SubscribeRepository;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

public class SubscribeRepositoryImpl implements SubscribeRepository {
    private final MutableLiveData<List<SubscribeItem>> subscribeList = new MutableLiveData<>();

    @Inject
    public SubscribeRepositoryImpl() {
        subscribeList.setValue(Arrays.asList(
                new SubscribeItem(1L, "Sample subscription", "2026-12-01")
        ));
    }

    @Override
    public LiveData<List<SubscribeItem>> observeSubscribeList() {
        return subscribeList;
    }
}
