package io.pnut.gamma.di

import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import io.pnut.gamma.presentation.activity.EntryActivityTest
import javax.inject.Singleton

@Component(
  modules = [
    AndroidSupportInjectionModule::class,
    ActivityModule::class,
    FakeAppModule::class,
    FakeUseCaseModule::class
  ]
)
@Singleton
interface TestAppComponent : AppComponent {
  fun inject(entryActivityTest: EntryActivityTest)
  fun fakeUseCaseComponentBuilder(): FakeUseCaseComponent.Builder
}