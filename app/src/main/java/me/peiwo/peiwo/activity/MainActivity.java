package me.peiwo.peiwo.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.PWActionConfig;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.fragment.TabFindFragment;
import me.peiwo.peiwo.fragment.TabFriendFragment;
import me.peiwo.peiwo.fragment.TabWildcatFragment;
import me.peiwo.peiwo.service.SynchronizedService;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.SharedPreferencesUtil;
import me.peiwo.peiwo.util.UmengStatisticsAgent;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.DrawerContentView;
import me.peiwo.peiwo.widget.NavgationViewController;
import me.peiwo.peiwo.widget.TabBarViewController.OnTabChangedListener;

import java.lang.reflect.Field;
import java.util.List;

/**
 * modify
 *
 * @author Fuhai
 */
@SuppressLint("NewApi")
public class MainActivity extends PWPreCallingActivity implements
        OnPageChangeListener, OnTabChangedListener {

    public static final int REQ_USER_DETAIL = 110;
    public static final int REQ_SETTING = 11;
    public static final int RESULT_LOGOUT = 100;

    private static final int MAX_TAB_COUNT = 4;
    private static final int TAB_WILDCAT = 0;
    private static final int TAB_MESSAGE = 2;
    public static final int TAB_FRIENDS = 3;
    private static final int TAB_ONLINE = 1;

    private static final int HANDLE_CHECK_OPEN_RECORD = 0x01;
    private static final int REQUEST_CODE_RECOMMFILTER = 6001;
    //private static final long MAX_RELOAD_MESSAGE_DELAY = 5000;

    //private TabBarViewController tabbar;
    private ViewPager vp_container;
    //private TextView tv_badge;


    private BroadcastReceiver receiver;

    //public static boolean isStart = false;


    private DrawerLayout drawer_layout;
    private NavgationViewController navgationViewController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setUpXGPush();
//        setUpHWPush();
        UmengStatisticsAgent.update(this);

        Intent intent = new Intent(MainActivity.this, SynchronizedService.class);
        intent.setAction(PWActionConfig.ACTION_SYNC_PWNOTE);
        startService(intent);

        if (PeiwoApp.getApplication().wildCatCallNotification) {
            PeiwoApp.getApplication().wildCatCallNotificationReStart = false;
            //如果限时聊在通知栏显示时，需要启动软件时默认进入时需要调用
            Intent callIntent = new Intent(this, WildCatCallActivity.class);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(callIntent);
            //tabbar.changeStatus(1);
            vp_container.setCurrentItem(0);
        }
        if (PeiwoApp.getApplication().realCallNotification) {
            //如果通话在通知栏显示时，需要启动软件时默认进入时需要调用
            PeiwoApp.getApplication().realCallNotificationReStart = false;
            Intent callIntent = new Intent(this, RealCallActivity.class);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(callIntent);
        }
        PWUtils.getUserInfo(this);
        PeiwoApp.getApplication().mExecutorService.execute(() -> {
            boolean isOldUser = SharedPreferencesUtil.getBooleanExtra(this, "old_user_" + UserManager.getUid(this), true);
            if (isOldUser) {
                SharedPreferencesUtil.putBooleanExtra(this, "old_user_" + UserManager.getUid(this), false);
                MsgDBCenterService.getInstance().insertSystemMessage();
            }
        });
        mHandle.sendEmptyMessageDelayed(HANDLE_CHECK_OPEN_RECORD, 1000);
    }

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_CHECK_OPEN_RECORD:
                    boolean isOpen = SharedPreferencesUtil.getBooleanExtra(MainActivity.this, "is_open_record_permission", false);
                    if (!isOpen) {
                        if (PWUtils.isOpenRecordPermission()) {
                            SharedPreferencesUtil.putBooleanExtra(MainActivity.this, "is_open_record_permission", true);
                        } else {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("检测到麦克风没有声音，请尝试在手机“设置->系统/安全->应用->陪我->权限->录音”中开启权限")
                                    .setPositiveButton("知道了", null).create().show();
                        }
                    }
                    break;
            }
        }
    };


    @Override
    protected void onStart() {
        //isStart = true;
        //ApiRequestWrapper.resolveXML(this);
        super.onStart();
    }

    @Override
    public void onResume() {
        //TcpProxy.getInstance().requestFriendPubFlow();
//        int likeCount = SharedPreferencesUtil.getIntExtra(PeiwoApp.getApplication(),
//                "like_num_" + UserManager.getUid(PeiwoApp.getApplication()), 0);
        //showLikeReadPonit(likeCount);
        super.onResume();
        updateNavgationViewControllerUI();
    }

    public void updateNavgationViewControllerUI() {
        navgationViewController.updateUI();
    }

    @Override
    protected void onRestart() {
        if (PeiwoApp.getApplication().wildCatCallNotification
                && PeiwoApp.getApplication().wildCatCallNotificationReStart) {
            PeiwoApp.getApplication().wildCatCallNotificationReStart = false;
            //如果限时聊在通知栏显示时，需要启动软件时默认进入时需要调用
            Intent callIntent = new Intent(this, WildCatCallActivity.class);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(callIntent);
        } else if (PeiwoApp.getApplication().realCallNotification
                && PeiwoApp.getApplication().realCallNotificationReStart) {
            //如果通话在通知栏显示时，需要启动软件时默认进入时需要调用
            PeiwoApp.getApplication().realCallNotificationReStart = false;
            Intent callIntent = new Intent(this, RealCallActivity.class);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(callIntent);
        }
        super.onRestart();
    }

    private void setUpXGPush() {
        if (MiPushClient.shouldUseMIUIPush(this)) {
            // 小米机型
            initXiaoMiPush();
            return;
        } else {
            // 开启logcat输出，方便debug，发布时请关闭
            XGPushConfig.enableDebug(this, false);
            // 如果需要知道注册是否成功，请使用registerPush(getApplicationContext(), XGIOperateCallback)带callback版本
            // 如果需要绑定账号，请使用registerPush(getApplicationContext(),"account")版本
            // 具体可参考详细的开发指南
            // 传递的参数为ApplicationContext
            XGPushManager.registerPush(getApplicationContext());
//            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            mNotifyMgr.cancel(Constans.NOTIFY_ID_MESSAGE);
        }
    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }

    private void initView() {
        setContentView(R.layout.fragment_tabs_pager);
        //tabbar = (TabBarViewController) findViewById(R.id.tabbar);
        //tabbar.setOnTabChangedListener(this);
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //View ll_drawer_view = findViewById(R.id.ll_drawer_view);
        //setDrawerViewSize(ll_drawer_view);
        DrawerContentView drawerContentView = (DrawerContentView) findViewById(R.id.drawer_content_view);
        drawerContentView.setDrawerLayout(drawer_layout);
        setDrawerLayoutEdgeSize(false);
        vp_container = (ViewPager) findViewById(R.id.vp_container);
        vp_container.setOffscreenPageLimit(MAX_TAB_COUNT);
        vp_container.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), getIntent()));
        vp_container.addOnPageChangeListener(this);
        if (getIntent().getIntExtra("action", -1) == Constans.ACTION_FLAG_WILDCAT) {
            vp_container.setCurrentItem(TAB_WILDCAT, false);
        } else if (getIntent().getIntExtra("action", -1) == Constans.ACTION_FLAG_MESSAGE) {
            vp_container.setCurrentItem(TAB_MESSAGE, false);
        }
        navgationViewController = (NavgationViewController) findViewById(R.id.navgationViewController);
        navgationViewController.setViewPager(vp_container);
        //tv_badge = (TextView) findViewById(R.id.tv_badge);

        if (UserManager.isLogin(this)) {
            receiver = new MessageReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(PWActionConfig.ACTION_LOGIN_OUT);
            filter.addAction(PWActionConfig.ACTION_ALERT_SHARE);
            registerReceiver(receiver, filter);
        }
        PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                PWUtils.upLoadToServer();
            }
        });
        /** 录音上传,v1.6.3屏蔽*/
        /*PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
            @Override
			public void run() {
				PWUtils.uploadRecordFiles(MainActivity.this);			
			}
		});*/
    }

    /**
     * 设置drawerView的大小，图片多大，就显示多大
     *
     * @param //ll_drawer_view
     */
