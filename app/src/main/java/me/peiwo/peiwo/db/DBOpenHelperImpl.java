package me.peiwo.peiwo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import me.peiwo.peiwo.constans.PWDBConfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by fuhaidong on 15/11/30.
 */
public class DBOpenHelperImpl extends SQLiteOpenHelper {
    private Context context;

    public DBOpenHelperImpl(Context context, String name,
                            SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PWDBConfig.TB_NAME_PW_CONTACTS + " ("
                + PWDBConfig.ContactsTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PWDBConfig.ContactsTable.UID + " INTEGER UNIQUE, "
                + PWDBConfig.ContactsTable.SYNC_ID + " INTEGER, "
                + PWDBConfig.ContactsTable.SIGNIN_TIME + " TEXT, "
                + PWDBConfig.ContactsTable.CONTACT_ID + " INTEGER, "
                + PWDBConfig.ContactsTable.BIRTHDAY + " TEXT, "
                + PWDBConfig.ContactsTable.AVATAR_THUMBNAIL + " TEXT, "
                + PWDBConfig.ContactsTable.SLOGAN + " TEXT, "
                + PWDBConfig.ContactsTable.PRICE + " TEXT, "
                + PWDBConfig.ContactsTable.NAME + " TEXT, "
                + PWDBConfig.ContactsTable.PROVINCE + " TEXT, "
                + PWDBConfig.ContactsTable.GENDER + " INTEGER, "
                + PWDBConfig.ContactsTable.AVATAR + " TEXT, "
                + PWDBConfig.ContactsTable.CITY + " TEXT, "
                + PWDBConfig.ContactsTable.CONTACT_STATE + " INTEGER, "
                + PWDBConfig.ContactsTable.CALL_DURATION + " TEXT, "
                + PWDBConfig.ContactsTable.WORD + " TEXT" + ");");

