package com.henry.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardResponse<T> {

    public static final String SUCCESS_CODE = "SUC0000";

    /**
     * 前三位是系统编码，后四位自定义编码
     * 如：SUC000；HRY0001
     */
    private String returnCode;

    private String errorMsg;

    private T body;

    public static <T> StandardResponse<T> success() {
        return new StandardResponse<T>(SUCCESS_CODE, null, null);
    }

    public static <T> StandardResponse<T> success(final T body) {
        return new StandardResponse<T>(SUCCESS_CODE, null, body);
    }

    public static <T> StandardResponse<T> failed(final ErrorCodeType errorCode, final String errorMsg) {
        return new StandardResponse<T>(errorCode.getErrorCode(), errorMsg, null);
    }

}
