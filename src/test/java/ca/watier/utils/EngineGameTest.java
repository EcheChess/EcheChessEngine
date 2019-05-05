package ca.watier.utils;

import ca.watier.echechess.common.tests.GameTest;
import ca.watier.echechess.engine.delegates.PieceMoveConstraintDelegate;

public abstract class EngineGameTest extends GameTest {
    protected static final PieceMoveConstraintDelegate CONSTRAINT_SERVICE = new PieceMoveConstraintDelegate();
}
