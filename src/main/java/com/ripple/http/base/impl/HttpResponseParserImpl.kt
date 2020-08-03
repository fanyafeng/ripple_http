package com.ripple.http.base.impl

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.ripple.http.base.IHttpResponse
import com.ripple.http.base.abs.AbsHttpResponseParser


/**
 * Author: fanyafeng
 * Data: 2020/7/29 18:48
 * Email: fanyafeng@live.cn
 * Description:
 */
open class HttpResponseParserImpl : AbsHttpResponseParser() {

    override fun <T> parseData(jsonObject: JSONObject, response: IHttpResponse): T {
        response.state = jsonObject.getInteger("status")
        response.message = jsonObject.getString("message")
        response.data = jsonObject.getString("data")

//        if (response.state != 0) {
//            throw HttpException(msg = response.message)
//        }

        val itemClazz = response.itemKClass
        return JSON.parseObject<T>(response.data, itemClazz)
    }
}