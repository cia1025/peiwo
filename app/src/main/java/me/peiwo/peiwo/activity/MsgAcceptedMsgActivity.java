package me.peiwo.peiwo.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.Bind;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.qiniu.android.http.ResponseInfo;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.model.UserInfo;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.MsgAcceptAdapter;
import me.peiwo.peiwo.callback.UploadCallback;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.*;
import me.peiwo.peiwo.fragment.RecorderDialogFragment;
import me.peiwo.peiwo.im.MessageModel;
import me.peiwo.peiwo.im.MessageUtil;
import me.peiwo.peiwo.model.*;
import me.peiwo.peiwo.model.groupchat.PacketIconModel;
import me.peiwo.peiwo.net.*;
import me.peiwo.peiwo.service.NetworkConnectivityListener.NetworkCallBack;
import me.peiwo.peiwo.util.*;
import me.peiwo.peiwo.util.group.ChatImageWrapper;
import me.peiwo.peiwo.widget.ChatBottomView;
import me.peiwo.peiwo.widget.EmotionEditText;
import me.peiwo.peiwo.widget.ExpressionPanelView;
import me.peiwo.peiwo.widget.ImageQuickSwitchView;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressLint("NewApi")
public class MsgAcceptedMsgActivity extends PWPreCallingActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        NetworkCallBack {

    private static final int WHAT_DATA_RECEIVE_CALL_PERMISSION = 6000;
    private static final int WHAT_DATA_RECEIVE_CALL_PERMISSION_ERROR = 7000;

    private static final int WHAT_DATA_RECEIVE_GET_MSG_ID_SUCCESS = 21000;
    private static final int WHAT_DATA_RECEIVE_GET_MSG_ID_ERROR = 21001;
    private static final int WHAT_DATA_RECEIVE_HOTVALUE_SUCCESS = 22000;
    private static final int WHAT_DATA_RECEIVE_HOTVALUE_ERROR = 23000;
    public static final int REQUESTCODE_USERINFO = 9001;

    public static final int SELECT_IMAGE_COMPLETE = 9002;
    private static final int WHAT_DATA_RECEIVE_USER_NOT_AVAILABLE = 8000;
    private static final int WHAT_RELIEVE_BLOCK_COMPLETE = 3000;

    public static final int WHAT_INPUT_FACE = 5000;
    public static final int WHAT_INPUT_GIF_FACE = 5001;
    public static final int WHAT_PREVIEW_GIF_FACE = 5002;
    public static final int WHAT_PREVIEW_GIF_FACE_CLOSE = 5003;
    private static final int REQUECT_CODE_IM_ACTION = 9003;
    private static final int REQUEST_GROUP_HOMEPAGE = 9004;

    private ImageQuickSwitchView image_quick_switch;

    private PWUserModel meModel;

    private PWUserModel otherModel;
    private String msg_id;

    private boolean show_prompt;
    private MyHandler mHandler;
    private MsgAcceptAdapter adapter;
    private List<MsgAcceptModel> mList = new ArrayList<MsgAcceptModel>();
    private int mPermission = 1;
    //private View call_phone_layout;
    private View send_message_layout;
    private EmotionEditText emotionEditText;

    private int loaderId;
    //private View hot_layout;
//    private ImageView iv_fire1;
//    private ImageView iv_fire2;
//    private ImageView iv_fire3;
    //private View rl_sayhello;

    //private ImageView btn_send_img_btn;
    //private View msg_more_type_layout;

    private ExpressionPanelView face_lay;

    private MsgDBCenterService dbService;


    private boolean isFirst = true;
    public static String Uid = "0";

    private int lastNetType = NetUtil.NO_NETWORK;
    //private CivilizationTipsView ct_tips;


    //private static int hotValue = 0;
    private int leftMsgNum = 0;

    private InputMethodManager inputMethodManager;
    private int what_message_from;
    //private int faceSize = 0;

    private MsgAcceptModel feed_dialog = null;

    private TextView title_view;
    private TextView btn_left;

    private EmotionInputDetector mDetector;
    private ChatBottomView view_bottom_panel;
    @Bind(R.id.view_input_txt)
    View view_input_txt;
    @Bind(R.id.iv_expression)
    View iv_expression;
    @Bind(R.id.msg_send_img_btn)
    View msg_send_img_btn;
    private CompositeSubscription mSubscriptions;
    private boolean img_permission;
    private TextView message_feedback;
    private FeedbackAgent feedbackAgent;
    private static final int REQUEST_CODE_REDBAG = 0x10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.msg_accepted_msg_activity);
        mSubscriptions = new CompositeSubscription();
        mHandler = new MyHandler(this);
        Intent intent = getIntent();
        otherModel = (PWUserModel) intent.getSerializableExtra("msg_user");
        //CustomLog.d("onCreate. group model avatar is : " + otherModel.avatar_thumbnail);
        dbService = MsgDBCenterService.getInstance();
        init();
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        emotionEditText = (EmotionEditText) findViewById(R.id.et_msg);
        view_bottom_panel = (ChatBottomView) findViewById(R.id.view_bottom_panel);
        //view_input_txt.setSelected(true);
        view_bottom_panel.bindTxtIndiView(view_input_txt).bindExpressIndiView(iv_expression).bindImageQuickIndiView(msg_send_img_btn);
        if (otherModel.uid != DfineAction.SYSTEM_UID) {
            mDetector = EmotionInputDetector.with(this)
                    .setEmotionView(view_bottom_panel)
                    .bindToContent(findViewById(R.id.lv_msgaccepted))
                    .bindToEditText(emotionEditText)
                    .bindToEmotionButton(iv_expression)
                    .bindToImageButton(msg_send_img_btn)
                    .build();
            mDetector.setTextMessageButtonPerformListener(() -> view_input_txt.setSelected(true));
            //mHandler.postDelayed(() -> inputMethodManager.showSoftInput(emotionEditText, 0), 500);
            mDetector.showSoftInputIfNeed();
        }
        feedbackAgent = new FeedbackAgent(this);
        feedbackAgent.sync();
        //faceSize = PWUtils.getFaceSizeFromScreen(this);
        loaderId = hashCode();

        getSupportLoaderManager().initLoader(loaderId, null, this);

        needUpdateDb = new ArrayList<>();
        lastNetType = PeiwoApp.getApplication().getNetType();
        PeiwoApp.getApplication().addNetworkCallBack(this);
        EventBus.getDefault().register(this);
