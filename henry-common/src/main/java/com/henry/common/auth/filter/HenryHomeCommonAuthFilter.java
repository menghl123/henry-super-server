package com.henry.common.auth.filter;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.base.Preconditions;
import com.henry.common.HenryHomeCommonProperties;
import com.henry.common.auth.AuthenticateService;
import com.henry.common.auth.exception.AuthorizationException;
import com.henry.common.response.StandardErrorCode;
import com.henry.common.response.StandardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * 认证过滤器，作为接入认证器的入口，主要负责提取token并交由认证助手进行处理。
 * 因为open-feign其实也是http请求，所以前端请求及服务间请求都会进入到此过滤器，通过判断后交由不同的认证器处理。
 */
@Slf4j
@RequiredArgsConstructor
public class HenryHomeCommonAuthFilter implements Filter {

    public final static String TOKEN_HEADER_NAME = "Authorization";
    private final HenryHomeCommonProperties henryHomeCommonProperties;
    private final AuthenticateService authenticateService;
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        HenryHomeCommonProperties.AuthProperties authProperties = henryHomeCommonProperties.getAuthProperties();
        Preconditions.checkNotNull(authProperties, "HenryHomeCommon.AuthProperties.is.empty");
        Preconditions.checkNotNull(authProperties.getServerName(), "接口鉴权需要，必须配置服务名称（yst-group.auth.serverName）");
        Preconditions.checkNotNull(authProperties.getPrivateKey(), "接口鉴权需要，必须配置服务私钥（yst-group.auth.privateKey）");
    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String servletPath = ((HttpServletRequest) servletRequest).getServletPath();
        if (isIgnoreRequest(servletPath)) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            try {
                String token = httpServletRequest.getHeader(TOKEN_HEADER_NAME);
                // 兼容标准 Authorization: Bearer <token>，也支持直接携带原始 token
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring("Bearer ".length());
                }
                if (isInnerRequest(servletPath)) {
                    //验证内部服务调用
                    authenticateService.verifyInnerToken(token);
                } else if (isBizRequest(servletPath)) {
                    //验证前端请求调用
                    authenticateService.verifyBizToken(token);
                } else {
                    throw new AuthorizationException(String.format("接口鉴权配置规则有误，请联系接口提供方！servletPath=%s", servletPath));
                }
                filterChain.doFilter(servletRequest, servletResponse);
            } catch (AuthorizationException e) {
                servletResponse.setContentType("application/json;charset=utf-8");
                StandardResponse<Object> res = StandardResponse.failed(StandardErrorCode.HRY0002, e.getMessage());
                servletResponse.getWriter().write(JSONUtil.toJsonStr(res));
            } finally {
                SecurityContextHolder.clearContext();
            }
        }

    }

    /**
     * 是否业务请求
     *
     * @param servletPath 请求路径
     * @return 匹配结果
     */
    private boolean isBizRequest(String servletPath) {
        return match(servletPath, henryHomeCommonProperties.getAuthProperties().getBizPatterns());
    }

    /**
     * 是否内部请求
     *
     * @param servletPath 请求路径
     * @return 匹配结果
     */
    private boolean isInnerRequest(String servletPath) {
        return match(servletPath, henryHomeCommonProperties.getAuthProperties().getInnerPatterns());
    }

    /**
     * 是否忽略认证的请求
     *
     * @param servletPath 请求路径
     * @return 匹配结果
     */
    private boolean isIgnoreRequest(String servletPath) {
        return match(servletPath, henryHomeCommonProperties.getAuthProperties().getIgnorePatterns());
    }

    /**
     * 路径匹配判断
     *
     * @param servletPath 请求路径
     * @return 匹配结果
     */
    private boolean match(String servletPath, List<String> patterns) {
        if (CollectionUtil.isNotEmpty(patterns)) {
            for (String pattern : patterns) {
                if (antPathMatcher.match(pattern, servletPath)) {
                    return true;
                }
            }
        }
        return false;
    }


}