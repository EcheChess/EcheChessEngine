/*
 *    Copyright 2014 - 2017 Yannick Watier
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ca.watier.echechess.engine.engines;

import ca.watier.echechess.common.enums.*;
import ca.watier.echechess.common.pojos.MoveHistory;
import ca.watier.echechess.common.responses.GameScoreResponse;
import ca.watier.echechess.common.sessions.Player;
import ca.watier.echechess.common.utils.CastlingPositionHelper;
import ca.watier.echechess.common.utils.MathUtils;
import ca.watier.echechess.common.utils.ObjectUtils;
import ca.watier.echechess.engine.abstracts.GameBoard;
import ca.watier.echechess.engine.abstracts.GameBoardData;
import ca.watier.echechess.engine.delegates.PieceMoveConstraintDelegate;
import ca.watier.echechess.engine.exceptions.MoveNotAllowedException;
import ca.watier.echechess.engine.handlers.GameEventEvaluatorHandlerImpl;
import ca.watier.echechess.engine.handlers.PlayerHandlerImpl;
import ca.watier.echechess.engine.interfaces.GameEventEvaluatorHandler;
import ca.watier.echechess.engine.interfaces.GameHandler;
import ca.watier.echechess.engine.interfaces.PlayerHandler;
import ca.watier.echechess.engine.models.enums.MoveStatus;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ca.watier.echechess.common.enums.KingStatus.OK;
import static ca.watier.echechess.common.enums.Side.*;


/**
 * Created by yannick on 5/5/2017.
 */
public class GenericGameHandler extends GameBoard implements GameHandler {
    private static final long serialVersionUID = 1139291295474732218L;
    public static final KingStatus CHECKMATE = KingStatus.CHECKMATE;
    public static final KingStatus CHECK = KingStatus.CHECK;
    public static final KingStatus STALEMATE = KingStatus.STALEMATE;

    private final GameEventEvaluatorHandler gameEventEvaluatorHandler;
    private final PieceMoveConstraintDelegate pieceDelegate;
    private final PlayerHandler playerHandler;
    private String uuid;

    public GenericGameHandler(PieceMoveConstraintDelegate pieceDelegate, PlayerHandler playerHandler, GameEventEvaluatorHandler gameEventEvaluatorHandler) {
        super();
        this.gameEventEvaluatorHandler = gameEventEvaluatorHandler;
        this.pieceDelegate = pieceDelegate;
        this.playerHandler = playerHandler;
    }

    public static GenericGameHandler newStandardHandlerFromConstraintDelegate(PieceMoveConstraintDelegate pieceMoveConstraintDelegate) {
        PlayerHandler playerHandler = new PlayerHandlerImpl();
        GameEventEvaluatorHandler gameEventEvaluatorHandler = new GameEventEvaluatorHandlerImpl();

        return new GenericGameHandler(pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);
    }

    /**
     * Move a piece to a selected position
     *
     * @param from
     * @param to
     * @param playerSide
     * @return
     */
    @Override
    public MoveType movePiece(CasePosition from, CasePosition to, Side playerSide) {
        if (ObjectUtils.hasNull(from, to, playerSide)) {
            return MoveType.MOVE_NOT_ALLOWED;
        }

        MoveHistory moveHistory = new MoveHistory(from, to, playerSide);
        Pieces piecesToBeforeAction = getPiece(to);

        MoveType moveType = getMoveTypeFromMove(from, to, playerSide, moveHistory);
        moveHistory.setMoveType(moveType);
        addHistory(moveHistory);

        if (MoveType.PAWN_PROMOTION.equals(moveType)) {//Check if the promotion kill a piece in the process
            if (piecesToBeforeAction != null) {
                MoveHistory moveHistoryCapture = new MoveHistory(from, to, playerSide);
                moveHistoryCapture.setMoveType(MoveType.CAPTURE);
                addHistory(moveHistoryCapture);
            }
        }
        return moveType;
    }

    private MoveType getMoveTypeFromMove(CasePosition from, CasePosition to, Side playerSide, MoveHistory moveHistory) {
        try {
            return movePiece(from, to, playerSide, moveHistory);
        } catch (MoveNotAllowedException e) {
            return MoveType.MOVE_NOT_ALLOWED;
        }
    }

