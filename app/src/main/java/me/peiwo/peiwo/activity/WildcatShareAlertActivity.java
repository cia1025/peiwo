package me.peiwo.peiwo.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler.Response;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.util.*;
import me.peiwo.peiwo.widget.WildcatShareAlertDialog;
import me.peiwo.peiwo.widget.WildcatShareAlertDialog.OnWildcalShareClickListener;

import java.io.File;
import java.util.Locale;

public class WildcatShareAlertActivity extends BaseActivity implements
        OnWildcalShareClickListener, Response {

    public static final int REQUESTCODE_SHARE_WX = 1001;
    public static final int REQUESTCODE_SHARE_WEIBO = 1002;
    public static final int REQUESTCODE_SHARE_QQZONE = 1003;
    public static final int SHARE_C_ANDROID = 1;
    public static final int SHARE_S_WEIXIN = 1;
    public static final int SHARE_S_WEIBO = 2;
    public static final int SHARE_S_QQZONE = 3;

    public static final int SHARE_S_QQZONE_SUCCESS = 2003;
    public static final int SHARE_S_QQZONE_FAILE = 2004;

    private Activity mContext = this;

    private IWeiboShareAPI mWeiboShareAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECALLRECORD);
        String data = SharedPreferencesUtil.getStringExtra(mContext,
                "wildcat_share_content" + UserManager.getUid(mContext), "");

        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constans.WEIBO_APP_KEY);

        WildcatShareAlertDialog shareDialog = new WildcatShareAlertDialog(
                mContext);
        shareDialog.show();
        shareDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                finish();
            }
        });
        shareDialog.setOnShareClickListener(this);
        shareDialog.setTextViewData(data);
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

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onShareClickListener(int which, Bitmap bitmap, String content) {
        String shareFilePath = ImageUtil.saveBitmap(bitmap, String.format(Locale.getDefault(), "%d%s", System.currentTimeMillis(), ".jpg"));
        switch (which) {
            case WildcatShareAlertDialog.SHARE_WHICH_FRIENDS_CYCLE: // 分享到限时圈
                try {
                    PWUtils.ShareToWXTimeline(mContext, new File(shareFilePath),
                            String.format("%s%s", content, "http://t.cn/RhH2S0K"),
                            REQUESTCODE_SHARE_WX);
                } catch (Exception e) {
                    showToast(mContext, "您没安装微信");
                }
                break;
            case WildcatShareAlertDialog.SHARE_WHICH_WEIBO: // 分享到微博
                if (!mWeiboShareAPI.isWeiboAppInstalled()) {
                    showToast(this, "您没有安装微博");
                    return;
                }
                PWUtils.shareToWeibo(this, mWeiboShareAPI, String.format("%s%s?c=%d&s=%d",
                        content, Constans.SHARE_APP_NEW_URL, SHARE_C_ANDROID,
                        SHARE_S_WEIBO), bitmap);
                break;
            case WildcatShareAlertDialog.SHARE_WHICH_QQ_ZONE: // 分享到QQ空间
                String url = String
                        .format("%s?c=%d&s=%d", Constans.SHARE_APP_NEW_URL,
                                SHARE_C_ANDROID, SHARE_S_QQZONE);
                if (null != shareFilePath) {
                    PWUtils.shareToQQZone(mContext, new File(shareFilePath), "陪我",
                            content, url, null);
                }
                break;
            case WildcatShareAlertDialog.SHARE_WHICH_CLOSE: {
                finish();
            }
            break;
        }
    }
}
