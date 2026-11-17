package com.example.submate.data.service.installedapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import com.example.submate.domain.model.InstalledApp;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.inject.Inject;

/**
 * PackageManager.queryIntentActivities로 실행 가능한 앱을 찾고,
 * ResolveInfo.loadLabel로 홈 화면에 표시되는 앱 이름을 가져옵니다.
 * Android 11 이상에서 다른 앱이 보이려면 AndroidManifest의 <queries> 선언이 필요합니다.
 */
public class InstalledAppServiceImpl implements InstalledAppService {
    private final Context context;

    @Inject
    public InstalledAppServiceImpl(@ApplicationContext Context context) {
        this.context = context;
    }

    @Override
    public List<InstalledApp> getInstalledApps() {
        PackageManager packageManager = context.getPackageManager();
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            resolveInfos = packageManager.queryIntentActivities(
                    launcherIntent, PackageManager.ResolveInfoFlags.of(0));
        } else {
            resolveInfos = packageManager.queryIntentActivities(launcherIntent, 0);
        }

        List<InstalledApp> apps = new ArrayList<>();
        Set<String> seenPackages = new HashSet<>();
        String ownPackage = context.getPackageName();
        for (ResolveInfo info : resolveInfos) {
            if (info.activityInfo == null) {
                continue;
            }
            String packageName = info.activityInfo.packageName;
            // 한 앱에 실행 아이콘이 여러 개여도 한 번만 넣고, 섭메이트 자신은 뺍니다.
            if (packageName == null || packageName.equals(ownPackage) || !seenPackages.add(packageName)) {
                continue;
            }
            CharSequence label = info.loadLabel(packageManager);
            String labelText = label == null ? "" : label.toString().trim();
            if (!labelText.isEmpty()) {
                apps.add(new InstalledApp(labelText, packageName));
            }
        }

        Collator collator = Collator.getInstance(Locale.KOREAN);
        apps.sort((a, b) -> collator.compare(a.getLabel(), b.getLabel()));
        return apps;
    }
}
