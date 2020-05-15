package ca.watier.echechess.engine.exceptions;

public class InvalidCaptureException extends ChessException {
    private static final long serialVersionUID = -7740170796796497416L;

    public InvalidCaptureException(String message) {
        super(message);
    }
}
