package com.ripple.http.demo

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.google.gson.Gson
import com.ripple.http.base.HttpMethod
import com.ripple.http.base.IRequestParams
import com.ripple.http.callback.OnHttpResult
import com.ripple.http.exception.BaseException
import com.ripple.http.util.getRequestBody
import com.ripple.log.tpyeextend.toLogD
import com.ripple.task.callback.OnItemDoing
import com.ripple.task.engine.ProcessEngine
import kotlinx.coroutines.*
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
    fun <T> httpGet(params: IRequestParams.IHttpRequestParams, callback: OnHttpResult<T>) {
        params.method = HttpMethod.GET
        call(params, callback)
    }

    fun <T> httpPost(params: IRequestParams.IHttpRequestParams, callback: OnHttpResult<T>) {
        params.method = HttpMethod.POST
        call(params, callback)
    }


    fun <T> call(params: IRequestParams.IHttpRequestParams, callback: OnHttpResult<T>) {

        GlobalScope.launch {
            callback.onItemStart(Unit)
            callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_START)
            launch(Dispatchers.IO) {
                /**
                 * 此时需要在在线程中进行处理
                 */
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
//                val paramsMap = params.getParams()
//                paramsMap.forEach { (key, value) ->
//                    urlBuilder?.addQueryParameter(key, value.toString())
//                }
                val urlResult = urlBuilder?.build()
                urlResult.toLogD()

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
                    .method(params.method.toString(), null)

                /**
                 * 构建请求结果
                 */
                val requestResult = request.build()

                /**
                 * 通过client发送请求
                 */
                val call = clientResult.newCall(requestResult)

                try {
                    /**
                     * 执行请求
                     */
                    val response = call.execute()

                    /**
                     * 获取请求结果
                     */
                    val result = response.body?.string()
                    if (result != null) {
                        /**
                         * 解析请求结果
                         */
                        params.response.response = result
                        params.response.parseItemParamType(callback)
                        val backResult = params.response.parser.parseData<T>(
                            JSON.parseObject(result),
                            params.response
                        )
                        launch(Dispatchers.Main) {
                            /**
                             * 成功执行完成后
                             * 进度条：[0-100] 0为开始 -1失败 100成功
                             * 请求成功回调请求结果
                             * 请求完成回调完成为true
                             */
                            callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_FINISH)
                            callback.onItemSuccess(backResult)
                            callback.onItemFinish(true)
                        }
                    } else {
                        launch(Dispatchers.Main) {
                            /**
                             * 成功执行完成后 失败
                             * 进度条：[0-100] 0为开始 -1失败 100成功
                             * 请求成功回调请求结果
                             * 请求完成回调完成为false
                             */
                            callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_FAILED)
                            callback.onItemFailed(BaseException(msg = "请求为空，请查看请求是否正确"))
                            callback.onItemFinish(false)
                        }
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        /**
                         * 成功执行完成后 失败
                         * 进度条：[0-100] 0为开始 -1失败 100成功
                         * 请求成功回调请求结果
                         * 请求完成回调完成为false
                         */
                        callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_FAILED)
                        callback.onItemFailed(BaseException(e))
                        callback.onItemFinish(false)
                    }
                }
            }
        }
    }


}