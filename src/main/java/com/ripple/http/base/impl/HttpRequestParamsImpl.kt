package com.ripple.http.base.impl

import com.ripple.http.base.IParamsBuilder
import com.ripple.http.base.IHttpResponse
import com.ripple.http.base.IRequestParams
import com.ripple.http.base.abs.AbsHttpRequestParams
import com.ripple.http.base.annotation.HttpIgnoreBuildParam

/**
 * Author: fanyafeng
 * Data: 2020/7/29 16:04
 * Email: fanyafeng@live.cn
 * Description:
 */
@HttpIgnoreBuildParam
open class HttpRequestParamsImpl(url: String = "") : AbsHttpRequestParams(url) {
    override fun getDefaultParamBuilder(): IParamsBuilder<IRequestParams.IHttpRequestParams> {
        return HttpParamsBuilderImpl()
    }

    override var response: IHttpResponse = HttpResponseImpl(this)
}