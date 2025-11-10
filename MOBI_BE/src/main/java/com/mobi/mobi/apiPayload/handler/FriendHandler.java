package com.mobi.mobi.apiPayload.handler;

import com.mobi.mobi.apiPayload.status.BaseErrorCode;

public class FriendHandler extends GeneralException {
    public FriendHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
