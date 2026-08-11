package com.henry.common.util;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;

public class WebIPUtil {
    private static final String IP_UNKNOWN = "unknown";
    private static final String[] HEADER_NAMES = new String[]{"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
    private static final int MANY_IP_LENGTH = 15;

    private WebIPUtil() {
    }

    /**
     * 获取当前请求的ip地址
     */
    public static Optional<String> getCurrentIpAddress() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .map(attr -> (ServletRequestAttributes)attr)
                .map(ServletRequestAttributes::getRequest)
                .map(WebIPUtil::getIpAddress);
    }

    /**
     * 从request从获取ip地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        if (Objects.isNull(request)) {
            return null;
        }
        String ip = null;
        String[] ips = HEADER_NAMES;
        int index = ips.length;

        for (int i = 0; i < index; ++i) {
            String headerName = ips[i];
            ip = request.getHeader(headerName);
            if (!StringUtils.isEmpty(ip)) {
                break;
            }
        }

        if (!StringUtils.hasLength(ip)) {
            ip = request.getRemoteAddr();
        }

        if (Objects.nonNull(ip) && ip.length() > MANY_IP_LENGTH) {
            ips = ip.split(",");

            for (index = 0; index < ips.length; ++index) {
                String strIp = ips[index];
                if (!IP_UNKNOWN.equalsIgnoreCase(strIp)) {
                    ip = strIp;
                    break;
                }
            }
        }

        return ip;
    }
}
