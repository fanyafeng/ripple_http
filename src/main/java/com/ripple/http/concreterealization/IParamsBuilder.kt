package com.ripple.http.concreterealization

import com.ripple.http.concreterealization.annotation.HttpRequest

/**
 * Author: fanyafeng
 * Data: 2020/5/29 20:07
 * Email: fanyafeng@live.cn
 * Description:
 */
interface IParamsBuilder<T : IRequestParams> {

    /**
     * 构建url
     */
    fun buildUri(param: T, httpRequest: HttpRequest, path: String? = null): String?
}