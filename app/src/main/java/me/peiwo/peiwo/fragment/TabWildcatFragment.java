package me.peiwo.peiwo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.AgoraWildCallActivity;
import me.peiwo.peiwo.activity.ChargeActivity;
import me.peiwo.peiwo.activity.ConstellationChooseActivity;
import me.peiwo.peiwo.activity.WildcatGuideActivity;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.util.*;
import org.json.JSONObject;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import java.util.ArrayList;

/**
 * Created by fuhaidong on 14-9-10.
 */
public class TabWildcatFragment extends PPBaseFragment implements
        View.OnClickListener {

    private TextView btn_match_constellation;
    private CompositeSubscription mCompositeSubscription;

    public static TabWildcatFragment newInstance() {
        return new TabWildcatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wildcat, container, false);
        init(v);
        return v;
    }

    private void init(View v) {
        mCompositeSubscription = new CompositeSubscription();
        View btn_start_wildcat = v.findViewById(R.id.btn_start_wildcat);
        btn_start_wildcat.setOnClickListener(this);
        int gender = UserManager.getGender(getActivity());
        btn_match_constellation = (TextView) v.findViewById(R.id.btn_match_constellation);
        ImageView iv_bg_new_voice = (ImageView) v.findViewById(R.id.iv_bg_new_voice);
        if (gender == AsynHttpClient.GENDER_MASK_FEMALE) {
            btn_match_constellation.setVisibility(View.VISIBLE);
            btn_match_constellation.setOnClickListener(this);
            iv_bg_new_voice.setImageResource(R.drawable.bg_new_voice_female);
            if (needGuide()) {
                btn_match_constellation.setText("新声引导");
            } else {
                btn_match_constellation.setText("星座匹配");
            }
        } else {
            btn_match_constellation.setVisibility(View.GONE);
            iv_bg_new_voice.setImageResource(R.drawable.bg_new_voice_male);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
            UmengStatisticsAgent.onEvent(getActivity(), UMEventIDS.UMENEWVOICE);
    }

    public void onDestroy() {
        super.onDestroy();
    }

    HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_start_wildcat:
                //原来的匹配
                if (needGuide()) {
                    startWildGuide();
                    btn_match_constellation.setText("星座匹配");

                    if (hourGlassAgent.getStatistics() && hourGlassAgent.getK24() == 0) {
                        hourGlassAgent.setK24(1);
                        PeiwoApp app = (PeiwoApp) getActivity().getApplicationContext();
                        app.postK("k24");
                    }
                } else {
                    startWildcat();
                }
                break;
            case R.id.btn_match_constellation:
                //新加选择星座的匹配
                if (needGuide()) {
                    startWildGuide();
                    btn_match_constellation.setText("星座匹配");
                } else {
                    startActivity(new Intent(getActivity(), ConstellationChooseActivity.class));
                }
                break;
        }
    }

    private void startWildcat() {
        UmengStatisticsAgent.onEvent(getActivity(), UMEventIDS.UMENEWVOICESTART);
        PeiwoApp app = PeiwoApp.getApplication();
        if (app.getCallType() == PeiwoApp.CALL_TYPE.CALL_REAL) {
            showToast(getActivity(), "您当前正在通话");
            return;
        }
        if (!PWUtils.isNetWorkAvailable(getActivity())) {
            showToast(getActivity(), "无网络");
            return;
        }
        //startActivity(new Intent(getActivity(), WildCatCallActivity.class));
        wildcallIfhasPermission();
    }

    private void wildcallIfhasPermission() {
        Subscription subscription = ApiRequestWrapper.apiGetJson(getActivity(), new ArrayList<>(), AsynHttpClient.API_WILDCAT_PERMISSION).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<JSONObject>() {
            @Override
            public void onStart() {
                showAnimLoading("", false, false);
            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                dismissAnimLoading();
                Toast.makeText(getActivity(), "请求失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(JSONObject object) {
                dismissAnimLoading();
                if (object.optInt("permission") == 1)
                    startActivity(new Intent(getActivity(), AgoraWildCallActivity.class));
                else
                    upgradePermission();
            }
        });
        mCompositeSubscription.add(subscription);
    }

    private void upgradePermission() {
        new AlertDialog.Builder(getActivity())
                .setTitle("升级权限")
                .setPositiveButton("确定", (dialog, which) -> {
                    new Intent(getActivity(), ChargeActivity.class);
                })
                .setNegativeButton("取消", null)
                .create().show();
    }

    private void startWildGuide() {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getActivity(), android.R.anim.fade_in, android.R.anim.fade_out);
        Intent intent = new Intent(getActivity(), WildcatGuideActivity.class);
        intent.putExtra(WildcatGuideActivity.K_NEED_WILDCAT, true);
        ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
    }

    private boolean needGuide() {
        return SharedPreferencesUtil.getBooleanExtra(getActivity(), Constans.SP_KEY_WILD_GUIDE, true);
    }


    @Override
    protected String getPageName() {
        return "新声页面";
    }
}