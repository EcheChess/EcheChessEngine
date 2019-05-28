package ca.watier.echechess.engine.interfaces;

import ca.watier.echechess.common.enums.*;
import ca.watier.echechess.common.responses.GameScoreResponse;
import ca.watier.echechess.common.sessions.Player;
import ca.watier.echechess.common.utils.Pair;
import ca.watier.echechess.engine.delegates.PieceMoveConstraintDelegate;

import java.util.List;
import java.util.Map;

public interface GameHandler {
    MoveType movePiece(CasePosition from, CasePosition to, Side playerSide);

    GameScoreResponse getGameScore();

    Map<CasePosition, Pieces> getPiecesLocation(Side side);

    List<CasePosition> getAllAvailableMoves(CasePosition from, Side playerSide);

    List<Pair<CasePosition, Pieces>> getAllPiecesThatCanMoveTo(CasePosition to, Side sideToKeep);

    boolean isKing(KingStatus kingStatus, Side side);

    boolean isCheckMate(Side side);

    boolean isCheck(Side side);

    boolean setPlayerToSide(Player player, Side side);

    Side getPlayerSide(Player player);

    boolean hasPlayer(Player player);

    Player getPlayerWhite();

    Player getPlayerBlack();

    String getUuid();

    void setUuid(String uuid);

    boolean isGameDone();

    PlayerHandler getPlayerHandler();

    PieceMoveConstraintDelegate getMoveConstraintDelegate();

}
