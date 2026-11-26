package com.db3k.diariodibordo.di

import android.content.Context
import com.db3k.diariodibordo.data.db.AppDatabase
import com.db3k.diariodibordo.data.db.PostDao
import com.db3k.diariodibordo.data.db.NotificationDao
import com.db3k.diariodibordo.data.preferences.UserPreferences
import com.db3k.diariodibordo.data.repository.PostRepository
import com.db3k.diariodibordo.data.repository.NotificationRepository
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Hilt module for database and repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun providePostDao(database: AppDatabase): PostDao {
        return database.postDao()
    }

    @Provides
    @Singleton
    fun provideNotificationDao(database: AppDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    @Singleton
    fun providePostRepository(postDao: PostDao, apiService: com.db3k.diariodibordo.network.ApiService): PostRepository {
        return PostRepository(postDao, apiService)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(notificationDao: NotificationDao): NotificationRepository {
        return NotificationRepository(notificationDao)
    }

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences.create(context)
    }
}