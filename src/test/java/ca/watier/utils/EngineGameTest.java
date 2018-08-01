package ca.watier.utils;

import ca.watier.echechess.common.tests.GameTest;
import ca.watier.echechess.engine.constraints.DefaultGameConstraint;

public abstract class EngineGameTest extends GameTest {
    protected static final DefaultGameConstraint CONSTRAINT_SERVICE = new DefaultGameConstraint();
}
