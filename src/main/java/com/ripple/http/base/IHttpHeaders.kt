package com.ripple.http.base

import java.util.concurrent.ConcurrentHashMap


/**
 * Author: fanyafeng
 * Data: 2020/7/29 14:26
 * Email: fanyafeng@live.cn
 * Description:
 */
interface IHttpHeaders {
    var headers: ConcurrentHashMap<String, Any>

    fun setHeader(key: String, value: String)
}

//class Headers(override var headers: ConcurrentHashMap<String, Any>) : IHttpHeaders {
//
//    override fun setHeader(key: String, value: String) {
//        headers[key] = value
//    }
//
//}