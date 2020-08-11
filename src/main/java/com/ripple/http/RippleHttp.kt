package com.ripple.http

import com.ripple.http.base.HttpMethod
import com.ripple.http.base.IRequestParams
import com.ripple.http.callback.OnHttpResult
import com.ripple.http.impl.HttpTask
import com.ripple.http.impl.OkHttpClientDelegate
import com.ripple.http.link.HttpLinkModel
import com.ripple.tool.kttypelians.SuccessLambda
import okhttp3.Call
import java.util.*
import java.util.concurrent.BlockingDeque

/**
 * Author: fanyafeng
 * Data: 2020/7/20 19:46
 * Email: fanyafeng@live.cn
 * Description:
 *
 * deque是唯一的
 */
object RippleHttp {

    /**
     * get请求
     *
     */
    fun <T> get(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>
    ): RippleHttpLink {
        val httpTask = HttpTask()
        httpTask.get(params, callback)
        return RippleHttpLink(httpTask)
    }

    /**
     * post请求
     */
    fun <T> post(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>
    ): RippleHttpLink {
        val httpTask = HttpTask()
        httpTask.post(params, callback)
        return RippleHttpLink(httpTask)
    }

    internal fun <T> request(
        params: IRequestParams.IHttpRequestParams,
        lambda: SuccessLambda<Any>,
        callback: OnHttpResult<T>
    ): RippleHttpLinkKotlin {
        val httpTask = HttpTask()
        if (params.method == HttpMethod.POST) {
            httpTask.post(params, callback, lambda)
        } else {
            httpTask.get(params, callback, lambda)
        }
        return RippleHttpLinkKotlin(httpTask)
    }

}