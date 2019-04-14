package com.example.webapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.webviewlocalserver.WebViewLocalServer;

import java.util.HashMap;

public class MainActivity extends Activity {

    private WebView mWebView;
    private WebViewLocalServer assetServer;
    private Proxy proxy;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = findViewById(R.id.main_webview);

        WebSettings webSettings = mWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(false);
        webSettings.setAllowUniversalAccessFromFileURLs(false);

        mWebView.addJavascriptInterface(new WebAppInterface(this), "__ANDROID__");

        assetServer = new WebViewLocalServer(getApplicationContext());
        WebViewLocalServer.AssetHostingDetails details =
                assetServer.hostAssets("www");

        proxy = new Proxy(details.getHttpPrefix().buildUpon().build(),
                new HashMap<String, String>() {{
                    put(Proxy.ALL, "/index.html");
                    put(Proxy.JS, "/js");
                    put(Proxy.CSS, "/css");
                    put(Proxy.FAVICON, "/favicon.ico");
                }});

        mWebView.setWebViewClient(new WebClient());

        mWebView.loadUrl(proxy.root());
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    class WebClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return assetServer.shouldInterceptRequest(proxy.get(request.getUrl()));
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (proxy.isExternal(Uri.parse(url))) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                view.getContext().startActivity(intent);
                return true;
            }
            return false;
        }
    }
}