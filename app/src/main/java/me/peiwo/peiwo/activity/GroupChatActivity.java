package me.peiwo.peiwo.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.qiniu.android.http.ResponseInfo;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.CommandMessage;
import me.peiwo.peiwo.BuildConfig;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.GroupMessageAdapter;
import me.peiwo.peiwo.callback.ReceiveRongMessageListener;
import me.peiwo.peiwo.callback.UploadCallback;
import me.peiwo.peiwo.constans.GroupConstant;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.model.EmotionModel;
import me.peiwo.peiwo.model.ExpressionBaseModel;
import me.peiwo.peiwo.model.GIFModel;
import me.peiwo.peiwo.model.GroupMemberModel;
import me.peiwo.peiwo.model.GroupMessageModel;
import me.peiwo.peiwo.model.ImageItem;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.model.TabfindGroupModel;
import me.peiwo.peiwo.model.groupchat.GroupBaseUserModel;
import me.peiwo.peiwo.model.groupchat.GroupCommandData;
import me.peiwo.peiwo.model.groupchat.GroupMessageBaseModel;
import me.peiwo.peiwo.model.groupchat.GroupMessageDecorationModel;
import me.peiwo.peiwo.model.groupchat.GroupMessageGIFModel;
import me.peiwo.peiwo.model.groupchat.GroupMessageImageModel;
import me.peiwo.peiwo.model.groupchat.GroupMessageRedBagModel;
import me.peiwo.peiwo.model.groupchat.GroupMessageRedBagTipModel;
import me.peiwo.peiwo.model.groupchat.GroupMessageRepuRedBagModel;
import me.peiwo.peiwo.model.groupchat.GroupMessageRepuRedBagTipModel;
import me.peiwo.peiwo.model.groupchat.GroupMessageTextModel;
import me.peiwo.peiwo.model.groupchat.PacketIconModel;
import me.peiwo.peiwo.model.groupchat.QNImageToken;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.PWUploader;
import me.peiwo.peiwo.util.ChatInputDetectorCompat;
import me.peiwo.peiwo.util.FileManager;
import me.peiwo.peiwo.util.Md5Util;
import me.peiwo.peiwo.util.MsgImageKeeper;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.util.group.ChatImageWrapper;
import me.peiwo.peiwo.util.group.RongMessageParse;
import me.peiwo.peiwo.widget.EmotionEditText;
import me.peiwo.peiwo.widget.GroupBottomActionView;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class GroupChatActivity extends BaseActivity implements ReceiveRongMessageListener {
    public static final int MAX_SILENT_SECONDS = 5;//游客禁言5秒
    public static final String K_GROUP_DATA = "group_data";
    public static final String K_MEMBER_DATA = "member_data";
    public static final String K_NEED_SILENT = "silent";
    public static final String K_UNREAD_COUNT = "unread_count";
    private static final int PAGE_SIZE = 10;
    private static final int REQUEST_CODE_MORE_IMAGE = 1000;
    public static final int REQUEST_GROUP_HOMEPAGE = 1001;
    private static final int REQUEST_CODE_AT_MEMBER = 1002;
    private static final int REQUEST_CODE_REDBAG = 1003;
    private static final int SELECT_MSG_IMG_OK = 1004;

    public static final String ACTION_CLEAR_MESSAGE = "me.peiwo.peiwo.ACTION_CLEAR_MESSAGE";
    public static final String ACTION_POST_MESSAGE = "me.peiwo.peiwo.ACTION_POST_MESSAGE";
    public static final String ACTION_SHOW_NICKNAME = "me.peiwo.peiwo.ACTION_SHOW_NICKNAME";
    public static final String K_POST_MESSAGE_DATA = "msg_data";
    public static final String K_POST_MESSAGE_TYPE = "msg_type";
    public static final String K_SHOW_NICK_NAME = "show_nick";
    public static final String K_NICK_NAME = "nick_name";


    private List<GroupBaseUserModel> atUsers;

    @Bind(R.id.v_input_txt_start)
    View v_input_txt_start;
    @Bind(R.id.v_expression_start)
    View v_expression_start;
    @Bind(R.id.v_image_quick_switch_start)
    View v_image_quick_switch_start;
    @Bind(R.id.v_recycler_message)
    RecyclerView v_recycler_message;
    @Bind(R.id.v_bottom_panel)
    GroupBottomActionView v_bottom_panel;
    @Bind(R.id.et_message_input)
    EmotionEditText et_message_input;
    @Bind(R.id.title_view)
    TextView title_view;
    @Bind(R.id.iv_im_avatar)
    ImageView iv_im_avatar;
    @Bind(R.id.btn_left)
    TextView btn_left;

    private GroupMessageAdapter mAdapter;
    private List<GroupMessageBaseModel> mList;
    private ChatInputDetectorCompat detectorCompat;
    private PWUserModel selfModel;
    private TabfindGroupModel groupModel;
    private GroupMemberModel groupMemberModel;
    private boolean isloading = false;
    private boolean has_more = true;
    private Handler mHandler;
    private MsgDBCenterService dbService;
    //private int mGroupPeopleNum;

    private CompositeSubscription mSubscriptions;
    private boolean auto_scroll_to_last = true;
    private int last_msg_count;
    //    private boolean show_errorcode = false;
    private boolean need_silent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        EventBus.getDefault().register(this);
        selfModel = UserManager.getPWUser(this);
        mHandler = new Handler();
        dbService = MsgDBCenterService.getInstance();
        mSubscriptions = new CompositeSubscription();
        init();
        dbService.cancelIMNotification();
        checkRongConnectStatus();
        //debug
//        findViewById(R.id.v_send_message).setOnLongClickListener(v -> {
////            Toast.makeText(this, "debug 模式", Toast.LENGTH_SHORT).show();
//            show_errorcode = true;
//            return true;
//        });
    }

    private void checkRongConnectStatus() {
        RongIMClient rongIMClient = RongIMClient.getInstance();
        if (RongIMClient.ConnectionStatusListener.ConnectionStatus.DISCONNECTED == rongIMClient.getCurrentConnectionStatus()) {
            if (RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTING != rongIMClient.getCurrentConnectionStatus()) {
                PeiwoApp app = (PeiwoApp) getApplicationContext();
                app.getRongCloudTokenAndConnect();
            }
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (groupModel.total_number > 0) {
            title_view.setText(String.format("%s(%d)", title, groupModel.total_number));
        } else {
            title_view.setText(title);
        }
    }

    private void init() {
        Intent intent = getIntent();
        groupModel = intent.getParcelableExtra(K_GROUP_DATA);
        if (groupModel == null) {
            showToast(this, "group is null");
            finish();
            return;
        }
        /*****%>_<%******/
        MsgAcceptedMsgActivity.Uid = groupModel.group_id;
        /*****%>_<%******/
        clearGroupMessageBadge();
        dbService.removeAtUser(groupModel.group_id);
        clearRongMessageUnredStatus();
        setTitle(groupModel.group_name);
        if (TextUtils.isEmpty(groupModel.member_type)) {
            //没有传过来member_type时先赋初值为成员
            groupModel.member_type = GroupConstant.MemberType.MEMBER;
        }
        getGroupAndMemberInfo();
        mList = new ArrayList<>();
        v_recycler_message.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        v_recycler_message.setLayoutManager(layoutManager);
        mAdapter = new GroupMessageAdapter(this, mList, groupModel.show_nickname);
        mAdapter.setOnRecendMessageListener(this::recendMessage);
        mAdapter.setOnMessageLongClickListener(this::handMessageOnLongClick);
        mAdapter.setOnUserAvatarLongClickListener(this::handleUserAvatarLongClick);
        mAdapter.setOnRedBagReceiveListener(this::handleReceiveRedBag);
        v_recycler_message.setAdapter(mAdapter);
        setRecyclerViewScrollListener();
        v_input_txt_start.setSelected(true);
        detectorCompat = ChatInputDetectorCompat.with(this).setEmotionView(v_bottom_panel)
                .bindToContent(v_recycler_message).bindToEditText(et_message_input)
                .bindToEmotionButton(v_expression_start).bindTextMessageButton(v_input_txt_start)
                .bindImageQuickSwitchButton(v_image_quick_switch_start).build();
        setDetectorListeners();
        v_bottom_panel.post(() -> v_bottom_panel.getImageQuickSwitchView().setOnMoreActionClickListener(this::chooseMoreImage));
        v_bottom_panel.post(() -> v_bottom_panel.getImageQuickSwitchView().setOnMoreActionClickListener(this::startShowAlbum));
        v_bottom_panel.post(() -> v_bottom_panel.getExpressionPanelView().setOnExpressionItemClickListener(this::expressionClick));
        v_bottom_panel.post(() -> v_bottom_panel.getExpressionPanelView().setOnExpressionDeleteEmotionListener(this::expressionDeleteClick));
        loadHistoryMessages();
        if (!TextUtils.isEmpty(groupModel.avatar)) {
            ImageLoader.getInstance().displayImage(groupModel.avatar, iv_im_avatar);
        }
        listenEditTextDataChanged();
        need_silent = intent.getBooleanExtra(K_NEED_SILENT, false);
        if (need_silent) {
            //禁言5s
            silentFiveSeconds();
            //首次进群
        } else {
            detectorCompat.showSoftInputIfNeed();
        }
        setUnReadMessageNum();
    }

    private void getGroupAndMemberInfo() {
        ArrayList<NameValuePair> parpms = new ArrayList<>();
        parpms.add(new BasicNameValuePair("group_id", groupModel.group_id));
        ApiRequestWrapper.openAPIGET(this, parpms, AsynHttpClient.API_GROUP_MEMBER_MERGE, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(object -> {
                    //group info
                    String group = object.optString("group");
                    groupModel = JSON.parseObject(group, TabfindGroupModel.class);
                    setTitle(groupModel.group_name);
                    if (GroupConstant.MemberType.ALIEN.equals(groupModel.member_type)) {
                        showKickoutTipsIfNeed();
                        //被踢的人屏蔽发图片
                        v_bottom_panel.setConsuming(true);
                    }
                    //member info
                    JSONObject user = object.optJSONObject("user");
                    if (groupMemberModel == null) {
                        groupMemberModel = JSON.parseObject(user.toString(), GroupMemberModel.class);
                    } else {
                        groupMemberModel.member_type = user.optString("member_type");
                        groupMemberModel.name = user.optString("name");
                        groupMemberModel.nickname = user.optString("nickname");
                    }
                    groupModel.member_type = groupMemberModel.member_type;
                    //showMemberTipsIfNeed(groupMemberModel.member_type, "你不是群成员，无法共享群声望");
                    groupModel.show_nickname = groupMemberModel.show_nickname;
                    mAdapter.refreshMemberName(groupMemberModel.show_nickname);
                    if (GroupConstant.MemberType.NEWBIE.equals(groupMemberModel.member_type)) {
                        //游客不能发图片
                        v_bottom_panel.setConsuming(true);
                    }

                    if (need_silent && !TextUtils.isEmpty(groupModel.notice)) {
                        //首次游客进群本地插入群公告
                        String body = RongMessageParse.encodeDecorationMessageBody("公告：" + groupModel.notice, selfModel, groupModel, groupMemberModel);
                        GroupMessageModel messageModel = new GroupMessageModel(body);
                        GroupMessageDecorationModel decorationModel = RongMessageParse.decodeDecorationObjectSelf(body, messageModel.hashCode(), GroupMessageDecorationModel.class);
                        mList.add(decorationModel);
                        notifyRecyclerLastChanged();
                        RongIMClient.getInstance().insertMessage(Conversation.ConversationType.GROUP, groupModel.group_id, String.valueOf(selfModel.uid), messageModel, null);
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }

    private void setUnReadMessageNum() {
        setTextBadge();
        Uri uri = Uri.parse("content://" + PWDBConfig.AUTOHORITY + "/" + PWDBConfig.TB_NAME_PW_MESSAGES + "/#");
        getContentResolver().registerContentObserver(uri, true, badgeObserver);
    }

    private void setTextBadge() {
        int count = dbService.getBadge();
        if (count > 0) {
            btn_left.setTextColor(Color.parseColor("#00b8d0"));
            btn_left.setText("•");
        } else {
            btn_left.setText(null);
        }
    }

    private ContentObserver badgeObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            if (!PWUtils.isMultiClick()) {
                mHandler.postDelayed(GroupChatActivity.this::setTextBadge, 1000);
            }
        }
    };

    private void clearRongMessageUnredStatus() {
        RongIMClient.getInstance().clearMessagesUnreadStatus(Conversation.ConversationType.GROUP, groupModel.group_id, null);
    }

    private void handleReceiveRedBag(GroupMessageBaseModel baseModel, int location) {
        if (baseModel instanceof GroupMessageRedBagModel) {
            //抢钱的红包
            GroupMessageRedBagModel redBagModel = (GroupMessageRedBagModel) baseModel;
            receiveMoneyRedbag(redBagModel);
        } else if (baseModel instanceof GroupMessageRepuRedBagModel) {
            GroupMessageRepuRedBagModel repuRedBagModel = (GroupMessageRepuRedBagModel) baseModel;
            receiveRepuRedbag(repuRedBagModel);
        }
    }

    private void receiveRepuRedbag(GroupMessageRepuRedBagModel repuRedBagModel) {
        if (repuRedBagModel.send_status != GroupConstant.SendStatus.SUCCESS) {
            return;
        }
        //抢红包
        showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("packet_id", repuRedBagModel.packet.packet_id));
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_GROUP_SCORE_PACKET_RECEIVE, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(object -> {
//                    packet_id	y	number	红包id
//                    score	y	number	抢到的声望
//                    balance	y	number	用户声望积分
//                    msg
                    dismissAnimLoading();
                    //String packet_id = object.optString("packet_id");
                    //String score = object.optString("score");
                    String balance = object.optString("balance");
                    UserManager.updateScore(GroupChatActivity.this, balance);
                    JSONArray msgs = object.optJSONArray("msgs");
                    for (int i = 0, z = msgs.length(); i < z; i++) {
                        String msg = msgs.optString(i);
                        String body = RongMessageParse.encodeRepuRedBagTipMessageBody(msg, selfModel, groupModel, groupMemberModel);
                        GroupMessageModel messageModel = new GroupMessageModel(body);
                        GroupMessageRepuRedBagTipModel repuRedBagTipModel = RongMessageParse.decodeRepuRedBagTipObjectSelf(body, messageModel.hashCode(), GroupMessageRepuRedBagTipModel.class);
                        mList.add(repuRedBagTipModel);
                        notifyRecyclerLastChanged();
                        sendMessageWithCallback(messageModel, repuRedBagTipModel.extra.pw_description);
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {
//                10001	PARAMETER_ERROR	红包不存在
//                20003	DATA_NOT_AVAILABLE	红包已失效
//                70003	PACKET_FINISHED	红包被领光
//                70004	PACKET_ALREADY_RECEIVED	已经领过该红包
//                80001	NOT_IN_GROUP	不在群组中
//                80003	NEWBIE_ACCESS_GROUP_PACKET	新手不能抢群积分红包
                Object[] objects = new Object[]{error, ret};
                Observable.just(objects).observeOn(AndroidSchedulers.mainThread()).subscribe(rst -> {
                    dismissAnimLoading();
                    int err = (int) rst[0];
                    Object o = rst[1];
                    switch (err) {
                        case 10001:
                            if (!parseError(o))
                                Snackbar.make(v_recycler_message, "红包不存在", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 80001:
                            if (!parseError(o))
                                Snackbar.make(v_recycler_message, "不在群组中", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 20003:
                            if (!parseError(o))
                                Snackbar.make(v_recycler_message, "红包已过期", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 70003:
                            if (!parseError(o))
                                Snackbar.make(v_recycler_message, "你来晚了，红包都被抢光啦！", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 70004:
                            if (!parseError(o))
                                Snackbar.make(v_recycler_message, "已经领过该红包", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 80003:
                            //Snackbar.make(v_recycler_message, "新手不能抢群积分红包", Snackbar.LENGTH_SHORT).show();
                            showMemberTipsIfNeed(GroupConstant.MemberType.NEWBIE, parseErrorMsg(o));
                            break;

                        default:
                            Snackbar.make(v_recycler_message, "网络连接错误", Snackbar.LENGTH_SHORT).show();
                            break;
                    }
                });
            }
        });
    }

    private boolean parseError(Object o) {
        String msg = parseErrorMsg(o);
        if (!TextUtils.isEmpty(msg)) {
            Snackbar.make(v_recycler_message, msg, Snackbar.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private String parseErrorMsg(Object o) {
        if (o instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) o;
            return jsonObject.optString("msg", "");
        }
        return null;
    }

    private void receiveMoneyRedbag(GroupMessageRedBagModel redBagModel) {
        //抢红包
        if (redBagModel.send_status != GroupConstant.SendStatus.SUCCESS) {
            return;
        }
        showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("packet_id", redBagModel.packet.packet_id));
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_GROUP_PACKET, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(object -> {
                    dismissAnimLoading();
                    //String packet_id = object.optString("packet_id");
                    //String money = object.optString("money");
                    String balance = object.optString("balance");
                    UserManager.updateMoney(GroupChatActivity.this, balance);
                    JSONArray msgs = object.optJSONArray("msgs");
                    for (int i = 0, z = msgs.length(); i < z; i++) {
                        String msg = msgs.optString(i);
                        String body = RongMessageParse.encodeRedBagTipMessageBody(msg, selfModel, groupModel, groupMemberModel);
                        GroupMessageModel messageModel = new GroupMessageModel(body);
                        GroupMessageRedBagTipModel messageRedBagTipModel = RongMessageParse.decodeRedBagTipObjectSelf(body, messageModel.hashCode(), GroupMessageRedBagTipModel.class);
                        mList.add(messageRedBagTipModel);
                        notifyRecyclerLastChanged();
                        sendMessageWithCallback(messageModel, messageRedBagTipModel.extra.pw_description);
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {
//                10001	PARAMETER_ERROR	红包不存在
//                20003	DATA_NOT_AVAILABLE	红包已过期
//                70003	PACKET_FINISHED	红包被领光
//                70004	PACKET_ALREADY_RECEIVED	已经领过该红包
//                80001	NOT_IN_GROUP	不在群组中
//                80002	NEWBIE_ACCESS_MEMBER_PACKET	新手不能抢群成员的红包
//                80003	NEWBIE_ACCESS_GROUP_PACKET	新手不能抢收益红包
                Object[] objects = new Object[]{error, ret};
                Observable.just(objects).observeOn(AndroidSchedulers.mainThread()).subscribe(rst -> {
                    dismissAnimLoading();
                    int err = (int) rst[0];
                    Object o = rst[1];
                    switch (err) {
                        case 10001:
                            if (!parseError(o))
                                Snackbar.make(v_recycler_message, "红包不存在", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 80001:
                            if (!parseError(o))
                                Snackbar.make(v_recycler_message, "不在群组中", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 20003:
                            if (!parseError(o))
                                Snackbar.make(v_recycler_message, "红包已过期", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 70003:
                            if (!parseError(o))
                                Snackbar.make(v_recycler_message, "默认为你来晚了，红包都被抢光啦！", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 70004:
                            if (!parseError(o))
                                Snackbar.make(v_recycler_message, "已经领过该红包", Snackbar.LENGTH_SHORT).show();
                            break;
                        case 80002:
                            //Snackbar.make(v_recycler_message, "新手不能抢群成员的红包", Snackbar.LENGTH_SHORT).show();

                            showMemberTipsIfNeed(GroupConstant.MemberType.NEWBIE, parseErrorMsg(o));
                            break;
                        case 80003:
                            //Snackbar.make(v_recycler_message, "新手不能抢收益红包", Snackbar.LENGTH_SHORT).show();
                            showMemberTipsIfNeed(GroupConstant.MemberType.NEWBIE, parseErrorMsg(o));
                            break;

                        default:
                            Snackbar.make(v_recycler_message, "网络连接错误", Snackbar.LENGTH_SHORT).show();
                            break;
                    }
                });
            }
        });
    }


    private void silentFiveSeconds() {
        Subscription subscription = Observable.interval(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Long>() {
            @Override
            public void onStart() {
                Log.i("rongs--", "onStart == " + (Looper.myLooper() == Looper.getMainLooper()));
                if (!isFinishing()) {
                    //新人进群发条消息
                    sendSilentMessage();
                    et_message_input.setHint("5 s禁言中......");
                    et_message_input.setEnabled(false);
                    v_input_txt_start.setEnabled(false);
                    v_expression_start.setEnabled(false);
                    v_image_quick_switch_start.setEnabled(false);
                    findViewById(R.id.v_send_message).setEnabled(false);
                    findViewById(R.id.v_redbag_start).setEnabled(false);
                } else {
                    if (!isUnsubscribed()) {
                        unsubscribe();
                    }
                }
            }

            @Override
            public void onCompleted() {
                Log.i("rongs--", "onCompleted == " + (Looper.myLooper() == Looper.getMainLooper()));
                if (!isUnsubscribed()) {
                    unsubscribe();
                }
                if (!isFinishing()) {
                    et_message_input.setHint("输入内容......");
                    et_message_input.setEnabled(true);
                    v_input_txt_start.setEnabled(true);
                    v_expression_start.setEnabled(true);
                    v_image_quick_switch_start.setEnabled(true);
                    findViewById(R.id.v_send_message).setEnabled(true);
                    findViewById(R.id.v_redbag_start).setEnabled(true);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Long second) {
                //Log.i("rongs--", "onNext == " + (Looper.myLooper() == Looper.getMainLooper()));
                if (!isFinishing()) {
                    if (second < 4) {
                        et_message_input.setHint(String.format("%s s禁言中......", MAX_SILENT_SECONDS - second - 1));
                    } else {
                        onCompleted();
                    }
                } else {
                    if (!isUnsubscribed()) {
                        unsubscribe();
                    }
                }
            }
        });
        mSubscriptions.add(subscription);
    }

    private void sendDecorationMessage(String decoration, GroupMemberModel memberModel) {
        String body = RongMessageParse.encodeDecorationMessageBody(decoration, selfModel, groupModel, memberModel);
        GroupMessageModel messageModel = new GroupMessageModel(body);

        RongIMClient.getInstance().sendMessage(Conversation.ConversationType.GROUP, groupModel.group_id, messageModel, decoration, null, new RongIMClient.SendMessageCallback() {
            @Override
            public void onError(Integer messageId, RongIMClient.ErrorCode errorCode) {

            }

            @Override
            public void onSuccess(Integer messageId) {
                GroupMessageDecorationModel decorationModel = RongMessageParse.decodeDecorationObjectSelf(body, messageId, GroupMessageDecorationModel.class);
                mList.add(decorationModel);
                notifyRecyclerLastChanged();
            }
        }, new RongIMClient.ResultCallback<Message>() {
            @Override
            public void onSuccess(Message message) {

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }

    private void sendSilentMessage() {
        GroupMemberModel memberModel = new GroupMemberModel(selfModel.name, selfModel.name, selfModel.uid, GroupConstant.MemberType.NEWBIE, selfModel.avatar_thumbnail);
        String rst = selfModel.name + "已进入群组";
        sendDecorationMessage(rst, memberModel);
    }

    private void clearGroupMessageBadge() {
        Subscription subscription = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                MsgDBCenterService.getInstance().clearBadgeByUid(groupModel.group_id);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(aBoolean -> {
        });
        mSubscriptions.add(subscription);
    }


    private void showMemberTipsIfNeed(String member_type, String lable) {
        if (GroupConstant.MemberType.NEWBIE.equals(member_type)) {
            View v_member_type_tips = findViewById(R.id.v_member_type_tips);
            if (v_member_type_tips.getVisibility() == View.VISIBLE) {
                return;
            }
            TextView tv_lable = (TextView) v_member_type_tips.findViewById(R.id.tv_lable);
            tv_lable.setText(lable);
            v_member_type_tips.setVisibility(View.VISIBLE);
            v_member_type_tips.postDelayed(() -> v_member_type_tips.animate().alpha(0f).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    v_member_type_tips.setVisibility(View.GONE);
                    v_member_type_tips.setAlpha(1.0f);
                }
            }).start(), 5000);
            v_member_type_tips.setOnClickListener(v -> {
                Intent intent = new Intent(this, GlobalWebViewActivity.class);
                intent.putExtra(GlobalWebViewActivity.URL, "https://h5.peiwoapi.com/h5/group/grouprule.html");
                startActivity(intent);
            });
        }
    }

    private void showKickoutTipsIfNeed() {
        if (!GroupConstant.MemberType.ALIEN.equals(groupModel.member_type))
            return;
        View v_member_type_tips = findViewById(R.id.v_member_type_tips);
        if (v_member_type_tips.getVisibility() == View.VISIBLE) {
            return;
        }
        v_member_type_tips.setVisibility(View.VISIBLE);
        v_member_type_tips.findViewById(R.id.tv_how_tobe_member).setVisibility(View.GONE);
        TextView tv_lable = (TextView) v_member_type_tips.findViewById(R.id.tv_lable);
        tv_lable.setText(getResources().getString(R.string.you_had_been_kickout));
    }

    private void expressionClick(ExpressionBaseModel model) {
        if (model instanceof GIFModel) {
            sendGIFMessage((GIFModel) model);
        } else if (model instanceof EmotionModel) {
            et_message_input.setTextEmotion((EmotionModel) model);
        }
    }

    private void expressionDeleteClick() {
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
        et_message_input.onKeyDown(KeyEvent.KEYCODE_DEL, event);
    }

    private void startShowAlbum() {
        changeViewTextInputMode();
        detectorCompat.interceptBackPress();
        Intent intent = new Intent(this, MsgShowAlbumActvity.class);
        MsgImageKeeper.getInstance().addAll(v_bottom_panel.getImageQuickSwitchView().getSelectedImages());
        intent.putExtra(MsgShowAlbumActvity.ALBUM_SHOW_MODE, MsgShowAlbumActvity.ALBUM_SHOW_TILED);
        startActivityForResult(intent, SELECT_MSG_IMG_OK);
    }

    private void chooseMoreImage() {
        changeViewTextInputMode();
        detectorCompat.interceptBackPress();
        Intent intent = new Intent(this, AlbumCompatActivity.class);
        intent.putExtra(AlbumCompatActivity.CHOOSE_MODE, AlbumCompatActivity.CHOOSE_MODE_SECTION);
        intent.putExtra(AlbumCompatActivity.K_ALBUM_RST_COUNT, 5);
        startActivityForResult(intent, REQUEST_CODE_MORE_IMAGE);
    }

    private void loadHistoryMessages() {
        int page_size;
        int unread_count = getIntent().getIntExtra(K_UNREAD_COUNT, 0);
        if (unread_count < PAGE_SIZE) page_size = PAGE_SIZE;
        else page_size = unread_count > 200 ? 200 : unread_count;
        RongIMClient.getInstance().getLatestMessages(Conversation.ConversationType.GROUP, groupModel.group_id, page_size, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messageList) {
                if (messageList != null && messageList.size() > 0) {
                    ifHasMoreMessage(messageList.size(), page_size);
                    List<GroupMessageBaseModel> models = RongMessageParse.parseList(messageList);
                    mList.addAll(models);
                    mAdapter.notifyDataSetChanged();
                    last_msg_count = mList.size();
                    showUnreadMessageIfNeed(unread_count);
                } else {
                    ifHasMoreMessage(0, page_size);
                }
                setReceiveRongMessageListener();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                ifHasMoreMessage(0, page_size);
                setReceiveRongMessageListener();
            }
        });
    }

    private void showUnreadMessageIfNeed(int unread_count) {
        //超过200条未读不显示 →_→
        if (unread_count >= 200) {
            return;
        }
//        v_recycler_message.post(() -> {
//            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) v_recycler_message.getLayoutManager();
//            int first_visibile_position = linearLayoutManager.findFirstVisibleItemPosition();
//            if (unread_count > mList.size() - first_visibile_position) {
//                TextView tv_jump_unread_message = (TextView) findViewById(R.id.tv_jump_unread_message);
//                tv_jump_unread_message.setVisibility(View.VISIBLE);
//                tv_jump_unread_message.setText(unread_count + "条消息");
//                tv_jump_unread_message.setTag(unread_count);
//            }
//        });
    }

    private void ifHasMoreMessage(int size, int request_size) {
        has_more = size >= request_size;
    }

    private void setReceiveRongMessageListener() {
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        app.addReceiveRongMessageListener(this);
    }

    private void setDetectorListeners() {
        detectorCompat.setTextMessageButtonPerformListener(() -> {
            changeViewTextInputMode();
            scrollRecyclerToBottom();
        });
        detectorCompat.setEmotionButtonPerformListener(isEmotionViewShown -> {
            changeViewEmotionMode(isEmotionViewShown);
            scrollRecyclerToBottom();
        });
        detectorCompat.setImageButtonPerformListener(isEmotionViewShown -> {
            if (changeViewImageQuickSwitchMode(isEmotionViewShown)) {
                scrollRecyclerToBottom();
            }
        });
    }

    private void scrollRecyclerToBottom() {
        v_recycler_message.post(() -> {
            if (mList.size() > 0) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) v_recycler_message.getLayoutManager();
                linearLayoutManager.scrollToPosition(mList.size() - 1);
            }
            auto_scroll_to_last = true;
        });
    }

    private boolean changeViewImageQuickSwitchMode(boolean isEmotionViewShown) {
        if (!checkSendImage()) {
            return false;
        }
        if (isEmotionViewShown) {
            v_image_quick_switch_start.setSelected(true);
            v_input_txt_start.setSelected(false);
            v_expression_start.setSelected(false);
            v_bottom_panel.showImageQuickSwitchView();
        } else {
            v_input_txt_start.setSelected(true);
            v_expression_start.setSelected(false);
            v_image_quick_switch_start.setSelected(false);
        }
        return true;
    }

    private void changeViewEmotionMode(boolean isEmotionViewShown) {
        if (isEmotionViewShown) {
            v_expression_start.setSelected(true);
            v_input_txt_start.setSelected(false);
            v_image_quick_switch_start.setSelected(false);
            v_bottom_panel.showExpressionView();
        } else {
            v_input_txt_start.setSelected(true);
            v_expression_start.setSelected(false);
            v_image_quick_switch_start.setSelected(false);
        }
    }

    private void changeViewTextInputMode() {
        v_input_txt_start.setSelected(true);
        v_expression_start.setSelected(false);
        v_image_quick_switch_start.setSelected(false);
    }

//    private boolean check() {
//        //不再校验是否获取到member info，获取不到当群成员处理
//        if (groupMemberModel == null) {
//            Snackbar.make(v_recycler_message, "获取用户信息失败", Snackbar.LENGTH_SHORT).show();
//            return false;
//        }
//        return true;
//    }

    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.v_send_message:
                //if (check())
                sendTextOrImageMessageAction();
                break;

            case R.id.btn_left:
                finish();
                break;
            case R.id.iv_im_avatar:
                detectorCompat.interceptBackPress();
                enterGroupHomePageVerifi();
                break;
            case R.id.v_redbag_start:
                MsgImageKeeper.getInstance().clear();
                detectorCompat.interceptBackPress();
                changeViewTextInputMode();
                enterRedBagPagerVerifi();
                break;
            case R.id.tv_jump_unread_message:
                int unread_count = (int) v.getTag();
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) v_recycler_message.getLayoutManager();
                int offset_position = mList.size() - unread_count;
                if (offset_position >= 0 && offset_position < mList.size()) {
                    linearLayoutManager.scrollToPosition(offset_position);
                }
                animateUnreadMessageView(v);
                break;
        }
    }

    private void animateUnreadMessageView(View v) {
        v.animate().translationX(v.getWidth()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }

    private void enterRedBagPagerVerifi() {
        String member_type = groupMemberModel == null ? groupModel.member_type : groupMemberModel.member_type;
        if (GroupConstant.MemberType.ALIEN.equals(member_type)) {
            Snackbar.make(v_recycler_message, "您没有权限发红包", Snackbar.LENGTH_SHORT).show();
            return;
        }
        Intent redbag_intent = new Intent(this, GroupChatRedbagActivity.class);
        redbag_intent.putExtra(GroupChatRedbagActivity.K_REDBAG_TYPE, GroupChatRedbagActivity.REDBAG_TYPE_PERSONAL);
        redbag_intent.putExtra(K_GROUP_DATA, groupModel);
        startActivityForResult(redbag_intent, REQUEST_CODE_REDBAG);
    }

    private void enterGroupHomePageVerifi() {
        String memberType;
        if (groupMemberModel == null) {
            memberType = groupModel.member_type;
        } else {
            memberType = groupMemberModel.member_type;
        }
        if (GroupConstant.MemberType.ALIEN.equals(memberType)) {
            Snackbar.make(v_recycler_message, "您没有权限查看群主页", Snackbar.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, GroupHomePageActvity.class);
        intent.putExtra(GroupHomePageActvity.KEY_GROUP_ID, groupModel.group_id);
        intent.putExtra(K_GROUP_DATA, groupModel);
        startActivityForResult(intent, REQUEST_GROUP_HOMEPAGE);
    }

    private void sendTextOrImageMessageAction() {
        if (v_bottom_panel.isImageQuickSwitchShown()) {
            List<String> image_paths = v_bottom_panel.getImageQuickSwitchView().getSelectedImages();
            int size = image_paths.size();
            for (int i = 0; i < size; i++) {
                String imgUrl = image_paths.get(i);
                if (MsgImageKeeper.getInstance().getImgList().size() == 0 || !MsgImageKeeper.getInstance().contains(imgUrl)) {
                    MsgImageKeeper.getInstance().getImgList().add(imgUrl);
                }
            }
            List<String> sendImgUrls = new ArrayList<>();
            sendImgUrls.addAll(MsgImageKeeper.getInstance().getImgList());
            detectorCompat.interceptBackPress();
            changeViewTextInputMode();
            if (sendImgUrls.size() > 0) {
                sendMultiImageMessage(sendImgUrls);
            }
//            detectorCompat.interceptBackPress();
//            changeViewTextInputMode();
//            if (image_paths.size() > 0) {
//                sendMultiImageMessage(image_paths);
//            }
        } else {
            String text = et_message_input.getText().toString();
            sendSingleTextMessage(text);
        }
    }

    private boolean checkSendImage() {
        String member_type = groupMemberModel == null ? groupModel.member_type : groupMemberModel.member_type;
        if (GroupConstant.MemberType.NEWBIE.equals(member_type)) {
            showMemberTipsIfNeed(GroupConstant.MemberType.NEWBIE, "成为群成员开启发送图片权限");
            return false;
        }
        if (GroupConstant.MemberType.ALIEN.equals(member_type)) {
            Snackbar.make(v_recycler_message, "您已不是该群成员", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void sendMultiImageMessage(List<String> selectedImages) {
        showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PWUploader.K_UPLOAD_TYPE, PWUploader.UPLOAD_TYPE_AVATAR));
        params.add(new BasicNameValuePair("n", String.valueOf(selectedImages.size())));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_QINIU_TOKENS, new MsgStructure() {
            @Override
            public boolean onInterceptRawData(String rawStr) {
                QNImageToken imageToken = JSON.parseObject(rawStr, QNImageToken.class);
                Observable.just(imageToken).observeOn(AndroidSchedulers.mainThread()).subscribe(qnImageToken -> {
                    dismissAnimLoading();
                    compressImageAndSendImageMessage(selectedImages, qnImageToken.data);
                });
                return true;
            }

            @Override
            public void onReceive(JSONObject data) {

            }

            @Override
            public void onError(int error, Object ret) {
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> dismissAnimLoading());
            }
        });
    }

    private void compressImageAndSendImageMessage(List<String> selectedImages, List<QNImageToken.QNToken> tokens) {
        Observable<QNImageToken.QNToken> o_tokens = Observable.from(tokens);
        Observable<ImageItem> observable = ChatImageWrapper.createCompressObservable(selectedImages);
        Subscription subscription = Observable.zip(o_tokens, observable, (qnToken, imageItem) -> {
            if (imageItem != null) {
                imageItem.qn_token = qnToken.token;
                imageItem.qn_key = qnToken.key;
                imageItem.qn_thumbnail_url = qnToken.thumbnail_url;
                imageItem.qn_url = qnToken.url;
            }
            return imageItem;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(rst_image -> {
            if (rst_image != null) {
                sendSingleImageMessage(rst_image, false, -1);
            }
        });
        mSubscriptions.add(subscription);
    }

    private void sendSingleImageMessage(ImageItem imageItem, boolean resend, int recend_location) {
        String body = RongMessageParse.encodeImageMessageBody(imageItem, selfModel, groupModel, groupMemberModel);
        GroupMessageModel messageModel = new GroupMessageModel();

        GroupMessageImageModel model = RongMessageParse.decodeImageObjectSelf(body, messageModel.hashCode(), GroupMessageImageModel.class);
        if (resend && recend_location >= 0 && recend_location < mList.size()) {
            mList.set(recend_location, model);
            mAdapter.notifyItemChanged(recend_location);
        } else {
            mList.add(model);
            notifyRecyclerLastChanged();
        }
        PWUploader uploader = PWUploader.getInstance();
        uploader.add(imageItem.sourcePath, imageItem.qn_key, imageItem.qn_token, new UploadCallback() {
            @Override
            public void onComplete(String key, ResponseInfo responseInfo, JSONObject jsonObject) {
                imageItem.imageKey = key;
                imageItem.thumbnailPath = imageItem.qn_thumbnail_url;
                imageItem.sourcePath = imageItem.qn_url;
                String body = RongMessageParse.encodeImageMessageBody(imageItem, selfModel, groupModel, groupMemberModel);
                messageModel.setBody(body);
                sendMessageWithCallback(messageModel, model.extra.pw_description);
            }

            @Override
            public void onFailure(String key, ResponseInfo responseInfo, JSONObject jsonObject) {
                updateSingleItemWithError(messageModel.hashCode(), messageModel.hashCode());
            }
        });
    }

    private void sendSingleTextMessage(String text) {
        if (text.length() == 0) {
            Snackbar.make(et_message_input, "不能发送空消息", Snackbar.LENGTH_SHORT).show();
        } else if (text.length() > 1000) {
            Snackbar.make(et_message_input, "消息过长", Snackbar.LENGTH_SHORT).show();
        } else {
            et_message_input.setText(null);
            String body = RongMessageParse.encodeTextMessageBody(text, selfModel, groupModel, groupMemberModel, atUsers);
            GroupMessageModel messageModel = new GroupMessageModel(body);
            GroupMessageTextModel model = RongMessageParse.decodeTextObjectSelf(body, messageModel.hashCode(), GroupMessageTextModel.class);
            mList.add(model);
            notifyRecyclerLastChanged();

            //Log.i("rongs", "encode body == " + body);
            sendMessageWithCallback(messageModel, model.extra.pw_description);
            if (atUsers != null) atUsers.clear();
        }
    }

    private void sendRedBagMessage(PacketIconModel packetIconModel, int bag_type) {
        String body = RongMessageParse.encodeRedBagMessageBody(packetIconModel, bag_type, selfModel, groupModel, groupMemberModel);
        GroupMessageModel messageModel = new GroupMessageModel(body);
        GroupMessageRedBagModel model = RongMessageParse.decodeRedbagObjectSelf(body, messageModel.hashCode(), GroupMessageRedBagModel.class);
        mList.add(model);
        notifyRecyclerLastChanged();

        //Log.i("rongs", "encode body == " + body);
        sendMessageWithCallback(messageModel, model.extra.pw_description);
    }

    private void sendRepuRedBagMessage(PacketIconModel packetIconModel) {
        String body = RongMessageParse.encodeRepuRedBagMessageBody(packetIconModel, selfModel, groupModel, groupMemberModel);
        GroupMessageModel messageModel = new GroupMessageModel(body);
        GroupMessageRepuRedBagModel model = RongMessageParse.decodeRepuRedbagObjectSelf(body, messageModel.hashCode(), GroupMessageRepuRedBagModel.class);
        mList.add(model);
        notifyRecyclerLastChanged();

        //Log.i("rongs", "encode body == " + body);
        sendMessageWithCallback(messageModel, model.extra.pw_description);
    }

    private void sendGIFMessage(GIFModel gifModel) {
        String body = RongMessageParse.encodeGIFMessageBody(gifModel, selfModel, groupModel, groupMemberModel);
        GroupMessageModel messageModel = new GroupMessageModel(body);
        GroupMessageGIFModel model = RongMessageParse.decodeGIFObjectSelf(body, messageModel.hashCode(), GroupMessageGIFModel.class);
        mList.add(model);
        notifyRecyclerLastChanged();

        //Log.i("rongs", "encode body == " + body);
        sendMessageWithCallback(messageModel, model.extra.pw_description);
    }

    private void recendMessage(GroupMessageBaseModel model, int location) {
        if (model instanceof GroupMessageImageModel) {
            GroupMessageImageModel groupMessageImageModel = ((GroupMessageImageModel) model);
            if (!groupMessageImageModel.image.thumbnail_url.startsWith("http")) {
                //图片未上传成功
                ImageItem imageItem = new ImageItem(groupMessageImageModel.image.thumbnail_url, groupMessageImageModel.image.thumbnail_url, groupMessageImageModel.image.width, groupMessageImageModel.image.height);
                sendSingleImageMessage(imageItem, true, location);
                return;
            }
        }
        int message_id = model.message_id;
        String body = RongMessageParse.parseRecendMessage(model);
        GroupMessageModel messageModel = new GroupMessageModel(body);
        model.send_status = GroupConstant.SendStatus.SENDING;
        model.message_id = messageModel.hashCode();
        mAdapter.notifyItemChanged(location);
        sendMessageWithCallback(messageModel, model.extra.pw_description);
        RongIMClient.getInstance().deleteMessages(new int[]{message_id}, null);
    }

    private static class RongSendMessageback extends RongIMClient.SendMessageCallback {
        private WeakReference<GroupChatActivity> activity_ref;
        private int current_hashcode;

        public RongSendMessageback(GroupChatActivity activity, int current_hashcode) {
            activity_ref = new WeakReference<>(activity);
            this.current_hashcode = current_hashcode;
        }


        @Override
        public void onError(Integer messageId, RongIMClient.ErrorCode errorCode) {
            GroupChatActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) return;
            theActivity.updateSingleItemWithError(current_hashcode, messageId);
            if (errorCode == RongIMClient.ErrorCode.NOT_IN_GROUP) {
                Snackbar.make(theActivity.v_recycler_message, "您不在这个群组", Snackbar.LENGTH_SHORT).show();
            }
//            if (theActivity.show_errorcode)
//                Snackbar.make(theActivity.v_recycler_message, "SendMessageCallback errcode == " + errorCode, Snackbar.LENGTH_LONG).show();
            if (BuildConfig.DEBUG) {
                Log.i("rongs", "error code=" + errorCode.getValue() + " -- group_id=" + theActivity.groupModel.group_id);
            }
        }

        @Override
        public void onSuccess(Integer messageId) {
            GroupChatActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) return;
            theActivity.updateSingleItemWithSuccess(current_hashcode, messageId);
            if (BuildConfig.DEBUG)
                Log.i("rongs", "SendMessageCallback onSuccess");
        }
    }

    private void sendMessageWithCallback(GroupMessageModel messageModel, String pushContent) {
        //pushContent = String.format("%s:%s", groupMemberModel == null ? selfModel.name : groupMemberModel.name, pushContent);
        RongIMClient.getInstance().sendMessage(Conversation.ConversationType.GROUP, groupModel.group_id, messageModel, null, null, new RongSendMessageback(this, messageModel.hashCode()), null);
        if (BuildConfig.DEBUG)
            Log.i("rongs", "send body = " + messageModel.getBody());
    }

    private void updateSingleItemWithSuccess(int temp_message_id, Integer messageId) {
        for (int i = mList.size() - 1; i >= 0; i--) {
            GroupMessageBaseModel model = mList.get(i);
            if (model.message_id == temp_message_id) {
                model.message_id = messageId;
                model.send_status = GroupConstant.SendStatus.SUCCESS;
                mAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void updateSingleItemWithError(int temp_message_id, int messageId) {
        for (int i = mList.size() - 1; i >= 0; i--) {
            GroupMessageBaseModel model = mList.get(i);
            if (model.message_id == temp_message_id) {
                model.message_id = messageId;
                model.send_status = GroupConstant.SendStatus.ERROR;
                mAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void notifyRecyclerLastChanged() {
        mAdapter.notifyItemInserted(mAdapter.getItemCount());
        if (auto_scroll_to_last) {
            v_recycler_message.smoothScrollToPosition(mAdapter.getItemCount());
        }
    }

    @Override
    public void onBackPressed() {
        changeViewTextInputMode();
        if (!detectorCompat.interceptBackPress()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_MORE_IMAGE:
                    ArrayList<String> items = data.getStringArrayListExtra(AlbumCompatActivity.K_ALBUM_RST);
                    sendMultiImageMessage(items);
                    break;
                case REQUEST_CODE_AT_MEMBER:
                    //@
                    GroupMemberModel memberModel = data.getParcelableExtra(K_MEMBER_DATA);
                    if (memberModel != null) {
                        et_message_input.setText(et_message_input.getText() + memberModel.nickname + " ");
                        et_message_input.setSelection(et_message_input.getText().length());
                        addAtUsers(new GroupBaseUserModel(String.valueOf(memberModel.uid), memberModel.name, memberModel.nickname, 0, memberModel.avatar, memberModel.member_type));
                    }
                    break;
                case REQUEST_CODE_REDBAG:
                    //发红包
                    auto_scroll_to_last = true;
                    handleSendRedBag(data, GroupChatRedbagActivity.REDBAG_TYPE_PERSONAL);
                    break;
                case SELECT_MSG_IMG_OK:
                    ArrayList<String> msgUrls = data.getStringArrayListExtra(MsgShowAlbumActvity.MSG_IMG_URLS);
                    sendMultiImageMessage(msgUrls);
                    MsgImageKeeper.getInstance().clear();
                    v_bottom_panel.getImageQuickSwitchView().clearSelectedUrls();
                    break;
            }
        } else if (resultCode == GroupHomePageActvity.RESULT_GROUP_REDBAG) {
            //群红包
            auto_scroll_to_last = true;

            handleSendRedBag(data, GroupChatRedbagActivity.REDBAG_TYPE_GROUP);
        } else if (resultCode == GroupHomePageActvity.RESULT_GROUP_REPU_REDBAG) {
            //群声望红包
            auto_scroll_to_last = true;
            handleRepuRedbag(data);
        } else if (resultCode == GroupHomePageActvity.RESULT_GROUP_UPDATE) {
            TabfindGroupModel model = data.getParcelableExtra(K_GROUP_DATA);
            groupModel.group_name = model.group_name;
            groupModel.avatar = model.avatar;
//            groupModel.member_counts = model.member_counts;
            setTitle(model.group_name);
            ImageLoader.getInstance().displayImage(model.avatar, iv_im_avatar);
            if (!TextUtils.isEmpty(model.msg1)) {
                sendDecorationMessage(model.msg1, groupMemberModel);
            }
            if (!TextUtils.isEmpty(model.msg2)) {
                sendDecorationMessage(model.msg2, groupMemberModel);
            }
            //加发一条修改群资料的命令消息
            String c_data = RongMessageParse.encodeUpdateGroupCommandMessageData(groupModel.group_name, groupModel.avatar, selfModel.uid);
            sendCommandMessage(GroupConstant.CMD_NAME.UPDATE, c_data);
        } else if (resultCode == GroupHomePageActvity.RESULT_QUIT_AND_DELETE_GROUP) {
            if (mList != null)
                mList.clear();
            Intent it = getIntent();
//            it.putExtra("msg_id", groupModel.group_id);
            setResult(RESULT_OK, it);
            finish();
        }
    }


    private void handleSendRedBag(Intent data, int bag_type) {
        PacketIconModel packetIconModel = data.getParcelableExtra(GroupChatRedbagActivity.K_SINGLE_PACKET);
        if (packetIconModel != null) {
            sendRedBagMessage(packetIconModel, bag_type);
        }
    }

    private void handleRepuRedbag(Intent data) {
        PacketIconModel packetIconModel = data.getParcelableExtra(GroupChatRedbagActivity.K_SINGLE_PACKET);
        if (packetIconModel != null) {
            sendRepuRedBagMessage(packetIconModel);
        }
    }


    @Override
    public void onReceiveRongMessage(Message message, int integer) {
        if (!groupModel.group_id.equals(message.getTargetId())) {
            return;
        }
        MessageContent content = message.getContent();
        if (BuildConfig.DEBUG) {
            if (content instanceof GroupMessageModel) {
                String body = ((GroupMessageModel) content).getBody();
                Log.i("rongs", "receive rong == " + body);
            }
        }
        //Log.i("rec", "loop == " + (Looper.myLooper() == Looper.getMainLooper()));
        Observable.just(content).observeOn(AndroidSchedulers.mainThread()).subscribe(messageContent -> {
            if (messageContent instanceof GroupMessageModel) {
                GroupMessageBaseModel baseModel = RongMessageParse.parseReceiveMessage(message, integer);
                if (baseModel != null) {
                    mList.add(baseModel);
                    notifyRecyclerLastChanged();
                }
            } else if (messageContent instanceof CommandMessage) {
                //命令消息
                handleRongCommandMessage((CommandMessage) messageContent);
            }
        });
    }

    private void handleRongCommandMessage(CommandMessage commandMessage) {
        String name = commandMessage.getName();
        if (TextUtils.isEmpty(name)) return;
        switch (name) {
            case GroupConstant.CMD_NAME.REGULAR:
                //转正此人
                receiveReqularCommand(commandMessage.getData());
                break;
            case GroupConstant.CMD_NAME.UPDATE:
                //修改群资料
                receiveUpdateGroupInfoCommand(commandMessage.getData());
                break;
            default:
                break;
        }
    }

    private void receiveUpdateGroupInfoCommand(String data) {
        if (TextUtils.isEmpty(data)) return;
        GroupCommandData commandData = JSON.parseObject(data, GroupCommandData.class);
        //过期
        if (System.currentTimeMillis() - commandData.expired_at > 0) return;
        if (commandData.group != null) {
            String group_name = commandData.group.group_name;
            String group_avatar = commandData.group.avatar;
            if (!groupModel.group_name.equals(group_name)) {
                setTitle(group_name);
            }
            if (!groupModel.avatar.equals(group_avatar)) {
                ImageLoader.getInstance().displayImage(group_avatar, iv_im_avatar);
            }
        }
    }

    private void receiveReqularCommand(String data) {
        if (TextUtils.isEmpty(data)) return;
        GroupCommandData commandData = JSON.parseObject(data, GroupCommandData.class);
        //过期
        if (System.currentTimeMillis() - commandData.expired_at > 0) return;
        for (GroupCommandData.TUser tuser : commandData.target_ids) {
            if (selfModel.uid == tuser.uid) {
                //我是游客，被转正
                groupMemberModel.member_type = GroupConstant.MemberType.MEMBER;
                //打开发图片权限
                v_bottom_panel.setConsuming(false);
                //showToast(this, "游客转正");
                break;
            }
        }
    }


    private void loadMoreEarlierMessage() {
        int oldestMessageId = 0;
        if (isloading && mList.size() > 1) {
            oldestMessageId = mList.get(1).message_id;
        }
        if (BuildConfig.DEBUG)
            Log.i("rongs", "oldestMessageId == " + oldestMessageId);
        RongIMClient.getInstance().getHistoryMessages(Conversation.ConversationType.GROUP, groupModel.group_id, oldestMessageId, PAGE_SIZE, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (isloading) {
                    if (messages != null) {
                        //Log.i("rongs", "message size == " + messages.size());
                        ifHasMoreMessage(messages.size(), PAGE_SIZE);
                        List<GroupMessageBaseModel> models = RongMessageParse.parseList(messages);
                        RongMessageParse.reverseData(models);
                        isloading = false;
                        mList.remove(0);
                        mAdapter.notifyItemRemoved(0);
                        mList.addAll(0, models);
                        final int curr_location = models.size();
                        mAdapter.notifyDataSetChanged();
                        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) v_recycler_message.getLayoutManager();
                        linearLayoutManager.scrollToPositionWithOffset(curr_location, 0);
                    } else {
                        ifHasMoreMessage(0, PAGE_SIZE);
                        isloading = false;
                        mList.remove(0);
                        mAdapter.notifyItemRemoved(0);
                    }
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                if (isloading) {
                    isloading = false;
                    mList.remove(0);
                    mAdapter.notifyItemRemoved(0);
                }
            }
        });
    }

    private void setRecyclerViewScrollListener() {
        v_recycler_message.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (has_more) {
                        if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                            if (!isloading) {
                                isloading = true;
                                GroupMessageBaseModel model = new GroupMessageBaseModel();
                                model.dialog_type = GroupConstant.MessageType.TYPE_HEADER;
                                mList.add(0, model);
                                mAdapter.notifyItemInserted(0);
                                recyclerView.scrollToPosition(0);
                                recyclerView.postDelayed(GroupChatActivity.this::loadMoreEarlierMessage, 500);
                                if (BuildConfig.DEBUG) {
                                    Log.i("rongs", "load more message");
                                }
                            }
                        }
                    }
                    int last_visible_position = layoutManager.findLastVisibleItemPosition();
                    auto_scroll_to_last = mList.size() == 0 || last_visible_position == mList.size() - 1;
                    if (BuildConfig.DEBUG)
                        Log.i("rongs", "auto_scroll_to_last == " + auto_scroll_to_last);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }
        });
    }

    private void handleUserAvatarLongClick(GroupMessageBaseModel model, int location) {
//        et_message_input.setText(et_message_input.getText() + "@" + model.user.name + " ");
        if (!TextUtils.isEmpty(model.user.nickname)) {
            et_message_input.setText(et_message_input.getText() + "@" + model.user.nickname + " ");
        } else {
            et_message_input.setText(et_message_input.getText() + "@" + model.user.name + " ");
        }
        et_message_input.setSelection(et_message_input.getText().length());
        addAtUsers(model.user);
    }

    private void handMessageOnLongClick(GroupMessageBaseModel model, int location) {
        if (model.send_status == GroupConstant.SendStatus.SENDING) {
            return;
        }
        switch (model.dialog_type) {
            case GroupConstant.MessageType.TYPE_TEXT:
                handleTextTypeAction(model, location);
                break;
            case GroupConstant.MessageType.TYPE_IMAGE:
                handleImageTypeAction(model, location);
                break;
            case GroupConstant.MessageType.TYPE_REDBAG:
            case GroupConstant.MessageType.TYPE_REPUTATION_REDBAG:
                handleRedbagTypeAction(model, location);
                break;
            case GroupConstant.MessageType.TYPE_GIF:
                //handle the
                handleGifTypeAction(model, location);
                break;
            default:
                break;
        }
    }

    private void handleRedbagTypeAction(GroupMessageBaseModel model, int location) {
        new AlertDialog.Builder(this)
                .setTitle("操作")
                .setItems(new String[]{"删除", "取消"}, (dialog, which) -> {
                    if (which == 0) {
                        deleteSingleRongMessage(model, location);
                    }
                })
                .create().show();
    }

    private void handleImageTypeAction(GroupMessageBaseModel model, int location) {
        GroupMessageImageModel imageModel = (GroupMessageImageModel) model;
        File src = DiskCacheUtils.findInCache(imageModel.image.image_url, ImageLoader.getInstance().getDiskCache());
        if (src == null || !src.exists() || src.length() == 0) {
            src = DiskCacheUtils.findInCache(imageModel.image.thumbnail_url, ImageLoader.getInstance().getDiskCache());
        }
        final File f_src = src;
        String[] items;
        if (f_src != null && f_src.exists() && f_src.length() > 0) {
            items = new String[]{"保存图片", "删除", "取消"};
        } else {
            items = new String[]{"删除", "取消"};
        }
        new AlertDialog.Builder(this).setTitle("操作").setItems(items, (dialog, which) -> {
            if (items.length == 3) {
                if (which == 0) {
                    saveImageToStorage(f_src);
                } else if (which == 1) {
                    removeSingleImageMessage(model, location, imageModel);
                }
            } else {
                if (which == 0) {
                    removeSingleImageMessage(model, location, imageModel);
                }
            }
        }).create().show();
    }

    private void removeSingleImageMessage(GroupMessageBaseModel model, int location, GroupMessageImageModel imageModel) {
        if (imageModel.image.image_url.startsWith("http")) {
            deleteSingleRongMessage(model, location);
        } else {
            mList.remove(location);
            mAdapter.notifyItemRemoved(location);
        }
    }

    private void saveImageToStorage(File src) {
        Toast.makeText(this, "正在保存...", Toast.LENGTH_SHORT).show();
        File dst = new File(FileManager.getChatImageCopyPath(), String.format("%s.jpg", Md5Util.getMd5code(src.getAbsolutePath())));
        if (dst.exists() && dst.length() > 0) {
            Toast.makeText(this, "图片已存在" + dst.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            return;
        }
        Observable<Boolean> observable = FileManager.copyFile(src, dst);
        Subscription subscription = observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(GroupChatActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    String rst_path = dst.getAbsolutePath();
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + rst_path)));
                    Toast.makeText(GroupChatActivity.this, "图片已保存在" + rst_path, Toast.LENGTH_SHORT).show();
                }
            }
        });
        mSubscriptions.add(subscription);
    }

    private void handleGifTypeAction(GroupMessageBaseModel model, int location) {
        String[] items;
        items = new String[]{"删除", "取消"};
        GroupMessageGIFModel gifModel = (GroupMessageGIFModel) model;
        new AlertDialog.Builder(this).setTitle("操作").setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        deleteSingleRongMessage(model,location);
                        break;
                }
            }
        }).create().show();

    }

    private void handleTextTypeAction(GroupMessageBaseModel model, int location) {
        boolean has_power = hasPower(model);
        String[] items;
        if (has_power) {
            items = new String[]{"转正此人", "复制", "删除", "取消"};
        } else {
            items = new String[]{"复制", "删除", "取消"};
        }
        GroupMessageTextModel textModel = (GroupMessageTextModel) model;
        new AlertDialog.Builder(this).setTitle("操作").setItems(items, (dialog, which) -> {
            if (has_power) {
                if (which == 0) {
                    regularThisGuy(model.user);
                } else if (which == 1) {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, textModel.text.content));
                } else if (which == 2) {
                    deleteSingleRongMessage(model, location);
                }
            } else {
                if (which == 0) {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, textModel.text.content));
                } else if (which == 1) {
                    deleteSingleRongMessage(model, location);
                }
            }
        }).create().show();
    }

    private boolean hasPower(GroupMessageBaseModel model) {
        return GroupConstant.MemberType.NEWBIE.equals(model.user.member_type) && (GroupConstant.MemberType.ADMIN.equals(groupMemberModel.member_type) || GroupConstant.MemberType.MEMBER.equals(groupMemberModel.member_type));
    }

    //转正此人
    private void regularThisGuy(GroupBaseUserModel userModel) {
        showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("group_id", groupModel.group_id));
        params.add(new BasicNameValuePair("member_ids", userModel.uid));
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_GROUP_MEMBERS, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Subscription subscription = Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    Snackbar.make(v_recycler_message, "转正此人成功", Snackbar.LENGTH_SHORT).show();
                    String decoration = groupMemberModel.nickname + " 已邀请 " + userModel.nickname + " 加入群组";
                    sendDecorationMessage(decoration, groupMemberModel);
                    regularThisGuyByRong(userModel);
                });
                mSubscriptions.add(subscription);
            }

            @Override
            public void onError(int error, Object ret) {
                Subscription subscription = Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
                    dismissAnimLoading();
                    Snackbar.make(v_recycler_message, "转正此人失败", Snackbar.LENGTH_SHORT).show();
                });
                mSubscriptions.add(subscription);
            }
        });
    }

    private void regularThisGuyByRong(GroupBaseUserModel userModel) {
        String data = RongMessageParse.encodeRegularCommandMessageData(Integer.valueOf(userModel.uid), selfModel.uid);
        sendCommandMessage(GroupConstant.CMD_NAME.REGULAR, data);
    }


    public void sendCommandMessage(String name, String data) {
        if (BuildConfig.DEBUG) Log.i("rcommand", data);
        CommandMessage commandMessage = CommandMessage.obtain(name, data);
        RongIMClient.getInstance().sendMessage(Conversation.ConversationType.GROUP, groupModel.group_id, commandMessage, null, null, null, null);
    }

    private void deleteSingleRongMessage(GroupMessageBaseModel model, final int location) {
        RongIMClient.getInstance().deleteMessages(new int[]{model.message_id}, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    int index = mList.indexOf(model);
                    mList.remove(model);
                    mAdapter.notifyItemRemoved(index);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }

    private void listenEditTextDataChanged() {
        et_message_input.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            if ("@".equals(source.toString())) {
                Intent intent = new Intent(GroupChatActivity.this, GroupMembersNewbiesActivity.class);
                intent.putExtra(GroupHomePageActvity.K_GROUP_DATA, groupModel);
                intent.setAction(GroupMembersNewbiesActivity.ACTION_AT_MEMBER);
                startActivityForResult(intent, REQUEST_CODE_AT_MEMBER);
                //Snackbar.make(v_recycler_message, "@", Snackbar.LENGTH_SHORT).show();
            }
            return null;
        }});
    }


    @Override
    public void finish() {
        if (badgeObserver != null) {
            getContentResolver().unregisterContentObserver(badgeObserver);
            badgeObserver = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mSubscriptions != null && !mSubscriptions.isUnsubscribed()) {
            mSubscriptions.unsubscribe();
        }
        /**/
        EventBus.getDefault().unregister(this);
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        app.unRegisterReceiveRongMessageListener(this);
        /**/
        if (last_msg_count != mList.size() && mList.size() > 0) {// && mList.get(z - 1).direction == GroupConstant.Direction.SELF
            GroupMessageBaseModel baseModel = mList.get(mList.size() - 1);
            if (baseModel != null) {//&& baseModel.direction == GroupConstant.Direction.SELF
                baseModel.group.group_name = groupModel.group_name;
                baseModel.group.avatar = groupModel.avatar;
                MsgDBCenterService.getInstance().updateLatestDialogue(baseModel);
            }
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /********%>_<%********/
        MsgAcceptedMsgActivity.Uid = "0";
        /********%>_<%********/
    }

    public void onEventMainThread(Intent intent) {
        if (intent == null) {
            return;
        }
        int mGroupPeopleNum = intent.getIntExtra("total_number", 0);
        if (mGroupPeopleNum > 0) {
            groupModel.total_number = mGroupPeopleNum;
            title_view.setText(String.format("%s(%d)", groupModel.group_name, mGroupPeopleNum));
        }
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        if (ACTION_POST_MESSAGE.equals(action)) {
            int type = intent.getIntExtra(K_POST_MESSAGE_TYPE, -1);
            String extra = intent.getStringExtra(K_POST_MESSAGE_DATA);
            switch (type) {
                case GroupConstant.MessageType.TYPE_DECORATION:
                    GroupMessageDecorationModel decorationModel = JSON.parseObject(extra, GroupMessageDecorationModel.class);
                    mList.add(decorationModel);
                    notifyRecyclerLastChanged();
                    //加发一条转正此人命令消息
                    regularThisGuyByRong(decorationModel.user);
                    break;

                default:
                    break;
            }
        } else if (ACTION_SHOW_NICKNAME.equals(action)) {
            boolean check = intent.getBooleanExtra(K_SHOW_NICK_NAME, false);
            mAdapter.refreshMemberName(check ? 1 : 0);
            groupMemberModel.nickname = intent.getStringExtra(K_NICK_NAME);
        } else if (ACTION_CLEAR_MESSAGE.equals(action)) {
            mList.clear();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void addAtUsers(GroupBaseUserModel model) {
        if (this.atUsers == null) {
            this.atUsers = new ArrayList<>();
        }
        this.atUsers.add(model);
    }


    private Rect r = new Rect();

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                v_recycler_message.getGlobalVisibleRect(r);
                if (r.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    detectorCompat.interceptBackPress();
                    detectorCompat.hideSoftInput();
                    changeViewTextInputMode();
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
