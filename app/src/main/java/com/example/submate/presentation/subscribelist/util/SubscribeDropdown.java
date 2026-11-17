package com.example.submate.presentation.subscribelist.util;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.submate.R;
import java.util.List;

/**
 * 입력칸 바로 아래(공간이 부족하면 위)에 뜨는 선택 목록입니다.
 * 별도의 팝업 창을 쓰지 않고 현재 화면 위에 직접 그려서, 기기와 상관없이 같은 위치에 보입니다.
 * - show(): 드롭다운. 목록 바깥을 누르면 닫히고, 열려 있는 동안 뒤 화면은 눌리지 않습니다.
 * - showSuggestions(): 키보드로 입력하는 중의 추천 목록. 뒤 화면 터치를 막지 않습니다.
 */
public class SubscribeDropdown {
    public interface OnItemSelectedListener {
        void onItemSelected(int position);
    }

    private static final int GAP_DP = 4;
    private static final int SCREEN_MARGIN_DP = 8;
    private static final int ELEVATION_DP = 4;

    private final Activity activity;
    // 화면에 붙어 있는 뷰 (드롭다운이면 화면 전체를 덮는 투명한 판, 추천 목록이면 목록 자체)
    private View shownView;
    private boolean modal;
    private Runnable onDismiss;

    public SubscribeDropdown(Activity activity) {
        this.activity = activity;
    }

    public boolean isShowing() {
        return shownView != null;
    }

    /**
     * @param anchor    목록을 붙일 입력칸 (목록의 너비도 이 칸과 같게 맞춥니다)
     * @param onDismiss 목록이 닫힐 때 할 일 (화살표 되돌리기 등). 필요 없으면 null
     */
    public void show(View anchor, List<String> items,
                     OnItemSelectedListener listener, Runnable onDismiss) {
        showInternal(anchor, items, listener, onDismiss, true);
    }

    /** 입력 중 추천 목록. 입력칸이나 다른 곳을 눌러도 그대로 눌리고, 바깥을 누르면 닫힙니다. */
    public void showSuggestions(View anchor, List<String> items, OnItemSelectedListener listener) {
        showInternal(anchor, items, listener, null, false);
    }

    public void dismiss() {
        if (shownView == null) {
            return;
        }
        ViewGroup parent = (ViewGroup) shownView.getParent();
        if (parent != null) {
            parent.removeView(shownView);
        }
        shownView = null;
        if (onDismiss != null) {
            Runnable callback = onDismiss;
            onDismiss = null;
            callback.run();
        }
    }

    /** 추천 목록이 떠 있을 때만 닫습니다. (드롭다운은 그대로) */
    public void dismissSuggestions() {
        if (shownView != null && !modal) {
            dismiss();
        }
    }

    /**
     * 화면을 누를 때 Activity.dispatchTouchEvent에서 불러 주세요.
     * 추천 목록과 keepView(입력칸) 바깥을 누르면 추천 목록을 닫습니다. 누른 동작 자체는 막지 않습니다.
     */
    public void onTouchDown(MotionEvent event, View keepView) {
        if (shownView == null || modal) {
            return;
        }
        if (!contains(shownView, event) && !contains(keepView, event)) {
            dismiss();
        }
    }

    private void showInternal(View anchor, List<String> items, OnItemSelectedListener listener,
                              Runnable onDismiss, boolean modal) {
        dismiss();
        FrameLayout root = activity.findViewById(android.R.id.content);
        this.onDismiss = onDismiss;
        this.modal = modal;

        ScrollView panel = new ScrollView(activity);
        panel.setBackgroundResource(R.drawable.bg_subscribe_dropdown);
        panel.setClipToOutline(true);
        panel.setElevation(dp(ELEVATION_DP));
        panel.addView(createItemList(items, listener));
        FrameLayout.LayoutParams panelParams = createPanelLayoutParams(root, anchor, items.size());

        if (modal) {
            FrameLayout overlay = new FrameLayout(activity);
            overlay.setClickable(true);
            overlay.setOnClickListener(v -> dismiss());
            overlay.addView(panel, panelParams);
            root.addView(overlay, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            shownView = overlay;
        } else {
            root.addView(panel, panelParams);
            shownView = panel;
        }
    }

    private static boolean contains(View view, MotionEvent event) {
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        float x = event.getRawX();
        float y = event.getRawY();
        return x >= location[0] && x < location[0] + view.getWidth()
                && y >= location[1] && y < location[1] + view.getHeight();
    }

    private LinearLayout createItemList(List<String> items, OnItemSelectedListener listener) {
        LinearLayout list = new LinearLayout(activity);
        list.setOrientation(LinearLayout.VERTICAL);
        LayoutInflater inflater = LayoutInflater.from(activity);
        for (int i = 0; i < items.size(); i++) {
            int position = i;
            TextView item = (TextView) inflater.inflate(R.layout.subscribe_dropdown_item, list, false);
            item.setText(items.get(i));
            item.setOnClickListener(v -> {
                dismiss();
                listener.onItemSelected(position);
            });
            list.addView(item);
        }
        return list;
    }

    /** 입력칸 아래에 놓되, 아래 공간이 모자라고 위가 더 넓으면 입력칸 위에 놓습니다. */
    private FrameLayout.LayoutParams createPanelLayoutParams(FrameLayout root, View anchor, int itemCount) {
        int[] rootLocation = new int[2];
        int[] anchorLocation = new int[2];
        root.getLocationInWindow(rootLocation);
        anchor.getLocationInWindow(anchorLocation);
        int anchorLeft = anchorLocation[0] - rootLocation[0];
        int anchorTop = anchorLocation[1] - rootLocation[1];
        int anchorBottom = anchorTop + anchor.getHeight();

        // 상태바, 내비게이션바, 키보드에 가려지는 영역은 빼고 계산합니다.
        int topInset = 0;
        int bottomInset = 0;
        WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(root);
        if (windowInsets != null) {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            topInset = insets.top;
            bottomInset = insets.bottom;
        }
        int gap = dp(GAP_DP);
        int margin = dp(SCREEN_MARGIN_DP);
        int spaceBelow = root.getHeight() - bottomInset - margin - (anchorBottom + gap);
        int spaceAbove = (anchorTop - gap) - topInset - margin;

        int itemHeight = activity.getResources().getDimensionPixelSize(R.dimen.subscribe_dropdown_item_height);
        int wantedHeight = itemHeight * itemCount;
        boolean showBelow = spaceBelow >= wantedHeight || spaceBelow >= spaceAbove;
        // 공간이 모자라면 목록 안에서 스크롤되게 높이를 줄입니다. (최소 한 줄은 보이게)
        int height = Math.max(itemHeight, Math.min(wantedHeight, showBelow ? spaceBelow : spaceAbove));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                anchor.getWidth(), height, Gravity.TOP | Gravity.START);
        params.leftMargin = anchorLeft;
        params.topMargin = showBelow ? anchorBottom + gap : anchorTop - gap - height;
        return params;
    }

    private int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
