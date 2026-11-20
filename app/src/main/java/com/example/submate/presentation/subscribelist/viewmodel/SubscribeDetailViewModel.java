package com.example.submate.presentation.subscribelist.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
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
import com.example.submate.presentation.subscribelist.util.ServiceNameMatcher;
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
 * 칩·드롭다운 선택값과 '작성 중' 여부는 SavedStateHandle에 보관해서, 다른 앱에 간 사이 시스템이 앱을 종료해도
 * 돌아왔을 때 입력칸 글자(EditText가 스스로 복원)와 선택값이 서로 어긋나지 않게 합니다.
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

    // SavedStateHandle에 저장하는 값의 이름
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_DIRECT_INPUT = "direct_input";
    private static final String KEY_STATUS = "status";
    private static final String KEY_BILLING_CYCLE = "billing_cycle";
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_CHANGED = "changed";
    // 수정 모드에서 기존 구독의 값을 선택값에 한 번 채웠는지 (복원 후 다시 덮어쓰지 않기 위해)
    private static final String KEY_CHOICES_FILLED = "choices_filled";

    private final SubscribeRepository repository;
    private final InstalledAppRepository installedAppRepository;
    private final SavedStateHandle savedState;

    private final MutableLiveData<SubscribeCategory> category;
    private final MutableLiveData<Boolean> directInput;
    private final MutableLiveData<SubscribeStatus> status;
    private final MutableLiveData<BillingCycle> billingCycle;
    private final MutableLiveData<SubscribeCurrency> currency;
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Result> result = new MutableLiveData<>();
    private final MutableLiveData<SubscribeItem> editingItem = new MutableLiveData<>();

    private long subscribeId = NEW_SUBSCRIBE;
    private boolean initialized = false;

    @Inject
    public SubscribeDetailViewModel(SubscribeRepository repository,
                                    InstalledAppRepository installedAppRepository,
                                    SavedStateHandle savedState) {
        this.repository = repository;
        this.installedAppRepository = installedAppRepository;
        this.savedState = savedState;
        // 처음 열면 기본값, 앱이 종료됐다가 복원되면 마지막으로 고른 값이 들어 있습니다.
        category = savedState.getLiveData(KEY_CATEGORY, SubscribeCategory.OTT);
        directInput = savedState.getLiveData(KEY_DIRECT_INPUT, false);
        status = savedState.getLiveData(KEY_STATUS, SubscribeStatus.ACTIVE);
        billingCycle = savedState.getLiveData(KEY_BILLING_CYCLE, BillingCycle.MONTHLY);
        currency = savedState.getLiveData(KEY_CURRENCY, SubscribeCurrency.KRW);
        installedAppRepository.refresh();
    }

    /**
     * 화면을 처음 열 때 한 번만 동작합니다. 수정 모드면 기존 구독을 찾아 getEditingItem()으로 알려줍니다.
     * 앱이 종료됐다가 이 화면부터 복원되면 목록이 아직 비어 있으므로, 한 번 불러온 뒤 다시 찾습니다.
     * 이때 선택값은 사용자가 마지막으로 고른 값(SavedStateHandle)을 그대로 두고 기존 구독 값으로 덮어쓰지 않습니다.
     */
    public void init(long id) {
        if (initialized) {
            return;
        }
        initialized = true;
        if (id == NEW_SUBSCRIBE) {
            return;
        }
        subscribeId = id;
        SubscribeItem item = repository.findSubscribe(id);
        if (item != null) {
            applyEditingItem(item);
            return;
        }
        repository.refresh(new ResultCallback<List<SubscribeItem>>() {
            @Override
            public void onSuccess(List<SubscribeItem> list) {
                SubscribeItem found = repository.findSubscribe(id);
                if (found == null) {
                    result.setValue(Result.NOT_FOUND);
                } else {
                    applyEditingItem(found);
                }
            }

            @Override
            public void onError(String error) {
                // 화면이 바로 닫히므로 목록 화면에서 '찾을 수 없습니다' 안내만 보여줍니다.
                result.setValue(Result.NOT_FOUND);
            }
        });
    }

    private void applyEditingItem(SubscribeItem item) {
        if (!Boolean.TRUE.equals(savedState.get(KEY_CHOICES_FILLED))) {
            savedState.set(KEY_CHOICES_FILLED, true);
            category.setValue(item.getCategory());
            // 선택지에 없는 서비스명은 직접 입력한 것이므로 바로 고칠 수 있게 합니다.
            directInput.setValue(!ServicePreset.getServices(item.getCategory()).contains(item.getServiceName()));
            status.setValue(item.getStatus());
            billingCycle.setValue(item.getBillingCycle());
            currency.setValue(item.getCurrency());
        }
        editingItem.setValue(item);
    }

    /** 수정 모드에서 불러온 기존 구독 (입력칸 채우기용). 추가 모드이거나 아직 불러오는 중이면 null */
    public LiveData<SubscribeItem> getEditingItem() {
        return editingItem;
    }

    /** 수정 모드인데 기존 구독을 아직 불러오는 중인지 */
    private boolean isLoading() {
        return isEditMode() && editingItem.getValue() == null;
    }

    public boolean isEditMode() {
        return subscribeId != NEW_SUBSCRIBE;
    }

    /** 입력을 시작했는지 (뒤로 갈 때 '작성을 취소하시겠습니까?'를 물어볼지 결정) */
    public boolean isChanged() {
        return Boolean.TRUE.equals(savedState.get(KEY_CHANGED));
    }

    public void markChanged() {
        savedState.set(KEY_CHANGED, true);
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

    /** 서비스명과 같은 이름의 설치된 앱. 없으면 null (넷플릭스처럼 별명으로 써도 정식 이름으로 찾습니다) */
    public InstalledApp findInstalledApp(String serviceName) {
        return installedAppRepository.findInstalledApp(resolveServiceName(serviceName));
    }

    /**
     * 직접 입력한 서비스명을 이미 있는 이름으로 맞춥니다. (대소문자·띄어쓰기 무시)
     * 1) 서비스 목록·별명 표: "netflix", "넷플릭스" → "Netflix"
     * 2) 설치된 앱 이름: "chrome" → "Chrome"
     * 3) 둘 다 없으면 입력한 그대로 (앞뒤 공백만 정리)
     */
    public String resolveServiceName(String serviceText) {
        String serviceName = serviceText == null ? "" : serviceText.trim();
        if (serviceName.isEmpty()) {
            return serviceName;
        }
        String presetName = ServiceNameMatcher.findPresetName(serviceName);
        if (presetName != null) {
            return presetName;
        }
        InstalledApp app = installedAppRepository.findInstalledApp(serviceName);
        return app == null ? serviceName : app.getLabel();
    }

    /** 직접 입력 중 추천할 이름 목록: 서비스 목록(별명 포함) 먼저, 그다음 설치된 앱. 중복은 한 번만 */
    public List<String> searchServiceNames(String keyword) {
        List<String> names = ServiceNameMatcher.searchPresetNames(keyword, SUGGESTION_LIMIT);
        for (InstalledApp app : installedAppRepository.searchInstalledApps(keyword, SUGGESTION_LIMIT)) {
            if (names.size() >= SUGGESTION_LIMIT) {
                break;
            }
            ServiceNameMatcher.addOnce(names, app.getLabel());
        }
        return names;
    }

    /** 카테고리가 실제로 바뀌었으면 true (서비스 선택지가 달라지므로 화면에서 서비스명을 비웁니다) */
    public boolean selectCategory(SubscribeCategory value) {
        if (value == category.getValue()) {
            return false;
        }
        category.setValue(value);
        directInput.setValue(false);
        markChanged();
        return true;
    }

    /** 드롭다운에서 서비스를 골랐을 때 */
    public void selectService() {
        directInput.setValue(false);
        markChanged();
    }

    /** 드롭다운에서 '직접 입력'을 골랐을 때 */
    public void startDirectInput() {
        directInput.setValue(true);
        markChanged();
    }

    public void selectStatus(SubscribeStatus value) {
        status.setValue(value);
        markChanged();
    }

    public void selectBillingCycle(BillingCycle value) {
        billingCycle.setValue(value);
        markChanged();
    }

    public void selectCurrency(SubscribeCurrency value) {
        currency.setValue(value);
        markChanged();
    }

    /** 입력값을 확인하고 저장합니다. 잘못된 값이 있으면 안내 메시지만 보여줍니다. */
    public void save(String serviceText, String monthText, String dayText,
                     String amountText, String memoText) {
        if (Boolean.TRUE.equals(saving.getValue()) || isLoading()) {
            return;
        }
        String serviceName = resolveServiceName(serviceText);
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
        if (!isEditMode() || isLoading() || Boolean.TRUE.equals(saving.getValue())) {
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
