package com.mobi.mobi.saju.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SajuRequest {
    private LocalDate birthDate; // "yyyy-MM-dd"
    private String stockName;
}