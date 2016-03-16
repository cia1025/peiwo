package me.peiwo.peiwo.net;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.alibaba.fastjson.JSON;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.activity.WelcomeActivity;
import me.peiwo.peiwo.model.ProfileForUpdateModel;
import me.peiwo.peiwo.util.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscriber;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApiRequestWrapper {

    private static final boolean SIGN_POST = false;
    private static final boolean SIGN_GET = true;

    public static void signin(Context c, String social_type, String social_uid,
                              String access_token, MsgStructure msg) {

        String keyname = AsynHttpClient.KEY_SOCIAL_UID;
        String keypwd = AsynHttpClient.KEY_ACCESS_TOKEN;
        if (Integer.valueOf(social_type) == WelcomeActivity.SOCIAL_TYPE_PHONE) {
            keyname = "phone";
            keypwd = "password";
            access_token = Md5Util.getMd5code(access_token);
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_SOCIAL_TYPE,
                social_type));
        paramList.add(new BasicNameValuePair(keyname, social_uid));
        paramList.add(new BasicNameValuePair(keypwd, access_token));
        /**************** 添加前置参数 ******************/
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_VERSION, PWUtils
                .getVersionCode(c)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_APP, PWUtils
                .getChannel(c)));
        /**************** 添加前置参数 ******************/
        msg.requestUrl = buildGetUrl(AsynHttpClient.API_ACCOUNT_SIGNIN,
                paramList);
        CustomLog.d("request url is : " + msg.requestUrl);
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    public static void bindSocial(Context ctx, int uid, String social_uid, String access_token, int social_type, MsgStructure msg) {
        String keyname = AsynHttpClient.KEY_SOCIAL_UID;
        String keypwd = AsynHttpClient.KEY_ACCESS_TOKEN;
        if (social_type == WelcomeActivity.SOCIAL_TYPE_PHONE) {
            keyname = "phone";
            keypwd = "password";
            access_token = Md5Util.getMd5code(access_token);
        }

        String session_data = SharedPreferencesUtil.getStringExtra(ctx, UserManager.KEY_SESSION_DATA, "");
        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("uid", String.valueOf(uid)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_SOCIAL_TYPE,
                String.valueOf(social_type)));
        if (!TextUtils.isEmpty(social_uid)) {
            paramList.add(new BasicNameValuePair(keyname, social_uid));
        }
        paramList.add(new BasicNameValuePair(keypwd, access_token));
        addSign(ctx, paramList, AsynHttpClient.API_ACCOUNT_BIND_SOCIAL,
                SIGN_POST, session_data);
        msg.requestUrl = buildPostUrl(AsynHttpClient.API_ACCOUNT_BIND_SOCIAL, paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpPost(msg);
    }

    public static void getRecommendList(Context c, int uid, MsgStructure msg) {
        if (uid <= 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));
        addSign(c, paramList, AsynHttpClient.API_USERLIST_RECOMMENDLIST,
                SIGN_GET);

        msg.requestUrl = buildGetUrl(AsynHttpClient.API_USERLIST_RECOMMENDLIST,
                paramList);
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    // public static void getMainList(Context c, int uid, int index,
    // int genderMask, MsgStructure msg, ) {
    // getMainList(c, uid, index, genderMask, msg, isreload);
    // }

    public static void getMainList(Context c, int uid, String cursor,
                                   MsgStructure msg) {
        if (uid <= 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));
        if (!TextUtils.isEmpty(cursor)) {
            paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_CURSOR, String
                    .valueOf(cursor)));
        }


        addSign(c, paramList, AsynHttpClient.API_USERLIST_MAINLIST, SIGN_GET);

        msg.requestUrl = buildGetUrl(AsynHttpClient.API_USERLIST_MAINLIST,
                paramList);
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    public static void signout(Context c, int uid, MsgStructure msg) {
        if (uid < 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));
        addSign(c, paramList, AsynHttpClient.API_ACCOUNT_SIGNOUT, SIGN_GET);

        msg.requestUrl = buildGetUrl(AsynHttpClient.API_ACCOUNT_SIGNOUT,
                paramList);
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    public static void getUserInfo(Context c, int uid, String tuid,
                                   MsgStructure msg) {
        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_TUID, tuid));
        addSign(c, paramList, AsynHttpClient.API_USERINFO_GETINFO, SIGN_GET);

        msg.requestUrl = buildGetUrl(AsynHttpClient.API_USERINFO_GETINFO,
                paramList);
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    public static void applyCommit(Context c, int uid, int tuid, String reqStr,
                                   MsgStructure msg) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(tuid);
        applyCommit(c, uid, list, reqStr, msg);
    }

    public static void applyCommit(Context c, int uid,
                                   ArrayList<Integer> tuidList, String reqStr, MsgStructure msg) {
        if (uid <= 0 || tuidList == null || tuidList.size() == 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        sendMessage(c, uid, tuidList, AsynHttpClient.MSG_TYPE_CALL_APPLY,
                reqStr, msg);
    }

    private static String idList2Str(ArrayList<Integer> msgIdList) {
        if (msgIdList.size() < 1)
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append(msgIdList.get(0));
        for (int i = 1; i < msgIdList.size(); i++) {
            sb.append(',').append(msgIdList.get(i));
        }

        return sb.toString();
    }

    public static void callHistoryList(Context c, int uid, int index,
                                       boolean isreload, MsgStructure msg) {
        if (uid <= 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_INDEX, String
                .valueOf(index)));
        if (isreload) {
            paramList
                    .add(new BasicNameValuePair(AsynHttpClient.KEY_RELOAD, "1"));
        }
        addSign(c, paramList, AsynHttpClient.API_CALLHISTORY_LIST, SIGN_GET);

        msg.requestUrl = buildGetUrl(AsynHttpClient.API_CALLHISTORY_LIST,
                paramList);
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    public static void uploadUserInfoWithInit(Context c,
                                              ProfileForUpdateModel model, MsgStructure msg) {
        if (model == null) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();

        setParamValue(paramList, AsynHttpClient.KEY_NAME, model.name);
        setParamValue(paramList, AsynHttpClient.KEY_UID,
                String.valueOf(model.uid));
        setParamValue(paramList, AsynHttpClient.KEY_GENDER,
                String.valueOf(model.gender));
        setParamValue(paramList, AsynHttpClient.KEY_BIRTHDAY, model.birthday);
        String images = model.getImages();
        if (!TextUtils.isEmpty(images))
            paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_IMAGES, images));

        addSign(c, paramList, AsynHttpClient.API_USERINFO_UPDATE, SIGN_POST);

        msg.requestUrl = buildPostUrl(AsynHttpClient.API_USERINFO_UPDATE,
                paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpPost(msg);
    }

    public static void updateProfile(Context c, ProfileForUpdateModel model,
                                     MsgStructure msg) {
        if (model == null) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();

        setParamValue(paramList, AsynHttpClient.KEY_NAME, model.name);
        // debugLog("name==" + model.name);
        setParamValue(paramList, AsynHttpClient.KEY_UID,
                String.valueOf(model.uid));
        // debugLog("uid==" + model.uid);
        setParamValue(paramList, AsynHttpClient.KEY_BIRTHDAY, model.birthday);
        // debugLog("birthday==" + model.birthday);
//        if (!TextUtils.isEmpty(model.province)) {
        setParamValue(paramList, AsynHttpClient.KEY_PROVINCE,
                model.province);
        // debugLog("province==" + model.province);
//        }
//        if (!TextUtils.isEmpty(model.city)) {
        setParamValue(paramList, AsynHttpClient.KEY_CITY, model.city);
        // debugLog("city==" + model.city);
//        }

        setParamValue(paramList, AsynHttpClient.KEY_EMOTION,
                String.valueOf(model.emotion));
        // debugLog("emotion==" + model.emotion);
        setParamValue(paramList, AsynHttpClient.KEY_SLOGAN, model.slogan);
        // debugLog("slogan==" + model.slogan);
        setParamValue(paramList, AsynHttpClient.KEY_PROFESSION,
                model.profession);
        // debugLog("profession==" + model.profession);
        // setParamValue(paramList, AsynHttpClient.KEY_TAGS, model.tags);
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_TAGS,
                model.tags));

        paramList.add(new BasicNameValuePair("food_tags", model.food_tags));
        paramList.add(new BasicNameValuePair("music_tags", model.music_tags));
        paramList.add(new BasicNameValuePair("movie_tags", model.movie_tags));
        paramList.add(new BasicNameValuePair("book_tags", model.book_tags));
        paramList.add(new BasicNameValuePair("travel_tags", model.travel_tags));
        paramList.add(new BasicNameValuePair("sport_tags", model.sport_tags));
        paramList.add(new BasicNameValuePair("app_tags", model.game_tags));

        // debugLog("tags==" + model.tags);

        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_IMAGES, model
                .getImages()));
        // debugLog("getImages==" + model.getImages());

        addSign(c, paramList, AsynHttpClient.API_USERINFO_UPDATE, SIGN_POST);

        msg.requestUrl = buildPostUrl(AsynHttpClient.API_USERINFO_UPDATE,
                paramList);
        // debugLog("requestUrl==" + msg.requestUrl);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpPost(msg);
    }

    public static void getProfileComplement(Context c, ProfileForUpdateModel model, MsgStructure msg) {
        if (model == null) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();

        setParamValue(paramList, AsynHttpClient.KEY_NAME, model.name);
        setParamValue(paramList, AsynHttpClient.KEY_UID, String.valueOf(model.uid));
        setParamValue(paramList, AsynHttpClient.KEY_BIRTHDAY, model.birthday);
        // debugLog("birthday==" + model.birthday);
        if (!TextUtils.isEmpty(model.province)) {
            setParamValue(paramList, AsynHttpClient.KEY_PROVINCE, model.province);
        }
        if (!TextUtils.isEmpty(model.city)) {
            setParamValue(paramList, AsynHttpClient.KEY_CITY, model.city);
        }

        setParamValue(paramList, AsynHttpClient.KEY_EMOTION, String.valueOf(model.emotion));
        setParamValue(paramList, AsynHttpClient.KEY_SLOGAN, model.slogan);
        setParamValue(paramList, AsynHttpClient.KEY_PROFESSION, model.profession);
        setParamValue(paramList, AsynHttpClient.KEY_TAGS, model.tags);

        setParamValue(paramList, "food_tags", model.food_tags);
        setParamValue(paramList, "music_tags", model.music_tags);
        setParamValue(paramList, "movie_tags", model.movie_tags);
        setParamValue(paramList, "book_tags", model.book_tags);
        setParamValue(paramList, "travel_tags", model.travel_tags);
        setParamValue(paramList, "sport_tags", model.sport_tags);
        setParamValue(paramList, "app_tags", model.game_tags);

        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_IMAGES, model.getImages()));

        addSign(c, paramList, AsynHttpClient.API_GET_USERINFO_COMPLEMENT, SIGN_POST);

        msg.requestUrl = buildPostUrl(AsynHttpClient.API_GET_USERINFO_COMPLEMENT, paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpPost(msg);
    }

    private static void setParamValue(ArrayList<NameValuePair> paramList,
                                      String key, String value) {
        // if (!TextUtils.isEmpty(value)) {
        paramList.add(new BasicNameValuePair(key, value));
        // }
    }

    private static String buildPostUrl(String path,
                                       List<NameValuePair> paramList) {
        String strHTTPType = PeiwoApp.getApplication().GetPWConfig().GetHTTPSvr().m_strHTTPType;
        String strHTTPSvr = PeiwoApp.getApplication().GetPWConfig().GetHTTPSvr().m_strHostName;
        int nPort = PeiwoApp.getApplication().GetPWConfig().GetHTTPSvr().m_nPort;

        if (!TextUtils.isEmpty(PeiwoApp.getApplication().getAvailable_http_host())) {
            Uri uri = Uri.parse(PeiwoApp.getApplication().getAvailable_http_host());
            if (!strHTTPType.equals(uri.getScheme())
                    || !strHTTPSvr.equals(uri.getHost())
                    || nPort != uri.getPort()) {
                strHTTPType = uri.getScheme();
                strHTTPSvr = uri.getHost();
                nPort = uri.getPort();
            }
        }
        try {
            return URIUtils.createURI(strHTTPType, strHTTPSvr, nPort, path,
                    null, null).toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String buildGetUrl(String path, List<NameValuePair> paramList, boolean get) {
        CustomLog.i("buildGetUrl, Config is : " + PeiwoApp.getApplication().GetPWConfig());
        String strHTTPType = PeiwoApp.getApplication().GetPWConfig().GetHTTPSvr().m_strHTTPType;
        String strHTTPSvr = PeiwoApp.getApplication().GetPWConfig().GetHTTPSvr().m_strHostName;
        int nPort = PeiwoApp.getApplication().GetPWConfig().GetHTTPSvr().m_nPort;
        if (!TextUtils.isEmpty(PeiwoApp.getApplication().getAvailable_http_host())) {
            Uri uri = Uri.parse(PeiwoApp.getApplication().getAvailable_http_host());
            if (!strHTTPType.equals(uri.getScheme())
                    || !strHTTPSvr.equals(uri.getHost())
                    || nPort != uri.getPort()) {
                strHTTPType = uri.getScheme();
                strHTTPSvr = uri.getHost();
                nPort = uri.getPort();
            }
        }

        try {
            String query = "";
            if (get) {
                query = paramList != null && paramList.size() > 0 ? URLEncodedUtils
                        .format(paramList, AsynHttpClient.ENCODEING) : "";
            }
            return URIUtils.createURI(strHTTPType, strHTTPSvr, nPort, path,
                    query, null).toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String buildGetUrl(String path, List<NameValuePair> paramList) {
        return buildGetUrl(path, paramList, true);
    }

    /**
     * @param c
     * @param uid
     * @param token         小米的regId或者信鸽的token
     * @param platform_type 小米推送为0，信鸽推送为1
     * @param msg
     */
    public static void reportPushToken(Context c, int uid, String token, int platform_type, MsgStructure msg) {
        if (uid == 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();

        setParamValue(paramList, AsynHttpClient.KEY_UID, String.valueOf(uid));

        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_DEVICE_TOKEN,
                token));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_DEVICE_TYPE,
                AsynHttpClient.DEVICE_ANDROID));

        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_PLATFORM_TYPE,
                String.valueOf(platform_type)));

        addSign(c, paramList, AsynHttpClient.API_USERINFO_PUSHBIND, SIGN_POST);

        msg.requestUrl = buildPostUrl(AsynHttpClient.API_USERINFO_PUSHBIND,
                paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpPost(msg);
    }


    public static void addSign(Context c, List<NameValuePair> paramList,
                               String apiName, boolean isGetMethod) {
        /**************** 添加前置参数 ******************/
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_VERSION, PWUtils.getVersionCode(c)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_APP, PWUtils.getChannel(c)));
        /**************** 添加前置参数 ******************/
        List<String> sortList = new ArrayList<String>();
        sortList.add(new BasicNameValuePair(AsynHttpClient.KEY_SESSION_DATA, UserManager.getSessionData(c)).toString());
        // if (AppConfig.PARAM_DEBUG_1)
        // sortList.add(new BasicNameValuePair("debug", "1").toString());

        for (NameValuePair valuePair : paramList) {
            sortList.add(valuePair.toString());
        }
        Collections.sort(sortList);

        StringBuilder sb = new StringBuilder();
        sb.append(isGetMethod ? "GET" : "POST").append(apiName);
        for (String param : sortList) {
            sb.append(param);
        }

        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_SIGN, Md5Util
                .getMd5code(sb.toString())));

