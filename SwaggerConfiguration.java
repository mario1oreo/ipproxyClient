package com.fosun.financial.data.proxy.ipproxyclient;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 *
 * Swagger配置
 *
 * 配置相关的说明信息
 *
 * @author mario1oreo
 * @date 2018-1-17 17:16:09
 *
 */
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .build()
//                .pathMapping("/ip")
                ;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("IP代理 rest api")
                .description("通过restful接口获取可靠可用的代理IP，享受代理服务")
                .termsOfServiceUrl("http://www.baidu.com/")
                .version("1.0")
                .build();
    }

}