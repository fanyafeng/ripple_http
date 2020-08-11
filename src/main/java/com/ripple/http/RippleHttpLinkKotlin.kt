package com.ripple.http

import com.ripple.http.base.IRequestParams
import com.ripple.http.callback.OnHttpResult
import com.ripple.http.demo.isArrayType
import com.ripple.http.exception.BaseException
import com.ripple.http.impl.HttpTask
import com.ripple.tool.kttypelians.FinishLambda
import com.ripple.tool.kttypelians.SuccessLambda
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.reflect

/**
 * Author: fanyafeng
 * Data: 2020/8/10 10:16
 * Email: fanyafeng@live.cn
 * Description:
 */
class RippleHttpLinkKotlin internal constructor(private val httpTask: HttpTask) {

    /**
     * 同时调用get请求
     *
     */
    fun withGet(
        params: IRequestParams.IHttpRequestParams,
        lambda: SuccessLambda<Any>,
        successLambda: SuccessLambda<Any>,
        finishLambda: FinishLambda = null,
        startLambda: SuccessLambda<Unit> = null,
        doingLambda: SuccessLambda<Long> = null,
        failedLambda: SuccessLambda<BaseException> = null
    ): RippleHttpLinkKotlin {
        val callback = object : OnHttpResult<Any> {
            override fun onItemStart(startResult: Unit) {
                super.onItemStart(startResult)
                startLambda?.invoke(startResult)
            }

            override fun onItemDoing(doingResult: Long) {
                super.onItemDoing(doingResult)
                doingLambda?.invoke(doingResult)
            }

            override fun onItemSuccess(successResult: Any) {
                successLambda?.invoke(successResult)
            }

            override fun onItemFinish(finishResult: Boolean) {
                finishLambda?.invoke(finishResult)
            }

            override fun onItemFailed(failedResult: BaseException) {
                failedLambda?.invoke(failedResult)
            }

        }
        if (successLambda != null) {
            httpTask.withGet(params, callback, lambda)
        }
        return this
    }

    /**
     * 上一个任务完成后
     * 再去掉用get请求
     */
    fun thenGet(
        params: IRequestParams.IHttpRequestParams,
        lambda: SuccessLambda<Any>,
        successLambda: SuccessLambda<Any>,
        finishLambda: FinishLambda = null,
        startLambda: SuccessLambda<Unit> = null,
        doingLambda: SuccessLambda<Long> = null,
        failedLambda: SuccessLambda<BaseException> = null
    ): RippleHttpLinkKotlin {
        val callback = object : OnHttpResult<Any> {
            override fun onItemStart(startResult: Unit) {
                super.onItemStart(startResult)
                startLambda?.invoke(startResult)
            }

            override fun onItemDoing(doingResult: Long) {
                super.onItemDoing(doingResult)
                doingLambda?.invoke(doingResult)
            }

            override fun onItemSuccess(successResult: Any) {
                successLambda?.invoke(successResult)
            }

            override fun onItemFinish(finishResult: Boolean) {
                finishLambda?.invoke(finishResult)
            }

            override fun onItemFailed(failedResult: BaseException) {
                failedLambda?.invoke(failedResult)
            }

        }
        if (successLambda != null) {
            httpTask.thenGet(params, callback, lambda)
        }
        return this
    }

    /**
     * 同时调用post请求
     */
    fun withPost(
        params: IRequestParams.IHttpRequestParams,
        lambda: SuccessLambda<Any>,
        successLambda: SuccessLambda<Any>,
        finishLambda: FinishLambda = null,
        startLambda: SuccessLambda<Unit> = null,
        doingLambda: SuccessLambda<Long> = null,
        failedLambda: SuccessLambda<BaseException> = null
    ): RippleHttpLinkKotlin {
        val callback = object : OnHttpResult<Any> {
            override fun onItemStart(startResult: Unit) {
                super.onItemStart(startResult)
                startLambda?.invoke(startResult)
            }

            override fun onItemDoing(doingResult: Long) {
                super.onItemDoing(doingResult)
                doingLambda?.invoke(doingResult)
            }

            override fun onItemSuccess(successResult: Any) {
                successLambda?.invoke(successResult)
            }

            override fun onItemFinish(finishResult: Boolean) {
                finishLambda?.invoke(finishResult)
            }

            override fun onItemFailed(failedResult: BaseException) {
                failedLambda?.invoke(failedResult)
            }

        }
        if (successLambda != null) {
            httpTask.withPost(params, callback, lambda)
        }
        return this
    }

    /**
     * 上一个任务完成后再去调用post请求
     */
    fun thenPost(
        params: IRequestParams.IHttpRequestParams,
        lambda: SuccessLambda<Any>,
        successLambda: SuccessLambda<Any>,
        finishLambda: FinishLambda = null,
        startLambda: SuccessLambda<Unit> = null,
        doingLambda: SuccessLambda<Long> = null,
        failedLambda: SuccessLambda<BaseException> = null
    ): RippleHttpLinkKotlin {
        val callback = object : OnHttpResult<Any> {
            override fun onItemStart(startResult: Unit) {
                super.onItemStart(startResult)
                startLambda?.invoke(startResult)
            }

            override fun onItemDoing(doingResult: Long) {
                super.onItemDoing(doingResult)
                doingLambda?.invoke(doingResult)
            }

            override fun onItemSuccess(successResult: Any) {
                successLambda?.invoke(successResult)
            }

            override fun onItemFinish(finishResult: Boolean) {
                finishLambda?.invoke(finishResult)
            }

            override fun onItemFailed(failedResult: BaseException) {
                failedLambda?.invoke(failedResult)
            }

        }
        if (successLambda != null) {
            httpTask.thenPost(params, callback, lambda)
        }
        return this
    }

    /**
     * .thenPost or .thenGet肯定是队列的头
     * .withPost or .withGet肯定为列表的子
     */
    fun start() {
        httpTask.start()
    }


}