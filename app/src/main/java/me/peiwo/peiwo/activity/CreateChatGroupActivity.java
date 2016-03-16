package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.qiniu.android.http.ResponseInfo;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import me.peiwo.peiwo.BuildConfig;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.GroupAddFriendsAdapter;
import me.peiwo.peiwo.callback.UploadCallback;
import me.peiwo.peiwo.constans.GroupConstant;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.db.BriteDBHelperHolder;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.model.GroupMemberModel;
import me.peiwo.peiwo.model.GroupMessageModel;
import me.peiwo.peiwo.model.ImageModel;
import me.peiwo.peiwo.model.PWContactsModel;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.model.TabfindGroupModel;
import me.peiwo.peiwo.model.groupchat.GroupMessageDecorationModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.PWUploader;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.ImageUtil;
import me.peiwo.peiwo.util.PinYin;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.util.group.RongMessageParse;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by gaoxiang on 2015/12/8.
 */
public class CreateChatGroupActivity extends BaseActivity implements TextWatcher {

    private ArrayList<PWContactsModel> mList = new ArrayList<>();
    private SORT_TYPE curr_sort_type = SORT_TYPE.SOUR_BY_ACTIVE_TIME;
    public static final String ACTION_INVOLVE_MEMBER = "me.peiwo.peiwo.INVOLVE_MEMBER";
    private GroupAddFriendsAdapter mAdapter;
    @Bind(R.id.v_choose_friends_list)
    RecyclerView mRecyclerview;
    @Bind(R.id.tv_group_name_list)
    TextView tv_name_list;
    @Bind(R.id.et_group_name)
    EditText et_group_name;
    @Bind(R.id.et_search_name)
    EditText et_search_name;
    @Bind(R.id.civ_choose_album)
    ImageView civ_choose_album;
    @Bind(R.id.layout_group_avatar_name)
    LinearLayout layout_group_avatar_name;
    @Bind(R.id.layout_empty)
    RelativeLayout layout_empty;
    public static final String KEY_NEED_SHOW_AVATAR_LAYOUT = "need_show_avatar";
    private static final int REQUEST_CODE_START_ALBUM = 0x01;
    private String group_image_url;
    public static final int GROUP_MEMBER_MAX_COUNTS = 40;
    private ArrayList<GroupMemberModel> mMemberList;
    private TabfindGroupModel mGroupModel;

    private PWUserModel pwUserModel;
    private String mImageKey;

    private enum SORT_TYPE {
        SOUR_BY_ACTIVE_TIME,
        SOUR_BY_CALL_DURATION
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        pwUserModel = UserManager.getPWUser(this);
        init();
        loadContactList();
        EventBus.getDefault().register(this);
    }

