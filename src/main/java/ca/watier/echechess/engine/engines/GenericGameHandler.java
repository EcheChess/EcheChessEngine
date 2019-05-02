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
import ca.watier.echechess.common.utils.*;
import ca.watier.echechess.engine.abstracts.GameBoard;
import ca.watier.echechess.engine.exceptions.MoveNotAllowedException;
import ca.watier.echechess.engine.factories.GameConstraintFactory;
import ca.watier.echechess.engine.handlers.KingHandlerImpl;
import ca.watier.echechess.engine.interfaces.GameConstraintHandler;
import ca.watier.echechess.engine.interfaces.KingHandler;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

import static ca.watier.echechess.common.enums.KingStatus.OK;
import static ca.watier.echechess.common.enums.Side.*;


/**
 * Created by yannick on 5/5/2017.
 */
public class GenericGameHandler extends GameBoard {
    private static final long serialVersionUID = 1139291295474732218L;
    private final GameConstraintHandler gameConstraintHandler;
    private final KingHandler kingHandler;
    private final Set<SpecialGameRules> specialGameRules = new HashSet<>();
    private Player playerWhite;
    private Player playerBlack;
    private String uuid;
    private boolean allowOtherToJoin = false;
    private boolean allowObservers = false;
    private List<Player> observerList = new ArrayList<>();
    private GameType gameType;

    public GenericGameHandler(GameConstraintHandler gameConstraintHandler) {
        super();
        this.gameConstraintHandler = gameConstraintHandler;
        this.kingHandler = new KingHandlerImpl(this, gameConstraintHandler);
    }

    public GenericGameHandler() {
        super();
        this.gameConstraintHandler = GameConstraintFactory.getDefaultGameConstraint();
        this.kingHandler = new KingHandlerImpl(this, gameConstraintHandler);
    }

