package com.example.submate.di;

import com.example.submate.data.repository.SubscribeRepositoryImpl;
import com.example.submate.domain.repository.SubscribeRepository;
import javax.inject.Singleton;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {
    @Binds
    @Singleton
    public abstract SubscribeRepository bindSubscribeRepository(SubscribeRepositoryImpl implementation);
}
