package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.util.TitleUtil;

/**
 * Created by Dong Fuhai on 2014-08-18 14:49.
 *
 * @modify:
 */
public class UpdateUnameActivity extends BaseActivity {

    private EditText et_text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_uname);
        init();
    }

    private void init() {
        Intent intent = getIntent();
        setTitleBar();
        et_text = (EditText) findViewById(R.id.et_text);
        et_text.setText(intent.getStringExtra("uname"));
        et_text.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "编辑昵称", v -> {
            finish();
        }, null);
    }

    @Override
    public void finish() {
        String result = et_text.getText().toString().trim();
        if (TextUtils.isEmpty(result)) {
            showToast(this, "昵称不能为空");
            return;
        }
        Intent data = new Intent();
        data.putExtra("uname", result);
        setResult(RESULT_OK, data);
        super.finish();
    }
}