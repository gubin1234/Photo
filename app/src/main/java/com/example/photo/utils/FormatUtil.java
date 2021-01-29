package com.example.photo.utils;

public class FormatUtil {
    public static String lessThan8Words(String str){
        if(str.length()>8){
            return str.substring(0,8)+"……";
        }
        return str;
    }

    public static String addTimeSeparator(int hour, int minute){
        String zero = "0";
        if(hour < 10&&minute < 10)
            return  zero+hour+":"+zero+minute;
        if(hour < 10)
            return  zero+hour+":"+minute;
        if(minute < 10)
            return  hour+":"+zero+minute;
        return hour+":"+minute;
    }

    public static String getRepeatText(int repeat){
        if(repeat == 0x0)
            return Constants.ONLY_ONCE;
        else if(repeat == 0x11111117)
            return Constants.EVERYDAY;
        else if(repeat == 0x11111005)
            return Constants.WEEKDAY;
        else if(repeat == 0x00000112)
            return Constants.WEEKEND;
        else {
            String text = "";
            if ((repeat & 0x10000000) > 0)
                text = text + "、一";
            if ((repeat & 0x01000000) > 0)
                text = text + "、二";
            if ((repeat & 0x00100000) > 0)
                text = text + "、三";
            if ((repeat & 0x00010000) > 0)
                text = text + "、四";
            if ((repeat & 0x00001000) > 0)
                text = text + "、五";
            if ((repeat & 0x00000100) > 0)
                text = text + "、六";
            if ((repeat & 0x00000010) > 0)
                text = text + "、日";
            text = text.replaceFirst("、", "周");
            return text;
        }
    }
}
