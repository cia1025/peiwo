package me.peiwo.peiwo.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ChenHao on 2014-11-20 下午2:16.
 *
 * @modify:
 */
public class PWBlockModel extends PPBaseModel {
//    {
//        sync_id: 4,
//                block_time: "2014-08-27 16:32:04",
//            contact_id: 4,
//            user: {
//        uid: 208855,
//                birthday: "1990-01-01 00:00:00",
//                avatar_thumbnail: "http://thumbnail-cdn.peiwo.me/s_thumbnail/208855_49c6f944743ec3660d834df178f54f4d",
//                slogan: "如果没有遇见你，我将会是在哪里！",
//                price: 0,
//                name: "tttt",
//                province: null,
//                gender: 1,
//                avatar: "http://photo-cdn.peiwo.me/208855_49c6f944743ec3660d834df178f54f4d",
//                city: null
//    },
//        contact_state: 1
//    }

    public int sync_id;
    public String block_time;
    public int contact_id;

    public int uid;
    public String birthday;
    public String avatar_thumbnail;
    public String slogan;
    public String price;
    public String name;
    public String province;
    public int gender;
    public String avatar;
    public String city;
    public int contact_state;
    public String remark;

    public PWBlockModel() {
    }

//    public PWBlockModel(Cursor c) {
//        sync_id = c.getInt(c.getColumnIndex("sync_id"));
//        block_time = c.getString(c.getColumnIndex("block_time"));
//        contact_id = c.getInt(c.getColumnIndex("contact_id"));
//        uid = c.getInt(c.getColumnIndex("uid"));
//        birthday = c.getString(c.getColumnIndex("birthday"));
//        avatar_thumbnail = c.getString(c.getColumnIndex("avatar_thumbnail"));
//        slogan = c.getString(c.getColumnIndex("slogan"));
//        price = c.getString(c.getColumnIndex("price"));
//        name = c.getString(c.getColumnIndex("name"));
//        province = c.getString(c.getColumnIndex("province"));
//        gender = c.getInt(c.getColumnIndex("gender"));
//        avatar = c.getString(c.getColumnIndex("avatar"));
//        city = c.getString(c.getColumnIndex("city"));
//        contact_state = c.getInt(c.getColumnIndex("contact_state"));
//    }


    public PWBlockModel(JSONObject o) {
        try {
            sync_id = getJsonInt(o, "sync_id");
            block_time = getJsonValue(o, "block_time");
            block_time = "null".equalsIgnoreCase(block_time) ? "1970-01-01 00:00:00" : block_time;
            contact_id = getJsonInt(o, "contact_id");
            contact_state = getJsonInt(o, "contact_state");
            JSONObject oo = o.getJSONObject("user");
            uid = getJsonInt(oo, "uid");
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
