package me.peiwo.peiwo.model.groupchat;

/**
 * Created by fuhaidong on 15/12/11.
 */
public class GroupBaseUserModel {
    public String uid;
    public String name;
    public String nickname;
    public int gender;
    public String avatar_thumbnail;
    public String member_type;

    public GroupBaseUserModel(String uid, String name, String nickname, int gender, String avatar_thumbnail, String member_type) {
        this.uid = uid;
        this.name = name;
        this.nickname = nickname;
        this.gender = gender;
        this.avatar_thumbnail = avatar_thumbnail;
        this.member_type = member_type;
    }

    public GroupBaseUserModel() {
    }
}
