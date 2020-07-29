package com.ripple.http.base.abs

import com.ripple.http.base.IHttpResponse
import com.ripple.http.base.IRequestParams
import com.ripple.http.base.IResponseParser
import com.ripple.http.demo.isArrayType
import com.ripple.http.util.ParameterizedTypeUtil
import com.ripple.log.tpyeextend.toLogD
import com.ripple.tool.check.isEmpty
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import kotlin.reflect.KClass
import kotlin.reflect.KFunction


/**
 * Author: fanyafeng
 * Data: 2020/7/29 16:20
 * Email: fanyafeng@live.cn
 * Description:
 */
abstract class AbsHttpResponse : IHttpResponse {
    override var response: String = ""
    override var data: String? = null
    override var state: Int = -1
    override var msg: String = ""
    override var isListResult = false
    override var itemKClass: Class<*> = String::class.java

    var hasSuccess = true

    override fun handleParseData(response: String) = false

    /**
     * 获取类的泛型
     * 获取不到接口泛型
     */
    override fun parseItemParamType(paramEntity: Any?) {
        if (paramEntity == null) {
            return
        }
        if (paramEntity is KFunction<*>) {
            val paramType = paramEntity.parameters[0].type
            var clazz = (paramType.classifier as KClass<*>).java
            if (clazz.isArrayType()) {
                clazz = (paramType.arguments[0].type!!.classifier as KClass<*>).java
                isListResult = true
            }
            itemKClass = if (isListResult) {
                List::class.java
            } else {
                clazz
            }
        } else if (paramEntity is ParameterizedType) {
            if (paramEntity.rawType.isArrayType()) {
                isListResult = true
                val arg0 = paramEntity.actualTypeArguments[0]
                if (arg0 is WildcardType) {
                    if (!isEmpty(arg0.upperBounds)) {
                        itemKClass = arg0.upperBounds[0] as Class<*>
                    } else {
                        itemKClass = arg0.lowerBounds[0] as Class<*>
                    }
                } else {
                    itemKClass = paramEntity.actualTypeArguments[0] as Class<*>
                }
            } else {
                itemKClass = paramEntity.rawType as Class<*>
            }
        } else {
            val hasInterfaceT = ParameterizedTypeUtil.hasInterfaceT(paramEntity)
            if (hasInterfaceT) {

                val params: Array<Type> =
                    paramEntity.javaClass.genericInterfaces
                val type = params[0]
                val finalNeedType = if (params.size > 1) {
                    check(type is ParameterizedType) { "没有填写泛型参数" }
                    (type as ParameterizedType).actualTypeArguments[0]
                } else {
                    type
                }
                if (finalNeedType is ParameterizedType) {
                    if (finalNeedType.actualTypeArguments[0].isArrayType()) {
                        isListResult = true
                    }
                }
                itemKClass = ParameterizedTypeUtil.getClass(finalNeedType, 0) as Class<*>
            } else {
                val directParameterizedType =
                    ParameterizedTypeUtil.getDirectParameterizedType(paramEntity::class.java)
                if (directParameterizedType is ParameterizedType) {
                    if (directParameterizedType.rawType.isArrayType()) {
                        isListResult = true
                        itemKClass = directParameterizedType.actualTypeArguments[0] as Class<*>
                    } else {
                        itemKClass = directParameterizedType.rawType as Class<*>
                    }
                } else {
                    itemKClass = directParameterizedType as Class<*>
                }
            }
        }
        isListResult.toLogD("查看我是不是list：")
    }


    override fun isSuccess(): Boolean {
        return hasSuccess
    }
}