        // 申请通话的人的表
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PWDBConfig.TB_NAME_PW_REQUESTS + " ("
                + PWDBConfig.RequestsTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PWDBConfig.RequestsTable.REQUEST_ID + " INTEGER UNIQUE, "
                + PWDBConfig.RequestsTable.SYNC_ID + " INTEGER, "
                + PWDBConfig.RequestsTable.STATE + " INTEGER, "
                + PWDBConfig.RequestsTable.CONTENT + " TEXT, "
                + PWDBConfig.RequestsTable.UID + " INTEGER, "
                + PWDBConfig.RequestsTable.BIRTHDAY + " TEXT, "
                + PWDBConfig.RequestsTable.AVATAR_THUMBNAIL + " TEXT, "
                + PWDBConfig.RequestsTable.SLOGAN + " TEXT, "
                + PWDBConfig.RequestsTable.PRICE + " TEXT, "
                + PWDBConfig.RequestsTable.NAME + " TEXT, "
                + PWDBConfig.RequestsTable.PROVINCE + " TEXT, "
                + PWDBConfig.RequestsTable.GENDER + " INTEGER, "
                + PWDBConfig.RequestsTable.AVATAR + " TEXT, "
                + PWDBConfig.RequestsTable.CITY + " TEXT, "
                + PWDBConfig.RequestsTable.UPDATE_TIME + " TEXT" + ");");

        // 存取dialog的表
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PWDBConfig.TB_NAME_PW_MESSAGES + " ("
                + PWDBConfig.MessagesTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PWDBConfig.MessagesTable.UID + " INTEGER UNIQUE, "    //用户ID
                + PWDBConfig.MessagesTable.MSG_ID + " INTEGER, "        //MSG_ID
                + PWDBConfig.MessagesTable.USER + " TEXT, "                //USER信息
                + PWDBConfig.MessagesTable.TYPE + " INTEGER, "            //未接听还是已经接听
                + PWDBConfig.MessagesTable.MSG_TYPE + " INTEGER, "        //系统消息还是申请通话还是
                + PWDBConfig.MessagesTable.UPDATE_TIME + " TEXT, "        //更新时间
                + PWDBConfig.MessagesTable.INPUT_STATUS + " INTEGER, "    //无
                + PWDBConfig.MessagesTable.DRAFT_MSG + " TEXT, "        //无
                + PWDBConfig.MessagesTable.MESSGAE_COUNT + " INTEGER, "    //???
                + PWDBConfig.MessagesTable.UNREAD_COUNT + " INTEGER, "    //未读次数
                + PWDBConfig.MessagesTable.CONTENT + " TEXT, "
                + PWDBConfig.MessagesTable.INSIDE + " INTEGER, "
                + PWDBConfig.MessagesTable.INSIDE_NEW + " INTEGER DEFAULT 0, "
                + PWDBConfig.MessagesTable.IS_HIDE + " INTEGER DEFAULT 0"
                + ");");    //信息发送成功，系统已扣除您账户余额0.1元；单条信息扣费将不再显示

        createDialogs(db);

        createMsgIdUid(db);

        //Log.i("createdb", "oncreate createTableWildLogReportMap");
        createTableWildLogReportMap(db);
        createTablePWContacts(db);
        createTableAtUserMapping(db);
        createTableGroupChatNoDisturb(db);

        removeOldDataBase();
    }

    private void createTableGroupChatNoDisturb(SQLiteDatabase db) {
        //nodisturb == 0免打扰关闭nodisturb==1免打扰开启
        String sql = "CREATE TABLE IF NOT EXISTS %s (target_id varchar(20) primary key, nodisturb integer)";
        if (db != null) {
            db.execSQL(String.format(sql, PWDBConfig.TB_PW_NO_DISTURB));
        }
    }

    private void createTableAtUserMapping(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS %s (target_id varchar(20) primary key, extra varchar(20))";
        if (db != null) {
            db.execSQL(String.format(sql, PWDBConfig.TB_PW_AT_USER));
        }
    }

    private void createMsgIdUid(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PWDBConfig.TB_NAME_UID_MSGID + " ("
                + PWDBConfig.UidMsgId.UID + " TEXT,"
                + PWDBConfig.UidMsgId.MSG_ID + " TEXT" + ");");
    }

    private void createDialogs(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PWDBConfig.TB_NAME_PW_DIALOGS + " ("
                + PWDBConfig.DialogsTable.ID + " INTEGER PRIMARY KEY,"// AUTOINCREMENT
                + PWDBConfig.DialogsTable.DIALOG_ID + " TEXT UNIQUE, "    //每条短信的DIALOG_ID
                + PWDBConfig.DialogsTable.CONTENT + " TEXT, "                //
                + PWDBConfig.DialogsTable.UPDATE_TIME + " TEXT, "            //更新时间
                + PWDBConfig.DialogsTable.MSG_ID + " TEXT, "                //MSG_ID
                + PWDBConfig.DialogsTable.UID + " TEXT, "                //UID
                + PWDBConfig.DialogsTable.TYPE + " INTEGER, "                //自己发送还是对方发送
                + PWDBConfig.DialogsTable.DIALOG_TYPE + " INTEGER, "        //
                + PWDBConfig.DialogsTable.DETAILS + " TEXT, "                //发送的消息
                + PWDBConfig.DialogsTable.SEND_STATUS + " INTEGER, "        //是否发送成功
                + PWDBConfig.DialogsTable.READ_STATUS + " INTEGER, "        //
                + PWDBConfig.DialogsTable.FILE_LENGTH + " INTEGER, "        //无
                + PWDBConfig.DialogsTable.FILE_PATH + " TEXT, "                //无
                + PWDBConfig.DialogsTable.ERROR_CODE + " INTEGER, "            //
                + PWDBConfig.DialogsTable.DATA1 + " TEXT, "
                + PWDBConfig.DialogsTable.DATA2 + " TEXT, "
                + PWDBConfig.DialogsTable.DATA3 + " TEXT, "
                + PWDBConfig.DialogsTable.DATA4 + " TEXT" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                handleGarbageData();
            case 2:
                db.execSQL("ALTER TABLE " + PWDBConfig.TB_NAME_PW_MESSAGES + " ADD " + PWDBConfig.MessagesTable.INSIDE + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + PWDBConfig.TB_NAME_PW_CONTACTS + " ADD " + PWDBConfig.ContactsTable.CALL_DURATION + " TEXT");
            case 3:
                db.execSQL("ALTER TABLE " + PWDBConfig.TB_NAME_PW_MESSAGES + " ADD " + PWDBConfig.MessagesTable.INSIDE_NEW + " INTEGER DEFAULT 0");
            case 4:
                db.execSQL("ALTER TABLE " + PWDBConfig.TB_NAME_PW_MESSAGES + " ADD " + PWDBConfig.MessagesTable.IS_HIDE + " INTEGER DEFAULT 0");
                break;
            case 6:
                break;
        }
        //Log.i("createdb", "onUpgrade createTableWildLogReportMap");
        /******/
        if (!tableExists(db, PWDBConfig.TB_NAME_PW_DIALOGS + "old")) {
            db.execSQL("ALTER TABLE " + PWDBConfig.TB_NAME_PW_DIALOGS + " RENAME TO " + PWDBConfig.TB_NAME_PW_DIALOGS + "old");
            db.execSQL("ALTER TABLE " + PWDBConfig.TB_NAME_UID_MSGID + " RENAME TO " + PWDBConfig.TB_NAME_UID_MSGID + "old");
            createDialogs(db);
            createMsgIdUid(db);
            db.execSQL("INSERT INTO " + PWDBConfig.TB_NAME_PW_DIALOGS + " (content, dialog_id, update_time, msg_id, uid, type, dialog_type, details, send_status, read_status, file_length, file_path, error_code, data1, data2, data3, data4) SELECT content, dialog_id, update_time, msg_id, uid, type, dialog_type, details, send_status, read_status, file_length, file_path, error_code, data1, data2, data3, data4 FROM " + PWDBConfig.TB_NAME_PW_DIALOGS + "old");
            db.execSQL("INSERT INTO " + PWDBConfig.TB_NAME_UID_MSGID + " (uid, msg_id) SELECT uid, msg_id FROM " + PWDBConfig.TB_NAME_UID_MSGID + "old");
        }
        createTableAtUserMapping(db);
        createTableGroupChatNoDisturb(db);
        /******/

        createTableWildLogReportMap(db);
        createTablePWContacts(db);
    }

    private boolean tableExists(SQLiteDatabase db, String tableName) {
        String sql = "SELECT count(*) FROM sqlite_master WHERE type=? AND name=?";
        Cursor c = db.rawQuery(sql, new String[]{"table", tableName});
        int count = 0;
        if (c != null) {
            if (c.moveToFirst()) {
                count = c.getInt(0);
            }
            c.close();
        }
        if (count > 0) {
            //Log.i("rongs", "table exists");
            return true;
        }
        //Log.i("rongs", "table not exists");
        return false;
    }

    private void createTablePWContacts(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS %s (uid varchar(20) primary key, sync_id integer, avatar varchar(20), avatar_thumbnail varchar(20), birthday varchar(20), price varchar(20), gender integer, name varchar(20), slogan varchar(20), city varchar(20), province varchar(20), contact_id varchar(20), signin_time varchar(20), contact_state integer, call_duration integer)";
        if (db != null) {
            db.execSQL(String.format(sql, PWDBConfig.TB_PW_CONTACTS));
        }
    }

    private void createTableWildLogReportMap(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS %s (_id integer primary key autoincrement, uid varchar(20), report integer)";
        if (db != null) {
            db.execSQL(String.format(sql, PWDBConfig.TB_PW_WILDRLOG_REPORT_MAP));
        }
    }


    private final Object object1 = new Object();
    private int oldDbHashCode = 0;

    public int getOldDbHashCode() {
        return oldDbHashCode;
    }

    private void removeOldDataBase() {
        new Thread(() -> {
            synchronized (object1) {
                File msgDbFile = getContext().getDatabasePath(PWDBUtil.getCurrentMsgCenterOldDBName(getContext()));
                if (msgDbFile != null && msgDbFile.exists()) {
                    SQLiteDatabase oldDb = SQLiteDatabase.openDatabase(msgDbFile.getAbsolutePath(),
                            null, SQLiteDatabase.OPEN_READWRITE);
                    if (oldDb != null) {
                        HashMap<Integer, Integer> uidToMsgid = new HashMap<Integer, Integer>();
                        String sql = "select * from %s order by update_time desc";
                        sql = String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_PW_MESSAGES);
                        Cursor c = oldDb.rawQuery(sql, null);
                        if (c != null) {
                            int count = c.getCount();
                            if (count > 0) {
                                int index = 0;
                                ContentValues[] valuesArray = new ContentValues[count];
                                while (c.moveToNext()) {
                                    valuesArray[index] = new ContentValues();
                                    String user = c.getString(c.getColumnIndex("user"));
                                    int msg_id = c.getInt(c.getColumnIndex("msg_id"));
                                    int max_dialog_id = c.getInt(c.getColumnIndex("max_dialog_id"));
                                    if (max_dialog_id == 0) {
                                        continue;
                                    }
                                    valuesArray[index].put(PWDBConfig.MessagesTable.MSG_ID, msg_id);
                                    valuesArray[index].put(PWDBConfig.MessagesTable.USER, user);
                                    valuesArray[index].put(PWDBConfig.MessagesTable.MSG_TYPE, c.getInt(c.getColumnIndex("msg_type")));
                                    valuesArray[index].put(PWDBConfig.MessagesTable.UPDATE_TIME, c.getString(c.getColumnIndex("update_time")));
                                    valuesArray[index].put(PWDBConfig.MessagesTable.CONTENT, c.getString(c.getColumnIndex("content")));
                                    try {
                                        JSONObject userObject = new JSONObject(user);
                                        int uid = userObject.optInt("uid");
                                        valuesArray[index].put(PWDBConfig.MessagesTable.UID, uid);
                                        uidToMsgid.put(msg_id, uid);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    index++;
                                }
                                c.close();
                                getContext().getContentResolver().bulkInsert(PWDBConfig.MessagesTable.CONTENT_URI, valuesArray);
                            }
                        }

                        sql = "select * from %s order by update_time asc";
                        sql = String.format(Locale.getDefault(), sql, PWDBConfig.TB_NAME_PW_DIALOGS);
                        c = oldDb.rawQuery(sql, null);
                        if (c != null) {
                            int count = c.getCount();
                            if (count > 0) {
                                int index = 0;
                                ContentValues[] valuesArray = new ContentValues[count];
                                while (c.moveToNext()) {
                                    valuesArray[index] = new ContentValues();
                                    int msg_id = -1;
                                    try {
                                        msg_id = Integer.valueOf(c.getString(c.getColumnIndex("msg_id")));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    if (!uidToMsgid.containsKey(msg_id)) {
                                        continue;
                                    }
                                    int uid = uidToMsgid.get(msg_id);
                                    int oldUid = c.getInt(c.getColumnIndex("uid"));
                                    valuesArray[index].put(PWDBConfig.DialogsTable.DIALOG_ID, c.getInt(c.getColumnIndex("dialog_id")));
                                    valuesArray[index].put(PWDBConfig.DialogsTable.CONTENT, c.getString(c.getColumnIndex("content")));
                                    valuesArray[index].put(PWDBConfig.DialogsTable.UPDATE_TIME, c.getString(c.getColumnIndex("update_time")));
                                    valuesArray[index].put(PWDBConfig.DialogsTable.MSG_ID, msg_id);
                                    valuesArray[index].put(PWDBConfig.DialogsTable.UID, uid);
                                    if (oldUid == uid) {
                                        valuesArray[index].put(PWDBConfig.DialogsTable.TYPE, 1);
                                    } else {
                                        valuesArray[index].put(PWDBConfig.DialogsTable.TYPE, 0);
                                    }
                                    valuesArray[index].put(PWDBConfig.DialogsTable.DIALOG_TYPE, c.getInt(c.getColumnIndex("dialog_type")));
                                    valuesArray[index].put(PWDBConfig.DialogsTable.DETAILS, c.getString(c.getColumnIndex("details")));
                                    index++;
                                }
                                c.close();
                                oldDbHashCode = valuesArray.hashCode();
                                getContext().getContentResolver().bulkInsert(PWDBConfig.DialogsTable.CONTENT_URI, valuesArray);
                            }
                        }
                        oldDb.execSQL("DROP TABLE IF EXISTS " + PWDBConfig.TB_NAME_PW_MESSAGES);
                        oldDb.execSQL("DROP TABLE IF EXISTS " + PWDBConfig.TB_NAME_PW_DIALOGS);
                        oldDb.close();
                    }
                }
            }
        }).start();
    }

    public void handleGarbageData() {
        new Thread(() -> {
            Cursor mCursor = getContext().getContentResolver().query(PWDBConfig.MessagesTable.CONTENT_URI, null, null, null, null);
            if (mCursor != null) {
                if (mCursor.getCount() > 0) {
                    while (mCursor.moveToNext()) {
                        boolean noUid = mCursor.isNull(mCursor.getColumnIndex(PWDBConfig.MessagesTable.UID));
                        int uid = mCursor.getInt(mCursor.getColumnIndex(PWDBConfig.MessagesTable.UID));
                        if (noUid || uid == 0) {
                            String user = mCursor.getString(mCursor.getColumnIndex(PWDBConfig.MessagesTable.USER));
                            int id = mCursor.getInt(mCursor.getColumnIndex(PWDBConfig.MessagesTable.ID));
                            try {
                                JSONObject userObject = new JSONObject(user);
                                uid = userObject.optInt("uid");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (uid > 0) {
                                ContentValues values = new ContentValues();
                                values.put(PWDBConfig.MessagesTable.UID, uid);
                                String where = PWDBConfig.MessagesTable.ID + " = ?";
                                String[] selectionArgs = new String[]{String.valueOf(id)};
                                int count = 0;
                                try {
                                    count = getContext().getContentResolver().update(PWDBConfig.MessagesTable.CONTENT_URI, values, where, selectionArgs);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (count == 0) {
                                    getContext().getContentResolver().delete(PWDBConfig.MessagesTable.CONTENT_URI, where, selectionArgs);
                                }
                            }
                        }
                    }
                }
                mCursor.close();
                mCursor = null;
            }
        }).start();
    }
}
