package com.example.submate.domain.model;

public final class SubscribeItem {
    private final long id;
    private final String name;
    private final String nextPaymentDate;

    public SubscribeItem(long id, String name, String nextPaymentDate) {
        this.id = id;
        this.name = name;
        this.nextPaymentDate = nextPaymentDate;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getNextPaymentDate() { return nextPaymentDate; }
}
