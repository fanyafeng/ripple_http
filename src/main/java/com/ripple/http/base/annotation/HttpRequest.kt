package com.ripple.http.base.annotation

import com.ripple.http.IParamsBuilder
import com.ripple.http.base.IRequestParams
import com.ripple.http.base.abs.AbsHttpParamsBuilder
import kotlin.reflect.KClass


/**
 * Author: fanyafeng
 * Data: 2020/7/29 19:00
 * Email: fanyafeng@live.cn
 * Description:
 */
annotation class HttpRequest(
    val value: String,
    val host: String = "",
    val version: String = "",
    val builder: KClass<out IParamsBuilder<IRequestParams.IHttpRequestParams>> = AbsHttpParamsBuilder::class
)