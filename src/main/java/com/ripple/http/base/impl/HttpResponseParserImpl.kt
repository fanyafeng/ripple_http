package com.ripple.http.base.impl

import com.alibaba.fastjson.JSONObject
import com.ripple.http.base.IHttpResponse
import com.ripple.http.base.abs.AbsHttpResponseParser


/**
 * Author: fanyafeng
 * Data: 2020/7/29 18:48
 * Email: fanyafeng@live.cn
 * Description:
 */
open class HttpResponseParserImpl: AbsHttpResponseParser() {
    override fun parseCommonData(jsonObject: JSONObject, response: IHttpResponse) {
        response.state = jsonObject.getInteger("status")!!
        response.msg = jsonObject.getString("msg")
        response.data = jsonObject.getString("data")

//        if (response.state != 0) {
//            throw HttpException(msg = response.message)
//        }
    }
}