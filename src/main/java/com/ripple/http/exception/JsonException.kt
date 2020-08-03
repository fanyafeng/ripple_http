package com.ripple.http.exception


/**
 * Author: fanyafeng
 * Data: 2020/8/3 10:57
 * Email: fanyafeng@live.cn
 * Description:
 */
class JsonException(code: Int = 0, msg: String? = null, cause: Throwable? = null) : BaseException(code, msg, cause)