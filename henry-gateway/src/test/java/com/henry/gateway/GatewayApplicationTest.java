package com.henry.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 上下文加载测试：验证网关配置可正确装配、user/file 路由定义可加载（不依赖下游服务在线）
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayApplicationTest {

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Test
    void contextLoads() {
        assertThat(routeDefinitionLocator).isNotNull();
    }

    @Test
    void userAndFileRoutesAreRegistered() {
        final List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions().collectList().block();
        assertThat(routes).isNotNull()
                .anyMatch(r -> "henry-user".equals(r.getId()))
                .anyMatch(r -> "henry-file".equals(r.getId()));
    }
}
