package ca.watier.echechess.engine.exceptions;

import java.io.Serial;

public class ChessException extends Exception {

    @Serial
    private static final long serialVersionUID = 6124708432084937263L;

    public ChessException(String message) {
        super(message);
    }

    public ChessException(ChessException chessException) {
        super(chessException);
    }
}
