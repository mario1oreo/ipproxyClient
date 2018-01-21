package com.fosun.financial.data.proxy.ipproxyclient.utils.proxy;


import com.virjar.dungproxy.client.ippool.PreHeater;
import com.virjar.dungproxy.client.ippool.config.DungProxyContext;
import com.virjar.dungproxy.client.ippool.strategy.impl.DefaultContentProxyChecker;
import lombok.Data;

/**
 * 启动离线预热--固态
 *
 * @author mario1oreo
 * @date 2018-1-16 15:24:07
 */
@Data
public class OfflineFixedProxy implements Runnable {
    private volatile String userID;
    private volatile String domain;
    private volatile String expectContent;
    private volatile String testUrl;

    public OfflineFixedProxy(String userID, String domain, String expectContent, String testUrl) {
        this.userID = userID;
        this.domain = domain;
        this.expectContent = expectContent;
        this.testUrl = testUrl;
    }

    @Override
    public void run() {
        DungProxyContext dungProxyContext = DungProxyContext.create();
        dungProxyContext.setDefaultCoreSize(100).genDomainContext(getDomain()).setProxyChecker(new DefaultContentProxyChecker(getExpectContent(), getUserID(), getDomain()));
        //定制国家企业工商公示系统的IP检查

        PreHeater preHeater = dungProxyContext.getPreHeater();
        preHeater.addTask(getTestUrl());
        preHeater.setThreadNumber(100);
        preHeater.doPreHeat();
        preHeater.destroy();
    }
}