//        String url = buildGetUrl(apiName, paramList);
//        StringBuilder builder = new StringBuilder();
//        builder.append(url).append("\n");
//        for (NameValuePair pair : paramList) {
//            builder.append(pair.getName()).append("==").append(pair.getValue()).append("\n");
//        }
//        builder.append("\n\n\n\n");
//        Log.i("tag", builder.toString());
//        CustomLog.writeTestFile("peiwo_api_log", builder.toString());

    }

    public static void addSign(Context c, List<NameValuePair> paramList,
                               String apiName, boolean isGetMethod, String session_data) {
        /**************** 添加前置参数 ******************/
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_VERSION, PWUtils.getVersionCode(c)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_APP, PWUtils.getChannel(c)));
        /**************** 添加前置参数 ******************/
        List<String> sortList = new ArrayList<String>();

        sortList.add(new BasicNameValuePair(AsynHttpClient.KEY_SESSION_DATA, session_data).toString());
        // if (AppConfig.PARAM_DEBUG_1)
        // sortList.add(new BasicNameValuePair("debug", "1").toString());

        for (NameValuePair valuePair : paramList) {
            sortList.add(valuePair.toString());
        }
        Collections.sort(sortList);

        StringBuilder sb = new StringBuilder();
        sb.append(isGetMethod ? "GET" : "POST").append(apiName);
        for (String param : sortList) {
            sb.append(param);
        }

        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_SIGN, Md5Util
                .getMd5code(sb.toString())));

    }

    private static String optString(JSONObject jObj, String key) {
        if (!jObj.isNull(key)) {
            try {
                return jObj.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static void sendMessage(Context c, int uid, int tuid, int msgType,
                                   String reqStr, MsgStructure msg) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(tuid);
        sendMessage(c, uid, list, msgType, reqStr, msg);
    }

    public static void sendMessage(Context c, int uid,
                                   ArrayList<Integer> tuidList, int msgType, String reqStr,
                                   MsgStructure msg) {
        if (uid <= 0 || tuidList == null || tuidList.size() == 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_TUIDS,
                idList2Str(tuidList)));
        paramList
                .add(new BasicNameValuePair(AsynHttpClient.KEY_CONTENT, reqStr));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_MSG_TYPE,
                String.valueOf(msgType)));

        addSign(c, paramList, AsynHttpClient.API_MESSAGE_SEND, SIGN_POST);

        msg.requestUrl = buildPostUrl(AsynHttpClient.API_MESSAGE_SEND,
                paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpPost(msg);
    }

    public static void messageDelete(Context c, int uid, int msgId,
                                     MsgStructure msg) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(msgId);
        messageDelete(c, uid, list, msg);
    }

    public static void messageDelete(Context c, int uid,
                                     ArrayList<Integer> msgIdList, MsgStructure msg) {
        if (uid <= 0 || msgIdList == null || msgIdList.size() == 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_MSG_IDS,
                idList2Str(msgIdList)));

        addSign(c, paramList, AsynHttpClient.API_MESSAGE_DELETE, SIGN_GET);

        msg.requestUrl = buildGetUrl(AsynHttpClient.API_MESSAGE_DELETE,
                paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    public static void getPriceRange(Context c, int uid, MsgStructure msg) {
        if (uid <= 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));

        addSign(c, paramList, AsynHttpClient.API_SETTING_PRICE, SIGN_GET);

        msg.requestUrl = buildGetUrl(AsynHttpClient.API_SETTING_PRICE,
                paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    public static void settingPrice(Context c, int uid, String price,
                                    MsgStructure msg) {
        if (uid <= 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));
        // if (price == 0) {
        // paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_FREE,
        // String.valueOf(1)));
        // } else {
        // paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_FREE,
        // String.valueOf(0)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_PRICE, price));

        // }

        addSign(c, paramList, AsynHttpClient.API_SETTING_PRICE, SIGN_GET);

        msg.requestUrl = buildGetUrl(AsynHttpClient.API_SETTING_PRICE,
                paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    public static void getPaymentList(Context c, int uid, MsgStructure msg) {
        if (uid <= 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));

        addSign(c, paramList, AsynHttpClient.API_PAYMENT_ITEMS, SIGN_GET);

        msg.requestUrl = buildGetUrl(AsynHttpClient.API_PAYMENT_ITEMS,
                paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }


    public static void createOrder(Context c, int uid, String itemId,
                                   int channel, MsgStructure msg) {
        if (uid <= 0 || TextUtils.isEmpty(itemId)) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String.valueOf(uid)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_ITEM_ID, itemId));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_CHANNEL, String.valueOf(channel)));

        addSign(c, paramList, AsynHttpClient.API_PAYMENT_ORDER, SIGN_GET);
        msg.requestUrl = buildGetUrl(AsynHttpClient.API_PAYMENT_ORDER, paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    public static void withdraw(Context c, int uid, String money,
                                String account, String account_name, String password, MsgStructure msg) {
        if (uid <= 0 || TextUtils.isEmpty(account) || Float.valueOf(money) <= 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_MONEY, String
                .valueOf(money)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_ALIPAY_ACCOUNT,
                String.valueOf(account)));
        paramList.add(new BasicNameValuePair("password", password));
        paramList.add(new BasicNameValuePair("alipay_name", account_name));

        addSign(c, paramList, AsynHttpClient.API_PAYMENT_WITHDRAW, SIGN_GET);

        msg.requestUrl = buildGetUrl(AsynHttpClient.API_PAYMENT_WITHDRAW,
                paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    public static void settingNoDisturb(Context c, int uid, int nopush,
                                        int nodisturb, String interval, MsgStructure msg) {
        if (uid <= 0 || TextUtils.isEmpty(interval)) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_NOPUSH, String
                .valueOf(nopush)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_NODISTURB,
                String.valueOf(nodisturb)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_INTERVAL,
                interval));
        addSign(c, paramList, AsynHttpClient.API_SETTING_NODISTURB, SIGN_GET);
        msg.requestUrl = buildGetUrl(AsynHttpClient.API_SETTING_NODISTURB,
                paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    public static void getNoDisturbSetting(Context c, int uid, MsgStructure msg) {
        if (uid <= 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));

        addSign(c, paramList, AsynHttpClient.API_SETTING_NODISTURB, SIGN_GET);

        msg.requestUrl = buildGetUrl(AsynHttpClient.API_SETTING_NODISTURB,
                paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    // dfh
    public static void getBadge(Context c, int uid, MsgStructure msg) {
        if (uid <= 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));

        addSign(c, paramList, AsynHttpClient.API_MESSAGE_BADGE, SIGN_GET);

        msg.requestUrl = buildGetUrl(AsynHttpClient.API_MESSAGE_BADGE,
                paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    /**
     * 取得两个人的通话权限
     */
    public static void getPermission(Context c, int uid, int tuid,
                                     MsgStructure msg) {
        // API_USERINFO_PERMISSION
        if (uid <= 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_TUID, String
                .valueOf(tuid)));

        addSign(c, paramList, AsynHttpClient.API_USERINFO_PERMISSION, SIGN_GET);

        msg.requestUrl = buildGetUrl(AsynHttpClient.API_USERINFO_PERMISSION,
                paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    public static void getUserMsgId(Context c, int uid, int tuid, MsgStructure msg) {
        if (uid <= 0) {
            msg.onError(AsynHttpClient.ERROR_LOCAL_PARAM, null);
            return;
        }
        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_UID, String
                .valueOf(uid)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_TUID, String
                .valueOf(tuid)));

        addSign(c, paramList, AsynHttpClient.API_USERINFO_MESSAGEID, SIGN_GET);

        msg.requestUrl = buildGetUrl(AsynHttpClient.API_USERINFO_MESSAGEID,
                paramList);
        msg.paramList = paramList;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    /**
     * 通用api接口方法 dfh 避免此类方法过多
     *
     * @param c
     * @param params    参数不再需要传递uid等前置参数
     * @param apiMethod 请求的地址api方法
     * @param msg
     */
    public static void openAPIGET(Context c, ArrayList<NameValuePair> params,
                                  String apiMethod, MsgStructure msg) {
        request(c, params, apiMethod, true, msg);
    }

    public static void openAPIPOST(Context c, ArrayList<NameValuePair> params,
                                   String apiMethod, MsgStructure msg) {
        request(c, params, apiMethod, false, msg);
    }

    /********
     * rx
     *********/
    public static <T> Observable<T> apiGet(Context c, ArrayList<NameValuePair> params, String apiMethod, Class<T> clazz) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                request(c, params, apiMethod, true, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        if (subscriber != null && !subscriber.isUnsubscribed()) {
                            T t = JSON.parseObject(data.toString(), clazz);
                            subscriber.onNext(t);
                        }
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        if (subscriber != null && !subscriber.isUnsubscribed())
                            subscriber.onError(new PWError(error, ret));
                    }
                });
            }
        });
    }

    public static <T> Observable<T> apiGetIntercept(Context c, ArrayList<NameValuePair> params, String apiMethod, Class<T> clazz) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                request(c, params, apiMethod, true, new MsgStructure() {
                    @Override
                    public boolean onInterceptRawData(String rawStr) {
                        if (subscriber != null && !subscriber.isUnsubscribed()) {
                            T t = JSON.parseObject(rawStr, clazz);
                            subscriber.onNext(t);
                        }
                        return true;
                    }

                    @Override
                    public void onReceive(JSONObject data) {

                    }

                    @Override
                    public void onError(int error, Object ret) {
                        if (subscriber != null && !subscriber.isUnsubscribed())
                            subscriber.onError(new PWError(error, ret));
                    }
                });
            }
        });
    }

    public static <T> Observable<T> apiPost(Context c, ArrayList<NameValuePair> params, String apiMethod, Class<T> clazz) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                request(c, params, apiMethod, false, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        if (subscriber != null && !subscriber.isUnsubscribed()) {
                            T t = JSON.parseObject(data.toString(), clazz);
                            subscriber.onNext(t);
                        }
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        if (subscriber != null && !subscriber.isUnsubscribed())
                            subscriber.onError(new PWError(error, ret));
                    }
                });
            }
        });
    }

    public static <T> Observable<T> apiPostIntercept(Context c, ArrayList<NameValuePair> params, String apiMethod, Class<T> clazz) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                request(c, params, apiMethod, false, new MsgStructure() {
                    @Override
                    public boolean onInterceptRawData(String rawStr) {
                        if (subscriber != null && !subscriber.isUnsubscribed()) {
                            T t = JSON.parseObject(rawStr, clazz);
                            subscriber.onNext(t);
                        }
                        return true;
                    }

                    @Override
                    public void onReceive(JSONObject data) {

                    }

                    @Override
                    public void onError(int error, Object ret) {
                        if (subscriber != null && !subscriber.isUnsubscribed())
                            subscriber.onError(new PWError(error, ret));
                    }
                });
            }
        });
    }

    public static Observable<JSONObject> apiGetJson(Context c, ArrayList<NameValuePair> params, String apiMethod) {
        return Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                request(c, params, apiMethod, true, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        if (subscriber != null && !subscriber.isUnsubscribed())
                            subscriber.onNext(data);
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        if (subscriber != null && !subscriber.isUnsubscribed())
                            subscriber.onError(new PWError(error, ret));
                    }
                });
            }
        });
    }

    public static Observable<JSONObject> apiGetJsonIntercept(Context c, ArrayList<NameValuePair> params, String apiMethod) {
        return Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                request(c, params, apiMethod, true, new MsgStructure() {
                    @Override
                    public boolean onInterceptRawData(String rawStr) {
                        if (subscriber != null && !subscriber.isUnsubscribed()) {
                            try {
                                subscriber.onNext(new JSONObject(rawStr));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                subscriber.onError(new PWError(-1, rawStr));
                            }
                        }
                        return true;
                    }

                    @Override
                    public void onReceive(JSONObject data) {

                    }

                    @Override
                    public void onError(int error, Object ret) {
                        if (subscriber != null && !subscriber.isUnsubscribed())
                            subscriber.onError(new PWError(error, ret));
                    }
                });
            }
        });
    }

    public static Observable<JSONObject> apiPostJson(Context c, ArrayList<NameValuePair> params, String apiMethod) {
        return Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                request(c, params, apiMethod, false, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                        if (subscriber != null && !subscriber.isUnsubscribed())
                            subscriber.onNext(data);
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        if (subscriber != null && !subscriber.isUnsubscribed())
                            subscriber.onError(new PWError(error, ret));
                    }
                });
            }
        });
    }

    public static Observable<JSONObject> apiPostJsonIntercept(Context c, ArrayList<NameValuePair> params, String apiMethod) {
        return Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                request(c, params, apiMethod, false, new MsgStructure() {
                    @Override
                    public boolean onInterceptRawData(String rawStr) {
                        if (subscriber != null && !subscriber.isUnsubscribed()) {
                            try {
                                subscriber.onNext(new JSONObject(rawStr));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                subscriber.onError(new PWError(-1, rawStr));
                            }
                        }
                        return true;
                    }

                    @Override
                    public void onReceive(JSONObject data) {

                    }

                    @Override
                    public void onError(int error, Object ret) {
                        if (subscriber != null && !subscriber.isUnsubscribed())
                            subscriber.onError(new PWError(error, ret));
                    }
                });
            }
        });
    }

    /********
     * rx
     *********/

    private static void request(Context context,
                                ArrayList<NameValuePair> params, String apiMethod, boolean get,
                                MsgStructure msg) {
        if (params != null) {
            int uid = UserManager.getUid(context);
            CustomLog.d("apimethod is : " + apiMethod + "\t ,uid is : " + uid);
            if (uid != 0) {
                String suid = String.valueOf(uid);
                BasicNameValuePair uidparams = new BasicNameValuePair(AsynHttpClient.KEY_UID, suid);
                if (params.indexOf(uidparams) == -1) {
                    params.add(uidparams);
                }
            }

        }
        addSign(context, params, apiMethod, get);

        msg.requestUrl = buildGetUrl(apiMethod, params, get);
        msg.paramList = params;
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        if (get) {
            httpClient.execHttpGet(msg);
        } else {
            httpClient.execHttpPost(msg);
        }
    }

    /**
     * 注册提交手机号接口
     * , String captcha_type 不再需要这个type
     *
     * @param c
     * @param phone
     * @param captcha_type
     * @param msg
     */
    public static void captcha(Context c, String phone, String captcha_type,
                               MsgStructure msg) {
        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("phone", phone));
        paramList.add(new BasicNameValuePair("captcha_type", captcha_type));
        /**************** 添加前置参数 ******************/
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_VERSION, PWUtils
                .getVersionCode(c)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_APP, PWUtils
                .getChannel(c)));
        /**************** 添加前置参数 ******************/
        msg.requestUrl = buildGetUrl(AsynHttpClient.API_ACCOUNT_CAPTCHA,
                paramList);
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }

    //String captcha_type, 不再需要
    /*public static void signup(Context c, String phone, String captcha,
                              String password, MsgStructure msg) {
        // account/signup?phone=xxx&captcha=xxxx&password=xxxx&captcha_type=XXX
        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("phone", phone));
        paramList.add(new BasicNameValuePair("captcha", captcha));
        paramList.add(new BasicNameValuePair("password", password));
        //paramList.add(new BasicNameValuePair("captcha_type", captcha_type));
        *//**************** 添加前置参数 ******************//*
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_VERSION, PWUtils
                .getVersionCode(c)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_APP, PWUtils
                .getChannel(c)));
        *//**************** 添加前置参数 ******************//*
        msg.requestUrl = buildGetUrl(AsynHttpClient.API_ACCOUNT_SIGNUP,
                paramList);
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }*/

    /**
     * 参数为phone，captcha，新的password
     *
     * @param phone
     * @param captcha
     * @param password
     * @param msg
     */
    public static void forgetPhone(Context c, String phone, String captcha,
                                   String password, MsgStructure msg) {
        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("phone", phone));
        paramList.add(new BasicNameValuePair("captcha", captcha));
        paramList.add(new BasicNameValuePair("password", Md5Util.getMd5code(password)));
        //paramList.add(new BasicNameValuePair("captcha_type", captcha_type));
        /**************** 添加前置参数 ******************/
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_VERSION, PWUtils
                .getVersionCode(c)));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_APP, PWUtils
                .getChannel(c)));
        /**************** 添加前置参数 ******************/
        msg.requestUrl = buildGetUrl(AsynHttpClient.API_ACCOUNT_FORGETPWD,
                paramList);
        AsynHttpClient httpClient = AsynHttpClient.getInstance();
        httpClient.execHttpGet(msg);
    }
}
