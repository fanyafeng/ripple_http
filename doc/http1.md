# 自己动手编写http框架（一）
之前都是自己想好思路然后编写框架，也可能是之前的比较简单吧，不用那么费时间，然后现在要写最常用的`http`使用框架，相信大家基本都有相同的经历，如果不是在一个公司从头开始的，那么基本公司都有相对完善的`http`框架，很少有从0到1的过程，这也是为啥这个框架到现在才去写的原因，因为之前大部分是时间处于伸手党，用的多，写得少，或者是从半路开始写的，这里想根据自己的思路从头开始写，一步一步来，正好也开始写博客来记录一下过程。
## 一、http请求
第一步就是先去使用，然后再去抽离
本来想找个`mock`数据的，但是好多不能用了，就用`springboot`简单搭了一个，没有用数据库，这样测试起来也比较方便
现在基本都使用的是`okhttp`了，这里基于`okhttp`，这里强调一下，其实这样从框架层面来说是不对的，应该参考一下图片框架，底层使用的是`HTTPURLconnection`，然后支持用户去注入网络请求框架，下面开始分析一下常用的网络的请求方式，以及需要设置哪些属性等。
做这个不要着急，一步一步来
PS:我依赖的`okhttp`是`4.7.2`，已经全部是用`Kotlin`写的了，正好同时自己也去加深一下对`Kotlin`的理解，`http`库里面依赖了之前写的一些工具库。
### 1.1 http get请求
`http`的请求应该子线程中，但是请求本身是同步，这里做了两个测试，一个是同步，一个是异步
带有`header`的暂时先不去考虑，以下暂时罗列一下`get`请求的几种方式：
1. 最常见的就是`path`不加任何参数
2. 另外就是后面以`params`添加参数`path?xxx=xxx&yyy=yyy`
3. 还有就是路径添加参数`path/{xxx}/{yyy}"`
4. 还有就是以`json`的方式进行传参

以上列举的常用的四种情况
#### 1.1.1 第一种情况：
咱们常用的第一种不带参数的异步请求情况：
```
private fun httpGetASync1() {
    val client = OkHttpClient()
    val urlBuilder = url.toHttpUrlOrNull()?.newBuilder()
    val urlResult = urlBuilder?.build()

    val request = Request.Builder()
        .url(urlResult!!)
        .get()

    request.build().headers.logD()
    urlResult.logD()

    client.newCall(request.build()).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.toString().logD()
        }

        override fun onResponse(call: Call, response: Response) {
            val result = response.body?.string()
            result.logD()
            call.isCanceled().logD()
        }

    })
}
```
这里使用的是`OKhttp`自带的异步请求，暂时先基于这一种情况进行封装，现在类型是确认的，但是咱们的目标肯定是从外部传入的，先来定义下调用方式：

```
RippleHttp.getInstance()
            .get(URL, object : OnHttpResult.OnHttpSimpleResult<T> {
                override fun onItemSuccess(successResult: T) {
                    successResult.toLogD()
                }
            })
```
这应该是咱们的目标(一步一步来，暂时不考虑`param`以及`header`等等)，通过外部传入`url`以及泛型`T`来将请求成功的数据进行解析，到现在咱们开始考虑下一步了，`url`传的话肯定是很容易的，下一步比较麻烦的是需要解析方法上的泛型`T`，这里暂时先不细讲，后面会细讲，目标明确了，剩下的就是去按照自己想要的调用方式去编写框架了。
##### 1.1.1.1 首先定义回调
回调的话这里还是需要慎重一下的，这里定义了五种：`成功，完成，开始，进行中，失败`，下面简述一下为什么会有这五种
1. `OnItemSuccess<T>`成功回调，这个是肯定的，咱们的目的就是为了获取数据传给使用者，采用泛型是因为需要依据用户的需求进行解析
2. `OnItemFinish<Boolean>`结束回调，不论失败还是成功，都会走到此回调，因为使用者有时候需要在结束时做相应操作，不论此请求成功与否。
3. `OnItemStart<Unit>`开始回调，标志请求的一种状态
4. `OnItemDoing<Long>`任务进行中回调，这里是为了兼容下载文件时的请求，`0L-100L`为其正常范围，失败的话会返回`-1L`
5. `OnItemFailed<BaseException>`失败回调，会返回失败的原因`msg`以及相应的`code`

