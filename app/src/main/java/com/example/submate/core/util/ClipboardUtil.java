package com.example.submate.core.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public final class ClipboardUtil {
    private ClipboardUtil() {
    }

    public static void copy(Context context, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
        }
    }
}
