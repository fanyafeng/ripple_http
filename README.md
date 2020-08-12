# ripple_http
封装http请求，主要是帮助开发者更好的去专注于业务，并且能够完成一些复杂的链式请求，这里不讲具体的实现代码，在前两个文章中已经将具体的框架编写过程写的很清楚了，这里主要讲一下原理以及具体的使用
PS：因为自己犯懒，所以跳过`0.0.1`和`0.0.2`版本的说明，直接到`0.0.3`了，也是个库的质变版本
接入：

```

//根目录gradle
maven {
            url "https://dl.bintray.com/fanyafeng/ripple"
        }
        
        
//module目录gradle
implementation 'com.ripple.component:http:0.0.3'
```

## 一、背景
一般情况下大家都是单请求拿结果，还有就是基本是一个域名下开发，但是当业务复杂的时候不免会发生链式请求（包含链式的串行和并行）问题，这个库主要就是为了解决这个问题，另外就是很好的利用了`kotlin`的语法糖，这个想法是在ML大家的想法，也是在之前库的重构，并且用了`kotlin`的协程，但是还是不算完善，趁着有时间把`0.0.3`的博客补齐了，不然越欠越多
## 二、目标
在做事情之前都会定一个目标，中间难免会遇到一些问题，但是殊途同归，都是为了达到自己的目标。
下面说一下当时的想法：
1. 链式串行调用，下一个调用是在上一个任务完成时进行调用或者取消（依赖调用的请求失败，那么这个任务也没有必要请求和继续了）
2. 链式并行调用，同时有多个请求同时发起请求，互不影响无依赖，主要是为了简化调用
3. 链式串行并行同事调用，这时候就属于以上两种都涉及到，并且以上两种的异常情况都需要考虑在内，举例：`A`和`B`是两个请求，但是`C`需要依赖`A`和`B`的结果，那么`C`就需要在`A`和`B`都完成时再去调用
4. 无明显调用方法式调用，能够在调用链完成后自动发起调用，不需要`.start()`类的关键字
## 三、设计原理
本来应该上图的，但是就是懒，就拿代码画一下吧，如下

```
/**
 * Author: fanyafeng
 * Data: 2020/8/10 10:16
 * Email: fanyafeng@live.cn
 * Description:
 * 
 * 
 * ------------
 * |           |
 * |           |
 * | post/get  |
 * |           |
 * |           |
 * -------------
 *       |
 *       |
 *       |  then
 *       |
 *       |
 * -------------                   --------------                   --------------                   -------------- 
 * |            |                  |             |                  |             |                  |             |
 * |            |       with       |             |        with      |             |      with        |             |
 * |  post/get  | -----------------|  post/get   | -----------------|  post/get   | -----------------|   post/get  |
 * |            |                  |             |                  |             |                  |             |
 * |            |                  |             |                  |             |                  |             |
 * --------------                  ---------------                  ---------------                  ---------------            
 *                                                                                                         |       
 *                                                                                                         |       
 *                                                                                                         |  then     
 *                                                                                                         |       
 *                                                                                                         |       
 *                                                                                                   ------------- 
 *                                                                                                   |            |
 *                                                                                                   |            |
 *                                                                                                   |  post/get  |
 *                                                                                                   |            |
 *                                                                                                   |            |
 *                                                                                                   --------------
 *                                                                                                         |       
 *                                                                                                         |       
 *                                                                                                         |  then     
 *                                                                                                         |       
 *                                                                                                         |       
 *                                                                                                   ------------- 
 *                                                                                                   |            |
 *                                                                                                   |            |
 *                                                                                                   | post/get   |
 *                                                                                                   |            |
 *                                                                                                   |            |
 *                                                                                                   --------------
 * 
 */
```
### 3.1 抽离数据结构
以上便是设想的调用方式，通过上图抽离数据结构，很明显每个`then`的`post`或者`get`都是头，之后的`with`便是附加列表，换句话说，如果没有`then`那么这个就是都头了，`with`是跟在`then`之后的，数学模型现在基本就抽离了，每个`then`都是一个节点的头，之后每个`with`都是`next`而且顺序前后无所谓，但是每个节点都是有顺序的，下面的两个`data model`便是抽离的数据结构

```
private val httpLinkList: LinkedList<HttpLinkModel> = LinkedList()

/**
 * 链式调用的model
 */
internal class HttpLinkModel(
    val index: Int,
    val head: HttpLinkItemModel,
    val follow: MutableList<HttpLinkItemModel>
) : Serializable
```
### 3.2 分析并实现链式调用
数据结构定好就是具体的业务实现了，每个请求都可以抽为一个方法，每个请求的方法都有相应的回调（最主要的就是：`onItemFinish`回调，不论成功与否，只要请求结束都会走到此），如果只有`then`那么只要通过迭代的方式在`onItemFinish`中处理串行`next`即可，但是，如果`with`和`then`并存那么需要计数，在所有同级都走到`onItemFinish`中进行处理，这里面还会涉及到同级失败取消`next`请求的问题，只要任意一个同级`params`设置`cancelNext`即可（手动取消或根据同级调用状态取消即可），当请求链构造完成后（通过比较300毫秒前后的链的长度，也可以通过`.start()`显示调用）便发起请求。以上就是思路，下面贴一下核心代码（代码有点长，省略了一部分）：

```
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
            }
            call(params, innerCallBack, clazz)
        }
    }
```
## 四、调用
以上这些便是`http`的核心部分，具体实现可以移步`github`以及上面两篇文章(里面有设计思想以及抽离的核心接口代码)，这里不细讲了，文章以及`git`在下面
* [Ripple Http库地址](https://github.com/fanyafeng/ripple_http)
* [自己动手编写http框架（一）](https://blog.csdn.net/qq_23195583/article/details/107544821)
* [自己动手编写http框架（二）](https://blog.csdn.net/qq_23195583/article/details/107821289)


下面是具体调用实例：

```
            httpGet {
                val userParam = UserParam()
                params = userParam

                onSuccess<User> {
                    it.name.toLogD()
                }
            }.withGet {
                val userParam = UserParam()
                params = userParam

                onSuccess<User> {
                    it.name.toLogD("随行get请求")
                }
                
                onFailed { 
                    //取消之后的请求
                    userParam.setCancelNext(true)
                }
            }.thenPost {
                val userListPostParam = UserListPostIdParam()
                params = userListPostParam

                onSuccess<List<User>> {
                    it.toLogD()
                }
            }
```



