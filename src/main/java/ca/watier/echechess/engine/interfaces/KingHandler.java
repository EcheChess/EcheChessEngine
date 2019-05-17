package ca.watier.echechess.engine.interfaces;

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.KingStatus;
import ca.watier.echechess.common.enums.Pieces;
import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.common.utils.MultiArrayMap;
import ca.watier.echechess.common.utils.Pair;
import ca.watier.echechess.engine.engines.GenericGameHandler;

import java.util.List;

public interface KingHandler extends GenericHandler {
    KingStatus getKingStatusWhenPiecesCanHitKing(Side playerSide, CasePosition kingPosition, MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> piecesThatCanHitOriginalPosition);

    boolean isStalemate(Side playerSide, Pieces kingPiece, CasePosition kingPosition);

    boolean isKingCheckAfterMove(CasePosition from, CasePosition to);

    List<CasePosition> getPositionKingCanMove(Side playerSide);

    KingStatus getKingStatus(Side playerSide);

    boolean isKingCheckAtPosition(CasePosition currentPosition, Side playerSide, GenericGameHandler genericGameHandler);
}
