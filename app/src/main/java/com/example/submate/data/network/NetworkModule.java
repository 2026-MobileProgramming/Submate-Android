package com.example.submate.data.network;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/** 네트워크 관련 의존성을 등록하는 모듈입니다. */
@Module
@InstallIn(SingletonComponent.class)
public abstract class NetworkModule {
    private NetworkModule() {
    }
}
