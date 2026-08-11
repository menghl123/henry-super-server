package com.henry.common.auth.config;

import com.henry.common.auth.AuthenticateService;
import com.henry.common.auth.filter.HenryHomeCommonAuthFilter;
import com.henry.common.auth.InnerApiInterceptor;
import com.henry.common.auth.authenticator.BizServiceAuthenticator;
import com.henry.common.auth.authenticator.InnerServiceAuthenticator;
import com.henry.common.HenryHomeCommonProperties;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class HenryHomeCommonAuthConfig {

    @Bean
    @ConditionalOnBean(HenryHomeCommonProperties.class)
    public BizServiceAuthenticator bizServiceAuthenticator(final HenryHomeCommonProperties commonProperties) {
        return new BizServiceAuthenticator(commonProperties);
    }

    @Bean
    @ConditionalOnBean(HenryHomeCommonProperties.class)
    public InnerServiceAuthenticator innerServiceAuthenticator(final HenryHomeCommonProperties commonProperties) {
        return new InnerServiceAuthenticator(commonProperties);
    }

    @Bean
    @ConditionalOnBean(HenryHomeCommonProperties.class)
    public AuthenticateService authenticateService(final HenryHomeCommonProperties commonProperties,
                                                   final BizServiceAuthenticator bizServiceAuthenticator,
                                                   final InnerServiceAuthenticator innerServiceAuthenticator) {
        return new AuthenticateService(commonProperties, bizServiceAuthenticator, innerServiceAuthenticator);
    }

    @Bean
    @ConditionalOnBean(HenryHomeCommonProperties.class)
    public InnerApiInterceptor innerApiInterceptor(final AuthenticateService authenticateService,
                                                   final HenryHomeCommonProperties commonProperties) {
        return new InnerApiInterceptor(authenticateService, commonProperties);
    }

    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    @AllArgsConstructor
    public static class SecurityConfig extends WebSecurityConfigurerAdapter {
        private AuthenticateService authenticateService;

        private HenryHomeCommonProperties henryHomeCommonProperties;

        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            http.csrf().disable()
                    .addFilterBefore(henryHomeCommonAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        }

        private HenryHomeCommonAuthFilter henryHomeCommonAuthFilter() {
            return new HenryHomeCommonAuthFilter(henryHomeCommonProperties, authenticateService);
        }

    }

}
