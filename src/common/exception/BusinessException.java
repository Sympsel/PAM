package common.exception;


import lombok.Getter;
import lombok.Setter;

public class BusinessException extends Exception {
    @Getter
    @Setter
    private int errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = 400;
    }

    public BusinessException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
