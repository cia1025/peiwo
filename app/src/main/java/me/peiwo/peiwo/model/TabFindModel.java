package me.peiwo.peiwo.model;

import org.json.JSONException;
import org.json.JSONObject;

public class TabFindModel extends PPBaseModel {

    private static final long serialVersionUID = 1L;


    public TabFindModel(JSONObject o) {
        try {
            uid = getJsonInt(o, "uid");
            birthday = getJsonValue(o, "birthday");
            avatar_thumbnail = getJsonValue(o, "avatar_thumbnail");
            slogan = getJsonValue(o, "slogan");
            name = getJsonValue(o, "name");
            gender = getJsonInt(o, "gender");
            avatar = getJsonValue(o, "avatar");
            price = o.has("price") ? o.getDouble("price") : 0;
            province = getJsonValue(o, "province");
            city = getJsonValue(o, "city");
            recommended = o.optBoolean("recommended");
            voice = o.has("voice") ? new VoiceModel(o.getJSONObject("voice")) : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int uid;
    public String birthday;
    public String avatar_thumbnail;
    public String slogan;
    public double price;
    public String name;
    public int gender;
    public String avatar;
    public int type = 0;
    public String province;
    public String city;
    public boolean recommended;
    public VoiceModel voice;
}
