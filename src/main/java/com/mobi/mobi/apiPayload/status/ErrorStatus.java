package com.mobi.mobi.apiPayload.status;

import com.mobi.mobi.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // === Google OAuth / 로그인 관련 에러 ===
    OAUTH_PROVIDER_ERROR(HttpStatus.BAD_REQUEST, "OAUTH4001", "구글 OAuth 서버와 통신 중 오류가 발생했습니다."),
    OAUTH_INVALID_CODE(HttpStatus.BAD_REQUEST, "OAUTH4002", "유효하지 않은 인증 코드입니다."),
    OAUTH_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_REQUEST, "OAUTH4003", "구글 토큰 교환에 실패했습니다."),
    OAUTH_USERINFO_FAILED(HttpStatus.BAD_REQUEST, "OAUTH4004", "구글 사용자 정보를 가져오지 못했습니다."),

    // === JWT 관련 에러 ===
    JWT_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JWT5001", "JWT 발급 중 오류가 발생했습니다."),
    JWT_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT4001", "리프레시 토큰이 만료되었습니다."),
    JWT_REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "JWT4002", "리프레시 토큰이 존재하지 않습니다."),


    // 유효성 검사 에러(메시지는 @interface의 message로 처리)
    VALIDATOR_ERROR(HttpStatus.BAD_REQUEST,"VALID400",null),

    // 멤버 관련 에러
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4300", "사용자가 없습니다"),
    MEMBER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "MEMBER4001", "인증되지 않은 사용자입니다. 토큰을 확인해주세요."),

    // 프로필 이미지 관련 에러 추가
    MEMBER_PROFILE_IMAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4003", "프로필 이미지를 찾을 수 없습니다."),
    MEMBER_PROFILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER5001", "프로필 이미지 업로드에 실패했습니다."),
    MEMBER_PROFILE_INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "MEMBER4004", "잘못된 파일 형식입니다."),

    // 친구 관련 에러
    FRIEND_REQUEST_ALREADY_SENT(HttpStatus.CONFLICT, "FRIEND4314", "이미 친구 요청을 보냈습니다."),
    ALREADY_FRIENDS(HttpStatus.CONFLICT, "FRIEND4315", "이미 친구 상태입니다."),
    INVALID_FRIEND_REQUEST(HttpStatus.BAD_REQUEST, "FRIEND4316", "자기 자신에게 친구 요청을 보낼 수 없습니다."),
    FRIEND_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIEND4317", "친구 요청 대상을 찾을 수 없습니다."),
    FRIEND_REQUEST_NOT_PENDING(HttpStatus.BAD_REQUEST, "FRIEND4405", "이미 수락되었거나 처리된 요청입니다."),
    NOT_FRIEND(HttpStatus.NOT_FOUND, "FRIEND4406", "친구 관계가 존재하지 않습니다."),
    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIEND4407", "해당 친구 요청을 찾을 수 없습니다."),
    INVALID_FRIEND_REQUEST_STATUS(HttpStatus.BAD_REQUEST, "FRIEND4408", "요청 상태가 유효하지 않습니다."),
    SEARCH_QUERY_REQUIRED(HttpStatus.BAD_REQUEST, "FRIEND4001", "검색어를 입력해주세요."),
    SEARCH_QUERY_TOO_SHORT(HttpStatus.BAD_REQUEST, "FRIEND4002", "검색어는 1자 이상 입력해주세요."),

    // 채팅 관련 에러
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT4001","채팅방이 없습니다."),
    ALREADY_IN_CHAT_ROOM(HttpStatus.ALREADY_REPORTED,"CHAT 4002","이미 채팅방에 참여중입니다."),

    //마이데이터 관련 에러
    INVALID_KOREAN_STOCK_CODE(HttpStatus.BAD_REQUEST, "MYDATA4001", "한국 주식 코드는 6자리 숫자여야 합니다."),
    INVALID_ENGLISH_STOCK_CODE(HttpStatus.BAD_REQUEST, "MYDATA4002", "미국 주식 코드는 1~16자리의 대문자 알파벳(티커)이어야 합니다."),
    MYDATA_NOT_FOUND(HttpStatus.NOT_FOUND, "MYDATA4003", "해당 주식 정보를 찾을 수 없습니다."),
    MYDATA_ALREADY_EXISTS(HttpStatus.CONFLICT, "MYDATA4004", "이미 등록된 주식입니다."),


    // 테스트 용 응답
    TEST_FAIL(HttpStatus.BAD_REQUEST, "TEST400", "사용자 정의 실패 응답입니다."),


    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK4001", "해당 주식을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }
    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
