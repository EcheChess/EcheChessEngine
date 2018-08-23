package ca.watier.echechess.engine.exceptions;

public class InvalidCastlingException extends ChessException {
    private static final long serialVersionUID = -2060227552915724395L;

    public InvalidCastlingException(String message) {
        super(message);
    }
}
