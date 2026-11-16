package com.example.submate.data.service.installedapp;

import com.example.submate.domain.model.InstalledApp;
import java.util.List;

/** 설치된 앱 정보를 읽어오는 데이터 소스의 경계입니다. (실제 구현은 PackageManager 사용) */
public interface InstalledAppService {
    /** 홈 화면에 아이콘이 있는(실행 가능한) 앱 목록. 이 앱 자신은 제외합니다. 시간이 걸리므로 백그라운드에서 호출합니다. */
    List<InstalledApp> getInstalledApps();
}
