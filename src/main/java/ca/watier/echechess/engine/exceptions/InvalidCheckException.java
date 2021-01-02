package ca.watier.echechess.engine.exceptions;

import java.io.Serial;

public class InvalidCheckException extends ChessException {

    @Serial
    private static final long serialVersionUID = 5207045928395729656L;

    public InvalidCheckException(String message) {
        super(message);
    }
}
