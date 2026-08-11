package com.henry.common.response;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum StandardErrorCode implements ErrorCodeType {
    HRY0001("系统异常:{0}"),
    HRY0002("认证异常:{0}"),
    HRY0003("参数异常:{0}"),
    ;

    private final String errorMsg;

    @Override
    public String getErrorMsg() {
        return this.errorMsg;
    }

    @Override
    public String getErrorCode() {
        return this.name();
    }

}
