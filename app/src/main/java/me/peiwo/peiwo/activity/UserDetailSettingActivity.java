package me.peiwo.peiwo.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.qiniu.android.http.ResponseInfo;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.callback.UploadCallback;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.model.ImageModel;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.model.ProfileForUpdateModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.PWUploader;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.HourGlassAgent;
import me.peiwo.peiwo.util.ImageUtil;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.SharedPreferencesUtil;
import me.peiwo.peiwo.util.UmengStatisticsAgent;
import me.peiwo.peiwo.util.UserManager;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class UserDetailSettingActivity extends BaseActivity {
    private static final int REQUEST_CODE_BYCAMERA_CROP = 2000;
    private static final int WHAT_UPLOAD_IMG_SUCCESS = 3000;
    private static final int WHAT_UPLOAD_IMG_ERROR = 4000;
    private static final int REQUEST_CODE_START_ALBUM = 8000;
    private static final int REQUEST_CODE_HASINIT = 5000;

    public static final String ACTION = "action";
    public static final int ACTION_PHONE_REGIST = 1;

    // private LocalUser.PeiwoUser mPeiwoUser;
    private ImageView iv_setface;
    @Bind(R.id.et_uname)
    EditText et_uname;
    @Bind(R.id.rb_male_setting)
    AppCompatRadioButton rb_male_setting;
    @Bind(R.id.rb_fmale_setting)
    AppCompatRadioButton rb_fmale_setting;
    @Bind(R.id.iv_add_cross)
    ImageView iv_add_cross;
    // private TextView tv_gender_switch;
//    private TextView tv_birthday_switch;
    private ProfileForUpdateModel mProfile;
    private UploadHandler mHandler;
    //private TextView tv_man;
    //private TextView tv_female;
    private boolean bindPhone;
    private Button btn_save;
    private ArrayList<String> mServerPkgList = new ArrayList<>();
    private StringBuilder mAdvancePkgStr = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_detail_setting_activity);
        mProfile = new ProfileForUpdateModel();
        mProfile.uid = UserManager.getUid(this);
        mProfile.images.addAll(UserManager.getPWUser(this).images);
        mHandler = new UploadHandler(this);
        init();
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECOMPLETEPROFILE);

        SharedPreferencesUtil.putBooleanExtra(this, "old_user_" + mProfile.uid, false);
    }

    private void init() {
        setTitle(getString(R.string.fill_profile));
        iv_setface = (ImageView) findViewById(R.id.iv_setface);
        PWUserModel local = UserManager.getPWUser(this);
        String thumb_avatar = local.avatar_thumbnail;
        if (!TextUtils.isEmpty(thumb_avatar)) {
            ImageLoader.getInstance().displayImage(thumb_avatar, iv_setface, ImageUtil.getRoundedOptions());
            mProfile.avatar_thumbnail = thumb_avatar;
        }
        et_uname.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        et_uname.setText(local.name);
        et_uname.clearFocus();
        mProfile.name = local.name;

        et_uname.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (mProfile.name.equals(et_uname.getText().toString())) {
                    et_uname.setText("");
                }
            }
        });

        // tv_gender_switch = (TextView) findViewById(R.id.tv_gender_switch);
//        tv_birthday_switch = (TextView) findViewById(R.id.tv_birthday_switch);
        if (local.gender == 1) {
            rb_male_setting.setChecked(true);
            mProfile.gender = AsynHttpClient.GENDER_MASK_MALE;
        } else if (local.gender == 2) {
            rb_fmale_setting.setSelected(true);
            mProfile.gender = AsynHttpClient.GENDER_MASK_FEMALE;
        } else {
            mProfile.gender = rb_fmale_setting.isChecked() ? 2 : 1;
        }
        btn_save = (Button) findViewById(R.id.btn_save);
        if (getIntent().getBooleanExtra(FillPhonenoActivity.KEY_REGISTER, false)) {
            btn_save.setText("完成");
        } else {
            btn_save.setClickable(false);

        }
        // fillWithDefaultData();
        bindPhone = getIntent().getBooleanExtra("nophone", false);
