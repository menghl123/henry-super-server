package com.henry.common.auth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.henry.common.auth.exception.AuthorizationException;
import com.henry.common.auth.filter.HenryHomeCommonAuthFilter;
import com.henry.common.HenryHomeCommonProperties;
import com.henry.common.auth.authenticator.RequestOrigin;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class InnerApiInterceptor implements RequestInterceptor {
    private final AuthenticateService authenticateService;
    private final Cache<String, String> tokenCache;

    public InnerApiInterceptor(final AuthenticateService authenticateService, HenryHomeCommonProperties ystGroupProperties){
        this.authenticateService = authenticateService;
        int innerTokenTtl = ystGroupProperties.getAuthProperties().getInnerTokenTtl();
        tokenCache = CacheBuilder.newBuilder()
                .initialCapacity(5)
                .maximumSize(50L)
                //缓存有效期设置成比token有效时间少一个小时，以保证客户端发送的token不会过期
                .expireAfterWrite(innerTokenTtl > 1 ? innerTokenTtl - 1 : 1, TimeUnit.HOURS)
                .build();
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token;
        try {
            token = tokenCache.get(RequestOrigin.INNER_SERVICE.getOrigin(), () ->
                    authenticateService.generateInnerToken(Maps.newHashMap())
            );
        } catch (ExecutionException e) {
            throw new AuthorizationException("不能正常发起内部接口调用，原因：生成token失败！", e);
        }
        requestTemplate.header(HenryHomeCommonAuthFilter.TOKEN_HEADER_NAME, token);
    }
}
