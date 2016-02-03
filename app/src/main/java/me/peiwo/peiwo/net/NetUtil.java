package me.peiwo.peiwo.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

/**
 * 连接管理器(提供判断网络是否连接、当前网络状态是否为WIFI公共函数)
 *
 * @author: Kevin
 * @version: 2011-12-1 下午06:48:38
 */
public class NetUtil {
    public static final int NO_NETWORK = 0;
    public static final int WIFI_NETWORK = 1;
    public static final int G2_NETWORK = 2;// edge means gprs
    public static final int G3_NETWORK = 3;
    public static final int G4_NETWORK = 4;

    /**
     * 判断当前网络状态是否为已连接状态.
     *
     * @param context
     * @return
     * @author: Kevin
     * @version: 2015-2-8
     */
    public static boolean checkNet(Context mContext) {
        boolean connected = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if (info != null && info.isConnected() &&
                        info.getState() == NetworkInfo.State.CONNECTED) {
                    connected = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            connected = false;
        }
        return connected;
    }

    /**
     * 获取网络类型
     *
     * @param mcontext
     * @return
     * @author: Kevin
     * @version: 2015-2-8
     */
    public static boolean isWifi(Context mcontext) {
        // 获取当前可用网络信息
        if (mcontext == null) return false;
        ConnectivityManager connMng = (ConnectivityManager) mcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMng == null) return false;
        NetworkInfo netInf = connMng.getActiveNetworkInfo();
        // 如果当前是WIFI连接
        return netInf != null && "WIFI".equals(netInf.getTypeName());
    }

    /**
     * @param context
     * @return 0(无网络)，1(wifi),2(gprs),3(3g)
     */
    public static final int getSelfNetworkType(Context mContext) {
        if (mContext == null) return NO_NETWORK; // 网络未连接时当无网络
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return NO_NETWORK;
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo == null) {
            if (Build.VERSION.SDK_INT > 19) {// 判断sdk大于等于5.0
                activeNetInfo = connectivityManager.getNetworkInfo(0);
                if (activeNetInfo != null) {
                    if ("Cellular".equalsIgnoreCase(activeNetInfo.getTypeName())) {
                        return G3_NETWORK;
                    }
                } else {
                    return NO_NETWORK;
                }
            }
        }

        int netSubtype = -1;
        if (activeNetInfo != null) {
            netSubtype = activeNetInfo.getSubtype();
        }
        if (activeNetInfo != null && activeNetInfo.isConnected()) {
            if ("WIFI".equalsIgnoreCase(activeNetInfo.getTypeName())) {// wifi
                return WIFI_NETWORK;
            } else if (activeNetInfo.getTypeName() != null) {// 3g,双卡手机有时为mobile2
                if (activeNetInfo.getTypeName().toLowerCase().contains("mobile")) {
                    if (netSubtype == TelephonyManager.NETWORK_TYPE_UMTS
                            || netSubtype == TelephonyManager.NETWORK_TYPE_EVDO_0
                            || netSubtype == TelephonyManager.NETWORK_TYPE_EVDO_A
                            || netSubtype == TelephonyManager.NETWORK_TYPE_EVDO_B
                            || netSubtype == TelephonyManager.NETWORK_TYPE_HSDPA
                            || netSubtype == TelephonyManager.NETWORK_TYPE_HSUPA
                            || netSubtype == TelephonyManager.NETWORK_TYPE_HSPA
                            // 4.0系统 H+网络为15 TelephonyManager.NETWORK_TYPE_HSPAP
                            || netSubtype == 15) {
                        return G3_NETWORK;
                    } else if (netSubtype == TelephonyManager.NETWORK_TYPE_LTE) {
                        return G4_NETWORK;
                    } else {
                        return G2_NETWORK;
                    }
                } else if ("Cellular".equalsIgnoreCase(activeNetInfo.getTypeName())) {
                    return G3_NETWORK;
                }
            }
        }
        return NO_NETWORK;// 网络未连接时当无网络
    }

    private static WifiManager.WifiLock mWifiLock = null;

    public static void lockWifi(Context context) {
        if (mWifiLock == null) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            mWifiLock = wifiManager.createWifiLock("peiwo");
            mWifiLock.setReferenceCounted(true);
        }
        if (!mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    public static void unLockWifi() {
        try {
            if (mWifiLock != null && mWifiLock.isHeld()) {
                mWifiLock.release();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }


}