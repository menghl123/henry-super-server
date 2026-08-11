package com.henry.common.auth;

import com.google.common.base.Preconditions;
import com.henry.common.auth.authenticator.BizServiceAuthenticator;
import com.henry.common.auth.authenticator.InnerServiceAuthenticator;
import com.henry.common.auth.exception.AuthorizationException;
import com.henry.common.auth.model.UserToken;
import com.henry.common.HenryHomeCommonProperties;
import lombok.AllArgsConstructor;

/**
 * 认证服务
 */
@AllArgsConstructor
public class AuthenticateService {
    /**
     * 认证相关配置
     */
    private HenryHomeCommonProperties commonProperties;

    /**
     * 前端请求认证器
     */
    private BizServiceAuthenticator bizServiceAuthenticator;

    /**
     * 内部请求认证器
     */
    private InnerServiceAuthenticator innerServiceAuthenticator;


    /**
     * 生成内部请求token
     *
     * @param payload 需要携带的数据
     * @return 认证字符串
     */
    public String generateInnerToken(final Object payload) {
        return innerServiceAuthenticator.generateToken(payload, commonProperties.getAuthProperties().getServerName());
    }

    /**
     * 生成前端请求token
     *
     * @param localToken 用户信息
     * @param issuer     前端入口类型， 可选值：portal、mobile、address
     * @return 认证字符串
     */
    public String generateBizToken(final UserToken localToken, final String issuer) {
        Preconditions.checkNotNull(localToken, "生成前端请求token时，传入的用户信息不能为空！");
        Preconditions.checkNotNull(issuer, "生成前端请求token时，传入的前端入口类型不能为空！");
        return bizServiceAuthenticator.generateToken(localToken, issuer);
    }

    /**
     * 校验内部请求token是否有效
     *
     * @param token
     */
    public void verifyInnerToken(String token) throws AuthorizationException {
        innerServiceAuthenticator.verifyToken(token);
    }

    /**
     * 校验前端请求token是否有效
     *
     * @param token
     */
    public void verifyBizToken(String token) throws AuthorizationException {
        bizServiceAuthenticator.verifyToken(token);
    }
}