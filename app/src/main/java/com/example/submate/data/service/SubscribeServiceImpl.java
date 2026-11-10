package com.example.submate.data.service;

import com.example.submate.domain.model.SubscribeItem;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

public class SubscribeServiceImpl implements SubscribeService {
    @Inject
    public SubscribeServiceImpl() {}

    @Override
    public List<SubscribeItem> getSubscribeList() {
        return Arrays.asList(new SubscribeItem(1L, "Sample subscription", "2026-12-01"));
    }
}