    private void init() {
        mGroupModel = getIntent().getParcelableExtra(GroupHomePageActvity.K_GROUP_DATA);
        mMemberList = getIntent().getParcelableArrayListExtra(GroupHomePageActvity.KEY_MEMBER_LIST);
        boolean needShowAvatar = getIntent().getBooleanExtra(KEY_NEED_SHOW_AVATAR_LAYOUT, true);
        if (needShowAvatar) {
            layout_group_avatar_name.setVisibility(View.VISIBLE);
        } else {
            layout_group_avatar_name.setVisibility(View.GONE);
        }
        String titleStr = getString(R.string.create_group);
        if (isInvolveFriends()) {
            mAdapter = new GroupAddFriendsAdapter(this, mList, mMemberList, mGroupModel.member_number);
            titleStr = getString(R.string.involve_group_member_with_counts, mGroupModel.member_number);
            ImageLoader.getInstance().displayImage(mGroupModel.avatar, civ_choose_album);
            et_group_name.setText(mGroupModel.group_name);
            if (BuildConfig.DEBUG) {
                CustomLog.d("init(). group_id is : " + mGroupModel.group_id);
                CustomLog.d("init(). member list size is : " + mMemberList.size());
            }
        } else {
            mAdapter = new GroupAddFriendsAdapter(this, mList);

        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerview.setLayoutManager(layoutManager);
        mRecyclerview.setAdapter(mAdapter);
        et_search_name.addTextChangedListener(this);
        setTitle(titleStr);
        setRightText("确认");
    }

    private boolean isInvolveFriends() {
        return mGroupModel != null && mMemberList != null;
    }

    private void loadContactList() {
        int friend_uid = getIntent().getIntExtra("friend_uid", 0);
        CustomLog.d("loadContactList. friend uid is : " + friend_uid);
        BriteDatabase briteDatabase = BriteDBHelperHolder.getInstance().getBriteDatabase(this);
        String sql = String.format("select * from %s where contact_state = 0 order by signin_time desc", PWDBConfig.TB_PW_CONTACTS);
        Observable<SqlBrite.Query> observable = briteDatabase.createQuery(PWDBConfig.TB_PW_CONTACTS, sql);
        Subscription subscription = observable.subscribe(query -> {
            List<PWContactsModel> childs = new ArrayList<>();
            Cursor cursor = query.run();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    PWContactsModel model = new PWContactsModel(cursor);
                    if (model.uid.equals(String.valueOf(friend_uid))) {
                        CustomLog.d("loadContactList here! model uid is : " + model.uid);
                        model.is_group_added = true;
                        mAdapter.getMembersToBeList().add(model);
                        childs.add(0, model);
                        String title = getString(R.string.create_group_member_with_counts, 2);
                        setTitle(title);
                        showTobeNameList(model.name);
                    } else {
                        childs.add(model);
                    }
                }
                cursor.close();
            }
            mList.clear();
            mList.addAll(childs);
            mAdapter.notifyDataSetChanged();
        });
        subscription.unsubscribe();
    }

    public void showTobeNameList(String name) {
        tv_name_list.setText(name);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String result = s.toString();
        if (!TextUtils.isEmpty(result.trim())) {
            doSearch(result);
        } else {
            if (isInvolveFriends()) {
                mAdapter = new GroupAddFriendsAdapter(this, mList, mMemberList, mGroupModel.member_number);
            } else {
                mAdapter = new GroupAddFriendsAdapter(this, mList);
            }
            mRecyclerview.setAdapter(mAdapter);
            layout_empty.setVisibility(View.GONE);
        }
    }

    private void doSearch(String result) {
        String input_str = PinYin.getAllPinYin(result).toLowerCase();
        if (TextUtils.isEmpty(input_str)) return;
        ArrayList<PWContactsModel> findOutList = new ArrayList<>();
        for (int i = 0; i < mList.size(); i++) {
            PWContactsModel model = mList.get(i);
            String pinyin_name = PinYin.getAllPinYin(model.name).toLowerCase();
            if (pinyin_name.contains(input_str)) {
                findOutList.add(model);
            }
        }
        updateFindResult(findOutList);
    }

    private void updateFindResult(ArrayList<PWContactsModel> findOutList) {
        if (findOutList.size() > 0) {
            mAdapter = new GroupAddFriendsAdapter(this, mList, mMemberList, findOutList);
            layout_empty.setVisibility(View.GONE);
        } else {
            ArrayList emptyList = new ArrayList<>();
            mAdapter = new GroupAddFriendsAdapter(this, emptyList);
            layout_empty.setVisibility(View.VISIBLE);
        }
        mRecyclerview.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void right_click(View v) {
        ArrayList tobeList = mAdapter.getMembersToBeList();
        if (mMemberList == null && tobeList.size() < 2) {
            showToast(this, getString(R.string.choose_at_least_2_people));
        } else {
            if (tobeList.size() > 0) {
                if (mMemberList == null || mMemberList.size() == 0)
                    createGroup();
                else
                    involveFriends();
            } else {
                showToast(this, getString(R.string.add_at_least_1_person));
            }
        }
    }

    private void createGroup() {
        showAnimLoading();
        ArrayList<PWContactsModel> selectedList = mAdapter.getMembersToBeList();
        StringBuilder sb_name = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        String avatar_thumb_url = group_image_url;
        String groupName = et_group_name.getText().toString();
        sb_name.append(pwUserModel.name).append(",");
        for (int i = 0; i < selectedList.size(); i++) {
            PWContactsModel model = selectedList.get(i);
            sb.append(model.uid).append(",");
            sb_name.append(model.name).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb_name.deleteCharAt(sb_name.length() - 1);
        if (group_image_url == null) {
            avatar_thumb_url = pwUserModel.avatar_thumbnail;
        }
        if (TextUtils.isEmpty(groupName.trim())) {
            groupName = tv_name_list.getText().toString();
        }

        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("group_name", TextUtils.isEmpty(groupName) ? "no_name" : groupName));
        params.add(new BasicNameValuePair("avatar", avatar_thumb_url));
        params.add(new BasicNameValuePair("members", sb.toString()));
        CustomLog.d("createGroup. avatar thumb is : " + avatar_thumb_url);
        CustomLog.d("createGroup. members is : " + sb.toString());
        CustomLog.d("createGroup. group name is : " + (TextUtils.isEmpty(groupName) ? "no_name" : groupName));
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_CREATE_GROUP_CHAT, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("CreateChatGroupActivity, data is : " + data);
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    String rst = sb_name.toString() + "已加入群组，互相打个招呼吧";
                    sendGroupMessage(data, rst);
                });
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("CreateChatGroupActivity, error is : " + error + ", ret is : " + ret);
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
                    String str = getString(R.string.create_group_failed);
                    if(error == 20015) {
                        str = getString(R.string.create_group_failed_by_limit_counts);
                    }
                    showToast(CreateChatGroupActivity.this, str);
                    dismissAnimLoading();
                });
            }
        });
    }

    private void sendGroupMessage(JSONObject data, String rst) {
        showToast(CreateChatGroupActivity.this, getString(R.string.create_group_success));
        JSONObject groupData = data.optJSONObject("group");
//        String group_id = groupData.optString(GroupHomePageActvity.KEY_GROUP_ID);
//        String group_prefix = groupData.optString(GroupHomePageActvity.KEY_GROUP_NAME);
//        String avatar_thumb = groupData.optString(GroupHomePageActvity.KEY_GROUP_AVATAR);
//        int admin_id = groupData.optInt(GroupHomePageActvity.KEY_ADMIN_ID);

        TabfindGroupModel groupModel = JSON.parseObject(groupData.toString(), TabfindGroupModel.class);
        GroupMemberModel memberModel = new GroupMemberModel(pwUserModel.name, pwUserModel.name, pwUserModel.uid, GroupConstant.MemberType.ADMIN, pwUserModel.avatar_thumbnail);
        String body = RongMessageParse.encodeDecorationMessageBody(rst, pwUserModel, groupModel, memberModel);
        GroupMessageModel messageModel = new GroupMessageModel(body);

        RongIMClient.getInstance().sendMessage(Conversation.ConversationType.GROUP, groupModel.group_id, messageModel, rst, null, new RongIMClient.SendMessageCallback() {
            @Override
            public void onError(Integer messageId, RongIMClient.ErrorCode errorCode) {
                CustomLog.d("sendGroupMessage. messageId is : " + messageId + ", error is : " + errorCode);
                dismissAnimLoading();
                showToast(CreateChatGroupActivity.this, getString(R.string.create_group_failed));
            }

            @Override
            public void onSuccess(Integer messageId) {
                dismissAnimLoading();
                //MsgDBCenterService.getInstance().insertCreateGroupMsg(groupModel.group_id, groupModel.group_prefix, groupModel.avatar, rst, 0, groupModel.group_id);
                GroupMessageDecorationModel decorationModel = RongMessageParse.decodeDecorationObjectSelf(body, messageId, GroupMessageDecorationModel.class);
                MsgDBCenterService.getInstance().insertDialogsWithGroupchat(decorationModel);
                Intent it = new Intent(CreateChatGroupActivity.this, GroupChatActivity.class);
                it.putExtra(GroupHomePageActvity.K_GROUP_DATA, groupModel);
                startActivity(it);
                setResult(RESULT_OK);
                finish();
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

    private void involveFriends() {
        showAnimLoading();
        StringBuilder sb = new StringBuilder();
        StringBuilder sb_name = new StringBuilder();
        ArrayList<PWContactsModel> tobeList = mAdapter.getMembersToBeList();
        Observable.from(tobeList).subscribe(new Subscriber<PWContactsModel>() {
            @Override
            public void onCompleted() {
                sb.deleteCharAt(sb.length() - 1);
                sb_name.deleteCharAt(sb_name.length() - 1);

                CustomLog.d("involveFriends. member list is : " + sb);
                postData(sb.toString(), sb_name.toString());
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(PWContactsModel pwContactsModel) {
                sb.append(pwContactsModel.uid).append(",");
                sb_name.append(pwContactsModel.name).append("，");
            }
        });
    }

    private void postData(String s_uid, String s_name) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(GroupHomePageActvity.KEY_GROUP_ID, mGroupModel.group_id));
        params.add(new BasicNameValuePair(GroupHomePageActvity.KEY_MEMBER_IDS, s_uid));
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_GROUP_MEMBERS, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("involveFriends, data is : " + data);
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(object -> {
                    /**send rong message**/
                    String rst = pwUserModel.name + " 已邀请 " + s_name + " 加入群组";
                    GroupMemberModel memberModel = new GroupMemberModel(pwUserModel.name, pwUserModel.name, pwUserModel.uid, mGroupModel.member_type, pwUserModel.avatar);
                    String body = RongMessageParse.encodeDecorationMessageBody(rst, pwUserModel, mGroupModel, memberModel);
                    Intent intent = new Intent(GroupChatActivity.ACTION_POST_MESSAGE);
                    intent.putExtra(GroupChatActivity.K_POST_MESSAGE_TYPE, GroupConstant.MessageType.TYPE_DECORATION);
                    intent.putExtra(GroupChatActivity.K_POST_MESSAGE_DATA, body);
                    EventBus.getDefault().post(intent);

                    GroupMessageModel messageModel = new GroupMessageModel(body);
                    RongIMClient.getInstance().sendMessage(Conversation.ConversationType.GROUP, mGroupModel.group_id, messageModel, rst, null, new RongIMClient.SendMessageCallback() {
                        @Override
                        public void onError(Integer messageId, RongIMClient.ErrorCode errorCode) {

                        }

                        @Override
                        public void onSuccess(Integer messageId) {
                            GroupMessageDecorationModel decorationModel = RongMessageParse.decodeDecorationObjectSelf(body, messageId, GroupMessageDecorationModel.class);
                            MsgDBCenterService.getInstance().insertDialogsWithGroupchat(decorationModel);
                        }
                    }, new RongIMClient.ResultCallback<Message>() {
                        @Override
                        public void onSuccess(Message message) {

                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                    Intent it = new Intent(CreateChatGroupActivity.this, GroupChatActivity.class);
                    it.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    it.putExtra(GroupChatActivity.K_GROUP_DATA, mGroupModel);
                    startActivity(it);
                    int number = mGroupModel.total_number + mAdapter.getMembersToBeList().size();
                    it.putExtra("total_number", number);
                    EventBus.getDefault().post(it);
                    dismissAnimLoading();
                    mAdapter.getMembersToBeList().clear();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(int error, Object ret) {

                CustomLog.d("involveFriends, error is : " + error);
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
                    dismissAnimLoading();
                    showToast(CreateChatGroupActivity.this, getString(R.string.create_group_failed));
                });

            }
        });
    }

    public void click(View v) {
        switch (v.getId()) {
            case R.id.civ_choose_album:
                addPhoto();
                break;
            default:
                break;
        }
    }

    public void onEventMainThread(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (TextUtils.isEmpty(action) && !CreateChatGroupActivity.ACTION_INVOLVE_MEMBER.equals(action)) {
            return;
        }
        int totalSize = mAdapter.getMembersToBeList().size();
        if (mGroupModel != null) {
            totalSize += mGroupModel.member_number;
        }
        CustomLog.d("onEventMainThread. current member is : " + totalSize);
        String titleStr = getString(R.string.create_group_member_with_counts, totalSize + 1);
        if (isInvolveFriends()) {
            titleStr = getString(R.string.involve_group_member_with_counts, totalSize);
        }
        setTitle(titleStr);
    }


    private void chooceImageByAlbum() {
        Intent intent = new Intent(this, AlbumCompatActivity.class);
        intent.putExtra(AlbumCompatActivity.K_ALBUM_RST_COUNT, 1);
        startActivityForResult(intent, REQUEST_CODE_START_ALBUM);
    }

    private void addPhoto() {
        new AlertDialog.Builder(this)
                .setTitle("添加照片")
                .setItems(new String[]{"拍照", "相册", "取消"},
                        (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    mImageKey = PWUploader.getInstance().getKey(getIntent().getIntExtra("friend_uid", 0));
                                    ImageUtil.startImgPickerCamera(
                                            CreateChatGroupActivity.this,
                                            ImageUtil.PICK_FROM_CAMERA,
                                            ImageUtil.getPathForCameraCrop(mImageKey));
                                    break;
                                case 1:
                                    chooceImageByAlbum();
                                    break;
                                default:
                                    break;
                            }
                        }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_START_ALBUM) {
            ArrayList<String> items = data.getStringArrayListExtra(AlbumCompatActivity.K_ALBUM_RST);
            String path = items.get(0);
            uploadImgByCameraCrop(path);
            ImageLoader.getInstance().displayImage("file://" + path, civ_choose_album);
        } else if (resultCode == RESULT_OK && requestCode == ImageUtil.PICK_FROM_CAMERA) {
            File src = ImageUtil.getPathForCameraCrop(mImageKey);
            String path = src.getAbsolutePath();
            uploadImgByCameraCrop(path);
            CustomLog.d("onActivityResult PICK_FROM_CAMERA. path : " + path);
            ImageLoader.getInstance().displayImage("file://" + path, civ_choose_album);
        }
    }

    private void uploadImgByCameraCrop(String imagePath) {
        CustomLog.d("createGroup, image path is : " + imagePath);
        if (TextUtils.isEmpty(imagePath)) {
            showToast(this, "获取图片出错");
        } else {
            String imageKey = PWUploader.getInstance().getKey(pwUserModel.uid);
            ImageModel imgModel = new ImageModel(imagePath, imageKey);
            uploadImgBySCS(imgModel);
        }
//        if (imagePath == null) {
//            showToast(this, "获取图片出错");
//        } else {
//            String imageKey = PWUploader.getInstance().getKey(pwUserModel.uid);
//            ImageModel imgModel = new ImageModel(imagePath, imageKey);
//            uploadImgBySCS(imgModel);
//        }
    }

    private void uploadImgBySCS(final ImageModel imgModel) {
        showAnimLoading("", false, false, false);
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PWUploader.K_UPLOAD_TYPE, PWUploader.UPLOAD_TYPE_AVATAR));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_QINIU_TOKEN, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                PWUploader uploader = PWUploader.getInstance();
                uploader.add(imgModel.uploadpath, data.optString("key"), data.optString("token"), new UploadCallback() {
                    @Override
                    public void onComplete(String key, ResponseInfo responseInfo, JSONObject jsonObject) {
                        group_image_url = data.optString("thumbnail_url");
                        dismissAnimLoading();
                    }

                    @Override
                    public void onFailure(String key, ResponseInfo responseInfo, JSONObject jsonObject) {
                        CustomLog.d("CreateChatGroupActivity onFailure.");
                        Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                            dismissAnimLoading();
                            showToast(CreateChatGroupActivity.this, getString(R.string.load_failed));
                        });
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        mAdapter.getMembersToBeList().clear();
        super.onDestroy();
    }
}