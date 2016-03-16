package me.peiwo.peiwo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiangxiaoqiang on 16/2/19.
 * description: used to marked the urls which have been opened in big picture
 */
public class ImageModelKeeper {

    private static ImageModelKeeper instance;

    private List<String> imageModelUrlList;

    private ImageModelKeeper() {
        imageModelUrlList = new ArrayList<String>();
    }

    public static ImageModelKeeper getInstance() {
        if (instance == null) {
            instance = new ImageModelKeeper();
        }
        return instance;
    }

    public List<String> getUrlList() {
        return imageModelUrlList;
    }

}
