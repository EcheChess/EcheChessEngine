package ca.watier.echechess.engine.exceptions;

import ca.watier.echechess.common.enums.Side;

public class InvalidGameEndingException extends ChessException {
    private static final String THE_GAME_IS_SUPPOSED_TO_BE_WON_BY = "The game is supposed to be won by the %s player";
    private static final long serialVersionUID = 108266231963436838L;

    public InvalidGameEndingException(Side side) {
        super(String.format(THE_GAME_IS_SUPPOSED_TO_BE_WON_BY, side));
    }

    public InvalidGameEndingException(String message) {
        super(message);
    }
}
