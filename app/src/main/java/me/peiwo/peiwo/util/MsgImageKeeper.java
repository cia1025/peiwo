package me.peiwo.peiwo.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiangxiaoqiang on 16/3/7.
 */
public class MsgImageKeeper {

    private List<String> mMsgImgList;

    private MsgImageKeeper() {
        mMsgImgList = new ArrayList<>();
    }

    private static class MsgImageModelHolder {
        private static MsgImageKeeper INSTANCE = new MsgImageKeeper();
    }

    public static MsgImageKeeper getInstance() {
        return MsgImageModelHolder.INSTANCE;
    }

    public void add(String imgUrl) {
        mMsgImgList.add(imgUrl);
    }

    public void addAll(List<String> imgUrls) {
        mMsgImgList.addAll(imgUrls);
    }

    public void remove(String imgUrl) {
        if (mMsgImgList.contains(imgUrl)) {
            mMsgImgList.remove(imgUrl);
        }
    }

    public void clear() {
        mMsgImgList.clear();
    }

    public List<String> getImgList() {
        return mMsgImgList;
    }

    public boolean contains(String imgUrl) {
        return mMsgImgList.contains(imgUrl);
    }

}
