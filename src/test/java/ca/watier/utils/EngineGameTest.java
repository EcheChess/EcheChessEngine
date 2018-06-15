package ca.watier.utils;

import ca.watier.echechess.common.tests.GameTest;
import ca.watier.echechess.engine.game.GameConstraints;

public abstract class EngineGameTest extends GameTest {
    protected static final GameConstraints CONSTRAINT_SERVICE = new GameConstraints();
}
