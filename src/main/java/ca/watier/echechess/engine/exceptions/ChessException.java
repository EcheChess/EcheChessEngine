package ca.watier.echechess.engine.exceptions;

public class ChessException extends Exception {
    public ChessException(String message) {
        super(message);
    }

    public ChessException(ChessException chessException) {
        super(chessException);
    }
}
