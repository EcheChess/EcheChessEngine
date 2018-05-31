package ca.watier.echechess.engine.exceptions;

public class InvalidCheckException extends ChessException {
    public InvalidCheckException(String message) {
        super(message);
    }
}
