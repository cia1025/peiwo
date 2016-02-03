package me.peiwo.peiwo.model.groupchat;

import java.util.List;

/**
 * Created by fuhaidong on 16/1/19.
 */
public class QNImageToken {
    public List<QNToken> data;

    public class QNToken {
        public String token;
        public String key;
        public String thumbnail_url;
        public String url;
    }
}
