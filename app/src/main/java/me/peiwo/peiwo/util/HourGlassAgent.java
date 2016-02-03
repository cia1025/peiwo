package me.peiwo.peiwo.util;

import android.content.Context;

/**
 * Created by fuhaidong on 15/12/24.
 */
public class HourGlassAgent {
    private boolean isStatistics = false;
    public static final String K_HAS_STAT = "hour";

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

    public int k1;
    public int k2;
    public int k3;
    public int k4;
    public int k5;
    public int k6;
    public int k7;
    public int k8;
    public int k9;
    public int k10;
    public int k11;
    public int k12;
    public int k13;
    public int k14;
    public int k15;
    public int k16;
    public int k17;
    public int k18;
    public int k19;
    public int k20;
    public int k21;
    public int k22;
    public int k23;
    public int k24;
    public int k25;
    public int k26;
    public int k27;
    public int k28;
    public int k29;

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

}
