package me.peiwo.peiwo.util.group;

import android.text.TextUtils;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import me.peiwo.peiwo.BuildConfig;
import me.peiwo.peiwo.activity.ChatRedbagActivity;
import me.peiwo.peiwo.constans.GroupConstant;
import me.peiwo.peiwo.model.*;
import me.peiwo.peiwo.model.groupchat.*;
import me.peiwo.peiwo.util.TimeUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by fuhaidong on 15/12/11.
 */
public class RongMessageParse {
    public static List<GroupMessageBaseModel> parseList(List<Message> messageList) {
        List<GroupMessageBaseModel> models = new ArrayList<>();
        try {
            for (int i = messageList.size() - 1; i >= 0; i--) {
                Message message = messageList.get(i);
                MessageContent messageContent = message.getContent();
                if (messageContent instanceof GroupMessageModel) {
                    GroupMessageModel model = (GroupMessageModel) messageContent;
                    JSONObject object = new JSONObject(model.getBody());
                    if (BuildConfig.DEBUG) {
                        Log.i("rongs", "msg body == " + object.toString());
                    }
                    int message_type = object.optInt("dialog_type");
                    GroupMessageBaseModel baseModel;
                    switch (message_type) {
                        case GroupConstant.MessageType.TYPE_TEXT:
                            baseModel = JSON.parseObject(model.getBody(), GroupMessageTextModel.class);
                            break;
                        case GroupConstant.MessageType.TYPE_IMAGE:
                            baseModel = JSON.parseObject(model.getBody(), GroupMessageImageModel.class);
                            break;
                        case GroupConstant.MessageType.TYPE_GIF:
                            baseModel = JSON.parseObject(model.getBody(), GroupMessageGIFModel.class);
                            break;
                        case GroupConstant.MessageType.TYPE_RADBAG:
                            baseModel = JSON.parseObject(model.getBody(), GroupMessageRedBagModel.class);
                            break;
                        case GroupConstant.MessageType.TYPE_REPUTATION_RADBAG:
                            baseModel = JSON.parseObject(model.getBody(), GroupMessageRepuRedBagModel.class);
                            break;
                        case GroupConstant.MessageType.TYPE_REDBAG_TIP:
                            baseModel = JSON.parseObject(model.getBody(), GroupMessageRedBagTipModel.class);
                            break;
                        case GroupConstant.MessageType.TYPE_DECORATION:
                            baseModel = JSON.parseObject(model.getBody(), GroupMessageDecorationModel.class);
                            break;
                        case GroupConstant.MessageType.TYPE_REPUREDBAG_TIP:
                            baseModel = JSON.parseObject(model.getBody(), GroupMessageRepuRedBagTipModel.class);
                            break;
                        default:
                            baseModel = JSON.parseObject(model.getBody(), GroupMessageBaseModel.class);
                            break;
                    }
                    baseModel.message_id = message.getMessageId();
                    baseModel.direction = message.getMessageDirection() == Message.MessageDirection.SEND ? GroupConstant.Direction.SELF : GroupConstant.Direction.OTHER;
                    if (baseModel.direction == GroupConstant.Direction.SELF) {
                        if (message.getSentStatus() == Message.SentStatus.SENDING) {
                            baseModel.send_status = GroupConstant.SendStatus.SENDING;
                        } else if (message.getSentStatus() == Message.SentStatus.FAILED) {
                            baseModel.send_status = GroupConstant.SendStatus.ERROR;
                        } else {
                            baseModel.send_status = GroupConstant.SendStatus.SUCCESS;
                        }
                    } else {
                        //收到的消息置为成功
                        baseModel.send_status = GroupConstant.SendStatus.SUCCESS;
                    }
                    models.add(baseModel);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return models;
    }

    public static void reverseData(List<GroupMessageBaseModel> models) {
        if (models != null) {
            Collections.sort(models, (lhs, rhs) -> lhs.message_id - rhs.message_id);
        }
    }

    public static String encodeTextMessageBody(String content, PWUserModel self, TabfindGroupModel groupModel, GroupMemberModel groupMemberModel, List<GroupBaseUserModel> atUsersModel) {
        try {
            JSONObject object = prepareBaseObject(self, groupModel, groupMemberModel, content);
            object.put("dialog_type", GroupConstant.MessageType.TYPE_TEXT);
            JSONObject text = new JSONObject();
            text.put("content", content);
            if (atUsersModel != null && atUsersModel.size() > 0) {
                JSONArray atUsers = new JSONArray();
                for (GroupBaseUserModel userModel : atUsersModel) {
                    JSONObject o = new JSONObject();
                    o.put("uid", userModel.uid);
                    o.put("name", userModel.name);
                    atUsers.put(o);
                }
                text.put("atUsers", atUsers);
            }
            object.put("text", text);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeGIFMessageBody(GIFModel gifModel, PWUserModel self, TabfindGroupModel groupModel, GroupMemberModel groupMemberModel) {
        try {
            JSONObject object = prepareBaseObject(self, groupModel, groupMemberModel, "[动态表情]");
            object.put("dialog_type", GroupConstant.MessageType.TYPE_GIF);
            JSONObject gif = new JSONObject();
            gif.put("gif_name", gifModel.regular);
            gif.put("res_id", gifModel.res_id);
            gif.put("movie_res_id", gifModel.movie_res_id);
            object.put("gif", gif);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeImageMessageBody(ImageItem imageItem, PWUserModel self, TabfindGroupModel groupModel, GroupMemberModel groupMemberModel) {
        try {
            JSONObject object = prepareBaseObject(self, groupModel, groupMemberModel, "[图片]");
            object.put("dialog_type", GroupConstant.MessageType.TYPE_IMAGE);
            JSONObject image = new JSONObject();
            image.put("name", imageItem.imageKey);
            image.put("image_url", imageItem.sourcePath);
            image.put("thumbnail_url", TextUtils.isEmpty(imageItem.thumbnailPath) ? imageItem.sourcePath : imageItem.thumbnailPath);
            image.put("width", imageItem.width);
            image.put("height", imageItem.height);
            object.put("image", image);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeDecorationMessageBody(String content, PWUserModel self, TabfindGroupModel groupModel, GroupMemberModel groupMemberModel) {
        try {
            JSONObject object = prepareBaseObject(self, groupModel, groupMemberModel, content);
            object.put("dialog_type", GroupConstant.MessageType.TYPE_DECORATION);
            JSONObject text = new JSONObject();
            text.put("content", content);
            object.put("text", text);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeRedBagTipMessageBody(String content, PWUserModel self, TabfindGroupModel groupModel, GroupMemberModel groupMemberModel) {
        try {
            JSONObject object = prepareBaseObject(self, groupModel, groupMemberModel, content);
            object.put("dialog_type", GroupConstant.MessageType.TYPE_REDBAG_TIP);
            JSONObject text = new JSONObject();
            text.put("content", content);
            object.put("text", text);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeRepuRedBagTipMessageBody(String content, PWUserModel self, TabfindGroupModel groupModel, GroupMemberModel groupMemberModel) {
        try {
            JSONObject object = prepareBaseObject(self, groupModel, groupMemberModel, content);
            object.put("dialog_type", GroupConstant.MessageType.TYPE_REPUREDBAG_TIP);
            JSONObject text = new JSONObject();
            text.put("content", content);
            object.put("text", text);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeRedBagMessageBody(PacketIconModel packetIconModel, int bag_type, PWUserModel self, TabfindGroupModel groupModel, GroupMemberModel groupMemberModel) {
        try {
            JSONObject object = prepareBaseObject(self, groupModel, groupMemberModel, bag_type == ChatRedbagActivity.REDBAG_TYPE_PERSONAL ? "发来一个红包" : "发来一个群收益红包");
            object.put("dialog_type", GroupConstant.MessageType.TYPE_RADBAG);
            JSONObject packet = new JSONObject();
            //long parse compat iOS NSNumber
            packet.put("packet_id", Long.valueOf(packetIconModel.id));
            packet.put("icon_url", packetIconModel.send_icon);
            packet.put("msg", packetIconModel.msg);
            object.put("packet", packet);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeRepuRedBagMessageBody(PacketIconModel packetIconModel, PWUserModel self, TabfindGroupModel groupModel, GroupMemberModel groupMemberModel) {
        try {
            JSONObject object = prepareBaseObject(self, groupModel, groupMemberModel, "发来一个声望红包");
            object.put("dialog_type", GroupConstant.MessageType.TYPE_REPUTATION_RADBAG);
            JSONObject packet = new JSONObject();
            //long parse compat iOS NSNumber
            packet.put("packet_id", Long.valueOf(packetIconModel.id));
            packet.put("icon_url", packetIconModel.send_icon);
            packet.put("msg", packetIconModel.msg);
            object.put("packet", packet);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JSONObject prepareBaseObject(PWUserModel self, TabfindGroupModel groupModel, GroupMemberModel groupMemberModel, String description) throws JSONException {
        JSONObject object = new JSONObject();
        JSONObject user = new JSONObject();
        user.put("uid", self.uid);
        user.put("name", self.name);
        user.put("gender", self.gender);
        user.put("avatar_thumbnail", self.avatar_thumbnail);
        if (groupMemberModel != null) {
            user.put("member_type", groupMemberModel.member_type);
            user.put("nickname", groupMemberModel.nickname);
        } else {
            //没有获取到member info 的时候当成群成员处理
            user.put("member_type", GroupConstant.MemberType.MEMBER);
            user.put("nickname", self.name);
        }
        object.put("user", user);
        JSONObject group = new JSONObject();
        group.put("group_id", groupModel.group_id);
        group.put("group_name", groupModel.group_name);
        group.put("avatar", groupModel.avatar);
        object.put("group", group);
        object.put("update_time", TimeUtil.getDateTimeExact());
        JSONObject extra = new JSONObject();
        extra.put("content", "您的版本不支持此消息");
        extra.put("pw_description", description);
        object.put("extra", extra);
        return object;
    }

    @SuppressWarnings("unchecked")
    public static GroupMessageTextModel decodeTextObjectSelf(String body, int temp_message_id, Class<GroupMessageTextModel> clazz) {
        GroupMessageTextModel model = JSON.parseObject(body, clazz);
        model.send_status = GroupConstant.SendStatus.SENDING;
        model.direction = GroupConstant.Direction.SELF;
        //将对象hashcode作为暂时的messageid传入
        model.message_id = temp_message_id;
        return model;
    }

    @SuppressWarnings("unchecked")
    public static GroupMessageRedBagTipModel decodeRedBagTipObjectSelf(String body, int temp_message_id, Class<GroupMessageRedBagTipModel> clazz) {
        GroupMessageRedBagTipModel model = JSON.parseObject(body, clazz);
        model.send_status = GroupConstant.SendStatus.SUCCESS;
        //model.direction = GroupConstant.Direction.SELF;
        //将对象hashcode作为暂时的messageid传入
        model.message_id = temp_message_id;
        return model;
    }

    @SuppressWarnings("unchecked")
    public static GroupMessageRepuRedBagTipModel decodeRepuRedBagTipObjectSelf(String body, int temp_message_id, Class<GroupMessageRepuRedBagTipModel> clazz) {
        GroupMessageRepuRedBagTipModel model = JSON.parseObject(body, clazz);
        model.send_status = GroupConstant.SendStatus.SUCCESS;
        //model.direction = GroupConstant.Direction.SELF;
        //将对象hashcode作为暂时的messageid传入
        model.message_id = temp_message_id;
        return model;
    }

    @SuppressWarnings("unchecked")
    public static GroupMessageDecorationModel decodeDecorationObjectSelf(String body, int temp_message_id, Class<GroupMessageDecorationModel> clazz) {
        GroupMessageDecorationModel model = JSON.parseObject(body, clazz);
        model.send_status = GroupConstant.SendStatus.SUCCESS;
        //model.direction = GroupConstant.Direction.SELF;
        //将对象hashcode作为暂时的messageid传入
        model.message_id = temp_message_id;
        return model;
    }

    @SuppressWarnings("unchecked")
    public static GroupMessageRedBagModel decodeRedbagObjectSelf(String body, int temp_message_id, Class<GroupMessageRedBagModel> clazz) {
        GroupMessageRedBagModel model = JSON.parseObject(body, clazz);
        model.send_status = GroupConstant.SendStatus.SENDING;
        model.direction = GroupConstant.Direction.SELF;
        //将对象hashcode作为暂时的messageid传入
        model.message_id = temp_message_id;
        return model;
    }

    @SuppressWarnings("unchecked")
    public static GroupMessageRepuRedBagModel decodeRepuRedbagObjectSelf(String body, int temp_message_id, Class<GroupMessageRepuRedBagModel> clazz) {
        GroupMessageRepuRedBagModel model = JSON.parseObject(body, clazz);
        model.send_status = GroupConstant.SendStatus.SENDING;
        model.direction = GroupConstant.Direction.SELF;
        //将对象hashcode作为暂时的messageid传入
        model.message_id = temp_message_id;
        return model;
    }

    @SuppressWarnings("unchecked")
    public static GroupMessageGIFModel decodeGIFObjectSelf(String body, int temp_message_id, Class<GroupMessageGIFModel> clazz) {
        GroupMessageGIFModel model = JSON.parseObject(body, clazz);
        model.send_status = GroupConstant.SendStatus.SENDING;
        model.direction = GroupConstant.Direction.SELF;
        //将对象hashcode作为暂时的messageid传入
        model.message_id = temp_message_id;
        return model;
    }

    @SuppressWarnings("unchecked")
    public static GroupMessageImageModel decodeImageObjectSelf(String body, int temp_message_id, Class<GroupMessageImageModel> clazz) {
        GroupMessageImageModel model = JSON.parseObject(body, clazz);
        model.send_status = GroupConstant.SendStatus.SENDING;
        model.direction = GroupConstant.Direction.SELF;
        //将对象hashcode作为暂时的messageid传入
        model.message_id = temp_message_id;
        return model;
    }

    public static String parseRecendMessage(GroupMessageBaseModel model) {
        return JSON.toJSONString(model);
    }

    public static GroupMessageBaseModel parseReceiveMessage(Message message, int integer) {
        try {
            MessageContent messageContent = message.getContent();
            if (messageContent instanceof GroupMessageModel) {
                String body = ((GroupMessageModel) messageContent).getBody();
                GroupMessageBaseModel model;
                JSONObject object = new JSONObject(body);
                int dialog_type = object.optInt("dialog_type", GroupConstant.MessageType.TYPE_UNKNOWN);
                switch (dialog_type) {
                    case GroupConstant.MessageType.TYPE_TEXT:
                        model = JSON.parseObject(body, GroupMessageTextModel.class);
                        break;
                    case GroupConstant.MessageType.TYPE_IMAGE:
                        model = JSON.parseObject(body, GroupMessageImageModel.class);
                        break;
                    case GroupConstant.MessageType.TYPE_GIF:
                        model = JSON.parseObject(body, GroupMessageGIFModel.class);
                        break;
                    case GroupConstant.MessageType.TYPE_RADBAG:
                        model = JSON.parseObject(body, GroupMessageRedBagModel.class);
                        break;
                    case GroupConstant.MessageType.TYPE_REPUTATION_RADBAG:
                        model = JSON.parseObject(body, GroupMessageRepuRedBagModel.class);
                        break;
                    case GroupConstant.MessageType.TYPE_REDBAG_TIP:
                        model = JSON.parseObject(body, GroupMessageRedBagTipModel.class);
                        break;
                    case GroupConstant.MessageType.TYPE_REPUREDBAG_TIP:
                        model = JSON.parseObject(body, GroupMessageRepuRedBagTipModel.class);
                        break;
                    case GroupConstant.MessageType.TYPE_DECORATION:
                        model = JSON.parseObject(body, GroupMessageDecorationModel.class);
                        break;
                    default:
                        model = JSON.parseObject(body, GroupMessageBaseModel.class);
                        break;
                }
                model.message_id = message.getMessageId();
                model.direction = GroupConstant.Direction.OTHER;
                //收到的消息置为成功
                model.send_status = GroupConstant.SendStatus.SUCCESS;
                return model;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String encodeRegularCommandMessageData(int tuid, int selfUid) {
        try {
            JSONObject object = new JSONObject();
            JSONArray target_ids = new JSONArray();
            JSONObject target_id = new JSONObject();
            target_id.put("uid", tuid);
            target_ids.put(target_id);
            object.put("target_ids", target_ids);
            JSONObject user = new JSONObject();
            user.put("uid", selfUid);
            object.put("user", user);
            object.put("expired_at", System.currentTimeMillis() + 10 * 60 * 1000);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String encodeUpdateGroupCommandMessageData(String group_name, String avatar, int selfUid) {
        try {
            JSONObject object = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("uid", selfUid);
            object.put("user", user);
            object.put("expired_at", System.currentTimeMillis() + 10 * 60 * 1000);
            JSONObject group = new JSONObject();
            group.put("group_name", group_name);
            group.put("avatar", avatar);
            object.put("group", group);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
