package com.ripple.http.base


/**
 * Author: fanyafeng
 * Data: 2020/7/29 15:02
 * Email: fanyafeng@live.cn
 * Description:
 */
interface IResponseParser {

    fun <T> parse(response: IHttpResponse, dataType: Class<T>): List<T>?
}