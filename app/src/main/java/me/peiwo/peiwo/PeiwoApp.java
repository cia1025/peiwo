package me.peiwo.peiwo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.umeng.update.UmengUpdateAgent;
import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.MessageContent;
import io.rong.message.CommandMessage;
import me.peiwo.peiwo.activity.MsgAcceptedMsgActivity;
import me.peiwo.peiwo.activity.ServerDownActivity;
import me.peiwo.peiwo.activity.UpgradeAppActivity;
import me.peiwo.peiwo.activity.WelcomeActivity;
import me.peiwo.peiwo.callback.ReceiveRongMessageListener;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.PWActionConfig;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.db.BriteDBHelperHolder;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.db.PWConfig;
import me.peiwo.peiwo.exception.PWCrashHandler;
import me.peiwo.peiwo.model.GroupMessageModel;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.model.ServerInfo;
import me.peiwo.peiwo.model.groupchat.GroupMessageBaseModel;
import me.peiwo.peiwo.model.groupchat.GroupMessageTextModel;
import me.peiwo.peiwo.net.*;
import me.peiwo.peiwo.service.CoreService;
import me.peiwo.peiwo.service.NetworkConnectivityListener;
import me.peiwo.peiwo.service.NetworkConnectivityListener.NetworkCallBack;
import me.peiwo.peiwo.util.*;
import me.peiwo.peiwo.util.group.RongMessageParse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class PeiwoApp extends MultiDexApplication {

    private List<ReceiveRongMessageListener> receiveRongListeners;

    private static PeiwoApp mInstance;
    private boolean iscalling = false;
    private CALL_TYPE current_call_type = CALL_TYPE.CALL_NONE;
    /**
     * 记录是否需要启动软件直接进入限时聊界面标识
     */
    public boolean wildCatCallNotification = false;
    /**
     * 判断是否经过Home键退出限时聊
     */
    public boolean wildCatCallNotificationReStart = false;

    /**
     * 记录是否需要启动软件直接进入通话界面标识
     */
    public boolean realCallNotification = false;
    /**
     * 判断是否经过Home键退出通话
     */
    public boolean realCallNotificationReStart = false;

    private String available_http_host = "";

    /**
     * 线程池, 防止每次都会new Thread, 避免不必要的内在消耗
     */
    public ExecutorService mExecutorService = null;

    private NetworkConnectivityListener mNetChangeReceiver;
    /**
     * 0(无网络),1(wifi),2(gprs),3(3g),4(4g)
     **/
    private int netType = 0;

    //public boolean isDeubg = false;

    //清除图片资源，释放空间,图片资源超过1000的时候
    public void cleanImages() {
        // final long t1 = System.currentTimeMillis();
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                File file = ImageLoader.getInstance().getDiskCache().getDirectory();
                if (file.exists() && file.isDirectory() && file.list().length > 1000) {
                    ImageLoader.getInstance().clearDiskCache();
                }
            }
        });
    }

    private AtomicBoolean isstartwelcome = new AtomicBoolean(false);

    public void setStartWelcome(boolean b) {
        isstartwelcome.set(b);
    }

    public boolean getStartWelcome() {
        return isstartwelcome.get();
    }

    public enum CALL_TYPE {
        CALL_REAL,
        CALL_WILD,
        CALL_NONE
    }

    public void setCalling(boolean b, CALL_TYPE call_type) {
        iscalling = b;
        current_call_type = call_type;
    }

    public boolean getIsCalling() {
        return iscalling;
    }

    public CALL_TYPE getCallType() {
        return current_call_type;
    }

    public long sStartTime;

    private SparseArray<String> note_map;

    protected PWConfig m_oConfig = new PWConfig();

    public PWConfig GetPWConfig() {
        return m_oConfig;
    }


    @Override
    public void onCreate() {
        //System.out.println("PeiwoApp onCreate: " + strProName);
        super.onCreate();
//        LoadDebugParam();
        if (shouldInit()) {
//            LeakCanary.install(this);
            decideEnv();
            mExecutorService = Executors.newFixedThreadPool(8);
            mInstance = this;
            DebugConfig.getInstance().readConfig(this);
            if (!BuildConfig.DEBUG) {
                PWCrashHandler mCreahHandler = PWCrashHandler.getInstance();
                mCreahHandler.init(this);
            }

            addGlobalErrHandle();

            getUploadConfig();

            initImageLoader();
            UmengUpdateAgent.setUpdateCheckConfig(false);
            UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEAPPOPEN);

            note_map = new SparseArray<>();
            MsgDBCenterService service = MsgDBCenterService.getInstance();
            service.getAllRemarks(note_map);

            startService(new Intent(this, CoreService.class));
            TcpProxy.getInstance().bindCorService(this);
            AsynHttpClient.getInstance().getHttpServers(this);

            netType = NetUtil.getSelfNetworkType(this);
            mNetChangeReceiver = new NetworkConnectivityListener();
            mNetChangeReceiver.startListening(this);
            addNetworkCallBack(mNetworkCallBack);

            setUpRongCloud();

            setUpAtuserAndNodisturb();
        }
    }

    public void setUpAtuserAndNodisturb() {
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                if (UserManager.isLogin(PeiwoApp.this)) {
                    MsgDBCenterService.getInstance().setUpAtusersAndNodisturb();
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    private void getUploadConfig() {
        //获取漏斗统计开关
        HourGlassAgent hourGlass = HourGlassAgent.getInstance();
        File dbFile = getDatabasePath(PWDBConfig.DB_NAME_USER);
        if (dbFile != null && dbFile.exists()) {
            //Log.i("dbconfig", "exists");
            if (!hourGlass.getHasStatistics(this)) {
                hourGlass.setHasStatistics(this, true);
            }
            return;
        }
        ArrayList<NameValuePair> params = new ArrayList<>();
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_GETUPLOADCONFIG, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(object -> {
                    if (!hourGlass.getHasStatistics(PeiwoApp.this)) {
                        HourGlassAgent.getInstance().setHasStatistics(PeiwoApp.this, true);
                        boolean b = object.optInt("switch", 0) == 1;
                        CustomLog.d("getUploadConfig. data is : " + object);
                        hourGlass.setStatistics(b);
                        if (b) {
                            HourGlassAgent.getInstance().setK1(1);
                            postK("k1");
                        }
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }

    public void postK(String k) {
        postKV(k, null);
    }

    public void postKV(String k, String v) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(k, v == null ? "1" : v));
        ApiRequestWrapper.openAPIPOST(this, params, AsynHttpClient.API_HOURGLASS_UPLOADDATA, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                HourGlassAgent agent = HourGlassAgent.getInstance();
                agent.clearData();
            }

            @Override
            public void onError(int error, Object ret) {
                HourGlassAgent agent = HourGlassAgent.getInstance();
                agent.clearData();
            }
        });
    }

    private void setUpRongCloud() {
        try {
            RongIMClient.init(this);
            RongIMClient.setConnectionStatusListener(connectionStatus -> {
                if (RongIMClient.ConnectionStatusListener.ConnectionStatus.DISCONNECTED == connectionStatus) {
                    //dis connected
                    if (RongIMClient.getInstance().getCurrentConnectionStatus() != RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTING) {
                        if (BuildConfig.DEBUG) {
                            Log.i("rongs", "dis connected & re connected");
                        }
                        getRongCloudTokenAndConnect();
                    }
                } else if (RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED == connectionStatus) {
                    //connected
                    if (BuildConfig.DEBUG) {
                        Log.i("rongs", "connected");
                    }
                } else if (RongIMClient.ConnectionStatusListener.ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT == connectionStatus) {
                    // 被踢下线
                    Log.i("rongs", "KICKED_OFFLINE_BY_OTHER_CLIENT");
                } else if (RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTING == connectionStatus) {
                    //connecting
                    Log.i("rongs", "connecting");
                } else if (RongIMClient.ConnectionStatusListener.ConnectionStatus.NETWORK_UNAVAILABLE == connectionStatus) {
                    //网络不可用
                    Log.i("rongs", "NETWORK_UNAVAILABLE");
                } else if (RongIMClient.ConnectionStatusListener.ConnectionStatus.SERVER_INVALID == connectionStatus) {
                    //服务不可用
                    Log.i("rongs", "SERVER_INVALID");
                } else if (RongIMClient.ConnectionStatusListener.ConnectionStatus.TOKEN_INCORRECT == connectionStatus) {
                    //token 不正确
                    if (RongIMClient.getInstance().getCurrentConnectionStatus() != RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTING) {
                        Log.i("rongs", "token error & reconnect");
                        getRongCloudTokenAndConnect();
                    }
                }
            });
            RongIMClient.registerMessageType(GroupMessageModel.class);
            RongIMClient.registerMessageType(CommandMessage.class);
            RongIMClient.setOnReceivePushMessageListener(pushNotificationMessage -> {
                if (BuildConfig.DEBUG) Log.i("rongs", "rong push == " + pushNotificationMessage.getPushContent());
                return true;
            });
            RongIMClient.setOnReceiveMessageListener((message, i) -> {
                handleReceiveRongMessage(message, i);
                mergeRecentMessageList(message);
                return false;
            });

            getRongCloudTokenAndConnect();
        } catch (AnnotationNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void mergeRecentMessageList(io.rong.imlib.model.Message message) {
        if (BuildConfig.DEBUG) {
            Log.i("rongs", "loop == " + (Looper.myLooper() == Looper.getMainLooper()));
            if (message.getContent() instanceof GroupMessageModel) {
                String body = ((GroupMessageModel) message.getContent()).getBody();
                Log.i("rongs", "merge message == " + body);
            }
        }
        MessageContent content = message.getContent();
        if (content instanceof GroupMessageModel) {
            GroupMessageBaseModel baseModel = RongMessageParse.parseReceiveMessage(message, 0);
            if (baseModel != null) {
                if (baseModel instanceof GroupMessageTextModel && !MsgAcceptedMsgActivity.Uid.equals(baseModel.group.group_id)) {
                    MsgDBCenterService.getInstance().addAtUser((GroupMessageTextModel) baseModel);
                }
                MsgDBCenterService.getInstance().insertDialogsWithGroupchat(baseModel);
            }
        }
    }

    private void handleReceiveRongMessage(io.rong.imlib.model.Message message, int integer) {
        if (receiveRongListeners == null) {
            return;
        }
        for (ReceiveRongMessageListener listener : receiveRongListeners) {
            if (listener != null) {
                listener.onReceiveRongMessage(message, integer);
            }
        }
    }

    public void getRongCloudTokenAndConnect() {
        if (!UserManager.isLogin(this)) return;
        ArrayList<NameValuePair> params = new ArrayList<>();
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            String rong_appkey = appInfo.metaData.getString("RONG_CLOUD_APP_KEY");
            params.add(new BasicNameValuePair("appkey", rong_appkey));
            ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_RONG_CLOUD_TOKEN, new MsgStructure() {
                @Override
                public void onReceive(JSONObject data) {
                    //{"token":"DoHF6UCkb3nO+IyYC9guN1mJ1FqRvdy\/KFnXlf8x\/Pd8UM5HQwX9YEUpXlzPX4ox6oT9ZyvoLVmVG0E5DRMjeeyk37wWV2DY"}
                    if (BuildConfig.DEBUG) {
                        Log.i("rong", data.toString());
                    }
                    String rong_token = data.optString("token");
                    if (BuildConfig.DEBUG) {
                        Log.i("rong", "connectRongCloud rong_token == " + rong_token);
                    }
                    Observable.just(rong_token).observeOn(AndroidSchedulers.mainThread()).subscribe(PeiwoApp.this::connectRongCloud);
                }

                @Override
                public void onError(int error, Object ret) {
                    Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
                        if (integer == 10003)
                            Toast.makeText(PeiwoApp.this, "融云app key 错误", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void connectRongCloud(String rong_token) {
        if (RongIMClient.getInstance().getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTING) {
            return;
        }
        RongIMClient.connect(rong_token, new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                if (BuildConfig.DEBUG) {
                    Log.i("rong", "onTokenIncorrect");
                }
            }

            @Override
            public void onSuccess(String userid) {
                if (BuildConfig.DEBUG) {
                    Log.i("rong", "onSuccess userid == " + userid);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                if (BuildConfig.DEBUG) {
                    Log.i("rong", "onError==" + errorCode.getMessage());
                }
            }
        });
    }

    public void loadStartUpScreenImg() {
        if (UserManager.isLogin(this)) {
            //resolution	y	string	格式为 "widthxheight",例 "200x400"
            ArrayList<NameValuePair> params = new ArrayList<>();
            DisplayMetrics metrics = PWUtils.getMetrics(this);
            params.add(new BasicNameValuePair("resolution", metrics.widthPixels + "x" + (metrics.heightPixels - PWUtils.getStatusBarHeight(this))));
            ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_SETTING_SYSTEM, new MsgStructure() {
                @Override
                public void onReceive(JSONObject data) {
                    String startup_screen_url = data.optString("startup_screen_url");
                    SharedPreferencesUtil.putStringExtra(PeiwoApp.this, "startup_screen_url", startup_screen_url);
//                    File dest = new File(FileManager.getTempFilePath(), ImageUtil.STARTUP_SCREEN_PATH_NAME);
//                    PWDownloader.getInstance().add(startup_screen_url, dest, null);
                }

                @Override
                public void onError(int error, Object ret) {

                }
            });
        }
    }

    public boolean isOnLineEnv() {
        return SharedPreferencesUtil.getBooleanExtra(getApplicationContext(), Constans.SP_KEY_ONLINE_ENVIROMENT, false);
    }

    private void decideEnv() {
        boolean isOnlineChecked = isOnLineEnv();
        boolean isRelease = !isDebuggable();
        if (isOnlineChecked || isRelease) {
            goOnlineEnviroment();
        } else {
            goOfflineEnvironment();
        }
        CustomLog.i("decideEnv, Config is : " + m_oConfig);
    }

    private NetworkCallBack mNetworkCallBack = new NetworkCallBack() {
        public void getSelfNetworkType(int type) {
            if (netType != type) {
                setAvailable_http_host("");
            }
            setNetType(type);
        }
    };

    public int getNetType() {
        return netType;
    }

    public void setNetType(int netType) {
        this.netType = netType;
    }

    public void addNetworkCallBack(NetworkCallBack mNetworkCallBack) {
        mNetChangeReceiver.registerNetworkCallBack(mNetworkCallBack);
    }

    public void removeNetworkCallBack(NetworkCallBack mNetworkCallBack) {
        mNetChangeReceiver.unregisterNetworkCallBack(mNetworkCallBack);
    }


    public void setAvailable_http_host(String available_http_host) {
        this.available_http_host = available_http_host;
    }

    public String getAvailable_http_host() {
        if (available_http_host == null)
            return "";
        return available_http_host;
    }


    public void goOnlineEnviroment() {
        CustomLog.d("goOnLineEnviroment.");
        m_oConfig = new PWConfig();
        SharedPreferencesUtil.putBooleanExtra(getApplicationContext(), Constans.SP_KEY_ONLINE_ENVIROMENT, true);
        CustomLog.i("goOnlineEnv, Config is : " + m_oConfig);
    }

    public boolean goOfflineEnvironment() {
        CustomLog.d("goOffLineEnviroment.");
        boolean bRet = false;
        InputStream ins = null;
        InputStreamReader inReader = null;
        BufferedReader reader = null;
        try {
            ins = getAssets().open("txt/debug.txt");
            inReader = new InputStreamReader(ins, "utf-8");
            reader = new BufferedReader(inReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            List<ServerInfo> serverList = JSON.parseArray(sb.toString(), ServerInfo.class);
            CustomLog.d("serverList == " + serverList);
            for (ServerInfo server : serverList) {
                CustomLog.d("server is ： " + server);
                String strName = server.name;
                String strHost = server.host;
                int nPort = TextUtils.isEmpty(server.port) ? 0 : Integer.valueOf(server.port);
                String strType = server.type;
                if (strName.equals("srv_http")) {
                    m_oConfig.GetHTTPSvr().m_strHostName = strHost;
                    if (nPort != 0) m_oConfig.GetHTTPSvr().m_nPort = nPort;
                    m_oConfig.GetHTTPSvr().m_strHTTPType = strType;
                } else if (strName.equals("srv_tcp")) {
                    m_oConfig.GetTCPSvr().m_strHostName = strHost;
                    if (nPort != 0) m_oConfig.GetTCPSvr().m_nPort = nPort;
                }
            }
            SharedPreferencesUtil.putBooleanExtra(getApplicationContext(), Constans.SP_KEY_ONLINE_ENVIROMENT, false);
            bRet = true;
            CustomLog.i("goOfflineEnv, Config is : " + m_oConfig);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
        return bRet;
    }

    public void fetchNoteMap(JSONObject o) {
        if (note_map == null) {
            note_map = new SparseArray<String>();
        }
        note_map.clear();
        Iterator<String> it = o.keys();
        while (it.hasNext()) {
            String key = it.next();
            note_map.put(Integer.parseInt(key), o.optString(key, ""));
            //Trace.i("map note == " + o.optString(key, ""));
        }
    }

    public String getNoteByUid(int uid) {
        if (note_map == null || note_map.size() == 0)
            return "";
        return note_map.get(uid);
    }

    public void putNoteByUid(int uid, String note) {
        if (note_map != null) {
            note_map.put(uid, note);
        }
    }

    public void removeNoteByUid(int uid) {
        if (note_map != null) {
            note_map.remove(uid);
        }
    }

    public SparseArray<String> getNoteMap() {
        return note_map;
    }

    private void addGlobalErrHandle() {
        AsynHttpClient.getInstance().setGlobalErrHandler(new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.i("addGlobalErrHandle upgrade", data.toString());
                //需要更新
                int hide_dialog = data.optInt("hide_dialog");
                boolean forced = data.optBoolean("forced");
                int ver = data.optInt("version");

                if (!forced) { // 不需要强制更新
                    if (hide_dialog != 1 && ver != SharedPreferencesUtil.getIntExtra(getApplicationContext(), "hide_dialog_ver", 0)) {
                        //有诱导更新
                        alertUpdate(data);
                        SharedPreferencesUtil.putIntExtra(getApplicationContext(), "hide_dialog_ver", ver);
                    }
                } else {// 需要强制更新
                    alertUpdate(data);
                }
                SharedPreferencesUtil.putIntExtra(PeiwoApp.this, Constans.SP_KEY_SERVER_APPVER, ver);
                SharedPreferencesUtil.putStringExtra(PeiwoApp.this, Constans.SP_KEY_UPGRADE_DATA, data.toString());
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("addGlobalErrHandle, onError. ret is : " + ret);
                AsynHttpClient.getInstance().clearAllRequest();
                restartApp(error, ret);
            }
        });
    }

    private void alertUpdate(final JSONObject data) {
        handler.removeMessages(0);
        Message msg = handler.obtainMessage();
        msg.what = 0;
        msg.obj = data;
        handler.sendMessageDelayed(msg, 3000);
    }


    public void toastOnUI(final int error) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (error == AsynHttpClient.ERR_USER_AUTH) {
                    showToast(getApplicationContext(), "用户认证失败，请重新登录");
                } else {
                    //showToast(getApplicationContext(), "我们正在升级服务器，请10分钟后再打开陪我使用");
                }
            }
        });
    }

    private Toast mToast = null;

    public void showToast(Context context, String msg) {
        if (TextUtils.isEmpty(msg) || context == null || (context instanceof Activity && ((Activity) context).isFinishing())) {
            return;
        }
        if (mToast == null) {
            mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        }
        mToast.setText(msg);
        mToast.show();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mNetChangeReceiver.unregisterNetworkCallBack(mNetworkCallBack);
        mNetChangeReceiver.stopListening();
        TcpProxy.getInstance().unbindCorService(this);
    }

    public void logoutLocalUser() {
        wildCatCallNotification = false;
        wildCatCallNotificationReStart = false;

        realCallNotification = false;
        realCallNotificationReStart = false;
        if (RongIMClient.getInstance() != null) RongIMClient.getInstance().logout();
        UserManager.clearUser(this);
        BriteDBHelperHolder dbHelperHolder = BriteDBHelperHolder.getInstance();
        dbHelperHolder.resetBriteDatebase();
        SharedPreferencesUtil.putStringExtra(this, Constans.SP_KEY_OPENID, "");
        SharedPreferencesUtil.putStringExtra(this, Constans.SP_KEY_OPENTOKEN, "");
        SharedPreferencesUtil.putIntExtra(this, Constans.SP_KEY_SOCIALTYPE, -1);
        TcpProxy.getInstance().closeRTC();
        HourGlassAgent hourGlass = HourGlassAgent.getInstance();
        hourGlass.setStatistics(false);
    }


    private void initImageLoader() {
        int memoryCacheSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            int memClass = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
            memoryCacheSize = (memClass / 8) * 1024 * 1024;
        } else {
            memoryCacheSize = 2 * 1024 * 1024;
        }
        //.discCacheFileNameGenerator(new Md5FileNameGenerator())
        DisplayImageOptions defaultDisplayImageOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_default_avatar).considerExifParams(true)
                .showImageForEmptyUri(R.drawable.ic_default_avatar).showImageOnFail(R.drawable.ic_default_avatar).cacheInMemory(true).cacheOnDisk(true).build();
        //.cacheInMemory(true)
        //.cacheOnDisk(true)

        File cacheDir = FileManager.getImagePath();
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this).threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSize(memoryCacheSize).denyCacheImageMultipleSizesInMemory().diskCache(new UnlimitedDiscCache(cacheDir))
                .tasksProcessingOrder(QueueProcessingType.LIFO) //.diskCacheSize(MAXDISCCACHESIZE)
                .defaultDisplayImageOptions(defaultDisplayImageOptions).build();
        ImageLoader.getInstance().init(configuration);
    }

    /**
     * 设置消息推送，勿扰
     */
    public void doSettingNoDistrub(int uid, int nopush, int nodisturb, String interval) {
        ApiRequestWrapper.settingNoDisturb(this, uid, nopush, nodisturb, interval, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {

            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }

    public void reportPushToken(String xg_token, int platform_type) {
        ApiRequestWrapper.reportPushToken(this, UserManager.getUid(this), xg_token, platform_type, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
            }

            @Override
            public void onError(int error, Object ret) {
            }
        });
    }

    public void initUserBackground() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_ACCOUNT_AUTOSIGNIN, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                //Trace.i("user data == " + data.toString());
                UserManager.saveUser(PeiwoApp.this, new PWUserModel(data));
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
        getTips();
    }

    private void getTips() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_REPORT_WARNING, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                if (data != null) {
                    String tips = data.optString("msg");
                    SharedPreferencesUtil.putStringExtra(PeiwoApp.this, Constans.SP_KEY_CIVTIPS, tips);
                }
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }

    public static synchronized PeiwoApp getApplication() {
        return mInstance;
    }

    public void restartApp(int nError, Object ret) {
        if (ret != null && ret instanceof JSONObject)
            alertServerDown(ret.toString());
        if (getStartWelcome()) {
            return;
        }
        CustomLog.d("restartApp. nError is : " + nError);
        sendBroadcast(new Intent(PWActionConfig.ACTION_FINISH_ALL).putExtra("finish_type", 1));
        toastOnUI(nError);
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(Constans.NOTIFY_ID_WILDCAT_BACKGROUND);
        mNotifyMgr.cancel(Constans.NOTIFY_ID_CALL_BACKGROUND);
        mNotifyMgr.cancel(Constans.NOTIFY_ID_IM_MESSAGE);

        Intent intent = null;
        if (nError < 0) {
//            intent.setClass(PeiwoApp.this, SplashActivity.class);
//            intent.setAction(PWActionConfig.ACTION_SERVER_DOWNTIME);
//            if (ret != null && ret instanceof JSONObject) {
//                intent.putExtra("data", ret.toString());
//            }
        } else {
            intent = new Intent();
            logoutLocalUser();
            intent.setClass(PeiwoApp.this, WelcomeActivity.class);
        }
        if (intent != null && isRunningForeground()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    private boolean showed = false;

    public void dissServerDownShowed() {
        showed = false;
    }

    public boolean isServerDown() {
        return showed;
    }

    private void alertServerDown(String json) {
        if (showed) return;
        showed = true;
        Observable.just(json).observeOn(AndroidSchedulers.mainThread()).subscribe(s -> {
            Intent intent = new Intent(this, ServerDownActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("data", s);
            startActivity(intent);
        });
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    if (existence_update_dialog) {
                        return;
                    }
                    if (isRunningForeground()) {
                        Intent intent = new Intent(PeiwoApp.this, UpgradeAppActivity.class);
                        intent.putExtra("data_str", msg.obj.toString());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
                break;
            }
        }
    };
    public boolean existence_update_dialog = false;


    public boolean isRunningForeground() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!TextUtils.isEmpty(currentPackageName)
                && currentPackageName.equals(getPackageName())) {
            return true;
        }
        return false;
    }


    //判断AndroidManifest.xml中isDebugable的值，与线上线下环境无关，如果想判断是线上环境，用isOnlineEnv()
    public boolean isDebuggable() {
//        boolean debuggable = false;
//        Context ctx = getApplicationContext();
//        PackageManager pm = ctx.getPackageManager();
//        try {
//            ApplicationInfo appinfo = pm.getApplicationInfo(ctx.getPackageName(), 0);
//            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
//        } catch (PackageManager.NameNotFoundException e) {
//        /*debuggable variable will remain false*/
//        }
//        CustomLog.d("isDebggable ? : " + debuggable);
//        return debuggable;
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.metaData.getBoolean("DEBUGGABLE", false);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void startCallActivity(Class<? extends Activity> clazz) {
        Intent intent = new Intent(this, clazz);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    public void addReceiveRongMessageListener(ReceiveRongMessageListener listener) {
        if (receiveRongListeners == null) {
            receiveRongListeners = new ArrayList<>();
        }
        receiveRongListeners.add(listener);
    }

    public void unRegisterReceiveRongMessageListener(ReceiveRongMessageListener listener) {
        if (receiveRongListeners != null && listener != null)
            receiveRongListeners.remove(listener);
    }


}