    /**
     * Move a piece to a selected position
     *
     * @param from
     * @param to
     * @param playerSide
     * @return
     */
    public MoveType movePiece(CasePosition from, CasePosition to, Side playerSide) {
        if (ObjectUtils.hasNull(from, to, playerSide)) {
            return MoveType.MOVE_NOT_ALLOWED;
        }

        MoveHistory moveHistory = new MoveHistory(from, to, playerSide);
        Pieces piecesToBeforeAction = getPiece(to);

        MoveType moveType;
        try {
            moveType = movePiece(from, to, playerSide, moveHistory);
        } catch (MoveNotAllowedException e) {
            moveType = MoveType.MOVE_NOT_ALLOWED;
        }
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

    private MoveType movePiece(CasePosition from, CasePosition to, Side playerSide, MoveHistory moveHistory) throws MoveNotAllowedException {
        if (ObjectUtils.hasNull(from, to, playerSide, moveHistory)) {
            throw new MoveNotAllowedException();
        }

        Pieces piecesFrom = getPiece(from);

        if (piecesFrom == null || !isPlayerTurn(playerSide) || !piecesFrom.getSide().equals(playerSide)) {
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

        MoveType moveType = gameConstraintHandler.getMoveType(from, to, this);
        KingStatus evaluatedCurrentKingStatus = OK;
        boolean isEatingPiece = piecesTo != null;

        if (MoveType.NORMAL_MOVE.equals(moveType) || MoveType.PAWN_HOP.equals(moveType)) {
            if (!isPieceMovableTo(from, to, playerSide)) {
                throw new MoveNotAllowedException();
            }
            cloneCurrentState();
            movePieceTo(from, to, piecesFrom);
            evaluatedCurrentKingStatus = kingHandler.getKingStatus(playerSide);

            if (KingStatus.isCheckOrCheckMate(evaluatedCurrentKingStatus)) { //Cannot move, revert
                restoreLastState();
                throw new MoveNotAllowedException();
            } else {
                changeAllowedMoveSide();

                if (isEatingPiece) { //Count the point for the piece
                    updatePointsForSide(playerSide, piecesTo.getPoint());
                    moveType = MoveType.CAPTURE;
                }
                removeLastState();
            }
        } else if (MoveType.CASTLING.equals(moveType)) {
            handleCastlingWhenMove(from, to, playerSide, piecesFrom, piecesTo, isEatingPiece);
        } else if (MoveType.EN_PASSANT.equals(moveType)) {
            evaluatedCurrentKingStatus = handleEnPassantWhenMove(from, to, playerSide, otherPlayerSide, piecesFrom);
        }

        KingStatus evaluatedOtherKingStatusAfterMove = kingHandler.getKingStatus(otherPlayerSide);

        moveHistory.setCurrentKingStatus(evaluatedCurrentKingStatus);
        moveHistory.setOtherKingStatus(evaluatedOtherKingStatusAfterMove);

        setKingStatusBySide(evaluatedCurrentKingStatus, playerSide);
        setKingStatusBySide(evaluatedOtherKingStatusAfterMove, Side.getOtherPlayerSide(playerSide));

        return moveType;
    }

    private KingStatus handleEnPassantWhenMove(CasePosition from, CasePosition to, Side playerSide, Side otherPlayerSide, Pieces piecesFrom) throws MoveNotAllowedException {
        cloneCurrentState();

        movePieceTo(from, to, piecesFrom);
        CasePosition enemyPawnPosition = MathUtils.getNearestPositionFromDirection(to, otherPlayerSide.equals(BLACK) ? Direction.SOUTH : Direction.NORTH);
        Pieces enemyPawnToEat = getPiece(enemyPawnPosition);
        removePieceFromBoard(enemyPawnPosition);

        KingStatus kingStatus = kingHandler.getKingStatus(playerSide);
        if (KingStatus.isCheckOrCheckMate(kingStatus)) {
            restoreLastState();
            throw new MoveNotAllowedException();
        }

        updatePointsForSide(playerSide, enemyPawnToEat.getPoint());
        changeAllowedMoveSide();
        removeLastState();
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

    private boolean isPlayerTurn(Side sideFrom) {
        if (sideFrom == null) {
            return false;
        }

        return isGameHaveRule(SpecialGameRules.NO_PLAYER_TURN) || getCurrentAllowedMoveSide().equals(sideFrom);
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

    public boolean isGameHaveRule(SpecialGameRules rule) {
        return specialGameRules.contains(rule);
    }

    public GameScoreResponse getGameScore() {
        return new GameScoreResponse(getWhitePlayerPoint(), getBlackPlayerPoint());
    }

    public boolean isKingCheckAtPosition(CasePosition currentPosition, Side playerSide) {
        if (ObjectUtils.hasNull(currentPosition, playerSide) || isGameHaveRule(SpecialGameRules.NO_CHECK_OR_CHECKMATE)) {
            return false;
        }

        return !getPiecesThatCanHitPosition(getOtherPlayerSide(playerSide), currentPosition).isEmpty();
    }

    /**
     * Gets the pieces / CasePosition based on a side
     *
     * @param side
     * @return
     */
    public final Map<CasePosition, Pieces> getPiecesLocation(Side side) {
        Map<CasePosition, Pieces> values = new EnumMap<>(CasePosition.class);

        if (side == null) {
            return values;
        }

        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : getPiecesLocation().entrySet()) {
            CasePosition key = casePositionPiecesEntry.getKey();
            Pieces value = casePositionPiecesEntry.getValue();

            if (side.equals(value.getSide())) {
                values.put(key, value);
            }
        }

        return values;
    }


    /**
     * Check if the piece can be moved to the selected position
     *
     * @param from
     * @param to
     * @param playerSide
     * @return
     */
    public final boolean isPieceMovableTo(CasePosition from, CasePosition to, Side playerSide) {
        if (ObjectUtils.hasNull(from, to, playerSide)) {
            return false;
        }

        return gameConstraintHandler.isPieceMovableTo(from, to, playerSide, this, MoveMode.NORMAL_OR_ATTACK_MOVE);
    }

    /**
     * Return a List containing all the moves for the selected piece
     *
     * @param from
     * @param playerSide
     * @return
     */
    public List<CasePosition> getAllAvailableMoves(CasePosition from, Side playerSide) {
        List<CasePosition> positions = new ArrayList<>();

        if (ObjectUtils.hasNull(from, playerSide)) {
            return positions;
        }

        Pieces pieces = getPiece(from);

        if (pieces == null || !Pieces.isSameSide(pieces, playerSide)) {
            return positions;
        }

        CasePosition[] casePositionWithoutCurrent = ArrayUtils.removeElement(CasePosition.values(), from);

        for (CasePosition position : casePositionWithoutCurrent) { //FIXME: ADD SPECIAL MOVES
            if (isPieceMovableTo(from, position, playerSide)) {
                positions.add(position);
            }
        }

        return positions;
    }

    /**
     * Return a list of @{@link Pieces} that can moves to the selected position
     *
     * @param to
     * @param sideToKeep
     * @return
     */
    public List<Pair<CasePosition, Pieces>> getAllPiecesThatCanMoveTo(CasePosition to, Side sideToKeep) {
        List<Pair<CasePosition, Pieces>> values = new ArrayList<>();

        if (ObjectUtils.hasNull(to, sideToKeep)) {
            return values;
        }

        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : getPiecesLocation().entrySet()) {
            CasePosition from = casePositionPiecesEntry.getKey();
            Pieces piecesFrom = casePositionPiecesEntry.getValue();

            if (!sideToKeep.equals(piecesFrom.getSide())) {
                continue;
            }

            MoveType moveType = gameConstraintHandler.getMoveType(from, to, this);
            boolean isEnPassant = MoveType.EN_PASSANT.equals(moveType);

            if (!kingHandler.isKingCheckAfterMove(from, to, sideToKeep) && isPieceMovableTo(from, to, sideToKeep) || isEnPassant) {
                values.add(new Pair<>(from, piecesFrom));
            }
        }

        return values;
    }

    /**
     * Gets the pieces that can hit the target, the {@link CasePosition} inside the {@link Pair} is the starting position of the attacking {@link Pieces}
     *
     * @param positions
     * @param sideToKeep
     * @return
     */
    public MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> getPiecesThatCanHitPosition(Side sideToKeep, CasePosition... positions) {
        MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> values = new MultiArrayMap<>();

        if (ArrayUtils.isEmpty(positions)) {
            return values;
        }

        for (CasePosition position : positions) {
            for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : getPiecesLocation().entrySet()) {
                CasePosition key = casePositionPiecesEntry.getKey();
                Pieces value = casePositionPiecesEntry.getValue();

                Side pieceSide = value.getSide();
                if (!pieceSide.equals(sideToKeep)) {
                    continue;
                }

                if (gameConstraintHandler.isPieceMovableTo(key, position, pieceSide, this, MoveMode.IS_KING_CHECK_MODE)) {
                    values.put(position, new Pair<>(key, value));
                }
            }
        }

        return values;
    }

    public boolean isKing(KingStatus kingStatus, Side side) {
        if (Objects.isNull(kingStatus)) {
            return false;
        }

        return kingStatus.equals(kingHandler.getKingStatus(side));
    }

    public boolean isCheckMate(Side side) {
        return KingStatus.CHECKMATE.equals(kingHandler.getKingStatus(side));
    }

    public boolean isCheck(Side side) {
        return KingStatus.CHECK.equals(kingHandler.getKingStatus(side));
    }

    public final boolean setPlayerToSide(Player player, Side side) {
        if (ObjectUtils.hasNull(player, side)) {
            return false;
        }

        boolean value;

        switch (side) {
            case BLACK: {
                removePlayerFromWhite(player);
                value = changePlayerToBlack(player);
                observerList.remove(player);
                break;
            }
            case WHITE: {
                removePlayerFromBlack(player);
                value = changePlayerToWhite(player);
                observerList.remove(player);
                break;
            }
            default: {
                removePlayerFromWhite(player);
                removePlayerFromBlack(player);
                observerList.add(player);
                value = true;
                break;
            }
        }

        return value;
    }

    private void removePlayerFromWhite(Player player) {
        if (player != null && player.equals(playerWhite)) {
            playerWhite = null;
        }
    }

    private boolean changePlayerToBlack(Player player) {
        if (playerBlack == null) {
            playerBlack = player;
            return true;
        }

        return false;
    }

    private void removePlayerFromBlack(Player player) {
        if (playerBlack == player) {
            playerBlack = null;
        }
    }

    private boolean changePlayerToWhite(Player player) {
        if (playerWhite == null) {
            playerWhite = player;
            return true;
        }

        return false;
    }

    /**
     * Get the side of the player, null if not available
     *
     * @param player
     * @return
     */
    public final Side getPlayerSide(Player player) {
        Side side = null;

        if (player == null) {
            return null;
        }

        if (player.equals(playerWhite)) {
            side = WHITE;
        } else if (player.equals(playerBlack)) {
            side = BLACK;
        } else if (observerList.contains(player)) {
            side = Side.OBSERVER;
        }

        return side;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public final boolean hasPlayer(Player player) {
        if (player == null) {
            return false;
        }

        return observerList.contains(player) || player.equals(playerBlack) || player.equals(playerWhite);
    }

    public Player getPlayerWhite() {
        return playerWhite;
    }

    public Player getPlayerBlack() {
        return playerBlack;
    }

    public boolean isAllowOtherToJoin() {
        return allowOtherToJoin;
    }

    public void setAllowOtherToJoin(boolean allowOtherToJoin) {
        this.allowOtherToJoin = allowOtherToJoin;
    }

    public boolean isAllowObservers() {
        return allowObservers;
    }

    public void setAllowObservers(boolean allowObservers) {
        this.allowObservers = allowObservers;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void addSpecialRule(SpecialGameRules... rules) {
        if (ArrayUtils.isEmpty(rules)) {
            return;
        }

        specialGameRules.addAll(Arrays.asList(rules));
    }

    public void removeSpecialRule(SpecialGameRules... rules) {
        if (ArrayUtils.isEmpty(rules)) {
            return;
        }

        specialGameRules.removeAll(Arrays.asList(rules));
    }

    public List<Player> getObserverList() {
        return Collections.unmodifiableList(observerList);
    }

    public Set<SpecialGameRules> getSpecialGameRules() {
        return Collections.unmodifiableSet(specialGameRules);
    }

    public boolean isGameDone() {
        return KingStatus.CHECKMATE.equals(getEvaluatedKingStatusBySide(BLACK)) ||
                KingStatus.CHECKMATE.equals(getEvaluatedKingStatusBySide(WHITE)) ||
                isGameDraw();
    }

    public List<CasePosition> getPositionKingCanMove(Side white) {
        return kingHandler.getPositionKingCanMove(white);
    }
}
