package ca.watier.echechess.engine.exceptions;

import java.io.Serial;

public class InvalidCaptureException extends ChessException {

    @Serial
    private static final long serialVersionUID = -7740170796796497416L;

    public InvalidCaptureException(String message) {
        super(message);
    }
}
