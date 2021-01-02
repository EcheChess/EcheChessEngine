package ca.watier.echechess.engine.exceptions;

import java.io.Serial;

public class InvalidMoveException extends ChessException {

    @Serial
    private static final long serialVersionUID = -6232175468222285183L;

    public InvalidMoveException(String message) {
        super(message);
    }
}
