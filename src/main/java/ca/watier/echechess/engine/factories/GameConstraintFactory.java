package ca.watier.echechess.engine.factories;

import ca.watier.echechess.engine.constraints.DefaultGameConstraint;
import ca.watier.echechess.engine.interfaces.GameConstraint;

public class GameConstraintFactory {

    private static final DefaultGameConstraint DEFAULT_GAME_CONSTRAINT = new DefaultGameConstraint();

    public static GameConstraint getDefaultGameConstraint() {
        return DEFAULT_GAME_CONSTRAINT;
    }
}
