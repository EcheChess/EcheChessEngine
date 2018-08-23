package ca.watier.echechess.engine.exceptions;

public class InvalidPawnPromotionException extends ChessException {
    private static final long serialVersionUID = -5155034510137075525L;

    public InvalidPawnPromotionException(String message) {
        super(message);
    }
}
