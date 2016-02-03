package me.peiwo.peiwo.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import android.text.TextUtils;

public class PinYin {


    public static String zhuanhuan(Character c) {
        if (c < 65 || c > 90) {
            c = "#".charAt(0);
        }
        return c.toString();
    }


    public static String getPinYinAlpha(String input) {
        if (input.length() == 0) {
            return "#";
        } else {
            String[] array = PinyinHelper.toHanyuPinyinStringArray(input.charAt(0));
            if (array != null && array.length > 0) {
                input = String.valueOf(array[0].charAt(0)).toUpperCase();
            } else {
                input = zhuanhuan(String.valueOf(input.charAt(0)).toUpperCase().charAt(0));
            }
        }
        return input;
    }

    private static final HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();

    public static String getAllPinYin(String source) {
        if (TextUtils.isEmpty(source)) return "";
        StringBuilder sb = new StringBuilder();
        try {
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            format.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);

            for (char c : source.toCharArray()) {
                String[] array = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (array != null && array.length > 0) {
                    //input = String.valueOf(array[0].charAt(0)).toUpperCase();
                    for (String s : array) {
                        sb.append(s);
                    }
                } else {
                    sb.append(String.valueOf(c).toLowerCase());
                }

            }
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }
        return sb.toString();
    }

}
