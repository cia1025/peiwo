package me.peiwo.peiwo.activity;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.*;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler.Response;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import me.peiwo.peiwo.BuildConfig;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.PWActionConfig;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.im.MessageUtil;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.*;
import me.peiwo.peiwo.util.UserManager;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;

public class RedBagActivity extends BaseActivity implements Response {

    private WebView wv_redbag;
    private String package_id;
    public static final String PACKID = "package_id";
    private BroadcastReceiver wxReceiver;
    private IWXAPI mWXApi;

    public static final int SHARE_S_QQZONE_SUCCESS = 2003;
    public static final int SHARE_S_QQZONE_FAILE = 2004;
    private IWeiboShareAPI mWeiboShareAPI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mWXApi = WXAPIFactory.createWXAPI(this, Constans.WX_APP_ID, true);
        mWXApi.registerApp(Constans.WX_APP_ID);
        initWebView();
        wxReceiver = new WXReceiver();
        registerReceiver(wxReceiver, new IntentFilter(PWActionConfig.ACTION_WXSHARE_SUCCESS));
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constans.WEIBO_APP_KEY);

        showAnimLoading();

        //IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        //registerReceiver(receiver, filter);
    }

//    BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//            if (myDownloadReference == reference) {
//                dismissAnimLoading();
//                showToast(context, "图片已保存在本地相册，可以用来分享咯");
//            }
//        }
//    };

    private long myDownloadReference = 0;

    private void initWebView() {
        wv_redbag = (WebView) findViewById(R.id.webView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        wv_redbag.getSettings().setJavaScriptEnabled(true);

        String url = getIntent().getStringExtra("url");
        // String url ="http://www.peiwo.cn/test/li/h5/package.html?package_id=1116&uid=190509";
        wv_redbag.loadUrl(url);
        wv_redbag.setWebChromeClient(new MyWebChromeClient());
        wv_redbag.setWebViewClient(new MyWebViewClient());
        Uri parse = Uri.parse(url);
        package_id = parse.getQueryParameter("package_id");
        setTitleBar("");
    }

    @Override
    public void finish() {
        if (wv_redbag.canGoBack()) {
            wv_redbag.goBack();
        } else {
            //unregisterReceiver(receiver);
            super.finish();
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().equals("user")) {
                Uri parse = Uri.parse(url);
                String uid = parse.getQueryParameter("uid");
                startUserinfoActivity(uid);
                return true;
            }
            if (Uri.parse(url).getHost().equals("share")) {
                Uri parse = Uri.parse(url);
                String content = parse.getQueryParameter("content");
                String shareUrl = parse.getQueryParameter("url");
                String shareType = parse.getQueryParameter("app");//action=star
                String action = parse.getQueryParameter("action");
                int drawID = R.drawable.hb_share;
                if (!TextUtils.isEmpty(action) && action.equals("star")) {
                    drawID = R.drawable.star_share;
                }
                if ("0".equals(shareType)) {
                    // 微信朋友圈
                    shareToWX(content, shareUrl, drawID);
                } else if ("1".equals(shareType)) {
                    // 微博
                    PWUtils.shareToWeibo(RedBagActivity.this, mWeiboShareAPI, content, BitmapFactory.decodeResource(getResources(), drawID));
                } else if ("2".equals(shareType)) {
                    // QQ空间
                    PWUtils.shareToQQZone(RedBagActivity.this, null, "陪我",
                            content, shareUrl, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    switch (msg.what) {
                                        case SHARE_S_QQZONE_SUCCESS:
                                            // 分享成功
                                            showToast(getApplicationContext(), "分享成功");
                                            break;
                                        case SHARE_S_QQZONE_FAILE:
                                            // 分享失败
                                            showToast(getApplicationContext(), "分享失败");
                                            break;
                                    }
                                    super.handleMessage(msg);
                                }
                            });
                }
                return true;
            }
            if (url.endsWith(".jpg") || url.endsWith(".png")) {
                UmengStatisticsAgent.onEvent(RedBagActivity.this, UMEventIDS.UMECLICKSHARECONSTELLATION);
                //showAnimLoading("", false, false);
                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(url);
                Request request = new Request(uri);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "ShareConstellation.jpg");
                myDownloadReference = downloadManager.enqueue(request);
                return true;
            }
            view.loadUrl(url);
            //showAnimLoading("", false, false);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            //Log.i("urlerror", "onReceivedError");
            dismissAnimLoading();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            dismissAnimLoading();
            super.onPageFinished(view, url);
            PWUserModel mUser = UserManager.getPWUser(RedBagActivity.this);
            if (mUser.gender == 2) {
                getMoney();
            }
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mWeiboShareAPI.handleWeiboResponse(intent, this);// 当前应用唤起微博分享后，返回当前应用
    }

    @Override
    public void onResponse(BaseResponse baseResp) {
        // 接收微客户端博请求的数据。
        switch (baseResp.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                break;
        }
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

    private void getMoney() {
        if (TextUtils.isEmpty(package_id))
            return;
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(PACKID, package_id));
        ApiRequestWrapper.openAPIPOST(RedBagActivity.this, params, AsynHttpClient.API_RECEIVE, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                String money = data.optString("money");
                UserManager.updateMoney(RedBagActivity.this, money);
                int id = getIntent().getIntExtra("id", 0);
                String redbag_extra = getIntent().getStringExtra("redbag_extra");
                MessageUtil.updateRedBagStatus(id);
                MessageUtil.updateMessageContent(1, redbag_extra);
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }


    public void startUserinfoActivity(String tuid) {
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra(UserInfoActivity.TARGET_UID, Integer.valueOf(tuid));
        startActivity(intent);
    }

    class WXReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 微信分享成功
            if (UserManager.getPWUser(RedBagActivity.this).gender == 2) {
                getBonusMoney();
            }
        }
    }

    private void getBonusMoney() {
        if (TextUtils.isEmpty(package_id))
            return;
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(PACKID, package_id));
        ApiRequestWrapper.openAPIPOST(getApplicationContext(), params,
                AsynHttpClient.API_RECEIVEBONUS, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        String money = data.optString("money");
                        UserManager.updateMoney(RedBagActivity.this, money);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                wv_redbag.loadUrl("javascript:showBox()");
                            }
                        });
                    }

                    @Override
                    public void onError(int error, Object ret) {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (mWXApi != null) {
            mWXApi.unregisterApp();
        }
        if (wxReceiver != null) {
            unregisterReceiver(wxReceiver);
            wxReceiver = null;
        }
        super.onDestroy();
    }

    private void shareToWX(String content, String url, int drawID) {
        boolean isWXAppInstalled = mWXApi.isWXAppInstalled();
        boolean isWXAppSupportAPI = mWXApi.isWXAppSupportAPI();
        if ((!isWXAppInstalled) || (!isWXAppSupportAPI)) {
            downLoadWX();
            return;
        }
        WXMediaMessage message = new WXMediaMessage();
        message.title = content;
        message.mediaObject = new WXWebpageObject(url);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), drawID);
        message.thumbData = ImageUtil.bmpToByteArray(bmp);
        if (!bmp.isRecycled()) {
            bmp.recycle();
        }
        message.description = Constans.WX_SHARE_DES;
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = SendMessageToWX.Req.WXSceneTimeline;
        req.transaction = buildTransaction("img");
        req.message = message;
        mWXApi.sendReq(req);
    }

    private void downLoadWX() {
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        localBuilder.setTitle("提示");
        localBuilder.setMessage("您未安装最新版微信,是否现在就下载更新呢?");
        localBuilder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                paramDialogInterface.cancel();
            }
        });
        localBuilder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent localIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://weixin.qq.com/m"));
                RedBagActivity.this.startActivity(localIntent);
            }
        });
        localBuilder.create().show();
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private void setTitleBar(String title) {
        TitleUtil.setTitleBar(this, title, v -> {
            onBackPressed();
        }, null); //
    }


}
