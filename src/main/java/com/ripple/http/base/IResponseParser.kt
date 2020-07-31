package com.ripple.http.base

import com.alibaba.fastjson.JSONObject


/**
 * Author: fanyafeng
 * Data: 2020/7/29 15:02
 * Email: fanyafeng@live.cn
 * Description:
 */
interface IResponseParser {

    fun <T> parseData(jsonObject: JSONObject, response: IHttpResponse): T
}