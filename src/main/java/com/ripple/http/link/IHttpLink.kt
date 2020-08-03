package com.ripple.http.link

import okhttp3.Call
import java.util.concurrent.BlockingDeque


/**
 * Author: fanyafeng
 * Data: 2020/8/3 16:23
 * Email: fanyafeng@live.cn
 * Description:
 *
 * 链式调用接口类
 */
interface IHttpLink {


    /**
     * 当前请求是否被取消
     */
    fun cancel(): Boolean

    /**
     * 设置当前请求是否取消
     */
    fun setCancel(cancel: Boolean)

    /**
     * 当前请求是否执行
     */
    fun isExecuted(): Boolean

    fun setExecute(execute: Boolean)
}