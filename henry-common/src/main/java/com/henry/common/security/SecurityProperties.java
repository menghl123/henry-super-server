package com.henry.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 鉴权开关配置，前缀 app.security
 */
@Data
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    /** 是否启用 token 鉴权拦截器 */
    private boolean enabled = false;

    /** 放行路径（Ant 风格） */
    private List<String> excludePaths = new ArrayList<>();
}
