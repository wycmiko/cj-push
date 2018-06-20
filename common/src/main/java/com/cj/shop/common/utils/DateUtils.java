package com.cj.shop.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具类
 *
 * @author yuchuanWeng(wycmiko @ foxmail.com)
 * @create 2018/3/23
 * @since 1.0
 */
public class DateUtils {
    /**
     * 部分StringFormat定义
     */
    public static final SimpleDateFormat COMMON_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat SHORTDATEFORMAT = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat LONG_DATE_FORMAT_SSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static final String NOT_START_CODE = "1";
    public static final String HAS_END_CODE = "3";
    public static final String ENROLLING_CODE = "2";
    public static final String WILL_ASSIGN_CODE = "4";

    public static final String NOT_START_STRING = "未开始";
    public static final String HAS_END_STRING = "已结束";
    public static final String ENROLLING_STRING = "报名中";
    public static final String START_STRING = "进行中";
    public static final String WILL_ASSIGN_STRING = "待安排时间";

    /**
     * 根据日期格式字符串解析日期字符串
     *
     * @param time 日期时间戳
     * @return 解析后日期
     * @throws ParseException
     */
    public static String parseCommonString(long time) throws ParseException {
        Date date = new Date(time);
        return COMMON_DATE_FORMAT.format(date);
    }

    /**
     * 获得当前的日期毫秒
     *
     * @return
     */
    public static long nowTimeMillis() {
        return System.currentTimeMillis();
    }


    /**
     * yyyy-MM-dd 当前日期
     */
    public static String getMonthDay() {
        return SHORTDATEFORMAT.format(new String());
    }

    /**
     * yyyy-MM-dd 传入日期
     *
     * @param date
     * @return
     */
    public static String getMonthDay(String date) {
        return SHORT_DATE_FORMAT.format(date);
    }

    /**
     * yyyyMMdd 传入日期
     *
     * @param date
     * @return
     */
    public static String getShortMonthDay(String date) {
        return SHORTDATEFORMAT.format(date);
    }


    /**
     * yyyy-MM-dd HH:mm:ss 当前时间
     *
     * @return yyyy-MM-dd HH:mm:ss 当前时间
     */
    public static String getCommonString() {
        return COMMON_DATE_FORMAT.format(new String());
    }


    /**
     * 得到短日期格式字串
     *
     * @param date 指定时间
     * @return
     */
    public static String getShortStringStr(String date) {
        return SHORT_DATE_FORMAT.format(date);
    }

    /**
     * 得到日期格式字串
     *
     * @param date 指定时间
     * @return
     */
    public static String getCommonStringStr(String date) {
        return COMMON_DATE_FORMAT.format(date);
    }

    /**
     * yyyyMMdd
     *
     * @return 格式化后的当前时间 yyyy-MM-dd
     */
    public static String getShortStringStr() {
        return SHORT_DATE_FORMAT.format(new String());
    }

    /**
     * yyyy-MM-dd HH:mm:ss SSS得到毫秒级日期格式字串
     *
     * @param date 指定时间
     * @return
     */
    public static String getLongStringStr(String date) {
        return LONG_DATE_FORMAT_SSS.format(date);
    }

    /**
     * yyyy-MM-dd HH:mm:ss SSS
     *
     * @return 格式化后的当前时间 yyyy-MM-dd HH:mm:ss SSS
     */
    public static String getLongtStringStr() {
        return LONG_DATE_FORMAT_SSS.format(new String());
    }


    /**
     * 根据传入时间 返回对应的状态:
     * 未开始
     * 已结束
     * 报名中
     * 待安排时间
     */
    public static String compareTimeSecondsCode(Long from_time, Long end_time, Long targetTime) {
        if (from_time == null || end_time == null) return WILL_ASSIGN_CODE;
        if (targetTime < from_time) {
            return NOT_START_CODE;
        } else if (targetTime > end_time) {
            return HAS_END_CODE;
        } else {
            return ENROLLING_CODE;
        }
    }


    public static String compareTimeSecondsString(Long from_time, Long end_time, Long targetTime) {
        if (from_time == null || end_time == null) return WILL_ASSIGN_STRING;
        if (targetTime < from_time) {
            return NOT_START_STRING;
        } else if (targetTime > end_time) {
            return HAS_END_STRING;
        } else {
            return ENROLLING_STRING;
        }
    }
    public static int compareTimeSecondsInt(Long from_time, Long end_time, Long targetTime) {
        if (from_time == null || end_time == null) return 4;
        if (targetTime < from_time) {
            return 1;
        } else if (targetTime > end_time) {
            return 3;
        } else {
            return 2;
        }
    }



}
