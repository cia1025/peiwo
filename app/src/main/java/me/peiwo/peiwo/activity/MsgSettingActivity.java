package me.peiwo.peiwo.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.SharedPreferencesUtil;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.util.UserManager;
import net.simonvt.numberpicker.NumberPicker;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class MsgSettingActivity extends BaseActivity implements
        NumberPicker.OnScrollListener, CompoundButton.OnCheckedChangeListener {
    private static final String[] TIME_ARR = new String[]{"00:00", "01:00",
            "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00",
            "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00",
            "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00",
            "23:00"};
    private int mUid;
    private SwitchCompat v_switch_push_master_control;
    private SwitchCompat v_switch_sound_control;
    private SwitchCompat v_switch_vibrate_control;
    private SwitchCompat v_switch_push_nodisturb_control;
    private View ll_control;
    private TextView tv_nodisturb;
    private NumberPicker np_time_s;
    private NumberPicker np_time_e;
    private View ll_numpiker;
    private int time_s;
    private int time_e;
    private MyHandler mHandler;
    private TextView tv_no_disturb_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.msg_setting_activity);
        mUid = UserManager.getUid(this);
        mHandler = new MyHandler(this);
        init();
    }

    private void init() {
        setTitleBar();
        ll_control = findViewById(R.id.ll_control);
        v_switch_push_master_control = (SwitchCompat) findViewById(R.id.v_switch_push_master_control);
        v_switch_sound_control = (SwitchCompat) findViewById(R.id.v_switch_sound_control);
        v_switch_vibrate_control = (SwitchCompat) findViewById(R.id.v_switch_vibrate_control);
        v_switch_push_nodisturb_control = (SwitchCompat) findViewById(R.id.v_switch_push_nodisturb_control);
        v_switch_push_master_control.setOnCheckedChangeListener(this);
        v_switch_push_nodisturb_control.setOnCheckedChangeListener(this);
        tv_no_disturb_msg = (TextView) findViewById(R.id.tv_no_disturb_msg);
        tv_nodisturb = (TextView) findViewById(R.id.tv_nodisturb);
        ll_numpiker = findViewById(R.id.ll_numpiker);
        np_time_s = (NumberPicker) findViewById(R.id.np_time_s);
        np_time_s.setMaxValue(23);
        np_time_s.setMinValue(0);
        np_time_s.setFocusable(true);
        np_time_s.setFocusableInTouchMode(true);
        np_time_s.setDisplayedValues(TIME_ARR);
        np_time_e = (NumberPicker) findViewById(R.id.np_time_e);
        np_time_e.setMaxValue(23);
        np_time_e.setMinValue(0);
        np_time_e.setFocusable(true);
        np_time_e.setFocusableInTouchMode(true);
        np_time_e.setDisplayedValues(TIME_ARR);
        // np_time_s.setOnValueChangedListener(this);
        // np_time_e.setOnValueChangedListener(this);
        np_time_s.setOnScrollListener(this);
        np_time_e.setOnScrollListener(this);
        initLocalSetting();
        getMsgSetting();

    }

    @Override
    public void onScrollStateChange(NumberPicker picker, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            if (picker == np_time_s) {
                time_s = np_time_s.getValue();
            } else {
                time_e = np_time_e.getValue();
            }
            String ts = TIME_ARR[time_s];
            String te = TIME_ARR[time_e];
            if (Integer.valueOf(te.substring(0, te.indexOf(":"))) <= Integer
                    .valueOf(ts.substring(0, ts.indexOf(":")))) {
                tv_nodisturb.setText(String.format(Locale.getDefault(),
                        "勿扰时段(%s 至 次日%s)", ts, te));
            } else {
                tv_nodisturb.setText(String.format(Locale.getDefault(),
                        "勿扰时段(%s 至 %s)", ts, te));
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        switch (id) {
            case R.id.v_switch_push_master_control:
                ll_control.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                break;

            case R.id.v_switch_push_nodisturb_control:
                tv_no_disturb_msg.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                ll_numpiker.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                break;
        }
    }

    static class MyHandler extends Handler {
        WeakReference<MsgSettingActivity> acivity_ref;

        public MyHandler(MsgSettingActivity activity) {
            acivity_ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MsgSettingActivity theActivity = acivity_ref.get();
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.doSettingSetUp(msg.obj);
                    break;

                case WHAT_DATA_RECEIVE_ERROR:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private void doSettingSetUp(Object obj) {
        try {
            if (obj instanceof JSONObject) {
                JSONObject o = (JSONObject) obj;
                // {"time_end":20,"nodisturb":false,"nopush":true,"time_start":19}
                int ts = o.getInt("time_start");
                int te = o.getInt("time_end");
                time_s = ts;
                time_e = te;
                np_time_s.setValue(ts);
                np_time_e.setValue(te);
                if (te <= ts) {
                    tv_nodisturb.setText(String.format(Locale.getDefault(),
                            "勿扰时段(%s:00 至 次日%s:00)", ts, te));
                } else {
                    tv_nodisturb.setText(String.format(Locale.getDefault(),
                            "勿扰时段(%s:00 至 %s:00)", ts, te));
                }
                boolean nopush = o.getBoolean("nopush");
                boolean nodisturb = o.getBoolean("nodisturb");
                if (nopush) {
                    // selected off
                    v_switch_push_master_control.setChecked(false);
                    ll_control.setVisibility(View.GONE);
                    // ll_numpiker.setVisibility(View.GONE);
                    // tv_no_disturb_msg.setVisibility(View.VISIBLE);
                    // rl_push_nodisturb_control.setVisibility(View.GONE);
                } else {
                    v_switch_push_master_control.setChecked(true);
                    ll_control.setVisibility(View.VISIBLE);
                    // rl_push_nodisturb_control.setVisibility(View.VISIBLE);

                    if (nodisturb) {
                        v_switch_push_nodisturb_control.setChecked(true);
                        ll_numpiker.setVisibility(View.VISIBLE);
                        tv_no_disturb_msg.setVisibility(View.GONE);
                    } else {
                        v_switch_push_nodisturb_control.setChecked(false);
                        ll_numpiker.setVisibility(View.INVISIBLE);
                        tv_no_disturb_msg.setVisibility(View.VISIBLE);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "消息设置", v -> {
            finish();
        }, null);
    }

    private void getMsgSetting() {
        ApiRequestWrapper.getNoDisturbSetting(MsgSettingActivity.this, mUid,
                new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        // Trace.i("setting data==" + data.toString());
                        Message message = mHandler.obtainMessage();
                        message.what = WHAT_DATA_RECEIVE;
                        message.obj = data;
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
                    }
                });
    }

    private void initLocalSetting() {
        // Constans.SP_KEY_PUSH_STR
        try {
            String key = SharedPreferencesUtil.getStringExtra(this, Constans.SP_KEY_PUSH_STR, "");
            if (TextUtils.isEmpty(key)) {
                v_switch_sound_control.setChecked(true);
                v_switch_vibrate_control.setChecked(true);
                return;
            }
            //doSettingSetUp(new JSONObject(key));
            settingLocalVar(new JSONObject(key));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void settingLocalVar(JSONObject o) {
        v_switch_sound_control.setChecked(o.optBoolean("sound", true));
        v_switch_vibrate_control.setChecked(o.optBoolean("vibrate", true));
    }

    @Override
    public void finish() {
        // 0为关闭免打扰，1为开启免打扰,1==nopush(不推)0==nopush(推)
        int nodisturb = v_switch_push_nodisturb_control.isChecked() ? 1 : 0;
        int nopush = v_switch_push_master_control.isChecked() ? 0 : 1;
//        int sound = v_switch_sound_control.isChecked() ? 0 : 1; // isSelected=false  是开启sound;
//        int vibrate = v_switch_vibrate_control.isChecked() ? 0 : 1;
        if (!v_switch_push_nodisturb_control.isChecked()) {
            time_s = 0;
            time_e = 0;
        }
        String interval = String.format("%s,%s", time_s, time_e);
        // {"time_end":20,"nodisturb":false,"sound":true,"vibrate":false,"nopush":true,"time_start":19}
        String result = "";
        try {
            JSONObject o = new JSONObject();
            o.put("time_end", time_e);
            o.put("nodisturb", v_switch_push_nodisturb_control.isChecked());
            o.put("nopush", !v_switch_push_master_control.isChecked());
            o.put("sound", v_switch_sound_control.isChecked());
            o.put("vibrate", v_switch_vibrate_control.isChecked());
            o.put("time_start", time_s);
            result = o.toString();
            SharedPreferencesUtil.putStringExtra(this, Constans.SP_KEY_PUSH_STR, result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        app.doSettingNoDistrub(mUid, nopush, nodisturb, interval);
        super.finish();
    }

    public void click(View v) {
        switch (v.getId()) {
            case R.id.rl_push_master_control:
//                if (iv_push_master_control.isSelected()) {
//                    // rl_push_nodisturb_control.setVisibility(View.VISIBLE);
//                    // ll_numpiker.setVisibility(View.VISIBLE);
//                    // rl_sound_control.setVisibility(View.VISIBLE);
//                    // rl_vibrate_control.setVisibility(View.VISIBLE);
//                    ll_control.setVisibility(View.VISIBLE);
//                } else {
//                    // rl_push_nodisturb_control.setVisibility(View.GONE);
//                    // ll_numpiker.setVisibility(View.GONE);
//                    // rl_sound_control.setVisibility(View.GONE);
//                    // rl_vibrate_control.setVisibility(View.GONE);
//                    ll_control.setVisibility(View.GONE);
//                }
//                iv_push_master_control.setSelected(!iv_push_master_control.isSelected());
                break;

            case R.id.rl_push_nodisturb_control:
//                iv_push_nodisturb_control.setSelected(!iv_push_nodisturb_control
//                        .isSelected());
//                if (iv_push_nodisturb_control.isSelected()) {
//                    ll_numpiker.setVisibility(View.GONE);
//                    tv_no_disturb_msg.setVisibility(View.VISIBLE);
//                } else {
//                    ll_numpiker.setVisibility(View.VISIBLE);
//                    tv_no_disturb_msg.setVisibility(View.GONE);
//                }
                break;
            case R.id.rl_vibrate_control:
                //iv_vibrate_control.setSelected(!iv_vibrate_control.isSelected());
                break;
            case R.id.rl_sound_control:
                //iv_sound_control.setSelected(!iv_sound_control.isSelected());
                break;
            default:
                break;
        }
    }

}
