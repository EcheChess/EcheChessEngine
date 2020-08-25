package ca.watier.echechess.engine.exceptions;

public class InvalidCheckException extends ChessException {
    private static final long serialVersionUID = 5207045928395729656L;

    public InvalidCheckException(String message) {
        super(message);
    }
}
