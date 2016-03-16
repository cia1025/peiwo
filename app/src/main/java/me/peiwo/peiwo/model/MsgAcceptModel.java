package me.peiwo.peiwo.model;

import android.database.Cursor;
import android.text.TextUtils;
import me.peiwo.peiwo.adapter.MsgAcceptAdapter;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.im.MessageModel;
import me.peiwo.peiwo.model.groupchat.PacketIconModel;
import me.peiwo.peiwo.util.CustomLog;
import org.json.JSONException;
import org.json.JSONObject;

public class MsgAcceptModel extends PPBaseModel {
    private static final long serialVersionUID = 1L;
    public long id;
    public String content;
    public int dialog_id;
    public String update_time;
    public int view_type;
    public int dialog_type;
    public String details;

    public String displayTime;

    public int send_status = MessageModel.SEND_STATUS_DEFAULT;
    public int read_status = 0;

    public int error_code = 0;

    public String local_path;
    public int imageHeight;
    public int imageWidth;
    public String thumbnail_url;
    public String image_url;

    public String redbag_title;
    public String redbag_content;
    public String redbag_extra;
    public String icon_url;
    public String url;
    public String icon_name;
    public String feed_id;
    public VoiceModel voice;
    public PacketIconModel packetIconModel;

    public MsgAcceptModel() {

    }

    public MsgAcceptModel(PWUserModel mUser, Cursor c) {
        id = c.getInt(c.getColumnIndex(PWDBConfig.DialogsTable.ID));
        String _content = c.getString(c.getColumnIndex(PWDBConfig.DialogsTable.CONTENT));
        dialog_id = c.getInt(c.getColumnIndex(PWDBConfig.DialogsTable.DIALOG_ID));
        update_time = c.getString(c.getColumnIndex(PWDBConfig.DialogsTable.UPDATE_TIME));
        dialog_type = c.getInt(c.getColumnIndex(PWDBConfig.DialogsTable.DIALOG_TYPE));
        CustomLog.d("dialog_type is : " + dialog_type);
        details = c.getString(c.getColumnIndex(PWDBConfig.DialogsTable.DETAILS));
        send_status = c.getInt(c.getColumnIndex(PWDBConfig.DialogsTable.SEND_STATUS));
        read_status = c.getInt(c.getColumnIndex(PWDBConfig.DialogsTable.READ_STATUS));
        error_code = c.getInt(c.getColumnIndex(PWDBConfig.DialogsTable.ERROR_CODE));

        int type = c.getInt(c.getColumnIndex(PWDBConfig.DialogsTable.TYPE));

        if (type == 0) {
            view_type = MsgAcceptAdapter.VIEW_TYPE_ME;
            content = resetContent(dialog_type, details, _content, true);
            if (dialog_type == MessageModel.DIALOG_TYPE_PACKAGE) {
                view_type = MsgAcceptAdapter.VIEW_TYPE_ME_FEED;
            } else if (dialog_type == MessageModel.DIALOG_TYPE_ATTENTION) {
                view_type = MsgAcceptAdapter.VIEW_TYPE_ATTENTION_PROMPT;
            } else if (dialog_type == MessageModel.DIALOG_TYPE_IMAGE_MESSAGE) {
                view_type = MsgAcceptAdapter.VIEW_TYPE_ME_IMG;
            } else if (dialog_type == MessageModel.DIALOG_TYPE_IM_PACKET) {
                view_type = MsgAcceptAdapter.VIEW_TYPE_ME_PACKET;
            } else if (!TextUtils.isEmpty(content) && content.startsWith("{")
                    && content.endsWith("}")) {
                view_type = MsgAcceptAdapter.VIEW_TYPE_ME_GIF;
//                for (String key : ExpressionUtil.getInstance().gifFaceMap.keySet()) {
//                    if (content.contains(key)) {
//                        view_type = MsgAcceptAdapter.VIEW_TYPE_ME_GIF;
//                        break;
//                    }
//                }
            }

        } else {
            view_type = MsgAcceptAdapter.VIEW_TYPE_OTHER;
            if (MessageModel.dialogTypeSet.contains(dialog_type)) {
                content = resetContent(dialog_type, details, _content, false);
            } else {
                content = _content;
                CustomLog.d("dialog_type is : " + dialog_type + ", \t MsgAcceptModel. content is : " + content);
            }

            if (dialog_type == MessageModel.DIALOG_TYPE_PACKAGE) {
                view_type = MsgAcceptAdapter.VIEW_TYPE_OTHER_FEED;
            } else if (dialog_type == MessageModel.DIALOG_TYPE_ATTENTION) {
                view_type = MsgAcceptAdapter.VIEW_TYPE_ATTENTION_PROMPT;
            } else if (dialog_type == MessageModel.DIALOG_TYPE_IMAGE_MESSAGE) {
                view_type = MsgAcceptAdapter.VIEW_TYPE_OTHER_IMG;
            } else if (dialog_type == MessageModel.DIALOG_TYPE_VOICE_MESSAGE) {
                view_type = MsgAcceptAdapter.VIEW_TYPE_OTHER_VOICE;
            } else if (dialog_type == MessageModel.DIALOG_TYPE_IM_PACKET) {
                view_type = MsgAcceptAdapter.VIEW_TYPE_OTHER_PACKET;
            } else if (!TextUtils.isEmpty(content) && content.startsWith("{")
                    && content.endsWith("}")) {
                view_type = MsgAcceptAdapter.VIEW_TYPE_OTHER_GIF;
//                for (String key : ExpressionUtil.getInstance().gifFaceMap.keySet()) {
//                    if (content.contains(key)) {
//                        view_type = MsgAcceptAdapter.VIEW_TYPE_OTHER_GIF;
//                        break;
//                    }
//                }
            }
        }
    }

