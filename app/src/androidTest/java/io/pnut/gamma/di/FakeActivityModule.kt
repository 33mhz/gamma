package io.pnut.gamma.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.pnut.gamma.presentation.activity.EntryActivityTest

@Module
abstract class FakeActivityModule {
  @ContributesAndroidInjector
  abstract fun contributeEntryActivityTest(): EntryActivityTest

}