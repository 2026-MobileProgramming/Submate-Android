package com.example.submate.data.service;

import com.example.submate.domain.model.SubscribeItem;
import java.util.List;

/** External data source boundary. Replace with Retrofit/Room implementation later. */
public interface SubscribeService {
    List<SubscribeItem> getSubscribeList();
}
