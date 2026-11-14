package com.example.submate.data.service.subscribelist;

import com.example.submate.domain.model.BillingCycle;
import com.example.submate.domain.model.SubscribeCategory;
import com.example.submate.domain.model.SubscribeCurrency;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.model.SubscribeStatus;
import com.example.submate.domain.repository.ResultCallback;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 서버가 준비되기 전에 사용하는 임시 구현체입니다. (앱 메모리에만 저장)
 * NetworkModule.USE_MOCK_SERVER 값을 false로 바꾸면 SubscribeServiceImpl(서버)을 사용합니다.
 */
@Singleton
public class SubscribeMockServiceImpl implements SubscribeService {
    private final List<SubscribeItem> items = new ArrayList<>();
    private long nextId = 1L;

    @Inject
    public SubscribeMockServiceImpl() {
        // 피그마 예시 데이터
        insert(SubscribeCategory.OTT, "넷플릭스", SubscribeStatus.ACTIVE,
                BillingCycle.MONTHLY, 0, 3, SubscribeCurrency.KRW, 4250);
        insert(SubscribeCategory.OTT, "라프텔", SubscribeStatus.ACTIVE,
                BillingCycle.MONTHLY, 0, 15, SubscribeCurrency.KRW, 7450);
        insert(SubscribeCategory.FOOD, "배달의민족", SubscribeStatus.CANCEL_SCHEDULED,
                BillingCycle.MONTHLY, 10, 11, SubscribeCurrency.FREE_TRIAL, 0);
        insert(SubscribeCategory.AI, "Gemini", SubscribeStatus.CANCEL_SCHEDULED,
                BillingCycle.MONTHLY, 0, 27, SubscribeCurrency.KRW, 29000);
        insert(SubscribeCategory.OTT, "YouTube", SubscribeStatus.ACTIVE,
                BillingCycle.YEARLY, 1, 21, SubscribeCurrency.USD, 14.00);
        insert(SubscribeCategory.SHOPPING, "N+스토어", SubscribeStatus.CANCELED,
                BillingCycle.YEARLY, 10, 17, SubscribeCurrency.KRW, 46800);
        insert(SubscribeCategory.MUSIC, "Apple Music", SubscribeStatus.CANCELED,
                BillingCycle.MONTHLY, 0, 4, SubscribeCurrency.KRW, 2250);
    }

    private void insert(SubscribeCategory category, String name, SubscribeStatus status,
                        BillingCycle cycle, int month, int day,
                        SubscribeCurrency currency, double amount) {
        items.add(new SubscribeItem(nextId++, category, name, status, cycle,
                month, day, currency, amount, ""));
    }

    @Override
    public void getSubscribeList(ResultCallback<List<SubscribeItem>> callback) {
        callback.onSuccess(new ArrayList<>(items));
    }

    @Override
    public void addSubscribe(SubscribeItem item, ResultCallback<SubscribeItem> callback) {
        SubscribeItem saved = item.withId(nextId++);
        items.add(saved);
        callback.onSuccess(saved);
    }

    @Override
    public void updateSubscribe(SubscribeItem item, ResultCallback<SubscribeItem> callback) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == item.getId()) {
                items.set(i, item);
                callback.onSuccess(item);
                return;
            }
        }
        callback.onError("구독 정보를 찾을 수 없습니다.");
    }

    @Override
    public void deleteSubscribe(long id, ResultCallback<Void> callback) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == id) {
                items.remove(i);
                callback.onSuccess(null);
                return;
            }
        }
        callback.onError("구독 정보를 찾을 수 없습니다.");
    }
}
