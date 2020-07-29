package com.ripple.http.base

import com.ripple.task.callback.result.OnItemResult


/**
 * Author: fanyafeng
 * Data: 2020/7/20 19:40
 * Email: fanyafeng@live.cn
 * Description:
 *
 * http的请求接口
 * 一般http有get，post等
 */
interface IHttpRequest {

    fun <T> get(httpParams: IRequestParams.IHttpRequestParams, callback: OnItemResult<T>): OnItemResult<T>
}