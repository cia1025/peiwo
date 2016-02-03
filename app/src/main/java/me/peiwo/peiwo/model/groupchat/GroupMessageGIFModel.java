package me.peiwo.peiwo.model.groupchat;

/**
 * Created by fuhaidong on 15/12/10.
 */
public class GroupMessageGIFModel extends GroupMessageBaseModel {
    public GIF gif;

    public class GIF {
        public String gif_name;
        public int res_id;
        public int movie_res_id;
    }
}
