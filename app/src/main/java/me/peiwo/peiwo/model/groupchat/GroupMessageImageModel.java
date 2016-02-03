package me.peiwo.peiwo.model.groupchat;

/**
 * Created by fuhaidong on 15/12/10.
 */
public class GroupMessageImageModel extends GroupMessageBaseModel {
    public GroupImage image;

    public class GroupImage {
        public String name;
        public String image_url;
        public String thumbnail_url;
        public int width;
        public int height;
    }
}
