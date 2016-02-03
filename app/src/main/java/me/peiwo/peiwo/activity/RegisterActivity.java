package me.peiwo.peiwo.activity;

import java.lang.ref.WeakReference;

import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.PWTimer;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.TitleUtil;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by fuhaidong on 14-9-24.
 */
public class RegisterActivity extends BaseActivity {

	private static final int REQUEST_CODE_FILLPHONENO = 1000;
	private static final int REQUEST_CODE_COUNTRY_CODE = 2000;
	private EditText et_phoneno;
	private String mPhoneCode;

	private MyHandler mHandler;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		init();
	}

	private void init() {
		setTitleBar();
		mHandler = new MyHandler(this);
		et_phoneno = (EditText) findViewById(R.id.et_phoneno);
		et_phoneno.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
		TextView tv_clause = (TextView) findViewById(R.id.tv_clause);
		tv_clause.setText(PWUtils.getClauselinks(this,"点击下一步按钮，即表示同意《陪我用户协议》",13));
		tv_clause.setMovementMethod(LinkMovementMethod.getInstance());
	}

	private void setTitleBar() {
		TitleUtil.setTitleBar(this, "填写手机号", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		}, null);
	}

	public void click(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.btn_submit:
			if (check()) {
				doNextStep();
			}
			break;
		case R.id.ll_countries:
			startActivityForResult(new Intent(this, CountriesPhoneCodeActivity.class), REQUEST_CODE_COUNTRY_CODE);
			break;
		}
	}

	class MyHandler extends Handler {
		WeakReference<RegisterActivity> activity_ref;

		public MyHandler(RegisterActivity activity) {
			activity_ref = new WeakReference<RegisterActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			RegisterActivity theActivity = activity_ref.get();
			if (theActivity == null || theActivity.isFinishing())
				return;
			int what = msg.what;
			switch (what) {
			case WHAT_DATA_RECEIVE:
				dismissAnimLoading();
				final String phoneNumber = PWUtils.getFormatPhoneNo(mPhoneCode, et_phoneno.getText().toString());
				if (phoneVerfi(msg.obj)) {
					PWTimer timer = new PWTimer(60 * 1000, 1000) {
						@Override
						public void onFinish() {
							DfineAction.verificationCodeMap.remove(phoneNumber);
						}
					};
					timer.start();
					DfineAction.verificationCodeMap.put(phoneNumber, timer);
					startFillPhoneActivity(phoneNumber);
				}
				break;
			case WHAT_DATA_RECEIVE_ERROR:
				dismissAnimLoading();
				showToast(theActivity, "请求失败");
				break;
			}
			super.handleMessage(msg);
		}

	}
	
	private void startFillPhoneActivity(String phoneNumber) {
		Intent intent = new Intent(this, FillPhonenoActivity.class);
		intent.putExtra(FillPhonenoActivity.KEY_PHONENO, phoneNumber);
		startActivityForResult(intent, REQUEST_CODE_FILLPHONENO);
	}
	
	private boolean phoneVerfi(Object obj) {
		JSONObject o = (JSONObject) obj;
		int state = o.optInt("state");
		if (state != -1) {
			showToast(this, "该手机号已被注册");
			return false;
		}
		return true;
	}

	private void doNextStep() {
		String phoneNumber = PWUtils.getFormatPhoneNo(mPhoneCode, et_phoneno.getText().toString());
		if (DfineAction.verificationCodeMap.containsKey(phoneNumber)) {
			startFillPhoneActivity(phoneNumber);
		} else {
			showAnimLoading("", false, false, false);
			ApiRequestWrapper.captcha(this, phoneNumber, FillPhonenoActivity.CAPTCHA_TYPE_REGISTER, new MsgStructure() {
						@Override
						public void onReceive(JSONObject data) {
							// Trace.i("phone data == " + data.toString());
							// {"state":-1} 未注册的
							mHandler.sendMessage(mHandler.obtainMessage(WHAT_DATA_RECEIVE, data));
						}

						@Override
						public void onError(int error, Object ret) {
							mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
						}
					});
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_FILLPHONENO:
				setResult(RESULT_OK);
				finish();
				break;
			case REQUEST_CODE_COUNTRY_CODE:
				// 国家电话代码回调
				// data.putExtra(COUNTRY, model.country);
				// data.putExtra(PHONE_CODE, model.p_code);
				((TextView) findViewById(R.id.tv_country)).setText(data
						.getStringExtra(CountriesPhoneCodeActivity.COUNTRY));
				mPhoneCode = data
						.getStringExtra(CountriesPhoneCodeActivity.PHONE_CODE);
				((TextView) findViewById(R.id.tv_pcode)).setText("(+"
						+ mPhoneCode + ")");
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private boolean check() {
		String s = et_phoneno.getText().toString();
		if (TextUtils.isEmpty(s) || s.length() < 4 || s.length() > 20) {
			showToast(this, "请输入正确的手机号");
			return false;
		}
		return true;
	}
}