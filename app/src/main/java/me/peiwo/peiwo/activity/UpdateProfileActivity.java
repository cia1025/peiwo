package me.peiwo.peiwo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qiniu.android.http.ResponseInfo;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.callback.UploadCallback;
import me.peiwo.peiwo.constans.PWActionConfig;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.model.ImageModel;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.model.ProfileForUpdateModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.PWUploader;
import me.peiwo.peiwo.util.*;
import me.peiwo.peiwo.util.LocationUtil.GetLocationCallback;
import me.peiwo.peiwo.widget.FlowLayout;
import me.peiwo.peiwo.widget.ProfileFaceGridView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

/**
 * 新的更新用户资料页面
 *
 * @author Fuhai
 */
public class UpdateProfileActivity extends BaseActivity implements
        ProfileFaceGridView.OnImgItemClickListener, LocationListener {

    private static final int REQUESTCODE_MAIN_TAGS = 1000;
    private static final int REQUESTCODE_FOOD_TAGS = 1001;
    private static final int REQUESTCODE_MUSIC_TAGS = 1002;
    private static final int REQUESTCODE_MOVIE_TAGS = 1003;
    private static final int REQUESTCODE_BOOK_TAGS = 1004;
    private static final int REQUESTCODE_TRAVEL_TAGS = 1005;
    private static final int REQUESTCODE_SPORTS_TAGS = 1006;
    private static final int REQUESTCODE_GAME_TAGS = 1007;

    private static final int REQUEST_CODE_BYCAMERA_CROP = 2000;
    private static final int WHAT_UPLOAD_IMG_SUCCESS = 3000;
    private static final int WHAT_UPLOAD_IMG_ERROR = 4000;
    private static final int REQUEST_CODE_MODIFY_UNAME = 5000;
    private static final int REQUEST_CODE_MODIFY_SLOGN = 6000;
    private static final int REQUEST_CODE_MODIFY_PROFESSION = 7000;
    private static final int REQUEST_CODE_START_ALBUM = 8000;
    private static final int REQUEST_CODE_GPS_ACCESS_PERMISSION = 9000;

    private static final int REQUEST_UPDATE_INFO_COMPLEMENT = 9000;
    private static final int TOPIC_GET_LOCATION_SUCCESS = 0x1004;
    private static final int TOPIC_GET_LOCATION_ERROR = 0x1005;
    private static final int CLEAR_LOCATION = 0x1006;
    private ProfileForUpdateModel mProfile;

    private TextView tv_name;
    private TextView tv_birthday;
    private TextView tv_address;
    private TextView tv_slogan;
    private TextView tv_emotion;
    private TextView tv_profession;

//	private TextView tv_want_talk_display;
//	private TextView tv_want_talk_count;

    private ProfileFaceGridView dgv_images;
    private boolean needAlert = false;
    private UploadHandler mHandler;
    private String oldAvatar;// 用作判断是不是修改过头像
    //private View ll_complement;
    //private View layoutComplete;
    //private TextView tv_complement;
    //private TextView tv_complete;
    private int imageNum = 1;

    private FlowLayout userinfo_main_tag_container;
    private FlowLayout edit_food_tag_container;
    private FlowLayout edit_music_tag_container;
    private FlowLayout edit_movie_tag_container;
    private FlowLayout edit_book_tag_container;
    private FlowLayout edit_travel_tag_container;
    private FlowLayout edit_sports_tag_container;
    private FlowLayout edit_game_tag_container;

    private TextView userinfo_null_main_tag_text;
    private TextView edit_food_tag_default_text;
    private TextView edit_music_tag_default_text;
    private TextView edit_movie_tag_default_text;
    private TextView edit_book_tag_default_text;
    private TextView edit_travel_tag_default_text;
    private TextView edit_sports_tag_default_text;
    private TextView edit_game_tag_default_text;
    private ImageView img_location;
    private RelativeLayout coordinate_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        mHandler = new UploadHandler(this);
        init();
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEUPDATEINFO);
    }

    private void init() {
        setTitleBar("更新资料");
        mProfile = new ProfileForUpdateModel(UserManager.getPWUser(this));
        dgv_images = (ProfileFaceGridView) findViewById(R.id.dgv_images);
        tv_name = (TextView) findViewById(R.id.edit_userinfo_name);
        tv_birthday = (TextView) findViewById(R.id.edit_userinfo_birthday);
        tv_address = (TextView) findViewById(R.id.edit_userinfo_address);
        tv_slogan = (TextView) findViewById(R.id.edit_userinfo_slogan);
        tv_emotion = (TextView) findViewById(R.id.edit_userinfo_emotion);
        tv_profession = (TextView) findViewById(R.id.edit_userinfo_profession);

//		tv_want_talk_display = (TextView) findViewById(R.id.tv_want_talk_display);
//		tv_want_talk_count = (TextView) findViewById(R.id.tv_want_talk_count);

//		ll_set_address = findViewById(R.id.ll_set_address);
//		np_city_set = (WheelView) findViewById(R.id.np_city_set);
//		np_province_set = (WheelView) findViewById(R.id.np_province_set);
//        layoutComplete = findViewById(R.id.complete_layout);
//        tv_complete = (TextView) findViewById(R.id.tv_complete);
//        ll_complement = findViewById(R.id.ll_complement);
//        tv_complement = (TextView) findViewById(R.id.tv_complement);
//        setComplementVisible();
//		np_province_set.setVisibleItems(5);
//		np_city_set.setVisibleItems(5);
//
//		np_province_set.setViewAdapter(new ArrayWheelAdapter<String>(this,
//				CityData.PROVINCE));
//		np_province_set.addScrollingListener(this);
        requestGetComplement();
        coordinate_layout = (RelativeLayout) findViewById(R.id.rl_address);
        userinfo_main_tag_container = (FlowLayout) findViewById(R.id.userinfo_main_tag_container);
        edit_food_tag_container = (FlowLayout) findViewById(R.id.edit_food_tag_container);
        edit_music_tag_container = (FlowLayout) findViewById(R.id.edit_music_tag_container);
        edit_movie_tag_container = (FlowLayout) findViewById(R.id.edit_movie_tag_container);
        edit_book_tag_container = (FlowLayout) findViewById(R.id.edit_book_tag_container);
        edit_travel_tag_container = (FlowLayout) findViewById(R.id.edit_travel_tag_container);
        edit_sports_tag_container = (FlowLayout) findViewById(R.id.edit_sports_tag_container);
        edit_game_tag_container = (FlowLayout) findViewById(R.id.edit_game_tag_container);

        userinfo_null_main_tag_text = (TextView) findViewById(R.id.userinfo_null_main_tag_text);
        edit_food_tag_default_text = (TextView) findViewById(R.id.edit_food_tag_default_text);
        edit_music_tag_default_text = (TextView) findViewById(R.id.edit_music_tag_default_text);
        edit_movie_tag_default_text = (TextView) findViewById(R.id.edit_movie_tag_default_text);
        edit_book_tag_default_text = (TextView) findViewById(R.id.edit_book_tag_default_text);
        edit_travel_tag_default_text = (TextView) findViewById(R.id.edit_travel_tag_default_text);
        edit_sports_tag_default_text = (TextView) findViewById(R.id.edit_sports_tag_default_text);
        edit_game_tag_default_text = (TextView) findViewById(R.id.edit_game_tag_default_text);
        img_location = (ImageView) findViewById(R.id.img_location);
        fillData();
    }

    private void fillData() {
        PWUserModel model = UserManager.getPWUser(this);
        boolean isLocationOff = TextUtils.isEmpty(model.province.trim());
        CustomLog.d("model.province is : " + model.province + ".");
        CustomLog.d("isLocation on ? " + !isLocationOff);
        if (!isLocationOff) {
            getLocation();
        } else {
            removeLocation();
        }

        tv_name.setText(mProfile.name);
        tv_birthday.setText(TimeUtil.getBirthdayDisplay(mProfile.birthday));
        if (!TextUtils.isEmpty(mProfile.province) || TextUtils.isEmpty(mProfile.city)) {
//			tv_address.setText(String.format(Locale.getDefault(), "%s %s", mProfile.province, mProfile.city));
        }
        if (!TextUtils.isEmpty(mProfile.slogan)) {
            tv_slogan.setText(mProfile.slogan);
        }

        String tv_emotion_text = PWUtils.getEmotion(mProfile.emotion);
        tv_emotion.setText(tv_emotion_text);
        if (!TextUtils.isEmpty(mProfile.profession)) {
            tv_profession.setText(mProfile.profession);
        }

        addEmotionAndProfessionTag();
        setOtherTags(userinfo_main_tag_container, userinfo_null_main_tag_text, mProfile.tags,
                R.drawable.userinfo_main_tags_bg, Color.parseColor("#4d4d4d"));


        setOtherTags(edit_food_tag_container, edit_food_tag_default_text, mProfile.food_tags,
                R.drawable.userinfo_food_tags_bg, Color.rgb(0xFA, 0x9E, 0x00));

        setOtherTags(edit_music_tag_container, edit_music_tag_default_text, mProfile.music_tags,
                R.drawable.userinfo_music_tags_bg, Color.rgb(0xFF, 0x40, 0x86));

        setOtherTags(edit_movie_tag_container, edit_movie_tag_default_text, mProfile.movie_tags,
                R.drawable.userinfo_movie_tags_bg, Color.rgb(0x62, 0x2E, 0x96));

        setOtherTags(edit_book_tag_container, edit_book_tag_default_text, mProfile.book_tags,
                R.drawable.userinfo_book_tags_bg, Color.rgb(0x05, 0xA8, 0x59));

        setOtherTags(edit_travel_tag_container, edit_travel_tag_default_text, mProfile.travel_tags,
                R.drawable.userinfo_traveling_tags_bg, Color.rgb(0x00, 0x9A, 0xFF));

        setOtherTags(edit_sports_tag_container, edit_sports_tag_default_text, mProfile.sport_tags,
                R.drawable.userinfo_sports_tags_bg, Color.rgb(0xFE, 0x3F, 0x1C));

        setOtherTags(edit_game_tag_container, edit_game_tag_default_text, mProfile.game_tags,
                R.drawable.userinfo_game_tags_bg, Color.rgb(0x23, 0x43, 0xE9));
        // 填充头像图片
        fillImages();

    }

    private void removeLocation() {
        coordinate_layout.setClickable(true);
        img_location.setVisibility(View.GONE);
        mProfile.province = " ";
        mProfile.city = " ";
    }

    private void addEmotionAndProfessionTag() {
        String emotion = mProfile.emotion != 0 ? PWUtils.getEmotion(mProfile.emotion) : "";
        String profession = mProfile.profession;
        if (!TextUtils.isEmpty(emotion)) {
            mProfile.tags += "," + emotion;
            mProfile.emotion = 0;
            UserManager.clearEmotion(this);
        }
        if (!TextUtils.isEmpty(profession)) {
            mProfile.tags += "," + profession;
            mProfile.profession = "";
            UserManager.clearProfession(this);
        }
    }


    private void getLocation() {
        LocationUtil.getMyLocation(new GetLocationCallback() {
            @Override
            public void onError() {
                mHandler.sendEmptyMessage(TOPIC_GET_LOCATION_ERROR);
            }

            @Override
            public void onComplete(String adress, String city) {
                Message msg = mHandler.obtainMessage(TOPIC_GET_LOCATION_SUCCESS);
                msg.obj = adress + "," + city;

                if (!TextUtils.isEmpty(adress) && adress.equals(city)) {
                    msg.obj = adress;
                }
                mHandler.sendMessage(msg);
            }
        });
    }


    private void setOtherTags(FlowLayout tags_container, TextView defaultText, String otherTags, int bgId, int textColor) {
        String[] tagArr = TextUtils.isEmpty(otherTags) ? null : otherTags.split(",");
        if (tagArr == null)
            return;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String aTagArr : tagArr) {
            if (TextUtils.isEmpty(aTagArr)) {
                continue;
            }
            TextView tagItem = (TextView) inflater.inflate(R.layout.tag_gray_item, null);
            tagItem.setBackgroundResource(bgId);
            tagItem.setTextColor(textColor);
//            tagItem.setText("# " + aTagArr);
            if (tags_container.getId() == R.id.userinfo_main_tag_container) {
                tagItem.setText("# " + aTagArr);
            } else {
                tagItem.setText("  " + aTagArr);
            }
            tags_container.addView(tagItem);
        }
        defaultText.setVisibility(View.GONE);
    }


    private void fillImages() {
        if (mProfile.images.size() > 0) {
            oldAvatar = mProfile.images.get(0).thumbnail_url;
        }
        dgv_images.displayImages(mProfile.images);
        dgv_images.setOnImgItemClickListener(this);
        imageNum = mProfile.images.size();
    }

    //delete the right btn
    private void setTitleBar(String title) {
//        TitleUtil.setTitleBar(this, title, v -> {
//            finish();
//        }, "保存", v -> {
//            if (shouldShowDilag()) {
//                showUpdateProfileDialog();
//            } else {
//                doUpdateProfile();
//            }
//        });
        TitleUtil.setTitleBar(this, title, v -> {
            finish();
        }, v -> {
            //do nothing
        });
    }


    private void requestGetComplement() {
        ApiRequestWrapper.getProfileComplement(this, mProfile, new MsgStructure() {
            /* (non-Javadoc)
             * @see me.peiwo.peiwo.net.MsgStructure#onReceive(org.json.JSONObject)
             */
            @Override
            public void onReceive(JSONObject data) {
                int complement = data.optInt("complement");
                mHandler.sendMessage(mHandler.obtainMessage(REQUEST_UPDATE_INFO_COMPLEMENT, complement));
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }

    private void setComplementVisible(int complement) {
        if (complement < 30) {
            setTitleBar(String.format("更新资料(完善度%d%s)", complement, "%"));
        } else {
            setTitleBar(String.format("更新资料", complement));
        }
    }


    static class UploadHandler extends Handler {
        WeakReference<UpdateProfileActivity> activity_ref;

        public UploadHandler(UpdateProfileActivity activity) {
            activity_ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            UpdateProfileActivity theActivity = activity_ref.get();
            if (theActivity == null) {
                return;
            }
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.dismissAnimLoading();
                    theActivity.showToast(theActivity, "上传信息成功");
                    theActivity.needAlert = false;
                    EventBus.getDefault().post(new Intent(PWActionConfig.ACTION_USER_AVATAR_CHANGED));
                    theActivity.finish();
                    break;

                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.dismissAnimLoading();
                    if (!PWUtils.isNetWorkAvailable(theActivity)) {
                        theActivity.showToast(theActivity, "网络连接失败");
                    } else {
                        theActivity.showToast(theActivity, "上传信息失败");
                    }
                    break;
                case WHAT_UPLOAD_IMG_SUCCESS:
                    theActivity.dismissAnimLoading();
                    theActivity.showToast(theActivity, "上传照片成功");
                    theActivity.needAlert = true;
                    ImageModel model = (ImageModel) msg.obj;
                    if (theActivity.mReplaceIndex != -1) {
                        if (model != null) {
                            theActivity.mProfile.images.set(
                                    theActivity.mReplaceIndex, model);
                        }
                        theActivity.mReplaceIndex = -1;
                    } else {
                        if (model != null) {
                            theActivity.mProfile.images.add(model);
                        }
                    }
                    theActivity.dgv_images.reload(theActivity.mProfile.images);
                    if (theActivity.imageNum < theActivity.mProfile.images.size()) {
                        theActivity.requestGetComplement();
                        theActivity.imageNum = theActivity.mProfile.images.size();
                    }
                    break;
                case WHAT_UPLOAD_IMG_ERROR:
                    theActivity.dismissAnimLoading();
                    theActivity.needAlert = true;
                    theActivity.showToast(theActivity, "上传照片失败");
                    break;
                case REQUEST_UPDATE_INFO_COMPLEMENT:
                    theActivity.setComplementVisible((Integer) msg.obj);
                    break;
                case TOPIC_GET_LOCATION_ERROR:
                    theActivity.tv_address.setText("显示坐标");
                    theActivity.coordinate_layout.setClickable(true);
                    break;
                case TOPIC_GET_LOCATION_SUCCESS:
                    String address = (String) msg.obj;
                    String province = "";
                    String city = "";
                    System.out.println("handleMessage(), address : " + address);
                    if (address.contains(",")) {
                        String addArr[] = address.split(",");
                        province = addArr[0];
                        city = addArr[1];
                        System.out.println("handleMessage(), province : " + province + "\t city : " + city);
                    } else {
                        province = address;
                        city = address;
                    }

                    boolean needRequest = false;
                    if (!province.equals(theActivity.mProfile.province) || !city.equals(theActivity.mProfile.city)) {
                        theActivity.needAlert = true;
                        if (TextUtils.isEmpty(province) || TextUtils.isEmpty(theActivity.mProfile.province)
                                || TextUtils.isEmpty(city) || TextUtils.isEmpty(theActivity.mProfile.city)) {
                            needRequest = true;
                        }
                    }
                    theActivity.mProfile.province = province;
                    theActivity.mProfile.city = city;
                    if (needRequest) {
                        theActivity.requestGetComplement();
                    }

                    CustomLog.d("handleMessage(). my.province : " + theActivity.mProfile.province + "\t my.city : " + theActivity.mProfile.city);

                    theActivity.tv_address.setText(address);
                    theActivity.img_location.setVisibility(View.VISIBLE);
                    theActivity.coordinate_layout.setClickable(false);
                    break;
                case CLEAR_LOCATION:
                    if (!" ".equals(theActivity.mProfile.province)
                            || !" ".equals(theActivity.mProfile.city)) {
                        theActivity.needAlert = true;
                    }
                    theActivity.mProfile.province = " ";
                    theActivity.mProfile.city = " ";
                    break;
            }
            super.handleMessage(msg);
        }
    }


    private boolean shouldShowDilag() {
        if (mProfile.gender == AsynHttpClient.GENDER_MASK_MALE) { // 是个男的不需要弹
            return false;
        }

        if ((UserManager.getPWUserFlags(this) & 3) % 2 == 0) { // 如果没认证，或者认证失败就不需要弹
            return false;
        }
        if (mProfile.images.size() > 0 && mProfile.images.get(0).thumbnail_url.equals(oldAvatar)) { // 如果头像没改动就不需要弹
            return false;
        }
        return true;
    }

    private void showUpdateProfileDialog() {
        new AlertDialog.Builder(this).setMessage("修改或删除认证头像后，需重新认证，确定修改？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> {
                    doUpdateProfile();
                }).create().show();

    }

    private void doUpdateProfile() {
        boolean showLocation = false;
        if (img_location.getVisibility() == View.VISIBLE) {
            showLocation = true;
        }
        ApiRequestWrapper.updateProfile(this, mProfile, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("onReceive. data is : " + data);
                UserManager.saveUser(UpdateProfileActivity.this, new PWUserModel(data));
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE);
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
            }
        });
    }

    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.rl_name:
                updateName();
                break;
            case R.id.rl_birthday:
                updateBirthday();
                break;
            case R.id.rl_address:
                if (getResources().getString(R.string.show_location).equals(tv_address.getHint())) {
                    tv_address.setHint("正在获取坐标信息");
                    coordinate_layout.setClickable(false);
                    getLocation();
                }
                break;
            case R.id.img_location:
                img_location.setVisibility(View.GONE);
                tv_address.setText("");
                tv_address.setHint(getResources().getString(R.string.show_location));
                coordinate_layout.setClickable(true);
                mHandler.sendEmptyMessage(CLEAR_LOCATION);
                break;
            case R.id.rl_slogan:
                updateSlogn();
                break;
            case R.id.rl_emotion:
                updateEmotion();
                break;
            case R.id.rl_profession:
                updateProfession();
                break;
            case R.id.rl_want_talk: //专属标签
                editMainTags();
                break;
            case R.id.edit_food_tag_layout:
                editFoodTags();
                break;
            case R.id.edit_music_tag_layout:
                editMusicTags();
                break;
            case R.id.edit_movie_tag_layout:
                editMovieTags();
                break;
            case R.id.edit_book_tag_layout:
                editBookTags();
                break;
            case R.id.edit_travel_tag_layout:
                editTravelTags();
                break;
            case R.id.edit_sports_tag_layout:
                editSportsTags();
                break;
            case R.id.edit_game_tag_layout:
                editGameTags();
                break;

        }
    }

    private void editFoodTags() {
        Intent intent = new Intent(this, AddTagsActivity.class);
        intent.putExtra(AddTagsActivity.KEY_TAGS, mProfile.food_tags);
        intent.putExtra(AddTagsActivity.KEY_TYPE, 1);
        startActivityForResult(intent, REQUESTCODE_FOOD_TAGS);
    }

    private void editMusicTags() {
        Intent intent = new Intent(this, AddTagsActivity.class);
        intent.putExtra(AddTagsActivity.KEY_TAGS, mProfile.music_tags);
        intent.putExtra(AddTagsActivity.KEY_TYPE, 2);
        startActivityForResult(intent, REQUESTCODE_MUSIC_TAGS);
    }

    private void editMovieTags() {
        Intent intent = new Intent(this, AddTagsActivity.class);
        intent.putExtra(AddTagsActivity.KEY_TAGS, mProfile.movie_tags);
        intent.putExtra(AddTagsActivity.KEY_TYPE, 3);
        startActivityForResult(intent, REQUESTCODE_MOVIE_TAGS);
    }

    private void editBookTags() {
        Intent intent = new Intent(this, AddTagsActivity.class);
        intent.putExtra(AddTagsActivity.KEY_TAGS, mProfile.book_tags);
        intent.putExtra(AddTagsActivity.KEY_TYPE, 4);
        startActivityForResult(intent, REQUESTCODE_BOOK_TAGS);
    }

    private void editTravelTags() {
        Intent intent = new Intent(this, AddTagsActivity.class);
        intent.putExtra(AddTagsActivity.KEY_TAGS, mProfile.travel_tags);
        intent.putExtra(AddTagsActivity.KEY_TYPE, 5);
        startActivityForResult(intent, REQUESTCODE_TRAVEL_TAGS);
    }

    private void editSportsTags() {
        Intent intent = new Intent(this, AddTagsActivity.class);
        intent.putExtra(AddTagsActivity.KEY_TAGS, mProfile.sport_tags);
        intent.putExtra(AddTagsActivity.KEY_TYPE, 6);
        startActivityForResult(intent, REQUESTCODE_SPORTS_TAGS);
    }

    private void editGameTags() {
        Intent intent = new Intent(this, AddTagsActivity.class);
        intent.putExtra(AddTagsActivity.KEY_TAGS, mProfile.game_tags);
        intent.putExtra(AddTagsActivity.KEY_TYPE, 7);
        startActivityForResult(intent, REQUESTCODE_GAME_TAGS);
    }

    private void editMainTags() {
        Intent intent = new Intent(this, AddTagsActivity.class);
        intent.putExtra(AddTagsActivity.KEY_TAGS, mProfile.tags);
        intent.putExtra(AddTagsActivity.KEY_TYPE, 0);
        startActivityForResult(intent, REQUESTCODE_MAIN_TAGS);
    }

    private void resetTags(String newTags, String oldTags, FlowLayout flowLayout,
                           TextView default_text, int bgId, int textColor) {
        if (!newTags.equals(oldTags)) {
            needAlert = true;
        } else {
            return;
        }
        flowLayout.removeAllViews();
        if (TextUtils.isEmpty(newTags)) {
            default_text.setVisibility(View.VISIBLE);
            return;
        }
        setOtherTags(flowLayout, default_text, newTags, bgId, textColor);
    }

    private void updateProfession() {
        Intent intent = new Intent(this, UpdateProfessionActivity.class);
        intent.putExtra("profession", mProfile.profession);
        startActivityForResult(intent, REQUEST_CODE_MODIFY_PROFESSION);
    }

    private void updateEmotion() {
        final String[] items = new String[]{"保密", "单身", "恋爱中", "已婚", "同性"};
        new AlertDialog.Builder(this).setTitle("情感状态")
                .setItems(items, (dialog, which) -> {
                    boolean needRequest = false;
                    if (!(which == mProfile.emotion)) {
                        needAlert = true;
                        if (mProfile.emotion == 0 || which == 0) {
                            needRequest = true;
                        }
                    }
                    mProfile.emotion = which;
                    tv_emotion.setText(items[which]);
                    if (needRequest) {
                        requestGetComplement();
                    }
                }).create().show();
    }

    private void updateSlogn() {
        Intent intent = new Intent(this, UpdateSlognActivity.class);
        intent.putExtra("slogn", mProfile.slogan);
        startActivityForResult(intent, REQUEST_CODE_MODIFY_SLOGN);
    }


    private void updateBirthday() {
        String birthday = mProfile.birthday;
        int year;
        int month;
        int day;
        if (TextUtils.isEmpty(birthday)) {
            year = 1990;
            month = 0;
            day = 1;
        } else {
            String birth = birthday.substring(0, birthday.indexOf(" "));
            String[] births = birth.split("-");
            year = Integer.valueOf(births[0]);
            month = Integer.valueOf(births[1]);
            day = Integer.valueOf(births[2]);
        }
        DatePickerDialog pickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // monthOfYear+1
                    String resultMonth = monthOfYear + 1 > 9 ? String
                            .valueOf(monthOfYear + 1) : String.format(
                            Locale.getDefault(), "0%d", monthOfYear + 1);
                    String resultDay = dayOfMonth > 9 ? String
                            .valueOf(dayOfMonth) : String.format(
                            Locale.getDefault(), "0%d", dayOfMonth);
                    if (!TimeUtil.checkBirthday(year1, monthOfYear,
                            dayOfMonth)) {
                        showToast(UpdateProfileActivity.this,
                                "输入的生日不能超过今天哟！");
                        return;
                    }
                    String birthday1 = String.valueOf(year1) + "-" + resultMonth + "-" + resultDay + " " + "00:00:00";
                    if (!birthday1.equals(mProfile.birthday)) {
                        needAlert = true;
                    }
                    mProfile.birthday = birthday1;
                    tv_birthday.setText(TimeUtil
                            .getBirthdayDisplay(mProfile.birthday));
                }, year, month - 1, day);
        pickerDialog.show();
    }

    private void updateName() {
        Intent intent = new Intent(this, UpdateUnameActivity.class);
        intent.putExtra("uname", mProfile.name);
        startActivityForResult(intent, REQUEST_CODE_MODIFY_UNAME);
    }

    private void alertFinish() {
//        new AlertDialog.Builder(this).setTitle("是否放弃修改")
//                .setNegativeButton("放弃", (dialog, which) -> {
//                    UpdateProfileActivity.super.finish();
//                }).setPositiveButton("继续编辑", null).create().show();
        new AlertDialog.Builder(this).setTitle("是否保存本次编辑")
                .setNegativeButton("放弃", (dialog, which) -> {
                    UpdateProfileActivity.super.finish();
                }).setPositiveButton("保存", (dialog, which) -> {
            if (shouldShowDilag()) {
                showUpdateProfileDialog();
            } else {
                doUpdateProfile();
            }
        }).create().show();
    }

    @Override
    public void finish() {
        setResult(Activity.RESULT_OK);
        if (needAlert) {
            alertFinish();
        } else {
            mHandler.removeCallbacksAndMessages(null);
            super.finish();
        }
    }

    @Override
    public void onImgItemClick(int index) {
        if (index == -1) {
            // 添加图片的加号
            addPhoto();
        } else {
            // 其余图片
            imageCRUD(index);
        }
    }

    private int mReplaceIndex = -1;

    private void imageCRUD(final int index) {
        String[] items;
        if (mProfile.images.size() > 1) {
            items = new String[]{"删除图片", "设置为头像", "相册", "拍照", "取消"};
            new AlertDialog.Builder(this).setTitle("替换或删除")
                    .setItems(items, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                mProfile.images.remove(index);
                                dgv_images.reload(mProfile.images);
                                requestGetComplement();
                                imageNum = mProfile.images.size();
                                break;
                            case 1:
                                replacePhotoBylocal(index);
                                break;
                            case 2:
                                replacePhotoByGallery(index);
                                break;
                            case 3:
                                replacePhotoByCamera(index);
                                break;
                            default:
                                break;
                        }
                    }).create().show();
        } else {
            items = new String[]{"相册", "拍照", "取消"};
            new AlertDialog.Builder(this).setTitle("替换")
                    .setItems(items, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                replacePhotoByGallery(index);
                                break;

                            case 1:
                                replacePhotoByCamera(index);
                                break;

                            default:
                                break;
                        }
                    }).create().show();
        }
