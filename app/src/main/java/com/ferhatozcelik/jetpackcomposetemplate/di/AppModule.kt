package com.ferhatozcelik.jetpackcomposetemplate.di

import com.ferhatozcelik.jetpackcomposetemplate.data.remote.GoldApiService
import com.ferhatozcelik.jetpackcomposetemplate.data.repository.GoldRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ... Keep existing providers like provideRetrofit here ...

    @Provides
    @Singleton
    fun provideGoldApiService(retrofit: Retrofit): GoldApiService {
        return retrofit.create(GoldApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGoldRepository(apiService: GoldApiService): GoldRepository {
        return GoldRepository(apiService)
    }
}
