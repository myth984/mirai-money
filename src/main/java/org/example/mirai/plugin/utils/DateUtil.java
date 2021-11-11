package org.example.mirai.plugin.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static SimpleDateFormat SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static String formatDate(Date date) {
        return SimpleDateFormat.format(date);
    }

    public static String formatDate() {
        return SimpleDateFormat.format(new Date());
    }
}
