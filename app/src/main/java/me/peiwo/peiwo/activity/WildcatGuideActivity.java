package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.fragment.*;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.service.PlayerService;
import me.peiwo.peiwo.util.HourGlassAgent;
import me.peiwo.peiwo.util.SharedPreferencesUtil;
import me.peiwo.peiwo.util.UserManager;

public class WildcatGuideActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    public static final String K_NEED_WILDCAT = "need_wildcat";
    private static final int MAX_PAGE = 6;
    private int mGender;
    private ViewPager vp_wild_guide;
    private static final String PATH_PREFIX = "wild_guide_music/";
    private static final String[][] MUSIC_PATH_ARRAY = {
            {PATH_PREFIX + "mguide0.mp3", PATH_PREFIX + "mguide1.mp3", PATH_PREFIX + "mguide2.mp3", PATH_PREFIX + "mguide3.mp3", PATH_PREFIX + "mguide4.mp3", PATH_PREFIX + "mguide5.mp3"},
            {PATH_PREFIX + "wguide0.mp3", PATH_PREFIX + "wguide1.mp3", PATH_PREFIX + "wguide2.mp3", PATH_PREFIX + "wguide3.mp3", PATH_PREFIX + "wguide4.mp3", PATH_PREFIX + "wguide5.mp3"}
    };

    private long start = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wildcat_guide);
        mGender = UserManager.getGender(this);
        init();

        HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK25() == 0) {
            hourGlassAgent.setK25(1);
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.postK("k25");
        }
    }


    private void init() {
        SharedPreferencesUtil.putBooleanExtra(this, Constans.SP_KEY_WILD_GUIDE, false);
        vp_wild_guide = (ViewPager) findViewById(R.id.vp_wild_guide);
        vp_wild_guide.setOffscreenPageLimit(MAX_PAGE);
        vp_wild_guide.setAdapter(new WildGuidePagerAdapter(getSupportFragmentManager()));
        vp_wild_guide.addOnPageChangeListener(this);
        playMusicByIndex(0);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        playMusicByIndex(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void click(View v) {
        if (v.getId() == R.id.v_stub) {
            changePage();
        }
    }

    private void changePage() {
        int index = vp_wild_guide.getCurrentItem();
        index++;
        if (index >= MAX_PAGE) {
            finish();
            if (getIntent().getBooleanExtra(K_NEED_WILDCAT, false)) {
                startActivity(new Intent(this, AgoraWildCallActivity.class));
            }
        } else {
            vp_wild_guide.setCurrentItem(index, false);
        }
    }

    private void playMusicByIndex(int index) {
        int location;
        if (mGender == AsynHttpClient.GENDER_MASK_MALE) {
            location = 1;
        } else {
            location = 0;
        }
        //AudioPlayerUtil.playAudioByAssetsPath(this, MUSIC_PATH_ARRAY[location][index], false);
        PlayerService playerService = PlayerService.getInstance();
        playerService.playAssetFileCommand(playerService.getMusicAssetPath(this, MUSIC_PATH_ARRAY[location][index]), false);
    }

    @Override
    public void finish() {
        PlayerService.getInstance().releaseIgnoreCaseCommand();
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK26() == 0) {
            hourGlassAgent.setK26(1);
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            long temp = (System.currentTimeMillis() - start) / 1000;
            app.postKV("k26", String.valueOf(temp));
        }
    }

    public int getGender() {
        return mGender;
    }

    static class WildGuidePagerAdapter extends FragmentPagerAdapter {

        public WildGuidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return WildGuideStepOneFragment.newInstance();
                case 1:
                    return WildGuideStepTwoFragment.newInstance();
                case 2:
                    return WildGuideStepThreeFragment.newInstance();
                case 3:
                    return WildGuideStepFourFragment.newInstance();
                case 4:
                    return WildGuideStepFiveFragment.newInstance();
                case 5:
                    return WildGuideStepSixFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return MAX_PAGE;
        }
    }
}
