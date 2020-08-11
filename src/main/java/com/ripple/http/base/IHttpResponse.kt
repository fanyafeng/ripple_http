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
    var message: String

    /**
     * 解析结果是否为list
     */
    var isListResult: Boolean

    /**
     * 解析结果后的的class
     */
    var itemKClass: Class<*>

    /**
     * 序列化返回结果
     */
    var parser: IResponseParser

    /**当前这个response的params*/
    var requestParams: IRequestParams.IHttpRequestParams

    /**
     * 是否进行自定义解析
     * 如果返回false则是交给框架解析
     */
    fun handleParseData(response: String): Boolean

    /**
     * 解析传入泛型类型
     */
    fun parseItemParamType(paramEntity: Any?)

    fun isSuccess() = true
}