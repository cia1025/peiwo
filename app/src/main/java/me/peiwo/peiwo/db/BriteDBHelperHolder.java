package me.peiwo.peiwo.db;

import android.content.Context;
import android.text.TextUtils;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.io.IOException;

/**
 * Created by fuhaidong on 15/9/6.
 */
public class BriteDBHelperHolder {

    private static BriteDBHelperHolder INSTANCE;
    private BriteDatabase briteDatabase;

    private BriteDBHelperHolder() {

    }

    private void setUpSqlBrite(Context context, String dbName) {
        SqlBrite sqlBrite = SqlBrite.create();
        briteDatabase = sqlBrite.wrapDatabaseHelper(new DBOpenHelperImpl(context, dbName, null, PWDbContentProvider.DB_VERSION));
    }


    public static BriteDBHelperHolder getInstance() {
        synchronized (BriteDBHelperHolder.class) {
            if (INSTANCE == null) {
                INSTANCE = new BriteDBHelperHolder();
            }
        }
        return INSTANCE;
    }

    public BriteDatabase getBriteDatabase(Context context) {
        if (this.briteDatabase == null) {
            String dbName = PWDBUtil.getCurrentMsgCenterDBName(context);
            if (!TextUtils.isEmpty(dbName)) {
                setUpSqlBrite(context, dbName);
                //Log.i("brite", "lazy setup sqlbrite");
            }
        }
        return this.briteDatabase;
    }

    public void resetBriteDatebase() {
        try {
            if (briteDatabase != null) {
                briteDatabase.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            briteDatabase = null;
        }
    }


}
