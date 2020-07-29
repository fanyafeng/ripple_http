package com.ripple.http.base

import com.alibaba.fastjson.JSONArray


/**
 * Author: fanyafeng
 * Data: 2020/7/29 18:40
 * Email: fanyafeng@live.cn
 * Description:
 */
interface IJsonHelper {
    /**将一个对象转换为一个json*/
    fun toJson(any: Any?): String

    /**将一个json转换为一个指定类型的object，如果没有指定类型则转为object类型*/
    fun <T> toObject(json: String?, clazz: Class<T>?): T?

    /**将一个json转换为一个jsonArray*/
    fun toJsonArray(json: String?): JSONArray?

    /**判断一个json是否是一个jsonArray*/
    fun isJsonArray(json: String?): Boolean

    /**将一个json转换为一个指定参数类型的list*/
    fun <T> toList(json: String?, clazz: Class<T>?): MutableList<T>
}