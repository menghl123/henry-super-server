package com.henry.common.ddd.adapter;

import cn.hutool.core.exceptions.ValidateException;
import com.henry.common.auth.exception.AuthorizationException;
import com.henry.common.response.StandardErrorCode;
import com.henry.common.response.StandardResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice(basePackages = {"com.henry"})
@AllArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = {
            MethodArgumentNotValidException.class,
    })
    public StandardResponse<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        exception.printStackTrace();
        return StandardResponse.failed(StandardErrorCode.HRY0003,
                StandardErrorCode.HRY0003.getErrorMsg(exception.getBindingResult().getFieldError().getDefaultMessage()));
    }

    @ResponseBody
    @ExceptionHandler(value = {
            ValidateException.class, IllegalStateException.class, IllegalArgumentException.class,
    })
    public StandardResponse<Object> handleValidateException(Exception exception) {
        exception.printStackTrace();
        return StandardResponse.failed(StandardErrorCode.HRY0003, StandardErrorCode.HRY0003.getErrorMsg(exception.getMessage()));
    }

    @ResponseBody
    @ExceptionHandler(value = {AuthorizationException.class})
    public StandardResponse<Object> handleAuthorizationException(Exception exception) {
        exception.printStackTrace();
        return StandardResponse.failed(StandardErrorCode.HRY0002, StandardErrorCode.HRY0002.getErrorMsg(exception.getMessage()));
    }

    @ResponseBody
    @ExceptionHandler(value = {Exception.class})
    public StandardResponse<Object> handleException(Exception exception) {
        exception.printStackTrace();
        return StandardResponse.failed(StandardErrorCode.HRY0001, StandardErrorCode.HRY0001.getErrorMsg(exception.getMessage()));
    }

}
