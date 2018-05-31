package ca.watier.echechess.engine.exceptions;

public class InvalidCheckMateException extends ChessException {
    public InvalidCheckMateException(String message) {
        super(message);
    }
}
