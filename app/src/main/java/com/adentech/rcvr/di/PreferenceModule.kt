package com.adentech.rcvr.di


import com.adentech.rcvr.preferences.Preferences
import com.adentech.rcvr.preferences.RecoveryPreferenceManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferenceModule {

    @Binds
    abstract fun providePreferences(preferences: RecoveryPreferenceManager): Preferences
}