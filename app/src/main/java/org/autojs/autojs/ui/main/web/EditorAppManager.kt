package org.autojs.autojs.ui.main.web

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import org.autojs.autojs.ui.widget.SwipeRefreshWebView
import org.autojs.autojs.ui.widget.WebDataKt
import org.autojs.autojs.ui.widget.fillMaxSize

class EditorAppManager : Fragment() {

    val swipeRefreshWebView by lazy { SwipeRefreshWebView(requireContext()) }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return swipeRefreshWebView.apply {
            webView.loadUrl(WebDataKt.homepage)
            webView.settings.safeBrowsingEnabled = true;
            fillMaxSize()
        }
    }

}