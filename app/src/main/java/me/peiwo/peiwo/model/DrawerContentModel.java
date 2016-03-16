package me.peiwo.peiwo.model;

/**
 * Created by fuhaidong on 15/10/19.
 */
public class DrawerContentModel {
    //这个属性暂时没用了
    public boolean isselected = false;
    public String drawer_lable;
    public int badge_num;
    public int view_type;


    public String uname;
    public String avatar_thumbnail;
    public String pwnum;
    public String voice_var;

    public String uid;
    public String session_data;
    //just for lazy voice
    private boolean hasVoice = true;

    public DrawerContentModel(String uname, String avatar_thumbnail, String pwnum, int view_type) {
        this.view_type = view_type;
        this.uname = uname;
        this.avatar_thumbnail = avatar_thumbnail;
        this.pwnum = pwnum;
    }

    public DrawerContentModel(boolean isselected, String drawer_lable, int badge_num, int view_type) {
        this.isselected = isselected;
        this.drawer_lable = drawer_lable;
        this.badge_num = badge_num;
        this.view_type = view_type;
    }

    public boolean isHasVoice() {
        return hasVoice;
    }

    public void setHasVoice(boolean hasVoice) {
        if ("懒人招呼".equals(this.drawer_lable))
            this.hasVoice = hasVoice;
    }
}