//    private void setDrawerViewSize(View ll_drawer_view) {
//        DisplayMetrics metrics = PWUtils.getMetrics(this);
//        int h = metrics.heightPixels - PWUtils.getStatusBarHeight(this);
//        int w = 423 * h / 1294;
//        ViewGroup.LayoutParams params = ll_drawer_view.getLayoutParams();
//        params.width = w;
//        ll_drawer_view.setLayoutParams(params);
//    }


    class MyPagerAdapter extends FragmentPagerAdapter {
        private Intent intent;

        public MyPagerAdapter(FragmentManager fm, Intent intent) {
            super(fm);
            this.intent = intent;
        }

        @Override
        public Fragment getItem(int position) {
            return createItem(position);
        }

        @Override
        public int getCount() {
            return MAX_TAB_COUNT;
        }

        private Fragment createItem(int position) {
            switch (position) {
                case 0:
                    return TabWildcatFragment.newInstance();
                case 1:
                    return TabFindFragment.newInstance();
                case 2:
                    String suid = intent.getStringExtra("uid");
                    int msg_type = intent.getIntExtra("msg_type", 0);
                    if (intent.getIntExtra("action", -1) == Constans.ACTION_FLAG_MESSAGE) {
                        return TabMsgFragment.newInstance(suid, msg_type);
                    }
                    return TabMsgFragment.newInstance();
                case 3:
                    return TabFriendFragment.newInstance();
            }
            return null;
        }
    }

    private long exitTime = 0;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                drawer_layout.closeDrawer(GravityCompat.START);
                return true;
            }
            if (System.currentTimeMillis() - exitTime > 2000) {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
                return true;
            }
            sendBroadcast(new Intent(PWActionConfig.ACTION_FINISH_ALL).putExtra("finish_type", 0));
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int index) {
        if (index != TAB_FRIENDS) {
            String tag = makeFragmentName(vp_container.getId(), TAB_FRIENDS);
            Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
            if (f instanceof TabFriendFragment) {
                ((TabFriendFragment) f).hideSoftKeyBoard();
            }
        }
        //TcpProxy.getInstance().requestFriendPubFlow();
        //tabbar.changeStatus(position);

        if (index == TAB_MESSAGE) {
            MsgDBCenterService.getInstance().cancelIMNotification();
        }


//        if (index == 0 || index == 2) {
//            if (index == vp_container.getCurrentItem()) {
//                String tag = makeFragmentName(vp_container.getId(), index);
//                Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
//                if (f instanceof PPBaseFragment) {
//                    ((PPBaseFragment) f).scrollToTop();
//                }
//            }
//        }
//        vp_container.setCurrentItem(index, false);
//        if (index == 2) {
//            String tag = makeFragmentName(vp_container.getId(), index);
//            Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
//            if (f instanceof TabMsgAndFriendFragment) {
//                TabMsgAndFriendFragment tf = (TabMsgAndFriendFragment) f;
//                tf.setItem(lastPageIdx == index);
//            }
//            UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEME);
//        }
//        if (index == 3) {
//            UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEME);
//        } else if (index == 2) {
//            UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEMESSAGE);
//        }
//        lastPageIdx = index;
    }

    private String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }

    private int lastPageIdx = -1;

    @Override
    public void onTabChangedListener(int index) {
//        if (index == 0 || index == 2) {
//            if (index == vp_container.getCurrentItem()) {
//                String tag = makeFragmentName(vp_container.getId(), index);
//                Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
//                if (f instanceof PPBaseFragment) {
//                    ((PPBaseFragment) f).scrollToTop();
//                }
//            }
//        }
//        vp_container.setCurrentItem(index, false);
//        if (index == 2) {
//            String tag = makeFragmentName(vp_container.getId(), index);
//            Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
//            if (f instanceof TabMsgAndFriendFragment) {
//                TabMsgAndFriendFragment tf = (TabMsgAndFriendFragment) f;
//                tf.setItem(lastPageIdx == index);
//            }
//            UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEME);
//        }
//        if (index == 3) {
//            UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEME);
//        } else if (index == 2) {
//            UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEMESSAGE);
//        }
//        lastPageIdx = index;
    }

    public void updateMsgBadge(int num) {
        navgationViewController.setMessageBadge(num);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PWActionConfig.ACTION_LOGIN_OUT.equalsIgnoreCase(intent.getAction())) {
                PeiwoApp app = (PeiwoApp) getApplicationContext();
                if (app.getStartWelcome()) {
                    finish();
                    return;
                }
                Intent _intent = new Intent(MainActivity.this, WelcomeActivity.class);
                _intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(_intent);
                finish();
            } else if (PWActionConfig.ACTION_ALERT_SHARE.equalsIgnoreCase(intent.getAction())) {
                String tag = makeFragmentName(vp_container.getId(), 0);
                Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
                if (f instanceof TabWildcatFragment) {
                    PWUtils.showWildcatShareActivity(MainActivity.this, true);
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null) {
            //Trace.i("action == "+intent.getIntExtra("action", -1));
            if (intent.getIntExtra("action", -1) == Constans.ACTION_FLAG_MESSAGE) {
                String extra = intent.getStringExtra("uid");
                if (TextUtils.isEmpty(extra) || extra.equalsIgnoreCase("null")) {
                    vp_container.setCurrentItem(TAB_MESSAGE, false);
                    return;
                }
                if (TabMsgFragment.GROUP_MESSAGE == intent.getIntExtra("msg_type", 0)) {
                    handleInGroupChatActivity(intent.getStringExtra("uid"));
                } else {
                    int uid = Integer.valueOf(intent.getStringExtra("uid"));
                    if (uid > 0) {
                        handleInChatActivity(uid);
                    } else {
                        vp_container.setCurrentItem(TAB_MESSAGE, false);
                    }
                }
            } else if (intent.getIntExtra("action", -1) == Constans.ACTION_FLAG_WILDCAT) {
                vp_container.setCurrentItem(TAB_WILDCAT, false);
            } else if (intent.getIntExtra("current_item", -1) >= 0) {
                int current_item = intent.getIntExtra("current_item", -1);
                if (current_item >= 0 && current_item < MAX_TAB_COUNT) {
                    vp_container.setCurrentItem(current_item, false);
                }
            }
        }
    }

    private void handleInGroupChatActivity(String uid) {
        String tag = makeFragmentName(vp_container.getId(), TAB_MESSAGE);
        Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
        if (f != null && f instanceof TabMsgFragment) {
            ((TabMsgFragment) f).inGroupChatActivity(uid);
        }
    }

    private void handleInChatActivity(int uid) {
        String tag = makeFragmentName(vp_container.getId(), TAB_MESSAGE);
        Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
        if (f != null && f instanceof TabMsgFragment) {
            ((TabMsgFragment) f).inChatActivity(uid);
        }
    }


    
    
	/*private String getMobileBRAND(){
        Build bd = new Build();
		String model = bd.BRAND;
		return model;
	}*/

    private void initXiaoMiPush() {
        /*注册小米推送*/
        if (shouldInit()) {
            MiPushClient.registerPush(this, Constans.XIAOMI_APP_ID, Constans.XIAOMI_APP_KEY);
        }
        /*小米推送Logcat调试日志*/
        LoggerInterface newLogger = new LoggerInterface() {
            @Override
            public void setTag(String tag) {
                // ignore
            }

            @Override
            public void log(String content, Throwable t) {
                Log.d("PeiwoApp", content, t);
            }

            @Override
            public void log(String content) {
                Log.d("PeiwoApp", content);
            }
        };
        Logger.setLogger(this, newLogger);
    }

    /**
     * 这个方法保证初始化只在主进程调用，防止多个进程同时调用初始化方法导致的问题
     *
     * @return
     */
    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }


    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.v_drawer_indi:
                if (!drawer_layout.isDrawerOpen(GravityCompat.START)) {
                    openDrawer();
                }
                break;

            default:
                break;
        }
    }

    public void openDrawer() {
        drawer_layout.openDrawer(GravityCompat.START);
    }


    private void setDrawerLayoutEdgeSize(boolean really) {
        if (!really) return;
        //有bug
        try {
            // find ViewDragHelper and set it accessible
            Field leftDraggerField = drawer_layout.getClass().getDeclaredField(
                    "mLeftDragger");
            leftDraggerField.setAccessible(true);
            ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField
                    .get(drawer_layout);
            // find edgesize and set is accessible
            Field edgeSizeField = leftDragger.getClass().getDeclaredField(
                    "mEdgeSize");
            edgeSizeField.setAccessible(true);
            int edgeSize = edgeSizeField.getInt(leftDragger);
            // set new edgesize
            Point displaySize = new Point();
            getWindowManager().getDefaultDisplay()
                    .getSize(displaySize);
            edgeSizeField.setInt(leftDragger, Math.max(edgeSize,
                    (int) (displaySize.x * 0.5f)));
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            // ignore
        }
    }

    public void startRecommentFilter() {
        startActivityForResult(new Intent(this, RecommendUserFilterActivity.class), REQUEST_CODE_RECOMMFILTER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_RECOMMFILTER:
                    String tag = makeFragmentName(vp_container.getId(), TAB_ONLINE);
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
                    if (fragment instanceof TabFindFragment) {
                        ((TabFindFragment) fragment).refreshData();
                    }
                    break;

                default:
                    break;
            }
        }
    }

    public int getCurrentFragmentIndex() {
        return vp_container.getCurrentItem();
    }

    public void setNavgationViewOnlineStatus(int mask, boolean price_on) {
        navgationViewController.updateUI(mask, price_on);
    }
}
