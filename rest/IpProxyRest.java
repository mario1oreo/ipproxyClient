package com.fosun.financial.data.proxy.ipproxyclient.rest;

import com.alibaba.fastjson.JSONObject;
import com.fosun.financial.data.proxy.ipproxyclient.constant.KeyConstant;
import com.fosun.financial.data.proxy.ipproxyclient.constant.MessageConstant;
import com.fosun.financial.data.proxy.ipproxyclient.utils.proxy.OfflineDynamicProxy;
import com.fosun.financial.data.proxy.ipproxyclient.utils.proxy.OfflineFixedProxy;
import com.fosun.financial.data.proxy.ipproxyclient.utils.proxy.OnlineProxy;
import com.fosun.financial.data.proxy.ipproxyclient.utils.redis.RedisTools;
import com.virjar.dungproxy.client.ningclient.concurrent.NamedThreadFactory;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * 代理IP入口服务
 * <p>
 * 1.取对应domain下的可用IP
 * 2.锁定对应domain下的IP
 * 3.下线对应domain下的IP
 * 4.对应domain下的IP打分
 * 5.启动预热---固态/动态
 *
 * @author mario1oreo
 * @date 2018-1-2 11:34:59
 */


@Api(value = "/ip", description = "\n1.取对应domain下的可用IP\n2.锁定对应domain下的IP\n3.下线对应domain下的IP\n4.对应domain下的IP打分\n5.启动预热---固态/动态", tags = "IpProxyRest")
@RestController
@RequestMapping("/ip")
@Slf4j
public class IpProxyRest {

    private static RedisTools redisTools = new RedisTools();
    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(25, 30, 30, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(), new NamedThreadFactory("myself-preheat"),
            new ThreadPoolExecutor.DiscardPolicy());

