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

    // 유효성 검사 에러(메시지는 @interface의 message로 처리)
    VALIDATOR_ERROR(HttpStatus.BAD_REQUEST,"VALID400",null),

    // 멤버 관련 에러
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4300", "사용자가 없습니다"),

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


    // 테스트 용 응답
    TEST_FAIL(HttpStatus.BAD_REQUEST, "TEST400", "사용자 정의 실패 응답입니다."),

    ;

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
