package com.example.submate.domain.repository;

import androidx.lifecycle.LiveData;
import com.example.submate.domain.model.SubscribeItem;
import java.util.List;

/**
 * 구독 데이터를 앱 전체에서 공유하는 저장소입니다. (앱에 하나만 존재)
 * 캘린더/리포트/설정 탭도 ViewModel에 주입받아 observeSubscribeList()를 관찰하면
 * 구독이 추가/수정/삭제되거나 환율이 바뀔 때마다 최신 목록을 받습니다.
 */
public interface SubscribeRepository {
    /** 해지완료를 포함한 전체 구독 목록 (화면에서 관찰용) */
    LiveData<List<SubscribeItem>> observeSubscribeList();

    /**
     * 지금 가지고 있는 전체 구독 목록 (해지완료 포함, 읽기 전용).
     * 알림 작업처럼 화면이 아닌 곳에서 바로 값이 필요할 때 사용합니다. 어느 스레드에서 불러도 됩니다.
     */
    List<SubscribeItem> getSubscribeList();

    /** 구독 목록(과 환율)을 다시 불러옵니다. */
    void refresh(ResultCallback<List<SubscribeItem>> callback);

    /** 이미 불러온 목록에서 id로 구독을 찾습니다. 없으면 null */
    SubscribeItem findSubscribe(long id);

    void addSubscribe(SubscribeItem item, ResultCallback<SubscribeItem> callback);

    void updateSubscribe(SubscribeItem item, ResultCallback<SubscribeItem> callback);

    void deleteSubscribe(long id, ResultCallback<Void> callback);
}
