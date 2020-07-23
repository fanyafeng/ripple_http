package com.ripple.http.interfacespecification

import com.ripple.http.concreterealization.IRequestParams

/**
 * Author: fanyafeng
 * Data: 2020/5/28 20:03
 * Email: fanyafeng@live.cn
 * Description:
 */
abstract class RequestParams : IRequestParams {

    var hostname: String = "https://www.baidu.com"

    var url: String = ""


}