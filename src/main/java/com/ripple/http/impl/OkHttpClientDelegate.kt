package com.ripple.http.impl

import com.ripple.http.base.HttpMethod
import com.ripple.http.base.IRequestParams
import com.ripple.http.RippleHttpClient
import com.ripple.http.interceptor.GzipRequestInterceptor
import com.ripple.http.util.getRequestBody
import com.ripple.log.extend.logD
import com.ripple.log.extend.logE
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.concurrent.TimeUnit


/**
 * Author: fanyafeng
 * Data: 2020/8/3 13:48
 * Email: fanyafeng@live.cn
 * Description:
 */
internal class OkHttpClientDelegate {

    /**
     * 同步get请求
     * 返回结果定义为call
     * 为了链式调用可以取消任务
     */
    fun get(params: IRequestParams.IHttpRequestParams): Call {
        val clientResult = getClientResult(params)
        val requestResult = getRequestResult(params, null, HttpMethod.GET)
        return clientResult.newCall(requestResult)
    }

    /**
     * 同步post请求
     * 返回结果为call
     * 为了链式调用可以取消任务
     */
    fun post(params: IRequestParams.IHttpRequestParams): Call {
        val requestBody = getRequestBody(params)
        val clientResult = getClientResult(params)
        val requestResult = getRequestResult(params, requestBody, HttpMethod.POST)
        return clientResult.newCall(requestResult)
    }

    fun request(params: IRequestParams.IHttpRequestParams): Call {
        val clientResult = getClientResult(params)
        val method = params.method
        val requestResult = getRequestResult(
            params,
            if (method == HttpMethod.GET) null else getRequestBody(params),
            params.method
        )
        return clientResult.newCall(requestResult)
    }


    /**
     * 获取http client
     */
    private fun getClientResult(params: IRequestParams.IHttpRequestParams): OkHttpClient {
        val client = RippleHttpClient.getInstance().newBuilder()
        client.readTimeout(params.getReadTimeOut(), TimeUnit.MILLISECONDS)
        client.writeTimeout(params.getWriteTimeOut(), TimeUnit.MILLISECONDS)
        client.connectTimeout(params.getConnectTimeOut(), TimeUnit.MILLISECONDS)
        client.addInterceptor(GzipRequestInterceptor())
        /**
         * 添加信任证书
         */
        if (params.getSSLSocketFactory() != null && params.getX509TrustManager() != null) {
            client.sslSocketFactory(params.getSSLSocketFactory()!!, params.getX509TrustManager()!!)
        } else {
            logE("ssl证书信息未设置")
        }
        return client.build()
    }

    /**
     * 获取request结果
     */
    private fun getRequestResult(
        params: IRequestParams.IHttpRequestParams,
        requestBody: RequestBody?,
        method: HttpMethod
    ): Request {
        /**
         * 构造带有path的url
         * 这里采用buffer，主要因为它是线程安全的而且高效
         */
        val httpUrl = params.getUrl()
        val urlBuilder = httpUrl?.toHttpUrlOrNull()?.newBuilder()

        /**
         * 构造header请求头
         * 这里采用ConcurrentHashMap，考虑到线程安全，防止重复添加相同的key，value
         */
        val hashMap = params.getHeader()
        val headerBuilder = Headers.Builder()
        hashMap.forEach { (key: String, value: Any) ->
            headerBuilder.add(key, value)
        }
        val urlHeaderResult = headerBuilder.build()

        /**
         * 构造params
         * id为int类型
         */
//                val paramsMap = params.getParams()
//                paramsMap.forEach { (key, value) ->
//                    urlBuilder?.addQueryParameter(key, value.toString())
//                }
        val urlResult = urlBuilder?.build()
//        urlResult.toLogD()

        /**
         * 构建request
         * 包含以下:
         * 请求头
         * 请求url
         * 请求方法
         */
        val request = Request.Builder()
            //header构建
            .headers(urlHeaderResult)
            //url构建
            .url(urlResult!!)
            //get请求
            .method(method.toString(), requestBody)

        /**
         * 构建请求结果
         */
        return request.build()
    }
}
