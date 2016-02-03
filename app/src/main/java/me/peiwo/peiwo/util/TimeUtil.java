package me.peiwo.peiwo.util;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Time;
import me.peiwo.peiwo.R;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public class TimeUtil {

    private static final SimpleDateFormat DEFAULT_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat MSG_TIME_FORMAT = new SimpleDateFormat(
            "HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATE_WEEK_FORMATWITHTIE = new SimpleDateFormat(
            "E HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATE_WEEK_FORMAT = new SimpleDateFormat(
            "E", Locale.getDefault());
    private static final SimpleDateFormat DATE_MONTH_DAY_FORMAT = new SimpleDateFormat(
            "MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DATE_MONTH_DAY_FORMAT_TIME = new SimpleDateFormat(
            "MM-dd HH:mm", Locale.getDefault());
    private static final SimpleDateFormat BIRTHDAY_FORMAT = new SimpleDateFormat(
            "yyyy年M月d日", Locale.getDefault());

    private static final SimpleDateFormat BIRTHDAYV2_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd", Locale.getDefault());

    private static final Date NOW = new Date();
    public static final long YEAR_TIME_MILLIS = 31536000000L;

    public static String getAgeByBirthday(String birthday) {
        if (TextUtils.isEmpty(birthday) || "null".equalsIgnoreCase(birthday))
            return "0";
        long time = 0;
        try {
            time = ((NOW.getTime() - DEFAULT_FORMAT.parse(birthday).getTime()) / YEAR_TIME_MILLIS);
        } catch (ParseException e) {
            time = 0;
            e.printStackTrace();
        }
        return String.valueOf(time);
    }

    public static String getBirthdayDisplay(String birthday) {
        try {
            if (TextUtils.isEmpty(birthday)) {
                return "未填写";
            } else {
                return BIRTHDAY_FORMAT.format(DEFAULT_FORMAT.parse(birthday));
            }
        } catch (ParseException e) {
            return birthday;
        }
    }

    private static final long ONE_DAY = 86400000;
    private static final long TWO_DAY = 2 * ONE_DAY;
    private static final long ONE_WEEK = 7 * ONE_DAY;


    public static String getMsgTimeDisplay(String birthday, boolean showTime) {
        //long tt = System.currentTimeMillis();
        if (TextUtils.isEmpty(birthday))
            return "";
        try {
            Date bir = DEFAULT_FORMAT.parse(birthday);
            long t1 = BIRTHDAYV2_FORMAT.parse(birthday).getTime();
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, c.get(Calendar.YEAR));
            c.set(Calendar.MONTH, c.get(Calendar.MONTH));
            c.set(Calendar.DATE, c.get(Calendar.DAY_OF_MONTH));
            long t2 = c.getTimeInMillis();
            long temp = t2 - t1;
            if (temp <= ONE_DAY) {
                //Trace.i("time = "+(System.currentTimeMillis() - tt));
                return MSG_TIME_FORMAT.format(bir);
            } else if (temp <= TWO_DAY) { //temp > ONE_DAY &&
                //Trace.i("time = "+(System.currentTimeMillis() - tt));
                //return String.format(Locale.getDefault(), "昨天 %s", MSG_TIME_FORMAT.format(bir));
                if (showTime)
                    return String.format(Locale.getDefault(), "昨天 %s", MSG_TIME_FORMAT.format(bir));
                return "昨天";
            } else if (temp < ONE_WEEK) {
                //Date d = new Date();
                //d.setTime(bir.getTime());
                if (showTime)
                    return DATE_WEEK_FORMATWITHTIE.format(bir);
                return DATE_WEEK_FORMAT.format(bir);
            } else {
                //Trace.i("time = "+(System.currentTimeMillis() - tt));
                if (showTime)
                    return DATE_MONTH_DAY_FORMAT_TIME.format(bir);
                return DATE_MONTH_DAY_FORMAT.format(bir);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getMsgTimeDisplay(long time) {
        long stmp = System.currentTimeMillis() - time;
        if (stmp <= 60000) {
            return "刚刚";
        } else if (stmp <= 3600000) {
            return String.format(Locale.getDefault(), "%s分钟前", stmp / 60000L);
        } else if (stmp <= 86400000L) {
            return String.format(Locale.getDefault(), "%d小时前", stmp / 3600000L);
        } else if (stmp <= 2592000000L) {
            return String.format(Locale.getDefault(), "%d天前", stmp / 86400000L);
        } else if (stmp <= 62208000000L) {
            return String.format(Locale.getDefault(), "%d月前", stmp / 2592000000L);
        } else {
            return String.format(Locale.getDefault(), "%d年前", stmp / 31104000000L);
        }
    }


    public static String getSignInTime(String timeStr) {
        if (TextUtils.isEmpty(timeStr) || "1970-01-01 00:00:00".equals(timeStr))
            return "很久以前";
        try {
            Date date = DEFAULT_FORMAT.parse(timeStr);
            long time = date.getTime();
            return getMsgTimeDisplay(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean checkBirthday(int year, int monthOfYear, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        int fyear = c.get(Calendar.YEAR);
        if (year > fyear) {
            return false;
        } else if (year == fyear && monthOfYear > c.get(Calendar.MONTH)) {
            return false;
        } else if (year == fyear && monthOfYear == c.get(Calendar.MONTH) && dayOfMonth > c.get(Calendar.DAY_OF_MONTH)) {
            return false;
        }
        return true;
    }


    public static String getConstellation(String date) {
        if (TextUtils.isEmpty(date)) {
            return "";
        }
        try {
            Calendar cTime = Calendar.getInstance();
            cTime.setTime(DEFAULT_FORMAT.parse(date));
            int month = cTime.get(Calendar.MONTH);
            int day = cTime.get(Calendar.DAY_OF_MONTH);
            if (day < constellationEdgeDay[month]) {
                month = month - 1;
            }
            if (month >= 0) {
                return constellationArr[month];
            }
            return constellationArr[11];
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    //public static final long YEAR_TIME_MILLIS = 365L * 24L * 3600L * 1000L;
    public static final String[] constellationArr = {
            "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座",
            "天蝎座", "射手座", "摩羯座"
    };

    public static String getConstellationEnglish(String date) {
        try {
            Calendar cTime = Calendar.getInstance();
            cTime.setTime(DEFAULT_FORMAT.parse(date));
            int month = cTime.get(Calendar.MONTH);
            int day = cTime.get(Calendar.DAY_OF_MONTH);
            if (day < constellationEdgeDay[month]) {
                month = month - 1;
            }
            if (month >= 0) {
                return constellationArrEnglish[month];
            }
            return constellationArrEnglish[11];
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static final String[] constellationArrEnglish = {
            "Aquarius", "Pisces", "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra",
            "Scorpio", "Sagittarius", "Capricornus"};

    public static final int[] constellationEdgeDay = {
            20, 19, 21, 21, 21, 22, 23, 23, 23, 23, 22, 22
    };

    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder,
            Locale.getDefault());
    private static final Object[] sTimeArgs = new Object[5];

    public static String makeTimeString(Context context, long secs) {
        //String durationformat = context.getString(secs < 3600 ? R.string.durationformatshort : R.string.durationformatlong);
        String durationformat = context.getString(R.string.durationformatlong);

        /*
         * Provide multiple arguments so the format can be changed easily by
         * modifying the xml.
         */
        sFormatBuilder.setLength(0);

        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = (secs / 60) % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;

        return sFormatter.format(durationformat, timeArgs).toString();
    }

    public static String getFormatTime(long time, SimpleDateFormat formater) {
        if (formater == null) return "";
        return formater.format(new Date(time));
    }

    /**
     * 获取年月日
     *
     * @return
     */
    public static String getDateTime() {
        return BIRTHDAYV2_FORMAT.format(new Date());
    }

    public static String getDateTimeExact() {
        return DEFAULT_FORMAT.format(new Date());
    }

    public static String getFileLastModifiedTime(File file) {
        Calendar cal = Calendar.getInstance();
        long time = file.lastModified();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cal.setTimeInMillis(time);
        return formatter.format(cal.getTime());
    }

    public static long parseData(String time) {
        try {
            if (TextUtils.isEmpty(time)) {
                return 0;
            }
            return DEFAULT_FORMAT.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Date getGroupchatDate(String date) {
        try {
            return DEFAULT_FORMAT.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static String getGroupchatFormatTime(Date date) {
        try {
            Time time = new Time();
            time.set(date.getTime());
            int thenYear = time.year;
            int thenMonth = time.month;
            int thenMonthDay = time.monthDay;
            int thenWeekDay = time.weekDay;
            int thenHour = time.hour;
            int thenMinute = time.minute;

            String temp_hour = thenHour < 10 ? "0" + thenHour : String.valueOf(thenHour);
            String temp_minute = thenMinute < 10 ? "0" + thenMinute : String.valueOf(thenMinute);

            time.set(System.currentTimeMillis());
            if (thenYear == time.year && thenMonth == time.month && thenMonthDay == time.monthDay) {
                return String.format("%s:%s", temp_hour, temp_minute);
            } else if (thenYear == time.year && thenMonth == time.month && time.monthDay - thenMonthDay == 1) {
                //跨周优先显示日 && thenWeedDay == time.weekDay
                return String.format("昨天 %s:%s", temp_hour, temp_minute);
            } else if (thenYear == time.year && thenMonth == time.month && thenWeekDay != time.weekDay) {
                return String.format("%s %s:%s", getWeekDay(thenWeekDay), temp_hour, temp_minute);
            } else {
                return String.format("%d月%d日 %s:%s", thenMonth + 1, thenMonthDay, temp_hour, temp_minute);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getWeekDay(int thenWeekDay) {
        switch (thenWeekDay) {
            case 0:
                return "周日";
            case 1:
                return "周一";
            case 2:
                return "周二";
            case 3:
                return "周三";
            case 4:
                return "周四";
            case 5:
                return "周五";
            case 6:
                return "周六";
        }
        return null;
    }
}
