package com.ripple.http.demo

import okhttp3.OkHttpClient


/**
 * Author: fanyafeng
 * Data: 2020/7/22 19:59
 * Email: fanyafeng@live.cn
 * Description:
 *
 * 此类封装http client
 * 主要是存取header，缓存，设置请求模式
 *
 *
 * 设置请求格式：
 * 1.json格式
 * 2.param格式
 * 3./xxx/yyy路径模式
 */
class RippleHttpClient {
    companion object {

        @Volatile
        private var httpInstance: OkHttpClient? = null

        fun getInstance(): OkHttpClient {
            if (httpInstance == null) {
                synchronized(RippleHttpClient::class) {
                    if (httpInstance == null) {
                        httpInstance = OkHttpClient()
                    }
                }
            }
            return httpInstance!!
        }

    }
}