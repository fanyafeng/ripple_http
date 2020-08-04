package com.ripple.http

import com.ripple.http.base.IRequestParams
import com.ripple.http.callback.OnHttpResult
import com.ripple.http.impl.HttpTask
import com.ripple.http.link.IHttpLink
import com.ripple.http.link.impl.HttpLinkImpl
import okhttp3.Call
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque

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
    fun <T> get(params: IRequestParams.IHttpRequestParams, callback: OnHttpResult<T>): IHttpLink {
        val httpLinkImpl = HttpLinkImpl()
        HttpTask.get(params, callback)
        return httpLinkImpl
    }

    /**
     * post请求
     */
    fun <T> post(params: IRequestParams.IHttpRequestParams, callback: OnHttpResult<T>) {
        HttpTask.post(params, callback)
    }

    /**
     * 同时调用get请求
     *
     */
    fun <T> withGet(deque: BlockingDeque<Call>, list: MutableList<Call>) {
    }

    /**
     * 上一个任务完成后
     * 再去掉用get请求
     */
    fun <T> thenGet(deque: BlockingDeque<Call>) {
        //new MutableList<Call>

    }

    /**
     * 同时调用post请求
     */
    fun <T> withPost(deque: BlockingDeque<Call>, list: MutableList<Call>) {

    }

    /**
     * 上一个任务完成后再去调用post请求
     */
    fun <T> thenPost(deque: BlockingDeque<Call>) {
        //new MutableList<Call>

    }


    /**
     * .thenPost or .thenGet肯定是队列的头
     * .withPost or .withGet肯定为列表的子
     */
}