    private MoveType movePiece(CasePosition from, CasePosition to, Side playerSide, MoveHistory moveHistory) throws MoveNotAllowedException {
        if (ObjectUtils.hasNull(from, to, playerSide, moveHistory)) {
            throw new MoveNotAllowedException();
        }

        Pieces piecesFrom = getPiece(from);

        if (piecesFrom == null || !gameEventEvaluatorHandler.isPlayerTurn(playerSide, getCloneOfCurrentDataState()) || !Pieces.isSameSide(piecesFrom, playerSide)) {
            throw new MoveNotAllowedException();
        }

        Pieces piecesTo = getPiece(to);
        Side otherPlayerSide = getOtherPlayerSide(playerSide);

        if (Pieces.isPawn(piecesFrom) && Ranks.EIGHT.equals(Ranks.getRank(to, playerSide))) {
            addPawnPromotion(from, to, playerSide);
            setGamePaused(true);
            changeAllowedMoveSide();

            return MoveType.PAWN_PROMOTION;
        }

        MoveType moveType = pieceDelegate.getMoveType(from, to, getCloneOfCurrentDataState());

        KingStatus evaluatedCurrentKingStatus = OK;
        boolean isEatingPiece = piecesTo != null;

        if (MoveType.NORMAL_MOVE.equals(moveType) || MoveType.PAWN_HOP.equals(moveType)) {
            MoveStatus moveStatus = getMoveStatus(from, to, getCloneOfCurrentDataState());

            switch (moveStatus) {
                case INVALID_MOVE:
                case INVALID_ATTACK:
                case CAN_PROTECT_FRIENDLY:
                case KING_ATTACK_KING:
                case ENEMY_KING_PARTIAL_CHECK:
                    throw new MoveNotAllowedException();
            }

            movePieceTo(from, to, piecesFrom);
            evaluatedCurrentKingStatus = pieceDelegate.getKingStatus(playerSide, getCloneOfCurrentDataState());

            if (KingStatus.isCheckOrCheckMate(evaluatedCurrentKingStatus)) { //Cannot move, revert
                throw new MoveNotAllowedException();
            } else {
                changeAllowedMoveSide();

                if (isEatingPiece) { //Count the point for the piece
                    updatePointsForSide(playerSide, piecesTo.getPoint());
                    moveType = MoveType.CAPTURE;
                }
            }
        } else if (MoveType.CASTLING.equals(moveType)) {
            handleCastlingWhenMove(from, to, playerSide, piecesFrom, piecesTo, isEatingPiece);
        } else if (MoveType.EN_PASSANT.equals(moveType)) {
            evaluatedCurrentKingStatus = handleEnPassantWhenMove(from, to, playerSide, otherPlayerSide, piecesFrom);
        }

        KingStatus evaluatedOtherKingStatusAfterMove = pieceDelegate.getKingStatus(otherPlayerSide, getCloneOfCurrentDataState());

        moveHistory.setCurrentKingStatus(evaluatedCurrentKingStatus);
        moveHistory.setOtherKingStatus(evaluatedOtherKingStatusAfterMove);

        return moveType;
    }


    private KingStatus handleEnPassantWhenMove(CasePosition from, CasePosition to, Side playerSide, Side otherPlayerSide, Pieces piecesFrom) throws MoveNotAllowedException {
        movePieceTo(from, to, piecesFrom);
        CasePosition enemyPawnPosition = MathUtils.getNearestPositionFromDirection(to, otherPlayerSide.equals(BLACK) ? Direction.SOUTH : Direction.NORTH);
        Pieces enemyPawnToEat = getPiece(enemyPawnPosition);
        removePieceFromBoard(enemyPawnPosition);

        KingStatus kingStatus = pieceDelegate.getKingStatus(playerSide, getCloneOfCurrentDataState());
        if (KingStatus.isCheckOrCheckMate(kingStatus)) {
            throw new MoveNotAllowedException();
        }

        updatePointsForSide(playerSide, enemyPawnToEat.getPoint());
        changeAllowedMoveSide();
        return kingStatus;
    }


