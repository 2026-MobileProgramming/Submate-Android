package com.example.submate.presentation.subscribelist.activity;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.submate.R;
import com.example.submate.presentation.subscribelist.viewmodel.SubscribeListViewModel;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
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
        findViewById(R.id.setting_list).setOnClickListener(v -> showSettingDetail(R.layout.setting_list, "구독 목록"));
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
    }

    private void showSettingMenu() {
        FrameLayout container = findViewById(R.id.tab_container);
        container.removeAllViews();
        LayoutInflater.from(this).inflate(R.layout.screen_setting, container, true);
        bindSettingMenu();
    }
}
