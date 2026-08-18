package io.pnut.gamma.domain.usecases

abstract class SyncUseCase<out Type, in Params> where Type : Any {
    abstract fun run(params: Params): Type
}
