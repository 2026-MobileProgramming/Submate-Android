package com.example.submate.presentation.common.view;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatEditText;

/** Shared edit text component for the Submate screens. */
public class SubmateEditText extends AppCompatEditText {
    public SubmateEditText(Context context) { super(context); }
    public SubmateEditText(Context context, AttributeSet attrs) { super(context, attrs); }
    public SubmateEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}