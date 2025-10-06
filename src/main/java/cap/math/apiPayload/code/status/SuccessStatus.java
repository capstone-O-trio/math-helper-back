package cap.math.apiPayload.code.status;

import lombok.Getter;


@Getter
public enum SuccessStatus {

    _OK("2000", "OK"),
    _CREATED("2010", "Created"),
    _ACCEPTED("2020", "Accepted"),
    _NO_CONTENT("2040", "No Content"),
    _RESET_CONTENT("2050", "Reset Content"),
    _PARTIAL_CONTENT("2060", "Partial Content"),
    _MULTI_STATUS("2070", "Multi-Status"),
    _ALREADY_REPORTED("2080", "Already Reported"),
    _IM_USED("2260", "IM Used");

    private final String code;
    private final String message;

    SuccessStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }
}