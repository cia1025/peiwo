package me.peiwo.peiwo.activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.db.BriteDBHelperHolder;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.FocusEvent;
import me.peiwo.peiwo.eventbus.event.RedPonitVisibilityEvent;
import me.peiwo.peiwo.eventbus.event.UserInfoEvent;
import me.peiwo.peiwo.fragment.RecorderDialogFragment;
import me.peiwo.peiwo.model.PWContactsModel;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.NetUtil;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.SharedPreferencesUtil;
import me.peiwo.peiwo.util.TimeUtil;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.util.UmengStatisticsAgent;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.FlowLayout;
import me.peiwo.peiwo.widget.GenderWithAgeView;
import me.peiwo.peiwo.widget.ProfileFaceGridView;
import me.peiwo.peiwo.widget.RemarksContactView;
import rx.Observable;
import rx.Subscription;

public class UserInfoActivity
        extends PWPreCallingActivity implements
        ProfileFaceGridView.OnImgItemClickListener {


    //个人资料调接口传这两个参数，第一个是是否需要返回动态，第二个是是否返回星座信息，默认不返回，需要返回传‘1’
    public static final String K_NEED_FEED = "k_need_feed";
    public static final String K_NEED_ZODIAC = "k_need_zodiac";

    public static final String TARGET_UID = "tuid";
    public static final String TARGET_NAME = "tname";
    public static final String USER_INFO = "user_info";

    public static final String MESSAGE_FROM = "message_from";

    private static final int WHAT_DATA_RECEIVE_REPORT_SUCCESS = 5000;
    private static final int WHAT_DATA_RECEIVE_USER_NOT_AVAILABLE = 8000;
    private static final int WHAT_DATA_RECEIVE_BLOCK_SECCESS = 9000;
    private static final int WHAT_DATA_RECEIVE_BLOCK_FAILURE = 20002;
    private static final int WHAT_DATA_FOCUS_SUCCESS = 11000;
    private static final int WHAT_DATA_FOCUS_ERROR = 13000;
    private static final int WHAT_DATA_CANCELFOCUS_SUCCESS = 12000;
    public static final int REQUEST_STATE_CHECKED = 2;
    private static final String TAG = "UserInfoActivity";
    private static final int REQUEST_CODE_MSGACCEPT = 1001;
    private ProfileFaceGridView dgv_images;
    /**
     * 年龄
     **/
    private GenderWithAgeView v_gender_age;
    /**
     * 星座
     **/
    private TextView tv_constellation;
    /**
     * 通话价格
     **/
    private TextView tv_call_price;
    /**
     * 通话价格控件
     **/
    private View tv_call_price_layout;

    /**
     * 个人签名
     **/
    private TextView userinfo_slogan;

    /**
     * 陪我号
     **/
    private TextView userinfo_no_text;

    /**
     * 通话时长
     **/
    private TextView tv_call_time;
    /**
     * 情感状况
     **/
    private TextView userinfo_emotion;
    /**
     * 职业
     **/
    private TextView userinfo_profession;

    /**
     * 昵称
     **/
    private TextView tv_nickname;


    private FlowLayout userinfo_main_tag_container;

    private ArrayList<View> addViewList = null;


    private View connect_constellation_layout;//合拍星座

    private View userinfo_other_tags_title;
    private int mUid;
    private int tUid;
    private InfoHandler mHandler;
    private PWUserModel mModel;

    private LinearLayout userinfo_layout;
    private RelativeLayout rl_chat;
    private LinearLayout ll_chat_sendMsg;
    private LinearLayout ll_chat_follow_doblack;

    private ScrollView pullToRefreshScrollView;

    private MsgDBCenterService msgDBCenterService;


    private int relation = 1;
    private View userinfo_dynamics_layout;
    private ImageView userinfo_dynamic_image;
    private TextView userinfo_dynamic_text_view;

    public static final int RELATION_FRIENDS = 0;// 好友
    public static final int RELATION_STRANGER = 1;// 陌生人
    public static final int RELATION_FOLLOW = 2;// 我关注的对象
    public static final int RELATION_FANS = 3;// 我的粉丝
    public static final int RELATION_TROUBLESOME_PERSON = 4;// 拉黑/被拉黑

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info_activity);
        mUid = UserManager.getUid(this);
        Intent intent = getIntent();
        tUid = intent.getIntExtra(TARGET_UID, 0);
        mModel = (PWUserModel) intent.getSerializableExtra((UserInfoActivity.USER_INFO));
        setTitleBar(intent.getStringExtra(TARGET_NAME), false);
        mHandler = new InfoHandler(this);
        msgDBCenterService = MsgDBCenterService.getInstance();
        init();
        if (!isme()) {
            UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEUSER);
        } else {
            UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEUMEMYHOME);
        }

        EventBus.getDefault().register(this);
    }

    private boolean isme() {
        return mUid == tUid;
    }

    private void init() {


        TextView userinfo_dynamic_tags = (TextView) findViewById(R.id.userinfo_dynamic_tags);
        if (isme()) {
            userinfo_dynamic_tags.setText("我的小事");
        } else {
            userinfo_dynamic_tags.setText("三两小事");
        }

        dgv_images = (ProfileFaceGridView) findViewById(R.id.dgv_images);//头像

//        pfgv_trend = (PersionTrendView) findViewById(R.id.pfgv_trend);
        v_gender_age = (GenderWithAgeView) findViewById(R.id.v_gender_age);//性别
        tv_constellation = (TextView) findViewById(R.id.tv_constellation);
        tv_call_price = (TextView) findViewById(R.id.tv_call_price);
        tv_call_price_layout = (View) findViewById(R.id.tv_call_price_layout);

        userinfo_slogan = (TextView) findViewById(R.id.userinfo_slogan);
        userinfo_no_text = (TextView) findViewById(R.id.userinfo_no_text);


        userinfo_dynamics_layout = findViewById(R.id.userinfo_dynamics_layout);
        userinfo_dynamic_image = (ImageView) findViewById(R.id.userinfo_dynamic_image);
        userinfo_dynamic_text_view = (TextView) findViewById(R.id.userinfo_dynamic_text_view);

        userinfo_emotion = (TextView) findViewById(R.id.userinfo_emotion);
        userinfo_profession = (TextView) findViewById(R.id.userinfo_profession);

        userinfo_main_tag_container = (FlowLayout) findViewById(R.id.userinfo_main_tag_container);//null


        tv_call_time = (TextView) findViewById(R.id.tv_call_time);
        tv_nickname = (TextView) findViewById(R.id.tv_nickname);

        rl_chat = (RelativeLayout) findViewById(R.id.userinfo_chat);
        ll_chat_sendMsg = (LinearLayout) findViewById(R.id.userinfo_chat_sendMsg);
        ll_chat_follow_doblack = (LinearLayout) findViewById(R.id.userinfo_chat_follow_doblack);

        connect_constellation_layout = findViewById(R.id.userinfo_connect_constellation_layout);
        userinfo_other_tags_title = findViewById(R.id.userinfo_other_tags_title);
        userinfo_layout = (LinearLayout) findViewById(R.id.userinfo_layout);


//        if (mModel == null) {
        getUserInfo(tUid, true);
//        } else {
//            fillData();
//            getUserInfo(tUid, false);
//        }

        pullToRefreshScrollView = (ScrollView) findViewById(R.id.pullToRefreshScrollView);
    }


    private void setTitleBar(String tname, boolean hasDoActionMore) {
        if (isme()) {
            TitleUtil.setTitleBar(this, tname, (v) -> {
                finish();
            }, "编辑", (v) -> {
                Intent updateIntent = new Intent(this, UpdateProfileActivity.class);
                if (mModel != null)
                    updateIntent.putExtra("complement", mModel.complement);
                startActivity(updateIntent);
                finish();
                UmengStatisticsAgent.onEvent(this,
                        UMEventIDS.UMEUPDATEPROFILE);

            });
        } else {
            String remark = UserManager.getNoteByUid(tUid, this);
            if (!TextUtils.isEmpty(remark)) tname = remark;
            if (mModel != null && mModel.has_lazy_voice && mModel.isCharge && mModel.relation != RELATION_FRIENDS) {
                TitleUtil.setTitleBar(this, tname, (v) -> {
                            finish();
                        },
                        R.drawable.icon_info_call, (v) -> {
                            if (checkCallPermission()) {
                                startCall();
                            }
                        }
                );
            } else {
                if (hasDoActionMore) {
                    TitleUtil.setTitleBar(this, tname, (v) -> {
                        finish();
                    }, null);
//                "更多", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (relation == 0) {
//                            doActionMore();
//                        } else {
//                            doActionMoreWithOutReMark();
//                        }
//                    }
//                }
                } else {
                    TitleUtil.setTitleBar(this, tname, (v) -> {
                        finish();
                    }, null);
                }
            }
        }
    }

    private void getUserInfo(int tUid, boolean isshowprogress) {
        if (isshowprogress) {
            showAnimLoading();
        }
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("uid", String.valueOf(mUid)));
        params.add(new BasicNameValuePair("tuid", String.valueOf(tUid)));
        params.add(new BasicNameValuePair("need_voice", String.valueOf(1)));
        String need_feed = getIntent().getStringExtra(K_NEED_FEED);
        need_feed = need_feed == null ? "1" : need_feed;
        String need_zodiac = getIntent().getStringExtra(K_NEED_ZODIAC);
        need_zodiac = need_zodiac == null ? "1" : need_zodiac;
        if (!TextUtils.isEmpty(need_feed)) {
            params.add(new BasicNameValuePair("need_feed", need_feed));
        }
        if (!TextUtils.isEmpty(need_zodiac)) {
            params.add(new BasicNameValuePair("need_zodiac", need_zodiac));
        }
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_USERINFO_GETINFO, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("cc, onReceive, data is : " + data);
                mModel = new PWUserModel(data);
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE);
                if (isme()) {
                    SharedPreferencesUtil.putStringExtra(UserInfoActivity.this, AsynHttpClient.KEY_INFO_COMPLEMENT, String.valueOf(mModel.complement));
                }
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("get user info, onError. error code is : " + error + ", ret is : " + ret);
                if (error == AsynHttpClient.PW_RESPONSE_DATA_NOT_AVAILABLE) {//判断用户是否被封了
                    mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_USER_NOT_AVAILABLE);
                } else {
                    mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
                }
            }
        });
    }

    @Override
    public void onImgItemClick(int index) {
        Intent intent = new Intent(this, ImagePagerActivity.class);
        //intent.putExtra(ImagePagerActivity.KEY_URL_LIST, (ArrayList) mModel.images);
        intent.putParcelableArrayListExtra(ImagePagerActivity.KEY_URL_LIST, mModel.images);
        intent.putExtra(ImagePagerActivity.KEY_POS, index);
        startActivity(intent);
    }


    private static class InfoHandler extends Handler {
        WeakReference<UserInfoActivity> activity_ref;

        public InfoHandler(UserInfoActivity activity) {
            activity_ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final UserInfoActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) {
                return;
            }
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.dismissAnimLoading();
                    theActivity.fillData();
                    break;

                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.dismissAnimLoading();
                    theActivity.showToast(theActivity, "网络连接失败");
                    break;
                case WHAT_DATA_RECEIVE_USER_NOT_AVAILABLE:
                    theActivity.dismissAnimLoading();
                    theActivity.ll_chat_follow_doblack.setVisibility(View.GONE);
                    theActivity.rl_chat.setVisibility(View.GONE);
                    theActivity.ll_chat_sendMsg.setVisibility(View.GONE);
                    View v = theActivity.findViewById(R.id.btn_right);
                    if (v != null) v.setVisibility(View.GONE);
                    //申请通话的用户被封禁
                    theActivity.userNotAvailableDelay();
                    Intent Contactintent = new Intent();
                    Contactintent.putExtra("uid", theActivity.tUid);
                    Contactintent.putExtra("usernotavailable", true);
                    theActivity.setResult(RESULT_OK, Contactintent);
                    break;
                case WHAT_DATA_RECEIVE_REPORT_SUCCESS:
                    theActivity.showToast(theActivity, "举报成功");
                    theActivity.ll_chat_follow_doblack.setVisibility(View.VISIBLE);
                    theActivity.rl_chat.setVisibility(View.GONE);
                    theActivity.ll_chat_sendMsg.setVisibility(View.GONE);
                    break;
                case WHAT_DATA_RECEIVE_BLOCK_SECCESS:
                    theActivity.dismissAnimLoading();
                    theActivity.msgDBCenterService.deletePWContact(theActivity.tUid);
                    theActivity.showToast(theActivity, "拉黑成功");
                    theActivity.ll_chat_follow_doblack.setVisibility(View.VISIBLE);
                    theActivity.rl_chat.setVisibility(View.GONE);
                    theActivity.ll_chat_sendMsg.setVisibility(View.GONE);
                    break;
                case WHAT_DATA_RECEIVE_BLOCK_FAILURE:
                    theActivity.showToast(theActivity, "该用户已经被拉黑过");
                    break;
                case WHAT_DATA_FOCUS_SUCCESS:
                    //关注成功b
                    CustomLog.d("WHAT_DATA_FOCUS_SUCCESS. relation is : " + theActivity.relation);
                    if (theActivity.relation == 3) {
                        theActivity.showToast(theActivity, theActivity.getString(R.string.accept_success));
                    }
                    theActivity.getUserInfo(theActivity.tUid, true);
                    theActivity.setTitleBar(theActivity.mModel.name, true);
                    break;
                case WHAT_DATA_FOCUS_ERROR:
                    String error_msg = (String) msg.obj;
                    Toast.makeText(theActivity, error_msg, Toast.LENGTH_SHORT).show();
                    break;
                case WHAT_DATA_CANCELFOCUS_SUCCESS:
                    //取消关注成功
                    theActivity.getUserInfo(theActivity.tUid, true);
                    theActivity.setTitleBar(theActivity.mModel.name, false);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private void userNotAvailableDelay() {
        showToast(this, "该用户已被封禁");
        msgDBCenterService.deleteMessageByUid(String.valueOf(tUid));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1500);
    }

    private void fillData() {
//		if (isme()) {
//			int likeCount = SharedPreferencesUtil.getIntExtra(PeiwoApp.getApplication(), 
//					"like_num_" + UserManager.getUid(PeiwoApp.getApplication()), 0);
//			showLikeReadPonit(likeCount);
//		}
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(mModel.name);
        relation = mModel.relation;
        dgv_images.displayImagesForUserInfo(mModel.images);
        if (mModel.dynamicList == null) {
            if (isme()) {
                SharedPreferencesUtil.putBooleanExtra(this, AsynHttpClient.KEY_INFO_SHOW_DYNAMIC, false);
                EventBus.getDefault().post(new RedPonitVisibilityEvent(0));
                SharedPreferencesUtil.putIntExtra(PeiwoApp.getApplication(),
                        "like_num_" + UserManager.getUid(PeiwoApp.getApplication()), 0);
                EventBus.getDefault().post(new UserInfoEvent());
            }

            userinfo_dynamics_layout.setVisibility(View.GONE);
            //findViewById(R.id.userinfo_dynamics_line).setVisibility(View.GONE);

        } else {
            if (isme()) {
                SharedPreferencesUtil.putBooleanExtra(this, AsynHttpClient.KEY_INFO_SHOW_DYNAMIC, true);
                EventBus.getDefault().post(new UserInfoEvent());
            }

            userinfo_dynamics_layout.setVisibility(View.VISIBLE);
            //findViewById(R.id.userinfo_dynamics_line).setVisibility(View.VISIBLE);
            if (mModel.dynamicList.size() > 0) {
                if (TextUtils.isEmpty(mModel.dynamicList.get(0))) {
                    userinfo_dynamic_image.setImageResource(R.drawable.bg_font);
                } else {
                    ImageLoader.getInstance().displayImage(mModel.dynamicList.get(0), userinfo_dynamic_image);
                }
            }
            userinfo_dynamic_text_view.setVisibility(View.VISIBLE);
            userinfo_dynamic_text_view.setText(mModel.dynamicContent);
        }


        TextView tv_call_duration = (TextView) findViewById(R.id.tv_call_duration);
        if (isme()) {
            tv_call_duration.setText(TimeUtil.makeTimeString(this, mModel.call_duration));
        } else {
            if (mModel.xzList != null && mModel.xzList.size() > 0) {
                connect_constellation_layout.setVisibility(View.VISIBLE);
                //findViewById(R.id.userinfo_connect_constellation__line).setVisibility(View.VISIBLE);
                createXZViews(mModel.xzList);
            }
            if (mModel.call_duration == 0 || mModel.xzList == null || mModel.xzList.size() == 0) {
                tv_call_duration.setText("初来乍到，请多关照");
            }
        }


        dgv_images.setOnImgItemClickListener(this);
        if (isme()) {
            v_gender_age.displayGenderWithAge(mModel.gender, TimeUtil.getAgeByBirthday(mModel.birthday));
        } else {
            v_gender_age.displayGenderWithAge(mModel.gender, "");
        }
        //  v_gender_age.displayGenderWithAge(mModel.gender, TimeUtil.getAgeByBirthday(mModel.birthday));
        StringBuilder sb = new StringBuilder();
        String province = mModel.province;
        String city = mModel.city;
        System.out.println("UserInfoActivity.fillData(), province : " + province + "\t city : " + city);
        if (province != null && province.equals(city)) {
            city = "";
        }
        if (province != null && province.equals(" ")) {
            province = "";
        }
        if (TextUtils.isEmpty(mModel.xingzuo)) {
            sb.append(TimeUtil.getConstellation(mModel.birthday));
        } else {
            sb.append(mModel.xingzuo);
        }
        sb.append(" ").append(province).append(" ").append(city);
        tv_constellation.setText(sb.toString());
        setPrice();
        userinfo_slogan.setText(mModel.slogan);
        setTags();
        userinfo_no_text.setText(String.valueOf(mModel.uid));
        tv_call_time.setText(TimeUtil.makeTimeString(this, mModel.call_duration));
        //userinfo_emotion.setText(PWUtils.getEmotion(mModel.emotion));
        //userinfo_profession.setText(TextUtils.isEmpty(mModel.profession) ? "保密" : mModel.profession);
        if (!isme()) {
            CustomLog.d("fillData, relation is : " + relation);
            switch (relation) {
                case RELATION_TROUBLESOME_PERSON:// 拉黑/被拉黑
                case RELATION_STRANGER: // 陌生人
                case RELATION_FOLLOW:// 我关注的对象
                    ll_chat_follow_doblack.setVisibility(View.VISIBLE);
                    rl_chat.setVisibility(View.GONE);
                    ll_chat_sendMsg.setVisibility(View.GONE);
                    setBottomControl(ll_chat_follow_doblack);
                    setTitleBar(mModel.name, false);
                    break;
                case RELATION_FANS:// 我的粉丝
                    TextView tv = (TextView) ll_chat_follow_doblack.findViewById(R.id.userinfo_btn_follow).findViewById(R.id.tv_apply_or_accept);
                    tv.setText(getResources().getString(R.string.text_accept));
                    ll_chat_follow_doblack.setVisibility(View.VISIBLE);
                    rl_chat.setVisibility(View.GONE);
                    ll_chat_sendMsg.setVisibility(View.GONE);
                    setBottomControl(ll_chat_follow_doblack);
                    setTitleBar(mModel.name, false);
                    break;
//                case RELATION_FOLLOW:// 我关注的对象
//                    ll_chat_follow_doblack.setVisibility(View.GONE);
//                    rl_chat.setVisibility(View.VISIBLE);
//                    ll_chat_sendMsg.setVisibility(View.GONE);
//                    setBottomControl(rl_chat);
//                    setTitleBar(mModel.name, true);
//                    break;
                case RELATION_FRIENDS: // 好友
                    ll_chat_follow_doblack.setVisibility(View.GONE);
                    rl_chat.setVisibility(View.GONE);
                    ll_chat_sendMsg.setVisibility(View.VISIBLE);
                    setBottomControl(ll_chat_sendMsg);
                    setTitleBar(mModel.name, true);
                    updatePWContacts();
                    break;
            }
        }
        mModel.remark = UserManager.getNoteByUid(tUid, this);
        if (!TextUtils.isEmpty(mModel.remark)) {
            changeViewForNickNameVisibility(true);
            tv_nickname.setText(mModel.name);
        }

        if (TextUtils.isEmpty(getIntent().getStringExtra(TARGET_NAME))) {
            //TODO:
        }

        if (addViewList != null) {
            for (int i = 0; i < addViewList.size(); i++) {
                userinfo_layout.removeView(addViewList.get(i));
            }
            addViewList.clear();
        }
        setOtherTags(mModel.food_tags, R.drawable.icon_tag_food,
                R.drawable.userinfo_food_tags_bg, Color.rgb(0xFA, 0x9E, 0x00));

        setOtherTags(mModel.music_tags, R.drawable.icon_tag_music,
                R.drawable.userinfo_music_tags_bg, Color.rgb(0xFF, 0x40, 0x86));

        setOtherTags(mModel.movie_tags, R.drawable.icon_tag_movie,
                R.drawable.userinfo_movie_tags_bg, Color.rgb(0x62, 0x2E, 0x96));

        setOtherTags(mModel.book_tags, R.drawable.icon_tag_book,
                R.drawable.userinfo_book_tags_bg, Color.rgb(0x05, 0xA8, 0x59));

        setOtherTags(mModel.travel_tags, R.drawable.icon_tag_travel,
                R.drawable.userinfo_traveling_tags_bg, Color.rgb(0x00, 0x9A, 0xFF));

        setOtherTags(mModel.sport_tags, R.drawable.icon_tag_sports,
                R.drawable.userinfo_sports_tags_bg, Color.rgb(0xFE, 0x3F, 0x1C));

        setOtherTags(mModel.game_tags, R.drawable.icon_tag_game,
                R.drawable.userinfo_game_tags_bg, Color.rgb(0x23, 0x43, 0xE9));

        connect_constellation_layout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                connect_constellation_layout.getViewTreeObserver().removeOnPreDrawListener(this);
                if (isme()) {
                    if (TextUtils.isEmpty(SharedPreferencesUtil.getStringExtra(UserInfoActivity.this, Constans.SP_KEY_WELCOME_PERCENT_PREFIX + PWUtils.getVersionName(UserInfoActivity.this), ""))) {
                        SharedPreferencesUtil.putStringExtra(UserInfoActivity.this, Constans.SP_KEY_WELCOME_PERCENT_PREFIX + PWUtils.getVersionName(UserInfoActivity.this), "welc_per");
                        //delete guide welcome
                        //findViewById(R.id.fl_guide_welcome_percent).setVisibility(View.VISIBLE);
                        View ll_guide_welcome_percent = findViewById(R.id.ll_guide_welcome_percent);
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) ll_guide_welcome_percent.getLayoutParams();
                        int[] location = new int[2];
                        connect_constellation_layout.getLocationInWindow(location);
                        params.setMargins(0, 0, 0, PWUtils.getWindowHeight(UserInfoActivity.this) - location[1] - connect_constellation_layout.getMeasuredHeight() / 2);
                        ll_guide_welcome_percent.setLayoutParams(params);
                    }
                } else {
                    if (mModel.has_lazy_voice && mModel.isCharge && mModel.relation != RELATION_FRIENDS) {
                        if (TextUtils.isEmpty(SharedPreferencesUtil.getStringExtra(UserInfoActivity.this, Constans.SP_KEY_CHARGE_CALL_GUIDE + PWUtils.getVersionName(UserInfoActivity.this), ""))) {
                            SharedPreferencesUtil.putStringExtra(UserInfoActivity.this, Constans.SP_KEY_CHARGE_CALL_GUIDE + PWUtils.getVersionName(UserInfoActivity.this), "charge_call_guide");
                            findViewById(R.id.fl_guide_charge_call).setVisibility(View.VISIBLE);
                        }
                    }
                }
                return true;
            }
        });

    }

    private void updatePWContacts() {
        //是好友更新好友数据库字段，头像等
        BriteDatabase briteDatabase = BriteDBHelperHolder.getInstance().getBriteDatabase(this);
        String sql = String.format("select * from %s where uid = ?", PWDBConfig.TB_PW_CONTACTS);
        Observable<SqlBrite.Query> observable = briteDatabase.createQuery(PWDBConfig.TB_PW_CONTACTS, sql, String.valueOf(tUid));
        Subscription subscription = observable.subscribe(query -> {
            Cursor cursor = query.run();
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    PWContactsModel model = new PWContactsModel(cursor);
                    if (ifNeedUpdateContactDB(model)) {
                        ContentValues values = new ContentValues();
                        values.put("avatar_thumbnail", mModel.avatar_thumbnail);
                        values.put("name", mModel.name);
                        values.put("slogan", mModel.slogan);
                        values.put("birthday", mModel.birthday);
                        values.put("price", mModel.price);
                        briteDatabase.update(PWDBConfig.TB_PW_CONTACTS, values, "uid = ?", model.uid);
                    }
                }
                cursor.close();
            }
        });
        subscription.unsubscribe();
    }

    private boolean ifNeedUpdateContactDB(PWContactsModel model) {
        if (!model.avatar_thumbnail.equals(mModel.avatar_thumbnail)) {
            return true;
        }
        if (!model.name.equals(mModel.name)) {
            return true;
        }
        if (!model.slogan.equals(mModel.slogan)) {
            return true;
        }
        if (!model.birthday.equals(mModel.birthday)) {
            return true;
        }
        if (!model.price.equals(mModel.price)) {
            return true;
        }
        return false;
    }

    private int getXZResId(int xzId) {
        int drawId = R.drawable.ic_xz_shuangzi;
        switch (xzId) {
            case 1:
                drawId = R.drawable.ic_xz_shuangzi;
                break;
            case 2:
                drawId = R.drawable.ic_xz_shuangyu;
                break;
            case 3:
                drawId = R.drawable.ic_xz_baiyang;
                break;
            case 4:
                drawId = R.drawable.ic_xz_jinniu;
                break;
            case 5:
                drawId = R.drawable.ic_xz_shuangzi;
                break;
            case 6:
                drawId = R.drawable.ic_xz_juxie;
                break;
            case 7:
                drawId = R.drawable.ic_xz_shizi;
                break;
            case 8:
                drawId = R.drawable.ic_xz_chunv;
                break;
            case 9:
                drawId = R.drawable.ic_xz_tiancheng;
                break;
            case 10:
                drawId = R.drawable.ic_xz_tianxie;
                break;
            case 11:
                drawId = R.drawable.ic_xz_sheshou;
                break;
            case 12:
                drawId = R.drawable.ic_xz_mojie;
                break;
        }
        return drawId;
    }

    private void createXZViews(ArrayList<Integer> xzList) {
        LinearLayout v_xz_container = (LinearLayout) findViewById(R.id.v_xz_container);
        v_xz_container.removeAllViews();
        if (xzList == null) {
            return;
        }
        LinearLayout.LayoutParams params_margin = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params_margin.setMargins(10, 0, 0, 0);
        for (int i = 0; i < xzList.size(); i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(getXZResId(xzList.get(i)));
            if (i > 0) {
                v_xz_container.addView(imageView, params_margin);
            } else {
                v_xz_container.addView(imageView);
            }
        }
    }

    private void setPrice() {
        try {
            if (!TextUtils.isEmpty(mModel.price) && Float.valueOf(mModel.price) > 0) {
                tv_call_price_layout.setVisibility(View.VISIBLE);
                tv_call_price.setText(mModel.price);
            } else {
                tv_call_price_layout.setVisibility(View.GONE);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void changeViewForNickNameVisibility(boolean isvisible) {
        int visible = isvisible ? View.VISIBLE : View.GONE;
        findViewById(R.id.userinfo_nickname_layout).setVisibility(visible);
        findViewById(R.id.userinfo_nickname_line).setVisibility(visible);
    }


    private void setTags() {
        String emotion = mModel.emotion != 0 ? PWUtils.getEmotion(mModel.emotion) : "";
        String profession = mModel.profession;
        if (!TextUtils.isEmpty(mModel.tags) || !TextUtils.isEmpty(emotion) || !TextUtils.isEmpty(profession)) {
            findViewById(R.id.userinfo_main_tags_title).setVisibility(View.VISIBLE);
            findViewById(R.id.userinfo_main_tags_layout).setVisibility(View.VISIBLE);
            userinfo_main_tag_container.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(this);
            String[] tagArr = mModel.tags.split(",");
            int color = Color.parseColor("#4d4d4d");
            for (String aTagArr : tagArr) {
                if (TextUtils.isEmpty(aTagArr)) {
                    continue;
                }
                TextView tagItem = (TextView) inflater.inflate(R.layout.tag_gray_item, null);
                tagItem.setBackgroundResource(R.drawable.userinfo_main_tags_bg);
                tagItem.setTextColor(color);
                tagItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                tagItem.setText("# " + aTagArr);
                userinfo_main_tag_container.addView(tagItem);
            }
            if (!TextUtils.isEmpty(emotion)) {
                TextView tagItem = (TextView) inflater.inflate(R.layout.tag_gray_item, null);
                tagItem.setBackgroundResource(R.drawable.userinfo_main_tags_bg);
                tagItem.setTextColor(color);
                tagItem.setText("# " + emotion);
                userinfo_main_tag_container.addView(tagItem);
            }
            if (!TextUtils.isEmpty(profession)) {
                TextView tagItem = (TextView) inflater.inflate(R.layout.tag_gray_item, null);
                tagItem.setBackgroundResource(R.drawable.userinfo_main_tags_bg);
                tagItem.setTextColor(color);
                tagItem.setText("# " + profession);
                userinfo_main_tag_container.addView(tagItem);
            }
        }
    }

    private void setOtherTags(String otherTags, int imageId, int bgId, int textColor) {
        String[] tagArr = TextUtils.isEmpty(otherTags) ? null : otherTags.split(",");
        if (tagArr == null)
            return;
        LayoutInflater inflater = LayoutInflater.from(this);
        View tag_layout = inflater.inflate(R.layout.user_info_tags_layout, null);
        FlowLayout tags_container = (FlowLayout) tag_layout.findViewById(R.id.userinfo_tags_container);
        ImageView image = (ImageView) tag_layout.findViewById(R.id.userinfo_tags_image);
        image.setImageResource(imageId);

        for (String aTagArr : tagArr) {
            if (TextUtils.isEmpty(aTagArr)) {
                continue;
            }
            TextView tagItem = (TextView) inflater.inflate(R.layout.tag_gray_item, null);
            tagItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tagItem.setBackgroundResource(bgId);
            tagItem.setTextColor(textColor);
//            tagItem.setText("# " + aTagArr);
            tagItem.setText("  " + aTagArr);
            tags_container.addView(tagItem);
        }
        userinfo_layout.addView(tag_layout, userinfo_layout.getChildCount() - 1);
        userinfo_other_tags_title.setVisibility(View.VISIBLE);
        if (addViewList == null) {
            addViewList = new ArrayList<>();
        }
        addViewList.add(tag_layout);
    }

    private void setBottomControl(final View view) {
        CustomLog.d("set bottom controller.");
        TranslateAnimation animation = new TranslateAnimation(0, 0, PWUtils.getPXbyDP(this, 50), 0);
        animation.setFillAfter(true);
        animation.setDuration(300);
        view.startAnimation(animation);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                CustomLog.d("bottom controller shown, onAnimationStart.");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                CustomLog.d("bottom controller shown, onAnimationEnd.");
                view.clearAnimation();
                findViewById(R.id.userinfo_bottom_line).setVisibility(View.VISIBLE);
            }
        });
    }

    public void click(View v) {
        switch (v.getId()) {
            // 关注聊天
            case R.id.userinfo_one_chat:
            case R.id.userinfo_sendmsg:
                // 好友聊天
                sendMsg(false);
                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECLICKCHAT);
                break;
            // 陌生人聊天
//            case R.id.userinfo_btn_chat:
//                //去掉陌生人打招呼的逻辑
//                sendMsg(false);
//                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECLICKCHAT);
//                break;
            // 打电话
            case R.id.userinfo_call:
                if (checkCallPermission()) {
                    startCall();
                }
                break;
            // 关注
            case R.id.userinfo_btn_follow:
//                focus();
                if (relation == RELATION_FANS) {
                    focus();
                } else {
                    boolean netAvailable = PWUtils.isNetWorkAvailable(this);
                    if (netAvailable) {
                        sendVoiceRequest();
                    } else {
                        showToast(this, getResources().getString(R.string.umeng_common_network_break_alert));
                    }
                }
                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECLICKFOCUS);
                break;
            // 拉黑举报
            case R.id.userinfo_btn_doblack_report:
                doReportOrDoBlack();
                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECLICKBLACKORREPORT);
                break;
            case R.id.userinfo_dynamics_layout:
                Intent feedIntent = new Intent(this, FeedFlowActivity.class);
                if (isme()) {
                    feedIntent.putExtra("creator_uid", mUid);
                } else {
                    feedIntent.putExtra("creator_uid", tUid);
                }
                startActivity(feedIntent);
                break;
            case R.id.userinfo_connect_constellation_layout:
                PeiwoApp app = (PeiwoApp) getApplicationContext();
                StringBuffer url = new StringBuffer(app.isOnLineEnv() ? Constans.RELEASE_LOVE_URL : Constans.DEBUG_LOVE_URL);
                String session = UserManager.getSessionData(this);

                if (!TextUtils.isEmpty(session) && session.length() > 10) {
                    session = session.substring(0, 10);
                }
                if (isme()) {
                    url.append("myConstellation.html?uid=").append(mUid)
                            .append("&tuid=").append(tUid).append("&session=")
                            .append(session);
                } else {
                    PWUserModel mUser = UserManager.getPWUser(this);
                    int meGender = mUser.gender;
                    String meXZ = TimeUtil.getConstellationEnglish(mUser.birthday);
                    String otherXZ = TimeUtil.getConstellationEnglish(mModel.birthday);
                    int t = 1;
                    String b = "";
                    String g = "";
                    if (meGender == mModel.gender) {
                        if (meGender == AsynHttpClient.GENDER_MASK_MALE) {
                            t = 2;
                        } else {
                            t = 3;
                        }
                        b = meXZ;
                        g = otherXZ;
                    } else {
                        if (meGender == AsynHttpClient.GENDER_MASK_MALE) {
                            b = meXZ;
                            g = otherXZ;
                        } else {
                            b = otherXZ;
                            g = meXZ;
                        }
                    }
                    url.append("otherConstellation.html?uid=").append(mUid)
                            .append("&tuid=").append(tUid)
                            .append("&session=").append(session).append("&b=")
                            .append(URLEncoder.encode(b)).append("&g=")
                            .append(URLEncoder.encode(g)).append("&t=").append(t);
                }
                Intent intent = new Intent(this, RedBagActivity.class);
                intent.putExtra("url", url.toString());
                startActivity(intent);
                break;
            case R.id.fl_guide_welcome_percent:
                v.setVisibility(View.GONE);
                break;
            case R.id.fl_guide_charge_call:
                v.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private void sendVoiceRequest() {
        RecorderDialogFragment mRecorderDialogFrag = RecorderDialogFragment.newInstance(mModel.uid, mModel.name, mModel.avatar_thumbnail, getMsgFrom());
        mRecorderDialogFrag.show(getSupportFragmentManager(), mRecorderDialogFrag.toString());
    }

    private int getMsgFrom() {
        return getIntent().getIntExtra(MESSAGE_FROM, 0);
    }

    private void focus() {
        showAnimLoading();
        int msgFrom = getMsgFrom();
        JSONObject jobj = new JSONObject();
        try {
            jobj.put("from", msgFrom);
            jobj.put("paid", mModel.paid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TcpProxy.getInstance().focusUser(tUid, jobj);
    }

    private void cancelFocus() {
        showAnimLoading();
        TcpProxy.getInstance().unFocusUser(tUid);
    }

    private void doReportOrDoBlack() {

        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setItems(new String[]{"拉黑", "举报并拉黑"},
                        (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    doBlock();
                                    break;
                                case 1:
                                    reportUser();
                                    break;

                                default:
                                    break;
                            }
                        }).create().show();

    }

    /**
     * @param isNeedKnowMsgFrom 需要知道用户通过哪个界面进入该个人主页,打招呼盒子才能显示招呼来源
     */
    private void sendMsg(boolean isNeedKnowMsgFrom) {
        if (getIntent().getIntExtra(MsgAcceptActionActivity.A_FLAG, -1) == MsgAcceptActionActivity.V_FLAG_USERINFO) {
            setResult(RESULT_OK);
            finish();
        } else {
            Intent intent = new Intent(this, MsgAcceptedMsgActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Serializable data = mModel;
            intent.putExtra("msg_user", data);
            if (isNeedKnowMsgFrom) {
                intent.putExtra(UserInfoActivity.MESSAGE_FROM, getMsgFrom());
            }
            startActivityForResult(intent, REQUEST_CODE_MSGACCEPT);
        }
    }

    private boolean checkCallPermission() {
        if (mModel == null)
            return false;
        if (PeiwoApp.getApplication().getNetType() == NetUtil.NO_NETWORK) {
            showToast(this, "网络连接失败");
            return false;
        }
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        if (app.getIsCalling()) {
            showToast(this, "您当前正在通话");
            return false;
        }
        return true;
    }

    private void doActionMoreWithOutReMark() {
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEMOREBUTN);
        new AlertDialog.Builder(this)
                .setTitle("更多")
                .setItems(new String[]{"取消关注", "举报/拉黑", "取消"},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        cancelFocus();
                                        UmengStatisticsAgent.onEvent(UserInfoActivity.this, UMEventIDS.UMECANCELFOCUS);
                                        break;
                                    case 1:
                                        //拉黑
                                        doReportOrDoBlack();
                                        break;
                                }
                            }
                        }).create().show();
    }

    private void doActionMore() {
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEMOREBUTN);
        new AlertDialog.Builder(this)
                .setTitle("更多")
                .setItems(new String[]{"备注", "取消关注", "举报/拉黑", "取消"},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        //备注
                                        reMarkUser();
                                        UmengStatisticsAgent.onEvent(UserInfoActivity.this, UMEventIDS.UMEFRIENDSREMARKS);
                                        break;
                                    case 1:
                                        cancelFocus();
                                        UmengStatisticsAgent.onEvent(UserInfoActivity.this, UMEventIDS.UMECANCELFOCUS);
                                        break;
                                    case 2:
                                        //拉黑
                                        doReportOrDoBlack();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).create().show();
    }

    private void reMarkUser() {
        RemarksContactView mark = new RemarksContactView(this);
        mark.show();
        mark.displayReMark(mModel.remark);
        mark.setOnRemarClickListener(new RemarksContactView.OnRemarkClickListener() {
            @Override
            public void onRemarkClick(String mark) {
                sumitMarkToServer(mark);
            }
        });
    }

    private void sumitMarkToServer(final String mark) {
        if (TextUtils.isEmpty(mark)) {
            //删除
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("tuid", String.valueOf(tUid)));
            ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_CONTACT_NOTE_DEL, new MsgStructure() {
                @Override
                public void onReceive(JSONObject data) {
                    //删除数据库存备注
                    msgDBCenterService.delSinglePwRemark(String.valueOf(tUid));
                    alertOnUIThread("删除备注成功");
                    //设置ui
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            PeiwoApp app = (PeiwoApp) getApplicationContext();
                            app.removeNoteByUid(tUid);
                            mModel.remark = "";
                            if (relation == 2) {
                                setTitleBar(mModel.name, true);
                            } else {
                                setTitleBar(mModel.name, false);
                            }
                            changeViewForNickNameVisibility(false);
                        }
                    });
                }

                @Override
                public void onError(int error, Object ret) {
                    alertOnUIThread("删除备注失败");
                }
            });
        } else {
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("tuid", String.valueOf(tUid)));
            params.add(new BasicNameValuePair("note", mark));
            ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_CONTACT_NOTE_ADD, new MsgStructure() {
                @Override
                public void onReceive(JSONObject data) {
                    //数据库存备注
                    msgDBCenterService.insertSinglePwRemark(String.valueOf(tUid), mark);
                    alertOnUIThread("备注成功");
                    //设置ui
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            PeiwoApp app = (PeiwoApp) getApplicationContext();
                            app.putNoteByUid(tUid, mark);
                            mModel.remark = mark;
                            if (relation == 2) {
                                setTitleBar(mark, true);
                            } else {
                                setTitleBar(mark, false);
                            }
                            changeViewForNickNameVisibility(true);
                        }
                    });
                }

                @Override
                public void onError(int error, Object ret) {
                    alertOnUIThread("备注失败");
                }
            });
        }

    }


    private void alertOnUIThread(final String alert) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showToast(UserInfoActivity.this, alert);
            }
        });


    }

    private void doRealBlock() {
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEBLACKLISTSURE);
        showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tuid", String.valueOf(tUid)));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_CONTACT_BLOCK, new MsgStructure() {
            @Override
            public boolean onInterceptRawData(String rawStr) {
                Message msg = mHandler.obtainMessage();
                msg.what = WHAT_DATA_RECEIVE_BLOCK_SECCESS;
                mHandler.sendMessage(msg);
                return true;
            }

            @Override
            public void onReceive(JSONObject data) {

            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(error == AsynHttpClient.PW_RESPONSE_OPERATE_ERROR ? WHAT_DATA_RECEIVE_BLOCK_FAILURE : WHAT_DATA_RECEIVE_ERROR);
            }
        });
    }

    private void doBlock() {
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEBLACKLIST);
        new AlertDialog.Builder(this).setTitle("提示")
                .setMessage("拉黑后将不再接到对方的消息")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doRealBlock();
                    }
                }).setNegativeButton("取消", null).create().show();
    }

    private void reportUser() {
        // DEFAULT = 0 其它
        // PRON = 1 色情
        // CHEAT = 2 欺诈
        // HARASS = 3 骚扰
        // INFRINGE = 4 侵权
        String[] menuArray = new String[]{"色情", "欺诈", "骚扰", "侵权", "其他", "取消"};
        new AlertDialog.Builder(this).setTitle("举报用户")
                .setItems(menuArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 5) {
                            return;
                        }
                        showToast(UserInfoActivity.this, "正在举报");
                        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
                        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_TUID, String.valueOf(tUid)));
                        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_REASON, String.valueOf(which == 4 ? 0 : which + 1)));
                        ApiRequestWrapper.openAPIGET(getApplicationContext(), paramList, AsynHttpClient.API_REPORT_DOBLOCK, new MsgStructure() {
                            @Override
                            public void onReceive(JSONObject data) {
                                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_REPORT_SUCCESS);
                            }

                            @Override
                            public void onError(int error, Object ret) {
                            }
                        });
                    }
                }).create().show();
    }

