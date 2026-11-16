package com.example.submate.presentation.subscribelist.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.example.submate.R;
import com.example.submate.presentation.subscribelist.fragment.SubscribeListFragment;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private static final String TAG_SUBSCRIBE_LIST = "subscribe_list";
    private static final String KEY_SELECTED_TAB = "selected_tab";
    private static final int[] TAB_IDS = {
            R.id.navigation_calendar,
            R.id.navigation_subscribe_list,
            R.id.navigation_report,
            R.id.navigation_setting
    };

    private int selectedTabId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
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
            showScreen(R.layout.screen_report);
        } else if (tabId == R.id.navigation_setting) {
            showScreen(R.layout.screen_setting);
        } else {
            showScreen(R.layout.screen_calendar);
        }
    }

    /** 아직 Fragment가 없는 탭은 레이아웃만 보여줍니다. */
    private void showScreen(int layoutId) {
        removeSubscribeListFragment();
        FrameLayout container = findViewById(R.id.tab_container);
        container.removeAllViews();
        LayoutInflater.from(this).inflate(layoutId, container, true);
    }

    /** 구독 목록 탭은 Fragment로 보여줍니다. */
    private void showSubscribeList() {
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
}
