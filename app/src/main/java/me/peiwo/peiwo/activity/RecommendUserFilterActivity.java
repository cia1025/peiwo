package me.peiwo.peiwo.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.model.UserFilterSetting;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.*;
import net.simonvt.numberpicker.NumberPicker;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class RecommendUserFilterActivity extends BaseActivity implements NumberPicker.OnValueChangeListener, NumberPicker.OnScrollListener, NumberPicker.Formatter, CompoundButton.OnCheckedChangeListener {

    private NumberPicker mLowerPicker;
    private NumberPicker mUpperPicker;

    private SwitchCompat v_switch_male_option;
    private SwitchCompat v_switch_female_option;
    private SwitchCompat v_switch_shoufei;
    private SwitchCompat v_switch_position_option;

    private TextView locationTv;
    private MyHandler mHandler;
    private Toast mToast = null;
    private static final int TOPIC_GET_LOCATION_SUCCESS = 0x1004;
    private static final int TOPIC_GET_LOCATION_ERROR = 0x1005;
    //	private String[] lowerValues = {"18","19","20","21","22","23","24","25","26","27","28"};
    private String lowerValues = "18,19,20,21,22,23,24,25,26,27,28";
    //	private String[] upperValues = {"20","21","22","23","24","25","26","27","28","29","30+"};
    private String upperValues = "20,21,22,23,24,25,26,27,28,29,30+";
    private static final int WHAT_DATA_LOADSETTING_RECEIVE = 4000;
    private String mProvince;
    private String mCity;
    public static String EXTRA_KEY_SEX_OPTION = "sexOption";

    @Override
    protected void onCreate(Bundle savedInstanceSinttate) {
        super.onCreate(savedInstanceSinttate);
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEFINDPAGESET);
        setContentView(R.layout.recomment_user_filter_layout);
        init();
    }

    private void init() {
        setTitleBar();

        v_switch_male_option = (SwitchCompat) findViewById(R.id.v_switch_male_option);
        v_switch_female_option = (SwitchCompat) findViewById(R.id.v_switch_female_option);
        v_switch_shoufei = (SwitchCompat) findViewById(R.id.v_switch_shoufei);
        v_switch_position_option = (SwitchCompat) findViewById(R.id.v_switch_position_option);
        v_switch_male_option.setOnCheckedChangeListener(this);
        v_switch_female_option.setOnCheckedChangeListener(this);
        v_switch_position_option.setOnCheckedChangeListener(this);

        mLowerPicker = (NumberPicker) findViewById(R.id.lower_age_picker);
        mUpperPicker = (NumberPicker) findViewById(R.id.upper_age_picker);
        locationTv = (TextView) findViewById(R.id.tv_location);
        initNumberPicker(mLowerPicker, true);
        initNumberPicker(mUpperPicker, false);
        mHandler = new MyHandler(this);
        loadFilterSettings();
        loadLocationOption();
    }

    private void loadLocationOption() {
        if (isFinishing()) {
            return;
        }
        PWUserModel userModel = UserManager.getPWUser(this);
        if (userModel == null) {
            return;
        }
        String province = userModel.province.trim();
        if (TextUtils.isEmpty(province))
            v_switch_position_option.setChecked(false);
        else
            v_switch_position_option.setChecked(true);
    }

    private void initNumberPicker(NumberPicker picker, boolean isLower) {
        picker.setOnValueChangedListener(this);
        picker.setOnScrollListener(this);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setFocusable(false);
        String[] arr;
        if (isLower) {
            arr = lowerValues.split(",");
            picker.setValue(0);
        } else {
            arr = upperValues.split(",");
            picker.setValue(10);
        }
        picker.setDisplayedValues(arr);
        picker.setMinValue(0);
        picker.setMaxValue(arr.length - 1);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        int lowerPickerValue = mLowerPicker.getValue();
        int upperPickerValue = mUpperPicker.getValue();
        int lastPos = 10;
        CustomLog.d("lower value is " + lowerPickerValue + "\n uppper value is : " + upperPickerValue);
        String str = "not any picker.";
        if (picker == mUpperPicker)
            str = "upperPicker";
        else if (picker == mLowerPicker)
            str = "lowerPicker";
        CustomLog.d("picker is " + str);
        if (upperPickerValue == 0 && picker == mUpperPicker) {
            mLowerPicker.setValue(0);
        } else if (lowerPickerValue == lastPos && picker == mLowerPicker) {
            mUpperPicker.setValue(lastPos);
        } else if (lowerPickerValue >= upperPickerValue + 1) {
            if (picker == mLowerPicker) {
                mUpperPicker.setValue(++upperPickerValue);
            } else if (picker == mUpperPicker) {
                mLowerPicker.setValue(--lowerPickerValue);
            }
        }
    }

    @Override
    public String format(int value) {
        CustomLog.d("format.");
        return null;
    }

    @Override
    public void onScrollStateChange(NumberPicker view, int scrollState) {
        CustomLog.d("onscrollChange.");
    }


    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "在线设置", v -> {
            finish();
        }, "保存", v -> {
            save();
        });
    }

    private void save() {
        showAnimLoading();
        final int gender = checkGenderOption();
        int age_from = getValueWithPos(mLowerPicker.getValue(), true);
        int age_to = getValueWithPos(mUpperPicker.getValue(), false);
        int locationOption = v_switch_position_option.isChecked() ? 1 : 0;
        String province = "";
        if (locationOption == 1) {
            province = mProvince;
        }

        PWUserModel model = UserManager.getPWUser(this);
        model.province = province;
        UserManager.saveUser(this, model);


        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_GENDER_FILTER, String.valueOf(gender)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_AGE_FROM_FILTER, String.valueOf(age_from)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_AGE_TO_FILTER, String.valueOf(age_to)));
        paramList.add(new BasicNameValuePair("price_on", v_switch_shoufei.isChecked() ? "1" : "0"));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_USER_LOCATION_FILTER, String.valueOf(locationOption)));
        if (!v_switch_position_option.isChecked()) {
            mProvince = "";
            mCity = "";
        }
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_PROVINCE, mProvince));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_CITY, mCity));
        ApiRequestWrapper.openAPIPOST(this, paramList, AsynHttpClient.API_USERS_FILTER_SETTING, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("onReceive1. data is : " + data);
                mHandler.post(() -> {
                    dismissAnimLoading();
//                    SharedPreferencesUtil.putIntExtra(RecommendUserFilterActivity.this, Constans.SP_KEY_CURRGENDER, gender);
//                    Intent it = getIntent();
//                    int sexOption = 0;
//                    if (!v_switch_male_option.isChecked() && !v_switch_female_option.isChecked())
//                        sexOption = 3;
//                    else if (v_switch_male_option.isChecked() && !v_switch_female_option.isChecked())
//                        sexOption = 2;
//                    else if (!v_switch_male_option.isChecked())
//                        sexOption = 1;
//                    it.putExtra(EXTRA_KEY_SEX_OPTION, sexOption);
                    //setResult(RESULT_OK, it);
                    setResult(RESULT_OK, null);
                    finish();
                });
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.post(RecommendUserFilterActivity.this::dismissAnimLoading);
                CustomLog.d("onError1. ret is : " + ret);
            }
        });


    }

    private int getValueWithPos(int pos, boolean isLowerPicker) {
        String arr[];
        if (isLowerPicker) {
            arr = lowerValues.split(",");
            return Integer.valueOf(arr[pos]);
        } else {
            arr = upperValues.split(",");
            String str = arr[pos];
            return str.equals("30+") ? 30 : Integer.valueOf(str);
        }
    }

    private void loadFilterSettings() {
        ApiRequestWrapper.openAPIGET(this, new ArrayList<>(), AsynHttpClient.API_USERS_FILTER_SETTING, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("onReceive. data == " + data);
                Message msg = mHandler.obtainMessage();
                msg.what = WHAT_DATA_LOADSETTING_RECEIVE;
                msg.obj = data;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("onError. ret == " + ret);
            }
        });
    }

    private void filldata(UserFilterSetting setting) {
        v_switch_shoufei.setChecked(setting.price_on == 1);
        //1:男，2:女，3:搜全部
        switch (setting.s_gender) {
            case 1:
                v_switch_male_option.setChecked(true);
                v_switch_female_option.setChecked(false);
                break;
            case 2:
                v_switch_female_option.setChecked(true);
                v_switch_male_option.setChecked(false);
                break;
            case 3:
                v_switch_male_option.setChecked(true);
                v_switch_female_option.setChecked(true);
                break;
            default:
                break;
        }

        boolean ageOption = setting.age_on;
        mLowerPicker.setEnabled(ageOption);
        mUpperPicker.setEnabled(ageOption);

        int lowerPickerPos = getPosWithValue(setting.s_age_from, true);
        int upperPickerPos = getPosWithValue(setting.s_age_to, false);
        //CustomLog.d("age to is : " + setting.getAgeTo());
        mLowerPicker.setValue(lowerPickerPos);
        mUpperPicker.setValue(upperPickerPos);

    }

    private int getPosWithValue(int age, boolean isLower) {

        String arr = lowerValues.toString();
        int index;
        CustomLog.d("arr is : " + arr + "\t , arr index is : " + arr.indexOf(age));
        if (isLower) {
            index = lowerValues.indexOf(String.valueOf(age));
        } else {
            index = upperValues.indexOf(String.valueOf(age));
        }
        int position = index / 3;
        CustomLog.d("The age(" + age + "), position is at : " + position);
        return position;
    }

    private int checkGenderOption() {
        //1:男，2:女，3:搜全部
        if (v_switch_male_option.isChecked() && v_switch_female_option.isChecked()) {
            return 3;
        } else if (v_switch_female_option.isChecked()) {
            return 2;
        } else {
            return 1;
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        switch (id) {
            case R.id.v_switch_male_option:
                if (!isChecked) {
                    v_switch_female_option.setChecked(true);
                }
                break;

            case R.id.v_switch_female_option:
                if (!isChecked) {
                    v_switch_male_option.setChecked(true);
                }
                break;
            case R.id.v_switch_position_option:
                if (isChecked) {
                    getLocation();
                } else {
                    locationTv.setText(getResources().getString(R.string.not_show_location));
                }
                break;
        }
    }

    public void click(View v) {
//        switch (v.getId()) {
//            case R.id.iv_male_option:
////                if (iv_show_boys.isSelected()) {
////                    iv_show_boys.setSelected(false);
////                } else {
////                    iv_show_boys.setSelected(true);
////                    if (iv_show_girls.isSelected()) {
////                        iv_show_girls.setSelected(false);
////                    }
////                }
//                iv_show_boys.setSelected(!iv_show_boys.isSelected());
//                if (iv_show_boys.isSelected()) {
//                    iv_show_girls.setSelected(false);
//                }
//                break;
//            case R.id.iv_female_option:
////                if (iv_show_girls.isSelected()) {
////                    iv_show_girls.setSelected(false);
////                } else {
////                    iv_show_girls.setSelected(true);
////                    if (iv_show_boys.isSelected()) {
////                        iv_show_boys.setSelected(false);
////                    }
////                }
//                iv_show_girls.setSelected(!iv_show_girls.isSelected());
//                if (iv_show_girls.isSelected()) {
//                    iv_show_boys.setSelected(false);
//                }
//                break;
////            case R.id.iv_shoufei:
////                iv_shoufei.setSelected(!iv_shoufei.isSelected());
////                break;
//            case R.id.iv_position_option:
//                CustomLog.d("iv_location is selected : " + iv_location_option.isSelected());
//                if (iv_location_option.isSelected()) {
//                    iv_location_option.setSelected(false);
//                    getLocation();
//                } else {
//                    locationTv.setText(getResources().getString(R.string.not_show_location));
//                    iv_location_option.setSelected(true);
//                }
//                break;
////			case R.id.btn_save:
////				save();
////				finish();
////				break;
//            default:
//                break;
//        }
    }

    private void getLocation() {
//		SharedPreferencesUtil.putBooleanExtra(RecommendUserFilterActivity.this, Constans.SP_KEY_LBS_OPTION, true);
        LocationUtil.getMyLocation(new LocationUtil.GetLocationCallback() {
            @Override
            public void onError() {
                CustomLog.d("getLocation, onError.");
                mHandler.sendEmptyMessage(TOPIC_GET_LOCATION_ERROR);
            }

            @Override
            public void onComplete(String adress, String city) {
                mProvince = adress;
                mCity = city;
                CustomLog.d("getLocation, onComplete.");
                Message msg = mHandler.obtainMessage(TOPIC_GET_LOCATION_SUCCESS);
                msg.obj = adress + city;
                if (!TextUtils.isEmpty(adress) && adress.equals(city)) {
                    msg.obj = adress;
                }
                mHandler.sendMessage(msg);
            }
        });
    }


    private static class MyHandler extends Handler {
        WeakReference<RecommendUserFilterActivity> acivity_ref;

        public MyHandler(RecommendUserFilterActivity activity) {
            acivity_ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            RecommendUserFilterActivity theActivity = acivity_ref.get();
            if (theActivity == null || theActivity.isFinishing())
                return;
            int what = msg.what;
            switch (what) {
                case TOPIC_GET_LOCATION_ERROR: {
                    CustomLog.d("handle message, error.");
                    theActivity.v_switch_position_option.setChecked(true);
                    theActivity.showToast(theActivity, "获取坐标失败");
                    theActivity.locationTv.setText(theActivity.getResources().getString(R.string.not_show_location));
                }
                break;
                case TOPIC_GET_LOCATION_SUCCESS: {
                    CustomLog.d("handle message, success.");
                    //theActivity.v_switch_position_option.setChecked(false);
                    String address = (String) msg.obj;
                    theActivity.locationTv.setText(address);
                }
                break;
                case WHAT_DATA_LOADSETTING_RECEIVE:
                    UserFilterSetting setting = JSON.parseObject(msg.obj.toString(), UserFilterSetting.class);
                    CustomLog.i("load user filter setting : " + setting);
                    theActivity.filldata(setting);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public void showToast(Context context, String msg) {
        if (TextUtils.isEmpty(msg) || context == null || (context instanceof Activity && ((Activity) context).isFinishing())) {
            return;
        }
        if (mToast == null) {
            mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        }
        mToast.setText(msg);
        mToast.show();
    }
}
