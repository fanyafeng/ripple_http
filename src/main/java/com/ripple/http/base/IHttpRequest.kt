package com.ripple.http.base

import com.ripple.http.callback.OnHttpResult


/**
 * Author: fanyafeng
 * Data: 2020/7/20 19:40
 * Email: fanyafeng@live.cn
 * Description:
 *
 * http的请求接口
 * 一般http有get，post等
 */
interface IHttpRequest {

    /**
     * get请求
     */
    fun <T> get(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>
    )

    fun <T> withGet(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>
    )

    fun <T> thenGet(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>
    )

    /**
     * post请求
     */
    fun <T> post(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>
    )

    fun <T> withPost(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>
    )

    fun <T> thenPost(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>
    )
}