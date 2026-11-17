package com.example.submate.data.repository.installedapp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 서비스 목록(ServicePreset)에 있는 서비스의 안드로이드 앱 패키지명입니다.
 * 앱 표시 이름은 기기 언어나 앱 업데이트에 따라 달라질 수 있지만 패키지명은 바뀌지 않으므로,
 * 목록에 있는 서비스는 이름 비교보다 먼저 이 표로 설치된 앱을 찾습니다.
 * 한 서비스에 앱이 여러 개면 앞에 있는 것부터 찾습니다.
 * 서비스를 추가할 때는 static 블록에 put("서비스 목록의 이름", "패키지명", ...) 한 줄을 넣어주세요.
 */
public final class ServicePackages {
    private static final Map<String, List<String>> PACKAGES = new HashMap<>();

    static {
        // OTT
        put("Disney+", "com.disney.disneyplus");
        put("Netflix", "com.netflix.mediaclient");
        put("TVING", "net.cj.cjhv.gs.tving");
        put("wavve", "kr.co.captv.pooqV2");
        put("쿠팡플레이", "com.coupang.mobile.play");
        put("라프텔", "laftel.net.laftel");
        put("YouTube", "com.google.android.youtube");
        // Apple TV는 안드로이드 앱 패키지명을 아직 확인하지 못해 표에 넣지 않았습니다. (확인되면 put 한 줄 추가)
        // 음악
        put("멜론", "com.iloen.melon");
        put("벅스", "com.neowiz.android.bugs");
        put("YouTube Music Premium", "com.google.android.apps.youtube.music");
        put("Spotify", "com.spotify.music");
        put("Apple Music", "com.apple.android.music");
        put("지니뮤직", "com.ktmusic.geniemusic");
        put("FLO", "skplanet.musicmate");
        // 외식
        put("배달의민족", "com.sampleapp");
        put("쿠팡이츠", "com.coupang.mobile.eats");
        put("요기요", "com.fineapp.yogiyo");
        // 쇼핑 (멤버십은 그 멤버십을 쓰는 쇼핑 앱으로 연결합니다)
        put("쿠팡 와우", "com.coupang.mobile");
        put("네이버플러스 멤버십", "com.navercorp.navershopping");
        put("컬리멤버스", "com.dbs.kurly.m2");
        put("SSG 유니버스", "kr.co.ssg");
        // AI
        put("ChatGPT", "com.openai.chatgpt");
        put("Claude", "com.anthropic.claude");
        put("Gemini", "com.google.android.apps.bard");
        put("Copilot", "com.microsoft.copilot");
        put("Perplexity", "ai.perplexity.app.android");
        // 디지털 콘텐츠
        put("밀리의 서재", "kr.co.millie.millieshelf");
        put("리디셀렉트", "com.initialcoms.ridi");
        // 웹툰과 시리즈는 같은 쿠키(자동충전)를 쓰므로 한 항목입니다. 둘 다 설치돼 있으면 앞의 웹툰 앱이 연결됩니다.
        put("네이버 웹툰·시리즈", "com.nhn.android.webtoon", "com.nhn.android.nbooks");
        put("카카오페이지", "com.kakao.page");
        // 기타 (클라우드 저장공간). iCloud+는 안드로이드 앱이 없어 표에 넣지 않습니다.
        put("Google One", "com.google.android.apps.subscriptions.red");
        put("네이버 MYBOX", "com.nhn.android.ndrive");
    }

    private ServicePackages() {
    }

    private static void put(String serviceName, String... packageNames) {
        PACKAGES.put(normalize(serviceName), Collections.unmodifiableList(Arrays.asList(packageNames)));
    }

    /** 서비스명에 해당하는 패키지명 목록. 표에 없는 서비스(직접 입력 등)면 빈 목록 */
    public static List<String> get(String serviceName) {
        List<String> packageNames = PACKAGES.get(normalize(serviceName));
        return packageNames == null ? Collections.emptyList() : packageNames;
    }

    /** 비교용 문자열: 소문자로 바꾸고 공백을 모두 뺍니다. (설치된 앱 이름 비교와 같은 기준) */
    static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }
}
