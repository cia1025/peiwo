package me.peiwo.peiwo.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.activity.RedBagActivity;
import me.peiwo.peiwo.activity.WelcomeActivity;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.db.PWDBHelper;
import me.peiwo.peiwo.model.ImageModel;
import me.peiwo.peiwo.model.PWUserModel;
import org.json.JSONArray;

import java.util.Locale;

/**
 * Created by Dong Fuhai on 2014-07-24 12:26.
 *
 * @modify:
 */
public class UserManager {
    //state: 用户状态 0: 未完成注册，需要完善注册信息  1: 正常状态
    public static final int STATE_UNINITED = 0;
    public static final int STATE_INITED = 1;
    public static final String KEY_STATE = "state";
    public static final String KEY_UID = "uid";
    public static final String KEY_TAGS = "tags";
    public static final String KEY_BIRTHDAY = "birthday";
    public static final String KEY_AVATAR_THUMBNAIL = "avatar_thumbnail";
    public static final String KEY_PROFESSION = "profession";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_CITY = "city";
    public static final String KEY_EMOTION = "emotion";
    public static final String KEY_SLOGAN = "slogan";
    public static final String KEY_PRICE = "price";
    public static final String KEY_MONEY = "money";
    public static final String KEY_NAME = "name";
    public static final String KEY_PROVINCE = "province";
    public static final String KEY_SESSION_DATA = "session_data";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_IMAGES = "images";
    public static final String KEY_CALL_DURATION = "call_duration";
    public static final String KEY_PHONE = "phone";
    //头像认证
    public static final String KEY_FLAGS = "flags";

    public static final String KEY_FOOD_TAGS = "food_tags";
    public static final String KEY_MUSIC_TAGS = "music_tags";
    public static final String KEY_MOVIE_TAGS = "movie_tags";
    public static final String KEY_BOOK_TAGS = "book_tags";
    public static final String KEY_TRAVEL_TAGS = "travel_tags";
    public static final String KEY_SPORTS_TAGS = "sports_tags";
    public static final String KEY_GAME_TAGS = "game_tags";

    public static boolean saveUser(Context context, PWUserModel model) {
        boolean b = true;
        SQLiteDatabase db = null;
        try {
            db = PWDBHelper.getInstance(context).getWritableDatabase(PWDBConfig.DB_NAME_USER);
            String del_sql = "delete from %s";
            db.execSQL(String.format(Locale.getDefault(), del_sql, PWDBConfig.TB_NAME_USER));
            String sql = "insert into %s (uid, tags, birthday, avatar_thumbnail, profession, state, avatar, city, emotion, slogan, price, money, name, province, session_data, gender, call_duration, images, phone, flags, food_tags, music_tags, movie_tags, book_tags, travel_tags, sports_tags, game_tags, reward_price, score) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            Object[] objects = new Object[]{model.uid, model.tags, model.birthday, model.avatar_thumbnail, model.profession, model.state, model.avatar, model.city, model.emotion, model.slogan, model.price, model.money, model.name, model.province, model.session_data, model.gender, model.call_duration, model.images_str, model.phone, model.flags, model.food_tags, model.music_tags, model.movie_tags, model.book_tags, model.travel_tags, model.sport_tags, model.game_tags, model.reward_price, model.score};
            db.execSQL(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), objects);
            SharedPreferencesUtil.putIntExtra(context, KEY_UID, model.uid);
            SharedPreferencesUtil.putStringExtra(context, KEY_SESSION_DATA, model.session_data);
            CustomLog.d("UserManager, saveUser. session data is : " + model.session_data);
            SharedPreferencesUtil.putIntExtra(context, KEY_GENDER, model.gender);
        } catch (Exception e) {
            b = false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return b;
    }

