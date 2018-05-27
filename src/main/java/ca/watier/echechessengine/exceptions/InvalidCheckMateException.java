package ca.watier.echechessengine.exceptions;

public class InvalidCheckMateException extends ChessException {
    public InvalidCheckMateException(String message) {
        super(message);
    }
}
