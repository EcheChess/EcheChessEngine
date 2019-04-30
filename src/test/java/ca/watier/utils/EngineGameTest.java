package ca.watier.utils;

import ca.watier.echechess.common.tests.GameTest;
import ca.watier.echechess.engine.handlers.DefaultGameConstraintHandlerImpl;

public abstract class EngineGameTest extends GameTest {
    protected static final DefaultGameConstraintHandlerImpl CONSTRAINT_SERVICE = new DefaultGameConstraintHandlerImpl();
}