//        emotionEditText.setOnTouchListener((v, event) -> {
//            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                closeFace(2);
//                findViewById(R.id.view_input_txt).setSelected(true);
//                findViewById(R.id.msg_send_img_btn).setSelected(false);
//                findViewById(R.id.iv_expression).setSelected(false);
//                //msg_more_type_layout.setVisibility(View.GONE);
//            }
//            return false;
//        });
        //mExecutorService = Executors.newFixedThreadPool(6);

        face_lay = (ExpressionPanelView) findViewById(R.id.face_lay);
        face_lay.setOnExpressionItemClickListener(model -> {
            if (model instanceof EmotionModel) {
                emotionEditText.setTextEmotion((EmotionModel) model);
            } else if (model instanceof GIFModel) {
                adapter.setAutoScroll(true);
                sendGifFaceMsg(model.regular);
            }
        });
        face_lay.setOnExpressionDeleteEmotionListener(() -> {
            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
            emotionEditText.onKeyDown(KeyEvent.KEYCODE_DEL, event);
        });
    }

    //private ExecutorService mExecutorService = null;

    private void init() {
        btn_left = (TextView) findViewById(R.id.btn_left);
        setUnReadMessageNum();
        title_view = (TextView) findViewById(R.id.title_view);
        message_feedback = (TextView) findViewById(R.id.message_feedback);
        message_feedback.setOnClickListener(v -> {
            startFeedbackActivity();
        });
        meModel = UserManager.getPWUser(this);
        Intent intent = getIntent();
        msg_id = intent.getStringExtra("msg_id");

        show_prompt = getIntent().getBooleanExtra("show_prompt", false);
        if (intent.hasExtra("msg_user")) {
            CustomLog.d("msg_user if case.");
            if (msg_id != null && msg_id.equals("0")) {
                String selection = PWDBConfig.UidMsgId.UID + " = ?";
                String[] selectionArgs = new String[]{String.valueOf(otherModel.uid)};
                Cursor mCursor = getContentResolver().query(PWDBConfig.UidMsgId.CONTENT_URI, null, selection, selectionArgs, null);
                if (mCursor != null) {
                    if (mCursor.getCount() > 0) {
                        mCursor.moveToFirst();
                        msg_id = mCursor.getString(mCursor.getColumnIndex(PWDBConfig.UidMsgId.MSG_ID));
                    }
                    mCursor.close();
                    mCursor = null;
                }
            }
            if (msg_id != null && msg_id.equals("0")) {
                getUserMsgId();
            }
        } else {
            int uid = intent.getIntExtra("uid", 0);
            String selection = PWDBConfig.MessagesTable.UID + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(uid)};
            Cursor mCursor = getContentResolver().query(PWDBConfig.MessagesTable.CONTENT_URI, null, selection, selectionArgs, null);
            if (mCursor == null) {
                return;
            }
            if (mCursor.getCount() == 0) {
                mCursor.close();
                mCursor = null;
                return;
            }
            mCursor.moveToFirst();
            TabMsgModel msgModel = new TabMsgModel(mCursor);
            otherModel = msgModel.userModel;
            CustomLog.d("msg_user else case.");
            mCursor.close();
            mCursor = null;
        }
        what_message_from = getIntent().getIntExtra(UserInfoActivity.MESSAGE_FROM, 0);
        if (what_message_from == 0x03) {
            createFeedFlowMessage();
        }
        setBarTitle(UserManager.getRealName(otherModel.uid, otherModel.name, this), otherModel.avatar_thumbnail);


        //call_phone_layout = findViewById(R.id.call_phone_layout);
        send_message_layout = findViewById(R.id.send_message_layout);

        //emotionEditText.setOnEditorActionListener(this);
        //emotionEditText.addTextChangedListener(new MessageEditTextWatcher());

        //ct_tips = (CivilizationTipsView) findViewById(R.id.ct_tips);

        //hot_layout = findViewById(R.id.hot_layout);
//        iv_fire1 = (ImageView) findViewById(R.id.iv_fire1);
//        iv_fire2 = (ImageView) findViewById(R.id.iv_fire2);
//        iv_fire3 = (ImageView) findViewById(R.id.iv_fire3);
        //rl_sayhello = findViewById(R.id.rl_sayhello);
        ListView lv_msgaccepted = (ListView) findViewById(R.id.lv_msgaccepted);
        if (otherModel.uid == DfineAction.SYSTEM_UID) {
            //lv_msgaccepted.setStackFromBottom(false);
            //lv_msgaccepted.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
        }
        adapter = new MsgAcceptAdapter(mList, this, lv_msgaccepted);

        //btn_send_img_btn = (ImageView) findViewById(R.id.btn_send_img_btn);


        //msg_more_type_layout = findViewById(R.id.msg_more_type_layout);

        //btn_send_text_btn.setVisibility(View.GONE);
        //btn_send_img_btn.setVisibility(View.VISIBLE);

        adapter.setUserData(meModel, otherModel);
        lv_msgaccepted.setAdapter(adapter);
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                showTipsIfNeed();
//            }
//        }, 200);

        if (otherModel.uid == DfineAction.SYSTEM_UID) {
            send_message_layout.setVisibility(View.GONE);
            message_feedback.setVisibility(View.VISIBLE);
            //call_phone_layout.setVisibility(View.GONE);
            //hot_layout.setVisibility(View.GONE);
            //rl_sayhello.setVisibility(View.GONE);
        }

        image_quick_switch = (ImageQuickSwitchView) findViewById(R.id.image_quick_switch);
        image_quick_switch.setOnMoreActionClickListener(this::startImageSwitch);
        onGrabPacket();
        if (otherModel.uid != DfineAction.SYSTEM_UID) {
            lv_msgaccepted.setOnTouchListener((v, event) -> {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        inputMethodManager.hideSoftInputFromWindow(emotionEditText.getWindowToken(), 0);
//                    if (face_lay.getVisibility() != View.GONE)
//                        face_lay.setVisibility(View.GONE);
//                    if (image_quick_switch.getVisibility() != View.GONE)
//                        image_quick_switch.setVisibility(View.GONE);
                        mDetector.interceptBackPress();
                        break;
                }
                return false;
            });
            lv_msgaccepted.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                        inputMethodManager.hideSoftInputFromWindow(emotionEditText.getWindowToken(), 0);
