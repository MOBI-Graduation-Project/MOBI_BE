package com.mobi.mobi.external.krx;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KrxResponse {
    private KrxHeader header;
    private KrxBody body;
}