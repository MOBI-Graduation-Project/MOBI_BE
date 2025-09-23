package com.mobi.mobi.apiPayload.status;


import com.mobi.mobi.apiPayload.code.ReasonDTO;

public interface BaseCode {

    ReasonDTO getReason();
    ReasonDTO getReasonHttpStatus();
}
