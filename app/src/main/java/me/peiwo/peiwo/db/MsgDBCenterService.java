package me.peiwo.peiwo.db;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.SparseArray;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.QueryObservable;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.MainActivity;
import me.peiwo.peiwo.activity.MsgAcceptedMsgActivity;
import me.peiwo.peiwo.activity.SayHelloActivity;
import me.peiwo.peiwo.activity.TabMsgFragment;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.GroupConstant;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.im.MessageModel;
import me.peiwo.peiwo.im.MessageUtil;
import me.peiwo.peiwo.model.MsgNotifiModel;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.model.groupchat.GroupBaseUserModel;
import me.peiwo.peiwo.model.groupchat.GroupMessageBaseModel;
import me.peiwo.peiwo.model.groupchat.GroupMessageTextModel;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.SharedPreferencesUtil;
import me.peiwo.peiwo.util.UserManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Subscription;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fuhaidong on 14-8-21.
 * 操作消息中心的db类
 */
public class MsgDBCenterService {

    private Map<String, String> atUserMapping;
    private Map<String, Integer> noDisturbMapping;

    public static final int WHAT_INSERT_REMARK_COMMPLETE = 10020;
    public static final int WHAT_INSERT_REMARK_ERROR = 10021;

    private static final String AT_EXTRA = "有人@我：";

    private static final int CONTACT_STATE_DELETE = 1; //好友被删除的标记

    private PWDBHelper mDBHelper;
    private Context mContext;
    public ExecutorService mExecutorService = null;
    private String selfUid;
    private HashMap<Integer, Integer> mNotifyMap = new HashMap<>();

    private MsgDBCenterService() {
        mContext = PeiwoApp.getApplication();
        mDBHelper = PWDBHelper.getInstance(PeiwoApp.getApplication());
        mExecutorService = Executors.newFixedThreadPool(1);
        atUserMapping = Collections.synchronizedMap(new HashMap<>());
        noDisturbMapping = Collections.synchronizedMap(new HashMap<>());
    }

    public void setUpAtusersAndNodisturb() {
        if (TextUtils.isEmpty(selfUid) || "0".equals(selfUid)) {
            selfUid = String.valueOf(UserManager.getPWUser(mContext).uid);
            fetchAtUserWithDB();
            fetchNoDisturbMappingWithDB();
        }
    }


