package cap.math.apiPayload.code;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorReasonDTO {
    private String code;
    private String message;
    private Boolean isSuccess;
    private HttpStatus httpStatus;
}