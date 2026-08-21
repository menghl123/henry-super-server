package com.henry.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 网关健康检查：与 user/file 服务 henry-common 提供的 /health 约定保持一致
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        final Map<String, String> result = new LinkedHashMap<>();
        result.put("status", "UP");
        return result;
    }
}
