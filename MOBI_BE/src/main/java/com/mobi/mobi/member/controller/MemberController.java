package com.mobi.mobi.member.controller;

import com.mobi.mobi.apiPayload.ApiResponse;
import com.mobi.mobi.apiPayload.handler.MemberHandler;
import com.mobi.mobi.apiPayload.status.ErrorStatus;
import com.mobi.mobi.apiPayload.status.SuccessStatus;
import com.mobi.mobi.common.service.S3Service;
import com.mobi.mobi.member.dto.*;
import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.entity.enums.RelationStatus;
import com.mobi.mobi.member.repository.MemberRepository;
import com.mobi.mobi.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Member API", description = "회원 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    @GetMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 확인 API", description = "입력한 닉네임이 이미 사용 중인지 확인합니다.")
    public ApiResponse<NicknameCheckResponseDTO> checkNickname(
            @RequestParam("nickname") String nickname) {

        NicknameCheckResponseDTO responseDTO = memberService.checkNicknameDuplication(nickname);
        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO);
    }

    @GetMapping("/profile/{memberId}")
    @Operation(summary = "다른 사용자 프로필 조회 API", description = "URL에 포함된 memberId를 사용하여 특정 사용자의 프로필 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MemberProfileResponseDTO> getProfile(
            @AuthenticationPrincipal User user,
            @Parameter(description = "조회할 사용자의 ID") @PathVariable("memberId") Long profileId
    ) {
        Long viewerId = Long.parseLong(user.getUsername());
        MemberProfileResponseDTO responseDTO = memberService.getProfile(viewerId, profileId);
        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO);
    }

    @GetMapping("/profile/my")
    @Operation(summary = "내 프로필 조회 API", description = "현재 로그인된 사용자의 프로필을 조회합니다. (JWT 토큰 필요)")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MemberProfileResponseDTO> getMyProfile(
            @AuthenticationPrincipal User user
    ) {
        Long memberId = Long.parseLong(user.getUsername());
        MemberProfileResponseDTO responseDTO = memberService.getMyProfile(memberId);
        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO);
    }

    @GetMapping("/search")
    @Operation(summary = "닉네임으로 사용자 검색 API", description = "친구 추가를 위해 닉네임으로 사용자를 검색합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<MemberSearchResponseDTO.MemberInfo>> searchMembers(
            @AuthenticationPrincipal User user,
            @RequestParam("nickname") String nickname) {
        Long viewerId = Long.parseLong(user.getUsername());
        List<MemberSearchResponseDTO.MemberInfo> responseDTO = memberService.searchMembersByNickname(viewerId, nickname);
        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO);
    }


    @PatchMapping("/profile/describe")
    @Operation(summary = "내 프로필 한줄메시지 수정 API", description = "로그인한 사용자의 한줄메시지를 수정합니다. (JWT 토큰 필요)")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MemberProfileResponseDTO> updateProfileDescribe(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateDescribeRequestDTO request) {
        Long memberId = Long.parseLong(user.getUsername());
        MemberProfileResponseDTO responseDTO = memberService.updateProfileDescribe(memberId, request.getProfileDescribe());
        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO);
    }

    @PatchMapping("/profile/avatar")
    @Operation(summary = "내 프로필 아바타 수정 API", description = "로그인한 사용자의 아바타를 수정합니다. (JWT 토큰 필요)")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MemberProfileResponseDTO> updateAvatar(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateAvatarRequestDTO request) {
        Long memberId = Long.parseLong(user.getUsername());
        MemberProfileResponseDTO responseDTO = memberService.updateAvatar(memberId, request.getAvatar());
        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO);
    }

    @PatchMapping(value = "/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "내 프로필 이미지 수정 API", description = "로그인한 사용자의 프로필 이미지를 수정합니다. form-data 형식으로 'image' 키에 파일을 담아 요청해주세요. (JWT 토큰 필요)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 이미지 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미지 파일이 없거나, 파일 형식이 잘못됨", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<MemberProfileResponseDTO> updateProfileImage(
            @AuthenticationPrincipal User user,
            @Parameter(
                    description = "업로드할 이미지 파일",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("image") MultipartFile imageFile) { // @RequestParam -> @RequestPart로 변경
        Long memberId = Long.parseLong(user.getUsername());
        MemberProfileResponseDTO responseDTO = memberService.updateProfileImage(memberId, imageFile);
        return ApiResponse.onSuccess(SuccessStatus._OK, responseDTO);
    }
}