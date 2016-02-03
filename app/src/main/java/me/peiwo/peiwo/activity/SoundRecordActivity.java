package me.peiwo.peiwo.activity;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.fragment.RecordPermFragment;
import me.peiwo.peiwo.fragment.RecordingFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

/**
 * Created by chenhao on 2014-10-21 下午3:31.
 *
 * @modify:
 */
public class SoundRecordActivity extends BaseActivity implements View.OnClickListener {
    private ViewPager viewpager_container;
    //    private static final int MAX_PAGES_COUNT = 3;
    private static final int MAX_PAGES_COUNT = 2;
//    public boolean needShowAlert = false;
    private Button btn_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_sound_record);
        findViewById(R.id.btn_left).setOnClickListener(this);
        btn_right = (Button) findViewById(R.id.btn_right);
        btn_right.setOnClickListener(this);
        viewpager_container = (ViewPager) findViewById(R.id.viewpager_container);
        viewpager_container.setOffscreenPageLimit(MAX_PAGES_COUNT);
        viewpager_container.setAdapter(new SRPagerAdapter(getSupportFragmentManager()));
        setPage(0);
    }


    public void setPage(int index) {
        viewpager_container.setCurrentItem(index);
//        if (index == 2) {
//            btn_right.setVisibility(View.VISIBLE);
//            needShowAlert = true;
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
//                if (needShowAlert) {
//                    new AlertDialog.Builder(SoundRecordActivity.this)
//                            .setMessage("您正在录音，是否要退出").setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            finish();
//                        }
//                    }).setNegativeButton("取消", null).create().show();
//                }
                onBackPressed();
                break;
            case R.id.btn_right:
//                doSaveRecord();
                break;
        }
    }
//
//    private void doSaveRecord() {
//        Trace.i("doSaveRecord");
//        finish();
//    }

    class SRPagerAdapter extends FragmentPagerAdapter {
        public SRPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return createItem(position);
        }

        @Override
        public int getCount() {
            return MAX_PAGES_COUNT;
        }

        private Fragment createItem(int position) {
            switch (position) {
                case 0:
                    return RecordPermFragment.newInstance();
                case 1:
                    return RecordingFragment.newInstance();
//                case 2:
//                    return RecordCompFragment.newInstance();

            }
            return null;
        }
    }

}
