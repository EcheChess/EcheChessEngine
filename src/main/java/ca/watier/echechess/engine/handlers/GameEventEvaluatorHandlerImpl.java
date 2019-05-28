package ca.watier.echechess.engine.handlers;

import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.engine.abstracts.GameBoardData;
import ca.watier.echechess.engine.interfaces.GameEventEvaluatorHandler;

public class GameEventEvaluatorHandlerImpl implements GameEventEvaluatorHandler {

    @Override
    public boolean isPlayerTurn(Side playerSide, GameBoardData gameBoardData) {
        if (playerSide == null) {
            return false;
        }

        return playerSide.equals(gameBoardData.getCurrentAllowedMoveSide());
    }
}
