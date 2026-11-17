package com.example.submate.presentation.subscribelist.activity;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.submate.R;
import com.example.submate.core.util.CommonToast;
import com.example.submate.presentation.subscribelist.viewmodel.SubscribeListViewModel;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.app.Dialog;
import android.view.Window;
import android.view.WindowManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private SubscribeListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(SubscribeListViewModel.class);
        View calendar = findViewById(R.id.navigation_calendar);
        View subscribeList = findViewById(R.id.navigation_subscribe_list);
        View report = findViewById(R.id.navigation_report);
        View setting = findViewById(R.id.navigation_setting);

        calendar.setOnClickListener(v -> selectTab(calendar, R.layout.screen_calendar));
        subscribeList.setOnClickListener(v -> selectTab(subscribeList, R.layout.screen_subscribe_list));
        report.setOnClickListener(v -> selectTab(report, R.layout.screen_report));
        setting.setOnClickListener(v -> selectTab(setting, R.layout.screen_setting));
        selectTab(calendar, R.layout.screen_calendar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void selectTab(View selectedTab, int layoutId) {
        findViewById(R.id.navigation_calendar).setSelected(false);
        findViewById(R.id.navigation_subscribe_list).setSelected(false);
        findViewById(R.id.navigation_report).setSelected(false);
        findViewById(R.id.navigation_setting).setSelected(false);
        selectedTab.setSelected(true);

        FrameLayout container = findViewById(R.id.tab_container);
        container.removeAllViews();
        LayoutInflater.from(this).inflate(layoutId, container, true);
        if (layoutId == R.layout.screen_setting) {
            bindSettingMenu();
        }
    }

    private void bindSettingMenu() {
        findViewById(R.id.setting_account).setOnClickListener(v -> showSettingDetail(R.layout.setting_account, "계정"));
        findViewById(R.id.setting_list).setOnClickListener(v -> showSettingDetail(R.layout.setting_list, "내 구독 현황"));
        findViewById(R.id.setting_notification).setOnClickListener(v -> showSettingDetail(R.layout.setting_notification, "알림 설정"));
        findViewById(R.id.setting_access).setOnClickListener(v -> showSettingDetail(R.layout.setting_access, "앱 내 권한"));
        findViewById(R.id.setting_contact).setOnClickListener(v -> showSettingDetail(R.layout.setting_contact, "문의하기"));
    }

    private void showSettingDetail(int layoutId, String title) {
        FrameLayout container = findViewById(R.id.tab_container);
        container.removeAllViews();
        LayoutInflater.from(this).inflate(layoutId, container, true);
        findViewById(R.id.common_sub_header).setOnClickListener(v -> showSettingMenu());
        ((android.widget.TextView) findViewById(R.id.common_sub_header_title)).setText(title);
        if (layoutId == R.layout.setting_notification) {
            bindNotificationSettings();
        }
        if (layoutId == R.layout.setting_list) {
            bindSubscribeListLink();
        }
        if (layoutId == R.layout.setting_contact) {
            bindContactEmailCopy();
        }
    }

    private void bindContactEmailCopy() {
        findViewById(R.id.contact_copy_email).setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("문의 이메일", "submate@mail.com"));
            CommonToast.show(this, "이메일이 복사되었습니다");
        });
    }

    private void showSettingMenu() {
        FrameLayout container = findViewById(R.id.tab_container);
        container.removeAllViews();
        LayoutInflater.from(this).inflate(R.layout.screen_setting, container, true);
        bindSettingMenu();
    }

    private void bindSubscribeListLink() {
        android.widget.TextView link = findViewById(R.id.subscribe_list_link);
        String message = "자세한 내용은 구독목록 탭에서 확인하세요!";
        SpannableString spannable = new SpannableString(message);
        int start = message.indexOf("구독목록");
        int end = start + "구독목록".length();
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                View subscribeList = findViewById(R.id.navigation_subscribe_list);
                selectTab(subscribeList, R.layout.screen_subscribe_list);
            }

            @Override
            public void updateDrawState(android.text.TextPaint drawState) {
                drawState.setColor(getColor(R.color.pri700));
                drawState.setUnderlineText(false);
            }

        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        link.setText(spannable);
        link.setMovementMethod(LinkMovementMethod.getInstance());
        link.setHighlightColor(android.graphics.Color.TRANSPARENT);
    }

    private void bindNotificationSettings() {
        findViewById(R.id.notification_push).setOnClickListener(v -> showPushDialog());
        findViewById(R.id.notification_payment).setOnClickListener(v -> showCommonNotificationDialog("결제일 알림", R.id.notification_payment_value));
        findViewById(R.id.notification_expiring).setOnClickListener(v -> showCommonNotificationDialog("해지예정 구독 알림", R.id.notification_expiring_value));
        findViewById(R.id.notification_trial).setOnClickListener(v -> showCommonNotificationDialog("무료체험 종료 알림", R.id.notification_trial_value));
    }

    private void showPushDialog() {
        Dialog dialog = createDialog(R.layout.modal_push, 380, 250);
        dialog.findViewById(R.id.modal_close).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.modal_reject).setOnClickListener(v -> {
            setToggleValue(R.id.notification_push_value, "거부", R.color.error500);
            dialog.dismiss();
        });
        dialog.findViewById(R.id.modal_agree).setOnClickListener(v -> {
            setToggleValue(R.id.notification_push_value, "동의", R.color.pri800);
            dialog.dismiss();
        });
        dialog.show();
        resizeDialog(dialog, 380, 250);
    }

    private void showCommonNotificationDialog(String title, int valueId) {
        Dialog dialog = createDialog(R.layout.common_modal, 380, 200);
        dialog.findViewById(R.id.modal_close).setOnClickListener(v -> dialog.dismiss());
        ((android.widget.TextView) dialog.findViewById(R.id.modal_title)).setText(title + "을 받으시겠습니까?");
        dialog.findViewById(R.id.modal_no).setOnClickListener(v -> {
            setToggleValue(valueId, "OFF", R.color.error500);
            dialog.dismiss();
        });
        dialog.findViewById(R.id.modal_yes).setOnClickListener(v -> {
            setToggleValue(valueId, "ON", R.color.pri800);
            dialog.dismiss();
        });
        dialog.show();
        resizeDialog(dialog, 380, 200);
    }

    private Dialog createDialog(int layoutId, int width, int height) {
        Dialog dialog = new Dialog(this);
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
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void setToggleValue(int viewId, String value, int colorId) {
        android.widget.TextView valueView = findViewById(viewId);
        valueView.setText(value);
        valueView.setTextColor(getColor(colorId));
    }
}
