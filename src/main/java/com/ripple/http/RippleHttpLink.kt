package com.ripple.http

import com.ripple.http.base.IRequestParams
import com.ripple.http.callback.OnHttpResult
import com.ripple.http.impl.HttpTask

/**
 * Author: fanyafeng
 * Data: 2020/8/10 10:16
 * Email: fanyafeng@live.cn
 * Description:
 */
class RippleHttpLink internal constructor(private val httpTask: HttpTask) {

    /**
     * 同时调用get请求
     *
     */
    fun <T> withGet(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>
    ): RippleHttpLink {
        httpTask.withGet(params, callback)
        return this
    }

    /**
     * 上一个任务完成后
     * 再去掉用get请求
     */
    fun <T> thenGet(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>
    ): RippleHttpLink {
        httpTask.thenGet(params, callback)
        return this
    }

    /**
     * 同时调用post请求
     */
    fun <T> withPost(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>
    ): RippleHttpLink {
        httpTask.withPost(params, callback)
        return this
    }

    /**
     * 上一个任务完成后再去调用post请求
     */
    fun <T> thenPost(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>
    ): RippleHttpLink {
        httpTask.thenPost(params, callback)
        return this
    }


    /**
     * .thenPost or .thenGet肯定是队列的头
     * .withPost or .withGet肯定为列表的子
     */
    fun start() {
        httpTask.start()
    }

}