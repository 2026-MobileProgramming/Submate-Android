package com.example.submate.presentation.subscribelist.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.submate.R;
import com.example.submate.domain.model.BillingCycle;
import com.example.submate.domain.model.InstalledApp;
import com.example.submate.domain.model.SubscribeCategory;
import com.example.submate.domain.model.SubscribeCurrency;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.model.SubscribeStatus;
import com.example.submate.presentation.common.view.SubmateDialog;
import com.example.submate.presentation.common.view.SubmateToast;
import com.example.submate.presentation.subscribelist.util.AmountTextWatcher;
import com.example.submate.presentation.subscribelist.util.MaxNumberInputFilter;
import com.example.submate.presentation.subscribelist.util.ServicePreset;
import com.example.submate.presentation.subscribelist.util.SubscribeDropdown;
import com.example.submate.presentation.subscribelist.util.SubscribeFormatter;
import com.example.submate.presentation.subscribelist.viewmodel.SubscribeDetailViewModel;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;
import dagger.hilt.android.AndroidEntryPoint;

/** 구독 추가 / 수정(상세) 화면 */
@AndroidEntryPoint
public class SubscribeDetailActivity extends AppCompatActivity {
    public static final String EXTRA_SUBSCRIBE_ID = "extra_subscribe_id";
    public static final String EXTRA_RESULT_MESSAGE = "extra_result_message";
    public static final long NO_ID = SubscribeDetailViewModel.NEW_SUBSCRIBE;

    private static final int DATE_INPUT_LENGTH = 2;
    private static final String HELPER_DEFAULT = "외화 선택 시 오늘 환율 기준 원화로 환산해서 보여줘요";
    private static final String HELPER_FREE_TRIAL = "무료 체험은 결제일 칸에 체험이 끝나는 날짜를 입력해주세요";
    private static final String SERVICE_HELPER_UNLINKED =
            "같은 이름의 앱이 설치되어 있지 않아 사용량 기록에는 반영되지 않아요";

    private SubscribeDetailViewModel viewModel;

    private final List<TextView> categoryChips = new ArrayList<>();
    private final List<TextView> statusChips = new ArrayList<>();
    private EditText etService;
    private ImageView ivServiceArrow;
    private TextView tvServiceHelper;
    private TextView tvCycle;
    private ImageView ivCycleArrow;
    private EditText etPaymentMonth;
    private TextView tvPaymentMonthUnit;
    private EditText etPaymentDay;
    private TextView tvPaymentDayUnit;
    private TextView tvCurrency;
    private ImageView ivCurrencyArrow;
    private EditText etAmount;
    private TextView tvAmountHelper;
    private EditText etMemo;
    private View btnSave;
    private AmountTextWatcher amountTextWatcher;
    private SubscribeDropdown dropdown;
    // 추천 목록에서 고른 앱 이름을 칸에 넣을 때 추천 목록이 다시 뜨지 않게 합니다.
    private boolean applyingSuggestion = false;
    // 수정 모드로 처음 열었을 때만 기존 값을 채웁니다. (화면 회전·복원 시에는 EditText가 스스로 복원)
    private boolean needFillInputs;
    // 기존 값을 채우는 동안에는 '변경됨'으로 보지 않습니다.
    private boolean fillingInputs = false;

    public static Intent newIntent(Context context, long subscribeId) {
        Intent intent = new Intent(context, SubscribeDetailActivity.class);
        intent.putExtra(EXTRA_SUBSCRIBE_ID, subscribeId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.subscribe_detail);
        applyWindowInsets();
        ((TextView) findViewById(R.id.common_sub_header_title)).setText("구독 목록");

        viewModel = new ViewModelProvider(this).get(SubscribeDetailViewModel.class);
        dropdown = new SubscribeDropdown(this);
        bindViews();
        setupCategoryChips();
        setupStatusChips();
        setupInputs();

        needFillInputs = savedInstanceState == null;
        viewModel.init(getIntent().getLongExtra(EXTRA_SUBSCRIBE_ID, NO_ID));
        findViewById(R.id.tv_subscribe_delete)
                .setVisibility(viewModel.isEditMode() ? View.VISIBLE : View.GONE);

        setupListeners();
        observeViewModel();
    }

