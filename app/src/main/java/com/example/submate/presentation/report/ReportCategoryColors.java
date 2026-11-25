package com.example.submate.presentation.report;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.example.submate.domain.model.SubscribeCategory;
import com.example.submate.presentation.subscribelist.util.SubscribeFormatter;

public final class ReportCategoryColors {

    private ReportCategoryColors() {
    }

    @ColorInt
    public static int getColor(Context context, SubscribeCategory category) {
        return ContextCompat.getColor(context, SubscribeFormatter.categoryColor(
                category == null ? SubscribeCategory.ETC : category));
    }
}
