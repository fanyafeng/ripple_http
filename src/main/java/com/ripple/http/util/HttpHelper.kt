package com.ripple.http.util

import com.ripple.http.base.IRequestParams
import com.ripple.http.base.abs.AbsHttpRequestParams
import com.ripple.http.base.annotation.HttpIgnoreBuildParam
import com.ripple.http.base.annotation.HttpParam
import com.ripple.http.base.annotation.HttpParamModel
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.kotlinProperty

/**
 * Author: fanyafeng
 * Data: 2020/7/29 19:38
 * Email: fanyafeng@live.cn
 * Description:
 */

private val BOOT_CL = String::class.java.classLoader

/***/
val IRequestParams.paramTypeBlackList: MutableList<Class<*>> by lazy {
    mutableListOf<Class<*>>()
}

/**item的过滤器，如果lambda返回true，则认为过滤掉了，如果为false则认为合法*/
val IRequestParams.paramFilter: MutableList<(IRequestParams, Any, Class<*>, Field?) -> Boolean> by lazy {
    val list = mutableListOf<(IRequestParams, Any, Class<*>, Field?) -> Boolean>()
    //添加黑名单
    list.add { params, entity, type, field ->
        if (field == null) {
            type.getAnnotation(HttpIgnoreBuildParam::class.java) != null
        } else {
            with(field) {
                isAccessible = true
                Modifier.isTransient(modifiers) ||
                        params.paramTypeBlackList.contains(this::class.java) ||
                        getAnnotation(HttpIgnoreBuildParam::class.java) != null ||
                        name == "serialVersionUID"
            }
        }
    }
    //添加通用过滤
    list.add { params, entity, type, field ->
        if (field != null) params.paramTypeBlackList.contains(field::class.java) else false
    }
    list
}

/**解析参数*/
fun IRequestParams.parseKV(entity: Any, type: Class<*>, paramCallback: (String, Any) -> Unit, pathParamCallback: (String, Any) -> Unit) {
    if (type == AbsHttpRequestParams::class.java) {
        return
    } else {
        val cl = type.classLoader
        if (cl == null || cl == BOOT_CL) {
            return
        }
    }

    paramFilter.forEach {
        if (it(this, entity, type, null)) return
    }

    val fields = type.declaredFields
    fields?.forEach {
        it.run {
            paramFilter.forEach {
                if (it(this@parseKV, entity, type, this)) return@run
            }
            isAccessible = true
            try {
                val property = get(entity) ?: return@run

                val atnParamModel = getAnnotation(HttpParamModel::class.java)

                //如果是ParamModel，则做相应解析
                if (atnParamModel != null) {
                    parseKV(property, type, paramCallback, pathParamCallback)
                } else {

                    var httpParamAnt = it.kotlinProperty?.findAnnotation<HttpParam>()
                    if (httpParamAnt == null) {
                        httpParamAnt = it.getAnnotation(HttpParam::class.java)
                    }
                    if (httpParamAnt == null) {
                        paramCallback(name, property)
                    } else {
                        if (httpParamAnt.pathParam) {
                            pathParamCallback(name, property)
                        } else {
                            paramCallback(name, property)
                        }
                    }
                }
            } catch (ex: IllegalAccessException) {
                ex.printStackTrace()
            }
        }
    }

    parseKV(entity, type.superclass!!, paramCallback, pathParamCallback)
}