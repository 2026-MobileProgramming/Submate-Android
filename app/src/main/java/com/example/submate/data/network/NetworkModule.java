package com.example.submate.data.network;

import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** 네트워크 관련 의존성을 등록하는 모듈입니다. */
@Module
@InstallIn(SingletonComponent.class)
public abstract class NetworkModule {
    // TODO: 서버 배포 후 실제 주소로 변경 (반드시 '/'로 끝나야 합니다)
    public static final String BASE_URL = "http://10.0.2.2:8080/api/";

    // 서버가 준비되기 전까지는 true: 앱 내부 임시 데이터로 동작합니다.
    public static final boolean USE_MOCK_SERVER = true;

    private NetworkModule() {
    }

    @Provides
    @Singleton
    static Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    static SubscribeApi provideSubscribeApi(Retrofit retrofit) {
        return retrofit.create(SubscribeApi.class);
    }
}
