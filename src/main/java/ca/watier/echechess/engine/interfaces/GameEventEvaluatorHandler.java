package ca.watier.echechess.engine.interfaces;

import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.engine.abstracts.GameBoardData;

import java.io.Serializable;

public interface GameEventEvaluatorHandler extends Serializable {
    boolean isPlayerTurn(Side playerSide, GameBoardData gameBoardData);
}
