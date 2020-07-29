package com.ripple.http.base.annotation

/**
 * Author: fanyafeng
 * Data: 2020/7/29 19:43
 * Email: fanyafeng@live.cn
 * Description:
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class HttpParam(val pathParam: Boolean = false)