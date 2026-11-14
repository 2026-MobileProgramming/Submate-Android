package com.example.submate.presentation.common.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.example.submate.R;

/**
 * 앱 전체에서 통일해서 쓰는 알림창입니다.
 * 버튼이 하나면 [확인]만, 취소 텍스트를 넣으면 [취소][확인] 두 개가 보입니다.
 */
public class SubmateDialog {
    private final Context context;
    private String message;
    private String warning;
    private String negativeText;
    private String positiveText = "확인";
    private Runnable onPositive;
    private Runnable onNegative;

    public SubmateDialog(Context context) {
        this.context = context;
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

    public Dialog show() {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.view_submate_dialog, null);
        dialog.setContentView(view);

        TextView tvMessage = view.findViewById(R.id.tv_dialog_message);
        TextView tvWarning = view.findViewById(R.id.tv_dialog_warning);
        TextView btnNegative = view.findViewById(R.id.btn_dialog_negative);
        TextView btnPositive = view.findViewById(R.id.btn_dialog_positive);

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
            btnNegative.setOnClickListener(v -> {
                dialog.dismiss();
                if (onNegative != null) {
                    onNegative.run();
                }
            });
        }

        btnPositive.setText(positiveText);
        btnPositive.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPositive != null) {
                onPositive.run();
            }
        });

        dialog.show();
        // 크기와 투명 배경은 show() 이후에 지정해야 적용됩니다.
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85f);
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }
}
