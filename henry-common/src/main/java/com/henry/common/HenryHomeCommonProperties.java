package com.henry.common;

import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "henry-home-common", ignoreInvalidFields = true)
public class HenryHomeCommonProperties {
    /**
     * 认证配置
     */
    private AuthProperties authProperties;

    /**
     * 公钥管理：以k-v形式配置，k为允许访问的服务名，v为允许访问的服务的公钥。前端入口方式也会作为服务名配置公钥
     */
    private Map<String, String> publicKey;

    @Data
    public static class AuthProperties {
        // 服务名：作为服务的唯一标识，可以直接取${spring.application.name}
        private String serverName;

        // 私钥：作为客户端时，在请求别人提供的接口时会使用此私钥计算签名
        private String privateKey;

        // （可选配置）前端接口：满足此规则的接口会被认为是前端调用接口
        private List<String> bizPatterns = Lists.newArrayList("/*");

        // （可选配置）内部接口：满足此规则的接口会被认为是内部调用接口
        private List<String> innerPatterns = Lists.newArrayList("/inner/*");

        // （可选配置）忽略列表：此处配置的接口不进行鉴权处理，直接放行
        private List<String> ignorePatterns = Lists.newArrayList("/open/*");

        // 允许的服务列表： 可以允许哪些服务访问本服务提供的接口
        private List<String> allowedServer = Lists.newArrayList();

        // 允许的前端入口：用来限制一些接口只能从特定的前端入口访问。取值有：*（允许所有），portal，mobile，address
        private List<String> allowedEntry = Lists.newArrayList();

        //（可选配置）内部调用token有效期（单位小时，默认24小时）
        private Integer innerTokenTtl = 24;

        // （可选配置）前端调用token有效期（单位小时，默认24小时）
        private Integer bizTokenTtl = 24;

        // （可选配置）鉴权过滤器执行顺序，如果与其他过滤器冲突，可以尝试调整此参数
        private Integer filterOrder = -1;
    }


}
