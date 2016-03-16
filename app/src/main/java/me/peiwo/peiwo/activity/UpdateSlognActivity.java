package me.peiwo.peiwo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.util.TitleUtil;

/**
 * Created by Dong Fuhai on 2014-08-18 16:20.
 *
 * @modify:
 */
public class UpdateSlognActivity extends BaseActivity implements TextWatcher {

    private EditText et_text;
    private ScrollView scroll_container;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_slogn);
        init();
    }

    private void init() {
        Intent intent = getIntent();
        setTitleBar();
        et_text = (EditText) findViewById(R.id.et_text);
        et_text.setText(intent.getStringExtra("slogn"));
//        et_announcement.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)});
        et_text.setSelection(et_text.getText().length());
        InputFilter filters[] = {new InputFilter.LengthFilter(1000)};
        et_text.setFilters(filters);
        et_text.addTextChangedListener(this);
        RelativeLayout touch_container= (RelativeLayout) findViewById(R.id.touch_container);
        touch_container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(v);
                return false;
            }
        });
    }

    private void hideSoftKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) UpdateSlognActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "编辑签名", v -> {
            finish();
        }, null);
    }

    @Override
    public void finish() {
        String result = et_text.getText().toString();
        if (!TextUtils.isEmpty(result) && result.length() > 1000) {
            showToast(this, getResources().getString(R.string.input_text_length_over_limit));
            return;
        }
        Intent data = new Intent();
        data.putExtra("slogn", result);
        setResult(RESULT_OK, data);
        super.finish();
    }

    @Override
    public void afterTextChanged(Editable arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        if (arg0.length() >= 1000) {
            showToast(this, getResources().getString(R.string.input_text_length_over_limit));
        }
    }

}