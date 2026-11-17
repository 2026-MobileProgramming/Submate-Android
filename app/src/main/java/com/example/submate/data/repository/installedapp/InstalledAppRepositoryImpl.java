package com.example.submate.data.repository.installedapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.submate.data.service.installedapp.InstalledAppService;
import com.example.submate.domain.model.InstalledApp;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.repository.InstalledAppRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Inject;

/** 설치된 앱 목록을 한 번 읽어 두고(캐시), 이름으로 찾거나 검색합니다. */
public class InstalledAppRepositoryImpl implements InstalledAppRepository {
    private final InstalledAppService service;
    private final Executor executor;
    private final MutableLiveData<List<InstalledApp>> installedApps =
            new MutableLiveData<>(Collections.emptyList());
    private volatile List<InstalledApp> cache = Collections.emptyList();
    private volatile boolean loading = false;
    private volatile boolean loaded = false;

    @Inject
    public InstalledAppRepositoryImpl(InstalledAppService service) {
        this(service, Executors.newSingleThreadExecutor());
    }

    /** 테스트에서 동기 Executor를 넣기 위한 생성자 */
    InstalledAppRepositoryImpl(InstalledAppService service, Executor executor) {
        this.service = service;
        this.executor = executor;
    }

    @Override
    public LiveData<List<InstalledApp>> observeInstalledApps() {
        return installedApps;
    }

    @Override
    public void refresh() {
        if (loading) {
            return;
        }
        loading = true;
        executor.execute(() -> {
            try {
                List<InstalledApp> apps = Collections.unmodifiableList(new ArrayList<>(service.getInstalledApps()));
                cache = apps;
                loaded = true;
                installedApps.postValue(apps);
            } catch (RuntimeException e) {
                // 앱 목록을 못 읽어도 구독 기능은 그대로 동작해야 하므로 빈 목록을 유지합니다.
            } finally {
                loading = false;
            }
        });
    }

    @Override
    public InstalledApp findInstalledApp(String serviceName) {
        String wanted = normalize(serviceName);
        if (wanted.isEmpty()) {
            return null;
        }
        for (InstalledApp app : cache) {
            if (normalize(app.getLabel()).equals(wanted)) {
                return app;
            }
        }
        return null;
    }

    @Override
    public List<InstalledApp> searchInstalledApps(String keyword, int limit) {
        List<InstalledApp> result = new ArrayList<>();
        String wanted = normalize(keyword);
        if (wanted.isEmpty() || limit <= 0) {
            return result;
        }
        for (InstalledApp app : cache) {
            if (normalize(app.getLabel()).contains(wanted)) {
                result.add(app);
                if (result.size() >= limit) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public String resolvePackageName(SubscribeItem item) {
        if (item == null) {
            return "";
        }
        if (!loaded) {
            // 아직 앱 목록을 읽기 전이면 저장된 값을 쓰고, 다음 호출을 위해 목록을 읽어 둡니다.
            refresh();
            return item.getPackageName();
        }
        if (item.isLinkedToApp()) {
            for (InstalledApp app : cache) {
                if (app.getPackageName().equals(item.getPackageName())) {
                    return app.getPackageName();
                }
            }
        }
        InstalledApp app = findInstalledApp(item.getServiceName());
        return app == null ? "" : app.getPackageName();
    }

    /** 비교용 문자열: 소문자로 바꾸고 공백을 모두 뺍니다. */
    static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }
}
