package me.peiwo.peiwo.callback;

import com.qiniu.android.http.ResponseInfo;
import org.json.JSONObject;

/**
 * Created by fuhaidong on 15/11/23.
 */
public interface UploadCallback {
    void onComplete(String key, ResponseInfo responseInfo, JSONObject jsonObject);

    void onFailure(String key, ResponseInfo responseInfo, JSONObject jsonObject);
}
