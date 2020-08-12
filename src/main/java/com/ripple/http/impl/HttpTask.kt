package com.ripple.http.impl

import android.os.Handler
import com.alibaba.fastjson.JSON
import com.ripple.http.base.HttpMethod
import com.ripple.http.base.IHttpRequest
import com.ripple.http.base.IRequestParams
import com.ripple.http.callback.OnHttpResult
import com.ripple.http.demo.isArrayType
import com.ripple.http.exception.BaseException
import com.ripple.http.link.HttpLinkItemModel
import com.ripple.http.link.HttpLinkModel
import com.ripple.http.util.ParameterizedTypeUtil
import com.ripple.task.callback.OnItemDoing
import com.ripple.tool.check.isEmpty
import com.ripple.tool.extend.forEach
import com.ripple.tool.kttypelians.SuccessLambda
import kotlinx.coroutines.*
import okhttp3.Call
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.reflect


/**
 * Author: fanyafeng
 * Data: 2020/8/3 14:04
 * Email: fanyafeng@live.cn
 * Description:
 */
internal class HttpTask : IHttpRequest {

    companion object {
        /**
         * 协程入参最大的线程数
         */
        private const val MAX_PRIORITY_THREADS = 256
    }

    /**
     * 请求链
     */
    private val httpLinkList: LinkedList<HttpLinkModel> = LinkedList()

    /**
     * 计数器初始值为-1
     * 从0开始
     */
    private var index = AtomicInteger(-1)

    /**
     * 防止二次发送请求
     */
    private var hasRequest = AtomicBoolean(false)

    /**
     * 重置方法
     * 每当从get() post调用都需要进行重置
     * 此时相当于又新开了一个请求
     */
    private fun init() {
        hasRequest.set(false)
        index.set(-1)
        httpLinkList.clear()
    }

    /**
     * 相当于新开一个node节点
     * http model的队首肯定是从以下几种开始
     * get()
     * thenGet()
     *
     * post()
     * thenPost()
     */
    override fun <T> get(params: IRequestParams.IHttpRequestParams, callback: OnHttpResult<T>) {
        get(params, callback, null)
    }

    internal fun <T> get(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>,
        lambda: SuccessLambda<Any>
    ) {
        init()
        params.method = HttpMethod.GET
        val httpLinkItemModel = HttpLinkItemModel(params, callback as OnHttpResult<Any>, lambda)
        val httpLinkModel =
            HttpLinkModel(index.incrementAndGet(), httpLinkItemModel, mutableListOf())
        httpLinkList.addLast(httpLinkModel)
        doRequest(index.get())
    }

    /**
     * 同级调用，
     * 请求不应该添加到head中
     * 需要添加到附带请求列表中
     */
    override fun <T> withGet(params: IRequestParams.IHttpRequestParams, callback: OnHttpResult<T>) {
        withGet(params, callback, null)
    }

    internal fun <T> withGet(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>,
        lambda: SuccessLambda<Any>
    ) {
        params.method = HttpMethod.GET
        val httpLinkItemModel = HttpLinkItemModel(params, callback as OnHttpResult<Any>, lambda)
        val httpLinkModel = httpLinkList[index.get()]
        httpLinkModel.follow.add(httpLinkItemModel)
        doRequest(index.get())
    }

    /**
     * 需要新开一个node节点，但是不能重置链式列表
     * 此时需要加到head中
     * 并且将index加一
     */
    override fun <T> thenGet(params: IRequestParams.IHttpRequestParams, callback: OnHttpResult<T>) {
        thenGet(params, callback, null)
    }

    internal fun <T> thenGet(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>,
        lambda: SuccessLambda<Any>
    ) {
        params.method = HttpMethod.GET
        val httpLinkItemModel = HttpLinkItemModel(params, callback as OnHttpResult<Any>, lambda)
        val httpLinkModel =
            HttpLinkModel(index.incrementAndGet(), httpLinkItemModel, mutableListOf())
        httpLinkList.addLast(httpLinkModel)
        doRequest(index.get())
    }

    //下方post请求同get请求
    override fun <T> post(params: IRequestParams.IHttpRequestParams, callback: OnHttpResult<T>) {
        post(params, callback, null)
    }

