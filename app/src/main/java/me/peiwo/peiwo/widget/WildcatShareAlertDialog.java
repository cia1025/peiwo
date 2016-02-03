package me.peiwo.peiwo.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import org.json.JSONException;
import org.json.JSONObject;

public class WildcatShareAlertDialog extends Dialog implements
        View.OnClickListener {
    public static final int SHARE_WHICH_FRIENDS_CYCLE = 0;
    public static final int SHARE_WHICH_WEIBO = 1;
    public static final int SHARE_WHICH_QQ_ZONE = 2;
    public static final int SHARE_WHICH_CLOSE = -1;

    private OnWildcalShareClickListener listener;
    //    private LinearLayout lin_close;
//    private TextView tv_hearing;
//    private TextView tv_connect;
//    private TextView tv_talk;
//    private TextView tv_favour;
//    private Typeface tf;
//    private ImageView iv_emotion;
//    private RelativeLayout v_share_view; // 保存截图
//    private Context context;
//    private String data;
    private String share_content_pople;
//    private String share_content_nickname;
//    private ImageLoader imageLoader;

    private TextView var_view_swz;
    private TextView var_view_shycd;
    private TextView var_view_call_time;
    private TextView var_view_jietong;
    private TextView var_view_recommend;
    private ImageView iv_title_img;
    private View v_share_view;

    public WildcatShareAlertDialog(Context context) {
        super(context, R.style.AnimDialogLoading);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_alert_wildcal_share);
        v_share_view = findViewById(R.id.v_share_view);
        var_view_swz = (TextView) findViewById(R.id.var_view_swz);
        var_view_shycd = (TextView) findViewById(R.id.var_view_shycd);
        var_view_call_time = (TextView) findViewById(R.id.var_view_call_time);
        var_view_jietong = (TextView) findViewById(R.id.var_view_jietong);
        var_view_recommend = (TextView) findViewById(R.id.var_view_recommend);
        iv_title_img = (ImageView) findViewById(R.id.iv_title_img);
        //Typeface tf_font = Typeface.createFromAsset(getContext().getAssets(), "fonts/harlowsi_o.ttf");
        //var_view_swz.setTypeface(tf_font);
        //var_view_shycd.setTypeface(tf_font);
        //var_view_call_time.setTypeface(tf_font);
        //var_view_jietong.setTypeface(tf_font);
        //var_view_recommend.setTypeface(tf_font);
        findViewById(R.id.v_close).setOnClickListener(this);
        findViewById(R.id.v_share_qq_zone).setOnClickListener(this);
        findViewById(R.id.v_share_friends_cycle).setOnClickListener(this);
        findViewById(R.id.v_share_weibo).setOnClickListener(this);
    }


    public void setTextViewData(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            //String share_content_nickname = obj.getString("title");
            var_view_swz.setText(obj.optString("volume"));
            var_view_shycd.setText(String.format("%.2f%s", obj.optDouble("welcome_percent") * 100, "%"));
            share_content_pople = obj.optString("count");
            var_view_jietong.setText(share_content_pople);
            var_view_call_time.setText(obj.optString("duration"));
            var_view_recommend.setText(obj.optString("like"));
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(false)
                    .cacheOnDisk(false)
                    .build();
            ImageLoader.getInstance().displayImage(obj.optString("image"), iv_title_img, options);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void show() {
        try {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            params.dimAmount = 0.5f;
            super.show();
            DisplayMetrics metrics = new DisplayMetrics();
            getWindow().getWindowManager().getDefaultDisplay()
                    .getMetrics(metrics);
            params.width = metrics.widthPixels * 3 / 4;
            //params.height = (int) (metrics.heightPixels * 0.7);
            getWindow().setAttributes(params);
        } catch (Exception ex) {
        }
    }

    public interface OnWildcalShareClickListener {
        void onShareClickListener(int which, Bitmap bitmap,
                                  String content);
    }

    public void setOnShareClickListener(OnWildcalShareClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if (listener == null) {
            return;
        }
        v_share_view.setBackgroundResource(R.drawable.wild_share_bg);
        v_share_view.setDrawingCacheEnabled(true);
        Bitmap bitmap = v_share_view.getDrawingCache();
        String content = getShareContent();
        // 刚刚在“陪我”和xxx人免费限时通话，获得了xxx称号，谁要来和我一起玩?
        switch (v.getId()) {
            case R.id.v_share_friends_cycle:
                listener.onShareClickListener(SHARE_WHICH_FRIENDS_CYCLE,
                        mergeBitmap(bitmap), content);
                dismiss(true);
                break;
            case R.id.v_share_weibo:
                listener.onShareClickListener(SHARE_WHICH_WEIBO,
                        mergeBitmap(bitmap), content);
                dismiss(false);
                break;
            case R.id.v_share_qq_zone:
                listener.onShareClickListener(SHARE_WHICH_QQ_ZONE,
                        mergeBitmap(bitmap), content);
                dismiss(true);
                break;
            default:
                dismiss(true);
                break;
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        v_share_view.setDrawingCacheEnabled(false);
    }

    public void dismiss(boolean isFinish) {
        super.dismiss();
        if (isFinish) {
            listener.onShareClickListener(SHARE_WHICH_CLOSE, null, null);
        }
    }

    private String getShareContent() {
        return String.format(Constans.WX_SHARE_CONTENT,
                share_content_pople);
    }

    public Bitmap mergeBitmap(Bitmap background) {
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        Bitmap banner = BitmapFactory.decodeResource(
                getContext().getResources(), R.drawable.banner);
        int banner_h = banner.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(bgWidth, bgHeight + banner_h, Config.ARGB_8888);
        Canvas cv = new Canvas(newBitmap);
        cv.drawBitmap(background, 0, 0, null);// 在 0，0坐标开始画入bg
        cv.drawBitmap(banner, 0, bgHeight, null);// 在
        cv.save(Canvas.ALL_SAVE_FLAG);// 保存
        cv.restore();// 存储
        if (!banner.isRecycled()) {
            banner.recycle();
        }
        return newBitmap;
    }
}
