package com.na.didi.skinz.data.network


data class ApiResponse<T>(
    val code: Int,
    val body: T?,
    val errorMessage: String?
) {

    fun isSuccessful(): Boolean {
        return code >= 200 && code < 300
    }

}

