package com.fosun.financial.data.proxy.ipproxyclient.constant;


import com.alibaba.fastjson.JSONObject;
import com.xiaoleilu.hutool.io.FileUtil;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * 提示信息相关常量
 *
 * @author mario1oreo
 */
public class MessageConstant {

    public static final String PARAM_IP_CAN_NOT_BE_NULL = "===>> ip <<=== can not be null!\n";
    public static final String PARAM_PORT_CAN_NOT_BE_NULL = "===>> port <<=== can not be null!\n";
    public static final String PARAM_USER_ID_CAN_NOT_BE_NULL = "===>> userID <<=== can not be null!\n";
    public static final String PARAM_DOMAIN_CAN_NOT_BE_NULL = "===>> domain <<=== can not be null!\n";
    public static final String PARAM_LOCKEDTIME_CAN_NOT_BE_NULL = "===>> lockedTime <<=== can not be null!\n";

    public static void main(String[] args) {
        String content = FileUtil.readString("d://soutv.txt", "utf-8");
        System.out.println("content = " + content);
        String result = StringEscapeUtils.unescapeJson(content);
        System.out.println("result = " + result.substring(1,result.length()-1));
        JSONObject json = JSONObject.parseObject(result.substring(1,result.length()-1));
        System.out.println("json = " + json);

    }
}
