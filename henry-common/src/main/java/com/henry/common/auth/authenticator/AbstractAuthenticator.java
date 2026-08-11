package com.henry.common.auth.authenticator;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.henry.common.auth.exception.AuthorizationException;
import com.henry.common.HenryHomeCommonProperties;
import com.henry.common.util.RSASerializeUtil;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class AbstractAuthenticator {
    private final static String HENRY_HOME_LOGIN_SERVER = "henry-home-login";
    final public static String REQUEST_ORIGIN_CLAIM_NAME = "origin";
    final public static String PAYLOAD_CLAIM_NAME = "data";
    final public static String ORIGIN_IP_CLAIM_NAME = "ip";

    protected HenryHomeCommonProperties commonProperties;
    protected RequestOrigin requestOrigin;

    private final Cache<String, Algorithm> algorithmCache = CacheBuilder.newBuilder()
            .initialCapacity(5)
            .maximumSize(50L)
            .expireAfterAccess(24L, TimeUnit.HOURS)
            .build();

    protected AbstractAuthenticator(RequestOrigin requestOrigin,
                                    HenryHomeCommonProperties commonProperties) {
        Preconditions.checkNotNull(commonProperties, "henryHomeCommonProperties.is.null");
        Preconditions.checkNotNull(requestOrigin, "requestOrigin.is.null");
        this.commonProperties = commonProperties;
        this.requestOrigin = requestOrigin;
    }

    /**
     * 生成token
     *
     * @param payload 需要携带的数据
     * @param issuer  颁发者
     * @return 认证字符串
     */
    public String generateToken(final Object payload, final String issuer) {
        //1、生成jwt基本结构
        final HenryHomeCommonProperties.AuthProperties authProperties = commonProperties.getAuthProperties();

        final Integer tokenItl = Objects.equals(this.requestOrigin, RequestOrigin.BIZ_SERVICE) ? authProperties.getBizTokenTtl() : authProperties.getInnerTokenTtl();

        final Date now = new Date();
        JWTCreator.Builder builder = JWT.create()
                .withIssuer(issuer) // 签发者
                .withIssuedAt(now) // 签发时间
                .withExpiresAt(DateUtil.offsetHour(now, tokenItl)) // 过期时间
                .withClaim(REQUEST_ORIGIN_CLAIM_NAME, requestOrigin.getOrigin())
                .withClaim(PAYLOAD_CLAIM_NAME, JSONUtil.toJsonStr(payload));
        //2、添加额外用于认证的信息并生成token
        return addMoreAuthInfo(builder).sign(loadPrivateAlgorithm());
    }

    /**
     * 加载私钥算法
     */
    private Algorithm loadPrivateAlgorithm() {
        final HenryHomeCommonProperties.AuthProperties authProperties = commonProperties.getAuthProperties();
        return Algorithm.RSA256(null, RSASerializeUtil.deSerializePrivateKey(authProperties.getPrivateKey()));
    }

    /**
     * 加载公钥算法
     *
     * @param issuer 签发者，bizToken时是entryCode，innerToken时是serverName
     */
    // todo 生成biz token时，issuer是entry名字，在这里生成token有bug
    private Algorithm loadPublicAlgorithm(String issuer) {
        final String serverName = Objects.equals(this.requestOrigin, RequestOrigin.BIZ_SERVICE) ? HENRY_HOME_LOGIN_SERVER : issuer;

        final Map<String, String> pubKeyMap = commonProperties.getPublicKey();
        try {
            return algorithmCache.get(serverName, () -> Algorithm.RSA256(RSASerializeUtil.deSerializePublicKey(pubKeyMap.get(serverName)), null));
        } catch (ExecutionException e) {
            throw new AuthorizationException(String.format("jwt算法加载失败，请检查是否已经正确配置公私钥。serverName=%s", serverName));
        }
    }

    /**
     * 验证token
     *
     * @param token
     */
    public void verifyToken(final String token) throws AuthorizationException {
        if (!StringUtils.hasLength(token)) {
            throw new AuthorizationException("http.header.authorization.is.null");
        }
        //解析认证字符串，如果解析失败，则认证失败
        final DecodedJWT decodedJWT = JWT.decode(token);
        //验证发布者是否在允许列表中
        final String jwtIssuer = decodedJWT.getIssuer();
        if (!allowed(jwtIssuer)) {
            throw new AuthorizationException("request.source.not.allowed");
        }
        //请求源与token的目标源是否一致，防止token混用
        final String jwtOrigin = decodedJWT.getClaim(REQUEST_ORIGIN_CLAIM_NAME).asString();
        if (!requestOrigin.getOrigin().equals(jwtOrigin)) {
            throw new AuthorizationException("request.origin.invalid");
        }
        //前端请求和内部请求分别校验各自更多的规则
        verifyMoreAuthInfo(decodedJWT);
        try {
            //token签名校验
            JWT.require(loadPublicAlgorithm(jwtIssuer)).build().verify(token);
            //验证通过后置处理
            afterAuth(decodedJWT.getClaim(PAYLOAD_CLAIM_NAME).asString());
        } catch (Exception e) {
            throw new AuthorizationException(String.format("token.verify.failed，reason:：%s", e.getMessage()));
        }
    }


    /**
     * 判断token发布者是否在允许列表中
     *
     * @param issuer token颁发者
     */
    protected abstract boolean allowed(final String issuer);

    /**
     * 需要向token中放入的额外的用于认证的信息
     *
     * @param builder jwt构造器
     * @return jwt构造器
     */
    protected JWTCreator.Builder addMoreAuthInfo(JWTCreator.Builder builder) {
        return builder;
    }

    /**
     * 前端接口与内部接口额外的差异化校验
     *
     * @param decodedJWT 认证信息
     * @return 校验结果
     */
    protected void verifyMoreAuthInfo(final DecodedJWT decodedJWT) throws AuthorizationException {
    }

    /**
     * 认证完成之后进行一些其他操作
     *
     * @param payload token负载数据
     */
    protected void afterAuth(final Object payload) {
    }
}