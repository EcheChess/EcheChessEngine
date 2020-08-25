package ca.watier.echechess.engine.exceptions;

public class InvalidMoveException extends ChessException {
    private static final long serialVersionUID = -6232175468222285183L;

    public InvalidMoveException(String message) {
        super(message);
    }
}
