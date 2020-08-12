package com.ripple.http.base

import com.alibaba.fastjson.JSONObject
import java.io.Serializable


/**
 * Author: fanyafeng
 * Data: 2020/7/29 15:02
 * Email: fanyafeng@live.cn
 * Description:
 */
interface IResponseParser : Serializable {

    /**
     * 解析返回数据为json对象
     * 采用的是fastjson解析
     */
    fun <T> parseData(jsonObject: JSONObject, response: IHttpResponse): T
}