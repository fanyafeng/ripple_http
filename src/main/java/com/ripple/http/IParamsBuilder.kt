package com.ripple.http

import com.ripple.http.base.IRequestParams
import com.ripple.http.base.annotation.HttpRequest
import javax.net.ssl.SSLSocketFactory


/**
 * Author: fanyafeng
 * Data: 2020/7/29 19:01
 * Email: fanyafeng@live.cn
 * Description:
 */
interface IParamsBuilder<in T : IRequestParams> {
    /**
     * 根据参数，构建请求的uri
     */
    fun buildUri(param: T, httpRequest: HttpRequest?, path: String? = null): String?

    /**获取sslSocketFactory,默认返回null*/
    fun getSSLSocketFactory(): SSLSocketFactory? = null

    /**构建参数，添加通用头信息等*/
    fun buildParams(params: T)

    /**构建签名信息，默认不做任何处理*/
    fun buildSign(param: T) = Unit

    /**获取用户UA，默认为null*/
    fun getUserAgent(): String? = null
}