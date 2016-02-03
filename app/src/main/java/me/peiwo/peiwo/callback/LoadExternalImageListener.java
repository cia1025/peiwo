package me.peiwo.peiwo.callback;

import me.peiwo.peiwo.model.ImageItem;

import java.util.List;

/**
 * Created by fuhaidong on 15/12/14.
 */
public interface LoadExternalImageListener {
    void loadComplete(List<ImageItem> data);

    void loadError(Throwable e);
}