    private void fetchNoDisturbMappingWithDB() {
        BriteDatabase briteDatabase = BriteDBHelperHolder.getInstance().getBriteDatabase(mContext);
        if (briteDatabase == null) return;
        String sql = String.format("select * from %s", PWDBConfig.TB_PW_NO_DISTURB);
        QueryObservable queryObservable = briteDatabase.createQuery(PWDBConfig.TB_PW_NO_DISTURB, sql);
        Subscription subscription = queryObservable.subscribe(query -> {
            Cursor cursor = query.run();
            noDisturbMapping.clear();
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    noDisturbMapping.put(cursor.getString(cursor.getColumnIndex("target_id")), cursor.getInt(cursor.getColumnIndex("nodisturb")));
                }
                cursor.close();
            }
        });
        subscription.unsubscribe();
    }

    public void updateNodisturbWithGroup(String group_id, boolean isOpen) {
        //ContentValues values = new ContentValues();
        //values.put("target_id", group_id);
        //values.put("nodisturb", isopen ? "1" : "0");

        BriteDatabase briteDatabase = BriteDBHelperHolder.getInstance().getBriteDatabase(mContext);
        if (briteDatabase == null) return;
//        String sql = String.format("select target_id from %s where target_id = ?", PWDBConfig.TB_PW_NO_DISTURB);
//        QueryObservable queryObservable = briteDatabase.createQuery(PWDBConfig.TB_PW_NO_DISTURB, sql, group_id);
//        final AtomicBoolean insert = new AtomicBoolean(true);
//        Subscription subscription = queryObservable.subscribe(query -> {
//            Cursor c = query.run();
//            if (c != null) {
//                insert.set(!c.moveToFirst());
//                c.close();
//            }
//        });
//        subscription.unsubscribe();
//        if (insert.get()) {
//            briteDatabase.insert(PWDBConfig.TB_PW_NO_DISTURB, values);
//        } else {
//            String where = "target_id = ?";
//            briteDatabase.update(PWDBConfig.TB_PW_NO_DISTURB, values, where, group_id);
//        }
        if (isOpen) {
            String sql = String.format("delete from %s where target_id = ?", PWDBConfig.TB_PW_NO_DISTURB);
            briteDatabase.execute(sql, group_id);
        } else {
            String sql = String.format("insert or replace into %s (target_id, nodisturb) values (?, ?)", PWDBConfig.TB_PW_NO_DISTURB);
            briteDatabase.execute(sql, group_id, 1);
        }
        fetchNoDisturbMappingWithDB();
    }

    private void fetchAtUserWithDB() {
        BriteDatabase briteDatabase = BriteDBHelperHolder.getInstance().getBriteDatabase(mContext);
        if (briteDatabase == null) return;
        String sql = String.format("select * from %s", PWDBConfig.TB_PW_AT_USER);
        QueryObservable queryObservable = briteDatabase.createQuery(PWDBConfig.TB_PW_AT_USER, sql);
        queryObservable.subscribe(query -> {
            Cursor cursor = query.run();
            atUserMapping.clear();
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    atUserMapping.put(cursor.getString(cursor.getColumnIndex("target_id")), cursor.getString(cursor.getColumnIndex("extra")));
                }
                cursor.close();
            }
        });
    }

    public Map<String, String> getAtUserMapping() {
        return this.atUserMapping;
    }

    public Map<String, Integer> getNoDisturbMapping() {
        return this.noDisturbMapping;
    }

    public void addAtUser(GroupMessageTextModel textModel) {
        String group_id = null;
        if (textModel.text.atUsers != null && textModel.text.atUsers.size() > 0) {
            for (GroupBaseUserModel atUserModel : textModel.text.atUsers) {
                if (selfUid.equals(atUserModel.uid)) {
                    group_id = textModel.group.group_id;
                    break;
                }
            }
        }
        if (group_id != null && !atUserMapping.containsKey(group_id)) {
            BriteDatabase briteDatabase = BriteDBHelperHolder.getInstance().getBriteDatabase(mContext);
            if (briteDatabase == null) return;
            ContentValues values = new ContentValues();
            values.put("target_id", group_id);
            values.put("extra", AT_EXTRA);

            String sql = String.format("select target_id from %s where target_id = ?", PWDBConfig.TB_PW_AT_USER);
            QueryObservable queryObservable = briteDatabase.createQuery(PWDBConfig.TB_PW_AT_USER, sql, group_id);
            final AtomicBoolean insert = new AtomicBoolean(true);
            Subscription subscription = queryObservable.subscribe(query -> {
                Cursor c = query.run();
                if (c != null) {
                    insert.set(!c.moveToFirst());
                    c.close();
                }
            });
            subscription.unsubscribe();
            if (insert.get()) {
                briteDatabase.insert(PWDBConfig.TB_PW_AT_USER, values);
            }
        }
    }

    public void removeAtUser(String target_id) {
        if (atUserMapping.containsKey(target_id)) {
            atUserMapping.remove(target_id);
            BriteDatabase briteDatabase = BriteDBHelperHolder.getInstance().getBriteDatabase(mContext);
            if (briteDatabase == null) return;
            briteDatabase.delete(PWDBConfig.TB_PW_AT_USER, "target_id = ?", target_id);
        }
    }

    private static MsgDBCenterService instance = null;

    public static MsgDBCenterService getInstance() {
        if (instance == null) {
            instance = new MsgDBCenterService();
        }
        return instance;
    }

    public ArrayList<MsgNotifiModel> msgNotifiList = new ArrayList<MsgNotifiModel>();


    public int getSayHelloListCount() {
        Cursor c = null;
        int sayHelloItem = 0;
        try {
            String selection = PWDBConfig.MessagesTable.INSIDE + " = ?";
            String[] selectionArgs = new String[1];
            selectionArgs[0] = "1";
            c = mContext.getContentResolver().query(PWDBConfig.MessagesTable.CONTENT_URI, null, selection, selectionArgs, null);
            if (c != null) {
                sayHelloItem = c.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
        }
        return sayHelloItem;
    }

    private void callBackOnUiThread(Message message, Handler mHandler) {
        if (mHandler != null)
            mHandler.sendMessage(message);
    }

    //根据用户id查询最后一条语音申请，招呼盒子用
    public String getDetailsByUidDesc(int uid) {
        Cursor c = null;
        try {
            String selection = PWDBConfig.DialogsTable.UID + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(uid)};
            String sortOrder = PWDBConfig.DialogsTable.UPDATE_TIME + " desc limit 1";
            c = mContext.getContentResolver().query(PWDBConfig.DialogsTable.CONTENT_URI, null, selection, selectionArgs, sortOrder);
            if (c != null && c.moveToFirst()) {
                return c.getString(c.getColumnIndex(PWDBConfig.DialogsTable.DETAILS));
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (c != null)
                c.close();
        }
        return null;
    }


    /**
     * 删除一条联系人
     *
     * @param tuid
     */
    public void deletePWContact(int tuid) {
        try {
            String where = PWDBConfig.ContactsTable.UID + " = ?";
            String[] selectionArgs = new String[1];
            selectionArgs[0] = String.valueOf(tuid);
            mContext.getContentResolver().delete(PWDBConfig.ContactsTable.CONTENT_URI, where, selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void insertDialogsWithGroupchat(GroupMessageBaseModel baseModel) {
        if (baseModel != null) {
            String description;
            switch (baseModel.dialog_type) {
                case GroupConstant.MessageType.TYPE_DECORATION:
                case GroupConstant.MessageType.TYPE_REDBAG_TIP:
                case GroupConstant.MessageType.TYPE_REPUREDBAG_TIP:
                    description = baseModel.extra.pw_description == null ? "" : baseModel.extra.pw_description;
                    break;
                default:
                    description = baseModel.extra.pw_description == null ? "" : baseModel.user.nickname + "：" + baseModel.extra.pw_description;
                    break;
            }
            insertCreateGroupMsg(baseModel.group.group_id, baseModel.group.group_name, baseModel.group.avatar, description, baseModel.message_id, baseModel.group.group_id, baseModel.dialog_type, baseModel.update_time);
        }
    }

    /**
     * 插入dialog与message
     *
     * @param
     */
    public long insertDialogsWithMessages(Context context, MessageModel model, PWUserModel userModel) {
        long dbId = -1;
        if (model == null) return dbId;
        try {
            ContentValues dialogValues = new ContentValues();
            dialogValues.put(PWDBConfig.DialogsTable.CONTENT, model.content);
            dialogValues.put(PWDBConfig.DialogsTable.DIALOG_ID, model.dialog_id);
            dialogValues.put(PWDBConfig.DialogsTable.UPDATE_TIME, model.update_time);
            dialogValues.put(PWDBConfig.DialogsTable.MSG_ID, model.msg_id);
            dialogValues.put(PWDBConfig.DialogsTable.UID, model.uid);
            dialogValues.put(PWDBConfig.DialogsTable.DIALOG_TYPE, model.dialog_type);
            dialogValues.put(PWDBConfig.DialogsTable.DETAILS, model.details);
            dialogValues.put(PWDBConfig.DialogsTable.SEND_STATUS, model.send_status);
            dialogValues.put(PWDBConfig.DialogsTable.TYPE, model.type);
            Uri uri = mContext.getContentResolver().insert(PWDBConfig.DialogsTable.CONTENT_URI, dialogValues);
            dbId = ContentUris.parseId(uri);

            if (model.dialog_type == MessageModel.DIALOG_TYPE_PACKAGE)
                return 0;
            String selection = PWDBConfig.MessagesTable.UID + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(model.uid)};
            ContentValues messageValues = new ContentValues();
            messageValues.put(PWDBConfig.MessagesTable.UPDATE_TIME, model.update_time);
            messageValues.put(PWDBConfig.MessagesTable.CONTENT, model.content);
            messageValues.put(PWDBConfig.MessagesTable.TYPE, 0);
            messageValues.put(PWDBConfig.MessagesTable.INSIDE, 0);
            CustomLog.d("insertDialogsWithMessages. func1");
            int count = mContext.getContentResolver().update(PWDBConfig.MessagesTable.CONTENT_URI, messageValues, selection, selectionArgs);
            if (count == 0) {
                messageValues.put(PWDBConfig.MessagesTable.MSG_ID, model.msg_id);
                messageValues.put(PWDBConfig.MessagesTable.UID, userModel.uid);
                JSONObject userObject = new JSONObject();
                userObject.put("uid", userModel.uid);
                userObject.put("birthday", userModel.birthday);
                userObject.put("avatar_thumbnail", userModel.avatar_thumbnail);
//                userObject.put("slogan",  msgModel.user.s);
                userObject.put("price", userModel.price);
                userObject.put("name", userModel.name);
                userObject.put("province", userModel.province);
                userObject.put("gender", userModel.gender);
                userObject.put("avatar", userModel.avatar);
                userObject.put("city", userModel.city);
                messageValues.put(PWDBConfig.MessagesTable.USER, userObject.toString());
                messageValues.put(PWDBConfig.MessagesTable.MSG_TYPE, 2);
                messageValues.put(PWDBConfig.MessagesTable.MESSGAE_COUNT, 1);
                messageValues.put(PWDBConfig.MessagesTable.UNREAD_COUNT, 0);
                if (model.is_hide == 1) {
                    messageValues.put(PWDBConfig.MessagesTable.IS_HIDE, 1);
                }
                mContext.getContentResolver().insert(PWDBConfig.MessagesTable.CONTENT_URI, messageValues);
            } else {
                reSetSayhelloData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbId;
    }

    /**
     * 打招呼盒子
     */
    public void insertSingleMessage() {
        try {
            CustomLog.d("insertSingleMessage. func");
            ContentValues values = new ContentValues();
            values.put(PWDBConfig.MessagesTable.MSG_ID, DfineAction.MSG_ID_SAYHELLO);
            values.put(PWDBConfig.MessagesTable.UID, DfineAction.MSG_ID_SAYHELLO);
            values.put(PWDBConfig.MessagesTable.MSG_TYPE, TabMsgFragment.USER_MESSAGE);
            values.put(PWDBConfig.MessagesTable.INSIDE, 0);
            mContext.getContentResolver().insert(PWDBConfig.MessagesTable.CONTENT_URI, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 数据库中插入接收消息(离线消息，以及长连接发送过来的消息),目前为短消息以及系统消息
     */
    public void insertDialogsWithMessages(final JSONArray array, boolean isgroup, int group_dialog_type) {
        CustomLog.d("insertDialogsWithMessages. array is : " + array);
        if (array == null || array.length() == 0) return;
        mExecutorService.execute(() -> {
            try {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject msgObject = array.getJSONObject(i);
                    JSONArray d_array = msgObject.getJSONArray("dialogs");
                    JSONObject user = msgObject.optJSONObject("user");
                    int dialog_count = d_array.length();
                    String userUid = user.optString("uid");
                    String msgid = msgObject.optString("msg_id");

                    //CustomLog.d("insertDialogsWithMessages, uid is : " + userUid + ", dialog detail is : " + getDialogDetailsByUid(userUid));
                    CustomLog.d("insertDialogsWithMessages, msg_id is : " + msgid);

//                        int msgInside = msgObject.optInt("inside");
//                        boolean isSayHello = (msgInside == 1);
                    boolean isSayHello = false;
                    int msgInside = 0;
                    int msg_type = msgObject.optInt("msg_type");

                    String name = (user != null && user.has("name")) ? user.optString("name") : "";

                    try {
                        ContentValues uidMsgIdValues = new ContentValues();
                        uidMsgIdValues.put(PWDBConfig.UidMsgId.UID, userUid);
                        uidMsgIdValues.put(PWDBConfig.UidMsgId.MSG_ID, msgid);
                        mContext.getContentResolver().insert(PWDBConfig.UidMsgId.CONTENT_URI, uidMsgIdValues);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String selection = PWDBConfig.MessagesTable.UID + " = ?";
                    String[] selectionArgs = new String[]{String.valueOf(userUid)};
                    CustomLog.d("insertDialogsWithMessages, uid is : " + userUid);
                    Cursor mCursor = null;

                    CustomLog.d("insertDialogsWithMessages. func2");
                    ContentValues[] dialogValues = new ContentValues[dialog_count];
                    int index = 0;
                    boolean needNotifi = false;

                    for (int j = 0; j < dialog_count; j++) {
                        JSONObject oo = d_array.getJSONObject(j);
                        String content = oo.optString("content");
                        String update_time = oo.optString("update_time");
                        String details = oo.optString("details");
                        String tuid = oo.getString("uid");
                        int dialog_id = oo.optInt("dialog_id");
                        int dialog_type = oo.optInt("dialog_type");
                        if (dialog_type == MessageModel.DIALOG_TYPE_FOCUS || dialog_type == MessageModel.DIALOG_TYPE_VOICE_MESSAGE) {
                            msgInside = 1;
                        }
                        msgInside = (dialog_type == MessageModel.DIALOG_TYPE_FOCUS || dialog_type == MessageModel.DIALOG_TYPE_VOICE_MESSAGE) ? 1 : 0;
                        CustomLog.d("insertDialogsWithMessages, hello box, dialog_type is : " + dialog_type);
                        CustomLog.d("insertDialogsWithMessages, hello box, msgInside is : " + msgInside);

                        isSayHello = (msgInside == 1);

                        dialogValues[index] = new ContentValues();
                        dialogValues[index].put(PWDBConfig.DialogsTable.CONTENT, content);
                        dialogValues[index].put(PWDBConfig.DialogsTable.DIALOG_ID, dialog_id);
                        dialogValues[index].put(PWDBConfig.DialogsTable.UPDATE_TIME, oo.optString("update_time"));
                        dialogValues[index].put(PWDBConfig.DialogsTable.MSG_ID, oo.optString("msg_id"));
                        dialogValues[index].put(PWDBConfig.DialogsTable.UID, userUid);

                        if (tuid.equals(userUid)) {
                            dialogValues[index].put(PWDBConfig.DialogsTable.TYPE, 1); //接收的消息
                            dialogValues[index].put(PWDBConfig.DialogsTable.READ_STATUS, 0);
                        } else {
                            dialogValues[index].put(PWDBConfig.DialogsTable.TYPE, 0); //发送的消息
                            dialogValues[index].put(PWDBConfig.DialogsTable.SEND_STATUS, 0);
                        }
                        dialogValues[index].put(PWDBConfig.DialogsTable.DIALOG_TYPE, dialog_type);
                        dialogValues[index].put(PWDBConfig.DialogsTable.DETAILS, details);
                        //MsgAcceptedMsgActivity.Uid != userUid && tuid == userUid
                        Integer nodisturb = noDisturbMapping.get(tuid);
                        nodisturb = nodisturb == null ? 0 : nodisturb;
                        boolean isnotify = true;
                        if (isgroup) {
                            if (nodisturb == 0) {
                                isnotify = false;
                            } else {
                                if ((group_dialog_type == GroupConstant.MessageType.TYPE_REDBAG || group_dialog_type == GroupConstant.MessageType.TYPE_REPUTATION_REDBAG)) {
                                    isnotify = true;
                                } else {
//                                    isnotify = !String.valueOf(MsgAcceptedMsgActivity.Uid).equals(userUid) && tuid.equals(userUid) && isgroup && noDisturbMapping.containsKey(tuid);
                                    isnotify = false;
                                }
                            }
                        } else {
                            isnotify = !String.valueOf(MsgAcceptedMsgActivity.Uid).equals(userUid) && tuid.equals(userUid);
                        }
                        if (isnotify) {
                            MsgNotifiModel notifiItem = new MsgNotifiModel();
                            notifiItem.dialog_id = dialog_id;
                            notifiItem.uid = userUid;
                            notifiItem.content = content;
                            notifiItem.isSayHello = isSayHello;
                            if (!TextUtils.isEmpty(details) && details.length() > 2) {
                                try {
                                    JSONObject detailObject = new JSONObject(details);
                                    CustomLog.d("detailObject is : " + detailObject);
                                    if (!TextUtils.isEmpty(detailObject.optString("msg")))
                                        notifiItem.content = detailObject.optString("msg");
                                    notifiItem.content = MessageUtil.resetContent(notifiItem.content, 1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (dialog_type == MessageModel.DIALOG_TYPE_IMAGE_MESSAGE || (isgroup && dialog_type == GroupConstant.MessageType.TYPE_IMAGE)) {
                                notifiItem.content = "[图片]";
                            }
                            notifiItem.name = name;
                            notifiItem.update_time = update_time;
                            synchronized (msgNotifiList) {
                                msgNotifiList.add(notifiItem);
                            }

                            needNotifi = true;
                        }
                        index++;
                    }

                    try {
                        mCursor = mContext.getContentResolver().query(PWDBConfig.MessagesTable.CONTENT_URI, null, selection, selectionArgs, null);
                        CustomLog.d("insertDialogsWithMessages, mCursor count is : " + mCursor.getCount());
                        ContentValues messageValues = new ContentValues();
                        messageValues.put(PWDBConfig.MessagesTable.MSG_ID, msgid);
                        messageValues.put(PWDBConfig.MessagesTable.USER, user != null ? user.toString() : "");
                        messageValues.put(PWDBConfig.MessagesTable.UID, userUid);
                        messageValues.put(PWDBConfig.MessagesTable.MSG_TYPE, msg_type);
                        messageValues.put(PWDBConfig.MessagesTable.IS_HIDE, 0);
                        messageValues.put(PWDBConfig.MessagesTable.CONTENT, dialogValues[0].getAsString(PWDBConfig.DialogsTable.CONTENT));

                        if (msgInside == 1 && msg_type == TabMsgFragment.USER_MESSAGE) {
                            insertSingleMessage();
                        }
                        if (mCursor != null && mCursor.getCount() > 0) {
                            boolean needReSetSayHello = false;
                            if (mCursor.moveToFirst()) {

                                messageValues.put(PWDBConfig.MessagesTable.INSIDE, msgInside);
                                needReSetSayHello = isSayHello;
                            }
                            CustomLog.d("insertDialogsWithMessages if, cursor count is == " + mCursor.getCount());
                            mContext.getContentResolver().update(PWDBConfig.MessagesTable.CONTENT_URI, messageValues, selection, selectionArgs);
                            if (needReSetSayHello) {
                                reSetSayhelloData();
                            }
                        } else {
                            CustomLog.d("insertDialogsWithMessages else, cursor count is == 0");
                            if (msg_type == TabMsgFragment.USER_MESSAGE) {
                                messageValues.put(PWDBConfig.MessagesTable.INSIDE, msgInside);
                            } else {
                                messageValues.put(PWDBConfig.MessagesTable.INSIDE, 0);
                            }
                            mContext.getContentResolver().insert(PWDBConfig.MessagesTable.CONTENT_URI, messageValues);
//                                if (msgInside == 1 && msg_type == TabMsgFragment.USER_MESSAGE) {
//                                    insertSingleMessage();
//                                }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (mCursor != null) {
                            mCursor.close();
                            mCursor = null;
                        }
                    }

                    mContext.getContentResolver().bulkInsert(PWDBConfig.DialogsTable.CONTENT_URI, dialogValues);
                    CustomLog.d("insertDialogsWithMessages() content is : " + dialogValues[0].getAsString(PWDBConfig.DialogsTable.CONTENT));
                    if (needNotifi) {//此次消息有需要触发通知时才处理通知队列里的消息触发
                        showIMNotification(msg_type);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 修改一条msg的user的price
     */
    public void updateMsgPrice(final float price, final int msg_id) {
        PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                Cursor c = null;
                try {
                    String[] projection = new String[1];
                    projection[0] = PWDBConfig.MessagesTable.USER;
                    String selection = PWDBConfig.MessagesTable.MSG_ID + " = ?";
                    String[] selectionArgs = new String[1];
                    selectionArgs[0] = String.valueOf(msg_id);
                    c = mContext.getContentResolver().query(PWDBConfig.MessagesTable.CONTENT_URI, projection, selection, selectionArgs, null);
                    if (c.moveToFirst()) {
                        JSONObject o = new JSONObject(c.getString(c.getColumnIndex(PWDBConfig.MessagesTable.USER)));
                        o.put("price", price);
                        ContentValues values = new ContentValues();
                        values.put(PWDBConfig.MessagesTable.USER, o.toString());
                        mContext.getContentResolver().update(PWDBConfig.MessagesTable.CONTENT_URI, values, selection, selectionArgs);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (c != null)
                        c.close();
                }
            }
        });
    }


    /**
     * 删除一条消息
     *
     * @param
     * @return
     */
    public boolean deleteMessageByUid(String uid) {
        CustomLog.d("deleteMessageByUid. uid is : " + uid);
        boolean b = false;
        try {
            String messageWhere = PWDBConfig.MessagesTable.UID + " = ?";
            String[] selectionArgs = new String[1];
            selectionArgs[0] = String.valueOf(uid);
            mContext.getContentResolver().delete(PWDBConfig.MessagesTable.CONTENT_URI, messageWhere, selectionArgs);
            if (String.valueOf(uid).equals(DfineAction.MSG_ID_SAYHELLO)) {
                String insideWhere = PWDBConfig.MessagesTable.INSIDE + " = ?";
                String[] insideSelectionArgs = new String[]{"1"};
                Cursor mCursor = mContext.getContentResolver().query(PWDBConfig.MessagesTable.CONTENT_URI, null, insideWhere, insideSelectionArgs, null);
                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        int msgUid = mCursor.getInt(mCursor.getColumnIndex(PWDBConfig.MessagesTable.UID));
                        String dialogWhere = PWDBConfig.DialogsTable.UID + " = ?";
                        mContext.getContentResolver().delete(PWDBConfig.DialogsTable.CONTENT_URI, dialogWhere, new String[]{String.valueOf(msgUid)});
                    }
                    mCursor.close();
                    mCursor = null;
                }
                mContext.getContentResolver().delete(PWDBConfig.MessagesTable.CONTENT_URI, insideWhere, insideSelectionArgs);
            } else {
                String dialogWhere = PWDBConfig.DialogsTable.UID + " = ?";
                mContext.getContentResolver().delete(PWDBConfig.DialogsTable.CONTENT_URI, dialogWhere, selectionArgs);
            }
            b = true;
        } catch (Exception e) {
            b = false;
        }
        return b;
    }

    public boolean deleteMessageByMsgId(String msgId) {
        CustomLog.d("deleteMessageByMsgId. msgId is : " + msgId);
        boolean b = false;
        try {
            String messageWhere = PWDBConfig.MessagesTable.MSG_ID + " = ?";
            String[] selectionArgs = new String[1];
            selectionArgs[0] = String.valueOf(msgId);
            mContext.getContentResolver().delete(PWDBConfig.MessagesTable.CONTENT_URI, messageWhere, selectionArgs);
            String dialogWhere = PWDBConfig.DialogsTable.MSG_ID + " = ?";
            mContext.getContentResolver().delete(PWDBConfig.DialogsTable.CONTENT_URI, dialogWhere, selectionArgs);
            b = true;
        } catch (Exception e) {
            b = false;
        }
        return b;
    }

    public void updateDialogue() {

    }


    public boolean deleteSayHelloMessageByUid(int uid) {
        CustomLog.d("deleteSayHelloMessageByUid. user id is : " + uid);
        boolean ret = false;
        try {
            String messageWhere = PWDBConfig.MessagesTable.UID + " = ?";
            String[] selectionArgs = new String[1];
            selectionArgs[0] = String.valueOf(uid);
            mContext.getContentResolver().delete(PWDBConfig.MessagesTable.CONTENT_URI, messageWhere, selectionArgs);
            String dialogWhere = PWDBConfig.DialogsTable.UID + " = ?";
            mContext.getContentResolver().delete(PWDBConfig.DialogsTable.CONTENT_URI, dialogWhere, selectionArgs);
            ret = true;
        } catch (Exception e) {
            ret = false;
        }
        return ret;
    }

    /**
     * 获取消息总数目
     *
     * @return
     */

    public int getBadge() {
        Cursor c = null;
        int num = 0;
        try {
            String[] projection = new String[1];
            projection[0] = PWDBConfig.MessagesTable.UNREAD_COUNT;
            String selection = PWDBConfig.MessagesTable.INSIDE + " = ?";
            String selectionArgs[] = new String[]{String.valueOf(0)};
            c = mContext.getContentResolver().query(PWDBConfig.MessagesTable.CONTENT_URI, projection, selection, selectionArgs, null);

            while (c.moveToNext()) {
                num += c.getInt(c.getColumnIndex(PWDBConfig.MessagesTable.UNREAD_COUNT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
        }
        return num;
    }


    /**
     * 清除消息数目
     *
     * @param
     */
    public void clearBadgeByMsgId(int uid) {
        try {
            {
                ContentValues values = new ContentValues();
                values.put(PWDBConfig.MessagesTable.UNREAD_COUNT, 0);
                String where = PWDBConfig.MessagesTable.UID + " = ?";
                String[] selectionArgs = new String[1];
                selectionArgs[0] = String.valueOf(uid);
                mContext.getContentResolver().update(PWDBConfig.MessagesTable.CONTENT_URI, values, where, selectionArgs);
            }
            {
                ContentValues values = new ContentValues();
                values.put(PWDBConfig.MessagesTable.INSIDE_NEW, 0);
                String where = PWDBConfig.MessagesTable.INSIDE + " = ?";
                String[] selectionArgs = new String[1];
                selectionArgs[0] = String.valueOf(1);
                mContext.getContentResolver().update(PWDBConfig.MessagesTable.CONTENT_URI, values, where, selectionArgs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearBadgeByUid(String uid) {
        try {
            ContentValues values = new ContentValues();
            values.put(PWDBConfig.MessagesTable.UNREAD_COUNT, 0);
            String where = PWDBConfig.MessagesTable.UID + " = ?";
            String[] selectionArgs = new String[1];
            selectionArgs[0] = String.valueOf(uid);
            mContext.getContentResolver().update(PWDBConfig.MessagesTable.CONTENT_URI, values, where, selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reSetSayhelloData() {
        PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int sayHelloCount = -1;
                    String sayHelloWhere = PWDBConfig.MessagesTable.INSIDE + " = ?";
                    String[] selectionArgs = new String[1];
                    selectionArgs[0] = String.valueOf(1);
                    String sortOrder = PWDBConfig.MessagesTable.UPDATE_TIME + " desc";

                    Cursor mCursor = mContext.getContentResolver().query(PWDBConfig.MessagesTable.CONTENT_URI, null, sayHelloWhere, selectionArgs, sortOrder);
                    if (mCursor != null) {
                        sayHelloCount = mCursor.getCount();

                        CustomLog.d("reSetSayhelloData. sayHelloCount is : " + sayHelloCount);
                        String messageWhere = PWDBConfig.MessagesTable.UID + " = ?";
                        selectionArgs[0] = String.valueOf(DfineAction.MSG_ID_SAYHELLO);

                        if (sayHelloCount == 0) {
                            mContext.getContentResolver().delete(PWDBConfig.MessagesTable.CONTENT_URI, messageWhere, selectionArgs);
                        } else {
                            Cursor thatCursor = mContext.getContentResolver().query(PWDBConfig.MessagesTable.CONTENT_URI, null, messageWhere, selectionArgs, sortOrder);
                            CustomLog.d("reSetSayhelloData, that cursor count is : " + thatCursor.getCount());
                            int newSayHelloCount = 0;
                            String newContent = "";
                            String newUpdate_time = "";
                            int newType = 0;
                            String name = "";
                            while (mCursor.moveToNext()) {
                                int msgId = mCursor.getInt(mCursor.getColumnIndex(PWDBConfig.MessagesTable.MSG_ID));
                                CustomLog.d("reSetSayhelloData, msg id is : " + msgId);
                                int userId = mCursor.getInt(mCursor.getColumnIndex(PWDBConfig.MessagesTable.UID));
                                CustomLog.d("reSetSayhelloData, user id is : " + userId);
                                int inside = mCursor.getInt(mCursor.getColumnIndex(PWDBConfig.MessagesTable.INSIDE));
                                int isNew = mCursor.getInt(mCursor.getColumnIndex(PWDBConfig.MessagesTable.INSIDE_NEW));
                                CustomLog.d("reSetSayhelloData, inside is : " + inside);
                                CustomLog.d("reSetSayhelloData, inside_new is : " + isNew);

                                if (isNew == 1) {
                                    newSayHelloCount++;
                                }
                                if (TextUtils.isEmpty(newContent) && TextUtils.isEmpty(newContent) && newType == 0) {
                                    newContent = mCursor.getString(mCursor.getColumnIndex(PWDBConfig.MessagesTable.CONTENT));
                                    newUpdate_time = mCursor.getString(mCursor.getColumnIndex(PWDBConfig.MessagesTable.UPDATE_TIME));
                                    newType = mCursor.getInt(mCursor.getColumnIndex(PWDBConfig.MessagesTable.TYPE));
                                    String user = mCursor.getString(mCursor.getColumnIndex(PWDBConfig.MessagesTable.USER));
                                    name = new JSONObject(user).optString("name");
//                                    if (!TextUtils.isEmpty(name)) {
//                                        newContent = name + ":" + newContent;
//                                    }
                                }
                            }
                            ContentValues values = new ContentValues();
                            values.put(PWDBConfig.MessagesTable.CONTENT, newContent);
                            values.put(PWDBConfig.MessagesTable.UPDATE_TIME, newUpdate_time);
                            values.put(PWDBConfig.MessagesTable.TYPE, newType);
                            values.put(PWDBConfig.MessagesTable.UNREAD_COUNT, newSayHelloCount);
                            values.put(PWDBConfig.MessagesTable.INSIDE, 0);
                            int updateNum = mContext.getContentResolver().update(PWDBConfig.MessagesTable.CONTENT_URI,
                                    values, messageWhere, selectionArgs);
                            CustomLog.d("reSetSayhelloData, updateNum is : " + updateNum);
                        }
                        mCursor.close();
                        mCursor = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public int findMsgIdByUid(int uid) {
        CustomLog.d("db service, findMsgIdByUid. uid is : " + uid);
        int ret = -1;
        String uidWhere = PWDBConfig.UidMsgId.UID + " = ?";
        String[] selectionArgs = new String[1];
        selectionArgs[0] = String.valueOf(uid);
        Cursor cursor = mContext.getContentResolver().query(PWDBConfig.UidMsgId.CONTENT_URI, null, uidWhere, selectionArgs, null);
        if (cursor.moveToFirst()) {
            ret = cursor.getInt(cursor.getColumnIndexOrThrow(PWDBConfig.UidMsgId.MSG_ID));
        }
        CustomLog.d("db service, findMsgIdByUid, \t msg_id is : " + ret);
        return ret;
    }

    public int findDialogTypeByUid(int uid) {
        int ret = -1;
        String msgWhere = PWDBConfig.DialogsTable.MSG_ID + " = ?";
        String[] selectionArgs = new String[1];
        selectionArgs[0] = String.valueOf(findMsgIdByUid(uid));
        String sortOrder = PWDBConfig.DialogsTable.UPDATE_TIME + " desc";
        Cursor cursor = mContext.getContentResolver().query(PWDBConfig.DialogsTable.CONTENT_URI, null, msgWhere, selectionArgs, sortOrder);
        if (cursor.moveToFirst()) {
            ret = cursor.getInt(cursor.getColumnIndexOrThrow(PWDBConfig.DialogsTable.DIALOG_TYPE));
        }
        CustomLog.d("db service, findMsgIdByUid. uid is : " + uid + ", \t dialog_type is : " + ret);
        return ret;
    }


    /**
     * 修改红包的状态
     *
     * @param id
     */
    public void updateRedBagStatus(int id) {
        try {
            ContentValues values = new ContentValues();
            values.put(PWDBConfig.DialogsTable.READ_STATUS, 1);
            String selection = PWDBConfig.DialogsTable.ID + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(id)};
            int code = mContext.getContentResolver().update(PWDBConfig.DialogsTable.CONTENT_URI, values, selection, selectionArgs);
            CustomLog.i("code" + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateMessageContent(int uid, String msgContent) {
        try {
            ContentValues values = new ContentValues();
            values.put(PWDBConfig.MessagesTable.CONTENT, msgContent);
            String selection = PWDBConfig.MessagesTable.UID + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(uid)};
            int code = mContext.getContentResolver().update(PWDBConfig.MessagesTable.CONTENT_URI, values, selection, selectionArgs);
            CustomLog.i("code" + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDialogDetails(long dbId, String details) {
        try {
            ContentValues values = new ContentValues();
            values.put(PWDBConfig.DialogsTable.DETAILS, details);
            String selection = PWDBConfig.DialogsTable.ID + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(dbId)};
            mContext.getContentResolver().update(PWDBConfig.DialogsTable.CONTENT_URI, values, selection, selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 忽略所有的打招呼
     */
    public void deleteAllInsideSayHello() {
        CustomLog.d("deleteAllInsideSayHello.");
        Cursor c = null;
        try {
            String selection = PWDBConfig.MessagesTable.INSIDE + " = ?";
            String[] selectionArgs = new String[]{"1"};

            String dialogWhere = PWDBConfig.DialogsTable.UID + " = ?";
            String[] selectionDialogArgs = new String[1];

            c = mContext.getContentResolver().query(PWDBConfig.MessagesTable.CONTENT_URI, null, selection, selectionArgs, null);
            while (c != null && c.moveToNext()) {
                int uid = c.getInt(c.getColumnIndex(PWDBConfig.MessagesTable.UID));
                //同步删除dialog表的数据
                selectionDialogArgs[0] = String.valueOf(uid);
                mContext.getContentResolver().delete(PWDBConfig.DialogsTable.CONTENT_URI, dialogWhere, selectionDialogArgs);
            }
            mContext.getContentResolver().delete(PWDBConfig.MessagesTable.CONTENT_URI, selection, selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
        }
    }

    public JSONArray getMsgIds() {
        JSONArray responseArray = new JSONArray();
        Cursor c = null;
        try {
            String selection = PWDBConfig.MessagesTable.INSIDE + " = ?";
            String[] selectionArgs = new String[]{"1"};
            c = mContext.getContentResolver().query(PWDBConfig.MessagesTable.CONTENT_URI, null, selection, selectionArgs, null);
            while (c != null && c.moveToNext()) {
                int msg_id = c.getInt(c.getColumnIndex(PWDBConfig.MessagesTable.MSG_ID));
                responseArray.put(msg_id);
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (c != null)
                c.close();
        }
        return responseArray;
    }

    public JSONArray getMsgId(String uid) {
        JSONArray responseArray = new JSONArray();
        Cursor mCursor = null;
        try {
            String selection = PWDBConfig.MessagesTable.UID + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(uid)};
            mCursor = mContext.getContentResolver().query(PWDBConfig.MessagesTable.CONTENT_URI, null, selection, selectionArgs, null);
            if (mCursor != null && mCursor.getCount() > 0) {
                if (mCursor.moveToFirst()) {
                    int msg_id = mCursor.getInt(mCursor.getColumnIndex(PWDBConfig.MessagesTable.MSG_ID));
                    responseArray.put(msg_id);
                }
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (mCursor != null)
                mCursor.close();
        }
        return responseArray;
    }


    public void insertSystemMessage() {
//		[
//		    {
//		        "dialogs": [
//		            {
//		                "dialog_id": 515036,
//		                "update_time": "2015-06-01 16:42:28",
//		                "uid": 1,
//		                "msg_id": 172381,
//		                "content": "0909llll",
//		                "details": "null",
//		                "dialog_type": 0
//		            }
//		        ],
//		        "msg_type": 3,
//		        "inside": 1,
//		        "msg_id": 172381,
//		        "user": {
//		            "province": null,
//		            "city": null,
//		            "slogan": null,
//		            "name": null,
//		            "gender": 0,
//		            "price": 0,
//		            "birthday": null,
//		            "avatar": null,
//		            "uid": 1
//		        },
//		        "badge": 1
//		    }
//		]
        try {
            SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentTime = formater.format(new Date());
            int msg_id = -10000;

            JSONObject dataJSON = new JSONObject();
            JSONArray dialogArray = new JSONArray();
            JSONObject dialogJSON = new JSONObject();
            JSONObject userJSON = new JSONObject();

            dialogJSON.put("dialog_id", -10000);
            dialogJSON.put("update_time", currentTime);
            dialogJSON.put("uid", 1);
            dialogJSON.put("msg_id", msg_id);
            dialogJSON.put("content", mContext.getResources().getString(R.string.welcome_back_str));
            dialogJSON.put("dialog_type", 0);
            dialogArray.put(dialogJSON);

            userJSON.put("gender", 0);
            userJSON.put("price", 0);
            userJSON.put("uid", 1);
            dataJSON.put("user", userJSON);
            dataJSON.put("dialogs", dialogArray);
            dataJSON.put("msg_type", TabMsgFragment.SYS_MESSAGE);
            dataJSON.put("inside", 1);
            dataJSON.put("msg_id", msg_id);
            dataJSON.put("badge", 1);

            JSONArray dataArray = new JSONArray();
            dataArray.put(dataJSON);
            insertDialogsWithMessages(dataArray, false, -1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void insertCreateGroupMsg(String group_id, String group_name, String group_avatar, String content, int dialog_id, String uid, int dialog_type, String currentTime) {
        CustomLog.d("insertCreateGroupMsg. content is : " + content);
        try {

            //CustomLog.d("insertCreateGroupMsg. group_id is : " + group_id + ", group_prefix is : " + group_prefix + ", admin_id is : " + admin_id);

            JSONObject dataJSON = new JSONObject();
            JSONArray dialogArray = new JSONArray();
            JSONObject dialogJSON = new JSONObject();
            JSONObject userJSON = new JSONObject();

            dialogJSON.put("dialog_id", dialog_id);
            dialogJSON.put("update_time", currentTime);
            dialogJSON.put("uid", uid);
            dialogJSON.put("msg_id", group_id);
            dialogJSON.put("content", content);
            dialogJSON.put("dialog_type", 0);
            dialogArray.put(dialogJSON);

            userJSON.put("name", group_name);
            userJSON.put("avatar_thumbnail", group_avatar);
//            userJSON.put("gender", 0);
//            userJSON.put("price", 0);
            userJSON.put("uid", uid);
            dataJSON.put("user", userJSON);
            dataJSON.put("dialogs", dialogArray);
            dataJSON.put("msg_type", TabMsgFragment.GROUP_MESSAGE);
            dataJSON.put("inside", 0);
            dataJSON.put("msg_id", group_id);
            dataJSON.put("badge", 1);

            JSONArray dataArray = new JSONArray();
            dataArray.put(dataJSON);
            CustomLog.d("insertCreateGroupMsg. array is : " + dataArray);
            insertDialogsWithMessages(dataArray, true, dialog_type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 批量插入联系人备注数据
     *
     * @param
     */
    public void insertPwRemark(final JSONObject o, final Handler mHandler) {
        if (o == null) {
            Message message = Message.obtain();
            message.what = WHAT_INSERT_REMARK_COMMPLETE;
            callBackOnUiThread(message, mHandler);
            return;
        }
        PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
            @Override
            public void run() {

                SQLiteDatabase db = null;
                SQLiteStatement sqLiteStatement = null;
                boolean isscuess = false;
                try {
                    String sql = "insert or replace into %s (uid, remark) values (?, ?)";
                    db = mDBHelper.getWritableDatabase(PWDBUtil.getCurrentReMarkDBName(mContext));
                    sql = String.format(sql, PWDBConfig.TB_NAME_PW_REMARK);
                    sqLiteStatement = db.compileStatement(sql);
                    db.beginTransaction();
                    Iterator<String> it = o.keys();
                    while (it.hasNext()) {
                        String key = it.next();
                        sqLiteStatement.bindString(1, key);
                        sqLiteStatement.bindString(2, o.getString(key));
                        sqLiteStatement.execute();
                    }
                    db.setTransactionSuccessful();
                    isscuess = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = WHAT_INSERT_REMARK_ERROR;
                    callBackOnUiThread(message, mHandler);
                } finally {
                    if (sqLiteStatement != null) {
                        sqLiteStatement.close();
                    }
                    if (db != null) {
                        db.endTransaction();
                        db.close();
                    }
                    if (isscuess) {
                        Message message = Message.obtain();
                        message.what = WHAT_INSERT_REMARK_COMMPLETE;
                        callBackOnUiThread(message, mHandler);
                    }
                }
            }
        });
    }

    /**
     * 插入一条联系人备注数据
     *
     * @param
     */
    public void insertSinglePwRemark(String uid, String remark) {
        SQLiteDatabase db = null;
        try {
            String sql = "insert or replace into %s (uid, remark) values (?, ?)";
            db = mDBHelper.getWritableDatabase(PWDBUtil.getCurrentReMarkDBName(mContext));
            db.execSQL(String.format(sql, PWDBConfig.TB_NAME_PW_REMARK), new Object[]{uid, remark});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * 删除一条联系人备注数据
     *
     * @param
     */
    public void delSinglePwRemark(String uid) {
        SQLiteDatabase db = null;
        try {
            String sql = "delete from %s where uid = ?";
            db = mDBHelper.getWritableDatabase(PWDBUtil.getCurrentReMarkDBName(mContext));
            db.execSQL(String.format(sql, PWDBConfig.TB_NAME_PW_REMARK), new Object[]{uid});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    public void getAllRemarks(SparseArray<String> sparseArray) {
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            String sql = "select * from %s";
            db = mDBHelper.getWritableDatabase(PWDBUtil.getCurrentReMarkDBName(mContext));
            c = db.rawQuery(String.format(sql, PWDBConfig.TB_NAME_PW_REMARK), null);
            while (c.moveToNext()) {
                sparseArray.put(c.getInt(0), c.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * 根据uid得到当时申请通话的消息记录
     *
     * @param tuid
     * @return
     */
    public String getRequestLogByUid(int tuid) {
        String request_log = "";
        Cursor c = null;
        try {
            String[] projection = new String[]{PWDBConfig.RequestsTable.CONTENT};
            String selection = PWDBConfig.RequestsTable.UID + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(tuid)};
            c = mContext.getContentResolver().query(PWDBConfig.RequestsTable.CONTENT_URI, projection, selection, selectionArgs, null);
            if (c.moveToFirst()) {
                request_log = c.getString(c.getColumnIndex(PWDBConfig.RequestsTable.CONTENT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
        }
        return request_log;
    }

    public void showIMNotification(int msg_type) {
        synchronized (msgNotifiList) {
            int size = msgNotifiList.size();
            if (size == 0) {
                return;
            }
            int notifiy_defults = Notification.DEFAULT_LIGHTS; //开启灯光
//	        if (PWUtils.isOnForeground(mContext)) {
//	        	notifiy_defults = notifiy_defults | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
//	        } else {
            String key = SharedPreferencesUtil.getStringExtra(mContext, Constans.SP_KEY_PUSH_STR, "");
            if (!PWUtils.isNeedPush(mContext)) {
                return;
            }
            if (!TextUtils.isEmpty(key)) {
                try {
                    JSONObject object = new JSONObject(key);
                    boolean sound = object.optBoolean("sound");
                    boolean vibrate = object.optBoolean("vibrate");
                    CustomLog.d("insertMessage, showImNotification. sound option is : " + sound + ", vibrate is : " + vibrate);
                    if (sound) {
                        notifiy_defults = notifiy_defults | Notification.DEFAULT_SOUND; // 设置了声音
                    }
                    if (vibrate) {
                        notifiy_defults = notifiy_defults | Notification.DEFAULT_VIBRATE;// 开了震动
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//	        }
            ArrayList<String> stringList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                boolean isNeedAdd = true;
                for (int j = 0; j < stringList.size(); j++) {
                    if (msgNotifiList.get(i).uid.equals(stringList.get(j))) {
                        isNeedAdd = false;
                        break;
                    }
                }
                if (isNeedAdd) {
                    stringList.add(msgNotifiList.get(i).uid);
                }
            }
            int count = stringList.size();

            String latest_uid = msgNotifiList.get(size - 1).uid;
            int notifyCounts = 1;
            if (!mNotifyMap.containsKey(latest_uid.hashCode())) {
                mNotifyMap.put(latest_uid.hashCode(), 1);
            } else {
                notifyCounts = mNotifyMap.get(latest_uid.hashCode());
                mNotifyMap.put(latest_uid.hashCode(), ++notifyCounts);
            }

            String contentText = "";
            String name = "";
            String name_prefix = "@";
            if (msg_type == TabMsgFragment.GROUP_MESSAGE) {
                name_prefix = "";
            }
            if (msgNotifiList.get(msgNotifiList.size() - 1).uid.equals("1")) {
                name = "系统消息";
            } else {
                name = name_prefix + msgNotifiList.get(msgNotifiList.size() - 1).name;
            }
            String content = msgNotifiList.get(msgNotifiList.size() - 1).content;
            String ticker = name + ":" + content;
            if (content.contains("@")) {
                ticker = content;
            }
            if (msg_type == TabMsgFragment.GROUP_MESSAGE) {
                ticker = msgNotifiList.get(msgNotifiList.size() - 1).content;
            }
            String unread_counts = mContext.getString(R.string.notify_counts, notifyCounts);
            if (notifyCounts == 1) {
                unread_counts = "";
            }
            contentText = unread_counts + ticker;
//            if (ticker.contains("@")) {
//                ticker = msgNotifiList.get(msgNotifiList.size() - 1).content;
//            }
//            if (count == 1) {
//                if (size == 1) {
//                    contentText = msgNotifiList.get(msgNotifiList.size() - 1).content;
//                } else {
//                    contentText = "发来" + size + "条消息";
//                }
//            } else {
//                name = "共有" + count + "个联系人";
//                contentText = "发来" + size + "条消息";
//            }

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    mContext).setSmallIcon(R.mipmap.ic_launcher)
                    .setDefaults(notifiy_defults)
                    .setContentTitle(name)
                    .setTicker(ticker).setContentText(contentText)
                    .setWhen(System.currentTimeMillis()).setAutoCancel(true)
                    .setOngoing(false);

            Intent intent = null;
            PendingIntent mIMPendingIntent = null;
            if (count == 1) {
                if (msg_type == TabMsgFragment.GROUP_MESSAGE) {
                    intent = new Intent(mContext, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("action", Constans.ACTION_FLAG_MESSAGE);
                    intent.putExtra("uid", msgNotifiList.get(msgNotifiList.size() - 1).uid);
                    intent.putExtra("msg_type", TabMsgFragment.GROUP_MESSAGE);
                } else {
                    if (msgNotifiList.get(msgNotifiList.size() - 1).uid.equals("1")) {
                        intent = new Intent(mContext, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("action", Constans.ACTION_FLAG_MESSAGE);
                        intent.putExtra("uid", msgNotifiList.get(msgNotifiList.size() - 1).uid);
                    } else {
                        if (msgNotifiList.get(msgNotifiList.size() - 1).isSayHello) {
                            intent = new Intent(mContext, SayHelloActivity.class);
                        } else {
                            intent = new Intent(mContext, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            intent.putExtra("action", Constans.ACTION_FLAG_MESSAGE);
                            intent.putExtra("uid", msgNotifiList.get(msgNotifiList.size() - 1).uid);
                        }
                    }
                }

            } else {
                boolean isAllSayHello = true;
                for (int j = 0; j < msgNotifiList.size(); j++) {
                    if (!msgNotifiList.get(j).isSayHello) {
                        isAllSayHello = false;
                        break;
                    }
                }
                if (isAllSayHello) {
                    intent = new Intent(mContext, SayHelloActivity.class);
                } else {
                    intent = new Intent(mContext, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("action", Constans.ACTION_FLAG_MESSAGE);
                }
            }
            mIMPendingIntent = PendingIntent.getActivity(mContext, 0,
                    intent, Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            mBuilder.setContentIntent(mIMPendingIntent);
            NotificationManager notifyMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notifyMgr.notify(latest_uid.hashCode(), mBuilder.build());

        }
    }

    public void cancelIMNotification() {
        msgNotifiList.clear();
        mNotifyMap.clear();
        NotificationManager mNotifyMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(Constans.NOTIFY_ID_IM_MESSAGE);
    }

    public void updateLatestDialogue(GroupMessageBaseModel baseModel) {
        BriteDatabase briteDatabase = BriteDBHelperHolder.getInstance().getBriteDatabase(mContext);
        if (briteDatabase != null) {
            String msg_id = baseModel.group.group_id;
            String sql = String.format("delete from %s where msg_id = ?", PWDBConfig.TB_NAME_PW_DIALOGS);
            briteDatabase.execute(sql, msg_id);
            String del_message_sql = String.format("delete from %s where msg_id = ?", PWDBConfig.TB_NAME_PW_MESSAGES);
            briteDatabase.execute(del_message_sql, msg_id);
            String del_uid_msgid = String.format("delete from %s where msg_id = ?", PWDBConfig.TB_NAME_UID_MSGID);
            briteDatabase.execute(del_uid_msgid, msg_id);
        }
        MsgDBCenterService.getInstance().insertDialogsWithGroupchat(baseModel);
    }
}
