package com.ripple.http.base


/**
 * Author: fanyafeng
 * Data: 2020/7/21 10:03
 * Email: fanyafeng@live.cn
 * Description:
 */
interface IRequestParams {

    interface IHttpParams {
        var method: HttpMethod

        var response: IHttpResponse
    }

}