    /**
     * If queen side, move rook to D1 / D8 and king to C1 / C8
     * Otherwise, move rook to F1 / F8 and king to G1 / G8
     *
     * @param from
     * @param to
     * @param playerSide
     * @param piecesFrom
     * @param piecesTo
     * @param isEatingPiece
     */
    private void handleCastlingWhenMove(CasePosition from, CasePosition to, Side playerSide, Pieces piecesFrom, Pieces piecesTo, boolean isEatingPiece) {
        CastlingPositionHelper castlingPositionHelper = new CastlingPositionHelper(from, to, playerSide).invoke();
        CasePosition kingPosition = castlingPositionHelper.getKingPosition();
        CasePosition rookPosition = castlingPositionHelper.getRookPosition();

        if (castlingPositionHelper.isQueenSide()) {
            switch (playerSide) {
                case BLACK:
                    setBlackQueenCastlingAvailable(false);
                    break;
                case WHITE:
                    setWhiteQueenCastlingAvailable(false);
                    break;
            }
        } else {
            switch (playerSide) {
                case BLACK:
                    setBlackKingCastlingAvailable(false);
                    break;
                case WHITE:
                    setWhiteKingCastlingAvailable(false);
                    break;
            }
        }

        movePieceTo(from, kingPosition, piecesFrom);

        if (isEatingPiece) {
            movePieceTo(to, rookPosition, piecesTo);
        }
        changeAllowedMoveSide();
    }

    private void updatePointsForSide(Side side, byte point) {
        if (side == null) {
            return;
        }

        switch (side) {
            case BLACK:
                addBlackPlayerPoint(point);
                break;
            case WHITE:
                addWhitePlayerPoint(point);
                break;
            default:
                break;
        }
    }

    @Override
    public GameScoreResponse getGameScore() {
        return new GameScoreResponse(getWhitePlayerPoint(), getBlackPlayerPoint());
    }

    /**
     * Gets the pieces / CasePosition based on a side
     *
     * @param side
     * @return
     */
    @Override
    public final Map<CasePosition, Pieces> getPiecesLocation(Side side) {
        Map<CasePosition, Pieces> values = new EnumMap<>(CasePosition.class);

        if (side == null) {
            return values;
        }

        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : getPiecesLocation().entrySet()) {
            CasePosition key = casePositionPiecesEntry.getKey();
            Pieces value = casePositionPiecesEntry.getValue();

            if (Pieces.isSameSide(value, side)) {
                values.put(key, value);
            }
        }

        return values;
    }

    @Override
    public List<CasePosition> getAllAvailableMoves(CasePosition from, Side playerSide) {
        return pieceDelegate.getAllAvailableMoves(from, playerSide, getCloneOfCurrentDataState());
    }

    @Override
    public MoveStatus getMoveStatus(CasePosition from, CasePosition to, GameBoardData cloneOfCurrentDataState) {
        return pieceDelegate.getMoveStatus(from, to, cloneOfCurrentDataState);
    }

    @Override
    public boolean isKing(KingStatus kingStatus, Side side) {
        if (Objects.isNull(kingStatus)) {
            return false;
        }

        return kingStatus.equals(pieceDelegate.getKingStatus(side, getCloneOfCurrentDataState()));
    }

    @Override
    public boolean isCheckMate(Side side) {
        return CHECKMATE.equals(pieceDelegate.getKingStatus(side, getCloneOfCurrentDataState()));
    }

    @Override
    public boolean isCheck(Side side) {
        return CHECK.equals(pieceDelegate.getKingStatus(side, getCloneOfCurrentDataState()));
    }

    @Override
    public final boolean setPlayerToSide(Player player, Side side) {
        return playerHandler.setPlayerToSide(player, side);
    }

    @Override
    public final Side getPlayerSide(Player player) {
        return playerHandler.getPlayerSide(player);
    }

    @Override
    public final boolean hasPlayer(Player player) {
        return playerHandler.hasPlayer(player);
    }

    @Override
    public Player getPlayerWhite() {
        return playerHandler.getPlayerWhite();
    }

    @Override
    public Player getPlayerBlack() {
        return playerHandler.getPlayerBlack();
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean isGameEnded() {
        GameBoardData cloneOfCurrentDataState = getCloneOfCurrentDataState();

        return CHECKMATE.equals(pieceDelegate.getKingStatus(BLACK, cloneOfCurrentDataState)) ||
                CHECKMATE.equals(pieceDelegate.getKingStatus(WHITE, cloneOfCurrentDataState)) ||
                isGameDraw();
    }

    @Override
    public boolean isGameStalemate() {
        return isKing(STALEMATE, WHITE) && isKing(STALEMATE, BLACK);
    }

    @Override
    public PlayerHandler getPlayerHandler() {
        return playerHandler;
    }

    @Override
    public PieceMoveConstraintDelegate getMoveConstraintDelegate() {
        return pieceDelegate;
    }
}