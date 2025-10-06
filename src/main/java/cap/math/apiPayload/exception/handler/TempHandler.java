package cap.math.apiPayload.exception.handler;

import cap.math.apiPayload.code.BaseErrorCode;
import cap.math.apiPayload.exception.GeneralException;

public class TempHandler extends GeneralException {
    public TempHandler(BaseErrorCode errorCode){
        super(errorCode);
    }
}