    private String resetContent(int dialog_type, String details,
                                String content, boolean isme) {
        if (details == null)
            return "";
        if (dialog_type == MessageModel.DIALOG_TYPE_TIP)
            return content;
        CustomLog.d("resetContent, details is : " + details);
        try {
            if (!TextUtils.isEmpty(details) && details.length() > 2
                    && details.contains("{") && details.contains("}")) {
                JSONObject o = new JSONObject(details);
                content = getJsonValue(o, "msg");
                redbag_title = getJsonValue(o, "title");
                redbag_content = getJsonValue(o, "msg");
                redbag_extra = getJsonValue(o, "extra");
                icon_url = getJsonValue(o, "icon_url");
                url = getJsonValue(o, "url");
                icon_name = getJsonValue(o, "icon_name");
                feed_id = getJsonValue(o, "feed_id");
                voice = new VoiceModel();
                voice.voice_url = getJsonValue(o, "voice_url");
                voice.voice_key = getJsonValue(o, "key");
                voice.length = getJsonInt(o, "length");

                if (o.has("im_image")) {
                    JSONObject im_image = new JSONObject(getJsonValue(o, "im_image"));
                    local_path = getJsonValue(im_image, "local_path");
                    imageHeight = getJsonInt(im_image, "height");
                    imageWidth = getJsonInt(im_image, "width");
                    thumbnail_url = getJsonValue(im_image, "thumbnail_url");
                    image_url = getJsonValue(im_image, "image_url");
                }

                if(o.has("im_packet")) {
                    JSONObject im_packet = new JSONObject(getJsonValue(o, "im_packet"));
                    packetIconModel = new PacketIconModel();
                    packetIconModel.id = getJsonValue(im_packet, "packet_id");
                    packetIconModel.send_icon = getJsonValue(im_packet, "icon_url");
                    packetIconModel.msg = getJsonValue(im_packet, "msg");
                }
            }

            if (dialog_type == MessageModel.DIALOG_TYPE_CALL_HISTORY && !TextUtils.isEmpty(content) && content.contains("]")) {
                int patIndex = content.lastIndexOf("]") + 1;
                String pattern = content.substring(0, patIndex);
                if ("[取消呼叫]".equals(pattern)) {
                    content = (isme ? "已取消" : "未接听");
                } else if ("[通话]".equals(pattern)) {
                    content = content.substring(patIndex, content.length());
                } else if ("[对方忙]".equals(pattern)) {
                    content = (isme ? "对方忙" : "未接听");
                } else if ("[无人接听]".equals(pattern)) {
                    content = (isme ? "已取消" : "未接听");
                } else if ("[拒绝通话]".equals(pattern)) {
                    content = (isme ? "对方已拒绝" : "未接听");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return content;
    }

}
