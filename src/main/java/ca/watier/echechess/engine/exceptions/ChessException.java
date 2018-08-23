package ca.watier.echechess.engine.exceptions;

public class ChessException extends Exception {
    private static final long serialVersionUID = 6124708432084937263L;

    public ChessException(String message) {
        super(message);
    }

    public ChessException(ChessException chessException) {
        super(chessException);
    }
}
