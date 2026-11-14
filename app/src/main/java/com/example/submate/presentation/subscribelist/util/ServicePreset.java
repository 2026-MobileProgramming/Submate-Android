package com.example.submate.presentation.subscribelist.util;

import com.example.submate.domain.model.SubscribeCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 카테고리별 서비스명 선택지입니다. 마지막에는 항상 "직접 입력"이 붙습니다. */
public final class ServicePreset {
    public static final String DIRECT_INPUT = "직접 입력";

    private ServicePreset() {
    }

    public static List<String> getServices(SubscribeCategory category) {
        List<String> services = new ArrayList<>();
        switch (category) {
            case OTT:
                services.addAll(Arrays.asList("Disney+", "Netflix", "TVING", "wavve", "쿠팡플레이",
                        "라프텔", "YouTube"));
                break;
            case MUSIC:
                services.addAll(Arrays.asList("멜론", "Spotify", "Apple Music", "지니뮤직", "FLO"));
                break;
            case FOOD:
                services.addAll(Arrays.asList("배달의민족", "쿠팡이츠", "요기요"));
                break;
            case SHOPPING:
                services.addAll(Arrays.asList("쿠팡 와우", "N+스토어", "컬리멤버스", "SSG 유니버스"));
                break;
            case AI:
                services.addAll(Arrays.asList("ChatGPT", "Gemini", "Copilot", "Perplexity"));
                break;
            case DIGITAL_CONTENT:
                services.addAll(Arrays.asList("밀리의 서재", "리디셀렉트", "네이버 웹툰", "카카오페이지"));
                break;
            default:
                break;
        }
        services.add(DIRECT_INPUT);
        return services;
    }
}
