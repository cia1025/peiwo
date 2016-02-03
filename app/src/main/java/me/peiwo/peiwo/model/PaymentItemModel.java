package me.peiwo.peiwo.model;

import org.json.JSONObject;

/**
 * Created by fuhaidong on 15/11/17.
 */
public class PaymentItemModel extends PPBaseModel {

//    iap_product_id": "me.peiwo.peiwo.item2",
//            "name": "\u966a\u6211\u8d26\u6237\u5145\u503c12\u5143",
//            "money": 12,
//            "price": 0.01,
//            "buyable": true,
//            "item_id": "item_2",
//            "desc": "\u966a\u6211\u8d26\u6237\u5145\u503c12\u5143"


    public String item_id;
    public String money;
    public String price;
    public String name;
    public String desc;
    public boolean buyable;
    public boolean isselected = false;

    public PaymentItemModel(JSONObject o) {
        item_id = getJsonValue(o, "item_id");
        money = getJsonValue(o, "money");
        price = getJsonValue(o, "price");
        name = getJsonValue(o, "name");
        desc = getJsonValue(o, "desc");
        buyable = o.optBoolean("buyable");
    }
}
