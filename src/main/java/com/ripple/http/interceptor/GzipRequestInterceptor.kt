package com.ripple.http.interceptor

import com.ripple.tool.check.isEmpty
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.Response
import okio.BufferedSink
import okio.GzipSink
import okio.Okio
import okio.buffer
import java.io.IOException


/**
 * Author: fanyafeng
 * Data: 2020/8/3 13:55
 * Email: fanyafeng@live.cn
 * Description:
 */
class GzipRequestInterceptor:Interceptor {
    companion object {
        /**header中的gzip开启标志*/
        const val KEY_GZIP = "isGzip"
        /**开启标志*/
        const val VALUE_OPEN = "1"
        /**关闭标志*/
        const val VALUE_CLOSE = "0"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val isGzip = originalRequest.header(KEY_GZIP)
        if (isEmpty(isGzip) || isGzip == VALUE_CLOSE) {
            return chain.proceed(originalRequest)
        }
        if (originalRequest.body == null || originalRequest.header("Content-Encoding") != null) {
            return chain.proceed(originalRequest)
        }
        val compressedRequest = originalRequest.newBuilder().header("Content-Encoding", "gzip")
            .method(originalRequest.method, gzip(originalRequest.body)).build()
        return chain.proceed(compressedRequest)
    }

    private fun gzip(body: RequestBody?): RequestBody {
        return object : RequestBody() {

            override fun contentType(): MediaType? {
                return body?.contentType()
            }

            override fun contentLength(): Long {
                return -1 // 无法知道压缩后的数据大小
            }

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                val gzipSink = GzipSink(sink).buffer()
                body?.writeTo(gzipSink)
                gzipSink.close()
            }
        }
    }
}