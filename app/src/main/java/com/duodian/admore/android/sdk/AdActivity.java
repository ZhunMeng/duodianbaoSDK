package com.duodian.admore.android.sdk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.duodian.admore.android.sdk.adview.splash.SplashAdManager;
import com.duodian.admore.android.sdk.config.AdmoreSdkConfig;
import com.duodian.admore.android.sdk.utils.LogUtil;

public class AdActivity extends AppCompatActivity {
    private boolean fromSplash;
    private ProgressBar progressBar;
    private String url;
    private WebView webView;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admoresdk_activity_ad);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
            getSupportActionBar().hide();
        }
        initViews();
    }

    private void initViews() {
        Intent intent = getIntent();
        if (intent != null) {
            this.url = intent.getStringExtra(AdmoreSdkConfig.URL);
            this.fromSplash = intent.getBooleanExtra(AdmoreSdkConfig.FROM_SPLASH, false);
        }
        this.webView = (WebView) findViewById(R.id.webView);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.webView.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                AdActivity.this.progressBar.setVisibility(View.VISIBLE);
            }

            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                AdActivity.this.progressBar.setVisibility(View.INVISIBLE);
            }

            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.toString());
                LogUtil.e(AdmoreSdkConfig.URL, request.toString());
                return true;
            }

            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }

            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
            }

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        this.webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                AdActivity.this.progressBar.setProgress(newProgress);
            }

            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return true;
            }

            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return true;
            }

            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return true;
            }

            public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }

        });
        this.webView.requestFocusFromTouch();
        WebSettings webSettings = this.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setSupportMultipleWindows(false);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setAllowFileAccess(true);
        webSettings.setNeedInitialFocus(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setPluginState(PluginState.ON);
        webSettings.setDefaultTextEncodingName("UTF-8");
        if (VERSION.SDK_INT >= 19) {
            webSettings.setLoadsImagesAutomatically(true);
        } else {
            webSettings.setLoadsImagesAutomatically(false);
        }
        if (VERSION.SDK_INT >= 21) {
            this.webView.getSettings().setMixedContentMode(2);
        }
        if (!TextUtils.isEmpty(this.url)) {
            this.webView.postDelayed(new Runnable() {
                public void run() {
                    AdActivity.this.webView.loadUrl(AdActivity.this.url);
                }
            }, 0);
        }
    }

    public void onBackPressed() {
        if (this.webView.canGoBack()) {
            this.webView.goBack();
        } else if (this.fromSplash) {
            gotoTargetClass();
        } else {
            finish();
        }
    }

    private void gotoTargetClass() {
        if (SplashAdManager.getInstance().getTargetClass() != null) {
            startActivity(new Intent(this, SplashAdManager.getInstance().getTargetClass()));
        }
        finish();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (this.webView == null || !this.webView.canGoBack()) {
                    finish();
                } else {
                    this.webView.goBack();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
