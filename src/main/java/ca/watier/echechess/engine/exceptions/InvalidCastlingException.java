package ca.watier.echechess.engine.exceptions;

import java.io.Serial;

public class InvalidCastlingException extends ChessException {

    @Serial
    private static final long serialVersionUID = -2060227552915724395L;

    public InvalidCastlingException(String message) {
        super(message);
    }
}
