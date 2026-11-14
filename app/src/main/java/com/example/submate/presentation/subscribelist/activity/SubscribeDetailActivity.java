package com.example.submate.presentation.subscribelist.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.NumberPicker;
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
import com.example.submate.domain.model.SubscribeCategory;
import com.example.submate.domain.model.SubscribeCurrency;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.model.SubscribeStatus;
import com.example.submate.presentation.common.view.SubmateDialog;
import com.example.submate.presentation.common.view.SubmateToast;
import com.example.submate.presentation.subscribelist.util.AmountTextWatcher;
import com.example.submate.presentation.subscribelist.util.ServicePreset;
import com.example.submate.presentation.subscribelist.util.SubscribeFormatter;
import com.example.submate.presentation.subscribelist.viewmodel.SubscribeDetailViewModel;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import dagger.hilt.android.AndroidEntryPoint;

/** 구독 추가 / 수정(상세) 화면 */
@AndroidEntryPoint
public class SubscribeDetailActivity extends AppCompatActivity {
    public static final String EXTRA_SUBSCRIBE_ID = "extra_subscribe_id";
    public static final String EXTRA_RESULT_MESSAGE = "extra_result_message";
    public static final long NO_ID = SubscribeDetailViewModel.NEW_SUBSCRIBE;

    private SubscribeDetailViewModel viewModel;

    private ChipGroup cgCategory;
    private final List<TextView> categoryChips = new ArrayList<>();
    private final List<TextView> statusChips = new ArrayList<>();
    private EditText etService;
    private ImageView ivServiceArrow;
    private TextView tvCycle;
    private ImageView ivCycleArrow;
    private TextView tvPaymentDate;
    private TextView tvCurrency;
    private ImageView ivCurrencyArrow;
    private EditText etAmount;
    private TextView tvAmountHelper;
    private EditText etMemo;
    private View btnSave;
    private AmountTextWatcher amountTextWatcher;

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

        viewModel = new ViewModelProvider(this).get(SubscribeDetailViewModel.class);
        bindViews();
        setupCategoryChips();
        setupStatusChips();

        // 수정 모드면 기존 금액/메모를 입력칸에 채웁니다. (화면 회전 시에는 EditText가 스스로 복원)
        SubscribeItem item = viewModel.init(getIntent().getLongExtra(EXTRA_SUBSCRIBE_ID, NO_ID));
        if (item != null) {
            etAmount.setText(SubscribeFormatter.formatAmountInput(item));
            etMemo.setText(item.getMemo());
        }
        findViewById(R.id.tv_subscribe_delete)
                .setVisibility(viewModel.isEditMode() ? View.VISIBLE : View.GONE);

        setupListeners();
        observeViewModel();
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.subscribe_detail_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void bindViews() {
        cgCategory = findViewById(R.id.cg_subscribe_category);
        etService = findViewById(R.id.et_subscribe_service);
        ivServiceArrow = findViewById(R.id.iv_subscribe_service_arrow);
        tvCycle = findViewById(R.id.tv_subscribe_cycle);
        ivCycleArrow = findViewById(R.id.iv_subscribe_cycle_arrow);
        tvPaymentDate = findViewById(R.id.tv_subscribe_payment_date);
        tvCurrency = findViewById(R.id.tv_subscribe_currency);
        ivCurrencyArrow = findViewById(R.id.iv_subscribe_currency_arrow);
        etAmount = findViewById(R.id.et_subscribe_amount);
        tvAmountHelper = findViewById(R.id.tv_subscribe_amount_helper);
        etMemo = findViewById(R.id.et_subscribe_memo);
        btnSave = findViewById(R.id.btn_subscribe_save);
    }

