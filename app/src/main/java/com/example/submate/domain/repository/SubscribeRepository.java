package com.example.submate.domain.repository;

import androidx.lifecycle.LiveData;
import com.example.submate.domain.model.SubscribeItem;
import java.util.List;

public interface SubscribeRepository {
    LiveData<List<SubscribeItem>> observeSubscribeList();

    /** 서버에서 구독 목록을 다시 불러옵니다. */
    void refresh(ResultCallback<List<SubscribeItem>> callback);

    /** 이미 불러온 목록에서 id로 구독을 찾습니다. 없으면 null */
    SubscribeItem findSubscribe(long id);

    void addSubscribe(SubscribeItem item, ResultCallback<SubscribeItem> callback);

    void updateSubscribe(SubscribeItem item, ResultCallback<SubscribeItem> callback);

    void deleteSubscribe(long id, ResultCallback<Void> callback);
}