    /**
     * 取对应domain下的可用IP
     * <p>
     * 1、查询是否存在 "PREHEAT_FLAG_LIST_"+userID+domain
     * 2、是否存在 "AVAILABLE_PROXY_LIST_"+userID+domain
     * 3、是否存在可用IP  是否可以返回
     *
     * @param userID     用户ID
     * @param domain     域名
     * @param testUrl    预热链接
     * @param lockedTime 使用间隔
     * @return json
     */
    @ApiImplicitParams({//这个是入参，因为入参是request，所以要在这里定义，如果是其它的比如spring或javabean入参，可以在参数上使用@ApiParam注解
            @ApiImplicitParam(
                    name = "userID",
                    value = "用户ID",
                    example = "USER_ID_001",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "domain",
                    value = "域名",
                    example = "www.creditchina.gov.cn",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "testUrl",
                    value = "预热链接",
                    example = "http://www.creditchina.gov.cn/api/credit_info_search?keyword=百度&templateId=&page=1&pageSize=10",
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "lockedTime",
                    value = "使用间隔",
                    example = "15",
                    required= false,
                    defaultValue = "15",
                    dataType = "String")
    })
    @ApiOperation(value = "取对应domain下的可用IP", notes = "通过用户名，域名，测试连接，获取到一个可用的代理IP。首次使用会先触发预热，返回failure，继续请求即可。", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value =
            {
                    @ApiResponse(code = 400, message = "Invalid ID supplied"),
                    @ApiResponse(code = 404, message = "Pet not found")
            }
    )
    @RequestMapping(value = "/getProxy", method = GET)
    private JSONObject getProxy(String userID, String domain, String testUrl,@RequestParam(defaultValue = "15") String lockedTime) {

        StringBuilder errorMessage = new StringBuilder();
        JSONObject result = new JSONObject();
        if (StringUtils.isBlank(userID)) {
            errorMessage.append(MessageConstant.PARAM_USER_ID_CAN_NOT_BE_NULL);
        } else if (StringUtils.isBlank(domain)) {
            errorMessage.append(MessageConstant.PARAM_DOMAIN_CAN_NOT_BE_NULL);
        } else if (StringUtils.isBlank(testUrl)) {
            testUrl = "http://" + domain;
        }

        String preHeatFlagKey = KeyConstant.PREHEAT_FLAG_LIST + userID + KeyConstant.UNDER_LINE + domain;
        String availableProxyListKey = KeyConstant.AVAILABLE_PROXY_LIST + userID + KeyConstant.UNDER_LINE + domain;
        String proxyScoreSortSetKey = KeyConstant.PROXY_SCORE_SORT_SET + userID + KeyConstant.UNDER_LINE + domain;
        String proxyUnscoreSetKey = KeyConstant.PROXY_UN_SCORE_SET + userID + KeyConstant.UNDER_LINE + domain;
        if (redisTools.existKey(preHeatFlagKey) && redisTools.countStr(availableProxyListKey) > 10) {
            int liveTime = redisTools.ttlKey(preHeatFlagKey);
            if ((liveTime > 1) && ((liveTime + KeyConstant.LIVE_TIME_4_HOUR) < KeyConstant.LIVE_TIME_5_HOUR)) {
                redisTools.expireKey(preHeatFlagKey, KeyConstant.LIVE_TIME_5_HOUR);
                redisTools.expireKey(availableProxyListKey, KeyConstant.LIVE_TIME_5_HOUR);
                redisTools.expireKey(proxyScoreSortSetKey, KeyConstant.LIVE_TIME_5_HOUR);
                redisTools.expireKey(proxyUnscoreSetKey, KeyConstant.LIVE_TIME_5_HOUR);
            }
            liveTime = redisTools.ttlKey(proxyUnscoreSetKey);
            if (liveTime == -1) {
                redisTools.expireKey(proxyUnscoreSetKey, KeyConstant.LIVE_TIME_5_HOUR);
            }
        } else {
            log.info("====>>  create preHeatFlag! {}", preHeatFlagKey);
            redisTools.setStr(preHeatFlagKey, KeyConstant.LIVE_TIME_1_HOUR);
            errorMessage.append("preHeat Proxy is running ,try again later!");
            pool.execute(new OnlineProxy(userID, domain, testUrl));

        }

        if (errorMessage.length() > 0) {
            log.warn(errorMessage.toString());
            result.put(KeyConstant.RETURN_MESSAGE_KEY, errorMessage.toString());
            result.put(KeyConstant.RETURN_TYPE_KEY, KeyConstant.RETURN_TYPE_VALUE_FAILURE);
            result.put(KeyConstant.RETURN_DATA_KEY, new JSONObject());
            return result;
        }


        if (redisTools.countStr(availableProxyListKey) > 0) {
            int tryTimes = 0;
            while (true) {
                String ipPort = redisTools.lpopStr(availableProxyListKey);
                tryTimes++;
                if (!ipPort.contains(KeyConstant.COLON)) {
                    errorMessage.append("proxy ip is not limited! should be ===>> ip:port <<===").append("    current proxy is :").append(ipPort);
                }

                if (errorMessage.length() > 0) {
                    log.error(errorMessage.toString());
                    result.put(KeyConstant.RETURN_MESSAGE_KEY, errorMessage.toString());
                    result.put(KeyConstant.RETURN_TYPE_KEY, KeyConstant.RETURN_TYPE_VALUE_FAILURE);
                    result.put(KeyConstant.RETURN_DATA_KEY, new JSONObject());
                    break;
                }
                String lockedProxyKey = KeyConstant.LOCKED_PROXY_LIST + userID + KeyConstant.UNDER_LINE + domain + KeyConstant.UNDER_LINE + ipPort;

                if (redisTools.zscoreSortSet(proxyScoreSortSetKey, ipPort) > KeyConstant.OFF_LINE_SCORE) {
                    redisTools.rpushStr(availableProxyListKey, ipPort);
                    if (!redisTools.existKey(lockedProxyKey)) {
                        redisTools.setStr(lockedProxyKey, Integer.valueOf(lockedTime));
                        String ip = ipPort.split(KeyConstant.COLON)[0];
                        String port = ipPort.split(KeyConstant.COLON)[1];
                        if (redisTools.sismemberSet(proxyUnscoreSetKey, ipPort)) {
                            doRate(userID, domain, ip, port, KeyConstant.RATE_SCORE_SUCCEED);
                        } else {
                            redisTools.saddSet(proxyUnscoreSetKey, ipPort);
                        }
                        result.put(KeyConstant.RETURN_MESSAGE_KEY, StringUtils.EMPTY);
                        result.put(KeyConstant.RETURN_TYPE_KEY, KeyConstant.RETURN_TYPE_VALUE_SUCCEED);
                        JSONObject dataMap = new JSONObject();
                        dataMap.put(KeyConstant.RETURN_DATA_IP_KEY, ip);
                        dataMap.put(KeyConstant.RETURN_DATA_PORT_KEY, port);
                        result.put(KeyConstant.RETURN_DATA_KEY, dataMap);
                        break;
                    } else if (tryTimes > 2) {
                        //连续10次取的ip都在使用中，说明IP不足。开启预热线程。
                        log.info("连续3次取的ip都在使用中，说明IP不足。开启预热线程。");
                        pool.execute(new OnlineProxy(userID, domain, testUrl));
                        tryTimes = 0;
                    }
                } else {
                    redisTools.zremSortSet(proxyScoreSortSetKey, ipPort);
                }
            }
        } else {
            errorMessage.append("Here is no available proxy,try again later!");
            log.error(errorMessage.toString());
            result.put(KeyConstant.RETURN_MESSAGE_KEY, errorMessage.toString());
            result.put(KeyConstant.RETURN_TYPE_KEY, KeyConstant.RETURN_TYPE_VALUE_FAILURE);
            result.put(KeyConstant.RETURN_DATA_KEY, new JSONObject());
        }
        return result;
    }

