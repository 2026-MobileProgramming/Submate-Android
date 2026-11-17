package com.example.submate.data.service.subscribelist;

import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.repository.ResultCallback;
import java.util.List;

/** 구독 데이터를 가져오는 외부 데이터 소스(서버)의 경계입니다. */
public interface SubscribeService {
    void getSubscribeList(ResultCallback<List<SubscribeItem>> callback);

    void addSubscribe(SubscribeItem item, ResultCallback<SubscribeItem> callback);

    void updateSubscribe(SubscribeItem item, ResultCallback<SubscribeItem> callback);

    void deleteSubscribe(long id, ResultCallback<Void> callback);
}
