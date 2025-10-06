package cap.math.apiPayload.code.status;

import cap.math.apiPayload.code.BaseErrorCode;
import cap.math.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    // For test
    TEMP_EXCEPTION(HttpStatus.BAD_REQUEST, "TEMP4001", "이거는 테스트"),

    // 로그인 관련 에러
    LOGIN_ERROR_ID(HttpStatus.BAD_REQUEST, "LOGIN4001", "올바르지 않은 이메일입니다."),
    LOGIN_ERROR_PW(HttpStatus.BAD_REQUEST, "LOGIN4002", "올바르지 않은 비밀번호입니다."),

    // 토큰 관련 에러
    TOKEN_UNVALID(HttpStatus.UNAUTHORIZED, "TOKEN4001", "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN4002", "토큰이 만료되었습니다"),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "TOKEN4003", "인증이 필요한 요청입니다"),
    TOKEN_UNKNOWN_ERROR(HttpStatus.UNAUTHORIZED, "TOKEN500", "토큰이 존재하지 않습니다."),
    TOKEN_WRONG_TYPE_ERROR(HttpStatus.BAD_REQUEST, "TOKEN4006", "변조된 토큰입니다."),
    TOKEN_UNSUPPORTED_ERROR(HttpStatus.BAD_REQUEST, "TOKEN4007", "변조된 토큰입니다."),

    // 유저 공통 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4041", "존재하지 않는 유저 정보입니다"),
    EMAIL_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "USER4001", "이미 존재하는 유저입니다."),


    // 기타 에러
    JSON_PARSING_ERROR(HttpStatus.BAD_REQUEST, "JSON4001", "JSON 파싱이 잘못되었습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                //            .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                //             .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }

}