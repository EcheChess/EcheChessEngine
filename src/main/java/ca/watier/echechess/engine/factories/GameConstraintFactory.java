package ca.watier.echechess.engine.factories;

import ca.watier.echechess.engine.handlers.DefaultGameConstraintHandlerImpl;
import ca.watier.echechess.engine.interfaces.GameConstraintHandler;

public final class GameConstraintFactory {

    private static final DefaultGameConstraintHandlerImpl DEFAULT_GAME_CONSTRAINT = new DefaultGameConstraintHandlerImpl();

    private GameConstraintFactory() {
    }

    public static GameConstraintHandler getDefaultGameConstraint() {
        return DEFAULT_GAME_CONSTRAINT;
    }
}
