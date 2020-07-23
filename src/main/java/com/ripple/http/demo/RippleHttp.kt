package com.ripple.http.demo

import com.google.gson.Gson
import com.ripple.http.callback.OnHttpResult
import com.ripple.http.exception.BaseException
import com.ripple.http.util.ParameterizedTypeUtil
import com.ripple.log.tpyeextend.toLogD
import com.ripple.task.callback.OnItemDoing
import com.ripple.tool.check.isEmpty
import com.ripple.tool.kttypelians.SuccessLambda
import com.ripple.tool.kttypelians.UnitLambda
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.reflect


/**
 * Author: fanyafeng
 * Data: 2020/7/21 17:46
 * Email: fanyafeng@live.cn
 * Description:
 *
 * 此类封装http请求，如 get，set，文件下载
 */
class RippleHttp private constructor() {


    companion object {
        @Volatile
        private var instance: RippleHttp? = null

        fun getInstance(): RippleHttp {
            if (instance == null) {
                synchronized(RippleHttp::class) {
                    if (instance == null) {
                        instance = RippleHttp()
                    }
                }
            }
            return instance!!
        }
    }

    fun <T> getLambda(
        url: String,
        success: SuccessLambda<T> = null,
        finish: SuccessLambda<Boolean> = null,
        start: UnitLambda = null,
        doing: SuccessLambda<Long> = null,
        failed: SuccessLambda<BaseException> = null
    ) {
        start?.invoke()
        /**
         * 构造urlBuilder
         */
        val urlBuilder = url.toHttpUrlOrNull()?.newBuilder()

        /**
         * 构造url
         * 使用了大量的构造者模式
         */
        val urlResult = urlBuilder?.build()

        /**
         * 构造request
         */
        val request = Request.Builder()
            .url(urlResult!!)
            .get()
        val requestResult = request.build()
        /**
         * 发起异步请求
         */
        doing?.invoke(OnItemDoing.CODE_ITEM_DOING_START)
        RippleHttpClient.getInstance().newCall(requestResult).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                /**
                 * call为回传的结果
                 */
                /**
                 * 任务进行中回调
                 * 任务失败，返回-1L
                 */
                doing?.invoke(OnItemDoing.CODE_ITEM_DOING_FAILED)

                /**
                 * 走到结束回调但是任务没完成
                 * 返回false
                 */
                finish?.invoke(false)
                /**
                 * call为回传的结果
                 */
                failed?.invoke(BaseException(e))
            }

            override fun onResponse(call: Call, response: Response) {

                /**
                 * 走到结束回调并且任务完成
                 * 返回true
                 */
                finish?.invoke(true)

                /**
                 * 任务进行中回调
                 * 任务成功，返回100L
                 */
                doing?.invoke(OnItemDoing.CODE_ITEM_DOING_FINISH)

                val result = response.body?.string()

                val clazz = parseItemParamType(success!!.reflect())
                clazz.name.toLogD("Lambda表达式 打印泛型名字：")
                val backResult = parseJsonString<T>(result, clazz)
                success.invoke(backResult)
            }

        })
    }


    /**
     * 以接口的方式抽离
     */
    fun <S> parseJsonString(result: String?, typeOf: Type): S {
        return Gson().fromJson(result, typeOf)
    }

    /**
     * 异步get请求
     */
    fun <T> get(url: String, callback: OnHttpResult<T>) {
        callback.onItemStart(Unit)
        /**
         * 构造urlBuilder
         */
        val urlBuilder = url.toHttpUrlOrNull()?.newBuilder()

        /**
         * 构造url
         * 使用了大量的构造者模式
         */
        val urlResult = urlBuilder?.build()

        /**
         * 构造request
         */
        val request = Request.Builder()
            .url(urlResult!!)
            .get()
        val requestResult = request.build()
        /**
         * 发起异步请求
         */
        callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_START)
        RippleHttpClient.getInstance().newCall(requestResult).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                /**
                 * 任务进行中回调
                 * 任务失败，返回-1L
                 */
                callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_FAILED)

                /**
                 * 走到结束回调但是任务没完成
                 * 返回false
                 */
                callback.onItemFinish(false)
                /**
                 * call为回传的结果
                 */
                callback.onItemFailed(BaseException(e))
            }

            override fun onResponse(call: Call, response: Response) {
                /**
                 * 走到结束回调并且任务完成
                 * 返回true
                 */
                callback.onItemFinish(true)

                /**
                 * 任务进行中回调
                 * 任务成功，返回100L
                 */
                callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_FINISH)

                val result = response.body?.string()

                val clazz = parseItemParamType(callback)
                clazz.toLogD("callback 打印泛型名字：")

                val backResult = parseJsonString<T>(result, clazz)
                /**
                 * 任务完成回调
                 * 返回成功model
                 */
                callback.onItemSuccess(backResult)
            }

        })
    }


    /**
     * 获取类的泛型
     * 获取不到接口泛型
     */
    private fun parseItemParamType(paramEntity: Any?): Class<*> {
        var itemKClass: Class<*> = String::class.java
        var isListResult = false
        if (paramEntity == null) {
            return itemKClass
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
        return itemKClass
    }

    private fun getClass(type: Type?, i: Int): Class<*>? {
        return when (type) {
            is ParameterizedType -> {
                ParameterizedTypeUtil.getGenericClass(
                    type as ParameterizedType?,
                    i
                )
            }
            is TypeVariable<*> -> {
                getClass(
                    type.bounds[0],
                    0
                )
            }
            else -> {
                type as Class<*>?
            }
        }
    }

    /**
     * 同步get请求
     */
    fun getSync() {

    }

}

fun Type.isArrayType(): Boolean {
    var loadType = this
    if (loadType is ParameterizedType) {
        loadType = loadType.rawType
    }
    var result = List::class.java.isAssignableFrom(loadType as Class<*>)
    if (!result) {
        result = loadType.isArray
    }
    return result
}