    internal fun <T> post(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>,
        lambda: SuccessLambda<Any>
    ) {
        init()
        params.method = HttpMethod.POST
        val httpLinkItemModel = HttpLinkItemModel(params, callback as OnHttpResult<Any>, lambda)
        val httpLinkModel =
            HttpLinkModel(index.incrementAndGet(), httpLinkItemModel, mutableListOf())
        httpLinkList.addLast(httpLinkModel)
        doRequest(index.get())
    }

    override fun <T> withPost(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>
    ) {
        withPost(params, callback, null)
    }

    internal fun <T> withPost(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>,
        lambda: SuccessLambda<Any>
    ) {
        params.method = HttpMethod.POST
        val httpLinkItemModel = HttpLinkItemModel(params, callback as OnHttpResult<Any>, lambda)
        val httpLinkModel = httpLinkList[index.get()]
        httpLinkModel.follow.add(httpLinkItemModel)
        doRequest(index.get())
    }

    override fun <T> thenPost(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>
    ) {
        thenPost(params, callback, null)
    }

    internal fun <T> thenPost(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>,
        lambda: SuccessLambda<Any>
    ) {
        params.method = HttpMethod.POST
        val httpLinkItemModel = HttpLinkItemModel(params, callback as OnHttpResult<Any>, lambda)
        val httpLinkModel =
            HttpLinkModel(index.incrementAndGet(), httpLinkItemModel, mutableListOf())
        httpLinkList.addLast(httpLinkModel)
        doRequest(index.get())
    }

    /**
     * 链式请求创建完成后需要有一个请求的时机
     * 有两种方案：
     * 显示请求：固定的关键字，比如.start()
     * 隐示请求：300毫秒后开始请求
     * 这里选择隐示请求，好处如下
     * 省去关键字，防止使用者忘记关键字的书写
     *
     * 可以手动切换为.start()
     * 考虑到有些需要对网络时间要求苛刻的
     *
     * 调用时机根据index的变化
     */
    private fun doRequest(nowIndex: Int) {
        /**
         * 如果延时300毫秒后两个值时相同的则认为用户是没有操作的，开始进行请求的调用
         */
        Handler().postDelayed({
            val linkIndex = httpLinkList.last.index
            if (nowIndex == linkIndex) {
                if (!hasRequest.get()) {
                    start()
                }
            }
        }, 300)
    }

    fun start() {
        hasRequest.set(true)
        /**
         * 构造协程实例
         */
        val coroutineScope =
            CoroutineScope(
                Executors.newFixedThreadPool(MAX_PRIORITY_THREADS).asCoroutineDispatcher()
            )
        val httpLinkSize = httpLinkList.size

        /**
         * 获取httpLinkIndex的示例，这些都是同时执行的
         */
        if (httpLinkSize > 0) {
            val httpLinkModel = httpLinkList[0]
            coroutineScope.launch(Dispatchers.IO) {
                executeHttpLinkItem(0, httpLinkModel)
            }
        }
    }


