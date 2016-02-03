package me.peiwo.peiwo.util;

import java.util.HashMap;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

/**
 * Created by Dong Fuhai on 2014-06-13 上午11:22.
 *
 * @modify:
 */
public class UmengStatisticsAgent {

    private static final boolean ISSTATISTICS = true;

    public static void openActivityDurationTrack(boolean b) {
        if (ISSTATISTICS) {
            MobclickAgent.openActivityDurationTrack(b);
        }
    }

    public static void onResume(Context context) {
        if (ISSTATISTICS) {
            MobclickAgent.onResume(context);
        }
    }

    public static void onPause(Context context) {
        if (ISSTATISTICS) {
            MobclickAgent.onPause(context);
        }
    }

    public static void onPageStart(String pageName) {
        if (ISSTATISTICS) {
            MobclickAgent.onPageStart(pageName);
        }
    }

    public static void onPageEnd(String pageName) {
        if (ISSTATISTICS) {
            MobclickAgent.onPageEnd(pageName);
        }
    }

    public static void onEvent(Context context, String s_id) {
        if (ISSTATISTICS) {
            MobclickAgent.onEvent(context, s_id);
        }
    }

    public static void onEvent(Context context, String s_id, HashMap<String, String> map) {
        if (ISSTATISTICS) {
            MobclickAgent.onEvent(context, s_id, map);
        }
    }

    public static void update(Context context) {
        if (ISSTATISTICS)
            UmengUpdateAgent.update(context);
    }
}
