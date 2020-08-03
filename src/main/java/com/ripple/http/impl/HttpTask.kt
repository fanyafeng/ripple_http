package com.ripple.http.impl

import com.alibaba.fastjson.JSON
import com.ripple.http.base.HttpMethod
import com.ripple.http.base.IRequestParams
import com.ripple.http.callback.OnHttpResult
import com.ripple.http.exception.BaseException
import com.ripple.task.callback.OnItemDoing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Call


/**
 * Author: fanyafeng
 * Data: 2020/8/3 14:04
 * Email: fanyafeng@live.cn
 * Description:
 */
object HttpTask {
    fun <T> get(params: IRequestParams.IHttpRequestParams, callback: OnHttpResult<T>) {
        call(params, HttpMethod.GET, callback)
    }

    fun <T> post(params: IRequestParams.IHttpRequestParams, callback: OnHttpResult<T>) {
        call(params, HttpMethod.POST, callback)
    }


    private fun <T> call(
        params: IRequestParams.IHttpRequestParams,
        method: HttpMethod,
        callback: OnHttpResult<T>
    ) {
        callback.onItemStart(Unit)
        callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_START)
        GlobalScope.launch {
            launch(Dispatchers.IO) {
                /**
                 * 初始化params
                 */
                params.method = method
                params.init()
                val call: Call = when (params.method) {
                    HttpMethod.GET -> {
                        OkHttpClientDelegate.get(params)
                    }
                    HttpMethod.POST -> {
                        OkHttpClientDelegate.post(params)
                    }
                }
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
                        if (backResult != null) {
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