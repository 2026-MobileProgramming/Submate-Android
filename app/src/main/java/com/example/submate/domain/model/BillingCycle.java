package com.example.submate.domain.model;

/** 결제 주기 */
public enum BillingCycle {
    MONTHLY("월간"),
    YEARLY("연간");

    private final String label;

    BillingCycle(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
