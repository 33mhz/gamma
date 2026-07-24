package io.pnut.gamma.di

import dagger.Component
import io.pnut.gamma.GammaApplication
import io.pnut.gamma.presentation.activity.EntryActivityTest
import javax.inject.Singleton

@Component(
  modules = [
    FakeActivityModule::class,
    FakeAppModule::class,
    FakeUseCaseModule::class
  ]
)
@Singleton
interface TestAppComponent {
  fun inject(entryActivityTest: EntryActivityTest)
  fun inject(application: GammaApplication)
  fun fakeUseCaseComponentBuilder(): FakeUseCaseComponent.Builder
}