package com.breadwallet.presenter.activities.settings;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.util.ActivityUTILS;
import com.breadwallet.presenter.activities.util.BRActivity;
import com.breadwallet.tools.animation.BRAnimator;
import com.breadwallet.tools.util.Utils;
import com.platform.HTTPServer;

import static com.breadwallet.R.string.error;


public class WebViewActivity extends BRActivity {
    private static final String TAG = WebViewActivity.class.getName();
    WebView webView;
    String theUrl;
    public static boolean appVisible = false;
    private static WebViewActivity app;

    public static WebViewActivity getApp() {
        return app;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        if (webView != null) webView.destroy();
        webView = (WebView) findViewById(R.id.web_view);
        webView.setWebChromeClient(new BRWebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.e(TAG, "shouldOverrideUrlLoading: " + request.getUrl());
                Log.e(TAG, "shouldOverrideUrlLoading: " + request.getMethod());
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.e(TAG, "onPageStarted: " + url);
                super.onPageStarted(view, url, favicon);
            }
        });

        theUrl = getIntent().getStringExtra("url");
        setupServerMode(theUrl);
        String articleId = getIntent().getStringExtra("articleId");
        if (Utils.isNullOrEmpty(theUrl)) throw new IllegalArgumentException("No url extra!");

        WebSettings webSettings = webView.getSettings();

        if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);

        if (articleId != null && !articleId.isEmpty())
            theUrl = theUrl + "/" + articleId;

        Log.e(TAG, "onCreate: theUrl: " + theUrl);
        webView.loadUrl(theUrl);
        if (articleId != null && !articleId.isEmpty())
            navigate(articleId);
    }

    private void navigate(String to) {
        String js = String.format("window.location = \'%s\';", to);
        webView.evaluateJavascript(js, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.e(TAG, "onReceiveValue: " + value);
            }
        });
    }

    private void setupServerMode(String url) {
        if (url.equalsIgnoreCase(HTTPServer.URL_BUY)) {
            HTTPServer.mode = HTTPServer.ServerMode.BUY;
        } else if (url.equalsIgnoreCase(HTTPServer.URL_SUPPORT)) {
            HTTPServer.mode = HTTPServer.ServerMode.SUPPORT;
        } else if (url.equalsIgnoreCase(HTTPServer.URL_EA)) {
            HTTPServer.mode = HTTPServer.ServerMode.EA;
        } else {
            throw new RuntimeException("unknown url: " + url);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    private class BRWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.e(TAG, "onConsoleMessage: consoleMessage: " + consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.e(TAG, "onJsAlert: " + message + ", url: " + url);
            return super.onJsAlert(view, url, message, result);
        }
    }

    @Override
    public void onBackPressed() {
//        BRAnimator.startBreadActivity(this, false);
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_up, R.anim.exit_to_bottom);
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
        ActivityUTILS.init(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }


}
