package com.mobi.mobi.apiPayload.handler;

import com.mobi.mobi.apiPayload.status.BaseErrorCode;

public class MemberHandler extends GeneralException {
    public MemberHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
