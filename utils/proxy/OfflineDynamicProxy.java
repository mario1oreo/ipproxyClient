package com.fosun.financial.data.proxy.ipproxyclient.utils.proxy;


import com.virjar.dungproxy.client.ippool.PreHeater;
import com.virjar.dungproxy.client.ippool.config.DungProxyContext;
import com.virjar.dungproxy.client.ippool.strategy.impl.DefaultContentProxyChecker;
import com.virjar.dungproxy.client.util.RedisTools;
import lombok.Data;

import java.util.List;

/**
 * 启动离线预热--动态
 *
 * @author mario1oreo
 * @date 2018-1-16 15:33:47
 */
@Data
public class OfflineDynamicProxy implements Runnable {
    private volatile String userID;
    private volatile String domain;
    private volatile String type;
    private volatile String topicName;
    private volatile String expectContent;

    public OfflineDynamicProxy(String userID, String domain, String type, String topicName, String expectContent) {
        this.userID = userID;
        this.domain = domain;
        this.type = type;
        this.topicName = topicName;
        this.expectContent = expectContent;

    }

    @Override
    public void run() {
        DungProxyContext dungProxyContext = DungProxyContext.create();
        //TODO 创建动态切换redis  kafka  等消息中间件的Checker   从不同的消息队列中获取动态变动的预热IP


        dungProxyContext.setDefaultCoreSize(100).genDomainContext(getDomain()).setProxyChecker(new DefaultContentProxyChecker(getExpectContent(), getUserID(), getDomain()));

        PreHeater preHeater = dungProxyContext.getPreHeater();
        RedisTools redisTools = new RedisTools();
        //redis
        if ("1".equals(getType())) {
            List<String> tasks = redisTools.lrange(topicName, 0, 10);
            tasks.forEach(task -> preHeater.addTask(task));
            //kafka
        } else if ("2".equals(getType())) {
            preHeater.addTask("");
        }
        preHeater.setThreadNumber(100);
        preHeater.doPreHeat();
        preHeater.destroy();
    }
}
