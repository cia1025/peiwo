package me.peiwo.peiwo.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.PWActionConfig;

/**
 * Created by Dong Fuhai on 2014-07-22 16:42.
 *
 * @modify:
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI wxApi;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wxApi = WXAPIFactory.createWXAPI(this, Constans.WX_APP_ID, true);
        wxApi.handleIntent(getIntent(), this);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        wxApi.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        switch (baseReq.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                break;
            default:
                break;
        }

        finish();
    }

    @Override
    public void onResp(BaseResp baseResp) {
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                String code = "0";
                if (baseResp instanceof SendMessageToWX.Resp) {
                    code = String.valueOf(SendMessageToWX.Resp.ErrCode.ERR_OK);
                } else if (baseResp instanceof SendAuth.Resp) {
                    code = ((SendAuth.Resp) baseResp).code;
                }
                Intent intent = new Intent(PWActionConfig.ACTION_WXSHARE_SUCCESS);
                intent.putExtra("wxcode", code);
                sendBroadcast(intent);
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                Intent e_intent = new Intent(PWActionConfig.ACTION_WXSHARE_SUCCESS);
                e_intent.putExtra("wxcode", "");
                sendBroadcast(e_intent);
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                break;
            default:
                break;
        }
        finish();
    }
}
