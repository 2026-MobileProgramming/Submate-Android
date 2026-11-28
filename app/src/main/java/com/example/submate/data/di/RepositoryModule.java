package com.example.submate.data.di;

import com.example.submate.data.network.NetworkModule;
import com.example.submate.data.repository.auth.AuthRepositoryImpl;
import com.example.submate.data.repository.exchangerate.ExchangeRateRepositoryImpl;
import com.example.submate.data.repository.installedapp.InstalledAppRepositoryImpl;
import com.example.submate.data.repository.subscribelist.SubscribeRepositoryImpl;
import com.example.submate.data.service.auth.AuthService;
import com.example.submate.data.service.auth.AuthServiceImpl;
import com.example.submate.data.service.exchangerate.ExchangeRateService;
import com.example.submate.data.service.exchangerate.ExchangeRateServiceImpl;
import com.example.submate.data.service.installedapp.InstalledAppService;
import com.example.submate.data.service.installedapp.InstalledAppServiceImpl;
import com.example.submate.data.service.subscribelist.SubscribeLocalServiceImpl;
import com.example.submate.data.service.subscribelist.SubscribeService;
import com.example.submate.data.service.subscribelist.SubscribeServiceImpl;
import com.example.submate.domain.repository.AuthRepository;
import com.example.submate.domain.repository.ExchangeRateRepository;
import com.example.submate.domain.repository.InstalledAppRepository;
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
            Provider<SubscribeLocalServiceImpl> localService) {
        return NetworkModule.USE_LOCAL_DATA ? localService.get() : remoteService.get();
    }

    @Binds
    @Singleton
    public abstract SubscribeRepository bindSubscribeRepository(SubscribeRepositoryImpl implementation);

    @Binds
    @Singleton
    public abstract ExchangeRateService bindExchangeRateService(ExchangeRateServiceImpl implementation);

    @Binds
    @Singleton
    public abstract ExchangeRateRepository bindExchangeRateRepository(ExchangeRateRepositoryImpl implementation);

    @Binds
    @Singleton
    public abstract InstalledAppService bindInstalledAppService(InstalledAppServiceImpl implementation);

    @Binds
    @Singleton
    public abstract InstalledAppRepository bindInstalledAppRepository(InstalledAppRepositoryImpl implementation);

    @Binds
    @Singleton
    public abstract AuthService bindAuthService(AuthServiceImpl implementation);

    @Binds
    @Singleton
    public abstract AuthRepository bindAuthRepository(AuthRepositoryImpl implementation);
}