//    private void setXZTag(TextView textView, int xzId) {
//        int drawId = 0;
//        String content = "";
//        switch (xzId) {
//            case 1:
//                drawId = R.drawable.icon_shuiping;
//                content = TimeUtil.constellationArr[0];
//                break;
//            case 2:
//                drawId = R.drawable.icon_shuangyu;
//                content = TimeUtil.constellationArr[1];
//                break;
//            case 3:
//                drawId = R.drawable.icon_baiyang;
//                content = TimeUtil.constellationArr[2];
//                break;
//            case 4:
//                drawId = R.drawable.icon_jinniu;
//                content = TimeUtil.constellationArr[3];
//                break;
//            case 5:
//                drawId = R.drawable.icon_shuangzi;
//                content = TimeUtil.constellationArr[4];
//                break;
//            case 6:
//                drawId = R.drawable.icon_juxie;
//                content = TimeUtil.constellationArr[5];
//                break;
//            case 7:
//                drawId = R.drawable.icon_shizi;
//                content = TimeUtil.constellationArr[6];
//                break;
//            case 8:
//                drawId = R.drawable.icon_chunv;
//                content = TimeUtil.constellationArr[7];
//                break;
//            case 9:
//                drawId = R.drawable.icon_tiancheng;
//                content = TimeUtil.constellationArr[8];
//                break;
//            case 10:
//                drawId = R.drawable.icon_tianxie;
//                content = TimeUtil.constellationArr[9];
//                break;
//            case 11:
//                drawId = R.drawable.icon_sheshou;
//                content = TimeUtil.constellationArr[10];
//                break;
//            case 12:
//                drawId = R.drawable.icon_mojie;
//                content = TimeUtil.constellationArr[11];
//                break;
//        }
//        if (!TextUtils.isEmpty(content) && content.length() > 2) {
//            content = content.substring(0, 2);
//        }
//        Drawable drawable = getResources().getDrawable(drawId);
//        textView.setText(content);
//        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
//    }

    private void startCall() {
        Intent intent = new Intent(this, RealCallActivity.class);
        intent.putExtra("face_url", mModel.avatar_thumbnail);
        intent.putExtra("gender", mModel.gender); // == 1 ? "男" : "女"
        intent.putExtra("address", String.format(Locale.getDefault(), "%s %s",
                mModel.province, mModel.city));
        intent.putExtra("age", TimeUtil.getAgeByBirthday(mModel.birthday));
        intent.putExtra("tid", tUid);
        intent.putExtra("uname", mModel.name);
        intent.putExtra("slogan", mModel.slogan);
        intent.putExtra("tags", mModel.tags);
        intent.putExtra("flag", DfineAction.OUTGOING_CALL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        prepareCalling(mUid, tUid, mModel.permission, mModel.getPriceFloat(), intent, new OnCallPreparedListener() {
            @Override
            public void onCallPreparedSuccess(int permission, final float price) {
                mHandler.post(() -> {
                            mModel.price = String.valueOf(price);
                            setPrice();
                        }
                );
            }

            @Override
            public void onCallPreparedError(int error, Object ret) {
            }
        });

    }

    @Override
    public void finish() {
        if (mHandler != null) mHandler.removeCallbacksAndMessages(null);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    public void onEventMainThread(FocusEvent event) {
        int type = event.type;
        dismissAnimLoading();
        CustomLog.d("onEventMainThread. relation is : " + relation);
        if (type == FocusEvent.FOCUS_SUCCESS_EVENT) {
            // 关注成功
            mHandler.sendEmptyMessage(WHAT_DATA_FOCUS_SUCCESS);
        } else if (type == FocusEvent.UNFOCUS_SUCCESS_EVENT) {
            // 取消关注成功
            mHandler.sendEmptyMessage(WHAT_DATA_CANCELFOCUS_SUCCESS);
        }
        String err_msg = event.err_msg;
        if (!TextUtils.isEmpty(err_msg)) {
            Message msg = mHandler.obtainMessage();
            msg.obj = err_msg;
            msg.what = WHAT_DATA_FOCUS_ERROR;
            mHandler.sendMessage(msg);
        }
    }

    public void onEventMainThread(Intent intent) {
        if (Constans.ACTION_CLOSE_USERINFO.equals(intent.getAction())) {
            int targetUid = intent.getIntExtra(TARGET_UID, -1);
            if (this.tUid == targetUid) {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_MSGACCEPT:
                    int v_flag = data.getIntExtra(MsgAcceptActionActivity.A_FLAG, -1);
                    handleMsgAcceptResult(v_flag);
                    break;

                default:
                    break;
            }
        }
    }

    private void handleMsgAcceptResult(int v_flag) {
        switch (v_flag) {
            case MsgAcceptActionActivity.V_FLAG_LAHEI:
            case MsgAcceptActionActivity.V_FLAG_DELETE_FRIEND:
                getUserInfo(tUid, true);
                setTitleBar(mModel.name, false);
                break;
        }
    }
}
