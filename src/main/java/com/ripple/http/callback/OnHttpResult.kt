package com.ripple.http.callback

import com.ripple.http.exception.BaseException
import com.ripple.task.callback.*


/**
 * Author: fanyafeng
 * Data: 2020/7/23 09:18
 * Email: fanyafeng@live.cn
 * Description:
 */
interface OnHttpResult<T> : OnItemSuccess<T>,
    OnItemFinish<Boolean>,
    OnItemStart<Unit>,
    OnItemDoing<Long>,
    OnItemFailed<BaseException> {

    /**
     * 任务开始回调
     */
    override fun onItemStart(startResult: Unit) {
    }

    /**
     * 任务进行中回调
     */
    override fun onItemDoing(doingResult: Long) {
    }


    interface OnHttpSimpleResult<T> : OnHttpResult<T> {
        override fun onItemSuccess(successResult: T) {
        }

        override fun onItemFinish(finishResult: Boolean) {
        }

        /**
         * 任务被打断回调
         * 被动取消或者主动取消
         * 最后都会走到Failed中
         */
        override fun onItemFailed(failedResult: BaseException) {

        }
    }
}