//        if (bindPhone) {
//            tv_clause.setVisibility(View.VISIBLE);
//            tv_clause.setText(PWUtils.getClauselinks(this, "点击完成按钮，即表示同意《陪我用户协议》", 13));
//            tv_clause.setMovementMethod(LinkMovementMethod.getInstance());
//        }

        et_uname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkSubmitButton();
            }
        });
        rb_fmale_setting.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkSubmitButton();
        });
        rb_male_setting.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkSubmitButton();
        });
        getPackageFromServer();
        checkSubmitButton();
    }

    private void getPackageFromServer() {
        showAnimLoading();
        ApiRequestWrapper.openAPIGET(this, new ArrayList<NameValuePair>(), AsynHttpClient.API_ACCOUNT_GET_PACKAGE, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
            }

            @Override
            public boolean onInterceptRawData(String rawStr) {
                CustomLog.d("getPackageFromServer. rawStr is : " + rawStr);
                Observable.just(rawStr).observeOn(AndroidSchedulers.mainThread()).subscribe(UserDetailSettingActivity.this::findAdvancePkg);
                JSONObject object = null;
                try {
                    object = new JSONObject(rawStr);
                    JSONArray array = object.optJSONArray("data");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return super.onInterceptRawData(rawStr);
            }

            @Override
            public void onError(int error, Object ret) {
            }
        });


    }

    private void findAdvancePkg(String str) {
        try {
            dismissAnimLoading();
            JSONObject object = new JSONObject(str);
            JSONArray array = object.optJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                if (array.get(i) instanceof String) {
                    mServerPkgList.add((String) array.get(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<String> advancePkgList = new ArrayList<>();
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        for (PackageInfo packInfo : packages) {
            if (mServerPkgList.contains(packInfo.packageName)) {
                advancePkgList.add(packInfo.packageName);
                CustomLog.d("findAdvancePkg. advance packInfo is : " + packInfo.packageName);
            }
        }

        Observable.from(advancePkgList).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                if (mAdvancePkgStr.length() > 0) {
                    mAdvancePkgStr.deleteCharAt(mAdvancePkgStr.length() - 1);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                mAdvancePkgStr.append(s).append(",");
            }
        });
    }

    // private void fillWithDefaultData() {
    // if (!TextUtils.isEmpty(mSocialUser.name)) {
    // et_uname.setText(mSocialUser.name);
    // et_uname.setSelection(TextUtils.isEmpty(et_uname.getText().toString())
    // ? 0 : et_uname.getText().length());
    // mProfile.name = et_uname.getText().toString();
    // }
    // if (mSocialUser.gender > 0) {
    // initGender(mSocialUser.gender);
    // }
    // }

    // private void initGender(int gender) {
    // if (gender == 1) {
    // tv_gender_switch.setText("男");
    // mProfile.gender = AppConfig.GENDER_MALE;
    // } else {
    // tv_gender_switch.setText("女");
    // mProfile.gender = AppConfig.GENDER_FEMALE;
    // }
    // }

    Rect nameRect = new Rect();

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                et_uname.getGlobalVisibleRect(nameRect);
                if (!nameRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    et_uname.clearFocus();
                    PWUtils.hideSoftKeyBoard(this);
                    if (TextUtils.isEmpty(et_uname.getText())) {
                        et_uname.setText(mProfile.name);
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void click(View v) {
        int id = v.getId();
        switch (id) {
            // case R.id.ll_gender_switch:
            // switchGender();
            // break;
//            case R.id.ll_birthday_switch:
//                switchBirthday();
//                break;
            case R.id.fl_uface:
                switchuserFace();
                break;
            case R.id.btn_save:
                if (dataAvailable()) {
                    HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
                    if (hourGlassAgent.getStatistics() && hourGlassAgent.getK50() == 0) {
                        hourGlassAgent.setK50(1);
                        PeiwoApp app = (PeiwoApp) getApplicationContext();
                        app.postK("k50");
                    }
                    doUploadUserInfo();
                    if (bindPhone) {
                        Intent intent = new Intent(UserDetailSettingActivity.this, BindPhoneActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_HASINIT);
                        break;
                    }
//                    doUploadUserInfo();
                }
                //

                break;
//            case R.id.tv_man:
//                tv_man.setSelected(true);
//                tv_female.setSelected(false);
//                mProfile.gender = AsynHttpClient.GENDER_MASK_MALE;
//                break;
//            case R.id.tv_female:
//                tv_man.setSelected(false);
//                tv_female.setSelected(true);
//                mProfile.gender = AsynHttpClient.GENDER_MASK_FEMALE;
//                break;
        }
    }

    private boolean dataAvailable() {
        mProfile.name = et_uname.getText().toString();
        if (TextUtils.isEmpty(mProfile.avatar_thumbnail)) {
            if (mProfile.images == null || mProfile.images.size() == 0) {
                showToast(this, "请选择头像");
                return false;
            }
        }

        if (TextUtils.isEmpty(mProfile.name.trim())) {
            showToast(this, "昵称不能为空");
            return false;
        }
        if (mProfile.name != null && TextUtils.isEmpty(mProfile.name.trim())) {
            showToast(this, "昵称不能全是空格");
            return false;
        }
        if (mProfile.name.length() > 16) { // PPUtils.calculateCharLength(mProfile.name)
            showToast(this, "昵称最多16个字符");
            return false;
        }
        if (mProfile.gender == 0) {
            showToast(this, "请选择性别");
            return false;
        }
//        if (TextUtils.isEmpty(mProfile.birthday)) {
//            showToast(this, "请选择生日");
//            return false;
//        }
        return true;
    }

    private void doUploadUserInfo() {
        showAnimLoading("", false, false, false);
        Intent it = getIntent();
        int social_type = it.getIntExtra("social_type", 0);
        String social_uid = it.getStringExtra("social_uid");
        String token = it.getStringExtra("token");
        mProfile.gender = rb_male_setting.isChecked() ? AsynHttpClient.GENDER_MASK_MALE : AsynHttpClient.GENDER_MASK_FEMALE;
        ArrayList<NameValuePair> param = new ArrayList<>();
        param.add(new BasicNameValuePair("uid", String.valueOf(mProfile.uid)));
        param.add(new BasicNameValuePair("name", mProfile.name));
        param.add(new BasicNameValuePair("images", mProfile.images.get(0).name));
//        param.add(new BasicNameValuePair("images", UserManager.getPWUser(this).images.get(0).name));
        param.add(new BasicNameValuePair("gender", String.valueOf(mProfile.gender)));
        param.add(new BasicNameValuePair("device_type", Build.MODEL + "," + Build.VERSION.SDK_INT + "," + Build.MANUFACTURER + "," + Build.VERSION.RELEASE + "," + Build.ID));
        param.add(new BasicNameValuePair("social_type", String.valueOf(social_type)));
        param.add(new BasicNameValuePair("access_token", token));
        param.add(new BasicNameValuePair("social_uid", social_uid));
        param.add(new BasicNameValuePair("packages", mAdvancePkgStr.toString()));
        ApiRequestWrapper.openAPIPOST(this, param, AsynHttpClient.API_ACCOUNT_SET_USERINFO, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("doUploadUserInfo. data is : " + data);
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    UserManager.saveUser(UserDetailSettingActivity.this, new PWUserModel(data));
                    showToast(UserDetailSettingActivity.this, getString(R.string.register_done));
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("doUploadUserInfo. error is : " + ret);
                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    dismissAnimLoading();
                    String tips = "failed " + error;
                    if (ret instanceof JSONObject) {
                        tips = ((JSONObject) ret).optString("msg");
                    }
                    showToast(UserDetailSettingActivity.this, tips);
                });
            }
        });

    }

    private String mImageKey;

    private void switchuserFace() {
        mImageKey = PWUploader.getInstance().getKey(mProfile.uid);
        choiceImageByAlbum(mImageKey);
    }

    private void choiceImageByAlbum(String mImageKey) {
        Intent intent = new Intent(UserDetailSettingActivity.this,
                AlbumCompatActivity.class);
        intent.putExtra(AlbumCompatActivity.CHOOSE_MODE, AlbumCompatActivity.CHOOSE_MODE_SECTION); // ImageUtil.getPathForUpload(mImageKey).getAbsolutePath()
        intent.putExtra(AlbumCompatActivity.K_ALBUM_RST_COUNT, 1); // ImageUtil.getPathForUpload(mImageKey).getAbsolutePath()
        startActivityForResult(intent, REQUEST_CODE_START_ALBUM);
    }

    /*private void switchBirthday() {
        int year, month, day;
        // final Calendar c = Calendar.getInstance();
        year = 1990;
        month = 0;
        day = 1;
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
                        showToast(UserDetailSettingActivity.this,
                                "输入的生日不能超过今天哟！");
                        return;
                    }
                    mProfile.birthday = new StringBuilder().append(year1)
                            .append("-").append(resultMonth).append("-")
                            .append(resultDay).append(" ")
                            .append("00:00:00").toString();
                    tv_birthday_switch.setText(TimeUtil
                            .getBirthdayDisplay(mProfile.birthday));
                    tv_birthday_switch.setTextColor(getResources()
                            .getColor(R.color.black));
                    checkSubmitButton();

                }, year, month, day);
        pickerDialog.show();
    }*/

    private void checkSubmitButton() {
        if (/*!TextUtils.isEmpty(tv_birthday_switch.getText())
                && */!TextUtils.isEmpty(et_uname.getText())
                && (mProfile != null && mProfile.images.size() > 0 || !TextUtils.isEmpty(mProfile.avatar_thumbnail))
                && (rb_male_setting.isChecked() || rb_fmale_setting.isChecked())) {
            btn_save.setClickable(true);
            btn_save.setBackgroundColor(getResources().getColor(R.color.valid_clickable_color));
        } else {
            btn_save.setClickable(false);
            btn_save.setBackgroundColor(getResources().getColor(R.color.invalid_clickable_color));
        }
    }

    // private void switchGender() {
    // new AlertDialog.Builder(this)
    // .setTitle("选择性别")
    // .setItems(new String[]{"男", "女"}, new DialogInterface.OnClickListener() {
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // switch (which) {
    // case 0:
    // mProfile.gender = AppConfig.GENDER_MALE;
    // tv_gender_switch.setText("男");
    // break;
    // case 1:
    // mProfile.gender = AppConfig.GENDER_FEMALE;
    // tv_gender_switch.setText("女");
    // break;
    // }
    // PPAlert.showToast(UserDetailSettingActivity.this, "注册后性别不可改", 2000);
    // }
    // })
    // .create().show();
    // }

