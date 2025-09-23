package com.mobi.mobi.mydata.controller;

import com.mobi.mobi.apiPayload.ApiResponse;
import com.mobi.mobi.apiPayload.status.SuccessStatus;
import com.mobi.mobi.mydata.dto.MyDataListResponseDTO;
import com.mobi.mobi.mydata.dto.MyDataRequestDTO;
import com.mobi.mobi.mydata.dto.MyDataResponseDTO;
import com.mobi.mobi.mydata.service.MyDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MyData API", description = "마이데이터(보유 주식) 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/mydata")
@SecurityRequirement(name = "bearerAuth")
public class MyDataController {

    private final MyDataService myDataService;

    @PostMapping("")
    @Operation(summary = "보유 주식 등록 API", description = "사용자가 보유한 주식 정보를 시스템에 등록합니다.")
    public ApiResponse<MyDataResponseDTO> addMyData(
            @AuthenticationPrincipal String memberId, // 수정됨
            @Valid @RequestBody MyDataRequestDTO requestDTO) {
        MyDataResponseDTO responseDTO = myDataService.addMyData(Long.parseLong(memberId), requestDTO); // 수정됨
        return ApiResponse.onSuccess(SuccessStatus._CREATED, responseDTO); // 생성 성공은 _CREATED가 더 적절합니다.
    }

    @GetMapping("")
    @Operation(summary = "보유 주식 목록 조회 API", description = "사용자가 보유한 모든 주식 정보를 조회합니다.")
    public ApiResponse<MyDataListResponseDTO> showMyData(
            @AuthenticationPrincipal String memberId) { // 수정됨
        MyDataListResponseDTO responseDTO = myDataService.getMyData(Long.parseLong(memberId)); // 수정됨
        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO); // 조회 성공은 _OK가 더 적절합니다.
    }

    @PutMapping("/{myDataId}")
    @Operation(summary = "보유 주식 정보 수정 API", description = "사용자가 보유한 특정 주식의 정보를 수정합니다.")
    @Parameter(name = "myDataId", description = "마이데이터 ID", required = true)
    public ApiResponse<MyDataResponseDTO> updateMyData(
            @AuthenticationPrincipal String memberId, // 수정됨
            @PathVariable Long myDataId,
            @Valid @RequestBody MyDataRequestDTO requestDTO) {
        MyDataResponseDTO responseDTO = myDataService.updateMyData(Long.parseLong(memberId), myDataId, requestDTO); // 수정됨
        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO);
    }

    @DeleteMapping("/{myDataId}")
    @Operation(summary = "보유 주식 정보 삭제 API", description = "사용자가 보유한 특정 주식의 정보를 삭제합니다.")
    @Parameter(name = "myDataId", description = "마이데이터 ID", required = true)
    public ApiResponse<?> deleteMyData(
            @AuthenticationPrincipal String memberId, // 수정됨
            @PathVariable Long myDataId) {
        myDataService.deleteMyData(Long.parseLong(memberId), myDataId); // 수정됨
        return ApiResponse.onSuccess(SuccessStatus._OK, null);
    }
}
