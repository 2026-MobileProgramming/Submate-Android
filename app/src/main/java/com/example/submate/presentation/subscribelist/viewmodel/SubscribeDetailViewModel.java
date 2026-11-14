package com.example.submate.presentation.subscribelist.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.submate.domain.model.BillingCycle;
import com.example.submate.domain.model.SubscribeCategory;
import com.example.submate.domain.model.SubscribeCurrency;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.model.SubscribeStatus;
import com.example.submate.domain.repository.ResultCallback;
import com.example.submate.domain.repository.SubscribeRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

/** 구독 추가/수정/삭제 화면의 상태를 관리합니다. */
@HiltViewModel
public class SubscribeDetailViewModel extends ViewModel {
    public enum Result { ADDED, UPDATED, DELETED }

    public static final long NEW_SUBSCRIBE = -1L;

    private final SubscribeRepository repository;

    private final MutableLiveData<SubscribeCategory> category = new MutableLiveData<>(SubscribeCategory.OTT);
    private final MutableLiveData<String> serviceName = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> directInput = new MutableLiveData<>(false);
    private final MutableLiveData<SubscribeStatus> status = new MutableLiveData<>(SubscribeStatus.ACTIVE);
    private final MutableLiveData<BillingCycle> billingCycle = new MutableLiveData<>(BillingCycle.MONTHLY);
    private final MutableLiveData<int[]> paymentDate = new MutableLiveData<>(new int[]{0, 0});
    private final MutableLiveData<SubscribeCurrency> currency = new MutableLiveData<>(SubscribeCurrency.KRW);
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Result> result = new MutableLiveData<>();

    private long subscribeId = NEW_SUBSCRIBE;
    private boolean initialized = false;
    private boolean changed = false;

    @Inject
    public SubscribeDetailViewModel(SubscribeRepository repository) {
        this.repository = repository;
    }

    /**
     * 화면을 처음 열 때 한 번만 호출합니다.
     * 수정 모드면 기존 구독을 돌려주고(입력칸 채우기용), 추가 모드면 null을 돌려줍니다.
     */
    public SubscribeItem init(long id) {
        if (initialized) {
            return null;
        }
        initialized = true;
        SubscribeItem item = repository.findSubscribe(id);
        if (item == null) {
            return null;
        }
        subscribeId = id;
        category.setValue(item.getCategory());
        serviceName.setValue(item.getServiceName());
        directInput.setValue(false);
        status.setValue(item.getStatus());
        billingCycle.setValue(item.getBillingCycle());
        paymentDate.setValue(new int[]{item.getPaymentMonth(), item.getPaymentDay()});
        currency.setValue(item.getCurrency());
        return item;
    }

    public boolean isEditMode() {
        return subscribeId != NEW_SUBSCRIBE;
    }

    public boolean isChanged() {
        return changed;
    }

    public void markChanged() {
        changed = true;
    }

    /** 결제일에 월까지 필요한지 (연간 결제 또는 무료 체험 종료일) */
    public boolean needPaymentMonth() {
        return billingCycle.getValue() == BillingCycle.YEARLY
                || currency.getValue() == SubscribeCurrency.FREE_TRIAL;
    }

    public void selectCategory(SubscribeCategory value) {
        if (value == category.getValue()) {
            return;
        }
        category.setValue(value);
        // 카테고리가 바뀌면 서비스 선택지도 바뀌므로 선택한 서비스명을 비웁니다.
        serviceName.setValue("");
        directInput.setValue(false);
        changed = true;
    }

    public void selectService(String name) {
        serviceName.setValue(name);
        directInput.setValue(false);
        changed = true;
    }

    public void startDirectInput() {
        serviceName.setValue("");
        directInput.setValue(true);
        changed = true;
    }

    /** 직접 입력 중 글자가 바뀔 때 호출 (LiveData 갱신 없이 값만 저장) */
    public void setDirectServiceName(String name) {
        serviceName.setValue(name);
    }

    public void selectStatus(SubscribeStatus value) {
        status.setValue(value);
        changed = true;
    }