    public static PWUserModel getPWUser(Context context) {
        SQLiteDatabase db = null;
        Cursor c = null;
        PWUserModel model = null;
        try {
            db = PWDBHelper.getInstance(context).getReadableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "select * from %s";
            c = db.rawQuery(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), null);
            model = new PWUserModel();
            while (c.moveToNext()) {
                model.uid = c.getInt(c.getColumnIndex(KEY_UID));
                model.gender = c.getInt(c.getColumnIndex(KEY_GENDER));
                model.session_data = c.getString(c.getColumnIndex(KEY_SESSION_DATA));
                model.avatar = c.getString(c.getColumnIndex(KEY_AVATAR));
                model.avatar_thumbnail = c.getString(c.getColumnIndex(KEY_AVATAR_THUMBNAIL));
                model.birthday = c.getString(c.getColumnIndex(KEY_BIRTHDAY));
                model.city = c.getString(c.getColumnIndex(KEY_CITY));
                model.emotion = c.getInt(c.getColumnIndex(KEY_EMOTION));
                model.money = c.getString(c.getColumnIndex(KEY_MONEY));
                model.price = c.getString(c.getColumnIndex(KEY_PRICE));
                model.profession = c.getString(c.getColumnIndex(KEY_PROFESSION));
                model.province = c.getString(c.getColumnIndex(KEY_PROVINCE));
                model.slogan = c.getString(c.getColumnIndex(KEY_SLOGAN));
                model.state = c.getInt(c.getColumnIndex(KEY_STATE));
                model.tags = c.getString(c.getColumnIndex(KEY_TAGS));
                model.name = c.getString(c.getColumnIndex(KEY_NAME));
                model.call_duration = c.getLong(c.getColumnIndex(KEY_CALL_DURATION));
                model.phone = c.getString(c.getColumnIndex(KEY_PHONE));
                model.flags = c.getInt(c.getColumnIndex(KEY_FLAGS));
                model.images_str = c.getString(c.getColumnIndex(KEY_IMAGES));

                model.food_tags = c.getString(c.getColumnIndex(KEY_FOOD_TAGS));
                model.music_tags = c.getString(c.getColumnIndex(KEY_MUSIC_TAGS));
                model.movie_tags = c.getString(c.getColumnIndex(KEY_MOVIE_TAGS));
                model.book_tags = c.getString(c.getColumnIndex(KEY_BOOK_TAGS));
                model.travel_tags = c.getString(c.getColumnIndex(KEY_TRAVEL_TAGS));
                model.sport_tags = c.getString(c.getColumnIndex(KEY_SPORTS_TAGS));
                model.game_tags = c.getString(c.getColumnIndex(KEY_GAME_TAGS));
                model.reward_price = c.getInt(c.getColumnIndex("reward_price"));
                model.score = c.getString(c.getColumnIndex("score"));

                JSONArray array = new JSONArray(model.images_str);
                for (int i = 0; i < array.length(); i++) {
                    model.images.add(new ImageModel(array.getJSONObject(i)));
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return model;
    }

    public static int getGender(Context context) {
        int gender = SharedPreferencesUtil.getIntExtra(context, KEY_GENDER, -1);
        if (gender == -1) {
            PWUserModel mUser = UserManager.getPWUser(context);
            gender = mUser.gender;
            SharedPreferencesUtil.putIntExtra(context, KEY_GENDER, gender);
        }
        return gender;
    }

    public static String getAvatar_thumbnail(Context context) {
        SQLiteDatabase db = null;
        Cursor c = null;
        String avatar_thumbnail = null;
        try {
            db = PWDBHelper.getInstance(context).getReadableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "select avatar_thumbnail from %s";
            c = db.rawQuery(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), null);
            if (c.moveToFirst()) {
                avatar_thumbnail = c.getString(c.getColumnIndex("avatar_thumbnail"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return avatar_thumbnail;
    }

    public static int getUid(Context context) {
        int uid = SharedPreferencesUtil.getIntExtra(context, KEY_UID, 0);
        CustomLog.d("getUid, uid is : " + uid);
        if (uid == 0) {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                db = PWDBHelper.getInstance(context).getReadableDatabase(PWDBConfig.DB_NAME_USER);
                String sql = "select uid from %s";
                c = db.rawQuery(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), null);
                if (c.moveToFirst()) {
                    uid = c.getInt(c.getColumnIndex(KEY_UID));
                }
                if (uid != 0) {
                    SharedPreferencesUtil.putIntExtra(context, KEY_UID, uid);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
                if (db != null) {
                    db.close();
                }
            }
        }
        return uid;
    }

//    public static PWUserModel getPWUser(Context context) {
//        PeiwoApp app = (PeiwoApp) context.getApplicationContext();
//        PWUserModel model = app.getPWUser();
//        if (model == null) {
//            app.setPWUser();
//            model = app.getPWUser();
//        }
//        return model;
//    }

    public static boolean isLogin(Context context) {
        SQLiteDatabase db = null;
        Cursor c = null;
        int uid = 0;
        int state = 0;
        String session_data = null;
        try {
            db = PWDBHelper.getInstance(context).getReadableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "select uid, session_data, state from %s";
            c = db.rawQuery(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), null);
            if (c.moveToFirst()) {
                uid = c.getInt(c.getColumnIndex(KEY_UID));
                session_data = c.getString(c.getColumnIndex(KEY_SESSION_DATA));
                state = c.getInt(c.getColumnIndex(KEY_STATE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return uid > 0 && !TextUtils.isEmpty(session_data) && state == STATE_INITED;
    }

    public static boolean isLoginForLocal(Context context) {
        PWUserModel model = getPWUserForService(context);
        return model != null && model.uid > 0 && !TextUtils.isEmpty(model.session_data) && model.state == STATE_INITED;
    }

    public static boolean isInited(Context context) {
        return getUserState(context) == STATE_INITED;
    }

    public static String getUserPhone(Context context) {
        SQLiteDatabase db = null;
        Cursor c = null;
        String phone = null;
        try {
            db = PWDBHelper.getInstance(context).getReadableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "select phone from %s";
            c = db.rawQuery(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), null);
            if (c.moveToFirst()) {
                phone = c.getString(c.getColumnIndex(KEY_PHONE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return phone;
    }

    public static int getUserState(Context context) {
        SQLiteDatabase db = null;
        Cursor c = null;
        int state = 0;
        try {
            db = PWDBHelper.getInstance(context).getReadableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "select state from %s";
            c = db.rawQuery(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), null);
            if (c.moveToFirst()) {
                state = c.getInt(c.getColumnIndex(KEY_STATE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return state;
    }

    public static String getSessionData(Context context) {
        String session_data = SharedPreferencesUtil.getStringExtra(context, KEY_SESSION_DATA, "");
        CustomLog.d("getSessionData1, session data is : " + session_data);
        if (!TextUtils.isEmpty(session_data))
            return session_data;
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = PWDBHelper.getInstance(context).getReadableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "select session_data from %s";
            c = db.rawQuery(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), null);
            if (c.moveToFirst()) {
                session_data = c.getString(c.getColumnIndex(KEY_SESSION_DATA));
                CustomLog.d("getSessionData2, session data is : " + session_data);
            }
            if (!TextUtils.isEmpty(session_data)) {
                SharedPreferencesUtil.putStringExtra(context, KEY_SESSION_DATA, session_data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return session_data;
    }

    public static void clearUser(Context context) {
        CustomLog.d("UserManager, clearUser.");
        SQLiteDatabase db = null;
        try {
            db = PWDBHelper.getInstance(context).getWritableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "delete from %s";
            db.execSQL(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER));
            SharedPreferencesUtil.putIntExtra(context, KEY_UID, 0);
            SharedPreferencesUtil.putStringExtra(context, KEY_SESSION_DATA, "");
            SharedPreferencesUtil.putIntExtra(context, KEY_GENDER, 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public static void updatePrice(Context context, float mPrice) {
        SQLiteDatabase db = null;
        try {
            db = PWDBHelper.getInstance(context).getWritableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "update %s set price = ?";
            Object[] objects = new Object[]{String.valueOf(mPrice)};
            db.execSQL(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), objects);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public static void updateRewardPrice(Context context, String rewardPrice) {
        SQLiteDatabase db = null;
        try {
            db = PWDBHelper.getInstance(context).getWritableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "update %s set reward_price = ?";
            Object[] objects = new Object[]{rewardPrice};
            db.execSQL(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), objects);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public static void updateMoney(Context context, String money) {
        SQLiteDatabase db = null;
        try {
            db = PWDBHelper.getInstance(context).getWritableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "update %s set money = ?";
            Object[] objects = new Object[]{money};
            db.execSQL(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), objects);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public static void updateScore(Context context, String score) {
        SQLiteDatabase db = null;
        try {
            db = PWDBHelper.getInstance(context).getWritableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "update %s set score = ?";
            Object[] objects = new Object[]{score};
            db.execSQL(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), objects);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public static PWUserModel getPWUserForService(Context context) {
        PWUserModel model = null;
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = PWDBHelper.getInstance(context).getReadableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "select uid, session_data, state from %s";
            c = db.rawQuery(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), null);
            if (c != null && c.getCount() > 0) {
                model = new PWUserModel();
            }
            while (c.moveToNext()) {
                model.uid = c.getInt(c.getColumnIndex(KEY_UID));
                model.session_data = c.getString(c.getColumnIndex(KEY_SESSION_DATA));
                model.state = c.getInt(c.getColumnIndex(KEY_STATE));
//                model.gender = c.getInt(c.getColumnIndex(KEY_GENDER));
//                model.avatar = c.getString(c.getColumnIndex(KEY_AVATAR));
//                model.avatar_thumbnail = c.getString(c.getColumnIndex(KEY_AVATAR_THUMBNAIL));
//                model.birthday = c.getString(c.getColumnIndex(KEY_BIRTHDAY));
//                model.city = c.getString(c.getColumnIndex(KEY_CITY));
//                model.emotion = c.getInt(c.getColumnIndex(KEY_EMOTION));
//                model.money = c.getInt(c.getColumnIndex(KEY_MONEY));
//                model.price = c.getString(c.getColumnIndex(KEY_PRICE));
//                model.profession = c.getString(c.getColumnIndex(KEY_PROFESSION));
//                model.province = c.getString(c.getColumnIndex(KEY_PROVINCE));
//                model.slogan = c.getString(c.getColumnIndex(KEY_SLOGAN));
//                model.state = c.getInt(c.getColumnIndex(KEY_STATE));
//                model.tags = c.getString(c.getColumnIndex(KEY_TAGS));
//                model.name = c.getString(c.getColumnIndex(KEY_NAME));
//                model.images_str = c.getString(c.getColumnIndex(KEY_IMAGES));
//                JSONArray array = new JSONArray(model.images_str);
//                for (int i = 0; i<array.length(); i++){
//                    model.images.add(new ImageModel(array.getJSONObject(i)));
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return model;
    }

    /**
     * 获取用户的手机号
     *
     * @param context
     * @return
     */
    public static String getPWUserPhone(Context context) {
        String phone = null;
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = PWDBHelper.getInstance(context).getReadableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "select phone from %s";
            c = db.rawQuery(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), null);
            if (c.moveToFirst()) {
                phone = c.getString(c.getColumnIndex(KEY_PHONE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return phone;
    }

    /**
     * 修改手机号
     *
     * @param context
     * @param phone
     */
    public static void updatePhone(Context context, String phone) {
        SQLiteDatabase db = null;
        try {
            db = PWDBHelper.getInstance(context).getWritableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "update %s set phone = ?";
            Object[] objects = new Object[]{phone};
            db.execSQL(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), objects);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * 此方法只能用来存储openid token socialtype，不能用来进行其他操作，尤其是重新赋值会清空已经保存的值产生错误
     *
     * @param context
     * @param openid
     * @param opentoken
     * @param socialType
     */
    public static void saveOpenResultInPreference(Context context, String openid, String opentoken, int socialType) {
//        SharedPreferencesUtil.putStringExtra(context, Constans.SP_KEY_OPENID, openid);
//        SharedPreferencesUtil.putStringExtra(context, Constans.SP_KEY_OPENTOKEN, opentoken);
//        SharedPreferencesUtil.putIntExtra(context, Constans.SP_KEY_SOCIALTYPE, socialType);
        SharedPreferences preferences = context.getSharedPreferences(
                Constans.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constans.SP_KEY_OPENID, openid);
        editor.putString(Constans.SP_KEY_OPENTOKEN, opentoken);
        editor.putInt(Constans.SP_KEY_SOCIALTYPE, socialType);
        editor.commit();
    }

    /**
     * 保存用户的密码，重置密码的时候用
     *
     * @param context
     * @param pwd
     */
    public static void savePWD(Context context, String pwd) {

        SharedPreferences preferences = context.getSharedPreferences(
                Constans.SP_NAME, Context.MODE_PRIVATE);
        int socialtype = preferences.getInt(Constans.SP_KEY_SOCIALTYPE, -1);
        if (socialtype == WelcomeActivity.SOCIAL_TYPE_PHONE) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constans.SP_KEY_OPENTOKEN, pwd);
            editor.commit();
        }
    }

    public static String getNoteByUid(int uid, Context context) {
        if (context == null) return "";
        PeiwoApp app = (PeiwoApp) context.getApplicationContext();
        return app.getNoteByUid(uid);
    }

    public static String getRealName(int uid, String name, Context context) {
        if (context == null) return "";
        PeiwoApp app = (PeiwoApp) context.getApplicationContext();
        String temp = app.getNoteByUid(uid);
        return TextUtils.isEmpty(temp) ? name : temp;
    }

    /**
     * 修改用户的flags字段
     *
     * @param context
     * @param flags
     */
    public static void updateFlags(Context context, String flags) {
        SQLiteDatabase db = null;
        try {
            db = PWDBHelper.getInstance(context).getWritableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "update %s set flags = ?";
            Object[] objects = new Object[]{flags};
            db.execSQL(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), objects);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * 取用户的flags
     *
     * @param context
     * @return
     */
    public static int getPWUserFlags(Context context) {
        int flags = 0;
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = PWDBHelper.getInstance(context).getReadableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "select flags from %s";
            c = db.rawQuery(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), null);
            if (c.moveToFirst()) {
                flags = c.getInt(c.getColumnIndex(KEY_FLAGS));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return flags;
    }

    /**
     * 修改用户的avatar_thumbnail字段
     *
     * @param context
     * @param avatar_thumbnail
     */
    public static void updateAvatar_thumbnail(Context context, String avatar_thumbnail) {
        SQLiteDatabase db = null;
        try {
            db = PWDBHelper.getInstance(context).getWritableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "update %s set avatar_thumbnail = ?";
            Object[] objects = new Object[]{avatar_thumbnail};
            db.execSQL(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), objects);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public static void clearEmotion(Context context) {
        SQLiteDatabase db = null;
        try {
            db = PWDBHelper.getInstance(context).getWritableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "update %s set emotion = ?";
            Object[] objects = new Object[]{"0"};
            db.execSQL(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), objects);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public static void clearProfession(Context context) {
        SQLiteDatabase db = null;
        try {
            db = PWDBHelper.getInstance(context).getWritableDatabase(PWDBConfig.DB_NAME_USER);
            String sql = "update %s set profession = ?";
            Object[] objects = new Object[]{""};
            db.execSQL(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER), objects);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public static Intent buildMyHEPAIIntent(Context context) {
        PeiwoApp app = (PeiwoApp) context.getApplicationContext();
        StringBuilder url = new StringBuilder(app.isOnLineEnv() ? Constans.RELEASE_LOVE_URL : Constans.DEBUG_LOVE_URL);
        String session = getSessionData(context);
        if (!TextUtils.isEmpty(session) && session.length() > 10) {
            session = session.substring(0, 10);
        }
        String uid = String.valueOf(getUid(context));
        url.append("myConstellation.html?uid=").append(uid)
                .append("&tuid=").append(uid).append("&session=")
                .append(session);
        Intent intent = new Intent(context, RedBagActivity.class);
        intent.putExtra("url", url.toString());
        return intent;
    }
}
