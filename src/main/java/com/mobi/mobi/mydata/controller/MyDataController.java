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
import org.springframework.security.core.userdetails.User;
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
            @AuthenticationPrincipal User user,
            @Valid @RequestBody MyDataRequestDTO requestDTO) {
        Long memberId = Long.parseLong(user.getUsername());
        MyDataResponseDTO responseDTO = myDataService.addMyData(memberId, requestDTO);
        return ApiResponse.onSuccess(SuccessStatus._CREATED, responseDTO);
    }

    @GetMapping("")
    @Operation(summary = "보유 주식 목록 조회 API", description = "사용자가 보유한 모든 주식 정보를 조회합니다.")
    public ApiResponse<MyDataListResponseDTO> showMyData(
            @AuthenticationPrincipal User user) {
        Long memberId = Long.parseLong(user.getUsername());
        MyDataListResponseDTO responseDTO = myDataService.getMyData(memberId);
        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO);
    }

    @PutMapping("/{myDataId}")
    @Operation(summary = "보유 주식 정보 수정 API", description = "사용자가 보유한 특정 주식의 정보를 수정합니다.")
    @Parameter(name = "myDataId", description = "마이데이터 ID", required = true)
    public ApiResponse<MyDataResponseDTO> updateMyData(
            @AuthenticationPrincipal User user,
            @PathVariable Long myDataId,
            @Valid @RequestBody MyDataRequestDTO requestDTO) {
        Long memberId = Long.parseLong(user.getUsername());
        MyDataResponseDTO responseDTO = myDataService.updateMyData(memberId, myDataId, requestDTO);
        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO);
    }

    @DeleteMapping("/{myDataId}")
    @Operation(summary = "보유 주식 정보 삭제 API", description = "사용자가 보유한 특정 주식의 정보를 삭제합니다.")
    @Parameter(name = "myDataId", description = "마이데이터 ID", required = true)
    public ApiResponse<?> deleteMyData(
            @AuthenticationPrincipal User user,
            @PathVariable Long myDataId) {
        Long memberId = Long.parseLong(user.getUsername());
        myDataService.deleteMyData(memberId, myDataId);
        return ApiResponse.onSuccess(SuccessStatus._OK, null);
    }
}
