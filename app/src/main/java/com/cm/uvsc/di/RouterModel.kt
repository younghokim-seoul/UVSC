package com.cm.uvsc.di

import com.cm.uvsc.route.Navigator
import com.cm.uvsc.route.NavigatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class RouterModel {

    @Binds
    @ActivityRetainedScoped
    abstract fun provideNavigator(
        navigator: NavigatorImpl
    ): Navigator
}
