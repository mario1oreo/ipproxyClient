package com.fosun.financial.data.proxy.ipproxyclient.constant;


/**
 *
 * key相关的变量
 *
 * @author mario1oreo
 * @date 2018-1-8 14:20:09
 */
public class KeyConstant {


    public static final String RETURN_MESSAGE_KEY = "message";
    public static final String RETURN_DATA_KEY = "data";
    public static final String RETURN_DATA_IP_KEY = "ip";
    public static final String RETURN_DATA_PORT_KEY = "port";
    public static final String RETURN_TYPE_KEY = "type";
    public static final String RETURN_TYPE_VALUE_SUCCEED = "succeed";
    public static final String RETURN_TYPE_VALUE_FAILURE = "failure";

    public static final String PREHEAT_FLAG_LIST = "PREHEAT_FLAG_LIST_";
    public static final String AVAILABLE_PROXY_LIST = "AVAILABLE_PROXY_LIST_";
    public static final String LOCKED_PROXY_LIST = "LOCKED_PROXY_LIST_";
    public static final String PROXY_SCORE_SORT_SET = "PROXY_SCORE_SORT_SET_";
    public static final String PROXY_UN_SCORE_SET = "PROXY_UN_SCORE_SET";
    public static final String UNDER_LINE = "_";
    public static final String COLON = ":";



    public static final int LIVE_TIME_55_MINUTES = 3300;
    public static final int LIVE_TIME_1_HOUR = 3600;
    public static final int LIVE_TIME_4_HOUR = 14400;
    public static final int LIVE_TIME_5_HOUR = 18000;

    public static final double OFF_LINE_SCORE = 0.30;

    public static final int REDIS_KEY_EXPIRE_STATUS_KEY_NOT_EXIST = -2;
    public static final int REDIS_KEY_EXPIRE_STATUS_KEY_NO_EXPIRE = -1;

    public static final int RATE_SCORE_WEIGHT_NUM = 15;
    public static final int RATE_SCORE_SUCCEED = 1;
    public static final int RATE_SCORE_FAILURE = 0;

}
