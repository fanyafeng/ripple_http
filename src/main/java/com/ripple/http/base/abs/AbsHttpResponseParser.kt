package com.ripple.http.base.abs

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.ripple.http.base.IHttpResponse
import com.ripple.http.base.IResponseParser
import com.ripple.http.util.JsonHelperImpl
import com.ripple.tool.check.isEmpty


/**
 * Author: fanyafeng
 * Data: 2020/7/29 16:48
 * Email: fanyafeng@live.cn
 * Description:
 */
abstract class AbsHttpResponseParser : IResponseParser {
    override fun <T> parse(response: IHttpResponse, dataType: Class<T>): List<T>? {
        val result = response.response

        val isSelfHandle = response.handleParseData(result)
        if (isSelfHandle) {
            return null
        }
        val json = JSON.parseObject(result)
        parseCommonData(json, response)

        var dataSet = mutableListOf<T>()

        if (String::class.java.isAssignableFrom(dataType) || Any::class.java == dataType) {
            dataSet.add(response.data as T)
            return dataSet
        }

        var isArray = true
        var dataJson: String? = null
        try {
            val jsonArray = JSON.parseArray(response.data) // 拿出data
            dataJson = jsonArray.toJSONString()
        } catch (e: Exception) {
            isArray = false
            try {
                val data = JSON.parseObject(response.data)
                if (data != null) {
                    dataJson = data.toJSONString()
                }
            } catch (e1: Exception) {
                dataJson = response.data
            }
        }
        if (isEmpty(dataJson)) {
            return dataSet
        }

        if (isArray) {
            if (dataType.isArray) {
//                dataSet = JsonHelperImpl.toList(dataJson, dataType.java.componentType as List<DataType>)
                TODO("这里需要实现解析数组的逻辑")
            } else {
                dataSet = JsonHelperImpl.toList(dataJson, dataType)
            }
        } else {
            val data = JsonHelperImpl.toObject(dataJson, dataType)
            if (data != null) {
                dataSet.add(data)
            }
        }
        return dataSet
    }

    abstract fun parseCommonData(jsonObject: JSONObject, response: IHttpResponse)
}