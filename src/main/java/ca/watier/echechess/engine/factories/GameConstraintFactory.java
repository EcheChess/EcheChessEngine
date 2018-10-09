package ca.watier.echechess.engine.factories;

import ca.watier.echechess.engine.constraints.DefaultGameConstraint;
import ca.watier.echechess.engine.interfaces.GameConstraint;

public final class GameConstraintFactory {

    private static final DefaultGameConstraint DEFAULT_GAME_CONSTRAINT = new DefaultGameConstraint();

    private GameConstraintFactory() {
    }

    public static GameConstraint getDefaultGameConstraint() {
        return DEFAULT_GAME_CONSTRAINT;
    }
}
