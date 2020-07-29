package com.ripple.http.util

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.ripple.http.base.IJsonHelper
import com.ripple.tool.check.isEmpty
import java.math.BigDecimal

/**
 * Author: fanyafeng
 * Data: 2020/7/29 18:41
 * Email: fanyafeng@live.cn
 * Description:
 */
object JsonHelperImpl:IJsonHelper {
    private val JSON_FILTER = mutableListOf<Class<*>>()

    init {
       JSON_FILTER.add(Int::class.java)
       JSON_FILTER.add(Long::class.java)
       JSON_FILTER.add(Double::class.java)
       JSON_FILTER.add(Float::class.java)
       JSON_FILTER.add(BigDecimal::class.java)
       JSON_FILTER.add(String::class.java)
    }

    override fun toJson(any: Any?): String = JSON.toJSONString(any)

    override fun <T> toObject(json: String?, clazz: Class<T>?): T? = when {
        clazz == null || clazz is JSONObject ->
            JSON.parseObject(json) as T
        JSON_FILTER.contains(clazz) ->
            parseForFilter(json, clazz)
        else ->
            JSON.parseObject(json, clazz)
    }

    override fun toJsonArray(json: String?): JSONArray? = JSON.parseArray(json)

    override fun isJsonArray(json: String?): Boolean {
        if (isEmpty(json)) {
            return false
        }
        return try {
            val jsonArray = toJsonArray(json)
            jsonArray != null && jsonArray.size > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun <T> toList(json: String?, clazz: Class<T>?): MutableList<T> = JSON.parseArray(json, clazz)

    private fun <T> parseForFilter(json: String?, clazz: Class<T>): T? = when {
        json == null ->
            null
        Int::class.java.isAssignableFrom(clazz) ->
            json.toInt() as T
        Long::class.java.isAssignableFrom(clazz) ->
            json.toLong() as T
        Double::class.java.isAssignableFrom(clazz) ->
            json.toDouble() as T
        Float::class.java.isAssignableFrom(clazz) ->
            json.toFloat() as T
        String::class.java.isAssignableFrom(clazz) ->
            json as T
        BigDecimal::class.java.isAssignableFrom(clazz) ->
            BigDecimal.valueOf(json.toDouble()) as T
        else ->
            null
    }
}