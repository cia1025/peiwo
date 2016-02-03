package me.peiwo.peiwo.db;

import android.content.Context;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.util.UserManager;

import java.util.Locale;

/**
 * Created by fuhaidong on 14-8-20.
 */
public class PWDBUtil {
    /**
     * 根据当前用户的uid创建消息中心表
     *
     * @param context
     * @return
     */
    public static String getCurrentMsgCenterDBName(Context context) {
        int uid = UserManager.getUid(context);
        if (uid == 0) {
            return null;
        }
        return String.format(Locale.getDefault(), "mmsgcenterv_db_%d", uid);
    }

    public static String getCurrentMsgCenterOldDBName(Context context) {
        return String.format(Locale.getDefault(), "mmsgcenterv2_%s", UserManager.getUid(context));
    }

    public static String getCurrentReMarkDBName(Context context) {
        return String.format("%s_%s", PWDBConfig.DB_NAME_MK_PREFIX, UserManager.getUid(context));
    }

}
