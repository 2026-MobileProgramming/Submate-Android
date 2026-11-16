package com.example.submate.presentation.common.view;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import com.example.submate.R;

/**
 * 앱 전체에서 통일해서 쓰는 알림창입니다.
 * 버튼이 하나면 [확인]만, 취소 텍스트를 넣으면 [취소][확인] 두 개가 보입니다.
 * 별도의 창을 띄우지 않고 현재 화면 위에 직접 그리며, 떠 있는 동안 뒤 화면은 눌리지 않습니다.
 *
 * 사용 예)
 * new SubmateDialog(this)
 *         .setMessage("작성을 취소하시겠습니까?")
 *         .setNegativeButton("취소", null)
 *         .setPositiveButton("확인", this::finish)
 *         .show();
 */
public class SubmateDialog {
    private static final float WIDTH_RATIO = 0.85f;
    private static final int ELEVATION_DP = 8;

    private final Activity activity;
    private String message;
    private String warning;
    private String negativeText;
    private String positiveText = "확인";
    private Runnable onPositive;
    private Runnable onNegative;

    // 화면 전체를 덮어 뒤 화면이 눌리지 않게 하는 투명한 판
    private FrameLayout overlay;
    private OnBackPressedCallback backCallback;

    public SubmateDialog(Activity activity) {
        this.activity = activity;
    }

    public SubmateDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    /** 메시지 아래 빨간 글씨로 보여줄 주의 문구 */
    public SubmateDialog setWarning(String warning) {
        this.warning = warning;
        return this;
    }

    public SubmateDialog setPositiveButton(String text, Runnable listener) {
        this.positiveText = text;
        this.onPositive = listener;
        return this;
    }

    public SubmateDialog setNegativeButton(String text, Runnable listener) {
        this.negativeText = text;
        this.onNegative = listener;
        return this;
    }

    public boolean isShowing() {
        return overlay != null;
    }

    public void show() {
        if (isShowing() || activity.isFinishing()) {
            return;
        }
        FrameLayout root = activity.findViewById(android.R.id.content);
        hideKeyboard(root);

        overlay = new FrameLayout(activity);
        overlay.setClickable(true);

        View card = LayoutInflater.from(activity).inflate(R.layout.view_submate_dialog, overlay, false);
        card.setElevation(ELEVATION_DP * activity.getResources().getDisplayMetrics().density);
        card.setClickable(true);
        bindCard(card);

        int width = (int) (root.getWidth() * WIDTH_RATIO);
        overlay.addView(card, new FrameLayout.LayoutParams(
                width, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        root.addView(overlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        registerBackCallback();
    }

    public void dismiss() {
        if (overlay == null) {
            return;
        }
        ViewGroup parent = (ViewGroup) overlay.getParent();
        if (parent != null) {
            parent.removeView(overlay);
        }
        overlay = null;
        if (backCallback != null) {
            backCallback.remove();
            backCallback = null;
        }
    }

    private void bindCard(View card) {
        TextView tvMessage = card.findViewById(R.id.tv_dialog_message);
        TextView tvWarning = card.findViewById(R.id.tv_dialog_warning);
        TextView btnNegative = card.findViewById(R.id.btn_dialog_negative);
        TextView btnPositive = card.findViewById(R.id.btn_dialog_positive);

        tvMessage.setText(message);
        if (warning == null) {
            tvWarning.setVisibility(View.GONE);
        } else {
            tvWarning.setText(warning);
        }

        if (negativeText == null) {
            btnNegative.setVisibility(View.GONE);
        } else {
            btnNegative.setText(negativeText);
            btnNegative.setOnClickListener(v -> close(onNegative));
        }
        btnPositive.setText(positiveText);
        btnPositive.setOnClickListener(v -> close(onPositive));
    }

    private void close(Runnable action) {
        dismiss();
        if (action != null) {
            action.run();
        }
    }

    /** 알림창이 떠 있을 때 뒤로가기를 누르면 화면이 닫히지 않고 알림창만 닫히게 합니다. (취소와 같은 동작) */
    private void registerBackCallback() {
        if (!(activity instanceof ComponentActivity)) {
            return;
        }
        backCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                close(negativeText == null ? null : onNegative);
            }
        };
        ((ComponentActivity) activity).getOnBackPressedDispatcher().addCallback(backCallback);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