    /** 추천 목록이 떠 있을 때 바깥을 누르면 닫습니다. (누른 버튼·칩은 그대로 동작) */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            dropdown.onTouchDown(event, etService);
        }
        return super.dispatchTouchEvent(event);
    }

    // ---------- 화면 준비 ----------

    /** 상태바·내비게이션바·키보드에 화면이 가려지지 않게 여백을 줍니다. */
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.subscribe_detail_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void bindViews() {
        etService = findViewById(R.id.et_subscribe_service);
        ivServiceArrow = findViewById(R.id.iv_subscribe_service_arrow);
        tvServiceHelper = findViewById(R.id.tv_subscribe_service_helper);
        tvCycle = findViewById(R.id.tv_subscribe_cycle);
        ivCycleArrow = findViewById(R.id.iv_subscribe_cycle_arrow);
        etPaymentMonth = findViewById(R.id.et_subscribe_payment_month);
        tvPaymentMonthUnit = findViewById(R.id.tv_subscribe_payment_month_unit);
        etPaymentDay = findViewById(R.id.et_subscribe_payment_day);
        tvPaymentDayUnit = findViewById(R.id.tv_subscribe_payment_day_unit);
        tvCurrency = findViewById(R.id.tv_subscribe_currency);
        ivCurrencyArrow = findViewById(R.id.iv_subscribe_currency_arrow);
        etAmount = findViewById(R.id.et_subscribe_amount);
        tvAmountHelper = findViewById(R.id.tv_subscribe_amount_helper);
        etMemo = findViewById(R.id.et_subscribe_memo);
        btnSave = findViewById(R.id.btn_subscribe_save);
    }

    /** 카테고리 칩 7개를 만들고, 각 칩 앞에 카테고리 색 점을 붙입니다. */
    private void setupCategoryChips() {
        ChipGroup cgCategory = findViewById(R.id.cg_subscribe_category);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (SubscribeCategory category : SubscribeCategory.values()) {
            TextView chip = (TextView) inflater.inflate(R.layout.subscribe_category_chip, cgCategory, false);
            chip.setText(category.getLabel());
            chip.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.bg_subscribe_dot, 0, 0, 0);
            chip.setCompoundDrawableTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, SubscribeFormatter.categoryColor(category))));
            chip.setOnClickListener(v -> {
                // 카테고리가 바뀌면 서비스 선택지도 바뀌므로 서비스명을 비웁니다.
                if (viewModel.selectCategory(category)) {
                    etService.setText("");
                }
            });
            cgCategory.addView(chip);
            categoryChips.add(chip);
        }
    }

    private void setupStatusChips() {
        LinearLayout llStatus = findViewById(R.id.ll_subscribe_status);
        LayoutInflater inflater = LayoutInflater.from(this);
        int gap = getResources().getDimensionPixelSize(R.dimen.subscribe_chip_gap);
        for (SubscribeStatus status : SubscribeStatus.values()) {
            TextView chip = (TextView) inflater.inflate(R.layout.subscribe_category_chip, llStatus, false);
            chip.setText(status.getLabel());
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) chip.getLayoutParams();
            params.setMarginEnd(gap);
            chip.setOnClickListener(v -> viewModel.selectStatus(status));
            llStatus.addView(chip, params);
            statusChips.add(chip);
        }
    }

    /** 숫자 입력칸의 입력 범위와 금액 콤마를 설정합니다. */
    private void setupInputs() {
        etPaymentMonth.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(DATE_INPUT_LENGTH),
                new MaxNumberInputFilter(SubscribeDetailViewModel.MAX_MONTH)});
        etPaymentDay.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(DATE_INPUT_LENGTH),
                new MaxNumberInputFilter(SubscribeDetailViewModel.MAX_DAY)});
        amountTextWatcher = new AmountTextWatcher(etAmount);
        etAmount.addTextChangedListener(amountTextWatcher);
    }

    private void fillInputs(SubscribeItem item) {
        fillingInputs = true;
        // 금액을 넣기 전에 소수점 허용 여부를 먼저 맞춥니다. (안 그러면 $14.99가 14로 잘립니다)
        amountTextWatcher.setAllowDecimal(item.getCurrency() == SubscribeCurrency.USD);
        etService.setText(item.getServiceName());
        if (item.getPaymentMonth() > 0) {
            etPaymentMonth.setText(String.valueOf(item.getPaymentMonth()));
        }
        etPaymentDay.setText(String.valueOf(item.getPaymentDay()));
        etAmount.setText(SubscribeFormatter.formatAmountInput(item));
        etMemo.setText(item.getMemo());
        fillingInputs = false;
    }

    private void setupListeners() {
        findViewById(R.id.iv_subscribe_back).setOnClickListener(v -> confirmExit());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 목록이 열려 있으면 화면을 나가지 않고 목록만 닫습니다.
                if (dropdown.isShowing()) {
                    dropdown.dismiss();
                } else {
                    confirmExit();
                }
            }
        });

        // 서비스명: 평소에는 누르면 드롭다운이 열리고, '직접 입력'을 고른 뒤에는 키보드로 입력합니다.
        etService.setOnClickListener(v -> {
            if (!isDirectInput()) {
                showServiceDropdown();
            }
        });
        ivServiceArrow.setOnClickListener(v -> showServiceDropdown());

        findViewById(R.id.layout_subscribe_cycle).setOnClickListener(v -> showCycleDropdown());
        findViewById(R.id.layout_subscribe_currency).setOnClickListener(v -> showCurrencyDropdown());
        // 결제일 칸의 빈 곳을 눌러도 바로 입력할 수 있게 합니다. 월 칸이 보이고 비어 있으면 월부터 입력합니다.
        findViewById(R.id.layout_subscribe_payment_date).setOnClickListener(v -> {
            boolean monthEmpty = etPaymentMonth.getVisibility() == View.VISIBLE && etPaymentMonth.length() == 0;
            showKeyboard(monthEmpty ? etPaymentMonth : etPaymentDay);
        });
        // 서비스명 칸에서 벗어나면 추천 목록을 닫고, 이미 있는 이름이면 그 이름으로 맞춥니다. (netflix → Netflix)
        etService.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                dropdown.dismissSuggestions();
                applyResolvedServiceName();
            }
        });

        // 사용자가 직접 입력할 때(포커스가 있을 때)만 변경된 것으로 봅니다. (화면 복원/자동 포맷 제외)
        for (EditText input : new EditText[]{etService, etPaymentMonth, etPaymentDay, etAmount, etMemo}) {
            input.addTextChangedListener(new SimpleTextWatcher(() -> {
                if (input.hasFocus() && !fillingInputs) {
                    viewModel.markChanged();
                }
            }));
        }
        etAmount.addTextChangedListener(new SimpleTextWatcher(this::renderAmountHelper));
        etService.addTextChangedListener(new SimpleTextWatcher(() -> {
            renderServiceHelper();
            showServiceSuggestions();
        }));

        btnSave.setOnClickListener(v -> {
            dropdown.dismissSuggestions();
            hideKeyboard();
            viewModel.save(
                    etService.getText().toString(),
                    etPaymentMonth.getText().toString(),
                    etPaymentDay.getText().toString(),
                    etAmount.getText().toString(),
                    etMemo.getText().toString());
        });
        findViewById(R.id.tv_subscribe_delete).setOnClickListener(v -> confirmDelete());
    }

    private void observeViewModel() {
        viewModel.getEditingItem().observe(this, item -> {
            if (item != null && needFillInputs) {
                needFillInputs = false;
                fillInputs(item);
            }
        });
        viewModel.getCategory().observe(this, selected -> {
            SubscribeCategory[] categories = SubscribeCategory.values();
            for (int i = 0; i < categoryChips.size(); i++) {
                categoryChips.get(i).setSelected(categories[i] == selected);
            }
        });
        viewModel.getStatus().observe(this, selected -> {
            SubscribeStatus[] statuses = SubscribeStatus.values();
            for (int i = 0; i < statusChips.size(); i++) {
                statusChips.get(i).setSelected(statuses[i] == selected);
            }
        });
        viewModel.getDirectInput().observe(this, this::renderDirectInput);
        // 설치된 앱 목록은 백그라운드에서 읽히므로, 준비되면 연결 안내를 다시 그립니다.
        viewModel.getInstalledApps().observe(this, apps -> renderServiceHelper());
        viewModel.getBillingCycle().observe(this, cycle -> {
            tvCycle.setText(cycle.getLabel());
            renderPaymentDate();
        });
        viewModel.getCurrency().observe(this, this::renderCurrency);
        viewModel.getSaving().observe(this, saving -> btnSave.setEnabled(!saving));
        viewModel.getMessage().observe(this, message -> {
            if (message != null) {
                SubmateToast.show(this, message);
                viewModel.clearMessage();
            }
        });
        viewModel.getResult().observe(this, this::finishWithResult);
    }

    // ---------- 화면 그리기 ----------

    private boolean isDirectInput() {
        return Boolean.TRUE.equals(viewModel.getDirectInput().getValue());
    }

    /** 직접 입력일 때만 서비스명 칸에 키보드로 입력할 수 있게 합니다. */
    private void renderDirectInput(boolean direct) {
        etService.setFocusable(direct);
        etService.setFocusableInTouchMode(direct);
        etService.setCursorVisible(direct);
        etService.setHint(direct ? "서비스명을 입력하세요" : "서비스를 선택하세요");
        if (!direct) {
            etService.clearFocus();
            dropdown.dismissSuggestions();
        }
    }

    /** 서비스명 아래에 설치된 앱과 연결되는지 보여줍니다. 리포트 탭의 사용량 기록은 연결된 구독만 가능합니다. */
    private void renderServiceHelper() {
        String serviceName = etService.getText().toString().trim();
        if (serviceName.isEmpty()) {
            tvServiceHelper.setVisibility(View.GONE);
            return;
        }
        InstalledApp app = viewModel.findInstalledApp(serviceName);
        tvServiceHelper.setText(app == null
                ? SERVICE_HELPER_UNLINKED
                : "설치된 '" + app.getLabel() + "' 앱과 연결돼요 · 리포트 사용량 기록에 반영돼요");
        tvServiceHelper.setVisibility(View.VISIBLE);
    }

    /**
     * 직접 입력한 이름이 이미 있는 이름과 대소문자·띄어쓰기만 다르거나 별명이면 정식 이름으로 바꿉니다.
     * 입력하는 도중에는 바꾸지 않습니다. (한글 조합이 깨지거나 커서가 튀지 않게)
     */
    private void applyResolvedServiceName() {
        if (!isDirectInput()) {
            return;
        }
        String current = etService.getText().toString();
        String resolved = viewModel.resolveServiceName(current);
        if (!resolved.equals(current)) {
            applyingSuggestion = true;
            etService.setText(resolved);
            applyingSuggestion = false;
        }
    }

    /** 직접 입력 중에는 서비스 목록(별명 포함)과 설치된 앱 이름을 추천합니다. 고르면 그 이름 그대로 들어갑니다. */
    private void showServiceSuggestions() {
        if (applyingSuggestion || !isDirectInput() || !etService.hasFocus()) {
            return;
        }
        List<String> labels = viewModel.searchServiceNames(etService.getText().toString());
        if (labels.isEmpty()) {
            dropdown.dismissSuggestions();
            return;
        }
        // 키보드는 그대로 둔 채 목록만 띄웁니다. 다른 칸이나 버튼은 그대로 누를 수 있습니다.
        dropdown.showSuggestions(findViewById(R.id.layout_subscribe_service), labels, position -> {
            applyingSuggestion = true;
            etService.setText(labels.get(position));
            etService.setSelection(etService.length());
            applyingSuggestion = false;
            viewModel.markChanged();
        });
    }

    /** 연간 결제·무료 체험이면 '월' 입력칸까지 보여줍니다. */
    private void renderPaymentDate() {
        int monthVisibility = viewModel.needPaymentMonth() ? View.VISIBLE : View.GONE;
        etPaymentMonth.setVisibility(monthVisibility);
        tvPaymentMonthUnit.setVisibility(monthVisibility);
        tvPaymentDayUnit.setText(viewModel.isFreeTrial() ? "일 종료" : "일");
    }

    private void renderCurrency(SubscribeCurrency currency) {
        boolean freeTrial = currency == SubscribeCurrency.FREE_TRIAL;
        tvCurrency.setText(currency.getLabel());
        etAmount.setEnabled(!freeTrial);
        etAmount.setBackgroundResource(freeTrial
                ? R.drawable.bg_subscribe_field_disabled : R.drawable.bg_subscribe_field);
        etAmount.setHint(freeTrial ? "무료 체험 중" : "금액을 입력하세요");
        if (freeTrial && etAmount.length() > 0) {
            etAmount.setText("");
        }
        amountTextWatcher.setAllowDecimal(currency == SubscribeCurrency.USD);
        renderAmountHelper();
        renderPaymentDate();
    }

    /** 금액 아래 안내 문구. 외화 금액을 입력하면 오늘 환율로 환산한 원화 금액을 보여줍니다. */
    private void renderAmountHelper() {
        SubscribeCurrency currency = viewModel.getCurrency().getValue();
        double amount = SubscribeFormatter.parseAmount(etAmount.getText().toString());
        if (currency == SubscribeCurrency.FREE_TRIAL) {
            tvAmountHelper.setText(HELPER_FREE_TRIAL);
        } else if (currency != null && currency.isForeign() && amount > 0) {
            tvAmountHelper.setText(SubscribeFormatter.formatExchangePreview(currency, amount));
        } else {
            tvAmountHelper.setText(HELPER_DEFAULT);
        }
    }

    // ---------- 드롭다운 ----------

    private void showServiceDropdown() {
        SubscribeCategory category = viewModel.getCategory().getValue();
        if (category == null) {
            return;
        }
        List<String> services = ServicePreset.getServices(category);
        showDropdown(findViewById(R.id.layout_subscribe_service), ivServiceArrow, services, position -> {
            String selected = services.get(position);
            if (ServicePreset.DIRECT_INPUT.equals(selected)) {
                confirmDirectInput();
            } else {
                viewModel.selectService();
                etService.setText(selected);
            }
        });
    }

    private void showCycleDropdown() {
        BillingCycle[] cycles = BillingCycle.values();
        List<String> labels = new ArrayList<>();
        for (BillingCycle cycle : cycles) {
            labels.add(cycle.getLabel());
        }
        showDropdown(findViewById(R.id.layout_subscribe_cycle), ivCycleArrow, labels,
                position -> viewModel.selectBillingCycle(cycles[position]));
    }

    private void showCurrencyDropdown() {
        SubscribeCurrency[] currencies = SubscribeCurrency.values();
        List<String> labels = new ArrayList<>();
        for (SubscribeCurrency currency : currencies) {
            labels.add(currency.getLabel());
        }
        showDropdown(findViewById(R.id.layout_subscribe_currency), ivCurrencyArrow, labels,
                position -> viewModel.selectCurrency(currencies[position]));
    }

    /** 입력칸 아래에 목록을 띄우고, 열려 있는 동안 화살표를 위로 바꿉니다. */
    private void showDropdown(View anchor, ImageView arrow, List<String> items,
                              SubscribeDropdown.OnItemSelectedListener listener) {
        hideKeyboard();
        clearInputFocus();
        arrow.setImageResource(R.drawable.ic_arrow_up);
        dropdown.show(anchor, items, listener,
                () -> arrow.setImageResource(R.drawable.ic_arrow_down));
    }

    // ---------- 알림창 ----------

    private void confirmDirectInput() {
        new SubmateDialog(this)
                .setMessage("직접 입력 시 서비스명을 실제 애플리케이션명과\n동일하게 입력해야 기록에 반영됩니다.")
                .setPositiveButton("확인", () -> {
                    viewModel.startDirectInput();
                    etService.setText("");
                    showKeyboard(etService);
                })
                .show();
    }

    private void confirmExit() {
        if (!viewModel.isChanged()) {
            finish();
            return;
        }
        new SubmateDialog(this)
                .setMessage("작성을 취소하시겠습니까?")
                .setNegativeButton("취소", null)
                .setPositiveButton("확인", this::finish)
                .show();
    }

    private void confirmDelete() {
        new SubmateDialog(this)
                .setMessage("삭제하지 않고도 해지예정/해지완료 상태로\n변경하여 해지된 구독을 관리할 수 있습니다.\n정말 구독을 삭제하시겠습니까?")
                .setWarning("삭제 시 리포트 탭에 반영되지 않습니다.")
                .setNegativeButton("취소", null)
                .setPositiveButton("확인", viewModel::delete)
                .show();
    }

    /** 저장/삭제가 끝나면 목록 화면에 보여줄 메시지를 넘기고 화면을 닫습니다. */
    private void finishWithResult(SubscribeDetailViewModel.Result result) {
        String message;
        switch (result) {
            case ADDED:
                message = "추가가 완료되었습니다.";
                break;
            case UPDATED:
                message = "수정이 완료되었습니다.";
                break;
            case DELETED:
                message = "삭제가 완료되었습니다.";
                break;
            default:
                message = "구독 정보를 찾을 수 없습니다.";
                break;
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_RESULT_MESSAGE, message);
        setResult(RESULT_OK, data);
        finish();
    }

    // ---------- 키보드 ----------

    private void showKeyboard(EditText target) {
        target.requestFocus();
        target.setSelection(target.length());
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT);
    }

    /** 드롭다운을 여는 동안 다른 입력칸에 커서가 남아 있지 않게 합니다. */
    private void clearInputFocus() {
        View focused = getCurrentFocus();
        if (focused != null) {
            focused.clearFocus();
        }
    }

    private void hideKeyboard() {
        View focused = getCurrentFocus();
        if (focused != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
    }

    /** 글자가 바뀐 뒤에만 필요할 때 쓰는 간단한 TextWatcher */
    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable onChanged;

        SimpleTextWatcher(Runnable onChanged) {
            this.onChanged = onChanged;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            onChanged.run();
        }
    }
}
