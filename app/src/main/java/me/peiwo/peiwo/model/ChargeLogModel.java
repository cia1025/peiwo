package me.peiwo.peiwo.model;

import org.json.JSONObject;

/**
 * Created by ChenHao on 2014-11-12 下午8:09.
 *
 * @modify:
 */
public class ChargeLogModel extends PPBaseModel {
    //    {
//        "update_time": "2014-11-07 01:39:30",
//            "money": 6,
//            "channel": 1,
//            "payment_id": 718
//    }
    public String update_time;
    public String money;
    public int channel;
    public int payment_id;

    public ChargeLogModel(JSONObject o) {
        update_time = getJsonValue(o, "update_time");
        money = getJsonValue(o, "money");
        channel = o.optInt("channel");
        payment_id = o.optInt("payment_id");
    }

}
