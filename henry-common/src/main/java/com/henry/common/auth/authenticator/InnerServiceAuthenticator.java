package com.henry.common.auth.authenticator;

import com.henry.common.HenryHomeCommonProperties;

public class InnerServiceAuthenticator extends AbstractAuthenticator {
    public InnerServiceAuthenticator(HenryHomeCommonProperties groupProperties) {
        super(RequestOrigin.INNER_SERVICE, groupProperties);
    }


    @Override
    protected boolean allowed(final String issuer) {
        return commonProperties.getAuthProperties().getAllowedServer().contains(issuer);
    }
}