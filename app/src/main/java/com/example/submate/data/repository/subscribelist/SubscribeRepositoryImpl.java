package com.example.submate.data.repository.subscribelist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.submate.data.service.subscribelist.SubscribeService;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.repository.ResultCallback;
import com.example.submate.domain.repository.SubscribeRepository;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/** 서버에서 받은 구독 목록을 보관하고, 변경될 때마다 화면에 알려줍니다. */
public class SubscribeRepositoryImpl implements SubscribeRepository {
    private final SubscribeService service;
    private final MutableLiveData<List<SubscribeItem>> subscribeList =
            new MutableLiveData<>(new ArrayList<>());

    @Inject
    public SubscribeRepositoryImpl(SubscribeService service) {
        this.service = service;
    }

    @Override
    public LiveData<List<SubscribeItem>> observeSubscribeList() {
        return subscribeList;
    }

    @Override
    public void refresh(ResultCallback<List<SubscribeItem>> callback) {
        service.getSubscribeList(new ResultCallback<List<SubscribeItem>>() {
            @Override
            public void onSuccess(List<SubscribeItem> result) {
                subscribeList.setValue(new ArrayList<>(result));
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public SubscribeItem findSubscribe(long id) {
        for (SubscribeItem item : currentList()) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    @Override
    public void addSubscribe(SubscribeItem item, ResultCallback<SubscribeItem> callback) {
        service.addSubscribe(item, new ResultCallback<SubscribeItem>() {
            @Override
            public void onSuccess(SubscribeItem saved) {
                List<SubscribeItem> list = currentList();
                list.add(saved);
                subscribeList.setValue(list);
                callback.onSuccess(saved);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public void updateSubscribe(SubscribeItem item, ResultCallback<SubscribeItem> callback) {
        service.updateSubscribe(item, new ResultCallback<SubscribeItem>() {
            @Override
            public void onSuccess(SubscribeItem saved) {
                List<SubscribeItem> list = currentList();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getId() == saved.getId()) {
                        list.set(i, saved);
                    }
                }
                subscribeList.setValue(list);
                callback.onSuccess(saved);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public void deleteSubscribe(long id, ResultCallback<Void> callback) {
        service.deleteSubscribe(id, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                List<SubscribeItem> list = currentList();
                for (int i = list.size() - 1; i >= 0; i--) {
                    if (list.get(i).getId() == id) {
                        list.remove(i);
                    }
                }
                subscribeList.setValue(list);
                callback.onSuccess(null);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private List<SubscribeItem> currentList() {
        List<SubscribeItem> value = subscribeList.getValue();
        return value == null ? new ArrayList<>() : new ArrayList<>(value);
    }
}