    /**
     * 锁定对应domain下的IP
     *
     * @param userID     用户ID
     * @param domain     域名
     * @param ip         ip
     * @param port       端口
     * @param lockedTime 锁定时间间隔
     * @return json
     */
    @ResponseBody
    @ApiImplicitParams({//这个是入参，因为入参是request，所以要在这里定义，如果是其它的比如spring或javabean入参，可以在参数上使用@ApiParam注解
            @ApiImplicitParam(
                    name = "userID",
                    value = "用户ID",
                    example = "USER_ID_001",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "domain",
                    value = "域名",
                    example = "www.creditchina.gov.cn",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "ip",
                    value = "ip",
                    example = "1.1.1.1",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "port",
                    value = "端口",
                    example = "8888",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "lockedTime",
                    value = "使用间隔",
                    example = "15",
                    defaultValue = "15",
                    dataType = "String")
    })
    @ApiOperation(value = "锁定对应domain下的IP", notes = "通过用户名，域名，ip，端口。锁定IP一段时间之后释放。", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value =
            {
                    @ApiResponse(code = 400, message = "Invalid ID supplied"),
                    @ApiResponse(code = 404, message = "Pet not found")
            }
    )
    @RequestMapping(value = "/lockProxy", method = GET)
    private JSONObject lockProxy(String userID, String domain, String ip, String port, String lockedTime) {
        StringBuilder errorMessage = new StringBuilder();
        JSONObject result = new JSONObject();
        if (StringUtils.isBlank(userID)) {
            errorMessage.append(MessageConstant.PARAM_USER_ID_CAN_NOT_BE_NULL);
        } else if (StringUtils.isBlank(domain)) {
            errorMessage.append(MessageConstant.PARAM_DOMAIN_CAN_NOT_BE_NULL);
        } else if (StringUtils.isBlank(ip)) {
            errorMessage.append(MessageConstant.PARAM_IP_CAN_NOT_BE_NULL);
        } else if (StringUtils.isBlank(port)) {
            errorMessage.append(MessageConstant.PARAM_PORT_CAN_NOT_BE_NULL);
        } else if (StringUtils.isBlank(lockedTime)) {
            errorMessage.append(MessageConstant.PARAM_LOCKEDTIME_CAN_NOT_BE_NULL);
        }

        if (errorMessage.length() > 0) {
            log.error(errorMessage.toString());
            result.put(KeyConstant.RETURN_MESSAGE_KEY, errorMessage.toString());
            result.put(KeyConstant.RETURN_TYPE_KEY, KeyConstant.RETURN_TYPE_VALUE_FAILURE);
            result.put(KeyConstant.RETURN_DATA_KEY, new JSONObject());
            return result;
        }
        String lockedProxyKey = KeyConstant.LOCKED_PROXY_LIST + userID + KeyConstant.UNDER_LINE + domain + KeyConstant.UNDER_LINE + ip + KeyConstant.COLON + port;
        if (redisTools.ttlKey(lockedProxyKey) != KeyConstant.REDIS_KEY_EXPIRE_STATUS_KEY_NOT_EXIST) {
            redisTools.expireKey(lockedProxyKey, Integer.valueOf(lockedTime));
        } else {
            redisTools.setStr(lockedProxyKey, Integer.valueOf(lockedTime));
        }
        result.put(KeyConstant.RETURN_MESSAGE_KEY, StringUtils.EMPTY);
        result.put(KeyConstant.RETURN_TYPE_KEY, KeyConstant.RETURN_TYPE_VALUE_SUCCEED);
        return result;
    }

