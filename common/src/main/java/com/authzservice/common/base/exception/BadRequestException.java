package com.authzservice.common.base.exception;

import com.authzservice.common.base.constant.ErrorCode;

public class BadRequestException extends GlobalException {

    public BadRequestException(String message) {
        super(ErrorCode.INVALID_REQUEST, message);
    }
}
