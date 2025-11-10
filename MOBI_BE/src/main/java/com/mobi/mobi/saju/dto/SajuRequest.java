// SajuRequest.java
package com.mobi.mobi.saju.dto;

import lombok.Getter;
import java.time.LocalDate;

@Getter
public class SajuRequest {
    private LocalDate birthDate; // "yyyy-MM-dd"
    private String stockName;
}