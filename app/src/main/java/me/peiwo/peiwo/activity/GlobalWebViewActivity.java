package me.peiwo.peiwo.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import butterknife.Bind;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import me.peiwo.peiwo.BuildConfig;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.util.Base64;
import me.peiwo.peiwo.util.ImageUtil;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.TitleUtil;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局的webview页面
 * Created by fuhaidong on 14-8-22.
 */
public class GlobalWebViewActivity extends BaseActivity {
    public static String URL = "url";
    public static String TITLE = "title";
    private WebView webView;
    private IWXAPI mWXApi;
    @Bind(R.id.v_share_panel)
    View v_share_panel;
    private String title;
    private String text;
    private String icon;
    private String share_url;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mWXApi = WXAPIFactory.createWXAPI(this, Constans.WX_APP_ID, true);
        mWXApi.registerApp(Constans.WX_APP_ID);
        init();
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        if (mWXApi != null) {
            mWXApi.unregisterApp();
        }
        super.onDestroy();
    }

    private void init() {
        String title = getIntent().getStringExtra(TITLE);
        setTitleBar(title);
        webView = (WebView) findViewById(R.id.webView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        WebSettings settings = webView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());
        String mUrl = getLinkUrl();
        if (!TextUtils.isEmpty(mUrl)) {
            webView.loadUrl(mUrl);
            showAnimLoading();
        }
    }

    private String getLinkUrl() {
        Intent intent = getIntent();
        String resultUrl = intent.getStringExtra(URL);
        if (TextUtils.isEmpty(resultUrl)) {
            Uri uri = intent.getData();
            if (uri != null) {
                String result = uri.getPath();
                if (result != null && result.length() > 0) {
                    resultUrl = result.substring(1);
                }
            }

        }
        return resultUrl;
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (handleUrlIfNeed(url)) {
                return true;
            }
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            dismissAnimLoading();
            super.onPageFinished(view, url);
        }
    }

    private boolean handleUrlIfNeed(String url) {
        //peiwoappwebviewshare://title=5o&text=5oi&icon=aHR0&url=aHR
        try {
            if (url.startsWith("peiwoappwebviewshare") || url.startsWith("peiWoAppWebviewShare")) {
                String rst = url.substring(url.lastIndexOf("/") + 1);
                String[] array = rst.split("&");
                Map<String, String> params = new HashMap<>();
                for (String temp : array) {
                    int index = temp.indexOf("=");
                    params.put(temp.substring(0, index), temp.substring(index + 1));
                }
                title = params.get("title");
                text = params.get("text");
                icon = params.get("icon");
                share_url = params.get("url");
                if (title == null || text == null || icon == null || share_url == null) {
                    return true;
                }
                Base64.Decoder decoder = Base64.getDecoder();
                title = new String(decoder.decode(title), "UTF-8");
                text = new String(decoder.decode(text), "UTF-8");
                icon = new String(decoder.decode(icon), "UTF-8");
                share_url = new String(decoder.decode(share_url), "UTF-8");
                shareWX();
                return true;
            } else if (url.startsWith("peiwoappwebviewwallet")) {
                startActivity(new Intent(this, WantMakeMoney.class));
                return true;
            } else if (url.startsWith("tel")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    private void shareWX() {
        if (v_share_panel.getVisibility() == View.VISIBLE) {
            return;
        }
        v_share_panel.setVisibility(View.VISIBLE);
        v_share_panel.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                v_share_panel.getViewTreeObserver().removeOnPreDrawListener(this);
                v_share_panel.setTranslationY(v_share_panel.getHeight());
                v_share_panel.animate().translationY(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v_share_panel.setVisibility(View.VISIBLE);
                    }
                });
                return false;
            }
        });
    }

    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.v_share_wxtimeline:
                closeSharePanel();
                prepareShare(1);
                break;
            case R.id.v_share_wx:
                closeSharePanel();
                prepareShare(0);
                break;

            case R.id.v_share_close:
                closeSharePanel();
                break;
        }
    }

    private void prepareShare(int flag) {
        ImageLoader.getInstance().loadImage(icon, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                showAnimLoading("", false, false, false);
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                dismissAnimLoading();
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                dismissAnimLoading();
                Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, PWUtils.THUMB_SIZE, PWUtils.THUMB_SIZE, true);
                byte[] bytes = ImageUtil.bmpToByteArray(thumbBmp);
                if (bytes.length < 32 * 1000) {
                    PWUtils.shareToWX(title, flag, share_url, text, bytes, mWXApi);
                }
                thumbBmp.recycle();
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                dismissAnimLoading();
            }
        });
    }

    private void closeSharePanel() {
        v_share_panel.animate().translationY(v_share_panel.getHeight()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v_share_panel.setVisibility(View.GONE);
            }
        });
    }

    private void setTitleBar(String title) {

        TitleUtil.setTitleBar(this, title, v -> {
            finish();
        }, null);
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            setTitleBar(title);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }
}