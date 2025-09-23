package com.mobi.mobi.mydata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyDataListResponseDTO {

    private List<MyDataResponseDTO> myDataList;
}
