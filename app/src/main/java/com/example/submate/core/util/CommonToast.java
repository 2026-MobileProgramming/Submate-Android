package com.example.submate.core.util;

import android.content.Context;
import android.widget.Toast;

public final class CommonToast {
    private CommonToast() {
    }

    public static void show(Context context, String message) {
        Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
