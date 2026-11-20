package com.example.submate.presentation.subscribelist.util;

import com.example.submate.domain.model.SubscribeCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 카테고리별 서비스명 선택지입니다. 많이 쓰는 서비스 순서로 나열하고, 마지막에는 항상 "직접 입력"이 붙습니다. */
public final class ServicePreset {
    public static final String DIRECT_INPUT = "직접 입력";

    private ServicePreset() {
    }

    public static List<String> getServices(SubscribeCategory category) {
        List<String> services = new ArrayList<>();
        switch (category) {
            case OTT:
                services.addAll(Arrays.asList("Netflix", "YouTube", "TVING", "Disney+", "라프텔",
                        "쿠팡플레이", "wavve", "Apple TV"));
                break;
            case MUSIC:
                services.addAll(Arrays.asList("멜론", "YouTube Music Premium", "Spotify", "FLO",
                        "지니뮤직", "벅스", "Apple Music"));
                break;
            case FOOD:
                services.addAll(Arrays.asList("배달의민족", "쿠팡이츠", "요기요"));
                break;
            case SHOPPING:
                services.addAll(Arrays.asList("쿠팡 와우", "네이버플러스 멤버십", "컬리멤버스", "SSG 유니버스"));
                break;
            case AI:
                services.addAll(Arrays.asList("ChatGPT", "Gemini", "Claude", "Copilot", "Perplexity"));
                break;
            case DIGITAL_CONTENT:
                services.addAll(Arrays.asList("네이버 웹툰·시리즈", "카카오페이지", "밀리의 서재", "리디셀렉트"));
                break;
            case ETC:
                services.addAll(Arrays.asList("Google One", "네이버 MYBOX", "iCloud+"));
                break;
            default:
                break;
        }
        services.add(DIRECT_INPUT);
        return services;
    }

    /** 모든 카테고리의 서비스명 ("직접 입력" 제외). 직접 입력한 이름을 정식 이름으로 맞출 때 씁니다. */
    public static List<String> getAllServices() {
        List<String> all = new ArrayList<>();
        for (SubscribeCategory category : SubscribeCategory.values()) {
            for (String service : getServices(category)) {
                if (!DIRECT_INPUT.equals(service)) {
                    all.add(service);
                }
            }
        }
        return all;
    }
}
