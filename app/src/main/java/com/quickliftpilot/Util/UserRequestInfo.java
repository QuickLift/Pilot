package com.quickliftpilot.Util;

/**
 * Created by pandey on 12/3/18.
 */

public class UserRequestInfo {
    private static String userKey;
    private static String userName;
    private static String userPhone;
    private static String userStatus;
    private static String d_lat;
    private static String d_lng;
    private static String en_lat;
    private static String en_lng;
    private static String st_lat;
    private static String st_lng;


    public static String getUserKey() {
        return userKey;
    }

    public static void setUserKey(String userKey) {
        UserRequestInfo.userKey = userKey;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        UserRequestInfo.userName = userName;
    }

    public static String getUserPhone() {
        return userPhone;
    }

    public static void setUserPhone(String userPhone) {
        UserRequestInfo.userPhone = userPhone;
    }

    public static String getUserStatus() {
        return userStatus;
    }

    public static void setUserStatus(String userStatus) {
        UserRequestInfo.userStatus = userStatus;
    }

    public static String getD_lat() {
        return d_lat;
    }

    public static void setD_lat(String d_lat) {
        UserRequestInfo.d_lat = d_lat;
    }

    public static String getD_lng() {
        return d_lng;
    }

    public static void setD_lng(String d_lng) {
        UserRequestInfo.d_lng = d_lng;
    }

    public static String getEn_lat() {
        return en_lat;
    }

    public static void setEn_lat(String en_lat) {
        UserRequestInfo.en_lat = en_lat;
    }

    public static String getEn_lng() {
        return en_lng;
    }

    public static void setEn_lng(String en_lng) {
        UserRequestInfo.en_lng = en_lng;
    }

    public static String getSt_lat() {
        return st_lat;
    }

    public static void setSt_lat(String st_lat) {
        UserRequestInfo.st_lat = st_lat;
    }

    public static String getSt_lng() {
        return st_lng;
    }

    public static void setSt_lng(String st_lng) {
        UserRequestInfo.st_lng = st_lng;
    }


}
