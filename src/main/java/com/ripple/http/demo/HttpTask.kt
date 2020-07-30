package com.ripple.http.demo

import com.google.gson.Gson
import com.ripple.http.base.IRequestParams
import com.ripple.http.callback.OnHttpResult
import com.ripple.log.tpyeextend.toLogD
import com.ripple.task.engine.ProcessEngine
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


/**
 * Author: fanyafeng
 * Data: 2020/7/30 10:33
 * Email: fanyafeng@live.cn
 * Description:
 */
object HttpTask {
    fun <T> call(params: IRequestParams.IHttpRequestParams, callback: OnHttpResult<T>) {
        params.init()
        /**
         * 构造OkHttpClient
         */
        val client = RippleHttpClient.getInstance().newBuilder()
        client.readTimeout(params.getReadTimeOut(), TimeUnit.MILLISECONDS)
        client.writeTimeout(params.getWriteTimeOut(), TimeUnit.MILLISECONDS)
        client.connectTimeout(params.getConnectTimeOut(), TimeUnit.MILLISECONDS)

        val clientResult = client.build()

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
        val paramsMap = params.getParams()
        paramsMap.forEach { (key, value) ->
            urlBuilder?.addQueryParameter(key, value.toString())
        }
        val urlResult = urlBuilder?.build()
        urlResult.toLogD()


        val request = Request.Builder()
            //header构建
            .headers(urlHeaderResult)
            //url构建
            .url(urlResult!!)
            //get请求
            .method(params.method.toString(), null)
        val requestResult = request.build()


        clientResult.newCall(requestResult).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.toString().toLogD("IOException")
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                result.toLogD()
                params.response.response = result!!
                params.response.parseItemParamType(callback)
                params.response.parser.parse(
                    params.response,
                    params.response.itemKClass.kotlin.java
                )

                val itemClazz = params.response.itemKClass

                val backResult = Gson().fromJson<T>(params.response.data, itemClazz)

                callback.onItemSuccess(backResult)
                params.response.itemKClass.toLogD("itemKClass")
            }
        })
    }

    fun testCall() {
        /**
         * 读取超时时间
         */
        val READ_TIMEOUT = 100_000L

        /**
         * 写入超时时间
         */
        val WRITE_TIMEOUT = 60_000L

        /**
         * 链接时间
         */
        val CONNECT_TIMEOUT = 60_000L

        /**
         * 构造OkHttpClient
         */
        val client = RippleHttpClient.getInstance().newBuilder()
        client.readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
        client.writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
        client.connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)

        val clientResult = client.build()

        /**
         * 构造带有path的url
         * 这里采用buffer，主要因为它是线程安全的而且高效
         */
        val urlBuffer = StringBuffer()
        urlBuffer.append("http://10.12.16.198:8080/get/getUserById")
        urlBuffer.append("/")
        urlBuffer.append("myname")
        val httpUrl = urlBuffer.toString()
        val urlBuilder = httpUrl.toHttpUrlOrNull()?.newBuilder()

        /**
         * 构造header请求头
         * 这里采用ConcurrentHashMap，考虑到线程安全，防止重复添加相同的key，value
         */
        val hashMap: ConcurrentHashMap<String, Any> = ConcurrentHashMap()
        val headerBuilder = Headers.Builder()
        hashMap.forEach { (key: String, value: Any) ->
            headerBuilder.add(key, value.toString())
        }
        val urlHeaderResult = headerBuilder.build()

        /**
         * 构造params
         * id为int类型
         */
        urlBuilder?.addQueryParameter("id", "666")
        val urlResult = urlBuilder?.build()
        urlResult.toLogD()

        val request = Request.Builder()
            //header构建
            .headers(urlHeaderResult)
            //url构建
            .url(urlResult!!)
            //get请求
            .get()
        val requestResult = request.build()


        clientResult.newCall(requestResult).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.toString().toLogD()
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                result.toLogD()
            }
        })
    }

}