//                    if (face_lay.getVisibility() != View.GONE)
//                        face_lay.setVisibility(View.GONE);
//                    if (image_quick_switch.getVisibility() != View.GONE)
//                        image_quick_switch.setVisibility(View.GONE);
                        mDetector.interceptBackPress();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                }
            });
        }

    }


    private ContentObserver badgeObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            if (!PWUtils.isMultiClick()) {
                mHandler.postDelayed(() -> {
                    setTextBadge();
                    //Log.i("change", "count == " + count);
                    //Log.i("change", "thread == " + Thread.currentThread().getName());
                }, 1000);
            }
        }
    };

    private void setTextBadge() {
        int count = dbService.getBadge();
        if (count > 0) {
            btn_left.setTextColor(Color.parseColor("#00b8d0"));
            btn_left.setText("•");
            //btn_left.setText("(" + count + ")");
        } else {
            btn_left.setText(null);
        }
    }


    private void setUnReadMessageNum() {
        setTextBadge();
        Uri uri = Uri.parse("content://" + PWDBConfig.AUTOHORITY + "/" + PWDBConfig.TB_NAME_PW_MESSAGES + "/#");
        getContentResolver().registerContentObserver(uri, true, badgeObserver);
    }


    /**
     * 获取打电话权限
     */
    private void getPermission(final boolean needTip, boolean needLoading) {
        if (otherModel.uid == DfineAction.SYSTEM_UID) {
            return;
        }
        if (needLoading) {
            showAnimLoading();
        }
        ApiRequestWrapper.getPermission(this, meModel.uid, otherModel.uid, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Message message = mHandler.obtainMessage();
                message.what = WHAT_DATA_RECEIVE_CALL_PERMISSION;
                Bundle b = new Bundle();
                b.putInt("permission", data.optInt("permission", 0));
                b.putDouble("price", data.optDouble("price", 0));
                message.setData(b);
                mHandler.sendMessage(message);
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_CALL_PERMISSION_ERROR);
            }
        });
    }

    private void getUserMsgId() {
        ApiRequestWrapper.getUserMsgId(this, meModel.uid, otherModel.uid, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                try {
                    msg_id = data.getString("msg_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_GET_MSG_ID_SUCCESS);
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_GET_MSG_ID_ERROR);
            }
        });
    }

    private static class MyHandler extends Handler {
        WeakReference<MsgAcceptedMsgActivity> acivity_ref;

        public MyHandler(MsgAcceptedMsgActivity activity) {
            acivity_ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MsgAcceptedMsgActivity theActivity = acivity_ref.get();
            if (theActivity == null || theActivity.isFinishing())
                return;
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    break;
                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.showToast(theActivity, "网络连接失败");
                    break;
                case WHAT_DATA_RECEIVE_CALL_PERMISSION:
                    theActivity.dismissAnimLoading();
                    Bundle data = msg.getData();
                    theActivity.mPermission = data.getInt("permission");
                    theActivity.otherModel.price = String.valueOf(data.getDouble("price"));


                    break;
                case WHAT_DATA_RECEIVE_CALL_PERMISSION_ERROR:
                    theActivity.dismissAnimLoading();
                    break;
                case WHAT_DATA_RECEIVE_GET_MSG_ID_SUCCESS:
                case WHAT_DATA_RECEIVE_GET_MSG_ID_ERROR:
                    theActivity.dismissAnimLoading();
                    break;
                case WHAT_DATA_RECEIVE_HOTVALUE_SUCCESS:


                    break;
                case WHAT_DATA_RECEIVE_HOTVALUE_ERROR:
                    CustomLog.d("hot value error!");
                    break;
                case WHAT_DATA_RECEIVE_USER_NOT_AVAILABLE:
                    theActivity.userNotAvailableDelay();
                    break;
                case WHAT_RELIEVE_BLOCK_COMPLETE:
                    //解除黑名单
                    theActivity.showToast(theActivity, "解除黑名单成功");
                    break;
                case WHAT_INPUT_FACE:
                    String image_item = (String) msg.obj;
                    //theActivity.emotionEditText.getText().insert(theActivity.emotionEditText.getSelectionStart(), image_item);
                    break;
                case WHAT_INPUT_GIF_FACE:
                    theActivity.sendGifFaceMsg(msg.obj.toString());
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }


    private void setBarTitle(String title_str, String avatar) {
        if (otherModel.uid == DfineAction.SYSTEM_UID) {
            this.title_view.setText("系统消息");
            findViewById(R.id.iv_im_avatar).setVisibility(View.GONE);
        } else {
            this.title_view.setText("您与" + title_str);
//            ImageView iv_im_avatar = (ImageView) findViewById(R.id.iv_im_avatar);
//            ImageLoader.getInstance().displayImage(avatar, iv_im_avatar);
        }

    }


    private long focusId = 0;
    private int focusDialogId = 0;

    public void focusContact(long id, int dialog_id) {
//        showAnimLoading("", false, false);
//        TcpProxy.getInstance().focusUser(otherModel.uid, 0);
//        focusId = id;
//        focusDialogId = dialog_id;
    }

    public void sendVoiceRequest() {
        boolean netAvailable = PWUtils.isNetWorkAvailable(this);
        if (!netAvailable) {
            showToast(this, getResources().getString(R.string.umeng_common_network_break_alert));
            return;
        }
        RecorderDialogFragment mRecorderDialogFrag = RecorderDialogFragment.newInstance(otherModel.uid, otherModel.name, otherModel.avatar_thumbnail, 0);
        mRecorderDialogFrag.show(getSupportFragmentManager(), mRecorderDialogFrag.toString());
    }

    public void click(View v) {
        switch (v.getId()) {
            case R.id.call_phone_button:
                checkCallPermission();
                inputMethodManager.hideSoftInputFromWindow(emotionEditText.getWindowToken(), 0);
                face_lay.setVisibility(View.GONE);
                image_quick_switch.setVisibility(View.GONE);
                break;
            case R.id.view_input_txt:
//                closeFace(2);
//                v.setSelected(true);
//                findViewById(R.id.msg_send_img_btn).setSelected(false);
//                findViewById(R.id.iv_expression).setSelected(false);
                view_bottom_panel.setVisibility(View.GONE);
                inputMethodManager.showSoftInput(emotionEditText, 0);
                break;
//            case R.id.ib_keyboard_send_message:
//                changeSendMsgLayout();
//                decideShowMoreLayout(R.id.ib_keyboard_send_message);
//                break;
//            case R.id.ib_keyboard_call_phone:
//                if (hotValue == 3 || relation == 0) {
//                    //call_phone_layout.setVisibility(View.VISIBLE);
//                    send_message_layout.setVisibility(View.GONE);
//                    inputMethodManager.hideSoftInputFromWindow(emotionEditText.getWindowToken(), 0);
//                } else {
//                    showToast(this, "集满热度或互相关注才能打电话哦!");
//                }
//                decideShowMoreLayout(R.id.ib_keyboard_call_phone);
//                closeFace(3);
//                break;
            case R.id.v_redbag_start:
                mDetector.interceptBackPress();
                changeViewTextInputMode();
                enterRedBagPagerVerifi();
                break;
            case R.id.btn_send_text_btn:
                //decideShowMoreLayout(R.id.btn_sendmsg);
                adapter.setAutoScroll(true);

                if (view_bottom_panel.isImageShown()) {
                    //ImageFetcher.getInstance().getSelectedImageList().addAll(image_quick_switch.getSelectedImages());
                    List<String> image_paths = image_quick_switch.getSelectedImages();
                    mDetector.interceptBackPress();
                    if (image_paths.size() > 0) {
                        if (img_permission) {
                            sendImageMsg(image_paths);
                        } else {
                            checkImagePermission(image_paths);
                        }
                    }
                    //view_bottom_panel.setVisibility(View.GONE);
                } else {
                    sendTextMsg();
                }
                break;
//            case R.id.msg_send_img_btn:
////                int call_duration = MsgDBCenterService.getInstance().getCallDuration(String.valueOf(otherModel.uid));
////                boolean hasCalled = SharedPreferencesUtil.getBooleanExtra(this, "called_with" + String.valueOf(otherModel.uid), false);
////                boolean hasCallHistory = (call_duration > 0) || hasCalled;
////                if (!hasCallHistory) {
////                    showToast(this, "与好友通过电话开启发送图片权限");
////                    return;
////                }
//                if (otherModel.call_duration <= 0) {
//                    showToast(this, "与好友通过电话开启发送图片权限");
//                    return;
//                }
//                //取消热度逻辑
//                //startImageSwitch();
//                //quick image switch
//
//                v.setSelected(true);
//                findViewById(R.id.view_input_txt).setSelected(false);
//                findViewById(R.id.iv_expression).setSelected(false);
//
//                inputMethodManager.hideSoftInputFromWindow(emotionEditText.getWindowToken(), 0);
//                face_lay.setVisibility(View.GONE);
//                image_quick_switch.setVisibility(View.VISIBLE);
////                boolean hasCalled = SharedPreferencesUtil.getBooleanExtra(this, "called_with" + String.valueOf(otherModel.uid), false);
////                boolean hasCallHistory = (call_duration > 0) || hasCalled;
////                if (hasCallHistory && relation == 0) {
////                    Intent intent = new Intent(this, ImageChooseActivity.class);
////                    startActivityForResult(intent, SELECT_IMAGE_COMPLETE);
////                } else {
////                    showToast(this, "互相关注并且通过电话才能发照片哦!");
////                }
//
//                break;
//            case R.id.hot_layout:
//                showHotDialog();
//                break;
//            case R.id.iv_expression:
//
//                v.setSelected(true);
//                findViewById(R.id.view_input_txt).setSelected(false);
//                findViewById(R.id.msg_send_img_btn).setSelected(false);
//
//                image_quick_switch.setVisibility(View.GONE);
//                showFaceView();
//                break;

            case R.id.view_left:
                finish();
                break;
            case R.id.iv_im_avatar:
//                startUserinfoForResult(false);
//                break;
                //case R.id.iv_im_action:
                Intent intent = new Intent(this, MsgAcceptActionActivity.class);
                Parcelable data = otherModel;
                intent.putExtra(MsgAcceptActionActivity.K_DATA, data);
                startActivityForResult(intent, REQUECT_CODE_IM_ACTION);
                break;
        }
    }

    private void changeViewTextInputMode() {
        view_input_txt.setSelected(true);
        iv_expression.setSelected(false);
        msg_send_img_btn.setSelected(false);
    }

    private void enterRedBagPagerVerifi() {
        Intent redbag_intent = new Intent(this, IMChatRedbagActivity.class);
        redbag_intent.putExtra("tuid", otherModel.uid);
        startActivityForResult(redbag_intent, REQUEST_CODE_REDBAG);
    }

    private void checkImagePermission(final List<String> image_paths) {
        showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("tuid", String.valueOf(otherModel.uid)));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_USERINFO_PERMISSION, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("btn_send_text_btn onReceive data is : " + data);
                img_permission = data.optInt("im_image_permission") == 1;
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    if (img_permission) {
                        sendImageMsg(image_paths);
                    } else {
                        showToast(MsgAcceptedMsgActivity.this, getResources().getString(R.string.no_permission_send_img));
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("btn_send_text_btn onError. error is : " + error + ", ret is : " + ret);
                img_permission = false;
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    showToast(MsgAcceptedMsgActivity.this, getResources().getString(R.string.network_not_stable));
                });
            }
        });
    }

    public void startImageSwitch() {
//        ImageFetcher.getInstance().getSelectedImageList().clear();
//        Intent intent = new Intent(this, ImageChooseActivity.class);
        Intent intent = new Intent(this, AlbumCompatActivity.class);
        intent.putExtra(AlbumCompatActivity.CHOOSE_MODE, AlbumCompatActivity.CHOOSE_MODE_SECTION); // ImageUtil.getPathForUpload(mImageKey).getAbsolutePath()
        intent.putExtra(AlbumCompatActivity.K_ALBUM_RST_COUNT, 5);
        startActivityForResult(intent, SELECT_IMAGE_COMPLETE);
    }


    private void sendTextMsg() {

        if (emotionEditText.getText().length() == 0)
            return;
        if (emotionEditText.getText().length() > 1000) {
            // 超出最大长度
            showToast(this, "您发送的信息内容超长，请分条发送");
            return;
        }
        final String msg = emotionEditText.getText().toString();


        if (!TextUtils.isEmpty(msg)) {
            PeiwoApp.getApplication().mExecutorService.execute(() -> {
                show_prompt = false;
                boolean netAvailable = PWUtils.isNetWorkAvailable(MsgAcceptedMsgActivity.this);
                long currentTime = System.currentTimeMillis() + SharedPreferencesUtil.getLongExtra(PeiwoApp.getApplication(), AsynHttpClient.KEY_CC_CURRENT_TIME, 0);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentDate = df.format(new Date(currentTime));
                long randomCount = (long) (Math.random() * 10000) * 1000000000l;
                currentTime %= 1000000000l;
                currentTime += randomCount;
                // 2015-01-15 11:00:10
                MessageModel model = new MessageModel();
                model.content = msg;
                model.dialog_type = MessageModel.DIALOG_TYPE_IM;
                model.msg_id = msg_id;
                if (netAvailable) {
                    model.send_status = MessageModel.SEND_STATUS_SUCCESS;
                } else {
                    model.send_status = MessageModel.SEND_STATUS_FAIL;
                }
                model.uid = otherModel.uid;
                model.update_time = currentDate;
                model.dialog_id = -currentTime;
                model.type = 0;
                if (feed_dialog != null) {
                    model.feed_id = feed_dialog.feed_id;
                }
                try {
                    model.details = new JSONObject().put("msg", msg).toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                model.id = MessageUtil.insertMessage(MsgAcceptedMsgActivity.this, model, otherModel);
                if (model.id >= 0 && netAvailable) {
                    boolean isSuccess = TcpProxy.getInstance().sendImTextMessage(otherModel.uid, model, what_message_from);
                    if (!isSuccess) {
                        MessageUtil.updateMessageFaileToDb(MsgAcceptedMsgActivity.this, -1, model.id);
                    } else {
                        insertFeedFlowMessageToDb();
                    }
                }
            });
        }
        emotionEditText.setText(null);
    }

    private void sendImageMsg(List<String> items) {
        //ArrayList<ImageItem> selectedList = ImageFetcher.getInstance().getSelectedImageList();
//        for (int i = 0; i < items.size(); i++) {
//            uploadImgBySCS(items.get(i));
//        }
        //ImageFetcher.getInstance().getSelectedImageList().clear();


        Observable<ImageItem> observable = ChatImageWrapper.createCompressObservable(items);
        Subscription subscription = observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<ImageItem>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(ImageItem rst_image) {
                if (rst_image != null) {
                    uploadImgBySCS(rst_image);
                }
            }
        });
        mSubscriptions.add(subscription);
    }

    private void sendRedBagMessage(PacketIconModel packetIconModel) {
        final boolean netAvailable = PWUtils.isNetWorkAvailable(MsgAcceptedMsgActivity.this);
        final MessageModel model = createPacketMsgModel();
        final JSONObject im_packetJson = new JSONObject();
        final JSONObject detailsObject = new JSONObject();

        try {
            im_packetJson.put("packet_id", packetIconModel.id);
            im_packetJson.put("icon_url", packetIconModel.send_icon);
            im_packetJson.put("msg", packetIconModel.msg);
            detailsObject.put("im_packet", im_packetJson);
            model.details = detailsObject.toString();
            MessageUtil.updateDialogDetails(model.id, model.details);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //update db
        if (model.id >= 0 && netAvailable) {
            boolean isSuccess = TcpProxy.getInstance().sendImTextMessage(otherModel.uid, model, what_message_from);
            if (!isSuccess) {
                MessageUtil.updateMessageFaileToDb(MsgAcceptedMsgActivity.this, -1, model.id);
            } else {
                insertFeedFlowMessageToDb();
            }
        }
    }

    private void onGrabPacket() {
        adapter.setOnGrabPacketListener((packet) -> {
            ArrayList<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("packet_id", packet.id));
            ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_PERSONAL_PACKET_GRAB, new MsgStructure() {

                @Override
                public void onReceive(JSONObject data) {

                }

                @Override
                public void onError(int error, Object ret) {
                    Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                        dismissAnimLoading();
                        showErrorToast(ret, getString(R.string.redbag_invalid));
                    });
                }
            });
        });
    }

    private void uploadImgBySCS(final ImageItem imageItem) {
        imageItem.setUid(meModel.uid);
        show_prompt = false;
        final boolean netAvailable = PWUtils.isNetWorkAvailable(MsgAcceptedMsgActivity.this);
        // 2015-01-15 11:00:10
        final MessageModel model = createImgMsgModel();
        final JSONObject im_imageJson = new JSONObject();
        final JSONObject detailsObject = new JSONObject();
        try {
            im_imageJson.put("local_path", imageItem.sourcePath);
            int[] wandh = PWUtils.getImageUrl(imageItem.sourcePath);
            if (wandh != null && wandh.length >= 2) {
                im_imageJson.put("width", wandh[0]);
                im_imageJson.put("height", wandh[1]);
            }
            im_imageJson.put("thumbnail_url", "");
            im_imageJson.put("image_url", "");

            detailsObject.put("im_image", im_imageJson);
            model.details = detailsObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PWUploader.K_UPLOAD_TYPE, PWUploader.UPLOAD_TYPE_AVATAR));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_QINIU_TOKEN, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                PWUploader uploader = PWUploader.getInstance();
                uploader.add(imageItem.sourcePath, data.optString("key"), data.optString("token"), new UploadCallback() {
                    @Override
                    public void onComplete(String key, ResponseInfo responseInfo, JSONObject jsonObject) {
                        imageItem.imageKey = key;
                        try {
                            im_imageJson.put("thumbnail_url", data.optString("thumbnail_url"));
                            im_imageJson.put("image_url", data.optString("url"));
                            detailsObject.put("im_image", im_imageJson);
                            model.details = detailsObject.toString();
                            MessageUtil.updateDialogDetails(model.id, model.details);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //update db
                        if (model.id >= 0 && netAvailable) {
                            boolean isSuccess = TcpProxy.getInstance().sendImTextMessage(otherModel.uid, model, what_message_from);
                            if (!isSuccess) {
                                MessageUtil.updateMessageFaileToDb(MsgAcceptedMsgActivity.this, -1, model.id);
                            } else {
                                insertFeedFlowMessageToDb();
                            }
                        }
                    }

                    @Override
                    public void onFailure(String key, ResponseInfo responseInfo, JSONObject jsonObject) {

                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });

//        mExecutorService.execute(() -> {
//            boolean isSuccess = SCSUpload.uploadFile(imageItem.sourcePath, imageItem.imageKey, SCSUpload.FileType.IMAGE);
//            CustomLog.i("MsgAcceptedMsgActivity.uploadImgBySCS(), run(), isSuccess : " + isSuccess);
//            if (isSuccess) {
//                try {
//                    if (PeiwoApp.getApplication().isOnLineEnv()) {
//                        im_imageJson.put("thumbnail_url", DfineAction.SOHU_THUMB_BASE_URL + imageItem.imageKey);
//                        im_imageJson.put("image_url", DfineAction.SOHU_BASE_URL + imageItem.imageKey);
//                    } else {
//                        im_imageJson.put("thumbnail_url", DfineAction.SOHU_THUMB_BASE_URL_DEBUG + imageItem.imageKey);
//                        im_imageJson.put("image_url", DfineAction.SOHU_BASE_URL_DEBUG + imageItem.imageKey);
//                    }
//                    detailsObject.put("im_image", im_imageJson);
//                    model.details = detailsObject.toString();
//                    MessageUtil.updateDialogDetails(model.id, model.details);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                //update db
//                if (model.id >= 0 && netAvailable) {
//                    isSuccess = TcpProxy.getInstance().sendImTextMessage(otherModel.uid, model, what_message_from);
//                    if (!isSuccess) {
//                        MessageUtil.updateMessageFaileToDb(MsgAcceptedMsgActivity.this, -1, model.id);
//                    } else {
//                        insertFeedFlowMessageToDb();
//                    }
//                }
//            }
//        });
    }

    private MessageModel createImgMsgModel() {
        boolean netAvailable = PWUtils.isNetWorkAvailable(MsgAcceptedMsgActivity.this);
        long currentTime = System.currentTimeMillis() + SharedPreferencesUtil.getLongExtra(PeiwoApp.getApplication(), AsynHttpClient.KEY_CC_CURRENT_TIME, 0);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = df.format(new Date(currentTime));
        long randomCount = (long) (Math.random() * 10000) * 1000000000l;
        currentTime %= 1000000000l;
        currentTime += randomCount;
        final MessageModel model = new MessageModel();
        model.content = getString(R.string.picture);
        model.dialog_type = MessageModel.DIALOG_TYPE_IMAGE_MESSAGE;
        model.msg_id = msg_id;
        if (netAvailable) {
            model.send_status = MessageModel.SEND_STATUS_SUCCESS;
        } else {
            model.send_status = MessageModel.SEND_STATUS_FAIL;
        }
        model.uid = otherModel.uid;
        model.update_time = currentDate;
        model.dialog_id = -currentTime;
        model.type = 0;
        if (feed_dialog != null) {
            model.feed_id = feed_dialog.feed_id;
        }
        model.id = MessageUtil.insertMessage(MsgAcceptedMsgActivity.this, model, otherModel);
        return model;
    }

    private MessageModel createPacketMsgModel() {
        boolean netAvailable = PWUtils.isNetWorkAvailable(MsgAcceptedMsgActivity.this);
        long currentTime = System.currentTimeMillis() + SharedPreferencesUtil.getLongExtra(PeiwoApp.getApplication(), AsynHttpClient.KEY_CC_CURRENT_TIME, 0);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = df.format(new Date(currentTime));
        long randomCount = (long) (Math.random() * 10000) * 1000000000l;
        currentTime %= 1000000000l;
        currentTime += randomCount;
        final MessageModel model = new MessageModel();
        model.content = getString(R.string.redbag);
        model.dialog_type = MessageModel.DIALOG_TYPE_IM_PACKET;
        model.msg_id = msg_id;
        if (netAvailable) {
            model.send_status = MessageModel.SEND_STATUS_SUCCESS;
        } else {
            model.send_status = MessageModel.SEND_STATUS_FAIL;
        }
        model.uid = otherModel.uid;
        model.update_time = currentDate;
        model.dialog_id = -currentTime;
        model.type = 0;
        if (feed_dialog != null) {
            model.feed_id = feed_dialog.feed_id;
        }
        model.id = MessageUtil.insertMessage(MsgAcceptedMsgActivity.this, model, otherModel);
        return model;
    }

//    private void uploadImgByQiniu(final ImageItem imageItem) {
//        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
//        params.add(new BasicNameValuePair("type", String.valueOf(QiniuUpload.UPLOAD_TYPE_IMAGE)));
//        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_QINIU_TOKEN, new MsgStructure() {
//            @Override
//            public void onReceive(JSONObject data) {
//                CustomLog.d("uploadImage, onReceive. data is : " + data);
//                String token = data.optString("token");
//                String key = data.optString("key");
//                String image_url = data.optString("url");
//                String thumbnail_url = data.optString("thumbnail_url");
//                QiniuUpload.uploadFile(imageItem.sourcePath, token, key, getOnUploadDone(image_url, thumbnail_url));
//            }
//
//            private QiniuUpload.UploadWorkListener getOnUploadDone(final String image_url, final String thumbnail_url) {
//                return new QiniuUpload.UploadWorkListener() {
//                    @Override
//                    public void onUploadDone(String key) {
//                        CustomLog.d("uploadImgByQiniu, onUploadDone.");
//                        File file = new File(imageItem.sourcePath);
//                        String md5_code = "";
//                        try {
//                            md5_code = Md5Util.md5Hex(file);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        final boolean netAvailable = PWUtils.isNetWorkAvailable(MsgAcceptedMsgActivity.this);
//                        final MessageModel model = createImgMsgModel();
//                        final JSONObject im_imageJson = new JSONObject();
//                        final JSONObject detailsObject = new JSONObject();
//                        try {
//                            im_imageJson.put("local_path", imageItem.sourcePath);
//                            int[] wandh = PWUtils.getImageUrl(imageItem.sourcePath);
//                            if (wandh != null && wandh.length >= 2) {
//                                im_imageJson.put("width", wandh[0]);
//                                im_imageJson.put("height", wandh[1]);
//                            }
//                            im_imageJson.put("thumbnail_url", "");
//                            im_imageJson.put("image_url", "");
//
//                            detailsObject.put("im_image", im_imageJson);
//                            model.details = detailsObject.toString();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                        try {
//                            im_imageJson.put("thumbnail_url", thumbnail_url);
//                            im_imageJson.put("image_url", image_url);
//                            im_imageJson.put("md5_code", md5_code);
//                            detailsObject.put("im_image", im_imageJson);
//                            model.details = detailsObject.toString();
//                            MessageUtil.updateDialogDetails(model.id, model.details);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                        //update db
//                        if (model.id >= 0 && netAvailable) {
//                            boolean isSuccess = TcpProxy.getInstance().sendImTextMessage(otherModel.uid, model, what_message_from);
//                            if (!isSuccess) {
//                                MessageUtil.updateMessageFaileToDb(MsgAcceptedMsgActivity.this, -1, model.id);
//                            } else {
//                                insertFeedFlowMessageToDb();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onUploadFail(String errorStr) {
//                        CustomLog.d("uploadImgByQiniu, onUploadFail, error str is : " + errorStr);
//                    }
//                };
//            }
//
//            @Override
//            public void onError(int error, Object ret) {
//                CustomLog.d("onError. ret is : " + ret);
//            }
//        });
//    }

    private void sendGifFaceMsg(final String gifFaceText) {
        PeiwoApp.getApplication().mExecutorService.execute(() -> {
            show_prompt = false;
            boolean netAvailable = PWUtils.isNetWorkAvailable(MsgAcceptedMsgActivity.this);
            long currentTime = System.currentTimeMillis() + SharedPreferencesUtil.getLongExtra(PeiwoApp.getApplication(), AsynHttpClient.KEY_CC_CURRENT_TIME, 0);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDate = df.format(new Date(currentTime));
            long randomCount = (long) (Math.random() * 10000) * 1000000000l;
            currentTime %= 1000000000l;
            currentTime += randomCount;
            // 2015-01-15 11:00:10
            MessageModel model = new MessageModel();
            model.content = gifFaceText;
            model.dialog_type = MessageModel.DIALOG_TYPE_IM;
            model.msg_id = msg_id;
            if (netAvailable) {
                model.send_status = MessageModel.SEND_STATUS_SUCCESS;
            } else {
                model.send_status = MessageModel.SEND_STATUS_FAIL;
            }
            model.uid = otherModel.uid;
            model.update_time = currentDate;
            model.dialog_id = -currentTime;
            model.type = 0;
            if (feed_dialog != null) {
                model.feed_id = feed_dialog.feed_id;
            }
            try {
                model.details = new JSONObject().put("msg", model.content).toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            model.id = MessageUtil.insertMessage(MsgAcceptedMsgActivity.this, model, otherModel);
            if (model.id >= 0 && netAvailable) {
                boolean isSuccess = TcpProxy.getInstance().sendImTextMessage(otherModel.uid, model, what_message_from);
                if (!isSuccess) {
                    MessageUtil.updateMessageFaileToDb(MsgAcceptedMsgActivity.this, -1, model.id);
                } else {
                    insertFeedFlowMessageToDb();
                }
            }
        });
    }

    public void resendMsg(MsgAcceptModel model) {
        boolean netAvailable = PWUtils.isNetWorkAvailable(MsgAcceptedMsgActivity.this);
        if (!netAvailable) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(PWDBConfig.DialogsTable.SEND_STATUS, 2);
        int count = MessageUtil.updateMessage(this, model.id, values);

        MessageModel sendModel = new MessageModel();
        sendModel.content = model.content;
        sendModel.dialog_type = model.dialog_type;
        sendModel.msg_id = msg_id;
        sendModel.send_status = MessageModel.SEND_STATUS_DEFAULT;
        sendModel.uid = otherModel.uid;
        sendModel.update_time = model.update_time;
        sendModel.dialog_id = model.dialog_id;
        sendModel.id = model.id;
        if (count > 0) {
            boolean isSuccess = TcpProxy.getInstance().sendImTextMessage(
                    otherModel.uid, sendModel, what_message_from);
            if (!isSuccess) {
                MessageUtil.updateMessageFaileToDb(MsgAcceptedMsgActivity.this,
                        -1, model.id);
            }
        }

    }

    public void resendImg(MsgAcceptModel model) {
        String local_path;
        if (TextUtils.isEmpty(model.local_path))
            return;
        local_path = model.local_path;
        ImageItem item = new ImageItem();
        item.sourcePath = local_path;
        //item.sourcePath = model.local_path;
        uploadImgBySCS(item);
    }


    private void checkCallPermission() {
        if (PeiwoApp.getApplication().getNetType() == NetUtil.NO_NETWORK) {
            showToast(this, "网络连接失败");
            return;
        }
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        if (app.getIsCalling()) {
            showToast(this, "您当前正在通话");
            return;
        }
        startCall();
    }

    private void startCall() {
        Intent intent = new Intent(MsgAcceptedMsgActivity.this, RealCallActivity.class);
        intent.putExtra("face_url", otherModel.avatar_thumbnail);
        intent.putExtra("gender", otherModel.gender);
        intent.putExtra("address", String.format(Locale.getDefault(), "%s %s", otherModel.province, otherModel.city));
        intent.putExtra("age", TimeUtil.getAgeByBirthday(otherModel.birthday));
        intent.putExtra("tid", otherModel.uid);
        intent.putExtra("uname", otherModel.name);
        intent.putExtra("slogan", otherModel.slogan);
        intent.putExtra("tags", otherModel.tags);
        intent.putExtra("flag", DfineAction.OUTGOING_CALL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        prepareCalling(meModel.uid, otherModel.uid, mPermission, otherModel.getPriceFloat(), intent,
                new OnCallPreparedListener() {
                    @Override
                    public void onCallPreparedSuccess(int permission,
                                                      float price) {
                        otherModel.price = String.valueOf(price);
                    }

                    @Override
                    public void onCallPreparedError(int error, Object ret) {
                    }
                });
    }

    @Override
    public void finish() {
        if (mSubscriptions != null && !mSubscriptions.isUnsubscribed()) {
            mSubscriptions.unsubscribe();
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        getSupportLoaderManager().destroyLoader(loaderId);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        if (badgeObserver != null) {
            getContentResolver().unregisterContentObserver(badgeObserver);
        }
        EventBus.getDefault().unregister(this);
        PeiwoApp.getApplication().removeNetworkCallBack(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mDetector != null) {
            if (!mDetector.interceptBackPress()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            boolean isVisib = face_lay.getVisibility() == View.VISIBLE || image_quick_switch.getVisibility() == View.VISIBLE;
//            if (image_quick_switch.getVisibility() == View.VISIBLE) {
//                image_quick_switch.setVisibility(View.GONE);
//            }
//            if (face_lay.getVisibility() == View.VISIBLE) {
//                closeFace(3);
//            }
//            if (isVisib) return false;
////            else if (msg_more_type_layout.getVisibility() == View.VISIBLE) {
////                msg_more_type_layout.setVisibility(View.GONE);
////                return false;
////            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onResume() {
        Uid = String.valueOf(otherModel.uid);
        dbService.cancelIMNotification();
        if (otherModel.uid != 0) {
            PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    dbService.clearBadgeByUid(String.valueOf(otherModel.uid));
                }
            });
        }
        super.onResume();
        setBarTitle(UserManager.getRealName(otherModel.uid, otherModel.name, this), otherModel.avatar_thumbnail);
        boolean isGroup = getIntent().getBooleanExtra(GroupHomePageActvity.KEY_IS_GROUP, false);
        if (!isGroup) {
            getUserInfo();
        }
        if (!Uid.equals("0")) {
            PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    JSONArray responseArray = dbService.getMsgId(Uid);
                    if (responseArray != null && responseArray.length() != 0)
                        TcpProxy.getInstance().receiveMessageResponse(responseArray);
                }
            });
        }
    }

    @Override
    public void onPause() {
        Uid = "0";
        if (send_message_layout.isShown()) {
            inputMethodManager.hideSoftInputFromWindow(emotionEditText.getWindowToken(), 0);
        }
        super.onPause();
    }

    /**
     * 获取热度
     */
    public void getUserInfo() {
        if (otherModel.uid == DfineAction.SYSTEM_UID) {
            return;
        }
        ApiRequestWrapper.getUserInfo(this, UserManager.getUid(getApplicationContext()), String.valueOf(Uid),
                new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        if (data != null && mHandler != null) {
                            otherModel = new PWUserModel(data);
                            CustomLog.d("getUserInfo. other model avatar is : " + otherModel.avatar_thumbnail);
                            Message msg = mHandler.obtainMessage();
                            Bundle b = new Bundle();
                            b.putInt("hot_value", data.optInt("hot_value"));
                            b.putInt("relation", data.optInt("relation"));
                            b.putInt("left_msg_nums", data.optInt("left_msg_nums"));
                            msg.setData(b);
                            msg.what = WHAT_DATA_RECEIVE_HOTVALUE_SUCCESS;
                            mHandler.sendMessage(msg);
                        }
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        if (mHandler == null)
                            return;
                        if (error == AsynHttpClient.PW_RESPONSE_DATA_NOT_AVAILABLE) {//判断用户是否被封了
                            mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_USER_NOT_AVAILABLE);
                        } else {
                            mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_HOTVALUE_ERROR);
                        }

                    }
                });
    }

    private void userNotAvailableDelay() {
        showToast(MsgAcceptedMsgActivity.this, "该用户已被封禁");
        dbService.deleteMessageByUid(String.valueOf(otherModel.uid));
        mHandler.postDelayed(() -> {
            Intent data = new Intent();
            data.putExtra("uid", Uid);
            setResult(RESULT_OK, data);

//            	dbService.deleteMessageByUid(Uid);

            finish();
        }, 1500);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CustomLog.i("onActivityResult, reqCode : " + requestCode + "\t resultCode : " + resultCode + "\t data : " + data);
        if (requestCode == REQUESTCODE_USERINFO) {
            if (resultCode == RESULT_OK) {
                if (data == null)
                    return;
                String name = data.getStringExtra("name");
                String avatar = data.getStringExtra("avatar");
                mPermission = data.getIntExtra("permission", 0);
                if (!TextUtils.isEmpty(name))
                    otherModel.name = name;
                if (!TextUtils.isEmpty(avatar)) {
                    otherModel.avatar_thumbnail = avatar;
                }
            }
        } else if (requestCode == SELECT_IMAGE_COMPLETE) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> items = data.getStringArrayListExtra(AlbumCompatActivity.K_ALBUM_RST);
                checkImagePermission(items);
            }
