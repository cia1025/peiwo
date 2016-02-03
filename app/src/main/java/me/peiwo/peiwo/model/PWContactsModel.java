package me.peiwo.peiwo.model;

import android.database.Cursor;
import me.peiwo.peiwo.constans.PWDBConfig;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fuhaidong on 14-8-27.
 */
public class PWContactsModel extends PPBaseModel {


    public int sync_id;
    public String signin_time;
    public String contact_id;

    public String uid;
    public String birthday;
    public String avatar_thumbnail;
    public String slogan;
    public String price;
    public String name;
    public String province;
    public int gender;
    public String avatar;
    public String city;
    public boolean is_group_added = false;

    public int contact_state;

    public int call_duration;

    public PWContactsModel() {
    }

    public PWContactsModel(Cursor c) {
        sync_id = c.getInt(c.getColumnIndex(PWDBConfig.ContactsTable.SYNC_ID));
        signin_time = c.getString(c.getColumnIndex(PWDBConfig.ContactsTable.SIGNIN_TIME));
        contact_id = c.getString(c.getColumnIndex(PWDBConfig.ContactsTable.CONTACT_ID));
        uid = c.getString(c.getColumnIndex(PWDBConfig.ContactsTable.UID));
        birthday = c.getString(c.getColumnIndex(PWDBConfig.ContactsTable.BIRTHDAY));
        avatar_thumbnail = c.getString(c.getColumnIndex(PWDBConfig.ContactsTable.AVATAR_THUMBNAIL));
        slogan = c.getString(c.getColumnIndex(PWDBConfig.ContactsTable.SLOGAN));
        price = c.getString(c.getColumnIndex(PWDBConfig.ContactsTable.PRICE));
        name = c.getString(c.getColumnIndex(PWDBConfig.ContactsTable.NAME));
        province = c.getString(c.getColumnIndex(PWDBConfig.ContactsTable.PROVINCE));
        gender = c.getInt(c.getColumnIndex(PWDBConfig.ContactsTable.GENDER));
        avatar = c.getString(c.getColumnIndex(PWDBConfig.ContactsTable.AVATAR));
        city = c.getString(c.getColumnIndex(PWDBConfig.ContactsTable.CITY));
        contact_state = c.getInt(c.getColumnIndex(PWDBConfig.ContactsTable.CONTACT_STATE));
        call_duration = c.getInt(c.getColumnIndex(PWDBConfig.ContactsTable.CALL_DURATION));
    }


    public PWContactsModel(JSONObject o) {
        try {
            sync_id = getJsonInt(o, "sync_id");
            signin_time = getJsonValue(o, "signin_time");
            signin_time = "null".equalsIgnoreCase(signin_time) ? "1970-01-01 00:00:00" : signin_time;
            contact_id = getJsonValue(o, "contact_id");
            contact_state = getJsonInt(o, "contact_state");
            JSONObject oo = o.getJSONObject("user");
            uid = getJsonValue(oo, "uid");
            birthday = getJsonValue(oo, "birthday");
            avatar_thumbnail = getJsonValue(oo, "avatar_thumbnail");
            slogan = getJsonValue(oo, "slogan");
            price = getJsonValue(oo, "price");
            name = getJsonValue(oo, "name");
            province = getJsonValue(oo, "province");
            gender = getJsonInt(oo, "gender");
            avatar = getJsonValue(oo, "avatar");
            city = getJsonValue(oo, "city");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