##### 1.1.1.2 再有就是解析泛型
这里是个大块，暂时放着，后面会细讲，因为这里涉及到了`java`以及`kotlin`还是有区别的

至此所有准备工作都完成，下面开始封装：

```
    /**
     * 以接口的方式抽离
     */
    fun <S> parseJsonString(result: String?, typeOf: Type): S {
        return Gson().fromJson(result, typeOf)
    }

    /**
     * 异步get请求
     */
    fun <T> get(url: String, callback: OnHttpResult<T>) {
        callback.onItemStart(Unit)
        /**
         * 构造urlBuilder
         */
        val urlBuilder = url.toHttpUrlOrNull()?.newBuilder()

        /**
         * 构造url
         * 使用了大量的构造者模式
         */
        val urlResult = urlBuilder?.build()

        /**
         * 构造request
         */
        val request = Request.Builder()
            .url(urlResult!!)
            .get()
        val requestResult = request.build()
        /**
         * 发起异步请求
         */
        callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_START)
        RippleHttpClient.getInstance().newCall(requestResult).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                /**
                 * 任务进行中回调
                 * 任务失败，返回-1L
                 */
                callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_FAILED)

                /**
                 * 走到结束回调但是任务没完成
                 * 返回false
                 */
                callback.onItemFinish(false)
                /**
                 * call为回传的结果
                 */
                callback.onItemFailed(BaseException(e))
            }

            override fun onResponse(call: Call, response: Response) {
                /**
                 * 走到结束回调并且任务完成
                 * 返回true
                 */
                callback.onItemFinish(true)

                /**
                 * 任务进行中回调
                 * 任务成功，返回100L
                 */
                callback.onItemDoing(OnItemDoing.CODE_ITEM_DOING_FINISH)

                val result = response.body?.string()

                val clazz = parseItemParamType(callback)
                clazz.toLogD("callback 打印泛型名字：")

                val backResult = parseJsonString<T>(result, clazz)
                /**
                 * 任务完成回调
                 * 返回成功model
                 */
                callback.onItemSuccess(backResult)
            }

        })
    }
```
完成后调用：
PS：这里我自己通过`SpringBoot`搭建了一个服务器，`Mock`的好多平台不能用了，还好自己会后端，嘿嘿

```
    /**
     * http get callback请求封装
     */
    private fun httpGetASyncPackCallback1() {
        RippleHttp.getInstance().get(GET_USER, object : OnHttpResult.OnHttpSimpleResult<User> {
            override fun onItemStart(startResult: Unit) {
                super.onItemStart(startResult)
                logD("HTTP任务开始", "Http请求封装：")
            }

            override fun onItemDoing(doingResult: Long) {
                super.onItemDoing(doingResult)
                doingResult.toLogD("Http请求封装：")
            }

            override fun onItemSuccess(successResult: User) {
                successResult.toLogD("Http请求封装：")
            }

            override fun onItemFailed(failedResult: BaseException) {
                super.onItemFailed(failedResult)
                failedResult.toLogD("Http请求封装：")
            }

            override fun onItemFinish(finishResult: Boolean) {
                super.onItemFinish(finishResult)
                finishResult.toLogD("Http请求封装：")
            }
        })
    }
```
第一部分暂时先到这里，感觉这篇文章会很长
# 自己动手编写http框架（一）
#### 1.1.2 http所有情况
这里将剩下的所有情况都进行了汇总，具体如下：
1. 构造请求端，也就是单例`Client`，为了确保`header`，配置等
2. 请求中`url`带有`path`的情况
3. 正常请求带有`params`的情况
4. 需要自己设置请求头`header`，确保请求头统一并且不重复
5. 请求方法，这里以`get`为例子

下方为具体业务代码，还未做抽象，不急，一步一步来：

```
    /**
     * get请求测试
     * 请求超时
     *
     * 包含以下几方面：
     * params
     * header
     * path
     */
    private fun httpGetASync2() {
        /**
         * 读取超时时间
         */
        val READ_TIMEOUT = 100_000L

        /**
         * 写入超时时间
         */
        val WRITE_TIMEOUT = 60_000L

        /**
         * 链接时间
         */
        val CONNECT_TIMEOUT = 60_000L

        /**
         * 构造OkHttpClient
         */
        val client = RippleHttpClient.getInstance().newBuilder()
        client.readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
        client.writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
        client.connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)

        val clientResult = client.build()

        /**
         * 构造带有path的url
         * 这里采用buffer，主要因为它是线程安全的而且高效
         */
        val urlBuffer = StringBuffer()
        urlBuffer.append(GET_USER_BY_ID)
        urlBuffer.append("/")
        urlBuffer.append("myname")
        val httpUrl = urlBuffer.toString()
        val urlBuilder = httpUrl.toHttpUrlOrNull()?.newBuilder()

        /**
         * 构造header请求头
         * 这里采用ConcurrentHashMap，考虑到线程安全，防止重复添加相同的key，value
         */
        val hashMap: ConcurrentHashMap<String, Any> = ConcurrentHashMap()
        val headerBuilder = Headers.Builder()
        hashMap.forEach { (key: String, value: Any) ->
            headerBuilder.add(key, value.toString())
        }
        val urlHeaderResult = headerBuilder.build()

        /**
         * 构造params
         * id为int类型
         */
        urlBuilder?.addQueryParameter("id", "666")
        val urlResult = urlBuilder?.build()
        urlResult.toLogD()

        val request = Request.Builder()
            //header构建
            .headers(urlHeaderResult)
            //url构建
            .url(urlResult!!)
            //get请求
            .get()
        val requestResult = request.build()


        clientResult.newCall(requestResult).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.toString().toLogD()
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                result.toLogD()
            }
        })
    }
```

### 1.2 抽象http get/post请求
大体分为三部分，因为我一边写博客一遍写代码，难免过程会有错误，后面以最终实现的代码为最终结果，心中想要实现的效果大体如下，先来列一下要实现的目标
最终是要下面两个接口的具体实现
```
interface IHttpRequest {

    /**
     * get请求
     */
    fun <T> get(params: IRequestParams.IHttpRequestParams, callback: OnHttpResult<T>)

    /**
     * post请求
     */
    fun <T> post(params: IRequestParams.IHttpRequestParams, callback: OnHttpResult<T>)
}
```
#### 1.2.1 client基础配置
基础配置一般是不动的，但是为了防止特殊情况还是需要支持用户修改，这里大体罗列了一下基础配置，并且把通常不会改的放到了同一个接口中。
1. 读取超时时间
2. 写入超时时间
3. 链接时间
4. 请求头
5. 编码
6. 上传格式
7. `ssl`证书信息

开始定义接口：

```
    interface IHttpRequestParams : IRequestParams {

        var response: IHttpResponse

        /**
         * 请求方法
         * 例如：get,post等等
         */
        var method: HttpMethod


        /**
         * 请求头
         * 构建http的请求头
         */
        fun getHeader(): MutableMap<String, String>

        fun addHeader(key: String, value: String)

        fun getUrl(): String?

        fun setUrl(url: String)

        /**获取连接超时，带有默认设置*/
        fun getConnectTimeOut() = CONNECT_TIMEOUT

        /**获取读取超时，带有默认设置*/
        fun getReadTimeOut() = READ_TIMEOUT

        /**获取写入超时，带有默认设置*/
        fun getWriteTimeOut() = WRITE_TIMEOUT

        /**是否在请求过程中启用cookie*/
        fun isUseCookie(): Boolean = true

        /**获取编码信息，带有默认编码*/
        fun getCharset() = CHARSET

        /**是否使用json格式上传数据*/
        fun isUseJsonFormat() = false

        /**
         * 获取下一个任务取消状态
         */
        fun cancelNext(): Boolean?

        /**
         * 设置下一个请求是否取消
         */
        fun setCancelNext(cancelNext: Boolean)

        /**
         * 构建https签名
         */
        fun getSSLSocketFactory(): SSLSocketFactory? = null

        fun getX509TrustManager(): X509TrustManager? = null
    }
```
#### 1.2.2 下一个就是每个请求的参数配置
这个要区分一下`get`和`post`请求，但是又需要自己去进行筛选。
大体可以分为两种，`json`的话是将`param`序列化为`json`
1. `param`入参对应后台：`@RequestParam`
2. `path`入参以及：`@PathVariable`
下面是接口定义

