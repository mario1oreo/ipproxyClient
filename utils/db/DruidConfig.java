package com.fosun.financial.data.proxy.ipproxyclient.utils.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by Samchu Li on 2017/5/2.
 */

@Configuration
public class DruidConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DruidConfig.class);

    @Value("${spring.datasource.driverClassName}")
    private String dirverClassname;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${srping.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.initialSize}")
    private int initialSize;

    @Value("${spring.datasource.minIdle}")
    private int minIdle;

    @Value("${spring.datasource.maxActive}")
    private int maxActive;

    @Value("${spring.datasource.maxWait}")
    private long maxWait;

    @Value("${spring.datasource.validationQuery}")
    private String validationQuesy;

    @Value("${spring.datasource.testonBorrow}")
    private boolean testOnBorrow;

    @Value("${spring.datasource.testOnReturn}")
    private boolean testOnReturn;

    @Value("${spring.datasource.testWhileIdle}")
    private boolean testWhileIdle;

    @Value("${spring.datasource.timeBetweenEvictionRunsMillos}")
    private long timeBetweenEvictionRunsMillis;

    @Value("${spring.datasource.minEvictableIdleTimeMillis}")
    private long minEvictableIdleTimeMillis;

    @Value("${spring.datasource.removeAbandoned}")
    private boolean removeAbandoned;

    @Value("${spring.datasource.removeAbandonedTimeout}")
    private int removeAbandonedTimeout;

    @Value("${spring.datasource.logAbandoned}")
    private boolean logAbandoned;

    @Value("${spring.datasource.filters}")
    private String filters;

    @Value("${spring.datasource.poolPreparedStatements}")
    private boolean poolPreparedStatements;

    @Value("${spring.datasource.manPoolPreparedStatementPreConnectionSize}")
    private int maxPoolPreparedStatementPreConnectionSize;


    @Bean
    @Primary
    public DataSource dataSource(){

        DruidDataSource datasource = new DruidDataSource();

        datasource.setDriverClassName(dirverClassname);
        datasource.setUrl(url);
        datasource.setUsername(username);
        datasource.setPassword(password);

        datasource.setInitialSize(initialSize);
        datasource.setMinIdle(minIdle);
        datasource.setMaxActive(maxActive);
        datasource.setMaxWait(maxWait);
        datasource.setValidationQuery(validationQuesy);
        datasource.setTestOnBorrow(testOnBorrow);
        datasource.setTestOnReturn(testOnReturn);
        datasource.setTestWhileIdle(testWhileIdle);
        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        datasource.setRemoveAbandoned(removeAbandoned);
        datasource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
        datasource.setLogAbandoned(logAbandoned);
        try {
            datasource.setFilters(filters);
        } catch (SQLException e) {
            LOGGER.error("druid configuration initialization filter error",e);
        }
        datasource.setPoolPreparedStatements(poolPreparedStatements);
        datasource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPreConnectionSize);

        return datasource;

    }


    /**
     * 注册一个StatViewServlet
     * @return
     */
    @Bean
    public ServletRegistrationBean DruidStatviewServlet(){
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(),"/druid/*");

        //添加初始化参数: initParams
        /** 白名单,如果不配置或者value为空，则允许所有*/
        servletRegistrationBean.addInitParameter("allow","");
        /** 黑名单，与白名单存在相同IP时，优先于白名单*/
        servletRegistrationBean.addInitParameter("deny","192.0.0.1");
        //用户名
        servletRegistrationBean.addInitParameter("loginUsername","druid");
        //密码
        servletRegistrationBean.addInitParameter("loginPassword","druid");
        //禁用页面上的"RESET ALL"功能
        servletRegistrationBean.addInitParameter("resetEnable","false");

        return servletRegistrationBean;

    }


    /**
     * 注册一个: WebStatFilter
     * @return
     */

    @Bean
    public FilterRegistrationBean duridStatFilter(){

        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());

        //过滤规则
        filterRegistrationBean.addUrlPatterns("/*");
        //忽略资源
        filterRegistrationBean.addInitParameter("exclusions","*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");

        return filterRegistrationBean;

    }




}
