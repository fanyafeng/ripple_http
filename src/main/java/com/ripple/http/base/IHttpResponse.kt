package com.ripple.http.base


/**
 * Author: fanyafeng
 * Data: 2020/7/21 10:12
 * Email: fanyafeng@live.cn
 * Description:
 */
interface IHttpResponse {

    /**
     * http请求后服务器返回的结果
     * 正常来说不为空
     */
    var response: String

    /**
     * 一般情况下，后台返回的数据都会做数据封装，格式统一
     *
     * 如下：
     * 其中data中可能为list
     * {
     *      "code":200,
     *      "msg":"请求成功",
     *      "result":"成功",
     *      "data":"测试"
     * }
     *
     */
    var data: String?

    var state: Int

    /**
     * http状态码
     */
    /**提示信息*/
    var msg: String

    var isListResult: Boolean

    var itemKClass: Class<*>

    var parser: IResponseParser

    /**当前这个response的params*/
    var requestParams: IRequestParams.IHttpRequestParams

    fun handleParseData(response: String): Boolean

    fun parseItemParamType(paramEntity: Any?)

    fun isSuccess() = true
}