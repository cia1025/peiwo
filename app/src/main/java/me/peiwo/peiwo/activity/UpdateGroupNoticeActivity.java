package me.peiwo.peiwo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.Bind;
import com.jakewharton.rxbinding.widget.RxTextView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.GroupConstant;
import me.peiwo.peiwo.util.SharedPreferencesUtil;

public class UpdateGroupNoticeActivity extends BaseActivity {
    private static final String K_FIST_NOTICE = "k_g_n";
    @Bind(R.id.et_group_notice)
    EditText et_group_notice;

    private String last_notice;

    public static final String K_GROUP_NOTICE = "notice";
    public static final String K_MEMBER_TYPE = "member_type";
    private InputMethodManager mInputManager;

    private boolean interrupt_touch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_group_notice);
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.lazy_guy_voice_time_color));
        mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        setTitle("群公告");
        init();
    }

    private void init() {
        //et_group_notice.setEnabled(false);
        et_group_notice.setOnTouchListener((v, event) -> interrupt_touch);
        et_group_notice.setCursorVisible(false);
        et_group_notice.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)});
        Intent data = getIntent();
        et_group_notice.setText(data.getStringExtra(K_GROUP_NOTICE));
        last_notice = et_group_notice.getText().toString();
        String member_type = data.getStringExtra(K_MEMBER_TYPE);
        editableIfAuthority(member_type);
        RxTextView.afterTextChangeEvents(et_group_notice).skip(1).subscribe(textViewAfterTextChangeEvent -> {
            if (textViewAfterTextChangeEvent.editable().length() >= 500) {
                Toast.makeText(this, "字数超过限制", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editableIfAuthority(String member_type) {
        if (GroupConstant.MemberType.ADMIN.equals(member_type)) {
            setRightText("编辑");
            int first = SharedPreferencesUtil.getIntExtra(this, K_FIST_NOTICE, 0);
            if (first == 0) {
                SharedPreferencesUtil.putIntExtra(this, K_FIST_NOTICE, 1);
                changeEditableMode();
            }
        }
    }

    public void changeEditableMode() {
        //et_group_notice.setEnabled(true);
        hideRightText();
        interrupt_touch = false;
        et_group_notice.setCursorVisible(true);
        et_group_notice.setSelection(et_group_notice.getText().length());
        et_group_notice.requestFocus();
        et_group_notice.post(() -> mInputManager.showSoftInput(et_group_notice, 0));
    }

    @Override
    public void right_click(View v) {
        changeEditableMode();
    }

    @Override
    public void finish() {
        if (!et_group_notice.getText().toString().equals(last_notice)) {
            mInputManager.hideSoftInputFromWindow(et_group_notice.getWindowToken(), 0);
            alert();
            return;
        }
        super.finish();
    }

    private void alert() {
        new AlertDialog.Builder(this)
                .setTitle("是否保存本次编辑")
                .setNegativeButton("放弃", (dialog1, which1) -> {
                    super.finish();
                })
                .setPositiveButton("保存", (dialog, which) -> {
                    String rst = et_group_notice.getText().toString();
                    Intent data = new Intent();
                    data.putExtra(K_GROUP_NOTICE, rst);
                    setResult(RESULT_OK, data);
                    super.finish();
                })
                .create().show();
    }

}
