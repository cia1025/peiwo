package me.peiwo.peiwo.callback;

import java.io.IOException;

/**
 * Created by fuhaidong on 15/10/28.
 */
public interface DownloadCallback {
    void onComplete(String path);

    void onFailure(String path, IOException e);
}
