package com.ripple.http.base


/**
 * Author: fanyafeng
 * Data: 2020/7/21 10:03
 * Email: fanyafeng@live.cn
 * Description:
 */
interface IRequestParams {

    companion object {
        /**
         * 读取超时时间
         */
        const val READ_TIMEOUT = 100_000L

        /**
         * 写入超时时间
         */
        const val WRITE_TIMEOUT = 60_000L

        /**
         * 链接时间
         */
        const val CONNECT_TIMEOUT = 60_000L

        const val CHARSET = "UTF-8"
    }

    interface IHttpRequestParams : IRequestParams {

        var response: IHttpResponse

        /**
         * 请求方法
         * 例如：get,post等等
         */
        var method: HttpMethod


        /**
         * 请求头
         * 构建http的请求头
         */
        fun getHeader(): MutableMap<String, String>

        fun addHeader(key: String, value: String)

        fun getUrl(): String?

        fun setUrl(url: String)

        /**获取连接超时，带有默认设置*/
        fun getConnectTimeOut() = CONNECT_TIMEOUT

        /**获取读取超时，带有默认设置*/
        fun getReadTimeOut() = READ_TIMEOUT

        /**获取写入超时，带有默认设置*/
        fun getWriteTimeOut() = WRITE_TIMEOUT

        /**是否在请求过程中启用cookie*/
        fun isUseCookie(): Boolean = true

        /**获取编码信息，带有默认编码*/
        fun getCharset() = CHARSET

        /**是否使用json格式上传数据*/
        fun isUseJsonFormat() = false

        /**
         * 获取下一个任务取消状态
         */
        fun cancelNext(): Boolean?

        /**
         * 设置下一个请求是否取消
         */
        fun setCancelNext(cancelNext: Boolean)
    }

    /**获取请求入参*/
    fun getParams(): MutableMap<String, Any>

    /**获取pathParam请求入参*/
    fun getPathParams(): MutableList<Any>

    /**添加请求参数*/
    fun addParam(key: String, value: Any)

    /**添加pathParam请求参数*/
    fun addPathParam(value: Any)

    fun containParam(key: String): Boolean

    /**初始话params*/
    fun init()

}