    /**
     * 下线对应domain下的IP
     *
     * @param userID 用户ID
     * @param domain 域名
     * @param ip     IP
     * @param port   端口
     * @return json
     */
    @ResponseBody
    @ApiImplicitParams({//这个是入参，因为入参是request，所以要在这里定义，如果是其它的比如spring或javabean入参，可以在参数上使用@ApiParam注解
            @ApiImplicitParam(
                    name = "userID",
                    value = "用户ID",
                    example = "USER_ID_001",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "domain",
                    value = "域名",
                    example = "www.creditchina.gov.cn",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "ip",
                    value = "ip",
                    example = "1.1.1.1",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "port",
                    value = "端口",
                    example = "8888",
                    required = true,
                    dataType = "String"),
    })
    @ApiOperation(value = "下线对应domain下的IP", notes = "通过用户名，域名，ip，端口。下线一个IP", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value =
            {
                    @ApiResponse(code = 400, message = "Invalid ID supplied"),
                    @ApiResponse(code = 404, message = "Pet not found")
            }
    )
    @RequestMapping(value = "/offLineProxy", method = GET)
    private JSONObject offLineProxy(String userID, String domain, String ip, String port) {
        StringBuilder errorMessage = new StringBuilder();
        JSONObject result = new JSONObject();
        if (StringUtils.isBlank(userID)) {
            errorMessage.append(MessageConstant.PARAM_USER_ID_CAN_NOT_BE_NULL);
        } else if (StringUtils.isBlank(domain)) {
            errorMessage.append(MessageConstant.PARAM_DOMAIN_CAN_NOT_BE_NULL);
        } else if (StringUtils.isBlank(ip)) {
            errorMessage.append(MessageConstant.PARAM_IP_CAN_NOT_BE_NULL);
        } else if (StringUtils.isBlank(port)) {
            errorMessage.append(MessageConstant.PARAM_PORT_CAN_NOT_BE_NULL);
        }

        if (errorMessage.length() > 0) {
            log.error(errorMessage.toString());
            result.put(KeyConstant.RETURN_MESSAGE_KEY, errorMessage.toString());
            result.put(KeyConstant.RETURN_TYPE_KEY, KeyConstant.RETURN_TYPE_VALUE_FAILURE);
            result.put(KeyConstant.RETURN_DATA_KEY, new JSONObject());
            return result;
        }
        String proxyScoreSortSetKey = KeyConstant.PROXY_SCORE_SORT_SET + userID + KeyConstant.UNDER_LINE + domain;
        String ipPort = ip + KeyConstant.COLON + port;
        if (redisTools.sismemberSortSet(proxyScoreSortSetKey, ipPort)) {
            redisTools.zremSortSet(proxyScoreSortSetKey, ipPort);
        } else {
            result.put(KeyConstant.RETURN_MESSAGE_KEY, "the proxy is already offline or the proxy does not exists!");
        }

        result.put(KeyConstant.RETURN_TYPE_KEY, KeyConstant.RETURN_TYPE_VALUE_SUCCEED);
        return result;
    }


