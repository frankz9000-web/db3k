package com.db3k.diariodibordo.di

import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Module

/**
 * Application-level Hilt module
 * Application context is provided by Hilt's built-in ApplicationContextModule
 */
@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule