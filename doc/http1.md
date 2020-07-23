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

