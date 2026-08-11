package com.henry.common.auth.exception;

/**
 * 认证错误
 */
public class AuthorizationException extends RuntimeException {
    public AuthorizationException(final String message) {
        super(message);
    }

    public AuthorizationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AuthorizationException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AuthorizationException(final Throwable cause) {
        super(cause);
    }
}