//
//        String[] items;
//        if (mProfile.images.size() > 1) {
//            items = new String[]{"查看", "删除图片", "取消"};
//            new AlertDialog.Builder(this).setTitle("长按并拖动可改变图片位置")
//                    .setItems(items, (dialog, which) -> {
//                        switch (which) {
//                            case 0:
//                                checkImg(index);
//                                break;
//                            case 1:
//                                mProfile.images.remove(index);
//                                dgv_images.reload(mProfile.images);
//                                requestGetComplement();
//                                imageNum = mProfile.images.size();
//                                needAlert=true;
//                                break;
//                            default:
//                                break;
//                        }
//                    }).create().show();
//        } else {
//            items = new String[]{"查看", "取消"};
//            new AlertDialog.Builder(this).setTitle("长按并拖动可改变图片位置")
//                    .setItems(items, (dialog, which) -> {
//                        switch (which) {
//                            case 0:
//                                checkImg(0);
//                                break;
//                            default:
//                                break;
//                        }
//
//                    }).create().show();
//        }

    }

    private void checkImg(int index) {
        Intent intent = new Intent(UpdateProfileActivity.this, ImagePagerActivity.class);
        intent.putParcelableArrayListExtra(ImagePagerActivity.KEY_URL_LIST, mProfile.images);
        intent.putExtra(ImagePagerActivity.KEY_POS, index);
        UpdateProfileActivity.this.startActivity(intent);
    }

    private void replacePhotoByCamera(int index) {
        mReplaceIndex = index;
        mImageKey = PWUploader.getInstance().getKey(mProfile.uid);
        ImageUtil.startImgPickerCamera(this, ImageUtil.PICK_FROM_CAMERA,
                ImageUtil.getPathForCameraCrop(mImageKey));
    }

    private void replacePhotoByGallery(int index) {
        mReplaceIndex = index;
        mImageKey = PWUploader.getInstance().getKey(mProfile.uid);
        // Util.startImgPickerGallery(
        // this,
        // Util.PICK_FROM_GALLERY,
        // getPathForUpload(mImageKey));
        choiceImageByAlbum(mImageKey);
    }

    private void replacePhotoBylocal(int index) {
        ImageModel model = mProfile.images.get(index);
        ImageModel modelReplace = mProfile.images.get(0);
        mProfile.images.set(0, model);
        mProfile.images.set(index, modelReplace);
        dgv_images.reload(mProfile.images);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CustomLog.d("onActivityResult. req code is : " + requestCode + ", result code is : " + resultCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUESTCODE_MAIN_TAGS: {
                    boolean needRequest = false;
                    String tags = data.getStringExtra(AddTagsActivity.KEY_TAGS);
                    if (tags == null) {
                        tags = "";
                    }
                    resetTags(tags, mProfile.tags, userinfo_main_tag_container,
                            userinfo_null_main_tag_text,
                            R.drawable.userinfo_main_tags_bg, Color.parseColor("#4d4d4d"));
                    if (!tags.equals(mProfile.tags)) {
                        needRequest = true;
                    }
                    mProfile.tags = tags;
                    if (needRequest) {
                        requestGetComplement();
                    }
                }
                break;
                case REQUESTCODE_FOOD_TAGS: {
                    boolean needRequest = false;
                    String tags = data.getStringExtra(AddTagsActivity.KEY_TAGS);
                    CustomLog.d("REQUESTCODE_MUSIC_TAGS food tag is : " + tags);
                    if (tags == null) {
                        tags = "";
                    }
                    resetTags(tags, mProfile.food_tags, edit_food_tag_container,
                            edit_food_tag_default_text,
                            R.drawable.userinfo_food_tags_bg,
                            Color.rgb(0xFA, 0x9E, 0x00));
                    if (!tags.equals(mProfile.tags)) {
                        needRequest = true;
                    }
                    mProfile.food_tags = tags;
                    if (needRequest) {
                        requestGetComplement();
                    }
                }
                break;
                case REQUESTCODE_MUSIC_TAGS: {
                    boolean needRequest = false;
                    String tags = data.getStringExtra(AddTagsActivity.KEY_TAGS);
                    CustomLog.d("REQUESTCODE_MUSIC_TAGS music tag is : " + tags);
                    if (tags == null) {
                        tags = "";
                    }
                    resetTags(tags, mProfile.music_tags, edit_music_tag_container,
                            edit_music_tag_default_text,
                            R.drawable.userinfo_music_tags_bg,
                            Color.rgb(0xFF, 0x40, 0x86));
                    if (!tags.equals(mProfile.tags)) {
                        needRequest = true;
                    }
                    mProfile.music_tags = tags;
                    if (needRequest) {
                        requestGetComplement();
                    }
                }
                break;
                case REQUESTCODE_MOVIE_TAGS: {
                    boolean needRequest = false;
                    String tags = data.getStringExtra(AddTagsActivity.KEY_TAGS);
                    if (tags == null) {
                        tags = "";
                    }
                    resetTags(tags, mProfile.movie_tags, edit_movie_tag_container,
                            edit_movie_tag_default_text,
                            R.drawable.userinfo_movie_tags_bg, Color.rgb(0x62, 0x2E, 0x96));
                    if (!tags.equals(mProfile.tags)) {
                        needRequest = true;
                    }
                    mProfile.movie_tags = tags;
                    if (needRequest) {
                        requestGetComplement();
                    }
                }
                break;
                case REQUESTCODE_BOOK_TAGS: {
                    boolean needRequest = false;
                    String tags = data.getStringExtra(AddTagsActivity.KEY_TAGS);
                    if (tags == null) {
                        tags = "";
                    }
                    resetTags(tags, mProfile.book_tags, edit_book_tag_container,
                            edit_book_tag_default_text,
                            R.drawable.userinfo_book_tags_bg,
                            Color.rgb(0x05, 0xA8, 0x59));
                    if (!tags.equals(mProfile.tags)) {
                        needRequest = true;
                    }
                    mProfile.book_tags = tags;
                    if (needRequest) {
                        requestGetComplement();
                    }
                }
                break;
                case REQUESTCODE_TRAVEL_TAGS: {
                    boolean needRequest = false;
                    String tags = data.getStringExtra(AddTagsActivity.KEY_TAGS);
                    if (tags == null) {
                        tags = "";
                    }
                    resetTags(tags, mProfile.travel_tags, edit_travel_tag_container,
                            edit_travel_tag_default_text,
                            R.drawable.userinfo_traveling_tags_bg,
                            Color.rgb(0x00, 0x9A, 0xFF));
                    if (!tags.equals(mProfile.tags)) {
                        needRequest = true;
                    }
                    mProfile.travel_tags = tags;
                    if (needRequest) {
                        requestGetComplement();
                    }
                }
                break;
                case REQUESTCODE_SPORTS_TAGS: {
                    boolean needRequest = false;
                    String tags = data.getStringExtra(AddTagsActivity.KEY_TAGS);
                    if (tags == null) {
                        tags = "";
                    }
                    resetTags(tags, mProfile.sport_tags, edit_sports_tag_container,
                            edit_sports_tag_default_text,
                            R.drawable.userinfo_sports_tags_bg,
                            Color.rgb(0xFE, 0x3F, 0x1C));
                    if (!tags.equals(mProfile.tags)) {
                        needRequest = true;
                    }
                    mProfile.sport_tags = tags;
                    if (needRequest) {
                        requestGetComplement();
                    }
                }
                break;
                case REQUESTCODE_GAME_TAGS: {
                    boolean needRequest = false;
                    String tags = data.getStringExtra(AddTagsActivity.KEY_TAGS);
                    if (tags == null) {
                        tags = "";
                    }
                    resetTags(tags, mProfile.game_tags, edit_game_tag_container,
                            edit_game_tag_default_text,
                            R.drawable.userinfo_game_tags_bg,
                            Color.rgb(0x23, 0x43, 0xE9));
                    if (!tags.equals(mProfile.tags)) {
                        needRequest = true;
                    }
                    mProfile.game_tags = tags;
                    if (needRequest) {
                        requestGetComplement();
                    }
                }
                break;
                case ImageUtil.PICK_FROM_CAMERA:
                    File src = ImageUtil.getPathForCameraCrop(mImageKey);
                    startCropWithOrientation(src.getAbsolutePath());
                    // startCrop(ImageUtil.getPathForCameraCrop(mImageKey));
                    break;
                case ImageUtil.PICK_FROM_GALLERY:
                    File thumb = ImageUtil.getPathForUpload(mImageKey);
                    if (thumb != null && thumb.exists()) {
                        ImageModel imgModel = new ImageModel(
                                thumb.getAbsolutePath(), mImageKey);
                        // mProfile.imagesForDisplay.add(imgModel);
                        // dgv_images.reload(mProfile.imagesForDisplay);
                        uploadImgBySCS(imgModel);
                    } else {
                        showToast(this, "获取图片出错");
                    }
                    break;
                case REQUEST_CODE_BYCAMERA_CROP:
                    uploadImgByCameraCrop(data);
                    break;
                case REQUEST_CODE_MODIFY_UNAME:
                    String uname = data.getStringExtra("uname");
                    if (!uname.equals(mProfile.name)) {
                        needAlert = true;
                    }
                    mProfile.name = uname;
                    tv_name.setText(uname);
                    break;
                case REQUEST_CODE_MODIFY_SLOGN: {
                    boolean needRequest = false;
                    String slogn = data.getStringExtra("slogn");
                    if (!slogn.equals(mProfile.slogan)) {
                        needAlert = true;
                        if (TextUtils.isEmpty(slogn) || TextUtils.isEmpty(mProfile.slogan)) {
                            needRequest = true;
                        }
                    }
                    mProfile.slogan = slogn;
                    tv_slogan.setText(slogn);
                    if (needRequest) {
                        requestGetComplement();
                    }
                }
                break;
                case REQUEST_CODE_MODIFY_PROFESSION: {
                    boolean needRequest = false;
                    String profession = data.getStringExtra("profession");
                    if (!profession.equals(mProfile.profession)) {
                        needAlert = true;
                        if (TextUtils.isEmpty(profession) || TextUtils.isEmpty(mProfile.profession)) {
                            needRequest = true;
                        }
                    }
                    mProfile.profession = profession;
                    tv_profession.setText(profession);
                    if (needRequest) {
                        requestGetComplement();
                    }
                }
                break;
                case REQUEST_CODE_START_ALBUM:
                    // 通过自定义的相册程序选择的照片
                    //uploadImgByCameraCrop(data);
                    ArrayList<String> arrayList = data.getStringArrayListExtra(AlbumCompatActivity.K_ALBUM_RST);
                    String path = arrayList.get(0);
                    startCropWithOrientation(path);
                    break;

                case REQUEST_CODE_GPS_ACCESS_PERMISSION:
                    //System.out.println("UpdateProfileActivity.onActivityResult() !!");
                    break;
            }


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("UpdateProfileActivity.onResume() !!");
    }

    private void startCropWithOrientation(String path) {
        File src = new File(path);
        ImageUtil.startCrop(this, src,
                ImageUtil.getPathForUpload(mImageKey));
    }


    private void uploadImgByCameraCrop(Intent data) {
        String path = data.getStringExtra(ImageUtil.IMAGE_PATH);
        if (path == null) {
            showToast(this, "获取图片出错");
            return;
        } else {
            ImageModel imgModel = new ImageModel(path, mImageKey);
            // mProfile.imagesForDisplay.add(imgModel);
            // dgv_images.reload(mProfile.imagesForDisplay);
            uploadImgBySCS(imgModel);
            // Trace.i("image path==" + path);
        }
    }

