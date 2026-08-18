package io.pnut.gamma.domain.usecases

abstract class UseCase<out Type, in Params> where Type : Any {
    abstract suspend fun run(params: Params): Type
}
