package com.db3k.diariodibordo.di

import android.content.Context
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Application-level Hilt module
 */
@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    @ApplicationContext
    fun provideApplicationContext(): Context = DB3KApplication.getContext()
}