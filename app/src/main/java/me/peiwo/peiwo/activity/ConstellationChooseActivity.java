package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.ConstellationChooseAdapter;
import me.peiwo.peiwo.model.ConstellationChooseModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.NetUtil;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.widget.PWGridView;
import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by fuhaidong on 15/9/28.
 */
public class ConstellationChooseActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private Handler handler = new Handler();
    private ProgressBar progressBar;
    private TextView tv_hepai;
    private String zodiac_name;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constellation_choose);
        init();
    }

    private void init() {
        PWGridView gv_layout = (PWGridView) findViewById(R.id.gv_layout);
        gv_layout.setSelector(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        gv_layout.setOnItemClickListener(this);
        List<ConstellationChooseModel> mList = new ArrayList<ConstellationChooseModel>();
        prepareData(mList);
        gv_layout.setAdapter(new ConstellationChooseAdapter(this, mList));
        tv_hepai = (TextView) findViewById(R.id.tv_hepai);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        getMatchzodiac(mList);
    }

    //获取合拍星座
    private void getMatchzodiac(final List<ConstellationChooseModel> mList) {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_MATCHZODIAC, new MsgStructure() {
            @Override
            public void onReceive(final JSONObject data) {
                if (isFinishing()) return;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        tv_hepai.setVisibility(View.VISIBLE);
                        setHepaiXingzuo(mList, data);
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {
                if (isFinishing()) return;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        tv_hepai.setVisibility(View.VISIBLE);
                        //客户端产生一个随机数，设置合拍星座的图标
                        Random random = new Random();
                        int index = random.nextInt(mList.size() - 1);
                        ConstellationChooseModel model = mList.get(index);
                        zodiac_name = model.key;
                        setHepaiDrawableByResource(model.res_id);
                    }
                });
            }
        });
    }

    private void setHepaiDrawableByResource(int res_id) {
        Drawable drawable = PWUtils.getCompoundDrawable(res_id, this);
        tv_hepai.setCompoundDrawables(null, drawable, null, null);
    }

    @Override
    public void finish() {
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        handler = null;
        super.finish();
    }

    private void setHepaiXingzuo(List<ConstellationChooseModel> mList, JSONObject data) {
        zodiac_name = data.optString("zodiac_name", "");
        for (ConstellationChooseModel model : mList) {
            if (zodiac_name.equals(model.key)) {
                setHepaiDrawableByResource(model.res_id);
                break;
            }
        }
    }

    private void prepareData(List<ConstellationChooseModel> mList) {
//        第一排：摩羯座  水瓶座 双鱼座
//
//        第二排：白羊座  金牛座 双子座
//
//        第三排：巨蟹座  狮子座 处女座
//
//        第四排：天秤座 天蝎座 射手座
        ConstellationChooseModel model;
        model = new ConstellationChooseModel(R.drawable.ic_mojie, "摩羯座", "摩羯");
        mList.add(model);
        model = new ConstellationChooseModel(R.drawable.ic_shuiping, "水瓶座", "水瓶");
        mList.add(model);
        model = new ConstellationChooseModel(R.drawable.ic_shuangyu, "双鱼座", "双鱼");
        mList.add(model);
        model = new ConstellationChooseModel(R.drawable.ic_baiyang, "白羊座", "白羊");
        mList.add(model);
        model = new ConstellationChooseModel(R.drawable.ic_jinniu, "金牛座", "金牛");
        mList.add(model);
        model = new ConstellationChooseModel(R.drawable.ic_shuangzi, "双子座", "双子");
        mList.add(model);
        model = new ConstellationChooseModel(R.drawable.ic_juxie, "巨蟹座", "巨蟹");
        mList.add(model);
        model = new ConstellationChooseModel(R.drawable.ic_shizi, "狮子座", "狮子");
        mList.add(model);
        model = new ConstellationChooseModel(R.drawable.ic_chunv, "处女座", "处女");
        mList.add(model);
        model = new ConstellationChooseModel(R.drawable.ic_tiancheng, "天秤座", "天秤");
        mList.add(model);
        model = new ConstellationChooseModel(R.drawable.ic_tianxie, "天蝎座", "天蝎");
        mList.add(model);
        model = new ConstellationChooseModel(R.drawable.ic_sheshou, "射手座", "射手");
        mList.add(model);
    }

    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tv_hepai:
                if (TextUtils.isEmpty(zodiac_name))
                    return;
                startWildCat(zodiac_name);
                break;
            case R.id.iv_close:
                finish();
                break;
            default:
                break;
        }
    }

    private void startWildCat(String constell) {
        PeiwoApp app = PeiwoApp.getApplication();
        if (app.getCallType() == PeiwoApp.CALL_TYPE.CALL_REAL) {
            showToast(this, "您当前正在通话");
            return;
        }
        if (app.getNetType() == NetUtil.NO_NETWORK) {
            showToast(this, "网络连接失败");
            return;
        }
        Intent intent = new Intent(this, WildCatCallActivity.class);
        intent.putExtra(WildCatCallActivity.K_CONSTELL, constell);
        startActivity(intent);
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ConstellationChooseModel model = (ConstellationChooseModel) parent.getAdapter().getItem(position);
        startWildCat(model.key);
    }
}