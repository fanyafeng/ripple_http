package com.ripple.http.concreterealization.annotation

/**
 * Author: fanyafeng
 * Data: 2020/5/29 19:37
 * Email: fanyafeng@live.cn
 * Description:
 */
@Target(AnnotationTarget.CLASS)
annotation class HttpRequest(
    /**
     * 接口请求http url路径
     */
    val value: String,
    /**
     *
     */
    val host: String = ""
)