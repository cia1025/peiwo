package me.peiwo.peiwo.net;

import android.support.annotation.NonNull;
import com.qiniu.android.storage.UploadManager;
import me.peiwo.peiwo.callback.UploadCallback;
import me.peiwo.peiwo.util.Md5Util;

import java.text.DecimalFormat;

/**
 * Created by fuhaidong on 15/11/20.
 */
public class PWUploader {

    public static final String K_UPLOAD_TYPE = "type";
    public static final String UPLOAD_TYPE_IMAGE = "1";
    public static final String UPLOAD_TYPE_VOICE = "2";
    public static final String UPLOAD_TYPE_AVATAR = "3";

    private UploadManager mUploadManager;

    private PWUploader() {
        mUploadManager = new UploadManager();
    }

    private static class SingletonHolder {
        public static final PWUploader INSTANCE = new PWUploader();
    }

    public static PWUploader getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 本地产生的图片唯一key
     *
     * @param uid
     * @return
     */
    public String getKey(int uid) {
        int number = (int) (Math.random() * 99999 + 1);
        // ss是由uid、时间戳、3位随机数组成的字符串
        String ss = String.format("%d_%s_%d", uid,
                new DecimalFormat("#.000").format(System.currentTimeMillis()), number);
        return String.format("%d_%s", uid,
                Md5Util.getMd5code(ss)); // key=uid+由ss生成的md5
    }

    public void add(@NonNull String filePath, String key, String token, UploadCallback callback) {
        mUploadManager.put(filePath, key, token, (rst_key, responseInfo, jsonObject) -> {
            if (responseInfo != null && responseInfo.isOK()) {
                if (callback != null) {
                    callback.onComplete(rst_key, responseInfo, jsonObject);
                }
            } else {
                if (callback != null) {
                    callback.onFailure(rst_key, responseInfo, jsonObject);
                }
            }
        }, null);
    }
}