    private fun executeHttpLinkItem(
        index: Int,
        httpLinkModel: HttpLinkModel
    ) {
        /**
         * 计数器
         */
        val executeCount = AtomicInteger(0)

        /**
         * 是否取消下一个请求
         * 针对上一个请求失败但是已经完成的情况下
         * 下一个请求又依赖上一个请求的结果，这样的haul再去进行下一个请求是无意义的
         */
        val cancelNext = AtomicBoolean(false)

        /**
         * head肯定不为空
         */
        val head = httpLinkModel.head
        val headParams = head.params
        val headCallback = head.callback
        val headClazz =
            if (head.lambda != null) {
                getType(head.lambda)
            } else {
                parseItemParamType(headCallback)
            }


        /**
         * follow肯定不为空，但是可能是空列表，即size=0
         */
        val follow = httpLinkModel.follow
        val followSize = follow.size
        val totalCount = followSize + 1

        val headInnerCallBack = object : OnHttpResult<Any> {
            override fun onItemStart(startResult: Unit) {
                super.onItemStart(startResult)
                headCallback.onItemStart(startResult)
            }

            override fun onItemDoing(doingResult: Long) {
                headCallback.onItemDoing(doingResult)
            }

            override fun onItemSuccess(successResult: Any) {
                headCallback.onItemSuccess(successResult)
            }

            override fun onItemFinish(finishResult: Boolean) {
                headCallback.onItemFinish(finishResult)
                executeCount.incrementAndGet()
                cancelNext.set(headParams.cancelNext() ?: false)
                if (executeCount.get() == totalCount) {
                    var targetIndex = index + 1
                    if (!cancelNext.get()) {
                        if (httpLinkList.size > targetIndex) {
                            val httpLinkModelItem = httpLinkList[targetIndex]
                            executeHttpLinkItem(targetIndex, httpLinkModelItem)
                        }
                    } else {
                        targetIndex += 1
                        if (httpLinkList.size > targetIndex) {
                            val httpLinkModelItem = httpLinkList[targetIndex]
                            executeHttpLinkItem(targetIndex, httpLinkModelItem)
                        }
                    }
                }
            }

            override fun onItemFailed(failedResult: BaseException) {
                headCallback.onItemFailed(failedResult)
            }

        }
        call(headParams, headInnerCallBack, headClazz)

        followSize.forEach { followIndex ->
            val followHttpLinkItemModel = follow[followIndex]
            val params = followHttpLinkItemModel.params
            val callback = followHttpLinkItemModel.callback
            val clazz =
                if (head.lambda != null) {
                    getType(head.lambda)
                } else {
                    parseItemParamType(callback)
                }
            val innerCallBack = object : OnHttpResult<Any> {
                override fun onItemStart(startResult: Unit) {
                    super.onItemStart(startResult)
                    callback.onItemStart(startResult)
                }

                override fun onItemDoing(doingResult: Long) {
                    super.onItemDoing(doingResult)
                    callback.onItemDoing(doingResult)
                }

                override fun onItemSuccess(successResult: Any) {
                    callback.onItemSuccess(successResult)
                }

                override fun onItemFinish(finishResult: Boolean) {
                    callback.onItemFinish(finishResult)
                    executeCount.incrementAndGet()
                    cancelNext.set(params.cancelNext() ?: false)
                    if (executeCount.get() == totalCount) {
                        var targetIndex = index + 1
                        if (!cancelNext.get()) {
                            if (httpLinkList.size > targetIndex) {
                                val httpLinkModelItem = httpLinkList[targetIndex]
                                executeHttpLinkItem(targetIndex, httpLinkModelItem)
                            }
                        } else {
                            targetIndex += 1
                            if (httpLinkList.size > targetIndex) {
                                val httpLinkModelItem = httpLinkList[targetIndex]
                                executeHttpLinkItem(targetIndex, httpLinkModelItem)
                            }
                        }
                    }
                }

                override fun onItemFailed(failedResult: BaseException) {
                    callback.onItemFailed(failedResult)
                }

            }
            call(params, innerCallBack, clazz)
        }
    }

    private fun getType(lambda: SuccessLambda<Any>): Class<*> {
        var isListResult = false
        var itemKClass: Class<*> = String::class.java

        val paramType = if (lambda != null) {
            val reflect = lambda.reflect()
            var result: KType? = null
            if (reflect != null) {
                result = reflect.parameters[0].type
            }
            result
        } else {
            String::class.createType()
        }

        var clazz = (paramType?.classifier as KClass<*>).java
        if (clazz.isArrayType()) {
            clazz = (paramType.arguments[0].type!!.classifier as KClass<*>).java
            isListResult = true
        }
        itemKClass = if (isListResult) {
            List::class.java
        } else {
            clazz
        }
        return itemKClass
    }

