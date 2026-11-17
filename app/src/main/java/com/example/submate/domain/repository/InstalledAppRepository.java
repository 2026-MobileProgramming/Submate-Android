package com.example.submate.domain.repository;

import androidx.lifecycle.LiveData;
import com.example.submate.domain.model.InstalledApp;
import com.example.submate.domain.model.SubscribeItem;
import java.util.List;

/**
 * 기기에 설치된 앱 목록을 관리합니다.
 * 구독의 서비스명이 설치된 앱 이름과 같으면 그 앱의 packageName을 구독에 저장하고,
 * 리포트 탭은 그 packageName으로 UsageStatsManager에서 실제 사용 시간을 조회합니다.
 */
public interface InstalledAppRepository {
    /** 설치된 앱 목록 (앱 이름 가나다순). 아직 불러오기 전이면 빈 목록 */
    LiveData<List<InstalledApp>> observeInstalledApps();

    /** 설치된 앱 목록을 백그라운드에서 다시 읽습니다. */
    void refresh();

    /**
     * 서비스명과 같은 이름의 앱을 찾습니다. 대소문자와 띄어쓰기는 무시합니다.
     * ex) "youtube music premium" == "YouTube Music Premium"
     *
     * @return 없으면 null
     */
    InstalledApp findInstalledApp(String serviceName);

    /** 앱 이름에 keyword가 들어가는 앱을 최대 limit개 돌려줍니다. (직접 입력 중 추천 목록용) */
    List<InstalledApp> searchInstalledApps(String keyword, int limit);

    /**
     * 구독의 사용량을 조회할 패키지명입니다. (리포트 탭용)
     * 저장된 패키지명의 앱이 지금도 설치되어 있으면 그대로 쓰고, 지워졌거나 저장 후에 설치했다면 서비스명으로 다시 찾습니다.
     * 연결할 앱이 없으면 ""
     */
    String resolvePackageName(SubscribeItem item);
}
