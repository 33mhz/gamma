package io.pnut.gamma.domain.usecases

abstract class AsyncUseCase<out Type, in Params> where Type : Any {

    abstract suspend fun run(params: Params): Type

}
