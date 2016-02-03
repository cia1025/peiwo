package me.peiwo.peiwo.model.groupchat;

/**
 * Created by fuhaidong on 15/12/10.
 */
public class GroupMessageRepuRedBagModel extends GroupMessageBaseModel {
    public Packet packet;

    public class Packet {
        public String packet_id;
        public String icon_url;
        public String msg;
    }
}
