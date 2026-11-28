package com.example.submate.data.network;

import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.submate.BuildConfig;

/** 네트워크 관련 의존성을 등록하는 모듈입니다. */
@Module
@InstallIn(SingletonComponent.class)
public abstract class NetworkModule {
    // 서버 주소는 local.properties의 BASE_URL 값을 사용합니다. (반드시 '/'로 끝나야 합니다)
    public static final String BASE_URL = BuildConfig.BASE_URL;
    public static final String EXCHANGE_RATE_URL = "https://api.frankfurter.dev/";

    // true: 구독 데이터를 기기 안에 저장합니다. (서버 준비 전)
    // false: BASE_URL의 서버와 통신합니다.
    public static final boolean USE_LOCAL_DATA = true;

    private NetworkModule() {
    }

    @Provides
    @Singleton
    static Retrofit provideRetrofit() {
        return createRetrofit(BASE_URL);
    }

    @Provides
    @Singleton
    static SubscribeApi provideSubscribeApi(Retrofit retrofit) {
        return retrofit.create(SubscribeApi.class);
    }

    // 환율 API는 주소가 달라서 Retrofit을 따로 만듭니다.
    @Provides
    @Singleton
    static ExchangeRateApi provideExchangeRateApi() {
        return createRetrofit(EXCHANGE_RATE_URL).create(ExchangeRateApi.class);
    }

    private static Retrofit createRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
