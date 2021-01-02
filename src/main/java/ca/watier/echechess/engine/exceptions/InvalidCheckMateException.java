package ca.watier.echechess.engine.exceptions;

import java.io.Serial;

public class InvalidCheckMateException extends ChessException {

    @Serial
    private static final long serialVersionUID = -7372475941054397751L;

    public InvalidCheckMateException(String message) {
        super(message);
    }
}
