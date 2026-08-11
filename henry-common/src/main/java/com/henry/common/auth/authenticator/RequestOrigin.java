package com.henry.common.auth.authenticator;

import lombok.Getter;

@Getter
public enum RequestOrigin {
    BIZ_SERVICE("biz"),
    INNER_SERVICE("inner");

    RequestOrigin(String origin) {
        this.origin = origin;
    }

    private String origin;
}
