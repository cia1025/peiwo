package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.sqlbrite.BriteDatabase;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.db.BriteDBHelperHolder;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.fragment.TabFriendFragment;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.util.UmengStatisticsAgent;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.InputBoxView;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 聊天页面更多的设置
 */
public class MsgAcceptActionActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    public static final String K_DATA = "k_data";
    private static final int MESSAGE_ID_DELETE_NOTE = 1000;
    private static final int MESSAGE_ID_ADD_NOTE = 1001;
    private static final int MESSAGE_ID_DELETE_NOTE_ERROR = 1002;
    private static final int MESSAGE_ID_ADD_NOTE_ERROR = 1003;
    public static final String A_FLAG = "a_flag";
    public static final int V_FLAG_UPDATE_NOTE = 1;
    public static final int V_FLAG_DELETE_IMLOGS = 2;
    public static final int V_FLAG_LAHEI = 3;
    public static final int V_FLAG_DELETE_FRIEND = 4;
    public static final int V_FLAG_LAHEI_REPORT = 5;
    public static final int V_FLAG_USERINFO = 6;
    private static final int WHAT_DATA_RECEIVE_BLOCK_SECCESS = 1004;
    private static final int WHAT_DATA_RECEIVE_BLOCK_FAILURE = 1005;
    private static final int WHAT_DATA_RECEIVE_REPORT_SUCCESS = 1006;
    private static final int WHAT_DATA_RECEIVE_DELCONTACT = 1007;
    private static final int REQUESTCODE_USERINFO = 1008;
    private static final int ID_RECEIVE_FREECALL = 1009;
    private static final int ID_RECEIVE_FREECALL_ERROR = 1010;
    private static final int ID_RECEIVE_FREECALL_STATUS = 1011;
    private static final int ID_RECEIVE_FREECALL_STATUS_ERROR = 1012;
    private static final int REQUSET_CREATE_GROUP = 1013;
    private static final String PERMISSION_CREATE_GROUP = "CREATE_GROUP";
    //private EditText et_remark;
    MsgDBCenterService msgDBCenterService = MsgDBCenterService.getInstance();

    private PWUserModel model;
    private TextView tv_note;
    private SwitchCompat v_switch_answer_free;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_accept_action);
        init();
        //触发联系人刷新机制，解决添加成员列表刷新
        EventBus.getDefault().post(new Intent(TabFriendFragment.ACTION_REFRESH));
    }

    private void init() {
        Intent data = getIntent();
        model = data.getParcelableExtra(K_DATA);
        ImageView civ_avatar = (ImageView) findViewById(R.id.civ_avatar);
        CustomLog.d("init(). avatar_thumb is : " + model.avatar_thumbnail + ", avatart is : " + model.avatar);
        ImageLoader.getInstance().displayImage(model.avatar_thumbnail, civ_avatar);
        //et_remark = (EditText) findViewById(R.id.et_remark);
        String user_note = UserManager.getNoteByUid(model.uid, this);
        setBarTitle();
        tv_note = (TextView) findViewById(R.id.tv_note);
        tv_note.setText(TextUtils.isEmpty(user_note) ? model.name : user_note);
        v_switch_answer_free = (SwitchCompat) findViewById(R.id.v_switch_answer_free);
        v_switch_answer_free.setOnCheckedChangeListener(this);
        //et_remark.setText(user_note);
        //et_remark.setSelection(et_remark.getText().length());
        TextView v_lahei = (TextView) findViewById(R.id.v_lahei);
        //TextView v_report = (TextView) findViewById(R.id.v_report);
        if (model.relation != UserInfoActivity.RELATION_FRIENDS) {
            //不是好友
            v_lahei.setVisibility(View.GONE);
            tv_note.setVisibility(View.GONE);
            findViewById(R.id.tv_note_lable).setVisibility(View.GONE);
            findViewById(R.id.v_remove_friend).setVisibility(View.GONE);
        }
        boolean isGroup = data.getBooleanExtra(GroupHomePageActvity.KEY_IS_GROUP, false);
        if (isGroup) {
            TextView tv_create_group = (TextView) findViewById(R.id.tv_create_group);
            tv_create_group.setText(getResources().getString(R.string.group_settings));
            TextView tv_rules = (TextView) findViewById(R.id.tv_create_group_rules);
            tv_rules.setText("");
            findViewById(R.id.layout_answer_free).setVisibility(View.GONE);
        }
        getFreeCallStatus();
    }

    private void getFreeCallStatus() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("tuid", String.valueOf(model.uid)));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_FREECALL, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                distributeMessage(ID_RECEIVE_FREECALL_STATUS, data);
            }

            @Override
            public void onError(int error, Object ret) {
                distributeMessage(ID_RECEIVE_FREECALL_STATUS_ERROR, null);
            }
        });
    }

    private void setBarTitle() {
        TitleUtil.setTitleBar(this, "聊天详情", v -> {
            finish();
        }, "确定", v -> {
            postUserNote();
        });
    }

    private void postUserNote() {
        showAnimLoading();
        if (TextUtils.isEmpty(tv_note.getText().toString().trim())) {
            //删除
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("tuid", String.valueOf(model.uid)));
            ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_CONTACT_NOTE_DEL, new MsgStructure() {
                @Override
                public void onReceive(JSONObject data) {
                    //删除数据库存备注
                    distributeMessage(MESSAGE_ID_DELETE_NOTE, data);
                }

                @Override
                public void onError(int error, Object ret) {
                    distributeMessage(MESSAGE_ID_DELETE_NOTE_ERROR, ret instanceof JSONObject ? (JSONObject) ret : null);
                }
            });
        } else {
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("tuid", String.valueOf(model.uid)));
            params.add(new BasicNameValuePair("note", tv_note.getText().toString().trim()));
            ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_CONTACT_NOTE_ADD, new MsgStructure() {
                @Override
                public void onReceive(JSONObject data) {
                    //数据库存备注
                    distributeMessage(MESSAGE_ID_ADD_NOTE, data);
                }

                @Override
                public void onError(int error, Object ret) {
                    distributeMessage(MESSAGE_ID_ADD_NOTE_ERROR, ret instanceof JSONObject ? (JSONObject) ret : null);
                }
            });
        }
    }

    @Override
    protected void handle_message(int message_id, JSONObject obj) {
        dismissAnimLoading();
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        switch (message_id) {
            case MESSAGE_ID_DELETE_NOTE:
                msgDBCenterService.delSinglePwRemark(String.valueOf(model.uid));
                app.removeNoteByUid(model.uid);
                resultForData(V_FLAG_UPDATE_NOTE);
                break;

            case MESSAGE_ID_ADD_NOTE:
                msgDBCenterService.insertSinglePwRemark(String.valueOf(model.uid), tv_note.getText().toString());
                String mark = tv_note.getText().toString().trim();
                if (TextUtils.isEmpty(mark)) {
                    app.removeNoteByUid(model.uid);
                } else {
                    app.putNoteByUid(model.uid, mark);
                }
                resultForData(V_FLAG_UPDATE_NOTE);
                break;
            case WHAT_DATA_RECEIVE_BLOCK_SECCESS:
                msgDBCenterService.deletePWContact(model.uid);
                showToast(this, "拉黑成功");
                resultForData(V_FLAG_LAHEI);
                break;
            case WHAT_DATA_RECEIVE_BLOCK_FAILURE:
                showToast(this, "该用户已经被拉黑过");
                break;
            case WHAT_DATA_RECEIVE_ERROR:
                showToast(this, "网络连接失败");
                break;
            case WHAT_DATA_RECEIVE_REPORT_SUCCESS:
                showToast(this, "举报成功");
                resultForData(V_FLAG_LAHEI_REPORT);
                break;
            case WHAT_DATA_RECEIVE_DELCONTACT:
                TcpProxy.getInstance().unFocusUser(model.uid);
                showToast(this, "删除成功");
                deletePWContactFromDB(model.uid);
                resultForData(V_FLAG_DELETE_FRIEND);
                break;
            case ID_RECEIVE_FREECALL:
                Snackbar.make(tv_note, "设置成功", Snackbar.LENGTH_SHORT).show();
                break;
            case ID_RECEIVE_FREECALL_ERROR:
                v_switch_answer_free.setTag("");
                v_switch_answer_free.setChecked(!v_switch_answer_free.isChecked());
                Snackbar.make(tv_note, "设置失败", Snackbar.LENGTH_SHORT).show();
                break;
            case ID_RECEIVE_FREECALL_STATUS:
                v_switch_answer_free.setTag("");
                int freecall = obj.optInt("freecall");
                v_switch_answer_free.setChecked(freecall == 1);
                v_switch_answer_free.setTag(null);
                break;
            case ID_RECEIVE_FREECALL_STATUS_ERROR:
                //
                break;
        }
    }

    private void deletePWContactFromDB(int uid) {
        BriteDatabase database = BriteDBHelperHolder.getInstance().getBriteDatabase(this);
        database.delete(PWDBConfig.TB_PW_CONTACTS, "uid = ?", String.valueOf(uid));
    }

    private void resultForData(int flag) {
        Intent data = new Intent();
        data.putExtra(A_FLAG, flag);
        setResult(RESULT_OK, data);
        finish();
    }


    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.v_report:
                reportUser();
                break;
            case R.id.v_delete_message:
                deleteMessage();
                break;
            case R.id.v_lahei:
                lahei();
                break;
            case R.id.v_remove_friend:
                deleteFriend();
                break;
            case R.id.tv_note:
            case R.id.tv_note_lable:
                updateNote();
                break;
            case R.id.civ_avatar:
                startUserInfo();
                break;
            case R.id.layout_create_group:
                checkCreateGroupPermission();
                break;
            default:
                break;
        }
    }

    private void checkCreateGroupPermission() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_CHECK_GROUP_PERMISSION, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("checkCreateGroupPermission onRecieve. data is : " + data);
                JSONArray permissions = data.optJSONArray("permissions");
                try {
                    CustomLog.d("checkCreateGroupPermission, permission is : " + permissions.get(0));
                    if (permissions.get(0).equals(PERMISSION_CREATE_GROUP)) {
                        Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                            Intent it = new Intent(MsgAcceptActionActivity.this, CreateChatGroupActivity.class);
                            it.putExtra("friend_uid", model.uid);
                            startActivityForResult(it, REQUSET_CREATE_GROUP);
                        });
                    } else {
                        Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                            showToast(MsgAcceptActionActivity.this, getResources().getString(R.string.no_permission_to_create_group));
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                        showToast(MsgAcceptActionActivity.this, getResources().getString(R.string.no_permission_to_create_group));
                    });
                }
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("checkCreateGroupPermission onError. error is : " + error);
                showToast(MsgAcceptActionActivity.this, getResources().getString(R.string.request_failed));
            }
        });
    }

    private void startUserInfo() {
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra(AsynHttpClient.KEY_TUID, model.uid);
        intent.putExtra(AsynHttpClient.KEY_NAME, model.name);
        intent.putExtra("price", model.price);
        intent.putExtra("avatar", model.avatar_thumbnail);
        intent.putExtra("remark", model.remark);
        {
            Serializable data = model;
            intent.putExtra(UserInfoActivity.USER_INFO, data);
        }
        intent.putExtra("position", getIntent().getIntExtra(TabMsgFragment.LOCATION, -1));
        intent.putExtra(A_FLAG, V_FLAG_USERINFO);
        startActivityForResult(intent, REQUESTCODE_USERINFO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUESTCODE_USERINFO:
                    setResult(RESULT_OK);
                    finish();
                    break;
                case REQUSET_CREATE_GROUP:
                    setResult(RESULT_FIRST_USER);
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    private void updateNote() {
        InputBoxView boxView = InputBoxView.newInstance(tv_note.getText().toString());
        boxView.show(getSupportFragmentManager(), "boxview");
        boxView.setOnInputConfirmListener(tv_note::setText);
    }

    private void deleteFriend() {
        new AlertDialog.Builder(this).setTitle("删除好友")
                .setPositiveButton("确定", (dialog, which) -> {
                    deletePWContact(model.uid);
                }).setNegativeButton("取消", null).create().show();
    }

    private void deletePWContact(int uid) {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tuids", String.valueOf(uid)));
        showAnimLoading("", false, false, false);
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_CONTACT_DELETE, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                distributeMessage(WHAT_DATA_RECEIVE_DELCONTACT, data);
            }

            @Override
            public void onError(int error, Object ret) {
                distributeMessage(WHAT_DATA_RECEIVE_ERROR, null);
            }
        });
    }

    private void lahei() {
        doBlock();

//        new AlertDialog.Builder(this)
//                .setTitle("提示")
//                .setItems(new String[]{"拉黑", "举报并拉黑"},
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog,
//                                                int which) {
//                                switch (which) {
//                                    case 0:
//                                        doBlock();
//                                        break;
//                                    case 1:
//                                        reportUser();
//                                        break;
//
//                                    default:
//                                        break;
//                                }
//                            }
//                        }).create().show();
    }

    private void doBlock() {
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEBLACKLIST);
        new AlertDialog.Builder(this).setTitle("提示")
                .setMessage("拉黑后将不再接到对方的消息")
                .setPositiveButton("确定", (dialog, which) -> {
                    doRealBlock();
                }).setNegativeButton("取消", null).create().show();
    }

    private void doRealBlock() {
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEBLACKLISTSURE);
        showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tuid", String.valueOf(model.uid)));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_CONTACT_BLOCK, new MsgStructure() {
            @Override
            public boolean onInterceptRawData(String rawStr) {
                try {
                    distributeMessage(WHAT_DATA_RECEIVE_BLOCK_SECCESS, new JSONObject(rawStr));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public void onReceive(JSONObject data) {

            }

            @Override
            public void onError(int error, Object ret) {
                distributeMessage(error == AsynHttpClient.PW_RESPONSE_OPERATE_ERROR ? WHAT_DATA_RECEIVE_BLOCK_FAILURE : WHAT_DATA_RECEIVE_ERROR, null);
            }
        });
    }


    private void reportUser() {
        // DEFAULT = 0 其它
        // PRON = 1 色情
        // CHEAT = 2 欺诈
        // HARASS = 3 骚扰
        // INFRINGE = 4 侵权
        String[] menuArray = new String[]{"色情", "欺诈", "骚扰", "侵权", "其他", "取消"};
        new AlertDialog.Builder(this).setTitle("举报用户")
                .setItems(menuArray, (dialog, which) -> {
                    if (which == 5) {
                        return;
                    }
                    ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
                    paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_TUID, String.valueOf(model.uid)));
                    paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_REASON, String.valueOf(which == 4 ? 0 : which + 1)));
                    ApiRequestWrapper.openAPIGET(getApplicationContext(), paramList, AsynHttpClient.API_REPORT_DOBLOCK, new MsgStructure() {
                        @Override
                        public void onReceive(JSONObject data) {
                            distributeMessage(WHAT_DATA_RECEIVE_REPORT_SUCCESS, data);
                        }

                        @Override
                        public void onError(int error, Object ret) {
                        }
                    });
                }).create().show();
    }

    private void deleteMessage() {
        new AlertDialog.Builder(this)
                .setTitle("是否删除聊天记录？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> {
                    msgDBCenterService.deleteMessageByUid(String.valueOf(model.uid));
                    resultForData(V_FLAG_DELETE_IMLOGS);
                })
                .create().show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Object tag = buttonView.getTag();
        if (tag != null) {
            buttonView.setTag(null);
            return;
        }
        showAnimLoading("", false, false, false);
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("tuid", String.valueOf(model.uid)));
        params.add(new BasicNameValuePair("action", isChecked ? "1" : "0"));
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_FREECALL, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                distributeMessage(ID_RECEIVE_FREECALL, null);
            }

            @Override
            public void onError(int error, Object ret) {
                distributeMessage(ID_RECEIVE_FREECALL_ERROR, null);
            }
        });
    }
}
