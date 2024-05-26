package org.autojs.autojs.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.webkit.JsResult
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import org.autojs.autojs.theme.widget.ThemeColorSwipeRefreshLayout
import org.autojs.autojs.tool.DatabaseHelper
import org.autojs.autojs.ui.main.scripts.MyJavaScriptInterface
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType



class SwipeRefreshWebView : ThemeColorSwipeRefreshLayout {

    val webView = WebView(context)
    val jsInterface = MyJavaScriptInterface(context)
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    init {
        isEnabled = false
        webView.apply {
            fillMaxSize()
            setup()
        }
        addView(webView)
        webView.addJavascriptInterface(jsInterface, "Android")
        jsInterface.webView = webView
        setOnRefreshListener {
            // 执行网页重新加载操作
            webView.reload()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun WebView.setup() {
        settings.apply {
            javaScriptEnabled = true  //设置支持Javascript交互
            javaScriptCanOpenWindowsAutomatically = true //支持通过JS打开新窗口
            allowFileAccess = true //设置可以访问文件
            allowFileAccessFromFileURLs = true;
            allowUniversalAccessFromFileURLs = true;
            allowContentAccess = true;
            defaultTextEncodingName = "utf-8"//设置编码格式
            setSupportMultipleWindows(false)
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            setSupportZoom(true) //支持缩放，默认为true。是下面那个的前提。
            builtInZoomControls = true //设置内置的缩放控件。若为false，则该WebView不可缩放
            displayZoomControls = false //设置原生的缩放控件，启用时被leakcanary检测到内存泄露
            useWideViewPort = true //让WebView读取网页设置的viewport，pc版网页
            loadWithOverviewMode = false
            loadsImagesAutomatically = true //设置自动加载图片
            blockNetworkImage = false
            blockNetworkLoads = false;
            setNeedInitialFocus(true);
            saveFormData = true;
            cacheMode = WebSettings.LOAD_NO_CACHE //使用缓存
//            setAppCacheEnabled(false);
            domStorageEnabled = true
            databaseEnabled = true   //开启 database storage API 功能
            pluginState = WebSettings.PluginState.ON
            if (Build.VERSION.SDK_INT >= 26) {
                safeBrowsingEnabled = false
            }
            mediaPlaybackRequiresUserGesture = false;
            // 5.0以上允许加载http和https混合的页面(5.0以下默认允许，5.0+默认禁止)
            mixedContentMode =
                WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;
        }
        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // 页面开始加载时的处理
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isRefreshing = false
            }
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // 处理页面跳转
                return true
            }
        }
    }
}
