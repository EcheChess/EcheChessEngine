package ca.watier.echechess.engine.handlers;

import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.engine.abstracts.GameBoardData;
import ca.watier.echechess.engine.interfaces.GameEventEvaluatorHandler;

import java.io.Serial;

public class GameEventEvaluatorHandlerImpl implements GameEventEvaluatorHandler {

    @Serial
    private static final long serialVersionUID = -7662914404339428735L;

    @Override
    public boolean isPlayerTurn(Side playerSide, GameBoardData gameBoardData) {
        if (playerSide == null) {
            return false;
        }

        return playerSide.equals(gameBoardData.getCurrentAllowedMoveSide());
    }
}
