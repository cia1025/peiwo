package me.peiwo.peiwo.model;

import org.json.JSONObject;

/**
 * Created by fuhaidong on 14/11/13.
 */

//    alipay_account 	支付宝账号
//    money　　		本次提现金额：（分）
//    pay_time　		 打款时间
//    reason 			失败原因：（１：）
//    remarks			提现备注
//    state 			状态（/0待审核 1删除记录 2已打款 3审核通过 4审核不通过 5打款失败）
//    uid　　			用户id
//    update_time 　　提现申请时间
//    withdraw_id 　　提现ＩＤ
//
//
//    提现失败原因：
//            １：支付宝信息不完整
//    ２：支付宝帐号错误
//    ３：支付宝未实名验证
//    ４：个人头像图片违规
//    ５：个人签名/标签违规
//    ９：其他原因

//uid: 805672,
//        update_time: "2014-11-13 16:30:58",
//        reason: 2,
//        state: 2,
//        money: 2200,
//        withdraw_id: 3522,
//        alipay_account: "       ",
//        remarks: null,
//        pay_time: "2014-11-14 13:54:54"

public class WithdrawHistoryModel extends PPBaseModel {

    public String alipay_account;
    public int money;
    public String pay_time;
    public int reason;
    public String reason_str;
    public String remarks;
    public int state;
    public String state_str;
    public int uid;
    public String update_time;
    public String withdraw_id;

    public WithdrawHistoryModel(JSONObject o) {
        alipay_account = getJsonValue(o, "alipay_account");
        money = getJsonInt(o, "money");
        pay_time = getJsonValue(o, "pay_time");
        reason = getJsonInt(o, "reason");
        remarks = getJsonValue(o, "remarks");
        state = getJsonInt(o, "state");
        uid = getJsonInt(o, "uid");
        update_time = getJsonValue(o, "update_time");
        withdraw_id = getJsonValue(o, "withdraw_id");

        switch (reason) {
            case 1:
                reason_str = "(支付宝信息不完整)";
                break;
            case 2:
                reason_str = "(支付宝帐号错误)";
                break;
            case 3:
                reason_str = "(支付宝未实名验证)";
                break;
            case 4:
                reason_str = "(个人头像图片违规)";
                break;
            case 5:
                reason_str = "(个人签名/标签违规)";
                break;
            case 9:
                reason_str = "(其他原因)";
                break;
            default:
                reason_str = "";
                break;

        }

        switch (state) {
            case 0:
                state_str = "待审核";
                break;
            case 1:
                state_str = "删除记录";
                break;
            case 2:
                state_str = "已打款";
                break;
            case 3:
                state_str = "审核通过";
                break;
            case 4:
                state_str = "审核不通过";
                break;
            case 5:
                state_str = "打款失败";
                break;
        }
    }

}
