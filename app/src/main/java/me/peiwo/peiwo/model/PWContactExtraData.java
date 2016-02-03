package me.peiwo.peiwo.model;

import org.json.JSONObject;

/**
 * Created by fuhaidong on 15/12/1.
 */
public class PWContactExtraData {
    public String signin_time;
    public int contact_call_duration;
    public String uid;
    public String user_active_time;

    public PWContactExtraData(JSONObject o) {
        signin_time = o.optString("signin_time");
        contact_call_duration = o.optInt("contact_call_duration");
        uid = o.optString("uid");
        user_active_time = o.optString("user_active_time");
    }
}
