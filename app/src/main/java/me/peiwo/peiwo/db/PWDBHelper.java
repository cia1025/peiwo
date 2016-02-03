package me.peiwo.peiwo.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import me.peiwo.peiwo.constans.PWDBConfig;

import java.util.Locale;

/**
 * Created by Dong Fuhai on 2014-07-24 17:03.
 *
 * @modify:
 */
public class PWDBHelper {
    private Context mContxt;
    //ver == 2添加个人资料的phone字段 ver == 3 升级dialogue消息,添加type 与 detail 字段
    // ver == 4 添加个人资料的flags字段
    private static final int DATABASE_VERSION = 7;

    private static PWDBHelper dbOpenHelper;

    private PWDBHelper(Context context) {
        mContxt = context;
    }

    public static synchronized PWDBHelper getInstance(Context context) {
        if (null == dbOpenHelper) {
            dbOpenHelper = new PWDBHelper(context);
        }
        return dbOpenHelper;
    }

    public SQLiteDatabase getWritableDatabase(String dbFName) throws Exception {
        for (int i = 10; i > 0; i--) {
            try {
                return getDatabase(dbFName).getWritableDatabase();
            } catch (Exception e) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e1) {
                }
                e.printStackTrace();
            }
        }
        throw new Exception();
    }

    /**
     * @param
     * @return
     * @throws Exception
     */
    public SQLiteDatabase getReadableDatabase(String dbFName) throws Exception {
        for (int i = 10; i > 0; i--) {
            try {
                if (getDatabase(dbFName) != null) {
                    return getDatabase(dbFName).getReadableDatabase();
                }
            } catch (Exception e) {

                e.printStackTrace();
            } finally {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e1) {
                }
            }
        }
        throw new Exception();
    }

    private PWSqlOpenHelper getDatabase(String dbFName) {
        PWSqlOpenHelper pwSqlOpenHelper;
        if (dbFName != null) {
            if (dbFName.equals(PWDBConfig.DB_NAME_USER)) {
                pwSqlOpenHelper = createMUserDB(dbFName);
            } else {
                pwSqlOpenHelper = createReMarkDB(PWDBUtil.getCurrentReMarkDBName(mContxt));
            }
            return pwSqlOpenHelper;
        }
        return null;
    }

    /**
     * 本地联系人备注数据库
     *
     * @param currentReMarkDBName
     * @return
     */
    private PWSqlOpenHelper createReMarkDB(String currentReMarkDBName) {
        return new PWSqlOpenHelper(mContxt, currentReMarkDBName, null, DATABASE_VERSION) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                String sql = "CREATE TABLE %s (uid integer primary key, remark varchar(20))";
                db.execSQL(String.format(sql, PWDBConfig.TB_NAME_PW_REMARK));
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            }
        };
    }

    private PWSqlOpenHelper createMUserDB(String dbFName) {
        return new PWSqlOpenHelper(mContxt, dbFName, null, DATABASE_VERSION) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                String sql = "CREATE TABLE %s (_id integer primary key autoincrement, uid integer, tags varchar(20), birthday varchar(20), " +
                        "avatar_thumbnail varchar(20), profession varchar(20), state integer, avatar varchar(20), city varchar(20), " +
                        "emotion integer, slogan varchar(20), price varchar(20), money varchar(20), name varchar(20), province varchar(20), " +
                        "session_data varchar(20), gender varchar(20), call_duration varchar(20), images varchar(20), phone varchar(20), flags integer, " +
                        "food_tags varchar(20), music_tags varchar(20), movie_tags varchar(20), book_tags varchar(20), travel_tags varchar(20), sports_tags varchar(20), game_tags varchar(20), reward_price varchar(20), score varchar(20))";
                db.execSQL(String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_USER));
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                super.onUpgrade(db, oldVersion, newVersion);
                //升级个人资料数据库
                if (!isColumnNameExist(db, PWDBConfig.TB_NAME_USER, "phone")) {
                    String sql = String.format(Locale.getDefault(), "ALTER TABLE %s ADD phone VARCHAR", PWDBConfig.TB_NAME_USER);
                    db.execSQL(sql);
                }
                if (!isColumnNameExist(db, PWDBConfig.TB_NAME_USER, "flags")) {
                    String sql = String.format(Locale.getDefault(), "ALTER TABLE %s ADD flags integer", PWDBConfig.TB_NAME_USER);
                    db.execSQL(sql);
                }
                if (!isColumnNameExist(db, PWDBConfig.TB_NAME_USER, "food_tags")) {
                    String sql = String.format(Locale.getDefault(), "ALTER TABLE %s ADD food_tags VARCHAR", PWDBConfig.TB_NAME_USER);
                    db.execSQL(sql);
                }
                if (!isColumnNameExist(db, PWDBConfig.TB_NAME_USER, "music_tags")) {
                    String sql = String.format(Locale.getDefault(), "ALTER TABLE %s ADD music_tags VARCHAR", PWDBConfig.TB_NAME_USER);
                    db.execSQL(sql);
                }
                if (!isColumnNameExist(db, PWDBConfig.TB_NAME_USER, "movie_tags")) {
                    String sql = String.format(Locale.getDefault(), "ALTER TABLE %s ADD movie_tags VARCHAR", PWDBConfig.TB_NAME_USER);
                    db.execSQL(sql);
                }
                if (!isColumnNameExist(db, PWDBConfig.TB_NAME_USER, "book_tags")) {
                    String sql = String.format(Locale.getDefault(), "ALTER TABLE %s ADD book_tags VARCHAR", PWDBConfig.TB_NAME_USER);
                    db.execSQL(sql);
                }
                if (!isColumnNameExist(db, PWDBConfig.TB_NAME_USER, "travel_tags")) {
                    String sql = String.format(Locale.getDefault(), "ALTER TABLE %s ADD travel_tags VARCHAR", PWDBConfig.TB_NAME_USER);
                    db.execSQL(sql);
                }
                if (!isColumnNameExist(db, PWDBConfig.TB_NAME_USER, "sports_tags")) {
                    String sql = String.format(Locale.getDefault(), "ALTER TABLE %s ADD sports_tags VARCHAR", PWDBConfig.TB_NAME_USER);
                    db.execSQL(sql);
                }
                if (!isColumnNameExist(db, PWDBConfig.TB_NAME_USER, "game_tags")) {
                    String sql = String.format(Locale.getDefault(), "ALTER TABLE %s ADD game_tags VARCHAR", PWDBConfig.TB_NAME_USER);
                    db.execSQL(sql);
                }
                if (!isColumnNameExist(db, PWDBConfig.TB_NAME_USER, "reward_price")) {
                    String sql = String.format(Locale.getDefault(), "ALTER TABLE %s ADD reward_price VARCHAR", PWDBConfig.TB_NAME_USER);
                    db.execSQL(sql);
                }
                if (!isColumnNameExist(db, PWDBConfig.TB_NAME_USER, "score")) {
                    String sql = String.format(Locale.getDefault(), "ALTER TABLE %s ADD score VARCHAR", PWDBConfig.TB_NAME_USER);
                    db.execSQL(sql);
                }
            }
        };
    }


    private class PWSqlOpenHelper extends SQLiteOpenHelper {
        public PWSqlOpenHelper(Context context, String name,
                               SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }


    boolean isColumnNameExist(SQLiteDatabase db, String table, String columnName) {
        //PRAGMA table_info([usertable])
//        Cursor c = null;
//        try {
//            //String _sql = "PRAGMA table_info([" + table + "])";
//            String sql = String.format("PRAGMA table_info ('%s')", table);
//            Trace.i("sql == " + sql);
//            c = db.rawQuery(sql, null);
//            if (c.getColumnIndex(columnName) != -1) {
//                return true;
//            } else {
//                return false;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            //Log.i("tag", "错误=="+e.getMessage());
//            return false;
//        } finally {
//            if (c != null)
//                c.close();
//        }


        boolean result = false;
        if (table == null) {
            return false;
        }

        Cursor cursor = null;
        try {
            String sql = "select count(1) as c from sqlite_master where type ='table' and name ='" + table.trim() + "' and sql like '%"
                    + columnName.trim() + "%'";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return result;
    }


}
