package com.example.submate.presentation.subscribelist.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.submate.domain.model.BillingCycle;
import com.example.submate.domain.model.InstalledApp;
import com.example.submate.domain.model.SubscribeCategory;
import com.example.submate.domain.model.SubscribeCurrency;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.model.SubscribeStatus;
import com.example.submate.domain.repository.InstalledAppRepository;
import com.example.submate.domain.repository.ResultCallback;
import com.example.submate.domain.repository.SubscribeRepository;
import com.example.submate.presentation.subscribelist.util.ServicePreset;
import com.example.submate.presentation.subscribelist.util.SubscribeFormatter;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.List;
import javax.inject.Inject;

/**
 * 구독 추가/수정/삭제 화면의 상태를 관리합니다.
 * 칩·드롭다운으로 고르는 값은 여기서 LiveData로 보관하고,
 * 키보드로 입력하는 값(서비스명, 결제일, 금액, 메모)은 저장할 때 화면에서 전달받습니다.
 * 저장할 때 서비스명과 같은 이름의 설치된 앱이 있으면 그 앱의 패키지명을 함께 저장합니다. (리포트 사용량 조회용)
 */
@HiltViewModel
public class SubscribeDetailViewModel extends ViewModel {
    public enum Result { ADDED, UPDATED, DELETED, NOT_FOUND }

    public static final long NEW_SUBSCRIBE = -1L;
    public static final int MAX_MONTH = 12;
    public static final int MAX_DAY = 31;
    // 월별 마지막 날 (2월은 윤년을 고려해 29일까지 허용)
    private static final int[] LAST_DAY_OF_MONTH = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public static final int SUGGESTION_LIMIT = 6;

    private final SubscribeRepository repository;
    private final InstalledAppRepository installedAppRepository;

    private final MutableLiveData<SubscribeCategory> category = new MutableLiveData<>(SubscribeCategory.OTT);
    private final MutableLiveData<Boolean> directInput = new MutableLiveData<>(false);
    private final MutableLiveData<SubscribeStatus> status = new MutableLiveData<>(SubscribeStatus.ACTIVE);
    private final MutableLiveData<BillingCycle> billingCycle = new MutableLiveData<>(BillingCycle.MONTHLY);
    private final MutableLiveData<SubscribeCurrency> currency = new MutableLiveData<>(SubscribeCurrency.KRW);
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Result> result = new MutableLiveData<>();

    private long subscribeId = NEW_SUBSCRIBE;
    private boolean initialized = false;
    private boolean changed = false;

    @Inject
    public SubscribeDetailViewModel(SubscribeRepository repository,
                                    InstalledAppRepository installedAppRepository) {
        this.repository = repository;
        this.installedAppRepository = installedAppRepository;
        installedAppRepository.refresh();
    }

    /**
     * 화면을 처음 열 때 한 번만 동작합니다.
     * 수정 모드면 기존 구독을 돌려주고(입력칸 채우기용), 추가 모드이거나 이미 초기화했으면 null을 돌려줍니다.
     */
    public SubscribeItem init(long id) {
        if (initialized) {
            return null;
        }
        initialized = true;
        if (id == NEW_SUBSCRIBE) {
            return null;
        }
        SubscribeItem item = repository.findSubscribe(id);
        if (item == null) {
            result.setValue(Result.NOT_FOUND);
            return null;
        }
        subscribeId = id;
        category.setValue(item.getCategory());
        // 선택지에 없는 서비스명은 직접 입력한 것이므로 바로 고칠 수 있게 합니다.
        directInput.setValue(!ServicePreset.getServices(item.getCategory()).contains(item.getServiceName()));
        status.setValue(item.getStatus());
        billingCycle.setValue(item.getBillingCycle());
        currency.setValue(item.getCurrency());
        return item;
    }

    public boolean isEditMode() {
        return subscribeId != NEW_SUBSCRIBE;
    }

    /** 입력을 시작했는지 (뒤로 갈 때 '작성을 취소하시겠습니까?'를 물어볼지 결정) */
    public boolean isChanged() {
        return changed;
    }

    public void markChanged() {
        changed = true;
    }

    /** 결제일에 월까지 필요한지 (연간 결제 또는 무료 체험 종료일) */
    public boolean needPaymentMonth() {
        return billingCycle.getValue() == BillingCycle.YEARLY || isFreeTrial();
    }

    public boolean isFreeTrial() {
        return currency.getValue() == SubscribeCurrency.FREE_TRIAL;
    }

