package com.pavlovalexey.pleinair.di

import com.example.myapp.data.UserRepository
import com.example.myapp.data.UserRepositoryImpl
import com.example.myapp.data.local.UserDao
import com.example.myapp.data.remote.ApiService
import com.example.myapp.domain.GetUsersUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService, userDao: UserDao): UserRepository {
        return UserRepositoryImpl(userDao, apiService)
    }

    @Provides
    @Singleton
    fun provideGetUsersUseCase(userRepository: UserRepository): GetUsersUseCase {
        return GetUsersUseCase(userRepository)
    }
}