    /** 카테고리 칩 7개를 만들고, 각 칩 앞에 카테고리 색 점을 붙입니다. */
    private void setupCategoryChips() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (SubscribeCategory category : SubscribeCategory.values()) {
            TextView chip = (TextView) inflater.inflate(R.layout.subscribe_category_chip, cgCategory, false);
            chip.setText(category.getLabel());
            chip.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.bg_subscribe_dot, 0, 0, 0);
            chip.setCompoundDrawableTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, SubscribeFormatter.categoryColor(category))));
            chip.setOnClickListener(v -> viewModel.selectCategory(category));
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

    private void setupListeners() {
        findViewById(R.id.iv_subscribe_back).setOnClickListener(v -> confirmExit());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmExit();
            }
        });

        // 서비스명: 평소에는 드롭다운, '직접 입력'을 고르면 키보드로 입력
        etService.setOnClickListener(v -> {
            if (!Boolean.TRUE.equals(viewModel.getDirectInput().getValue())) {
                showServiceDropdown();
            }
        });
        ivServiceArrow.setOnClickListener(v -> showServiceDropdown());
        etService.addTextChangedListener(new SimpleTextWatcher(text -> {
            if (Boolean.TRUE.equals(viewModel.getDirectInput().getValue())) {
                viewModel.setDirectServiceName(text);
            }
        }));

        findViewById(R.id.layout_subscribe_cycle).setOnClickListener(v -> showCycleDropdown());
        tvPaymentDate.setOnClickListener(v -> showPaymentDatePicker());
        findViewById(R.id.layout_subscribe_currency).setOnClickListener(v -> showCurrencyDropdown());

        amountTextWatcher = new AmountTextWatcher(etAmount);
        etAmount.addTextChangedListener(amountTextWatcher);
        // 사용자가 직접 입력할 때(포커스가 있을 때)만 변경된 것으로 봅니다. (화면 복원/자동 포맷 제외)
        etAmount.addTextChangedListener(new SimpleTextWatcher(text -> {
            if (etAmount.hasFocus()) {
                viewModel.markChanged();
            }
        }));
        etMemo.addTextChangedListener(new SimpleTextWatcher(text -> {
            if (etMemo.hasFocus()) {
                viewModel.markChanged();
            }
        }));

        btnSave.setOnClickListener(v -> {
            hideKeyboard();
            viewModel.save(etAmount.getText().toString(), etMemo.getText().toString());
        });
        findViewById(R.id.tv_subscribe_delete).setOnClickListener(v -> confirmDelete());
    }

    private void observeViewModel() {
        viewModel.getCategory().observe(this, selected -> {
            SubscribeCategory[] categories = SubscribeCategory.values();
            for (int i = 0; i < categoryChips.size(); i++) {
                categoryChips.get(i).setSelected(categories[i] == selected);
            }
        });
        viewModel.getServiceName().observe(this, name -> {
            // 직접 입력 중에는 사용자가 치는 글자를 덮어쓰지 않습니다.
            if (!Boolean.TRUE.equals(viewModel.getDirectInput().getValue())
                    && !name.equals(etService.getText().toString())) {
                etService.setText(name);
            }
        });
        viewModel.getDirectInput().observe(this, this::renderDirectInput);
        viewModel.getStatus().observe(this, selected -> {
            SubscribeStatus[] statuses = SubscribeStatus.values();
            for (int i = 0; i < statusChips.size(); i++) {
                statusChips.get(i).setSelected(statuses[i] == selected);
            }
        });
        viewModel.getBillingCycle().observe(this, cycle -> {
            tvCycle.setText(cycle.getLabel());
            renderPaymentDate();
        });
        viewModel.getPaymentDate().observe(this, date -> renderPaymentDate());
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

    private void renderDirectInput(boolean direct) {
        etService.setFocusable(direct);
        etService.setFocusableInTouchMode(direct);
        etService.setCursorVisible(direct);
        etService.setHint(direct ? "서비스명을 입력하세요" : "서비스를 선택하세요");
        if (direct) {
            etService.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etService, InputMethodManager.SHOW_IMPLICIT);
        } else {
            etService.clearFocus();
        }
    }

    private void renderPaymentDate() {
        int[] date = viewModel.getPaymentDate().getValue();
        if (date == null) {
            return;
        }
        boolean freeTrial = viewModel.getCurrency().getValue() == SubscribeCurrency.FREE_TRIAL;
        String text = SubscribeFormatter.formatPaymentDate(viewModel.needPaymentMonth(), date[0], date[1]);
        if (freeTrial && !text.isEmpty()) {
            text += " 종료";
        }
        tvPaymentDate.setText(text);
        tvPaymentDate.setHint(freeTrial ? "체험 종료일" : "결제일");
    }

    private void renderCurrency(SubscribeCurrency currency) {
        tvCurrency.setText(currency.getLabel());
        boolean freeTrial = currency == SubscribeCurrency.FREE_TRIAL;
        etAmount.setEnabled(!freeTrial);
        etAmount.setBackgroundResource(freeTrial
                ? R.drawable.bg_subscribe_field_disabled : R.drawable.bg_subscribe_field);
        if (freeTrial) {
            if (etAmount.length() > 0) {
                etAmount.setText("");
            }
            etAmount.setHint("무료 체험 중");
            tvAmountHelper.setText("무료 체험은 결제일 칸에 체험이 끝나는 날짜를 선택해주세요");
        } else {
            etAmount.setHint("금액을 입력하세요");
            tvAmountHelper.setText("외화 선택 시 오늘 환율 기준 원화로 환산해서 보여줘요");
        }
        amountTextWatcher.setAllowDecimal(currency == SubscribeCurrency.USD);
        renderPaymentDate();
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
                new SubmateDialog(this)
                        .setMessage("직접 입력 시 서비스명을 실제 애플리케이션명과\n동일하게 입력해야 기록에 반영됩니다.")
                        .setPositiveButton("확인", viewModel::startDirectInput)
                        .show();
            } else {
                viewModel.selectService(selected);
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

    private interface OnPositionSelected {
        void onSelected(int position);
    }

    /** 입력칸 바로 아래에 목록을 띄우고, 열려 있는 동안 화살표를 위로 바꿉니다. */
    private void showDropdown(View anchor, ImageView arrow, List<String> items, OnPositionSelected listener) {
        hideKeyboard();
        ListPopupWindow popup = new ListPopupWindow(this);
        popup.setAdapter(new ArrayAdapter<>(this, R.layout.subscribe_dropdown_item, items));
        popup.setAnchorView(anchor);
        popup.setWidth(anchor.getWidth());
        popup.setVerticalOffset(getResources().getDimensionPixelSize(R.dimen.subscribe_chip_gap) / 2);
        popup.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_subscribe_dropdown));
        popup.setModal(true);
        popup.setOnItemClickListener((parent, view, position, id) -> {
            popup.dismiss();
            listener.onSelected(position);
        });
        popup.setOnDismissListener(() -> arrow.setImageResource(R.drawable.ic_arrow_down));
        arrow.setImageResource(R.drawable.ic_arrow_up);
        popup.show();
    }

    // ---------- 결제일 선택 ----------

    private void showPaymentDatePicker() {
        hideKeyboard();
        int[] date = viewModel.getPaymentDate().getValue();
        if (viewModel.needPaymentMonth()) {
            Calendar today = Calendar.getInstance();
            int month = date != null && date[0] > 0 ? date[0] - 1 : today.get(Calendar.MONTH);
            int day = date != null && date[1] > 0 ? date[1] : today.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(this,
                    (picker, y, m, d) -> viewModel.selectPaymentDate(m + 1, d),
                    today.get(Calendar.YEAR), month, day).show();
        } else {
            NumberPicker picker = new NumberPicker(this);
            picker.setMinValue(1);
            picker.setMaxValue(31);
            picker.setValue(date != null && date[1] > 0 ? date[1] : 1);
            picker.setFormatter(value -> value + "일");
            new AlertDialog.Builder(this)
                    .setTitle("매월 결제일")
                    .setView(picker)
                    .setNegativeButton("취소", null)
                    .setPositiveButton("확인", (dialog, which) ->
                            viewModel.selectPaymentDate(0, picker.getValue()))
                    .show();
        }
    }

    // ---------- 나가기 / 삭제 ----------

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

    private void finishWithResult(SubscribeDetailViewModel.Result result) {
        String message;
        if (result == SubscribeDetailViewModel.Result.ADDED) {
            message = "추가가 완료되었습니다.";
        } else if (result == SubscribeDetailViewModel.Result.UPDATED) {
            message = "수정이 완료되었습니다.";
        } else {
            message = "삭제가 완료되었습니다.";
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_RESULT_MESSAGE, message);
        setResult(RESULT_OK, data);
        finish();
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
        interface OnChanged {
            void onChanged(String text);
        }

        private final OnChanged onChanged;

        SimpleTextWatcher(OnChanged onChanged) {
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
            onChanged.onChanged(s.toString());
        }
    }
}
