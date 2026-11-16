package com.example.submate.domain.model;

/** 기기에 설치된 앱 하나. label은 홈 화면에 보이는 앱 이름, packageName은 사용량 조회에 쓰는 고유 이름입니다. */
public final class InstalledApp {
    private final String label;
    private final String packageName;

    public InstalledApp(String label, String packageName) {
        this.label = label;
        this.packageName = packageName;
    }

    public String getLabel() {
        return label;
    }

    public String getPackageName() {
        return packageName;
    }
}
