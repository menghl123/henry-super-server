package com.henry.common.auth.authenticator;

import cn.hutool.json.JSONUtil;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.henry.common.auth.exception.AuthorizationException;
import com.henry.common.auth.model.UserToken;
import com.henry.common.HenryHomeCommonProperties;
import com.henry.common.util.WebIPUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

public class BizServiceAuthenticator extends AbstractAuthenticator {
    private final static String LOCALHOST_IP_FAKE = "0:0:0:0:0:0:0:1";

    /**
     * 黑名单（存放黑名单用户的openId）
     */
    private final Set<Long> blacklist = Sets.newHashSet();

    public BizServiceAuthenticator(HenryHomeCommonProperties groupProperties) {
        super(RequestOrigin.BIZ_SERVICE, groupProperties);
    }


    @Override
    protected JWTCreator.Builder addMoreAuthInfo(JWTCreator.Builder builder) {
        final String ip = WebIPUtil.getCurrentIpAddress()
                .orElseThrow(() -> new AuthorizationException("request.source.ip.is.null"));
        return builder.withClaim(ORIGIN_IP_CLAIM_NAME, ip);
    }

    @Override
    protected void verifyMoreAuthInfo(DecodedJWT decodedJWT) throws AuthorizationException {
        //验证token的持有者IP与实际请求客户端的IP是否一致
//        String jwtIp = decodedJWT.getClaim(ORIGIN_IP_CLAIM_NAME).asString();
//        String clientIp = WebUtil.getCurrentIpAddress().orElseThrow(() -> new AuthorizationException("request.source.ip.is.null"));
//        if (!Objects.equals(LOCALHOST_IP_FAKE, jwtIp) && !Objects.equals(jwtIp, clientIp)) {
//            throw new AuthorizationException(String.format("source.ip.is.not.equals.before！jwtIp=%s，clientIp=%s", jwtIp, clientIp));
//        }
    }

    @Override
    protected boolean allowed(final String issuer) {
        final List<String> allowedEntries = commonProperties.getAuthProperties().getAllowedEntry();
        return allowedEntries.contains("*") || allowedEntries.contains(issuer);
    }

    @Override
    protected void afterAuth(Object payload) {
        super.afterAuth(payload);
        UserToken userToken = JSONUtil.toBean((String) payload,UserToken.class);
        if (blacklist.contains(userToken.getUserId())) {
            throw new AuthorizationException(String.format("该用户已被加入黑名单！userId=%s", userToken.getUserId()));
        }
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(userToken, null, Lists.newArrayList()));
    }
}