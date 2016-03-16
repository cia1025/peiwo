package me.peiwo.peiwo.util;

import android.content.Context;

/**
 * Created by fuhaidong on 15/12/24.
 */
public class HourGlassAgent {
    private boolean isStatistics = false;
    public static final String K_HAS_STAT = "hour";
//    结束沙漏统计的3个条件：
//    1、进程被结束
//    2、第一次匿名聊挂断或接通
//    3、用户账号登出
    //本地保存已经统计过
    public void setHasStatistics(Context context, boolean b) {
        SharedPreferencesUtil.getBooleanExtra(context, K_HAS_STAT, b);
    }

    public boolean getHasStatistics(Context context) {
        return SharedPreferencesUtil.getBooleanExtra(context, K_HAS_STAT, false);
    }

    private static class SingletonHolder {

        public static final HourGlassAgent INSTANCE = new HourGlassAgent();
    }

    public static HourGlassAgent getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void setStatistics(boolean isStatistics) {
        this.isStatistics = isStatistics;
    }

    public boolean getStatistics() {
        return this.isStatistics;
    }

    private int k1; //激活app次数
    private int k2; //Loading页的打开次数
    private int k3; //第1张欢迎页打开次数（排重）
    private int k4; //第2张欢迎页打开次数（排重）
    private int k5; //第3张欢迎页打开次数（排重）
    private int k6; //登录页 打开次数
    private int k7; //登录页 点击“微信”的次数
    private int k8; //微信登录授权界面 点击“确认登录”的次数
    private int k9; //微信登录授权界面 点击取消的次数
    private int k10; //创建资料界面 点击下一步的次数
    private int k11; //创建资料界面 点击返回次数
    private int k12; //注册界面 点击获取验证码的次数
    private int k13; //注册界面 点击发送验证码的次数
    private int k14; //注册界面 点击完成的次数
    private int k15; //注册界面 点击重新获取次数
    private int k16; //注册界面 点击返回次数
    private int k17; //登录页 点击“登录”的次数
    private int k18; //登录界面 点击登录按钮次数
    private int k19; //登录界面 点击返回次数
    private int k20; //登录界面 点击qq次数
    private int k21; //登录界面 点击微信次数
    private int k22; //登录界面 点击微博次数
    private int k23; //登录界面 点击忘记密码次数
    private int k24; //匿名聊界面 点击呼叫按钮次数（开始引导）
    private int k25; //匿名聊界面 引导完成次数
    private int k26; //匿名聊界面 引导完成用时（秒）
    private int k27; //匹配界面 接通次数
    private int k28; //匹配界面 点击挂断次数
    private int k29; //匹配界面 等待时间（秒）
    private int k30; //登录界面 微信登录成功的次数
    private int k31; //登录界面 QQ登录成功的次数
    private int k32; //登录界面 微博登录成功的次数
    private int k33; //登录界面 点击“登录遇到问题”的次数
    private int k34; //登录遇到问题页 点击“重置密码”的次数
    private int k35; //登录页 点击“注册”的次数
    private int k36; //设置账号密码页 点击“下一步”的次数
    private int k37; //设置账号密码页 点击“返回”的次数
    private int k38; //获取验证码页 点击“下一步”的次数
    private int k39; //获取验证码页 点击“返回”的次数
    private int k40; //获取验证码页 点击“重新获取”的次数
    private int k41; //获取验证码页 点击“收不到验证码”的次数
    private int k42; //收不到验证码页 点击“获取语音验证码”的次数 (预留)
    private int k43; //绑定第三方页 点击“微信”的次数
    private int k44; //微信授权页 微信允许授权的次数
    private int k45; //绑定第三方页 点击“QQ”的次数
    private int k46; //QQ授权页 QQ允许授权的次数
    private int k47; //绑定第三方页 点击“微博”的次数
    private int k48; //微博授权页 微博允许授权的次数
    private int k49; //绑定第三方页 点击“跳过”的次数
    private int k50; //资料完善页 点击“完成”的次数
    private int k51; //资料完善页 点击“返回”的次数

    public void setK1(int k1) {
        this.k1 = k1;
    }

    public void setK2(int k2) {
        this.k2 = k2;
    }

    public void setK3(int k3) {
        this.k3 = k3;
    }

    public void setK4(int k4) {
        this.k4 = k4;
    }

    public void setK5(int k5) {
        this.k5 = k5;
    }

    public void setK6(int k6) {
        this.k6 = k6;
    }

    public void setK7(int k7) {
        this.k7 = k7;
    }

    public void setK8(int k8) {
        this.k8 = k8;
    }

    public void setK9(int k9) {
        this.k9 = k9;
    }

    public void setK10(int k10) {
        this.k10 = k10;
    }

    public void setK11(int k11) {
        this.k11 = k11;
    }

    public void setK12(int k12) {
        this.k12 = k12;
    }

    public void setK13(int k13) {
        this.k13 = k13;
    }

    public void setK14(int k14) {
        this.k14 = k14;
    }

    public void setK15(int k15) {
        this.k15 = k15;
    }

    public void setK16(int k16) {
        this.k16 = k16;
    }

    public void setK17(int k17) {
        this.k17 = k17;
    }

    public void setK18(int k18) {
        this.k18 = k18;
    }

    public void setK19(int k19) {
        this.k19 = k19;
    }

    public void setK20(int k20) {
        this.k20 = k20;
    }

    public void setK21(int k21) {
        this.k21 = k21;
    }

    public void setK22(int k22) {
        this.k22 = k22;
    }

