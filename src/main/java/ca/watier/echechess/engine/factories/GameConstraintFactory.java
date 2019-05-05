package ca.watier.echechess.engine.factories;

import ca.watier.echechess.engine.delegates.PieceMoveConstraintDelegate;

public final class GameConstraintFactory {

    private static final PieceMoveConstraintDelegate DEFAULT_GAME_CONSTRAINT = new PieceMoveConstraintDelegate();

    private GameConstraintFactory() {
    }

    public static PieceMoveConstraintDelegate getDefaultGameMoveDelegate() {
        return DEFAULT_GAME_CONSTRAINT;
    }
}