    public void selectBillingCycle(BillingCycle value) {
        if (value == billingCycle.getValue()) {
            return;
        }
        billingCycle.setValue(value);
        resetPaymentDateIfNeeded();
        changed = true;
    }

    public void selectCurrency(SubscribeCurrency value) {
        if (value == currency.getValue()) {
            return;
        }
        boolean beforeNeedMonth = needPaymentMonth();
        currency.setValue(value);
        if (beforeNeedMonth != needPaymentMonth()) {
            paymentDate.setValue(new int[]{0, 0});
        }
        changed = true;
    }

    public void selectPaymentDate(int month, int day) {
        paymentDate.setValue(new int[]{month, day});
        changed = true;
    }

    private void resetPaymentDateIfNeeded() {
        int[] date = paymentDate.getValue();
        if (date == null) {
            return;
        }
        // 월간으로 바뀌면 일만 남기고, 연간으로 바뀌면 월을 다시 고르도록 비웁니다.
        if (needPaymentMonth()) {
            paymentDate.setValue(new int[]{0, 0});
        } else {
            paymentDate.setValue(new int[]{0, date[1]});
        }
    }

    public void save(String amountText, String memo) {
        if (Boolean.TRUE.equals(saving.getValue())) {
            return;
        }
        String name = serviceName.getValue() == null ? "" : serviceName.getValue().trim();
        int[] date = paymentDate.getValue();
        SubscribeCurrency selectedCurrency = currency.getValue();

        if (name.isEmpty()) {
            message.setValue("서비스명을 선택해주세요.");
            return;
        }
        if (date == null || date[1] <= 0 || (needPaymentMonth() && date[0] <= 0)) {
            message.setValue("결제일을 선택해주세요.");
            return;
        }
        double amount = 0;
        if (selectedCurrency != SubscribeCurrency.FREE_TRIAL) {
            amount = parseAmount(amountText);
            if (amount <= 0) {
                message.setValue("결제 금액을 입력해주세요.");
                return;
            }
        }

        SubscribeItem item = new SubscribeItem(
                subscribeId,
                category.getValue(),
                name,
                status.getValue(),
                billingCycle.getValue(),
                needPaymentMonth() ? date[0] : 0,
                date[1],
                selectedCurrency,
                amount,
                memo == null ? "" : memo.trim());

        saving.setValue(true);
        ResultCallback<SubscribeItem> callback = new ResultCallback<SubscribeItem>() {
            @Override
            public void onSuccess(SubscribeItem saved) {
                saving.setValue(false);
                result.setValue(isEditMode() ? Result.UPDATED : Result.ADDED);
            }

            @Override
            public void onError(String error) {
                saving.setValue(false);
                message.setValue(error);
            }
        };
        if (isEditMode()) {
            repository.updateSubscribe(item, callback);
        } else {
            repository.addSubscribe(item, callback);
        }
    }

    public void delete() {
        if (!isEditMode() || Boolean.TRUE.equals(saving.getValue())) {
            return;
        }
        saving.setValue(true);
        repository.deleteSubscribe(subscribeId, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void ignored) {
                saving.setValue(false);
                result.setValue(Result.DELETED);
            }

            @Override
            public void onError(String error) {
                saving.setValue(false);
                message.setValue(error);
            }
        });
    }

    /** 메시지를 보여준 뒤 호출해서 화면 회전 시 다시 뜨지 않게 합니다. */
    public void clearMessage() {
        message.setValue(null);
    }

    private double parseAmount(String text) {
        if (text == null) {
            return 0;
        }
        try {
            return Double.parseDouble(text.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public LiveData<SubscribeCategory> getCategory() { return category; }
    public LiveData<String> getServiceName() { return serviceName; }
    public LiveData<Boolean> getDirectInput() { return directInput; }
    public LiveData<SubscribeStatus> getStatus() { return status; }
    public LiveData<BillingCycle> getBillingCycle() { return billingCycle; }
    public LiveData<int[]> getPaymentDate() { return paymentDate; }
    public LiveData<SubscribeCurrency> getCurrency() { return currency; }
    public LiveData<Boolean> getSaving() { return saving; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Result> getResult() { return result; }
}
