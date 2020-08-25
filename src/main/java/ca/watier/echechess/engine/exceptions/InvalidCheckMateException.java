package ca.watier.echechess.engine.exceptions;

public class InvalidCheckMateException extends ChessException {
    private static final long serialVersionUID = -7372475941054397751L;

    public InvalidCheckMateException(String message) {
        super(message);
    }
}
