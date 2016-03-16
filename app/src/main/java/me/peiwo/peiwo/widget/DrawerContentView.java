package me.peiwo.peiwo.widget;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.GlobalWebViewActivity;
import me.peiwo.peiwo.activity.LazyGuyActivity;
import me.peiwo.peiwo.activity.SettingActivity;
import me.peiwo.peiwo.activity.UpdateProfileActivity;
import me.peiwo.peiwo.activity.UserInfoActivity;
import me.peiwo.peiwo.activity.WantMakeMoney;
import me.peiwo.peiwo.activity.WildcatCallRecordActivity;
import me.peiwo.peiwo.adapter.DrawerContentAdapter;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.model.DrawerContentModel;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.UmengStatisticsAgent;
import me.peiwo.peiwo.util.UserManager;

/**
 * Created by fuhaidong on 15/10/19.
 */
public class DrawerContentView extends RecyclerView {

    public static final int ITEM_INDEX_UPDATE_MYWALLET = 1;
    public static final int ITEM_INDEX_UPDATE_WILDLOGS = 2;
    public static final int ITEM_INDEX_UPDATE_VOICE_VAR = 3;
    public static final int ITEM_INDEX_UPDATE_LAZYGUY = 4;
    public static final int ITEM_INDEX_UPDATE_NEWGUIDE = 6;
    public static final int ITEM_INDEX_UPDATE_SETTING = 7;

    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_ITEM = 1;
    public static final int VIEW_TYPE_DECORATION = 2;

    private List<DrawerContentModel> mList = new ArrayList<>();

    private DrawerLayout drawerLayout;
    private DrawerContentAdapter adapter;

    private String link;