//            else if (resultCode == RESULT_OK) {
//                takePhoto();
//            }
        } else if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            if (!TextUtils.isEmpty(photoPath)) {
                sendPhotoMsg();
            }
        } else if (requestCode == REQUECT_CODE_IM_ACTION) {
            if (resultCode == RESULT_OK && data != null) {
                handleIMActionResult(data);
            } else if (resultCode == RESULT_FIRST_USER) {
                finish();
            }
        } else if (requestCode == REQUEST_CODE_REDBAG) {
            if (resultCode == RESULT_OK) {
                PacketIconModel packet = data.getParcelableExtra(IMChatRedbagActivity.K_SINGLE_PACKET);
                sendRedBagMessage(packet);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleIMActionResult(Intent data) {
        int v_flag = data.getIntExtra(MsgAcceptActionActivity.A_FLAG, -1);
        if (v_flag == MsgAcceptActionActivity.V_FLAG_USERINFO) {
            return;
        }
        switch (v_flag) {
            case MsgAcceptActionActivity.V_FLAG_UPDATE_NOTE:
                //修改备注
                String user_note = UserManager.getNoteByUid(otherModel.uid, this);
                setBarTitle(user_note, otherModel.avatar_thumbnail);
                otherModel.remark = user_note;
                break;

            case MsgAcceptActionActivity.V_FLAG_DELETE_IMLOGS:
                //showToast(this, "delete messages");
                break;
            case MsgAcceptActionActivity.V_FLAG_LAHEI:
                finish();
                break;
            case MsgAcceptActionActivity.V_FLAG_LAHEI_REPORT:
                //举报回调
                break;
            case MsgAcceptActionActivity.V_FLAG_DELETE_FRIEND:
                finish();
                break;
        }
    }

    private void sendPhotoMsg() {
        File file = new File(photoPath);
        file.mkdirs();
        ImageItem item = new ImageItem();
        item.sourcePath = photoPath;
        uploadImgBySCS(item);
    }

    private static final int TAKE_PICTURE = 0x000000;
    //testing
    private static String photoPath = "";

//    /**
//     * 调用系统相机
//     */
//    public void takePhoto() {
//        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        File vFile = new File(FileManager.getTempFilePath(), System.currentTimeMillis() + Math.random() * 10000 + "");
//        if (!vFile.exists()) {
//            File vDirPath = vFile.getParentFile();
//            vDirPath.mkdirs();
//        } else {
//            if (vFile.exists()) {
//                vFile.delete();
//            }
//        }
//        photoPath = vFile.getPath();
//        CustomLog.d("cramePath == " + photoPath);
//        Uri cameraUri = Uri.fromFile(vFile);
//        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
//        startActivityForResult(openCameraIntent, TAKE_PICTURE);
//    }

    private ArrayList<Long> needUpdateDb = null;

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        String selection = PWDBConfig.DialogsTable.UID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(otherModel.uid)};
        String sortOrder = PWDBConfig.DialogsTable.UPDATE_TIME + " ASC";
        return new CursorLoader(this, PWDBConfig.DialogsTable.CONTENT_URI,
                null, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        String preTime = "-1";
        String currTime = "0";
        List<MsgAcceptModel> list = new ArrayList<MsgAcceptModel>();
        needUpdateDb.clear();
        boolean needAddFeedDialog = true;
        while (cursor.moveToNext()) {
            //String content = cursor.getString(cursor.getColumnIndex(PWDBConfig.DialogsTable.CONTENT));
            MsgAcceptModel model = new MsgAcceptModel(meModel, cursor);
            currTime = model.update_time.substring(0, model.update_time.lastIndexOf(":"));
            if (!preTime.equals(currTime)) {
                preTime = currTime;
                model.displayTime = TimeUtil.getMsgTimeDisplay(model.update_time, true);
            } else {
                model.displayTime = "";
            }
            if (isFirst && model.send_status == MessageModel.SEND_STATUS_SUCCESS) {
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date d;
                try {
                    d = sf.parse(model.update_time);
                    long t = d.getTime();
                    long currentTime = System.currentTimeMillis() + SharedPreferencesUtil.getLongExtra(
                            PeiwoApp.getApplication(), AsynHttpClient.KEY_CC_CURRENT_TIME, 0);
                    if (currentTime - t > 30000) {
                        needUpdateDb.add(model.id);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//			feed_dialog.feed_id.equals(model.feed_id)
            if (feed_dialog != null) {
                if (model.update_time.compareTo(feed_dialog.update_time) > 0) {
                    list.add(feed_dialog);
                }
                if (feed_dialog.feed_id != null && feed_dialog.feed_id.equals(model.feed_id)) {
                    needAddFeedDialog = false;
                }
            }
            CustomLog.d("dialog_type is : " + model.dialog_type);
            list.add(model);
        }
        mList.clear();
        mList.addAll(list);
        if (mList.size() == 0) {
            boolean isNeedShowGuide = true;
        }
        if (feed_dialog != null) {
            if (needAddFeedDialog) {
                if (!mList.contains(feed_dialog)) {
                    mList.add(feed_dialog);
                }
            } else {
                list.remove(feed_dialog);
                feed_dialog = null;
            }
        }

        if (show_prompt) {
            MsgAcceptModel model = new MsgAcceptModel();
            model.view_type = MsgAcceptAdapter.VIEW_TYPE_OTHER;
            model.dialog_type = MessageModel.DIALOG_TYPE_TIP;
            model.content = "提示:在你回复之前,对方不能查看你的资料";
            mList.add(model);
        }
        if (mList.size() > lastItemCount + 1 && needAddAttentionDialog
                && otherModel.relation != 0 && otherModel.relation != 2
                && mList.get(mList.size() - 1).dialog_type != MessageModel.DIALOG_TYPE_TIP
                && mList.get(mList.size() - 1).dialog_type != MessageModel.DIALOG_TYPE_HOTVALUE) {
            insertAttentionDialog();
            needAddAttentionDialog = false;
        }

        adapter.notifyDataSetChanged();
        //lv_msgaccepted.setSelection(adapter.getCount() - 1);
        isFirst = false;
        if (needUpdateDb.size() > 0) {
            new Thread(() -> {
                for (int i = 0; i < needUpdateDb.size(); i++) {
                    MessageUtil.updateMessageFaileToDb(PeiwoApp.getApplication(), -1, needUpdateDb.get(i));
                }
            }).start();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        adapter.notifyDataSetChanged();
    }


    @Override
    public void getSelfNetworkType(int type) {
        if (type != NetUtil.NO_NETWORK) {
            boolean hasGetPermission = false;
            if (!hasGetPermission && lastNetType != type) {
                getPermission(false, true);
            }
        }
        lastNetType = type;
    }

    /**
     * 收到发送文本成功的回执
     */
    public void onEventMainThread(SendMsgSuccessEvent event) {
        if (leftMsgNum != 0) {
            leftMsgNum--;
        } else {
            getUserInfo();
        }
    }

    /**
     * 收到热度改变的应答
     */
    private boolean needAddAttentionDialog = false;
    private int lastItemCount = 0;

    public void onEventMainThread(HotValueChangeEvent event) {
//        Intent intent = event.intent;
//        hotValue = intent.getIntExtra("hot_values", 0);
//        if (hotValue == 0) {
//            setHotFire(false, false, false);
//        } else if (hotValue == 1) {
//            setHotFire(true, false, false);
//        } else if (hotValue == 2) {
//            setHotFire(true, true, false);
//        } else if (hotValue == 3) {
//            setHotFire(true, true, true);
//            needAddAttentionDialog = true;
//            lastItemCount = adapter.getCount();
//        }
        //getUserInfo();
    }

    private void insertAttentionDialog() {
        long currentTime = System.currentTimeMillis() + SharedPreferencesUtil.getLongExtra(PeiwoApp.getApplication(), AsynHttpClient.KEY_CC_CURRENT_TIME, 0);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = df.format(new Date(currentTime));
        long randomCount = (long) (Math.random() * 10000) * 1000000000l;
        currentTime %= 1000000000l;
        currentTime += randomCount;
        // 2015-01-15 11:00:10
        MessageModel model = new MessageModel();
        model.dialog_type = MessageModel.DIALOG_TYPE_ATTENTION;
        model.msg_id = msg_id;
        model.send_status = MessageModel.SEND_STATUS_FAIL;
        model.uid = otherModel.uid;
        model.update_time = currentDate;
        model.dialog_id = -currentTime;
        model.type = 0;
        model.id = MessageUtil.insertMessage(MsgAcceptedMsgActivity.this, model, otherModel);
    }

    /**
     * 解除黑名单
     *
     * @param uid
     */
    private void relieveBlock(final String uid) {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(AsynHttpClient.KEY_TUID, String.valueOf(uid)));
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_CONTACT_UNBLOCK, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Message msg = mHandler.obtainMessage();
                msg.what = WHAT_RELIEVE_BLOCK_COMPLETE;
                msg.obj = uid;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
            }
        });
    }

    public void onEventMainThread(SendMsgErrorEvent event) {
        Intent intent = event.errorMsgIntent;
        int fail_type = intent.getIntExtra("fail_type", 0);
        String errorMsg = intent.getStringExtra("errorMsg");
        if (fail_type == 10) {
            //在自己的黑名单中
            UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEBLACKLIST);
            new AlertDialog.Builder(this).setTitle("提示").setMessage(errorMsg).setPositiveButton("确定", (dialog, which) -> {
                if (!Uid.equals("0")) {
                    relieveBlock(Uid);
                }
            }).setNegativeButton("取消", null).create().show();
            return;
        }
        showToast(getApplicationContext(), errorMsg);
    }

    /**
     * 隐藏表情选择框
     */
    private void showFaceView() {
        inputMethodManager.hideSoftInputFromWindow(emotionEditText.getWindowToken(), 0);
        face_lay.setVisibility(View.VISIBLE);
    }


    private void closeFace(int stauts) {
        if (stauts == 1) {
            inputMethodManager.hideSoftInputFromWindow(emotionEditText.getWindowToken(), 0);
        } else if (stauts == 2) {// 点击输入框，隐藏表情，调出输入法。
            face_lay.setVisibility(View.GONE);
            image_quick_switch.setVisibility(View.GONE);
            emotionEditText.setFocusableInTouchMode(true);
            emotionEditText.requestFocus();
            mHandler.postDelayed(() -> inputMethodManager.showSoftInput(emotionEditText, 0), 500);
        } else {// 打开表情的状态下，按返回键。隐藏表情。
            face_lay.setVisibility(View.GONE);
        }
        //iv_expression.setImageResource(R.drawable.icon_expression);
    }

    private void createFeedFlowMessage() {
        long currentTime = System.currentTimeMillis()
                + SharedPreferencesUtil.getLongExtra(PeiwoApp.getApplication(),
                AsynHttpClient.KEY_CC_CURRENT_TIME, 0);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = df.format(new Date(currentTime));
        long randomCount = (long) (Math.random() * 10000) * 1000000000l;
        currentTime %= 1000000000l;
        currentTime += randomCount;
        // 2015-01-15 11:00:10
        String title = getIntent().getStringExtra("topic_title");
        String content = getIntent().getStringExtra("topic_content");
        String icon_url = getIntent().getStringExtra("icon_url");
        String feed_id = getIntent().getStringExtra("feed_id");
        if (feed_dialog == null) {
            feed_dialog = new MsgAcceptModel();
        }
        feed_dialog.id = -1;
        feed_dialog.content = content;
        feed_dialog.dialog_id = (int) -currentTime;
        feed_dialog.update_time = currentDate;
        feed_dialog.dialog_type = MessageModel.DIALOG_TYPE_PACKAGE;

        try {
            feed_dialog.redbag_title = title;
            feed_dialog.redbag_content = content;
            feed_dialog.redbag_extra = content;
            feed_dialog.icon_url = icon_url;
            feed_dialog.url = "";
            feed_dialog.icon_name = "";
            feed_dialog.feed_id = feed_id;

            JSONObject detailsJson = new JSONObject();
            detailsJson.put("title", title);
            detailsJson.put("msg", content);
            detailsJson.put("extra", content);
            detailsJson.put("icon_url", icon_url);
            detailsJson.put("feed_id", feed_id);
            feed_dialog.details = detailsJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        feed_dialog.view_type = MsgAcceptAdapter.VIEW_TYPE_OTHER_FEED;
    }

    private void insertFeedFlowMessageToDb() {
        if (feed_dialog != null) {
            MessageModel imodel = new MessageModel();
            imodel.dialog_type = MessageModel.DIALOG_TYPE_PACKAGE;
            imodel.msg_id = msg_id;
            imodel.uid = otherModel.uid;
            imodel.update_time = feed_dialog.update_time;
            imodel.dialog_id = feed_dialog.dialog_id;
            imodel.type = 1;
            imodel.details = feed_dialog.details;
            MessageUtil.insertMessage(MsgAcceptedMsgActivity.this, imodel, otherModel);
            feed_dialog = null;
        }
    }


    public void onEventMainThread(FocusEvent event) {
        int type = event.type;
        dismissAnimLoading();
        if (type == FocusEvent.FOCUS_SUCCESS_EVENT) {
            // 关注成功
            MessageUtil.updateMessageSuccessToDb(this, (int) focusId, msg_id, focusDialogId);
        } else {
            String err_msg = event.err_msg;
            if (!TextUtils.isEmpty(err_msg)) {
                showToast(this, err_msg);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.resetToIdle();
        }
    }

    public void onEventMainThread(MessagePushEvent event) {
        Uid = String.valueOf(event.getTuid());
        getUserInfo();
    }


    private void startFeedbackActivity() {
        UserInfo info = feedbackAgent.getUserInfo();
        if (info == null)
            info = new UserInfo();
        Map<String, String> contact = info.getContact();
        if (contact == null)
            contact = new HashMap<>();
        contact.put("peiwo_id", String.valueOf(UserManager.getPWUser(this).uid));

        info.setContact(contact);
        feedbackAgent.setUserInfo(info);
        feedbackAgent.startFeedbackActivity();
    }

    public void onEventMainThread(Intent it) {
        if (Constans.ACTION_SEND_IMG_PERMISSION.equals(it.getAction())) {
            int callee = it.getIntExtra("callee", 0);
            CustomLog.d("countDown you can send with uid : " + callee);
            if (callee == otherModel.uid) {
                img_permission = true;
            }
        }
    }
}
