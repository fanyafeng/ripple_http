package com.ripple.http.base


/**
 * Author: fanyafeng
 * Data: 2020/7/21 10:03
 * Email: fanyafeng@live.cn
 * Description:
 */
interface IRequestParams {

    companion object{

    }

    interface IHttpRequestParams:IRequestParams {


        /**
         * 请求头
         * 构建http的请求头
         */
        var headers: Map<String, Any>
    }




    /**
     * 请求方法
     * 例如：get,post等等
     */
    var method: HttpMethod

    /**
     * http host
     * 一般一个app中只有一个
     * 但是也会有多个的情况
     */
    var host: String

    /**
     * http url
     * 构建http的url
     */
    var url: String

}