package io.pnut.gamma.di

import dagger.Subcomponent

@Subcomponent(modules = [FakeUseCaseModule::class])
interface FakeUseCaseComponent {
  @Subcomponent.Builder
  interface Builder {
    fun build(): FakeUseCaseComponent
  }
}