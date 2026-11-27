package com.db3k.diariodibordo.di

import android.content.Context
import com.db3k.diariodibordo.data.preferences.UserPreferences
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Application-level Hilt module for preferences
 */
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    @ApplicationContext
    fun provideUserPreferences(context: Context): UserPreferences {
        return UserPreferences.create(context)
    }
}