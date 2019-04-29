package ca.watier.utils;

import ca.watier.echechess.common.tests.GameTest;
import ca.watier.echechess.engine.handlers.DefaultGameConstraintHandler;

public abstract class EngineGameTest extends GameTest {
    protected static final DefaultGameConstraintHandler CONSTRAINT_SERVICE = new DefaultGameConstraintHandler();
}
