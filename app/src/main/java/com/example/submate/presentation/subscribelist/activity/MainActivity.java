package com.example.submate.presentation.subscribelist.activity;

import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.submate.R;
import com.example.submate.core.util.ClipboardUtil;
import com.example.submate.core.util.CommonToast;
import com.example.submate.core.util.UsageStatsUtil;
import com.example.submate.presentation.common.dialog.NotificationDialogManager;
import com.example.submate.presentation.subscribelist.viewmodel.SubscribeListViewModel;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private SubscribeListViewModel viewModel;
    private String currentNickname = "쥐이카와";
    private NotificationDialogManager notificationDialogManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(SubscribeListViewModel.class);
        notificationDialogManager = new NotificationDialogManager(this, this::setToggleValue);
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
        if (layoutId == R.layout.setting_account) {
            ((android.widget.TextView) findViewById(R.id.account_nickname)).setText(currentNickname + " 님");
            findViewById(R.id.account_edit_nickname).setOnClickListener(v -> showNicknameEdit());
        }
        if (layoutId == R.layout.setting_access) {
            bindUsageAccess();
        }
    }

    private void bindUsageAccess() {
        TextView button = findViewById(R.id.access_permission_button);
        button.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        updateUsageAccessButton(button);
    }

    private void updateUsageAccessButton(TextView button) {
        button.setText(UsageStatsUtil.hasUsageAccess(this)
                ? "접근 권한 허용됨"
                : "접근 허용하러 가기");
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView button = findViewById(R.id.access_permission_button);
        if (button != null) updateUsageAccessButton(button);
    }

    private void showNicknameEdit() {
        FrameLayout container = findViewById(R.id.tab_container);
        container.removeAllViews();
        LayoutInflater.from(this).inflate(R.layout.setting_acount_nickname, container, true);
        findViewById(R.id.common_sub_header).setOnClickListener(v -> showSettingDetail(R.layout.setting_account, "계정"));
        ((android.widget.TextView) findViewById(R.id.common_sub_header_title)).setText("계정");
        ((android.widget.EditText) findViewById(R.id.nickname_edit)).setText(currentNickname);
        ((android.widget.EditText) findViewById(R.id.nickname_edit)).setSelection(currentNickname.length());
        ((android.widget.TextView) findViewById(R.id.nickname_display)).setText(currentNickname + " 님");
        findViewById(R.id.nickname_display).setOnClickListener(v -> beginNicknameEdit());
        findViewById(R.id.nickname_edit_icon).setOnClickListener(v -> beginNicknameEdit());
        View.OnClickListener returnToAccount = v -> showSettingDetail(R.layout.setting_account, "계정");
        findViewById(R.id.nickname_cancel).setOnClickListener(returnToAccount);
        findViewById(R.id.nickname_save).setOnClickListener(v -> {
            android.widget.EditText nicknameEdit = findViewById(R.id.nickname_edit);
            String nickname = nicknameEdit.getText().toString().trim();
            if (!nickname.isEmpty()) {
                currentNickname = nickname;
            }
            showSettingDetail(R.layout.setting_account, "계정");
        });
    }

    private void beginNicknameEdit() {
        findViewById(R.id.nickname_display).setVisibility(View.GONE);
        findViewById(R.id.nickname_edit).setVisibility(View.VISIBLE);
        findViewById(R.id.nickname_edit_icon).setVisibility(View.GONE);
        findViewById(R.id.nickname_edit).requestFocus();
    }

    private void bindContactEmailCopy() {
        findViewById(R.id.contact_copy_email).setOnClickListener(v -> {
            ClipboardUtil.copy(this, "문의 이메일", "submate@mail.com");
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
        findViewById(R.id.notification_push).setOnClickListener(v -> notificationDialogManager.showPushDialog());
        findViewById(R.id.notification_payment).setOnClickListener(v -> notificationDialogManager.showCommonNotificationDialog("결제일 알림", R.id.notification_payment_value));
        findViewById(R.id.notification_expiring).setOnClickListener(v -> notificationDialogManager.showCommonNotificationDialog("해지예정 구독 알림", R.id.notification_expiring_value));
        findViewById(R.id.notification_trial).setOnClickListener(v -> notificationDialogManager.showCommonNotificationDialog("무료체험 종료 알림", R.id.notification_trial_value));
    }

    private void setToggleValue(int viewId, String value, int colorId) {
        android.widget.TextView valueView = findViewById(viewId);
        valueView.setText(value);
        valueView.setTextColor(getColor(colorId));
    }
}