    /** 설치된 앱 목록이 준비되면 알려줍니다. (서비스명 아래 연결 안내를 다시 그리기 위해 관찰) */
    public LiveData<List<InstalledApp>> getInstalledApps() {
        return installedAppRepository.observeInstalledApps();
    }

    /** 서비스명과 같은 이름의 설치된 앱. 없으면 null */
    public InstalledApp findInstalledApp(String serviceName) {
        return installedAppRepository.findInstalledApp(serviceName);
    }

    /** 직접 입력 중 추천할 설치된 앱 목록 */
    public List<InstalledApp> searchInstalledApps(String keyword) {
        return installedAppRepository.searchInstalledApps(keyword, SUGGESTION_LIMIT);
    }

    /** 카테고리가 실제로 바뀌었으면 true (서비스 선택지가 달라지므로 화면에서 서비스명을 비웁니다) */
    public boolean selectCategory(SubscribeCategory value) {
        if (value == category.getValue()) {
            return false;
        }
        category.setValue(value);
        directInput.setValue(false);
        changed = true;
        return true;
    }

    /** 드롭다운에서 서비스를 골랐을 때 */
    public void selectService() {
        directInput.setValue(false);
        changed = true;
    }

    /** 드롭다운에서 '직접 입력'을 골랐을 때 */
    public void startDirectInput() {
        directInput.setValue(true);
        changed = true;
    }

    public void selectStatus(SubscribeStatus value) {
        status.setValue(value);
        changed = true;
    }

    public void selectBillingCycle(BillingCycle value) {
        billingCycle.setValue(value);
        changed = true;
    }

    public void selectCurrency(SubscribeCurrency value) {
        currency.setValue(value);
        changed = true;
    }

    /** 입력값을 확인하고 저장합니다. 잘못된 값이 있으면 안내 메시지만 보여줍니다. */
    public void save(String serviceText, String monthText, String dayText,
                     String amountText, String memoText) {
        if (Boolean.TRUE.equals(saving.getValue())) {
            return;
        }
        String serviceName = serviceText == null ? "" : serviceText.trim();
        int month = needPaymentMonth() ? SubscribeFormatter.parseNumber(monthText) : 0;
        int day = SubscribeFormatter.parseNumber(dayText);
        double amount = isFreeTrial() ? 0 : SubscribeFormatter.parseAmount(amountText);

        String error = validate(serviceName, month, day, amount);
        if (error != null) {
            message.setValue(error);
            return;
        }

        InstalledApp linkedApp = installedAppRepository.findInstalledApp(serviceName);
        SubscribeItem item = new SubscribeItem(
                subscribeId,
                category.getValue(),
                serviceName,
                status.getValue(),
                billingCycle.getValue(),
                month,
                day,
                currency.getValue(),
                amount,
                memoText == null ? "" : memoText.trim(),
                linkedApp == null ? "" : linkedApp.getPackageName());

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

    /** 문제가 있으면 안내 문구를, 없으면 null을 돌려줍니다. */
    private String validate(String serviceName, int month, int day, double amount) {
        boolean direct = Boolean.TRUE.equals(directInput.getValue());
        String dateName = isFreeTrial() ? "체험 종료일" : "결제일";

        if (serviceName.isEmpty()) {
            return direct ? "서비스명을 입력해주세요." : "서비스명을 선택해주세요.";
        }
        if (needPaymentMonth() && (month < 1 || month > MAX_MONTH)) {
            return dateName + "의 월을 1~12 사이로 입력해주세요.";
        }
        if (day < 1 || day > MAX_DAY) {
            return dateName + "을 1~31 사이로 입력해주세요.";
        }
        if (needPaymentMonth() && day > LAST_DAY_OF_MONTH[month - 1]) {
            return month + "월은 " + LAST_DAY_OF_MONTH[month - 1] + "일까지 있어요.";
        }
        if (!isFreeTrial() && amount <= 0) {
            return "결제 금액을 입력해주세요.";
        }
        return null;
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

    public LiveData<SubscribeCategory> getCategory() {
        return category;
    }

    public LiveData<Boolean> getDirectInput() {
        return directInput;
    }

    public LiveData<SubscribeStatus> getStatus() {
        return status;
    }

    public LiveData<BillingCycle> getBillingCycle() {
        return billingCycle;
    }

    public LiveData<SubscribeCurrency> getCurrency() {
        return currency;
    }

    public LiveData<Boolean> getSaving() {
        return saving;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Result> getResult() {
        return result;
    }
}
