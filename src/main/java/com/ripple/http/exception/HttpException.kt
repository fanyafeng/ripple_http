package com.ripple.http.exception


/**
 * Author: fanyafeng
 * Data: 2020/7/31 10:21
 * Email: fanyafeng@live.cn
 * Description:
 */
class HttpException(code: Int = 0, msg: String? = null, cause: Throwable? = null) :
    BaseException(code, msg, cause) {
}