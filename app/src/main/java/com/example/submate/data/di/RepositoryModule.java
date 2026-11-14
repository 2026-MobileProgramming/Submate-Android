package com.example.submate.data.di;

import com.example.submate.data.network.NetworkModule;
import com.example.submate.data.repository.subscribelist.SubscribeRepositoryImpl;
import com.example.submate.data.service.subscribelist.SubscribeMockServiceImpl;
import com.example.submate.data.service.subscribelist.SubscribeService;
import com.example.submate.data.service.subscribelist.SubscribeServiceImpl;
import com.example.submate.domain.repository.SubscribeRepository;
import javax.inject.Provider;
import javax.inject.Singleton;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {
    @Provides
    @Singleton
    static SubscribeService provideSubscribeService(
            Provider<SubscribeServiceImpl> remoteService,
            Provider<SubscribeMockServiceImpl> mockService) {
        return NetworkModule.USE_MOCK_SERVER ? mockService.get() : remoteService.get();
    }

    @Binds
    @Singleton
    public abstract SubscribeRepository bindSubscribeRepository(SubscribeRepositoryImpl implementation);
}
