package com.example.submate.presentation.subscribelist.activity;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.submate.R;
import androidx.fragment.app.Fragment;
import com.example.submate.presentation.subscribelist.fragment.SubscribeListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private static final String TAG_SUBSCRIBE_LIST = "subscribe_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        View calendar = findViewById(R.id.navigation_calendar);
        View subscribeList = findViewById(R.id.navigation_subscribe_list);
        View report = findViewById(R.id.navigation_report);
        View setting = findViewById(R.id.navigation_setting);

        calendar.setOnClickListener(v -> selectTab(calendar, R.layout.screen_calendar));
        subscribeList.setOnClickListener(v -> selectSubscribeListTab(subscribeList));
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
        updateTabSelection(selectedTab);
        removeSubscribeListFragment();

        FrameLayout container = findViewById(R.id.tab_container);
        container.removeAllViews();
        LayoutInflater.from(this).inflate(layoutId, container, true);
    }

    /** 구독 목록 탭은 Fragment로 보여줍니다. */
    private void selectSubscribeListTab(View selectedTab) {
        updateTabSelection(selectedTab);
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

    private void updateTabSelection(View selectedTab) {
        findViewById(R.id.navigation_calendar).setSelected(false);
        findViewById(R.id.navigation_subscribe_list).setSelected(false);
        findViewById(R.id.navigation_report).setSelected(false);
        findViewById(R.id.navigation_setting).setSelected(false);
        selectedTab.setSelected(true);
    }
}
