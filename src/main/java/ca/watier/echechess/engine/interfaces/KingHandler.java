package ca.watier.echechess.engine.interfaces;

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.KingStatus;
import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.engine.abstracts.GameBoardData;

import java.io.Serializable;
import java.util.List;

public interface KingHandler extends Serializable {
    boolean isKingCheckAfterMove(CasePosition from, CasePosition to, GameBoardData gameBoardData);

    KingStatus getKingStatus(Side playerSide, GameBoardData gameBoardData);

    List<CasePosition> getPositionsThatCanMoveOrAttackPosition(CasePosition to, Side otherPlayerSide, GameBoardData gameBoardData);
}
