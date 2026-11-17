package com.example.submate.presentation.common.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.submate.R;

public final class NotificationDialogManager {
    public interface Listener {
        void onToggleChanged(int viewId, String value, int colorId);
    }

    private final Context context;
    private final Listener listener;

    public NotificationDialogManager(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void showPushDialog() {
        Dialog dialog = createDialog(R.layout.modal_push);
        dialog.findViewById(R.id.modal_close).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.modal_reject).setOnClickListener(v -> {
            listener.onToggleChanged(R.id.notification_push_value, "거부", R.color.error500);
            dialog.dismiss();
        });
        dialog.findViewById(R.id.modal_agree).setOnClickListener(v -> {
            listener.onToggleChanged(R.id.notification_push_value, "동의", R.color.pri800);
            dialog.dismiss();
        });
        dialog.show();
        resizeDialog(dialog, 380, 250);
    }

    public void showCommonNotificationDialog(String title, int valueId) {
        Dialog dialog = createDialog(R.layout.common_modal);
        dialog.findViewById(R.id.modal_close).setOnClickListener(v -> dialog.dismiss());
        ((TextView) dialog.findViewById(R.id.modal_title)).setText(title + "을 받으시겠습니까?");
        dialog.findViewById(R.id.modal_no).setOnClickListener(v -> {
            listener.onToggleChanged(valueId, "OFF", R.color.error500);
            dialog.dismiss();
        });
        dialog.findViewById(R.id.modal_yes).setOnClickListener(v -> {
            listener.onToggleChanged(valueId, "ON", R.color.pri800);
            dialog.dismiss();
        });
        dialog.show();
        resizeDialog(dialog, 380, 200);
    }

    private Dialog createDialog(int layoutId) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutId);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = 0.35f;
            window.setAttributes(params);
        }
        return dialog;
    }

    private void resizeDialog(Dialog dialog, int width, int height) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(dp(width), dp(height));
        }
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
