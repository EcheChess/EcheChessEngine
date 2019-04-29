package ca.watier.echechess.engine.factories;

import ca.watier.echechess.engine.handlers.DefaultGameConstraintHandler;
import ca.watier.echechess.engine.interfaces.GameConstraint;

public final class GameConstraintFactory {

    private static final DefaultGameConstraintHandler DEFAULT_GAME_CONSTRAINT = new DefaultGameConstraintHandler();

    private GameConstraintFactory() {
    }

    public static GameConstraint getDefaultGameConstraint() {
        return DEFAULT_GAME_CONSTRAINT;
    }
}
