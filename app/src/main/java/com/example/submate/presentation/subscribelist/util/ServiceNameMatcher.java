package com.example.submate.presentation.subscribelist.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 직접 입력한 서비스명을 이미 있는 서비스 이름으로 맞춥니다.
 * 규칙: 대소문자와 띄어쓰기는 무시하고 비교합니다.
 *  - "netflix", "NETFLIX", "net flix" → "Netflix" (서비스 목록에 있는 이름)
 *  - "넷플릭스" → "Netflix" (아래 별명 표에 있는 이름)
 *  - 목록·별명 표에 없는 이름은 입력한 그대로 둡니다. (앞뒤 공백만 정리)
 * 입력 중에는 글자를 바꾸지 않고, 저장할 때와 입력칸에서 벗어날 때만 바꿉니다.
 */
public final class ServiceNameMatcher {

    /** 별명 → 서비스 목록의 정식 이름. 별명은 비교할 때 대소문자·띄어쓰기를 무시합니다. */
    private static final Map<String, String> ALIASES = new LinkedHashMap<>();

    static {
        // OTT
        alias("Disney+", "디즈니플러스", "디즈니+", "디즈니", "disneyplus", "disney");
        alias("Netflix", "넷플릭스", "넷플");
        alias("TVING", "티빙");
        alias("wavve", "웨이브");
        alias("쿠팡플레이", "coupangplay", "coupang play");
        alias("라프텔", "laftel");
        alias("YouTube", "유튜브", "유튜브프리미엄", "youtubepremium");
        // 음악
        alias("멜론", "melon");
        alias("벅스", "bugs");
        alias("YouTube Music Premium", "유튜브뮤직", "유튜브뮤직프리미엄", "youtubemusic");
        alias("Spotify", "스포티파이");
        alias("Apple Music", "애플뮤직");
        alias("지니뮤직", "지니", "genie", "geniemusic");
        alias("FLO", "플로");
        // 배달
        alias("배달의민족", "배민", "baemin");
        alias("쿠팡이츠", "coupangeats");
        alias("요기요", "yogiyo");
        // 쇼핑
        alias("쿠팡 와우", "coupangwow");
        alias("N+스토어", "네이버플러스", "네이버플러스스토어", "naverplus");
        alias("컬리멤버스", "컬리", "kurly");
        alias("SSG 유니버스", "신세계유니버스");
        // AI
        alias("ChatGPT", "챗지피티", "챗gpt", "gpt");
        alias("Claude", "클로드");
        alias("Gemini", "제미나이", "제미니");
        alias("Copilot", "코파일럿");
        alias("Perplexity", "퍼플렉시티");
        // 디지털 콘텐츠
        alias("밀리의 서재", "밀리", "millie");
        alias("리디셀렉트", "리디", "ridi", "ridiselect");
        alias("네이버 웹툰", "naverwebtoon");
        alias("카카오페이지", "kakaopage");
    }

    private ServiceNameMatcher() {
    }

    private static void alias(String name, String... aliases) {
        for (String a : aliases) {
            ALIASES.put(normalize(a), name);
        }
    }

    /** 비교용 문자열: 소문자로 바꾸고 공백을 모두 뺍니다. (설치된 앱 연결 규칙과 같은 기준) */
    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    /**
     * 서비스 목록이나 별명 표에서 같은 이름을 찾아 정식 이름을 돌려줍니다. 없으면 null.
     * (설치된 앱 이름까지 맞추는 건 ViewModel에서 이어서 합니다.)
     */
    public static String findPresetName(String input) {
        String key = normalize(input);
        if (key.isEmpty()) {
            return null;
        }
        for (String name : ServicePreset.getAllServices()) {
            if (normalize(name).equals(key)) {
                return name;
            }
        }
        return ALIASES.get(key);
    }

    /**
     * 입력한 글자가 들어간 서비스 목록 이름을 추천합니다. 별명에 들어가 있어도 추천합니다.
     * 예) "net" → Netflix, "넷" → Netflix, "유튜브" → YouTube, YouTube Music Premium
     */
    public static List<String> searchPresetNames(String keyword, int limit) {
        List<String> result = new ArrayList<>();
        String key = normalize(keyword);
        if (key.isEmpty() || limit <= 0) {
            return result;
        }
        for (String name : ServicePreset.getAllServices()) {
            if (normalize(name).contains(key)) {
                addOnce(result, name);
            }
        }
        for (Map.Entry<String, String> entry : ALIASES.entrySet()) {
            if (entry.getKey().contains(key)) {
                addOnce(result, entry.getValue());
            }
        }
        return result.size() > limit ? new ArrayList<>(result.subList(0, limit)) : result;
    }

    /** 대소문자·띄어쓰기만 다른 이름이 이미 있으면 넣지 않습니다. */
    public static void addOnce(List<String> list, String name) {
        String key = normalize(name);
        for (String existing : list) {
            if (normalize(existing).equals(key)) {
                return;
            }
        }
        list.add(name);
    }
}
