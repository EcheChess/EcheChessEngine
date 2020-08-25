package ca.watier.echechess.engine.exceptions;

public class NoMoveTypeDefinedException extends ChessException {
    private static final long serialVersionUID = 8531778319959016206L;

    public NoMoveTypeDefinedException() {
        super("The Move Type is not defined for the current piece!");
    }
}
