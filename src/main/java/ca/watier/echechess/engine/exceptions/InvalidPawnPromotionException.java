package ca.watier.echechess.engine.exceptions;

import java.io.Serial;

public class InvalidPawnPromotionException extends ChessException {

    @Serial
    private static final long serialVersionUID = -5155034510137075525L;

    public InvalidPawnPromotionException(String message) {
        super(message);
    }
}