    /**
     * 对应domain下的IP打分
     *
     * @param userID 用户ID
     * @param domain 域名
     * @param ip     IP
     * @param port   端口
     * @param type   打分类型（0:失败，暂时不是用其他的反馈）
     * @return json
     */
    @ResponseBody
    @ApiImplicitParams({//这个是入参，因为入参是request，所以要在这里定义，如果是其它的比如spring或javabean入参，可以在参数上使用@ApiParam注解
            @ApiImplicitParam(
                    name = "userID",
                    value = "用户ID",
                    example = "USER_ID_001",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "domain",
                    value = "域名",
                    example = "www.creditchina.gov.cn",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "ip",
                    value = "ip",
                    example = "1.1.1.1",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "port",
                    value = "端口",
                    example = "8888",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "type",
                    value = "打分类型（0:失败，暂时不使用其他的反馈）",
                    example = "0",
                    defaultValue = "0",
                    dataType = "String")
    })
    @ApiOperation(value = "对应domain下的IP打分", notes = "通过用户名，域名，ip，端口，打分类型。对IP进行打分处理。", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value =
            {
                    @ApiResponse(code = 400, message = "Invalid ID supplied"),
                    @ApiResponse(code = 404, message = "Pet not found")
            }
    )
    @RequestMapping("/rateProxy")
    private JSONObject rateProxy(String userID, String domain, String ip, String port, String type) {
        StringBuilder errorMessage = new StringBuilder();
        JSONObject result = new JSONObject();
        if (StringUtils.isBlank(userID)) {
            errorMessage.append(MessageConstant.PARAM_USER_ID_CAN_NOT_BE_NULL);
        } else if (StringUtils.isBlank(domain)) {
            errorMessage.append(MessageConstant.PARAM_DOMAIN_CAN_NOT_BE_NULL);
        } else if (StringUtils.isBlank(ip)) {
            errorMessage.append(MessageConstant.PARAM_IP_CAN_NOT_BE_NULL);
        } else if (StringUtils.isBlank(port)) {
            errorMessage.append(MessageConstant.PARAM_PORT_CAN_NOT_BE_NULL);
        }
        if (errorMessage.length() > 0) {
            log.error(errorMessage.toString());
            result.put(KeyConstant.RETURN_MESSAGE_KEY, errorMessage.toString());
            result.put(KeyConstant.RETURN_TYPE_KEY, KeyConstant.RETURN_TYPE_VALUE_FAILURE);
            result.put(KeyConstant.RETURN_DATA_KEY, new JSONObject());
            return result;
        }
        return doRate(userID, domain, ip, port, KeyConstant.RATE_SCORE_FAILURE);
    }


    /**
     * 启动预热---固态
     * <p>
     * 单个url的测试连通性
     * <p>
     * 暂时只支持get请求
     *
     * @param userID        用户ID
     * @param domain        域名
     * @param testUrl       预热链接
     * @param expectContent 期望返回值
     * @return json
     */
    @ApiImplicitParams({//这个是入参，因为入参是request，所以要在这里定义，如果是其它的比如spring或javabean入参，可以在参数上使用@ApiParam注解
            @ApiImplicitParam(
                    name = "userID",
                    value = "用户ID",
                    example = "USER_ID_001",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "domain",
                    value = "域名",
                    example = "www.creditchina.gov.cn",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "testUrl",
                    value = "预热链接",
                    example = "http://www.creditchina.gov.cn/api/credit_info_search?keyword=百度&templateId=&page=1&pageSize=10",
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "expectContent",
                    value = "期望返回值",
                    example = "data",
                    required = true,
                    defaultValue = "data",
                    dataType = "String")
    })
    @ApiOperation(value = "启动预热---固态", notes = "单个url的测试连通性。", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value =
            {
                    @ApiResponse(code = 400, message = "Invalid ID supplied"),
                    @ApiResponse(code = 404, message = "Pet not found")
            }
    )
    @RequestMapping(value = "/preHeatDomainFixed", method = GET)
    private JSONObject preHeatDomainFixed(String userID, String domain, String testUrl, String expectContent) {
        JSONObject result = new JSONObject();
        String preHeatFlagKey = KeyConstant.PREHEAT_FLAG_LIST + userID + KeyConstant.UNDER_LINE + domain;
        if (!redisTools.existKey(preHeatFlagKey)) {
            redisTools.setStr(preHeatFlagKey, KeyConstant.LIVE_TIME_1_HOUR);
            result.put(KeyConstant.RETURN_MESSAGE_KEY, "离线预热已启动！");
            result.put(KeyConstant.RETURN_TYPE_KEY, KeyConstant.RETURN_TYPE_VALUE_SUCCEED);
            pool.execute(new OfflineFixedProxy(userID, domain, expectContent, testUrl));
        } else {
            result.put(KeyConstant.RETURN_MESSAGE_KEY, "离线预热已经在运行中！请直接请求获取代理接口。");
            result.put(KeyConstant.RETURN_TYPE_KEY, KeyConstant.RETURN_TYPE_VALUE_FAILURE);
        }
        return result;
    }