    private fun parseItemParamType(paramEntity: Any?): Class<*> {
        var itemKClass: Class<*> = String::class.java
        var isListResult = false
        if (paramEntity == null) {
            return itemKClass
        }
        if (paramEntity is KFunction<*>) {
            val paramType = paramEntity.parameters[0].type
            var clazz = (paramType.classifier as KClass<*>).java
            if (clazz.isArrayType()) {
                clazz = (paramType.arguments[0].type!!.classifier as KClass<*>).java
                isListResult = true
            }
            itemKClass = if (isListResult) {
                List::class.java
            } else {
                clazz
            }
        } else if (paramEntity is ParameterizedType) {
            if (paramEntity.rawType.isArrayType()) {
                isListResult = true
                val arg0 = paramEntity.actualTypeArguments[0]
                if (arg0 is WildcardType) {
                    if (!isEmpty(arg0.upperBounds)) {
                        itemKClass = arg0.upperBounds[0] as Class<*>
                    } else {
                        itemKClass = arg0.lowerBounds[0] as Class<*>
                    }
                } else {
                    itemKClass = paramEntity.actualTypeArguments[0] as Class<*>
                }
            } else {
                itemKClass = paramEntity.rawType as Class<*>
            }
        } else {
            val hasInterfaceT = ParameterizedTypeUtil.hasInterfaceT(paramEntity)
            if (hasInterfaceT) {

                val params: Array<Type> =
                    paramEntity.javaClass.genericInterfaces
                val type = params[0]
                val finalNeedType = if (params.size > 1) {
                    check(type is ParameterizedType) { "没有填写泛型参数" }
                    (type as ParameterizedType).actualTypeArguments[0]
                } else {
                    type
                }
                if (finalNeedType is ParameterizedType) {
                    if (finalNeedType.actualTypeArguments[0].isArrayType()) {
                        isListResult = true
                    }
                }
                itemKClass = ParameterizedTypeUtil.getClass(finalNeedType, 0) as Class<*>
            } else {
                val directParameterizedType =
                    ParameterizedTypeUtil.getDirectParameterizedType(paramEntity::class.java)
                if (directParameterizedType is ParameterizedType) {
                    if (directParameterizedType.rawType.isArrayType()) {
                        isListResult = true
                        itemKClass =
                            directParameterizedType.actualTypeArguments[0] as Class<*>
                    } else {
                        itemKClass = directParameterizedType.rawType as Class<*>
                    }
                } else {
                    itemKClass = directParameterizedType as Class<*>
                }
            }
        }
        return itemKClass
    }

    /**
     * 同步方法
     * 目的是为了获取结果，完成链式调用
     */
    @JvmOverloads
    private fun <T> call(
        params: IRequestParams.IHttpRequestParams,
        callback: OnHttpResult<T>,
        clazz: Class<*>? = null,
        successLambda: SuccessLambda<Call> = null
    ) {
        callback.onItemStart(Unit)
        callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_START)
        val coroutineScope =
            CoroutineScope(
                Executors.newFixedThreadPool(MAX_PRIORITY_THREADS).asCoroutineDispatcher()
            )
        coroutineScope.launch {
            launch(Dispatchers.IO) {
                /**
                 * 初始化params
                 */
                params.init()
                val call = OkHttpClientDelegate().request(params)
                successLambda?.invoke(call)
                /**
                 * 如果call为空则认为任务被取消
                 * 否则正常执行
                 */
                try {
                    /**
                     * 执行请求
                     */
                    val response = call.execute()

                    /**
                     * 获取请求结果
                     */
                    val result = response.body?.string()
                    if (result != null) {
                        /**
                         * 解析请求结果
                         */
                        params.response.response = result
                        if (clazz != null) {
                            params.response.itemKClass = clazz
                        } else {
                            params.response.parseItemParamType(callback)
                        }
                        val backResult = params.response.parser.parseData<T>(
                            JSON.parseObject(result),
                            params.response
                        )
                        if (backResult != null) {
                            launch(Dispatchers.Main) {
                                /**
                                 * 成功执行完成后
                                 * 进度条：[0-100] 0为开始 -1失败 100成功
                                 * 请求成功回调请求结果
                                 * 请求完成回调完成为true
                                 */
                                callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_FINISH)
                                callback.onItemSuccess(backResult)
                                callback.onItemFinish(true)
                            }
                        } else {
                            launch(Dispatchers.Main) {
                                /**
                                 * 成功执行完成后 失败
                                 * 进度条：[0-100] 0为开始 -1失败 100成功
                                 * 请求成功回调请求结果
                                 * 请求完成回调完成为false
                                 */
                                callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_FAILED)
                                callback.onItemFailed(BaseException(msg = "请求为空，请查看请求是否正确"))
                                callback.onItemFinish(false)
                            }
                        }

                    } else {
                        launch(Dispatchers.Main) {
                            /**
                             * 成功执行完成后 失败
                             * 进度条：[0-100] 0为开始 -1失败 100成功
                             * 请求成功回调请求结果
                             * 请求完成回调完成为false
                             */
                            callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_FAILED)
                            callback.onItemFailed(BaseException(msg = "请求为空，请查看请求是否正确"))
                            callback.onItemFinish(false)
                        }
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        /**
                         * 成功执行完成后 失败
                         * 进度条：[0-100] 0为开始 -1失败 100成功
                         * 请求成功回调请求结果
                         * 请求完成回调完成为false
                         */
                        callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_FAILED)
                        callback.onItemFailed(BaseException(e))
                        callback.onItemFinish(false)
                    }
                }
            }
        }
    }
}