package me.peiwo.peiwo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.widget.ClearEditText;

/**
 * Created by Dong Fuhai on 2014-08-18 16:32.
 *
 * @modify:
 */
public class UpdateProfessionActivity extends Activity {

    private ClearEditText et_text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_profession);
        init();
    }

    private void init() {
        Intent intent = getIntent();
        setTitleBar();
        et_text = (ClearEditText) findViewById(R.id.et_text);
        et_text.setText(intent.getStringExtra("profession"));
        et_text.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "编辑职业", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }, null);
    }

    @Override
    public void finish() {
        String result = et_text.getText().toString();
        Intent data = new Intent();
        data.putExtra("profession", result);
        setResult(RESULT_OK, data);
        super.finish();
    }
}