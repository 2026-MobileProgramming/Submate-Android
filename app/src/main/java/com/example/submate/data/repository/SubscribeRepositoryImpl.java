package com.example.submate.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.repository.SubscribeRepository;
import java.util.List;
import javax.inject.Inject;
import com.example.submate.data.service.SubscribeService;

public class SubscribeRepositoryImpl implements SubscribeRepository {
    private final MutableLiveData<List<SubscribeItem>> subscribeList = new MutableLiveData<>();

    @Inject
    public SubscribeRepositoryImpl(SubscribeService service) {
        subscribeList.setValue(service.getSubscribeList());
    }

    @Override
    public LiveData<List<SubscribeItem>> observeSubscribeList() {
        return subscribeList;
    }
}
