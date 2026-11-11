package com.example.submate.domain.repository;

import androidx.lifecycle.LiveData;
import com.example.submate.domain.model.SubscribeItem;
import java.util.List;

public interface SubscribeRepository {
    LiveData<List<SubscribeItem>> observeSubscribeList();
}
