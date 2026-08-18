package io.pnut.gamma.util

import com.squareup.moshi.JsonQualifier


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
@JsonQualifier
annotation class MicroTimestamp
