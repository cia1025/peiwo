package me.peiwo.peiwo.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.activity.MsgAcceptedMsgActivity;
import me.peiwo.peiwo.activity.SayHelloActivity;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.im.MessageUtil;
import org.apache.http.util.TextUtils;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Kevin
 * 13670164867
 *
 * @author Administrator
 */
public class PWDbContentProvider extends ContentProvider {
    public static final int DB_VERSION = 7;

    private DBOpenHelperImpl mDBOpenHelper;// 数据库维护类(表创建与维护)
    private SQLiteDatabase mDb; // 数据库操作类(表数据的增,删,改,查)
    private UriMatcher sMatcher;

    @Override
    public boolean onCreate() {
        sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sMatcher.addURI(PWDBConfig.AUTOHORITY, PWDBConfig.TB_NAME_PW_DIALOGS, PWDBConfig.TB_PW_DIALOGS_URI_CODE);
        sMatcher.addURI(PWDBConfig.AUTOHORITY, PWDBConfig.TB_NAME_PW_MESSAGES, PWDBConfig.TB_PW_MESSAGE_URI_CODE);
        sMatcher.addURI(PWDBConfig.AUTOHORITY, PWDBConfig.TB_NAME_PW_CONTACTS, PWDBConfig.TB_PW_CONTACTS_URI_CODE);
        sMatcher.addURI(PWDBConfig.AUTOHORITY, PWDBConfig.TB_NAME_PW_REQUESTS, PWDBConfig.TB_PW_REQUESTS_URI_CODE);
        sMatcher.addURI(PWDBConfig.AUTOHORITY, PWDBConfig.TB_NAME_UID_MSGID, PWDBConfig.TB_PW_UID_MSGID_URI_CODE);
        return true;
    }

    private String dbFName = "";

    private boolean isNeedReset(String currentDbName) {
        return !(!TextUtils.isEmpty(dbFName) && dbFName.equals(currentDbName));
    }

    private synchronized void initSQLite() {
        String dbName = PWDBUtil.getCurrentMsgCenterDBName(getContext());
        if (isNeedReset(dbName)) {
            mDBOpenHelper = new DBOpenHelperImpl(getContext(), dbName, null,
                    DB_VERSION);
            mDb = mDBOpenHelper.getWritableDatabase();
            dbFName = dbName;
        }
    }