    public void setK23(int k23) {
        this.k23 = k23;
    }

    public void setK24(int k24) {
        this.k24 = k24;
    }

    public void setK25(int k25) {
        this.k25 = k25;
    }

    public void setK26(int k26) {
        this.k26 = k26;
    }

    public void setK27(int k27) {
        this.k27 = k27;
    }

    public void setK28(int k28) {
        this.k28 = k28;
    }

    public void setK29(int k29) {
        this.k29 = k29;
    }

    public void setK30(int k30) {
        this.k30 = k30;
    }

    public void setK31(int k31) {
        this.k31 = k31;
    }

    public void setK32(int k32) {
        this.k32 = k32;
    }

    public void setK33(int k33) {
        this.k33 = k33;
    }

    public void setK34(int k34) {
        this.k34 = k34;
    }

    public void setK35(int k35) {
        this.k35 = k35;
    }

    public void setK36(int k36) {
        this.k36 = k36;
    }

    public void setK37(int k37) {
        this.k37 = k37;
    }

    public void setK38(int k38) {
        this.k38 = k38;
    }

    public void setK39(int k39) {
        this.k39 = k39;
    }

    public void setK40(int k40) {
        this.k40 = k40;
    }

    public void setK41(int k41) {
        this.k41 = k41;
    }

    public void setK42(int k42) {
        this.k42 = k42;
    }

    public void setK43(int k43) {
        this.k43 = k43;
    }

    public void setK44(int k44) {
        this.k44 = k44;
    }

    public void setK45(int k45) {
        this.k45 = k45;
    }

    public void setK46(int k46) {
        this.k46 = k46;
    }

    public void setK47(int k47) {
        this.k47 = k47;
    }

    public void setK48(int k48) {
        this.k48 = k48;
    }

    public void setK49(int k49) {
        this.k49 = k49;
    }

    public void setK50(int k50) {
        this.k50 = k50;
    }

    public void setK51(int k51) {
        this.k51 = k51;
    }

    public int getK1() {
        return k1;
    }

    public int getK2() {
        return k2;
    }

    public int getK3() {
        return k3;
    }

    public int getK4() {
        return k4;
    }

    public int getK5() {
        return k5;
    }

    public int getK6() {
        return k6;
    }

    public int getK7() {
        return k7;
    }

    public int getK8() {
        return k8;
    }

    public int getK9() {
        return k9;
    }

    public int getK10() {
        return k10;
    }

    public int getK11() {
        return k11;
    }

    public int getK12() {
        return k12;
    }

    public int getK13() {
        return k13;
    }

    public int getK14() {
        return k14;
    }

    public int getK15() {
        return k15;
    }

    public int getK16() {
        return k16;
    }

    public int getK17() {
        return k17;
    }

    public int getK18() {
        return k18;
    }

    public int getK19() {
        return k19;
    }

    public int getK20() {
        return k20;
    }

    public int getK21() {
        return k21;
    }

    public int getK22() {
        return k22;
    }

    public int getK23() {
        return k23;
    }

    public int getK24() {
        return k24;
    }

    public int getK25() {
        return k25;
    }

    public int getK26() {
        return k26;
    }

    public int getK27() {
        return k27;
    }

    public int getK28() {
        return k28;
    }

    public int getK29() {
        return k29;
    }

    public int getK30() {
        return k30;
    }

    public int getK31() {
        return k31;
    }

    public int getK32() {
        return k32;
    }

    public int getK33() {
        return k33;
    }

    public int getK34() {
        return k34;
    }

    public int getK35() {
        return k35;
    }

    public int getK36() {
        return k36;
    }

    public int getK37() {
        return k37;
    }

    public int getK38() {
        return k38;
    }

    public int getK39() {
        return k39;
    }

    public int getK40() {
        return k40;
    }

    public int getK41() {
        return k41;
    }

    public int getK42() {
        return k42;
    }

    public int getK43() {
        return k43;
    }

    public int getK44() {
        return k44;
    }

    public int getK45() {
        return k45;
    }

    public int getK46() {
        return k46;
    }

    public int getK47() {
        return k47;
    }

    public int getK48() {
        return k48;
    }

    public int getK49() {
        return k49;
    }

    public int getK50() {
        return k50;
    }

    public int getK51() {
        return k51;
    }

    public void clearData() {
        //k1~k6, k27排重
//        k1 = 0;
//        k2 = 0;
//        k3 = 0;
//        k4 = 0;
//        k5 = 0;
//        k6 = 0;
        k7 = 0;
        k8 = 0;
        k9 = 0;
        k10 = 0;
        k11 = 0;
        k12 = 0;
        k13 = 0;
        k14 = 0;
        k15 = 0;
        k16 = 0;
        k17 = 0;
        k18 = 0;
        k19 = 0;
        k20 = 0;
        k21 = 0;
        k22 = 0;
        k23 = 0;
        k24 = 0;
        k25 = 0;
        k26 = 0;
//        k27 = 0;
        k28 = 0;
        k29 = 0;
        k30 = 0;
        k31 = 0;
        k32 = 0;
        k33 = 0;
        k34 = 0;
        k35 = 0;
        k36 = 0;
        k37 = 0;
        k38 = 0;
        k39 = 0;
        k40 = 0;
        k41 = 0;
        k42 = 0;
        k43 = 0;
        k44 = 0;
        k45 = 0;
        k46 = 0;
        k47 = 0;
        k48 = 0;
        k49 = 0;
        k50 = 0;
        k51 = 0;
    }
}
