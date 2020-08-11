package com.ripple.http.extend

import com.ripple.http.RippleHttp
import com.ripple.http.RippleHttpLink
import com.ripple.http.RippleHttpLinkKotlin
import com.ripple.http.base.HttpMethod
import com.ripple.http.base.IRequestParams
import com.ripple.http.callback.OnHttpResult
import com.ripple.http.demo.isArrayType
import com.ripple.http.exception.BaseException
import com.ripple.log.tpyeextend.toLogD
import com.ripple.tool.kttypelians.FinishLambda
import com.ripple.tool.kttypelians.SuccessLambda
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.reflect


/**
 * Author: fanyafeng
 * Data: 2020/8/4 14:22
 * Email: fanyafeng@live.cn
 * Description:
 * http kotlin 扩展类
 */


fun httpGet(lambda: RippleHttpExtra.() -> Unit): RippleHttpLinkExtend {
    val rippleHttpExtra = RippleHttpExtra(HttpMethod.GET)
    rippleHttpExtra.apply {
        lambda()
        return RippleHttpLinkExtend(request())
    }
}

fun httpPost(lambda: RippleHttpExtra.() -> Unit): RippleHttpLinkExtra {
    val rippleHttpExtra = RippleHttpExtra(HttpMethod.POST)
    rippleHttpExtra.apply {
        lambda()
        return RippleHttpLinkExtra(request())
    }
}

class RippleHttpLinkExtra(private val rippleHttpLinkKotlin: RippleHttpLinkKotlin) {
    lateinit var params: IRequestParams.IHttpRequestParams

    private var mSuccessLambda: SuccessLambda<Any> = null
    private var finishLambda: FinishLambda = null
    private var startLambda: SuccessLambda<Unit> = null
    private var doingLambda: SuccessLambda<Long> = null
    private var failedLambda: SuccessLambda<BaseException> = null

    fun <T> onSuccess(successLambda: SuccessLambda<T>) {
        mSuccessLambda = successLambda as SuccessLambda<Any>
    }

    fun onFinish(finishLambda: FinishLambda) {
        this.finishLambda = finishLambda
    }

    fun onStart(startLambda: SuccessLambda<Unit>) {
        this.startLambda = startLambda
    }

    fun onDoing(doingLambda: SuccessLambda<Long>) {
        this.doingLambda = doingLambda
    }

    fun onFailed(failedLambda: SuccessLambda<BaseException>) {
        this.failedLambda = failedLambda
    }

    fun withGet(lambda: RippleHttpLinkExtra.() -> Unit) {
        this.apply(lambda)
        rippleHttpLinkKotlin.withGet(
            params,
            mSuccessLambda,
            mSuccessLambda,
            finishLambda,
            startLambda,
            doingLambda,
            failedLambda
        )
    }

    fun thenGet(lambda: RippleHttpLinkExtra.() -> Unit) {
        this.apply(lambda)
        rippleHttpLinkKotlin.thenGet(
            params,
            mSuccessLambda,
            mSuccessLambda,
            finishLambda,
            startLambda,
            doingLambda,
            failedLambda
        )
    }

    fun withPost(lambda: RippleHttpLinkExtra.() -> Unit) {
        this.apply(lambda)
        rippleHttpLinkKotlin.withPost(
            params,
            mSuccessLambda,
            mSuccessLambda,
            finishLambda,
            startLambda,
            doingLambda,
            failedLambda
        )
    }

    fun thenPost(lambda: RippleHttpLinkExtra.() -> Unit) {
        this.apply(lambda)
        rippleHttpLinkKotlin.thenPost(
            params,
            mSuccessLambda,
            mSuccessLambda,
            finishLambda,
            startLambda,
            doingLambda,
            failedLambda
        )
    }

    fun start() {
        rippleHttpLinkKotlin.start()
    }

}

class RippleHttpLinkExtend(
    private val rippleHttpLinkKotlin: RippleHttpLinkKotlin
) {
    fun withGet(lambda: RippleHttpLinkExtra.() -> Unit): RippleHttpLinkExtend {
        val rippleHttpLinkExtra = RippleHttpLinkExtra(rippleHttpLinkKotlin)
        rippleHttpLinkExtra.withGet(lambda)
        return this
    }

    fun thenGet(lambda: RippleHttpLinkExtra.() -> Unit): RippleHttpLinkExtend {
        val rippleHttpLinkExtra = RippleHttpLinkExtra(rippleHttpLinkKotlin)
        rippleHttpLinkExtra.thenGet(lambda)
        return this
    }

    fun withPost(lambda: RippleHttpLinkExtra.() -> Unit): RippleHttpLinkExtend {
        val rippleHttpLinkExtra = RippleHttpLinkExtra(rippleHttpLinkKotlin)
        rippleHttpLinkExtra.withPost(lambda)
        return this
    }

    fun thenPost(lambda: RippleHttpLinkExtra.() -> Unit): RippleHttpLinkExtend {
        val rippleHttpLinkExtra = RippleHttpLinkExtra(rippleHttpLinkKotlin)
        rippleHttpLinkExtra.thenPost(lambda)
        return this
    }

    fun start() {
        val rippleHttpLinkExtra = RippleHttpLinkExtra(rippleHttpLinkKotlin)
        rippleHttpLinkExtra.start()
    }
}

class RippleHttpExtra(private val method: HttpMethod) {
    lateinit var params: IRequestParams.IHttpRequestParams

    private var mSuccessLambda: SuccessLambda<Any> = null
    private var finishLambda: FinishLambda = null
    private var startLambda: SuccessLambda<Unit> = null
    private var doingLambda: SuccessLambda<Long> = null
    private var failedLambda: SuccessLambda<BaseException> = null

    fun <T> onSuccess(successLambda: SuccessLambda<T>) {
        mSuccessLambda = successLambda as SuccessLambda<Any>
    }

    fun onFinish(finishLambda: FinishLambda) {
        this.finishLambda = finishLambda
    }

    fun onStart(startLambda: SuccessLambda<Unit>) {
        this.startLambda = startLambda
    }

    fun onDoing(doingLambda: SuccessLambda<Long>) {
        this.doingLambda = doingLambda
    }

    fun onFailed(failedLambda: SuccessLambda<BaseException>) {
        this.failedLambda = failedLambda
    }

    fun request(): RippleHttpLinkKotlin {
        params.method = method

        return RippleHttp.request(params, mSuccessLambda, object : OnHttpResult<Any> {
            override fun onItemStart(startResult: Unit) {
                super.onItemStart(startResult)
                startLambda?.invoke(startResult)
            }

            override fun onItemDoing(doingResult: Long) {
                super.onItemDoing(doingResult)
                doingLambda?.invoke(doingResult)
            }

            override fun onItemSuccess(successResult: Any) {
                mSuccessLambda?.invoke(successResult)
            }

            override fun onItemFinish(finishResult: Boolean) {
                finishLambda?.invoke(finishResult)
            }

            override fun onItemFailed(failedResult: BaseException) {
                failedLambda?.invoke(failedResult)
            }

        })
    }

}
