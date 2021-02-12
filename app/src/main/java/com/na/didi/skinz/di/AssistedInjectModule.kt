package com.na.didi.skinz.di



import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

// AssistedInject puts all assisted bindings in the same module.
@InstallIn(FragmentComponent::class)
@AssistedModule
@Module(includes = [AssistedInject_AssistedInjectModule::class])
// Needed until AssistedInject is incorporated into Dagger
interface AssistedInjectModule



