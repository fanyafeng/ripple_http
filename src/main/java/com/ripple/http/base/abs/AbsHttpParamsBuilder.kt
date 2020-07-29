package com.ripple.http.base.abs

import android.os.Build
import com.ripple.http.IParamsBuilder
import com.ripple.http.base.HttpMethod
import com.ripple.http.base.IRequestParams
import com.ripple.http.base.annotation.HttpRequest
import com.ripple.tool.check.isEmpty
import java.util.*


/**
 * Author: fanyafeng
 * Data: 2020/7/29 19:08
 * Email: fanyafeng@live.cn
 * Description:
 */
abstract class AbsHttpParamsBuilder : IParamsBuilder<IRequestParams.IHttpRequestParams> {
    override fun buildUri(
        param: IRequestParams.IHttpRequestParams,
        httpRequest: HttpRequest?,
        path: String?
    ): String? {
        var pathVar = path
        if (httpRequest == null) {
            return pathVar
        }
        val url = StringBuilder()

        if (isEmpty(pathVar)) {
            pathVar = httpRequest.value
        }

        if (!param.getPathParams().isEmpty()) {
            pathVar = pathVar ?: "/"
            if (!pathVar.endsWith("/")) {
                pathVar = "$pathVar/"
            }
            val sb = StringBuilder()
            param.getPathParams().forEach {
                if (it !is CharSequence || !it.isEmpty()) {
                    sb.append(it)
                    sb.append("/")
                }
            }

            pathVar = "$pathVar$sb"
        }

        if (isEmpty(pathVar)) {
            return pathVar
        }

        //初始化host,如果入参pathVar本身就是一个完整url，则直接拼接pathVar，不做host处理
        if (isHttpUrl(pathVar!!)) {
            url.append(pathVar)
        } else {
            var host = httpRequest.host

            if (isEmpty(host)) {
                host = getDefaultHost()
            }

            url.append(host)

            if (!host.endsWith("/") && !pathVar.startsWith("/")) {
                url.append("/")
            } else if (host.endsWith("/") && pathVar.startsWith("/")) {
                pathVar = pathVar.substring(1)
            }
            url.append(pathVar)
        }

        if (!isEmpty(httpRequest.version)) {
            if (!pathVar.contains("?") && !pathVar.endsWith("/")) {
                url.append("/")
            }
            url.append(httpRequest.version)
        }

        val mapParam = param.getParams()

        if (param.method === HttpMethod.GET && !isEmpty(mapParam)) {
            if (!pathVar.contains("?")) {
                url.append("?")
            } else if (!pathVar.endsWith("&")) {
                url.append("&")
            }
            for (entry in mapParam.entries) {
                url.append(entry.key)
                url.append("=")
                url.append(entry.value.toString())
                url.append("&")
            }
        }

        if (url[url.length - 1] == '&') {
            url.deleteCharAt(url.length - 1)
        }
        if (url[url.length - 1] == '?') {
            url.deleteCharAt(url.length - 1)
        }
        return url.toString()
    }

    override fun buildParams(params: IRequestParams.IHttpRequestParams) {
        //当前的UA
        params.addHeader("User-Agent", getUserAgent() ?: "")

        //当前设备系统类型
        params.addHeader("X-platform", "Android")
        //当前设备系统版本号，比如5.1
        params.addHeader("X-os", Build.VERSION.RELEASE ?: "")
        //手机型号，比如m3note
        params.addHeader("X-product", Build.PRODUCT ?: "")
        //手机厂家，比如Meizu
        params.addHeader("X-manufacture", Build.MANUFACTURER + "")
        //设备ID，当前格式为IMEI+$+MAC，以后有可能改变
//        params.addHeader("X-di", DeviceUtil.getDI() ?: "")
//        try {
//            //当前网络类型，比如wifi："MLJR"
//            params.addHeader("X-network", URLEncoder.encode(DeviceUtil.getCurrentNetType() + "", "UTF-8"))
//        } catch (e: UnsupportedEncodingException) {
//            e.printStackTrace()
//        }
        params.addHeader("X-traceId", UUID.randomUUID().toString())

        //当前设备的mac地址
//        params.addHeader("X-macid", DeviceUtil.getMacAddress() ?: "")
        //当前设备的imei
//        params.addHeader("X-imei", DeviceUtil.getIMEI() ?: "")

    }

    abstract fun getDefaultHost(): String

    private fun isHttpUrl(url: String) =
        url.startsWith("http://", false) || url.startsWith("https://", false)
}