    public void setDrawerLayout(final DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
        if (drawerLayout != null) {
            drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {

                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    updateUser();
                    getADInfo();
                    getLazyVoice();
                    UmengStatisticsAgent.onEvent(getContext(), UMEventIDS.UMEPERSONALDYNAMIC);
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    Object tag = drawerLayout.getTag();
                    if (tag != null && tag instanceof Intent) {
                        getContext().startActivity((Intent) tag);
                        drawerLayout.setTag(null);
                    }
                }

                @Override
                public void onDrawerStateChanged(int newState) {

                }
            });
        }
    }

    private void getLazyVoice() {
        ApiRequestWrapper.openAPIGET(getContext(), new ArrayList<NameValuePair>(), AsynHttpClient.API_USERINFO_LAZY_VOICE, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                String voice_url = data.optString("voice_url", "");
                if (!TextUtils.isEmpty(voice_url)) {
                    if (!mList.isEmpty() && mList.size() > 5) {
                        DrawerContentModel lazyModel = mList.get(ITEM_INDEX_UPDATE_LAZYGUY);
                        lazyModel.setHasVoice(true);
                        if (DrawerContentView.this.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && !DrawerContentView.this.isComputingLayout()) {
                            adapter.notifyItemChanged(ITEM_INDEX_UPDATE_LAZYGUY);
                        }

                    }
                }
            }

            @Override
            public void onError(int error, Object ret) {
                if (!mList.isEmpty() && mList.size() > 5) {
                    DrawerContentModel lazyModel = mList.get(ITEM_INDEX_UPDATE_LAZYGUY);
                    lazyModel.setHasVoice(false);
                    if (DrawerContentView.this.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && !DrawerContentView.this.isComputingLayout()) {
                        adapter.notifyItemChanged(ITEM_INDEX_UPDATE_LAZYGUY);
                    }
                }
            }
        });
    }

    private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(false).build();

    private void getADInfo() {
        ImageView iv_ad_info = (ImageView) drawerLayout.findViewById(R.id.iv_ad_info);
        iv_ad_info.setOnClickListener(v -> {
            if (TextUtils.isEmpty(this.link)) return;
            Intent intent = new Intent(getContext(), GlobalWebViewActivity.class);
            DrawerContentModel model = mList.get(0);
            intent.putExtra(GlobalWebViewActivity.URL, String.format("%s&uid=%s&session_data=%s", this.link, model.uid, model.session_data));
            if (drawerLayout != null) {
                drawerLayout.setTag(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
                UmengStatisticsAgent.onEvent(getContext(), UMEventIDS.UMESLIDEBARSHARE);
            }
        });
        ArrayList<NameValuePair> params = new ArrayList<>();
        DisplayMetrics metrics = PWUtils.getMetrics(getContext());
        params.add(new BasicNameValuePair("resolution", metrics.widthPixels + "x" + (metrics.heightPixels - PWUtils.getStatusBarHeight(getContext()))));
        ApiRequestWrapper.openAPIGET(getContext(), params, AsynHttpClient.API_SETTING_SYSTEM, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                post(() -> {
                    try {
                        if (iv_ad_info.getHeight() == 0) {
                            ViewGroup.LayoutParams layoutParams = iv_ad_info.getLayoutParams();
                            layoutParams.width = getWidth()/* - PWUtils.getPXbyDP(getContext(), 20)*/;
                            layoutParams.height = layoutParams.width / 4;
                            iv_ad_info.setLayoutParams(layoutParams);
                        }
                        Object ads_info_object = data.get("ads_info");
                        if (ads_info_object instanceof JSONObject) {
                            JSONObject ads_info = (JSONObject) ads_info_object;
                            link = ads_info.optString("link");
                            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                                ImageLoader.getInstance().displayImage(ads_info.optString("image_url"), iv_ad_info, options);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }

    private void updateUser() {
        PWUserModel pwUser = UserManager.getPWUser(getContext());
        if (!TextUtils.isEmpty(pwUser.score)) {
            DrawerContentModel model = mList.get(ITEM_INDEX_UPDATE_VOICE_VAR);
            model.voice_var = String.format("%.2f", Float.valueOf(pwUser.score));
            adapter.notifyItemChanged(ITEM_INDEX_UPDATE_VOICE_VAR);
        }

        DrawerContentModel model_header = mList.get(0);
        model_header.avatar_thumbnail = pwUser.avatar_thumbnail;
        model_header.uname = pwUser.name;
        adapter.notifyItemChanged(0);
    }

    public DrawerContentView(Context context) {
        super(context);
        init();
    }

    public DrawerContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawerContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setLayoutManager(layoutManager);
        final PWUserModel pwUser = UserManager.getPWUser(getContext());
        prepareData(mList, pwUser);
        adapter = new DrawerContentAdapter(getContext(), mList);
        adapter.setOnUpdateProfileListener(() -> {
            Intent intent = new Intent(getContext(), UpdateProfileActivity.class);
            if (DrawerContentView.this.drawerLayout != null) {
                DrawerContentView.this.drawerLayout.setTag(intent);
                DrawerContentView.this.drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        adapter.setOnItemClickListener((itemView, position) -> {
            DrawerContentModel model = mList.get(position);
            //changeSelectedStatus(position, mList);
            //adapter.notifyDataSetChanged();
            Intent intent = null;
            if (model.view_type == VIEW_TYPE_ITEM) {
                switch (position) {
                    case ITEM_INDEX_UPDATE_MYWALLET:
                        intent = new Intent(getContext(), WantMakeMoney.class);
                        break;
                    case ITEM_INDEX_UPDATE_WILDLOGS:
                        intent = new Intent(getContext(), WildcatCallRecordActivity.class);
                        break;
                    case ITEM_INDEX_UPDATE_VOICE_VAR:
                        intent = UserManager.buildMyHEPAIIntent(getContext());
                        break;
                    case ITEM_INDEX_UPDATE_LAZYGUY:
                        intent = new Intent(getContext(), LazyGuyActivity.class);
                        break;
                    case ITEM_INDEX_UPDATE_NEWGUIDE:
                        //陪我学堂
                        intent = getPeiwoXueTangIntent();
                        break;
                    case ITEM_INDEX_UPDATE_SETTING:
                        intent = new Intent(getContext(), SettingActivity.class);
                        break;
                }
            } else if (model.view_type == VIEW_TYPE_HEADER) {
                intent = new Intent(getContext(), UserInfoActivity.class);
                intent.putExtra(AsynHttpClient.KEY_TUID, pwUser.uid);
            }
            if (intent != null) {
                if (DrawerContentView.this.drawerLayout != null) {
                    DrawerContentView.this.drawerLayout.setTag(intent);
                    DrawerContentView.this.drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });
        setAdapter(adapter);
    }

    private Intent getPeiwoXueTangIntent() {
        Intent intent = new Intent(getContext(), GlobalWebViewActivity.class);
        intent.putExtra(GlobalWebViewActivity.URL, Constans.PEIWO_SCHOOL_URL);
        return intent;
    }


    private void prepareData(List<DrawerContentModel> mList, PWUserModel pwUser) {
        DrawerContentModel model = new DrawerContentModel(pwUser.name, pwUser.avatar_thumbnail, "陪我号:" + pwUser.uid, VIEW_TYPE_HEADER);
        model.uid = String.valueOf(pwUser.uid);
        model.session_data = pwUser.session_data;
        mList.add(model);
//        model = new DrawerContentModel(true, "修改资料", 0, VIEW_TYPE_ITEM);
//        mList.add(model);
        model = new DrawerContentModel(false, "陪我特权", 0, VIEW_TYPE_ITEM);
        mList.add(model);
//        model = new DrawerContentModel(false, "陪我的圈", 0, VIEW_TYPE_ITEM);
//        mList.add(model);
        model = new DrawerContentModel(false, "新声记录", 0, VIEW_TYPE_ITEM);
        mList.add(model);
        model = new DrawerContentModel(false, "个人声望", 0, VIEW_TYPE_ITEM);
        model.voice_var = String.format("%.2f", Float.valueOf(TextUtils.isEmpty(pwUser.score) ? "0" : pwUser.score));
        mList.add(model);
//        model = new DrawerContentModel(false, "合拍统计", 0, VIEW_TYPE_ITEM);
//        mList.add(model);
        model = new DrawerContentModel(false, "懒人招呼", 0, VIEW_TYPE_ITEM);
        mList.add(model);
        model = new DrawerContentModel(false, "", 0, VIEW_TYPE_DECORATION);
        mList.add(model);
        model = new DrawerContentModel(false, "使用说明", 0, VIEW_TYPE_ITEM);
        mList.add(model);
        model = new DrawerContentModel(false, "设置", 0, VIEW_TYPE_ITEM);
        mList.add(model);
    }

}
