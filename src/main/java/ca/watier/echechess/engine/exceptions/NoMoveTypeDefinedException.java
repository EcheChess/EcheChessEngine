package ca.watier.echechess.engine.exceptions;

import java.io.Serial;

public class NoMoveTypeDefinedException extends ChessException {

    @Serial
    private static final long serialVersionUID = 8531778319959016206L;

    public NoMoveTypeDefinedException() {
        super("The Move Type is not defined for the current piece!");
    }
}