    /**
     * 启动预热---动态
     * <p>
     * 通过单个topic读取最新的url，只读不取。
     * <p>
     * 暂时只支持get请求
     *
     * @param userID        用户ID
     * @param domain        域名
     * @param type          动态类型
     * @param topicName     消息名
     * @param expectContent 期望返回值
     * @return json
     */
    @ApiImplicitParams({//这个是入参，因为入参是request，所以要在这里定义，如果是其它的比如spring或javabean入参，可以在参数上使用@ApiParam注解
            @ApiImplicitParam(
                    name = "userID",
                    value = "用户ID",
                    example = "USER_ID_001",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "domain",
                    value = "域名",
                    example = "www.creditchina.gov.cn",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "type",
                    value = "动态类型",
                    example = "0",
                    required = true,
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "topicName",
                    value = "消息名",
                    dataType = "String"),
            @ApiImplicitParam(
                    name = "expectContent",
                    value = "期望返回值",
                    example = "data",
                    required = true,
                    defaultValue = "",
                    dataType = "String")
    })
    @ApiOperation(value = "启动预热---动态", notes = "通过单个topic读取最新的url，只读不取。", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value =
            {
                    @ApiResponse(code = 400, message = "Invalid ID supplied"),
                    @ApiResponse(code = 404, message = "Pet not found")
            }
    )
    @RequestMapping(value = "/preHeatDomainDynamic", method = GET)
    private JSONObject preHeatDomainDynamic(String userID, String domain, String type, String topicName, String expectContent) {
        JSONObject result = new JSONObject();
        String preHeatFlagKey = KeyConstant.PREHEAT_FLAG_LIST + userID + KeyConstant.UNDER_LINE + domain;
        if (!redisTools.existKey(preHeatFlagKey)) {
            redisTools.setStr(preHeatFlagKey, KeyConstant.LIVE_TIME_1_HOUR);
            result.put(KeyConstant.RETURN_MESSAGE_KEY, "离线预热已启动！");
            result.put(KeyConstant.RETURN_TYPE_KEY, KeyConstant.RETURN_TYPE_VALUE_SUCCEED);
            pool.execute(new OfflineDynamicProxy(userID, domain, type, topicName, expectContent));
        } else {
            result.put(KeyConstant.RETURN_MESSAGE_KEY, "离线预热已经在运行中！请直接请求获取代理接口。");
            result.put(KeyConstant.RETURN_TYPE_KEY, KeyConstant.RETURN_TYPE_VALUE_FAILURE);
        }
        return result;
    }

    /**
     * 对IP进行打分
     *
     * @param userID    用户ID
     * @param domain    域名
     * @param ip        IP
     * @param port      端口
     * @param rateScore 分数
     * @return json
     */
    private static JSONObject doRate(String userID, String domain, String ip, String port, int rateScore) {
        JSONObject result = new JSONObject();
        String ipPort = ip + KeyConstant.COLON + port;
        String proxyUnscoreSetKey = KeyConstant.PROXY_UN_SCORE_SET + userID + KeyConstant.UNDER_LINE + domain;
        if (redisTools.sismemberSet(proxyUnscoreSetKey, ipPort)) {
            redisTools.sremSet(proxyUnscoreSetKey, ipPort);
        }
        String proxyScoreSortSetKey = KeyConstant.PROXY_SCORE_SORT_SET + userID + KeyConstant.UNDER_LINE + domain;
        if (redisTools.sismemberSortSet(proxyScoreSortSetKey, ipPort)) {
            double currentScore = redisTools.zscoreSortSet(proxyScoreSortSetKey, ipPort);
            double newScore = (currentScore * (KeyConstant.RATE_SCORE_WEIGHT_NUM - 1) + rateScore) / KeyConstant.RATE_SCORE_WEIGHT_NUM;
            redisTools.zaddSortSet(proxyScoreSortSetKey, newScore, ipPort);
            result.put(KeyConstant.RETURN_MESSAGE_KEY, StringUtils.EMPTY);
            result.put(KeyConstant.RETURN_TYPE_KEY, KeyConstant.RETURN_TYPE_VALUE_SUCCEED);
        } else {
            result.put(KeyConstant.RETURN_MESSAGE_KEY, "the proxy is not exist!");
            result.put(KeyConstant.RETURN_TYPE_KEY, KeyConstant.RETURN_TYPE_VALUE_FAILURE);
        }
        return result;
    }

}
