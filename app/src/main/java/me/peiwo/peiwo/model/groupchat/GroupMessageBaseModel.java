package me.peiwo.peiwo.model.groupchat;

/**
 * Created by fuhaidong on 15/12/10.
 */
public class GroupMessageBaseModel {
    //消息id
    public int message_id;
    //消息类型
    public int dialog_type;
    //方向
    public int direction;

    public int send_status;

    public String update_time;
    public boolean isshowtime;

    public GroupBaseUserModel user;
    public GroupBaseGroupModel group;
    public GroupBaseExtraModel extra;


}