```
interface IRequestParams {

    /**获取请求入参*/
    fun getParams(): MutableMap<String, Any>

    /**获取pathParam请求入参*/
    fun getPathParams(): MutableList<Any>

    /**添加请求参数*/
    fun addParam(key: String, value: Any)

    /**添加pathParam请求参数*/
    fun addPathParam(value: Any)

    fun containParam(key: String): Boolean

    /**初始化params*/
    fun init()

}
```
#### 1.2.3 初始化构建完成后开始解析
请求参数构建成功后，可以通过`okhttp`发送请求，然后进行相应的解析
内置一个抽象实现类，如果感觉不好可以自己再重写，实现代码有点多，而且知识点也涉及的比较多，暂时这里不贴代码了，后面会专门开一篇文章讲一下反射那个，方法的泛型是根据反射拿到的。

```
interface IHttpResponse {

    /**
     * http请求后服务器返回的结果
     * 正常来说不为空
     */
    var response: String

    /**
     * 一般情况下，后台返回的数据都会做数据封装，格式统一
     *
     * 如下：
     * 其中data中可能为list
     * {
     *      "code":200,
     *      "msg":"请求成功",
     *      "result":"成功",
     *      "data":"测试"
     * }
     *
     */
    var data: String?

    var state: Int

    /**
     * http状态码
     */
    /**提示信息*/
    var message: String

    /**
     * 解析结果是否为list
     */
    var isListResult: Boolean

    /**
     * 解析结果后的的class
     */
    var itemKClass: Class<*>

    /**
     * 序列化返回结果
     */
    var parser: IResponseParser

    /**当前这个response的params*/
    var requestParams: IRequestParams.IHttpRequestParams

    /**
     * 是否进行自定义解析
     * 如果返回false则是交给框架解析
     */
    fun handleParseData(response: String): Boolean

    /**
     * 解析传入泛型类型
     */
    fun parseItemParamType(paramEntity: Any?)

    fun isSuccess() = true
}
```
#### 1.2.5 再有就是数据解析了
这里面根据统一的协议写了个实现类，接口定义和实现类如下
接口：

```
interface IResponseParser {

    /**
     * 解析返回数据为json对象
     * 采用的是fastjson解析
     */
    fun <T> parseData(jsonObject: JSONObject, response: IHttpResponse): T
}
```
实现类：

```
open class HttpResponseParserImpl : AbsHttpResponseParser() {

    override fun <T> parseData(jsonObject: JSONObject, response: IHttpResponse): T {
        response.state = jsonObject.getInteger("status")
        response.message = jsonObject.getString("message")
        response.data = jsonObject.getString("data")

//        if (response.state != 0) {
//            throw HttpException(msg = response.message)
//        }

        val itemClazz = response.itemKClass
        return JSON.parseObject<T>(response.data, itemClazz)
    }
}
```
### 1.3 下面开始使用构建okhttp client
本来想贴代码的，但是有点多，写类名了，因为注释中写的很详细，然后这里说一下思路以及为什么这么做，`OkHttpClient`是采用单例直接用的一个，通过`params`每次去赋值，同理，`body`也是每次都去取`params`中的参数进行构建，这里就完成了`Call`对象的获取，之后就可以采用同步方式进行请求解析回调，这样方便之后链式调用的封装，所以返回的对象为`Call`
代码类：

```
//构建类
RippleHttpClient.getInstance()
//请求实现类
HttpTask.kt
```

至此，正常的请求就可以完成了，写的比较着急，也没有加`kotlin`顶层函数的封装，后续打算加上这些以及以下的这种链式以及同步的调用
PS：工作忙，写的博客有点敷衍，后面会把这里面需要注意的东西每个都拿出来专门做一个博客去讲，因为写一个框架类的东西不是一蹴而就而且还需要需要知识的积累，如果每个都讲的特别细估计得好多篇博客才能搞定，上面两篇文章算是把思路讲清了，后面的实现就看写的人如何去写了。

```
httpGet{
}.thenGet{
}.withPost{
}
```
