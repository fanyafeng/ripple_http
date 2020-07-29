package com.ripple.http.base.abs

import com.ripple.http.IParamsBuilder
import com.ripple.http.base.HttpMethod
import com.ripple.http.base.IHttpResponse
import com.ripple.http.base.IRequestParams
import com.ripple.http.base.annotation.HttpRequest
import com.ripple.http.base.impl.HttpParamsBuilderImpl
import com.ripple.http.util.parseKV
import com.ripple.tool.check.isEmpty
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.reflect.full.createInstance


/**
 * Author: fanyafeng
 * Data: 2020/7/29 15:47
 * Email: fanyafeng@live.cn
 * Description:
 */
abstract class AbsHttpRequestParams(private var url: String = "",private var builder: IParamsBuilder<IRequestParams.IHttpRequestParams>? = null) : IRequestParams.IHttpRequestParams {

    /**保存header*/
    private val header = ConcurrentHashMap<String, String>()

    /**构建的url*/
    private var buildUri: String? = null

    /**保存path param*/
    private val mPathParams = ArrayList<Any>()

    /**保存param*/
    private val params = ConcurrentHashMap<String, Any>()

    override var method: HttpMethod = HttpMethod.GET

    /**设置请求特性的注解成员*/
    private var httpRequest: HttpRequest? = null

    /**保存header*/
    private val paramsStream = ConcurrentHashMap<String, Any>()

    override fun getHeader(): MutableMap<String, String> {
        return header
    }

    override fun addHeader(key: String, value: String) {
        header[key] = value
    }

    override fun getUrl(): String? {
        return buildUri
    }

    override fun setUrl(url: String) {
        this.url = url
    }

    override fun getParams(): MutableMap<String, Any> {
        return params
    }

    override fun getPathParams(): MutableList<Any> {
        return mPathParams
    }

    override fun addParam(key: String, value: Any) {
        params[key] = value
    }

    override fun addPathParam(value: Any) {
        mPathParams.add(value)
    }

    override fun containParam(key: String): Boolean {
        return params.containsKey(key)
    }

    override fun init() {
        header.clear()
        params.clear()
        paramsStream.clear()

        if (httpRequest == null && this::class != AbsHttpRequestParams::class) {
            httpRequest = javaClass.getAnnotation(HttpRequest::class.java)
        }

        if (isEmpty(url) && httpRequest == null) {
            throw IllegalStateException("uri is empty && @HttpRequest == null")
        }

        //初始化参数
        parseKV(this, javaClass, { name, value ->
            internalAddParam(name, value)
        }, { name, value ->
            addPathParam(value)
        })

        //如果httpRequest的注解builder不为默认builder，则使用注解builder，否则使用默认builder
        if (httpRequest != null && builder == null) {
            val builderClazz = httpRequest!!.builder.takeIf { !it.java.isInterface }
            try {
                if (builderClazz != AbsHttpParamsBuilder::class)
                    builder = builderClazz?.createInstance()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        builder = builder ?: getDefaultParamBuilder()
        builder?.buildParams(this)
        builder?.buildSign(this)
        buildUri = builder?.buildUri(this, httpRequest, url)
    }

    private fun internalAddParam(key: String, value: Any) {
        val v: Any = filterParam(key, value) ?: return
        if (v is File || v is ByteArray) {
            paramsStream[key] = v
        } else {
            params[key] = value
        }
    }

    /**过滤并处理param*/
    protected open fun filterParam(key: String, value: Any): Any? {
        if (value is Date) {
            return value.time
        }
        return value
    }

    /**获取默认的builder*/
    abstract fun getDefaultParamBuilder(): IParamsBuilder<IRequestParams.IHttpRequestParams>
}