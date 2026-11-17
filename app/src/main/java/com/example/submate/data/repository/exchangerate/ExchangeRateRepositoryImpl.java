package com.example.submate.data.repository.exchangerate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.submate.data.service.exchangerate.ExchangeRateService;
import com.example.submate.domain.model.SubscribeCurrency;
import com.example.submate.domain.repository.ExchangeRateRepository;
import com.example.submate.domain.repository.ResultCallback;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;

/**
 * 환율 API에서 받은 환율을 SubscribeCurrency에 반영합니다.
 * 마지막으로 받은 환율은 기기에 저장해 두어, 인터넷이 안 될 때도 그 값을 사용합니다.
 */
public class ExchangeRateRepositoryImpl implements ExchangeRateRepository {
    private static final String PREFS_NAME = "exchange_rate";
    private static final String KEY_UPDATED_AT = "updated_at";
    private static final String KEY_RATE_PREFIX = "rate_";
    // 환율은 하루에 한 번 바뀌므로 1시간 안에는 다시 요청하지 않습니다.
    private static final long REFRESH_INTERVAL = 60 * 60 * 1000L;

    private final ExchangeRateService service;
    private final SharedPreferences prefs;
    private final MutableLiveData<Long> updatedAt;
    private volatile long lastRequestedAt;

    @Inject
    public ExchangeRateRepositoryImpl(@ApplicationContext Context context,
                                      ExchangeRateService service) {
        this.service = service;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long savedAt = restoreSavedRates();
        // 생성자가 백그라운드 스레드에서 불려도 안전하도록 setValue 대신 초기값으로 넣습니다.
        updatedAt = savedAt == 0L ? new MutableLiveData<>() : new MutableLiveData<>(savedAt);
    }

    @Override
    public LiveData<Long> observeUpdatedAt() {
        return updatedAt;
    }

    @Override
    public void refresh() {
        long now = System.currentTimeMillis();
        if (now - lastRequestedAt < REFRESH_INTERVAL) {
            return;
        }
        lastRequestedAt = now;
        for (SubscribeCurrency currency : SubscribeCurrency.values()) {
            if (currency.isForeign()) {
                requestRate(currency);
            }
        }
    }

    private void requestRate(SubscribeCurrency currency) {
        service.getKrwRate(currency, new ResultCallback<Double>() {
            @Override
            public void onSuccess(Double rate) {
                long now = System.currentTimeMillis();
                currency.setKrwRate(rate);
                prefs.edit()
                        .putFloat(KEY_RATE_PREFIX + currency.name(), rate.floatValue())
                        .putLong(KEY_UPDATED_AT, now)
                        .apply();
                publish(now);
            }

            @Override
            public void onError(String message) {
                // 실패하면 기존 환율을 그대로 쓰고, 다음 refresh() 때 다시 시도합니다.
                lastRequestedAt = 0L;
            }
        });
    }

    /** 저장해 둔 환율을 SubscribeCurrency에 반영하고, 저장된 시각을 돌려줍니다. 저장된 적이 없으면 0 */
    private long restoreSavedRates() {
        long savedAt = prefs.getLong(KEY_UPDATED_AT, 0L);
        if (savedAt == 0L) {
            return 0L;
        }
        for (SubscribeCurrency currency : SubscribeCurrency.values()) {
            currency.setKrwRate(prefs.getFloat(KEY_RATE_PREFIX + currency.name(), 0f));
        }
        lastRequestedAt = savedAt;
        return savedAt;
    }

    /** LiveData.setValue는 메인 스레드에서만 쓸 수 있어 나눠서 처리합니다. */
    private void publish(long time) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            updatedAt.setValue(time);
        } else {
            updatedAt.postValue(time);
        }
    }
}
