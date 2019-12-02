package exceptions;

import org.jetbrains.annotations.NonNls;

public class TransferException extends Exception {
    private static final long serialVersionUID = 7549081949504395944L;
    private final ErrorCode errorCode;

    public TransferException(@NonNls String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
