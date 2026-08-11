package com.henry.common.response;

import java.io.Serializable;
import java.text.MessageFormat;

public interface ErrorCodeType extends Serializable {
    String getErrorMsg();

    default String getErrorCode() {
        return this.toString();
    }

    default String getErrorMsg(final Object... params) {
        return MessageFormat.format(this.getErrorMsg(), params);
    }

}
