package ca.watier.echechess.engine.interfaces;

import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.engine.abstracts.GameBoardData;

public interface GameEventEvaluatorHandler {
    boolean isPlayerTurn(Side playerSide, GameBoardData gameBoardData);
}
