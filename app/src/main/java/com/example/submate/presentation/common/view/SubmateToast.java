package com.example.submate.presentation.common.view;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.example.submate.R;

/** 하단 네비게이션 위에 잠깐 떴다 사라지는 검은 말풍선 메시지입니다. */
public final class SubmateToast {
    private static final long SHOW_DURATION = 2000L;
    private static final long FADE_DURATION = 300L;

    private SubmateToast() {
    }

    public static void show(Activity activity, String message) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        FrameLayout root = activity.findViewById(android.R.id.content);
        TextView toast = (TextView) LayoutInflater.from(activity)
                .inflate(R.layout.view_submate_toast, root, false);
        toast.setText(message);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        params.bottomMargin = activity.getResources()
                .getDimensionPixelSize(R.dimen.submate_toast_bottom_margin);
        root.addView(toast, params);

        toast.setAlpha(0f);
        toast.animate().alpha(1f).setDuration(FADE_DURATION).start();
        toast.postDelayed(() -> toast.animate()
                .alpha(0f)
                .setDuration(FADE_DURATION)
                .withEndAction(() -> root.removeView(toast))
                .start(), SHOW_DURATION);
    }
}
