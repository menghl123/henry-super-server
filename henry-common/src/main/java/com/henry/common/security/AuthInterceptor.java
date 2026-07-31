package com.henry.common.security;

import com.henry.common.result.Result;
import com.henry.common.result.ResultCode;
import com.henry.common.util.JsonUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Token 鉴权拦截器：解析 Authorization: Bearer xxx，成功后写入 UserContext
 */
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final JwtProperties properties;

    public AuthInterceptor(JwtUtils jwtUtils, JwtProperties properties) {
        this.jwtUtils = jwtUtils;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader(properties.getHeader());
        String token = null;
        if (header != null && header.startsWith(properties.getTokenPrefix())) {
            token = header.substring(properties.getTokenPrefix().length());
        }
        if (token == null || !jwtUtils.validate(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JsonUtils.toJson(Result.fail(ResultCode.UNAUTHORIZED)));
            return false;
        }
        UserContext.set(jwtUtils.parseToken(token));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
