package com.ripple.http.base.impl

import com.ripple.http.base.IRequestParams
import com.ripple.http.base.IResponseParser
import com.ripple.http.base.abs.AbsHttpResponse


/**
 * Author: fanyafeng
 * Data: 2020/7/29 18:52
 * Email: fanyafeng@live.cn
 * Description:
 */
class HttpResponseImpl(override var requestParams: IRequestParams.IHttpRequestParams) :
    AbsHttpResponse() {
    override var parser: IResponseParser = HttpResponseParserImpl()
}