//    public void startCrop(File src) {
//        Intent intent = new Intent(this, CropImage.class);
//        intent.putExtra(CropImage.IMAGE_PATH, src.getAbsolutePath());
//        intent.putExtra(CropImage.SCALE, true);
//        intent.putExtra(CropImage.ASPECT_X, 1);
//        intent.putExtra(CropImage.ASPECT_Y, 1);
//        // intent.putExtra(CropImage.OUTPUT_X, 320);
//        // intent.putExtra(CropImage.OUTPUT_Y, 320);
//        startActivityForResult(intent, REQUEST_CODE_BYCAMERA_CROP);
//    }

    private String mImageKey;

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
                        imgModel.name = key;
                        Message message = mHandler.obtainMessage();
                        message.what = WHAT_UPLOAD_IMG_SUCCESS;
                        message.obj = imgModel;
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void onFailure(String key, ResponseInfo responseInfo, JSONObject jsonObject) {
                        mHandler.sendEmptyMessage(WHAT_UPLOAD_IMG_ERROR);
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(WHAT_UPLOAD_IMG_ERROR);
            }
        });
//        PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                if (!SCSUpload.uploadFile(imgModel.uploadpath, imgModel.name, SCSUpload.FileType.IMAGE)) {
//                    mHandler.sendEmptyMessage(WHAT_UPLOAD_IMG_ERROR);
//                } else {
//                    Message message = mHandler.obtainMessage();
//                    message.what = WHAT_UPLOAD_IMG_SUCCESS;
//                    message.obj = imgModel;
//                    mHandler.sendMessage(message);
//                }
//            }
//        });
    }

    private void addPhoto() {
//        new AlertDialog.Builder(this)
//                .setTitle("添加照片")
//                .setItems(new String[]{"拍照", "相册", "取消"},
//                        (dialog, which) -> {
//                            switch (which) {
//                                case 0:
//                                    mImageKey = PWUploader.getInstance().getKey(mProfile.uid);
//                                    ImageUtil
//                                            .startImgPickerCamera(
//                                                    UpdateProfileActivity.this,
//                                                    ImageUtil.PICK_FROM_CAMERA,
//                                                    ImageUtil.getPathForCameraCrop(mImageKey));
//                                    break;
//                                case 1:
//                                    mImageKey = PWUploader.getInstance().getKey(mProfile.uid);
//                                    // Util.startImgPickerGallery(
//                                    // UpdateProfileActivity.this,
//                                    // Util.PICK_FROM_GALLERY,
//                                    // getPathForUpload(mImageKey));
//                                    choiceImageByAlbum(mImageKey);
//                                    break;
//                                default:
//                                    break;
//                            }
//                        }).create().show();
        mImageKey = PWUploader.getInstance().getKey(mProfile.uid);
        choiceImageByAlbum(mImageKey);
    }

    private void choiceImageByAlbum(String mImageKey) {
        Intent intent = new Intent(UpdateProfileActivity.this, AlbumCompatActivity.class);
        intent.putExtra(AlbumCompatActivity.CHOOSE_MODE, AlbumCompatActivity.CHOOSE_MODE_SECTION); // ImageUtil.getPathForUpload(mImageKey).getAbsolutePath()
        intent.putExtra(AlbumCompatActivity.K_ALBUM_RST_COUNT, 1);
        startActivityForResult(intent, REQUEST_CODE_START_ALBUM);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mImageKey", mImageKey);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageKey = savedInstanceState.getString("mImageKey",
                PWUploader.getInstance().getKey(mProfile.uid));
    }

    @Override
    public void onLocationChanged(Location arg0) {
        //System.out.println("UpdateProfileActivity.onLocationChanged(), latitude : " + arg0.getLatitude() + "\t longittude : " + arg0.getLongitude());
    }

    @Override
    public void onProviderDisabled(String arg0) {


    }

    @Override
    public void onProviderEnabled(String arg0) {


    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {


    }

}