    @Override
    public String getType(Uri uri) {
        sMatcher.match(uri);
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        initSQLite();
        long rowId;
        switch (sMatcher.match(uri)) {
            case PWDBConfig.TB_PW_DIALOGS_URI_CODE: {
                rowId = mDb.insert(PWDBConfig.TB_NAME_PW_DIALOGS, null, values);
                if (rowId > 0) {
                    Uri noteUri = ContentUris.withAppendedId(PWDBConfig.DialogsTable.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(noteUri, null);
                    return noteUri;
                }
            }
            break;
            case PWDBConfig.TB_PW_MESSAGE_URI_CODE: {
                rowId = mDb.insert(PWDBConfig.TB_NAME_PW_MESSAGES, null, values);
                if (rowId > 0) {
                    Uri noteUri = ContentUris.withAppendedId(PWDBConfig.MessagesTable.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(noteUri, null);
                    return noteUri;
                }
            }
            break;
            case PWDBConfig.TB_PW_CONTACTS_URI_CODE: {
                rowId = mDb.insert(PWDBConfig.TB_NAME_PW_CONTACTS, null, values);
                if (rowId > 0) {
                    Uri noteUri = ContentUris.withAppendedId(PWDBConfig.ContactsTable.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(noteUri, null);
                    return noteUri;
                }
            }
            break;
            case PWDBConfig.TB_PW_REQUESTS_URI_CODE: {
                rowId = mDb.insert(PWDBConfig.TB_NAME_PW_REQUESTS, null, values);
                if (rowId > 0) {
                    Uri noteUri = ContentUris.withAppendedId(PWDBConfig.RequestsTable.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(noteUri, null);
                    return noteUri;
                }
            }
            break;
            case PWDBConfig.TB_PW_UID_MSGID_URI_CODE: {
                rowId = mDb.insert(PWDBConfig.TB_NAME_UID_MSGID, null, values);
                if (rowId > 0) {
                    Uri noteUri = ContentUris.withAppendedId(PWDBConfig.RequestsTable.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(noteUri, null);
                    return noteUri;
                }
            }
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
        throw new IllegalArgumentException("Unknown URI" + uri);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (values == null || values.length == 0)
            return 0;
        initSQLite();
        int rowId = 0;
        int meMsgCount = 0;
        mDb.beginTransaction();
        int count = values.length;
        ArrayList<Integer> faileArray = null;
        ContentValues lastValue = null;
        try {
            switch (sMatcher.match(uri)) {
                case PWDBConfig.TB_PW_DIALOGS_URI_CODE: {
                    for (int i = 0; i < count; i++) {
                        if (mDb.insert(PWDBConfig.TB_NAME_PW_DIALOGS, null, values[i]) >= 0) {
                            if (values[i].containsKey(PWDBConfig.DialogsTable.TYPE)
                                    && values[i].getAsInteger(PWDBConfig.DialogsTable.TYPE) == 0) {
                                meMsgCount++;
                            }
                            rowId++;
                            lastValue = values[i];
                        } else {
                            if (faileArray == null) {
                                faileArray = new ArrayList<Integer>();
                            }
                            if (values[i] != null && values[i].containsKey(PWDBConfig.DialogsTable.DIALOG_ID)) {
                                faileArray.add(values[i].getAsInteger(PWDBConfig.DialogsTable.DIALOG_ID));
                            }
                        }
                    }
                    mDb.setTransactionSuccessful();
                }
                break;
                case PWDBConfig.TB_PW_MESSAGE_URI_CODE: {
                    for (int i = 0; i < count; i++) {
                        if (mDb.insert(PWDBConfig.TB_NAME_PW_MESSAGES, null, values[i]) >= 0) {
                            rowId++;
                        }
                    }
                    mDb.setTransactionSuccessful();
                }
                break;
                case PWDBConfig.TB_PW_CONTACTS_URI_CODE: {
                    for (int i = 0; i < count; i++) {
                        if (mDb.insert(PWDBConfig.TB_NAME_PW_CONTACTS, null, values[i]) >= 0) {
                            rowId++;
                        }
                    }
                    mDb.setTransactionSuccessful();
                }
                break;
                case PWDBConfig.TB_PW_REQUESTS_URI_CODE: {
                    for (int i = 0; i < count; i++) {
                        if (mDb.insert(PWDBConfig.TB_NAME_PW_REQUESTS, null, values[i]) >= 0) {
                            rowId++;
                        }
                    }
                    mDb.setTransactionSuccessful();
                }
                break;
                case PWDBConfig.TB_PW_UID_MSGID_URI_CODE: {
                    for (int i = 0; i < count; i++) {
                        if (mDb.insert(PWDBConfig.TB_NAME_UID_MSGID, null, values[i]) >= 0) {
                            rowId++;
                        }
                    }
                    mDb.setTransactionSuccessful();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDb.endTransaction();
            getContext().getContentResolver().notifyChange(uri, null);
            if (sMatcher.match(uri) == PWDBConfig.TB_PW_DIALOGS_URI_CODE
                    && mDBOpenHelper.getOldDbHashCode() != values.hashCode()) {
                updateMessageFromDialog(lastValue, rowId, meMsgCount);
                if (faileArray != null) {
                    for (int i = 0; i < faileArray.size(); i++) {
                        for (int j = 0; j < MsgDBCenterService.getInstance().msgNotifiList.size(); j++) {
                            if (faileArray.get(i) == MsgDBCenterService.getInstance().msgNotifiList.get(j).dialog_id) {
                                MsgDBCenterService.getInstance().msgNotifiList.remove(j);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return rowId;
    }

    private final Object object = new Object();

    private void updateMessageFromDialog(final ContentValues value, final int insertCount, final int meMsgCount) {
        new Thread(() -> {
            synchronized (object) {
                if (value == null || insertCount == 0)
                    return;
                try {
                    String update_time = value.getAsString(PWDBConfig.DialogsTable.UPDATE_TIME);
                    String details = value.getAsString(PWDBConfig.DialogsTable.DETAILS);
                    String content = value.getAsString(PWDBConfig.DialogsTable.CONTENT);
                    int type = value.getAsInteger(PWDBConfig.DialogsTable.TYPE);
                    String uid = value.getAsString(PWDBConfig.DialogsTable.UID);
                    int dialogType = value.getAsInteger(PWDBConfig.DialogsTable.DIALOG_TYPE);
                    String name = "";
                    if (!TextUtils.isEmpty(details) && details.length() > 2) {
                        try {
                            JSONObject detailObject = new JSONObject(details);
                            content = detailObject.optString("msg");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (dialogType == 11) {
                        content = "[图片]";
                    }
                    String selection = PWDBConfig.MessagesTable.UID + " = ?";
                    String[] selectionArgs = new String[]{String.valueOf(uid)};

                    ContentValues msgValue = new ContentValues();


                    ContentValues sayHelloValue = null;

                    String last_update_time = "";
                    Cursor mCursor = query(PWDBConfig.MessagesTable.CONTENT_URI, null, selection, selectionArgs, null);
                    if (mCursor != null && mCursor.moveToFirst()) {
                        int unread_count = mCursor.getInt(mCursor.getColumnIndex(PWDBConfig.MessagesTable.UNREAD_COUNT));
                        last_update_time = mCursor.getString(mCursor.getColumnIndex(PWDBConfig.MessagesTable.UPDATE_TIME));
                        int isNew = mCursor.getInt(mCursor.getColumnIndex(PWDBConfig.MessagesTable.INSIDE_NEW));
                        int inside = mCursor.getInt(mCursor.getColumnIndex(PWDBConfig.MessagesTable.INSIDE));
                        String user = mCursor.getString(mCursor.getColumnIndex(PWDBConfig.MessagesTable.USER));
                        name = new JSONObject(user).optString("name");
                        if (inside == 1) {//盒子里面
                            sayHelloValue = new ContentValues();
                        }
                        if (!String.valueOf(MsgAcceptedMsgActivity.Uid).equals(uid)) {
                            unread_count += (insertCount - meMsgCount);
                            msgValue.put(PWDBConfig.MessagesTable.UNREAD_COUNT, unread_count);
                        }
                        if (sayHelloValue != null) {
                            if (insertCount > meMsgCount && isNew == 0) {
                                if (isNew == 0) {
                                    msgValue.put(PWDBConfig.MessagesTable.INSIDE_NEW, 1);
                                }
                                if (!SayHelloActivity.isActivity) {
                                    Cursor shmCursor = query(PWDBConfig.MessagesTable.CONTENT_URI, null, selection,
                                            new String[]{String.valueOf(DfineAction.MSG_ID_SAYHELLO)}, null);
                                    if (shmCursor != null) {
                                        if (shmCursor.moveToFirst()) {
                                            int shmUnread = shmCursor.getInt(shmCursor.getColumnIndex(PWDBConfig.MessagesTable.UNREAD_COUNT));
                                            shmUnread++;
                                            sayHelloValue.put(PWDBConfig.MessagesTable.UNREAD_COUNT, shmUnread);
                                        }
                                        shmCursor.close();
                                        shmCursor = null;
                                    }
                                }
                            }
                        }
                    }
                    if (mCursor != null) {
                        mCursor.close();
                        mCursor = null;
                    }
                    if (TextUtils.isEmpty(last_update_time) || last_update_time.compareTo(update_time) <= 0) {
                        msgValue.put(PWDBConfig.MessagesTable.CONTENT, content);
                        msgValue.put(PWDBConfig.MessagesTable.UPDATE_TIME, update_time);
                        msgValue.put(PWDBConfig.MessagesTable.TYPE, type);

                        if (sayHelloValue != null) {
                            if (!TextUtils.isEmpty(name)) {
                                String newContent = MessageUtil.resetContent(content, type);
                                content = /*name + ":" + */newContent;
                            }
                            sayHelloValue.put(PWDBConfig.MessagesTable.CONTENT, content);
                            sayHelloValue.put(PWDBConfig.MessagesTable.UPDATE_TIME, update_time);
                            sayHelloValue.put(PWDBConfig.MessagesTable.TYPE, type);
                        }
                    }
                    update(PWDBConfig.MessagesTable.CONTENT_URI, msgValue, selection, selectionArgs);

                    if (sayHelloValue != null) {
                        update(PWDBConfig.MessagesTable.CONTENT_URI, sayHelloValue, selection, new String[]{String.valueOf(DfineAction.MSG_ID_SAYHELLO)});
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        initSQLite();
        int count = 0;
        switch (sMatcher.match(uri)) {
            case PWDBConfig.TB_PW_CONTACTS_URI_CODE:
                count = mDb.delete(PWDBConfig.TB_NAME_PW_CONTACTS, selection, selectionArgs);
                break;
            case PWDBConfig.TB_PW_DIALOGS_URI_CODE:
                count = mDb.delete(PWDBConfig.TB_NAME_PW_DIALOGS, selection, selectionArgs);
                break;
            case PWDBConfig.TB_PW_MESSAGE_URI_CODE:
                count = mDb.delete(PWDBConfig.TB_NAME_PW_MESSAGES, selection, selectionArgs);
                break;
            case PWDBConfig.TB_PW_REQUESTS_URI_CODE:
                count = mDb.delete(PWDBConfig.TB_NAME_PW_REQUESTS, selection, selectionArgs);
                break;
            case PWDBConfig.TB_PW_UID_MSGID_URI_CODE:
                count = mDb.delete(PWDBConfig.TB_NAME_UID_MSGID, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        initSQLite();
        int count = 0;
        switch (sMatcher.match(uri)) {
            case PWDBConfig.TB_PW_CONTACTS_URI_CODE:
                count = mDb.update(PWDBConfig.TB_NAME_PW_CONTACTS, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            case PWDBConfig.TB_PW_DIALOGS_URI_CODE:
                count = mDb.update(PWDBConfig.TB_NAME_PW_DIALOGS, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            case PWDBConfig.TB_PW_MESSAGE_URI_CODE:
                count = mDb.update(PWDBConfig.TB_NAME_PW_MESSAGES, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            case PWDBConfig.TB_PW_REQUESTS_URI_CODE:
                count = mDb.update(PWDBConfig.TB_NAME_PW_REQUESTS, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            case PWDBConfig.TB_PW_UID_MSGID_URI_CODE:
                count = mDb.update(PWDBConfig.TB_NAME_UID_MSGID, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        initSQLite();
        Cursor mCursor = null;
        switch (sMatcher.match(uri)) {
            case PWDBConfig.TB_PW_CONTACTS_URI_CODE:
                mCursor = mDb.query(PWDBConfig.TB_NAME_PW_CONTACTS, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case PWDBConfig.TB_PW_DIALOGS_URI_CODE:
                mCursor = mDb.query(PWDBConfig.TB_NAME_PW_DIALOGS, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case PWDBConfig.TB_PW_MESSAGE_URI_CODE:
                mCursor = mDb.query(PWDBConfig.TB_NAME_PW_MESSAGES, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case PWDBConfig.TB_PW_REQUESTS_URI_CODE:
                mCursor = mDb.query(PWDBConfig.TB_NAME_PW_REQUESTS, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case PWDBConfig.TB_PW_UID_MSGID_URI_CODE:
                mCursor = mDb.query(PWDBConfig.TB_NAME_UID_MSGID, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
        if (mCursor != null) {
            mCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return mCursor;
    }


}
