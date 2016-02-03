package me.peiwo.peiwo.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.*;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.WildcatShareAlertActivity;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.UserInfoEvent;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.NetUtil;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;

public class PWUtils {

    public static final int THUMB_SIZE = 120;

    public static String getEmotion(int emotion) {
        // 情感状态 0: 默认保密, 1: 单身, 2: 恋爱中, 3: 已婚, 4: 同性
        switch (emotion) {
            case 0:
                return "保密";
            case 1:
                return "单身";
            case 2:
                return "恋爱中";
            case 3:
                return "已婚";
            case 4:
                return "同性";
            default:
                return "";
        }
    }

    public static int getPXbyDP(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public static int getDPbyPX(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f) - 15;
    }

    public static int getPXbySP(Context context, float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                context.getResources().getDisplayMetrics());
    }

    /**
     * server端返回float类型的price，json解析只能得到double，需转换
     *
     * @param price
     * @return
     */
    public static float getPriceByJsonValue(double price) {
        DecimalFormat format = new DecimalFormat("#.0");
        return Float.valueOf(format.format(price));
    }

    /**
     * 计算字符串长度，中英文混合计算
     *
     * @param src
     * @return
     */
    public static int calculateCharLength(String src) {
        if (TextUtils.isEmpty(src)) {
            return 0;
        }
        int counter = -1;
        if (src != null) {
            counter = 0;
            final int len = src.length();
            for (int i = 0; i < len; i++) {
                char sigleItem = src.charAt(i);
                if (isAlphanumeric(sigleItem)) {
                    counter++;
                } else if (Character.isLetter(sigleItem)) {
                    counter = counter + 2;
                } else {
                    counter++;
                }
            }
        } else {
            counter = -1;
        }

        return counter;
    }

    /**
     * 判断字符是否为英文字母或者阿拉伯数字.
     *
     * @param ch char字符
     * @return true or false
     */
    public static boolean isAlphanumeric(char ch) {
        // 常量定义
        final int DIGITAL_ZERO = 0;
        final int DIGITAL_NINE = 9;
        final char MIN_LOWERCASE = 'a';
        final char MAX_LOWERCASE = 'z';
        final char MIN_UPPERCASE = 'A';
        final char MAX_UPPERCASE = 'Z';

        if ((ch >= DIGITAL_ZERO && ch <= DIGITAL_NINE)
                || (ch >= MIN_LOWERCASE && ch <= MAX_LOWERCASE)
                || (ch >= MIN_UPPERCASE && ch <= MAX_UPPERCASE)) {
            return true;
        } else {
            return false;
        }
    }

    private static long[] DOWNHITS = new long[2];

    /**
     * 多点击事件
     *
     * @return
     */
    public static boolean isMultiClick() {
        System.arraycopy(DOWNHITS, 1, DOWNHITS, 0, DOWNHITS.length - 1);
        DOWNHITS[DOWNHITS.length - 1] = SystemClock.uptimeMillis();
        if (DOWNHITS[0] >= (SystemClock.uptimeMillis() - 500)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNetWorkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return (info != null && info.isAvailable());
    }

    public static int getWindowWidth(Context context) {
        DisplayMetrics metrics = getMetrics(context);
        return metrics.widthPixels;
    }

    public static int getWindowHeight(Context context) {
        int statusBarHeight = getStatusBarHeight(context);
        DisplayMetrics metrics = getMetrics(context);
        return metrics.heightPixels - statusBarHeight;
    }

    public static DisplayMetrics getMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return statusBarHeight;
    }

    public static boolean hasSmartBar() {
        try {
            // 新型号可用反射调用Build.hasSmartBar()
            Method method = Class.forName("android.os.Build").getMethod(
                    "hasSmartBar");
            return ((Boolean) method.invoke(null)).booleanValue();
        } catch (Exception e) {

        }
        // 反射不到Build.hasSmartBar()，则用Build.DEVICE判断
        if (Build.DEVICE.equals("mx2")) {
            return true;
        } else if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
            return false;
        }
        return false;
    }

    /**
     * 直接分享图片到微信,后门 通过查看具体requestCode可以得知有没有分享成功?
     */
    public static void ShareToWXTimeline(Activity activity, File shareFile, String content, int requestCode) throws ActivityNotFoundException {
        if (shareFile == null || !shareFile.exists())
            return;
        Uri uri = Uri.fromFile(shareFile);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI"));
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra("Kdescription", content);
        if (uri != null) {
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }
        activity.startActivityForResult(intent, requestCode);
    }


    public static void shareToWX(String title, int flag, String url, String description, byte[] data, IWXAPI mWXApi) {
        //WXTextObject textObject = new WXTextObject();
        //textObject.text = Constans.SHARE_DES;

        WXMediaMessage message = new WXMediaMessage();
        message.title = title;
        message.mediaObject = new WXWebpageObject(url);
        message.thumbData = data;
        message.description = description;
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
        req.transaction = "img" + System.currentTimeMillis();
        req.message = message;
        mWXApi.sendReq(req);
    }

    public static void shareToQQZone(final Activity activity, File shareFile,
                                     String title, String content, String url, final Handler mHandler) {
        Tencent mTencent;
        mTencent = Tencent.createInstance(Constans.QQ_APP_ID, activity);
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, content);
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, url);
        ArrayList<String> imageUrls = new ArrayList<String>();
        // imageUrls.add("http://imgcache.qq.com/qzone/space_item/pre/0/66768.gif");
        imageUrls.add(shareFile.getAbsolutePath());
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
        mTencent.shareToQzone(activity, params, new IUiListener() {
            @Override
            public void onCancel() {
                // PPAlert.showToast(DoTheTaskActivity.this,
                // "qq zone share onCancel");
            }

            @Override
            public void onError(UiError e) {
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(WildcatShareAlertActivity.SHARE_S_QQZONE_FAILE);
                } else {
                    if (activity != null && !activity.isFinishing()) {
                        Toast.makeText(activity, "分享错误", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onComplete(Object response) {
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(WildcatShareAlertActivity.SHARE_S_QQZONE_SUCCESS);
                } else {
                    if (activity != null && !activity.isFinishing()) {
                        Toast.makeText(activity, "分享成功", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public static void shareToWeibo(Activity activity, IWeiboShareAPI mWeiboShareAPI, String share_des,
                                    Bitmap bitmap) {
//        if (mWeiboShareAPI.checkEnvironment(true)) {
//        }
        mWeiboShareAPI.registerApp();
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        TextObject textObject = new TextObject();
        textObject.text = share_des;
        weiboMessage.textObject = textObject;

        ImageObject imageObject = new ImageObject();
        imageObject.setImageObject(bitmap);

        weiboMessage.imageObject = imageObject;
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;
        mWeiboShareAPI.sendRequest(activity, request);
    }

    public static void showSoftInput(Context context) {
        try {
            InputMethodManager inputMeMana = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMeMana.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
        }
    }

    public static void hideSoftInput(EditText view, Context context) {
        try {
            InputMethodManager inputMeMana = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMeMana.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideSoftKeyBoard(Activity context) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(context
                            .getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final SpannableString getClauselinks(final Context context, String clause_text, int index) {
        SpannableString spannableString = new SpannableString(clause_text);
        spannableString.setSpan(new URLSpan(Constans.scheme_host + Constans.CLAUSE_LINK) {

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(context.getResources().getColor(R.color.c_pw_main));
                ds.setUnderlineText(false);
            }

        }, index, clause_text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }


    /**
     * 获取当前程序的版本名称
     */
    public static String getVersionName(Context c) {
        PackageInfo packInfo = null;
        try {
            packInfo = c.getPackageManager().getPackageInfo(c.getPackageName(),
                    0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return (packInfo != null ? packInfo.versionName : "");
    }


    /**
     * version code
     *
     * @param c
     * @return
     */
    public static String getVersionCode(Context c) {
        PackageInfo packInfo = null;
        try {
            if (c == null || c.getPackageManager() == null) {
                return "";
            }
            packInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return (packInfo != null ? String.valueOf(packInfo.versionCode) : "");
    }

    public static String getChannel(Context context) {
        try {
            if (context == null || context.getPackageManager() == null)
                return "";
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.metaData.getString("UMENG_CHANNEL");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isNeedPush(Context mContext) {
        String key = SharedPreferencesUtil.getStringExtra(mContext, Constans.SP_KEY_PUSH_STR, "");
        if (!TextUtils.isEmpty(key)) {
            try {
                JSONObject object = new JSONObject(key);
                boolean nopush = object.optBoolean("nopush");
                if (nopush) {
                    return false;
                } else {
                    boolean nodisturb = object.optBoolean("nodisturb");
                    if (nodisturb) {
                        int startTime = object.optInt("time_start");
                        int endTime = object.optInt("time_end");

                        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
                        c.setTime(new Date(System.currentTimeMillis()));
                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        if (startTime < endTime) {
                            if (hour >= startTime && hour < endTime) {
                                return false;
                            }
                        } else {
                            if (hour >= startTime || hour < endTime) {
                                return false;
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean isOnForeground(Context context) {
        List<ActivityManager.RunningTaskInfo> taskInfos = getTaskInfos(context,
                2);
        if (taskInfos == null || taskInfos.isEmpty())
            return false;
        ActivityManager.RunningTaskInfo runningTaskInfo = taskInfos.get(0);
        ComponentName topActivity = runningTaskInfo.topActivity;
        if (context.getPackageName().equals(topActivity.getPackageName())) {
            return true;
        }
        return false;
    }

    private static List<ActivityManager.RunningTaskInfo> getTaskInfos(Context context,
                                                                      int maxNum) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfos = activityManager
                .getRunningTasks(maxNum);
        if (taskInfos == null || taskInfos.isEmpty())
            return null;
        return taskInfos;
    }

    public static Drawable getCompoundDrawable(int resId, Context context) {
        Drawable iconDrawable = context.getResources().getDrawable(resId);
        iconDrawable.setBounds(0, 0, iconDrawable.getMinimumWidth(),
                iconDrawable.getMinimumHeight());
        return iconDrawable;
    }

    public static String getPhoneCode(String phoneNum) {
        if (TextUtils.isEmpty(phoneNum)) return "86";
        int index = phoneNum.indexOf(":");
        return index > 0 ? phoneNum.substring(0, index) : "86";
    }

    public static String getRealPhone(String phoneNum) {
        if (TextUtils.isEmpty(phoneNum)) return "";
        int index = phoneNum.indexOf(":");
        return index > 0 ? phoneNum.substring(index + 1) : phoneNum;
    }

    public static String getFormatPhoneNo(String mPhoneCode, String mPhone) {
        if (TextUtils.isEmpty(mPhoneCode)) mPhoneCode = "86";
        return String.format("%s:%s", mPhoneCode, mPhone);
    }


    public static int getFaceSizeFromScreen(Context context) {
        int width = getWindowWidth(context);
        int faceSize;
        if (width <= 240) {
            faceSize = 22;
        } else if (width <= 320) {
            faceSize = 28;
        } else if (width <= 480) {
            faceSize = 33;
        } else if (width <= 640) {
            faceSize = 36;
        } else if (width <= 720) {
            faceSize = 42;
        } else if (width <= 1080) {
            faceSize = 62;
        } else {
            faceSize = 70;
        }
        return faceSize;
    }

    public static String getCountryForPhoneCode(Context context, String phoneCode) {
        if (TextUtils.isEmpty(phoneCode)) return "中国";
        InputStream ins = null;
        InputStreamReader inReader = null;
        BufferedReader reader = null;
        try {
            ins = context.getAssets().open("txt/p_code.txt");
            inReader = new InputStreamReader(ins, "utf-8");
            reader = new BufferedReader(inReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONObject o = new JSONObject(sb.toString());
            Iterator<?> it = o.keys();
            while (it.hasNext()) {
                JSONArray array = o.getJSONArray((String) it.next());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject oo = array.getJSONObject(i);
                    if (phoneCode.equals(oo.optString("dialingcode"))) {
                        return oo.optString("country_name_cn");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (inReader != null)
                    inReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (ins != null)
                    ins.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }


    public static int getResId(Context context, String name, String defType) {
        String packageName = context.getApplicationInfo().packageName;
        return context.getResources().getIdentifier(name, defType, packageName);
    }

    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * 上传到服务器，并将文件复制到已上传的文件夹中
     *
     * @param
     */
    public synchronized static void upLoadToServer() {
        CustomLog.d("(synchoronized) uploadToServer.");
        if (PeiwoApp.getApplication().getNetType() == NetUtil.NO_NETWORK) {
            return;
        }
        File baseFile = new File(Environment.getExternalStorageDirectory(), "peiwo/crash");
        if (!baseFile.exists()) {
            return;
        }
        File[] files = baseFile.listFiles();
        InputStream is = null;
        BufferedReader reader = null;
        InputStreamReader inputReader = null;
        StringBuffer sb = null;
        if (files != null && files.length > 0) {
            try {
                for (final File crashFile : files) {
                    is = new FileInputStream(crashFile);
                    inputReader = new InputStreamReader(is);
                    reader = new BufferedReader(inputReader);
                    sb = new StringBuffer();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

                    params.add(new BasicNameValuePair("content-length", sb.length() + ""));
                    params.add(new BasicNameValuePair("validation", Md5Util.MD5(sb.toString())));
                    params.add(new BasicNameValuePair("log", sb.toString()));
                    ApiRequestWrapper.openAPIPOST(PeiwoApp.getApplication(),
                            params, AsynHttpClient.API_REPORT_CRASH_LOG,
                            new MsgStructure() {
                                @Override
                                public void onReceive(JSONObject data) {
                                    moveToRecordedFile(crashFile);
                                    CustomLog.i("uploadToServer. data is : " + data);
                                }

                                @Override
                                public void onError(int error, Object ret) {
                                    System.out.println("error" + error);
                                }
                            });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputReader != null) {
                    try {
                        inputReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void moveToRecordedFile(File file) {
        InputStream input = null;
        OutputStream out = null;
        try {
            input = new FileInputStream(file);
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String strPath = Environment.getExternalStorageDirectory()
                        .toString();
                File dirRecordedBase = new File(strPath, "peiwo/recorded");
                if (!dirRecordedBase.exists()) {
                    dirRecordedBase.mkdirs();
                }
                File hasRecordePathCrashFile = new File(dirRecordedBase,
                        file.getName());
                out = new FileOutputStream(hasRecordePathCrashFile);
                byte[] bt = new byte[1024];
                int len = 0;
                while ((len = input.read(bt)) != -1) {
                    out.write(bt, 0, len);
                }
                file.delete(); // 删除该文件
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将文件转化为字节数组字符串，并对其进行Base64编码处理
     *
     * @param file
     * @return
     */
    public static String GetBase64Str(File file) {
        InputStream in = null;
        byte[] data = null;
        // 读取字节数组
        try {
            in = new FileInputStream(file);
            data = new byte[in.available()];
            in.read(data);
            in.close();
            return Base64.encodeToString(data, Base64.DEFAULT);// 返回Base64编码过的字节数组字符串
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 上传单个录音文件到服务器
     *
     * @param context
     * @param file
     */
    public static void uploadRecorde(final Context context, final File file) {
        String call_id = null;
        String caller_uid = null;
        String callee_uid = null;
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        final String fileName = file.getName();
        params.add(new BasicNameValuePair("filename", fileName));
        params.add(new BasicNameValuePair("filetype", "amr"));
        String fileSize = String.valueOf(file.length());
        params.add(new BasicNameValuePair("filesize", fileSize));
        String name = file.getName();
        String[] names = name.split("_");
        if (names.length > 3) {
            call_id = names[0];
            caller_uid = names[1];
            callee_uid = names[2];
        } else {
            boolean b = file.delete();
            if (b) {
                System.out.println("文件名错误，删除文件成功");
            } else {
                System.out.println("文件名错误，删除文件失败");
            }
            return;
        }

        params.add(new BasicNameValuePair("callid", call_id));
        params.add(new BasicNameValuePair("caller_uid", caller_uid));
        params.add(new BasicNameValuePair("callee_uid", callee_uid));
        try {
            params.add(new BasicNameValuePair("filecontent_hash", Md5Util.md5Hex(file)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        params.add(new BasicNameValuePair("is_compress", "true"));

        params.add(new BasicNameValuePair("filetime", TimeUtil.getFileLastModifiedTime(file)));

        ApiRequestWrapper.openAPIGET(context, params, AsynHttpClient.API_GETUPLOADER, new MsgStructure() {

            @Override
            public void onReceive(JSONObject data) {
                if (data == null) return;
                System.out.println("获取录音HANDLER成功：" + data.toString());
                ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                try {
                    params.add(new BasicNameValuePair("filehandler", data.getString("filehandler")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                params.add(new BasicNameValuePair("filename", fileName));
                params.add(new BasicNameValuePair("filecontent", PWUtils.GetBase64Str(file)));
                ApiRequestWrapper.openAPIPOST(context, params, AsynHttpClient.API_POSTUPLOADER, new MsgStructure() {

                    @Override
                    public void onReceive(JSONObject data) {
                        System.out.println("上传录音成功");
                        if (file != null && file.exists()) {
                            boolean isDelete = file.delete();
                            if (isDelete) {
                                System.out.println("删除文件成功");
                            } else {
                                System.out.println("删除文件失败");
                            }
                        }
                    }

                    @Override
                    public void onError(int error, Object ret) {
                        System.out.println("上传录音失败");
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {
                System.out.println("获取录音HANDLER失败：" + error);
            }
        });
    }

    /**
     * 遍历文件，将所有文件上传到服务器
     *
     * @param context
     */
    public static void uploadRecordFiles(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String strPath = Environment.getExternalStorageDirectory().toString();
            File dirRecordedBase = new File(strPath, "pwrec");
            if (!dirRecordedBase.exists()) {
                return;
            }
            File[] files = dirRecordedBase.listFiles();
            PeiwoApp app = PeiwoApp.getApplication();

            for (File file : files) {
                if (app.getCallType() != PeiwoApp.CALL_TYPE.CALL_REAL && app.getCallType() != PeiwoApp.CALL_TYPE.CALL_WILD) {
                    uploadRecorde(context, file);
                }
            }
        }
    }

    public static void showWildcatShareActivity(Context mContext, boolean isAuto) {
        String data = SharedPreferencesUtil.getStringExtra(mContext,
                "wildcat_share_content" + UserManager.getUid(mContext), "");
        try {
            JSONObject obj = new JSONObject(data);
            int count = obj.optInt("count");
            if (count == 0 && isAuto) {
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(mContext, WildcatShareAlertActivity.class);
        mContext.startActivity(intent);
    }

    public static void getWildcatShareData(final Context mContext, MsgStructure structure) {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        if (structure == null) {
            structure = new MsgStructure() {
                @Override
                public void onReceive(JSONObject data) {
                    SharedPreferencesUtil.putStringExtra(mContext, "wildcat_share_content" + UserManager.getUid(mContext), data.toString());
                }

                @Override
                public void onError(int error, Object ret) {

                }
            };
        }
        ApiRequestWrapper.openAPIGET(mContext, params, AsynHttpClient.API_USERINFO_WILDCAT, structure);
    }

    public static boolean isOpenRecordPermission() {
        File file = new File(FileManager.getTempFilePath(), "audio_test.pw");
        MediaRecorder recorder = null;
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setAudioEncodingBitRate(8);
            recorder.setAudioSamplingRate(8000);
            recorder.setOutputFile(file.getAbsolutePath());
            recorder.prepare();// 准备录制
            recorder.start();// 开始录制
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (e.toString().contains("permission")) {
                return false;
            }
            return true;
        } finally {
            if (recorder != null) {
                try {
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            file.delete();
        }
    }


    public static boolean getCpuInfoIsArm() {
        String str1 = "/proc/cpuinfo";
        String str2;
        BufferedReader localBufferedReader = null;
        StringBuilder sb = new StringBuilder();
        String[] arrayOfString;
        try {
            FileReader fr = new FileReader(str1);
            localBufferedReader = new BufferedReader(fr, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            for (int i = 2; i < arrayOfString.length; i++) {
                sb.append(arrayOfString[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (localBufferedReader != null) {
                try {
                    localBufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String result = sb.toString();
        return result.contains("arm") || result.contains("ARM");
    }

    /**
     * String[0] 为关注的人数
     * String[1] 为粉丝的人数
     */
    public static void getUserInfo(final Context context) {
        ApiRequestWrapper.getUserInfo(context, UserManager.getUid(context), String.valueOf(UserManager.getUid(context)), new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                UserInfoEvent userEvent = new UserInfoEvent();
                userEvent.fans_number = data.optInt("fans");
                userEvent.follows_number = data.optInt("focuses");
                userEvent.complement = data.optString("complement");

                JSONArray array = data.optJSONArray("feeds");
                if (array != null && array.length() > 0) {
                    userEvent.showDynamic = true;
                }

                SharedPreferencesUtil.putIntExtra(context, AsynHttpClient.KEY_FANS_NUMBER, userEvent.fans_number);
                SharedPreferencesUtil.putIntExtra(context, AsynHttpClient.KEY_FOLLOWS_NUMBER, userEvent.follows_number);
                SharedPreferencesUtil.putStringExtra(context, AsynHttpClient.KEY_INFO_COMPLEMENT, userEvent.complement);
                SharedPreferencesUtil.putBooleanExtra(context, AsynHttpClient.KEY_INFO_SHOW_DYNAMIC, userEvent.showDynamic);
                EventBus.getDefault().post(userEvent);
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }


    /**
     * 获取图片高、宽等信息
     *
     * @param path
     * @return
     */
    public static int[] getImageUrl(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (bitmap == null) {
            System.out.println("bitmap为空");
        }
        int realWidth = options.outWidth;
        int realHeight = options.outHeight;
        int[] array = new int[2];
        array[0] = realWidth;
        array[1] = realHeight;

        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int degree = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (degree == ExifInterface.ORIENTATION_ROTATE_90
                    || degree == ExifInterface.ORIENTATION_ROTATE_270) {
                array[0] = realHeight;
                array[1] = realWidth;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return array;
    }

    public static String getDeviceId(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString();
    }

    public static String FormatHMS(int sec) {
        String mm = (sec % 3600) / 60 > 9 ? (sec % 3600) / 60 + "" : "0" + (sec % 3600) / 60;
        String ss = (sec % 3600) % 60 > 9 ? (sec % 3600) % 60 + "" : "0" + (sec % 3600) % 60;
        return mm + ":" + ss;
    }
}
