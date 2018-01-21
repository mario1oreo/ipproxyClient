package com.fosun.financial.data.proxy.ipproxyclient.utils.proxy;


import com.virjar.dungproxy.client.ippool.IpPool;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 在线预热启动
 *
 * @author mario1oreo
 * @date 2018-1-15 13:48:28
 */
@Data
public class OnlineProxy implements Runnable {

    private volatile String userID;
    private volatile String domain;
    private volatile String testUrl;
    private volatile String expectContent;

    public OnlineProxy(String userID, String domain, String testUrl) {
        this.userID = userID;
        this.domain = domain;
        this.testUrl = testUrl;
        this.expectContent = StringUtils.EMPTY;
    }

    public OnlineProxy(String userID, String domain, String testUrl, String expectContent) {
        this.userID = userID;
        this.domain = domain;
        this.testUrl = testUrl;
        this.expectContent = expectContent;
    }

    @Override
    public void run() {
        if (StringUtils.isEmpty(getExpectContent())) {
            IpPool.getInstance().bind(getUserID(),getDomain(), getTestUrl());
        } else {
            IpPool.getInstance().bind(getUserID(),getDomain(), getTestUrl(), getExpectContent());
        }
    }
}
