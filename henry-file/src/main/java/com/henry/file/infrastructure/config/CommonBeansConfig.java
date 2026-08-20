package com.henry.file.infrastructure.config;

import com.henry.common.ddd.adapter.GlobalExceptionHandler;
import com.henry.common.ddd.adapter.HealthController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 引入 henry-common 的通用能力（统一异常处理、健康检查）。
 * <p>
 * 不整包扫描 com.henry，避免把 henry-common 中依赖 MyBatis-Plus / Spring Security 的自动配置
 * 带进无数据库、无鉴权需求的文件服务。
 */
@Configuration
@Import({GlobalExceptionHandler.class, HealthController.class})
public class CommonBeansConfig {
}
