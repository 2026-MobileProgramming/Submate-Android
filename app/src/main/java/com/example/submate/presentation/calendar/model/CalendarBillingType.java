package com.example.submate.presentation.calendar.model;

/** 결제 주기 **/
public enum CalendarBillingType {
    MONTHLY("월간"),
    YEARLY("연간"),
    FREE_TRIAL("무료체험");

    private final String label;

    CalendarBillingType(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
}
