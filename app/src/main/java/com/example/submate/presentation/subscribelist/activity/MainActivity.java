package com.example.submate.presentation.subscribelist.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.example.submate.R;
import com.example.submate.core.util.ClipboardUtil;
import com.example.submate.core.util.CommonToast;
import com.example.submate.presentation.common.dialog.NotificationDialogManager;
import com.example.submate.presentation.subscribelist.fragment.SubscribeListFragment;
import com.example.submate.presentation.report.ReportFragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private static final String TAG_SUBSCRIBE_LIST = "subscribe_list";
    private static final String KEY_SELECTED_TAB = "selected_tab";
    private static final String TAG_REPORT = "report";
    private static final int[] TAB_IDS = {
            R.id.navigation_calendar,
            R.id.navigation_subscribe_list,
            R.id.navigation_report,
            R.id.navigation_setting
    };

    private int selectedTabId;
    private String currentNickname = "쥐이카와";
    private NotificationDialogManager notificationDialogManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        notificationDialogManager = new NotificationDialogManager(this, this::setToggleValue);
        for (int tabId : TAB_IDS) {
            findViewById(tabId).setOnClickListener(v -> selectTab(tabId));
        }
        // 화면이 회전되어 다시 만들어져도 보고 있던 탭을 유지합니다.
        selectTab(savedInstanceState == null
                ? R.id.navigation_calendar
                : savedInstanceState.getInt(KEY_SELECTED_TAB, R.id.navigation_calendar));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, selectedTabId);
    }

    private void selectTab(int tabId) {
        selectedTabId = tabId;
        for (int id : TAB_IDS) {
            findViewById(id).setSelected(id == tabId);
        }
        if (tabId == R.id.navigation_subscribe_list) {
            showSubscribeList();
        } else if (tabId == R.id.navigation_report) {
            showReport();
        } else if (tabId == R.id.navigation_setting) {
            showScreen(R.layout.screen_setting);
        } else {
            showScreen(R.layout.screen_calendar);
        }
    }

    private void showReport() {
        if (getSupportFragmentManager().findFragmentByTag(TAG_REPORT) != null) {
            return;
        }

        removeSubscribeListFragment();

        FrameLayout container = findViewById(R.id.tab_container);
        container.removeAllViews();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.tab_container, new ReportFragment(), TAG_REPORT)
                .commit();
    }

    /** 아직 Fragment가 없는 탭은 레이아웃만 보여줍니다. */
    private void showScreen(int layoutId) {
        removeSubscribeListFragment();
        removeReportFragment();
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
                selectTab(R.id.navigation_subscribe_list);
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

    /** 구독 목록 탭은 Fragment로 보여줍니다. */
    private void showSubscribeList() {
        removeReportFragment();
        if (getSupportFragmentManager().findFragmentByTag(TAG_SUBSCRIBE_LIST) != null) {
            return;
        }
        FrameLayout container = findViewById(R.id.tab_container);
        container.removeAllViews();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.tab_container, new SubscribeListFragment(), TAG_SUBSCRIBE_LIST)
                .commit();
    }

    private void removeSubscribeListFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_SUBSCRIBE_LIST);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commitNow();
        }
    }

    private void removeReportFragment() {
        Fragment fragment = getSupportFragmentManager()
                .findFragmentByTag(TAG_REPORT);

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commitNow();
        }
    }
}
