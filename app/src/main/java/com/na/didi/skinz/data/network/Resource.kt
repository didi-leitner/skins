package com.na.didi.skinz.data.network



sealed class Resource<T> {

    data class Success<T>(val data: T?) : Resource<T>()

    data class Error<T>(val msg: String?) : Resource<T>()

}