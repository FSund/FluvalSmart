package com.inledco.fluvalsmart.web;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.inledco.fluvalsmart.R;
import com.inledco.fluvalsmart.base.BaseActivity;

import java.util.HashMap;
import java.util.Map;

public class WebActivity extends BaseActivity {

    private final String GOOGLE_DOC_URL = "https://docs.google.com/gview?embedded=true&url=";

    private boolean mAllowOpenInBrowser;
    private String mUrl;
    private Toolbar web_toolbar;
    private ProgressBar web_loading;
    private WebView web_show;
    private FrameLayout web_fs_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webpage);

        initView();
        initData();
        initEvent();
    }

    @Override
    protected void onDestroy() {
        if (web_show != null) {
            web_show.destroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mAllowOpenInBrowser) {
            getMenuInflater().inflate(R.menu.menu_web, menu);
            MenuItem menu_web = menu.findItem(R.id.menu_web_browser);
            menu_web.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Uri uri = Uri.parse(mUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return false;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                break;
        }
    }

    @Override
    protected void initView() {
        web_toolbar = findViewById(R.id.web_toolbar);
        web_loading = findViewById(R.id.web_loading);
        web_show = findViewById(R.id.web_show);
        web_fs_container = findViewById(R.id.web_fs_container);

        setSupportActionBar(web_toolbar);
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        mAllowOpenInBrowser = intent.getBooleanExtra("allow_open_in_browser", false);
        mUrl = intent.getStringExtra("url");
        showWebView(web_show, mUrl);
    }

    @Override
    protected void initEvent() {
        web_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        web_show.stopLoading();
        if (web_show.canGoBack()) {
            web_show.goBack();
            return;
        }
        super.onBackPressed();
    }

    private void showWebView(final WebView web, String url) {
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webSettings.setDisplayZoomControls(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccessFromFileURLs(true);

        web.setWebChromeClient(new MyWebChromeClient() {
            /**
             * 获取到链接标题
             * @param view
             * @param title
             */
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                web_toolbar.setTitle(title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                web_loading.setProgress(newProgress);
                if (newProgress >= 100) {
                    web_loading.setVisibility(View.GONE);
                } else {
                    web_loading.setVisibility(View.VISIBLE);
                }
            }
        });

        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String newurl = request.getUrl().toString();
                    if (TextUtils.isEmpty(newurl) == false && newurl.endsWith(".pdf")) {
                        web.loadUrl(GOOGLE_DOC_URL + newurl);
                    }
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept-Encoding", "gzip,deflate,br");
        web.loadUrl(url, headers);
    }

    public class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);

            web_toolbar.setVisibility(View.GONE);
            web_show.setVisibility(View.GONE);
            web_fs_container.setVisibility(View.VISIBLE);
            web_fs_container.addView(view);
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();

            web_toolbar.setVisibility(View.VISIBLE);
            web_show.setVisibility(View.VISIBLE);
            web_fs_container.setVisibility(View.GONE);
            web_fs_container.removeAllViews();
        }
    }
}
