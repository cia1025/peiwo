package me.peiwo.peiwo.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import me.peiwo.peiwo.activity.ChargeActivity;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.util.CustomLog;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";

    private IWXAPI mAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAPI = WXAPIFactory.createWXAPI(this, Constans.WX_APP_ID, true);
        mAPI.registerApp(Constans.WX_APP_ID);
        mAPI.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mAPI.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {

    }

    /**
     * int ERR_OK = 0;
     * int ERR_COMM = -1;
     * int ERR_USER_CANCEL = -2;
     * int ERR_SENT_FAILED = -3;
     * int ERR_AUTH_DENIED = -4;
     * int ERR_UNSUPPORT = -5;
     */
    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
//            MMAlert.showToast(this, resp.errCode + "===" + resp.errStr);
//            MMAlert.showToast(this, resp.openId);
            CustomLog.d("wechat pay response : " + resp.errCode + ", str : " + resp.errStr);
            switch (resp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
//                    Intent intent = new Intent(MMActionConfig.ACTION_WXPAY_SUCCESS);
//                    AppController.getInstance()getInstance.getBus().post(intent);
                    EventBus.getDefault().post(new Intent(ChargeActivity.ACTION_WECHAT_PAYMENT_DONE));
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL:
//                    AppController.getInstance().getBus().post(new Intent(MMActionConfig.ACTION_WXPAY_CANCLE));
                    Toast.makeText(this, "已取消", Toast.LENGTH_SHORT).show();
                    break;
                case BaseResp.ErrCode.ERR_UNSUPPORT:
//                    AppController.getInstance().getBus().post(new Intent(MMActionConfig.ACTION_WXPAY_UNSUPPORT));
//                    MMAlert.showToast(this, "您的微信版本不支持微信支付");
                    Toast.makeText(this, "您的微信版本不支持微信支付", Toast.LENGTH_SHORT).show();
                    break;
                default:
//                    Intent i = new Intent(MMActionConfig.ACTION_WXPAY_FAILD);
//                    AppController.getInstance().getBus().post(i);
                    EventBus.getDefault().post(new Intent(ChargeActivity.ACTION_WECHAT_PAYMENT_DONE));
                    break;
            }
            finish();
        }
    }


}