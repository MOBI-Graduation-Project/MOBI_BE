package com.mobi.mobi.apiPayload.status;

import com.mobi.mobi.apiPayload.code.ErrorReasonDTO;

public interface BaseErrorCode {

    ErrorReasonDTO getReason();
    ErrorReasonDTO getReasonHttpStatus();
}
