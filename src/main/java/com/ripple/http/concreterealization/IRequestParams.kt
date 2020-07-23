package com.ripple.http.concreterealization

/**
 * Author: fanyafeng
 * Data: 2020/5/28 19:49
 * Email: fanyafeng@live.cn
 * Description:
 *
 * http的params
 * 常用必须并且修改比较频繁的
 * 1.protocol                   协议 http,https
 * 2.hostname                   主机名
 * 3.port                       端口号
 * 4.path                       路径
 * 5.connectionTimeOut          超时时间
 *
 * 不常用必须并且修改不频繁的
 * 1.header                     请求头信息
 * 2.json                       是否采用json格式
 *
 */
interface IRequestParams {

    companion object {
        const val HTTP = "http://"
        const val HTTPS = "https://"
    }

    interface IHttpRequestParams : IRequestParams {
        /**
         * 获取网络协议
         * 可空，如果为空的话会拼接protocol,hostname,port,path
         */
//    fun getProtocol(): String?

        /**
         * 主机名
         */
        fun getHostName(): String

        /**
         * 端口号
         */
//    fun getPort(): String?

        /**
         * http路径
         */
        fun getPath(): String
    }

}