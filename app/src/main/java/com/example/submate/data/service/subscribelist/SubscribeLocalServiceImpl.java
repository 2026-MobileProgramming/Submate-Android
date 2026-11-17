package com.example.submate.data.service.subscribelist;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.submate.data.dto.subscribelist.SubscribeMapper;
import com.example.submate.data.dto.subscribelist.SubscribeResponse;
import com.example.submate.domain.model.BillingCycle;
import com.example.submate.domain.model.SubscribeCategory;
import com.example.submate.domain.model.SubscribeCurrency;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.model.SubscribeStatus;
import com.example.submate.domain.repository.ResultCallback;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * 서버 대신 기기 안(SharedPreferences)에 구독 목록을 저장하는 구현체입니다.
 * 서버와 같은 JSON 형식으로 저장하므로 앱을 껐다 켜도 목록이 유지됩니다.
 * NetworkModule.USE_LOCAL_DATA 값을 false로 바꾸면 SubscribeServiceImpl(서버)을 사용합니다.
 * 알림 작업 같은 백그라운드 스레드와 화면이 동시에 불러도 목록·id가 꼬이지 않도록 lock으로 막습니다.
 */
public class SubscribeLocalServiceImpl implements SubscribeService {
    private static final String PREFS_NAME = "subscribe_list";
    private static final String KEY_ITEMS = "items";
    private static final String NOT_FOUND = "구독 정보를 찾을 수 없습니다.";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private final List<SubscribeItem> items = new ArrayList<>();
    private long nextId = 1L;
    // items, nextId를 바꿀 때 잡는 잠금. 콜백은 잠금을 푼 뒤에 부릅니다. (콜백 안에서 다른 잠금을 잡아도 서로 기다리며 멈추지 않게)
    private final Object lock = new Object();

    @Inject
    public SubscribeLocalServiceImpl(@ApplicationContext Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!load()) {
            insertSampleItems();
            save();
        }
    }

    @Override
    public void getSubscribeList(ResultCallback<List<SubscribeItem>> callback) {
        List<SubscribeItem> copy;
        synchronized (lock) {
            copy = new ArrayList<>(items);
        }
        callback.onSuccess(copy);
    }

    @Override
    public void addSubscribe(SubscribeItem item, ResultCallback<SubscribeItem> callback) {
        SubscribeItem saved;
        synchronized (lock) {
            saved = item.withId(nextId++);
            items.add(saved);
            save();
        }
        callback.onSuccess(saved);
    }

    @Override
    public void updateSubscribe(SubscribeItem item, ResultCallback<SubscribeItem> callback) {
        boolean found;
        synchronized (lock) {
            int index = indexOf(item.getId());
            found = index >= 0;
            if (found) {
                items.set(index, item);
                save();
            }
        }
        if (found) {
            callback.onSuccess(item);
        } else {
            callback.onError(NOT_FOUND);
        }
    }

    @Override
    public void deleteSubscribe(long id, ResultCallback<Void> callback) {
        boolean found;
        synchronized (lock) {
            int index = indexOf(id);
            found = index >= 0;
            if (found) {
                items.remove(index);
                save();
            }
        }
        if (found) {
            callback.onSuccess(null);
        } else {
            callback.onError(NOT_FOUND);
        }
    }

    private int indexOf(long id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    /** 저장된 목록을 읽습니다. 저장된 적이 없으면(앱 첫 실행) false를 돌려줍니다. */
    private boolean load() {
        String json = prefs.getString(KEY_ITEMS, null);
        if (json == null) {
            return false;
        }
        List<SubscribeResponse> saved;
        try {
            saved = gson.fromJson(json, new TypeToken<List<SubscribeResponse>>() { }.getType());
        } catch (JsonSyntaxException e) {
            return false;
        }
        if (saved == null) {
            return false;
        }
        for (SubscribeResponse response : saved) {
            items.add(SubscribeMapper.toItem(response));
            nextId = Math.max(nextId, response.id + 1);
        }
        return true;
    }

    private void save() {
        List<SubscribeResponse> responses = new ArrayList<>();
        for (SubscribeItem item : items) {
            responses.add(SubscribeMapper.toResponse(item));
        }
        prefs.edit().putString(KEY_ITEMS, gson.toJson(responses)).apply();
    }

    /** 앱을 처음 실행했을 때 보여줄 피그마 예시 데이터 */
    private void insertSampleItems() {
        insert(SubscribeCategory.OTT, "Netflix", SubscribeStatus.ACTIVE,
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
}
