package com.ripple.http.link.impl

import com.ripple.http.link.IHttpLink
import okhttp3.Call
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque


/**
 * Author: fanyafeng
 * Data: 2020/8/3 17:29
 * Email: fanyafeng@live.cn
 * Description:
 */
class HttpLinkImpl : IHttpLink {
    private var cancel: Boolean = false
    private var execute: Boolean = false

    override fun cancel(): Boolean {
        return cancel
    }

    override fun isExecuted(): Boolean {
        return execute
    }

    override fun setExecute(execute: Boolean) {
        this.execute = execute
    }

    override fun setCancel(cancel: Boolean) {
        this.cancel = cancel
    }
}