package com.henry.common.ddd.adapter;

import com.henry.common.response.StandardResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;

@RestController
@RequestMapping("health")
public class HealthController {
    @Value("${spring.application.name}")
    private String applicationName;

    @GetMapping
    public StandardResponse<String> health() {
        return StandardResponse.success(MessageFormat.format("{0} is running.", this.applicationName));
    }
}
