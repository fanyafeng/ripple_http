package com.ripple.http.base.impl

import com.ripple.http.base.IRequestParams
import com.ripple.http.base.abs.AbsHttpParamsBuilder


/**
 * Author: fanyafeng
 * Data: 2020/7/29 19:20
 * Email: fanyafeng@live.cn
 * Description:
 */
class HttpParamsBuilderImpl : AbsHttpParamsBuilder() {

    override fun buildParams(params: IRequestParams.IHttpRequestParams) {
        super.buildParams(params)
    }

    override fun getDefaultHost(): String {
        return "http://10.12.16.198:8080"
    }

    override fun getUserAgent(): String? {
        return "Android"
    }
}