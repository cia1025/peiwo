package me.peiwo.peiwo.service;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.constans.PWActionConfig;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.PWDownloader;
import me.peiwo.peiwo.util.FileManager;
import me.peiwo.peiwo.util.Md5Util;
import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by fuhaidong on 14/11/12.
 */
public class SynchronizedService extends Service {

    private MsgDBCenterService dbCenterService;
    private Handler mHandler;

    public static final int WHAT_HTTP_ERROR = 1000;

    private NotificationManager notificationManager;
    private int notifyId;
    private CompositeSubscription mSubscriptions;
    private boolean is_downloading = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new MyHandler(this);
        dbCenterService = MsgDBCenterService.getInstance();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifyId = hashCode();
        mSubscriptions = new CompositeSubscription();
    }

    static class MyHandler extends Handler {
        WeakReference<SynchronizedService> service_ref;

        public MyHandler(SynchronizedService service) {
            service_ref = new WeakReference<SynchronizedService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            SynchronizedService theService = service_ref.get();
            if (theService == null) return;
            int what = msg.what;
            switch (what) {
                case MsgDBCenterService.WHAT_INSERT_REMARK_COMMPLETE:
                case MsgDBCenterService.WHAT_INSERT_REMARK_ERROR:
                    Object o = msg.obj;
                    if (o != null && o instanceof JSONObject) {
                        PeiwoApp app = (PeiwoApp) theService.getApplicationContext();
                        app.fetchNoteMap((JSONObject) o);
                    }
                    theService.stopSelf();
                    break;

                case WHAT_HTTP_ERROR:
                    theService.stopSelf();
                    break;

            }
            super.handleMessage(msg);
        }
    }

    //同步联系人的备注信息
    private void synchronizedPWContactNote() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        //params.add(new BasicNameValuePair("tuid", String.valueOf(tUid)));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_CONTACT_NOTE_LIST, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
            }

            @Override
            public boolean onInterceptRawData(String rawStr) {
                //Trace.i("note == " + rawStr);
                //{"code": 0, "data": {"865641": "\u65b0\u897f\u51702", "848974": "\u54af\u83ab"}}
                JSONObject oo = null;
                try {
                    if (!TextUtils.isEmpty(rawStr)) {
                        JSONObject o = new JSONObject(rawStr);
                        if (o.has("data"))
                            oo = o.getJSONObject("data");
                        else
                            oo = new JSONObject();
                        dbCenterService.insertPwRemark(oo, mHandler);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    Message message = mHandler.obtainMessage(MsgDBCenterService.WHAT_INSERT_REMARK_COMMPLETE, oo);
                    mHandler.sendMessage(message);
                }
                return true;
            }

            @Override
            public void onError(int error, Object ret) {
                mHandler.sendEmptyMessage(WHAT_HTTP_ERROR);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return 1;

        String strAction = intent.getAction();
        if (strAction == null)
            return 1;

        if (PWActionConfig.ACTION_SYNC_PWNOTE.equals(strAction)) {
            synchronizedPWContactNote();
        } else if (PWActionConfig.ACTION_DOWNLOAD_NEW_VER.equals(strAction)) {
            if (!is_downloading) {
                is_downloading = true;
                String downloadurl = intent.getStringExtra("download_url");
                downloadAPK(downloadurl);
            }
        }
        return START_NOT_STICKY;
    }

    private void downloadAPK(String downloadurl) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setContentTitle("下载陪我").setSmallIcon(android.R.drawable.stat_sys_download).setOngoing(true);
        ProgressUpdater progressUpdater = new ProgressUpdater();
        Subscription subscription = Observable.create(progressUpdater).distinct().filter(integer -> integer % 2 == 0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                builder.setSmallIcon(android.R.drawable.stat_sys_download_done).setOngoing(false).setContentText("下载成功").setProgress(0, 0, false);
                notificationManager.notify(notifyId, builder.build());
                File file = new File(FileManager.getTempFilePath(), String.format("%s.apk", Md5Util.getMd5code(downloadurl)));
                if (file.exists() && file.length() > 0)
                    startInstall(file);
                stopSelf();
            }

            @Override
            public void onError(Throwable e) {
                builder.setSmallIcon(android.R.drawable.stat_notify_error).setOngoing(false).setContentText("下载失败").setProgress(0, 0, false);
                notificationManager.notify(notifyId, builder.build());
                is_downloading = false;
                stopSelf();
            }

            @Override
            public void onNext(Integer integer) {
                if (integer > 100) integer = 100;
                builder.setContentText(String.format("%d/100", integer)).setProgress(100, integer, false);
                notificationManager.notify(notifyId, builder.build());
            }
        });
        mSubscriptions.add(subscription);
        File file = new File(FileManager.getTempFilePath(), String.format("%s.apk", Md5Util.getMd5code(downloadurl)));
        PWDownloader downloader = PWDownloader.getInstance();
        downloader.add(downloadurl, file, progressUpdater);
    }

    private class ProgressUpdater implements PWDownloader.ProgressListener, Observable.OnSubscribe<Integer> {
        private Subscriber<? super Integer> subscriber;

        @Override
        public void update(long totalBytesWritten, long limit, boolean done) {
            if (subscriber != null && !subscriber.isUnsubscribed()) {
                if (done) {
                    subscriber.onCompleted();
                } else {
                    int progress = (int) ((double) totalBytesWritten / (double) limit * 100.00f);
                    subscriber.onNext(progress);
                }
            }
        }

        @Override
        public void failure(Exception e) {
            if (subscriber != null && !subscriber.isUnsubscribed()) {
                subscriber.onError(e);
            }
        }

        @Override
        public void call(Subscriber<? super Integer> subscriber) {
            this.subscriber = subscriber;
        }
    }


    private void startInstall(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
    public void onDestroy() {
        if (mSubscriptions != null) mSubscriptions.unsubscribe();
        super.onDestroy();
    }
}