//    public void startCrop(File src) {
//        Intent intent = new Intent(this, CropImage.class);
//        intent.putExtra(CropImage.IMAGE_PATH, src.getAbsolutePath());
//        intent.putExtra(CropImage.SCALE, true);
//        intent.putExtra(CropImage.ASPECT_X, 1);
//        intent.putExtra(CropImage.ASPECT_Y, 1);
//        startActivityForResult(intent, REQUEST_CODE_BYCAMERA_CROP);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ImageUtil.PICK_FROM_CAMERA:
                    File src = ImageUtil.getPathForCameraCrop(mImageKey);
                    startCropWithOrientation(src.getAbsolutePath());
                    // startCrop(getPathForCameraCrop(mImageKey));
                    break;
                case ImageUtil.PICK_FROM_GALLERY:
                    File thumb = ImageUtil.getPathForUpload(mImageKey);
                    if (thumb.exists()) {
                        ImageModel imgModel = new ImageModel(
                                thumb.getAbsolutePath(), mImageKey);
                        uploadImgBySCS(imgModel);
                    } else {
                        showToast(this, "获取图片出错");
                    }
                    iv_add_cross.setVisibility(View.GONE);
                    break;
                case REQUEST_CODE_BYCAMERA_CROP:
                    uploadImgByCameraCrop(data);
                    break;
                case REQUEST_CODE_START_ALBUM:
                    // 通过自定义的相册程序选择的照片
                    uploadImgByCameraCrop(data);
                    ArrayList<String> items = data.getStringArrayListExtra(AlbumCompatActivity.K_ALBUM_RST);
                    startCropWithOrientation(items.get(0));
                    iv_add_cross.setVisibility(View.GONE);
                    break;
                case REQUEST_CODE_HASINIT:
                    doUploadUserInfo();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void startCropWithOrientation(String path) {
//        File src = ImageUtil.getPathForCameraCrop(mImageKey);
//        if (ImageUtil.getExifOrientation(src.getAbsolutePath()) == ExifInterface.ORIENTATION_ROTATE_90) {
//            //矫正
//            redressImage(src);
//        } else {
//            ImageUtil.startCrop(this, src, ImageUtil.getPathForUpload(mImageKey));
//        }
        File src = new File(path);
        ImageUtil.startCrop(this, src, ImageUtil.getPathForUpload(mImageKey));
    }


    private void uploadImgByCameraCrop(Intent data) {
        ArrayList<String> rst = data.getStringArrayListExtra(AlbumCompatActivity.K_ALBUM_RST);
        String path = rst.get(0);
        if (path == null) {
            showToast(this, "获取图片出错");
        } else {
            ImageModel imgModel = new ImageModel(path, mImageKey);
            uploadImgBySCS(imgModel);
        }
    }

    static class UploadHandler extends Handler {
        WeakReference<UserDetailSettingActivity> activity_ref;

        public UploadHandler(UserDetailSettingActivity activity) {
            activity_ref = new WeakReference<>(
                    activity);
        }

        @Override
        public void handleMessage(Message msg) {
            UserDetailSettingActivity theActivity = activity_ref.get();
            if (theActivity == null) {
                return;
            }
            int what = msg.what;
            switch (what) {
//                case WHAT_DATA_RECEIVE:
//                    UserManager.updateState(theActivity, UserManager.STATE_INITED);
//                    theActivity.dismissAnimLoading();
//                    theActivity.showToast(theActivity, "注册成功");
//                    theActivity.setResult(RESULT_OK);
//                    theActivity.finish();
//                    break;
//                case WHAT_DATA_RECEIVE_ERROR:
//                    theActivity.dismissAnimLoading();
//                    theActivity.showToast(theActivity, "上传信息失败");
//                    break;
                case WHAT_UPLOAD_IMG_SUCCESS:
                    theActivity.dismissAnimLoading();
                    theActivity.showToast(theActivity, "上传照片成功");
                    ImageModel model = (ImageModel) msg.obj;
                    if (theActivity.mProfile.images.size() == 0) {
                        theActivity.mProfile.images.add(0, model);
                    } else {
                        theActivity.mProfile.images.set(0, model);
                    }
                    ImageLoader.getInstance().displayImage(
                            theActivity.mProfile.images.get(0).thumbnail_url,
                            theActivity.iv_setface, ImageUtil.getRoundedOptions());
                    theActivity.checkSubmitButton();
                    break;
                case WHAT_UPLOAD_IMG_ERROR:
                    theActivity.dismissAnimLoading();
                    theActivity.showToast(theActivity, "上传照片失败");
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
            if (hourGlassAgent.getStatistics() && hourGlassAgent.getK11() == 0) {
                hourGlassAgent.setK11(1);
                PeiwoApp app = (PeiwoApp) getApplicationContext();
                app.postK("k11");
            }
            if (!bindPhone)
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void uploadImgBySCS(final ImageModel imgModel) {
        showAnimLoading("", false, false, false);
        // final long t1 = System.currentTimeMillis();
        PWUploader uploader = PWUploader.getInstance();
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PWUploader.K_UPLOAD_TYPE, PWUploader.UPLOAD_TYPE_AVATAR));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_QINIU_TOKEN, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
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
    public void left_click(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK51() == 0) {
            hourGlassAgent.setK51(1);
            app.postK("k51");
        }
    }
}