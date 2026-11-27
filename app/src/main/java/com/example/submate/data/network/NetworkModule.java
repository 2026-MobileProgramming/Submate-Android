package com.example.submate.data.network;

import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** 네트워크 관련 의존성을 등록하는 모듈입니다. */
@Module
@InstallIn(SingletonComponent.class)
public abstract class NetworkModule {
    // TODO: 서버 배포 후 실제 주소로 변경 (반드시 '/'로 끝나야 합니다)
    public static final String BASE_URL = "http://10.0.2.2:8080/api/";
    public static final String EXCHANGE_RATE_URL = "https://api.frankfurter.dev/";

    // true: 구독 데이터를 기기 안에 저장합니다. (서버 준비 전)
    // false: BASE_URL의 서버와 통신합니다.
    public static final boolean USE_LOCAL_DATA = true;

    private NetworkModule() {
    }

    @Provides
    @Singleton
    static OkHttpClient provideOkHttpClient(TokenManager tokenManager) {
        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String token = tokenManager.getAccessToken();
                    if (token != null && !token.isEmpty()) {
                        Request authorized = original.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(authorized);
                    }
                    return chain.proceed(original);
                })
                .build();
    }

    @Provides
    @Singleton
    static Retrofit provideRetrofit() {
        return createRetrofit(BASE_URL);
    }

    @Provides
    @Singleton
    static AuthApi provideAuthApi(Retrofit retrofit) {
        return retrofit.create(AuthApi.class);
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
