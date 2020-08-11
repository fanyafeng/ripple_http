package com.ripple.http.link

import com.ripple.http.base.IRequestParams
import com.ripple.http.callback.OnHttpResult
import com.ripple.tool.kttypelians.SuccessLambda
import okhttp3.Call
import java.io.Serializable


/**
 * Author: fanyafeng
 * Data: 2020/8/3 19:17
 * Email: fanyafeng@live.cn
 * Description:
 */

/**
 * 链式调用的model
 */
internal class HttpLinkModel(
    val index: Int,
    val head: HttpLinkItemModel,
    val follow: MutableList<HttpLinkItemModel>
) : Serializable

/**
 * 请求调用的item
 */
internal class HttpLinkItemModel @JvmOverloads constructor(
    val params: IRequestParams.IHttpRequestParams,
    val callback: OnHttpResult<Any>,
    var lambda: SuccessLambda<Any> = null
) : Serializable