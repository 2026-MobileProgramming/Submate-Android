package com.example.submate.data.repository.subscribelist;

import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.example.submate.data.service.subscribelist.SubscribeService;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.repository.ExchangeRateRepository;
import com.example.submate.domain.repository.ResultCallback;
import com.example.submate.domain.repository.SubscribeRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

/**
 * 불러온 구독 목록을 보관하고, 목록이나 환율이 바뀔 때마다 화면에 알려줍니다.
 * 화면이 아닌 곳(알림 작업 등 백그라운드 스레드)에서 불러도 안전하도록,
 * 목록은 내부 변수(current)에 먼저 저장하고 LiveData에는 스레드에 맞는 방법으로 전달합니다.
 * 목록을 언제 불러올지는 Repository가 아니라 ViewModel이 정합니다(refresh 호출).
 */
public class SubscribeRepositoryImpl implements SubscribeRepository {
    private final SubscribeService service;
    private final ExchangeRateRepository exchangeRateRepository;
    private final MediatorLiveData<List<SubscribeItem>> subscribeList =
            new MediatorLiveData<>(Collections.emptyList());
    // 항상 최신 목록. 외부에서 고칠 수 없도록 읽기 전용 목록으로 보관합니다.
    private volatile List<SubscribeItem> current = Collections.emptyList();

    @Inject
    public SubscribeRepositoryImpl(SubscribeService service,
                                   ExchangeRateRepository exchangeRateRepository) {
        this.service = service;
        this.exchangeRateRepository = exchangeRateRepository;
        // 환율이 바뀌면 원화 환산 금액이 달라지므로 목록을 다시 알려줍니다.
        subscribeList.addSource(exchangeRateRepository.observeUpdatedAt(),
                updatedAt -> publish(current));
        // 생성자에서는 네트워크 요청을 하지 않습니다. 목록 조회는 화면(ViewModel)이 refresh()로 요청합니다.
    }

    @Override
    public LiveData<List<SubscribeItem>> observeSubscribeList() {
        return subscribeList;
    }

    @Override
    public List<SubscribeItem> getSubscribeList() {
        return current;
    }

    @Override
    public void refresh(ResultCallback<List<SubscribeItem>> callback) {
        exchangeRateRepository.refresh();
        service.getSubscribeList(new ResultCallback<List<SubscribeItem>>() {
            @Override
            public void onSuccess(List<SubscribeItem> result) {
                publish(result);
                if (callback != null) {
                    callback.onSuccess(current);
                }
            }

            @Override
            public void onError(String message) {
                if (callback != null) {
                    callback.onError(message);
                }
            }
        });
    }

    @Override
    public SubscribeItem findSubscribe(long id) {
        for (SubscribeItem item : current) {
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
                List<SubscribeItem> list = new ArrayList<>(current);
                list.add(saved);
                publish(list);
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
                List<SubscribeItem> list = new ArrayList<>(current);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getId() == saved.getId()) {
                        list.set(i, saved);
                    }
                }
                publish(list);
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
                List<SubscribeItem> list = new ArrayList<>(current);
                for (int i = list.size() - 1; i >= 0; i--) {
                    if (list.get(i).getId() == id) {
                        list.remove(i);
                    }
                }
                publish(list);
                callback.onSuccess(null);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    /** 최신 목록을 저장하고 화면에 알립니다. LiveData.setValue는 메인 스레드에서만 쓸 수 있어 나눠서 처리합니다. */
    private void publish(List<SubscribeItem> list) {
        current = Collections.unmodifiableList(new ArrayList<>(list));
        if (Looper.myLooper() == Looper.getMainLooper()) {
            subscribeList.setValue(current);
        } else {
            subscribeList.postValue(current);
        }
    }
}
