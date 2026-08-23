package com.ferhatozcelik.jetpackcomposetemplate.di

import com.ferhatozcelik.jetpackcomposetemplate.data.remote.GoldApiService
import com.ferhatozcelik.jetpackcomposetemplate.data.repository.GoldRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.binance.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

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
