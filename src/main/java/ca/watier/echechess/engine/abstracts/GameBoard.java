/*
 *    Copyright 2014 - 2018 Yannick Watier
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

package ca.watier.echechess.engine.abstracts;

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.Pieces;
import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.common.interfaces.BaseUtils;
import ca.watier.echechess.common.pojos.MoveHistory;
import ca.watier.echechess.common.utils.MathUtils;
import ca.watier.echechess.common.utils.ObjectUtils;
import ca.watier.echechess.common.utils.Pair;
import org.apache.commons.lang3.exception.CloneFailedException;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by yannick on 6/29/2017.
 */
public abstract class GameBoard implements Serializable {

    private static final long serialVersionUID = 807194077405321185L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GameBoard.class);
    private GameBoardData gameBoardData;

    public GameBoard() {
        super();
        gameBoardData = new GameBoardData();
    }

    protected final void addPawnPromotion(CasePosition from, CasePosition to, Side side) {
        if (ObjectUtils.hasNull(side, from, to) || Side.OBSERVER.equals(side)) {
            return;
        }

        gameBoardData.addPawnPromotionToMap(side, new Pair<>(from, to));
    }

    /**
     * Get the piece at the specific position
     *
     * @param position
     * @return
     */
    public final Pieces getPiece(CasePosition position) {
        return gameBoardData.getPieceFromPosition(position);
    }

    /**
     * Change a piece position, there's no check/constraint(s) on this method (Direct access to the Map)
     *
     * @param from
     * @param to
     * @param piece
     */
    protected final void movePieceTo(CasePosition from, CasePosition to, Pieces piece) {
        if (ObjectUtils.hasNull(from, to, piece)) {
            return;
        }

        gameBoardData.removePiece(from);
        gameBoardData.setPiecePositionWithoutMoveState(piece, to);
        gameBoardData.changeMovedStateOfPiece(piece, from, to);
        changePawnSpecialMove(piece, from, to);
        updatePlayerTurnValue(piece.getSide());
        gameBoardData.changePieceTurnNumber(from, to);
        gameBoardData.incrementTotalMove();
    }

    /**
     * Change the state of the pawn if the move is 2
     *
     * @param piece
     * @param from
     * @param to
     */
    protected void changePawnSpecialMove(Pieces piece, CasePosition from, CasePosition to) {
        if (Pieces.isPawn(piece)) {
            boolean isValid = BaseUtils.getSafeBoolean(gameBoardData.isPawnUsedSpecialMove(from)) || MathUtils.getDistanceBetweenPositions(from, to) == 2;

            gameBoardData.addPawnUsedSpecialMove(to, isValid);
            gameBoardData.removePawnUsedSpecialMove(from);
        }
    }

    /**
     * Update the turn number of the player (based on the color of the piece)
     *
     * @param side
     */
    protected void updatePlayerTurnValue(Side side) {
        switch (side) {
            case WHITE:
                gameBoardData.incrementWhiteTurnNumber();
                break;
            case BLACK:
                gameBoardData.incrementBlackTurnNumber();
                break;
            case OBSERVER:
            default:
                break;
        }
    }

    public final boolean upgradePiece(CasePosition to, Pieces pieces, Side playerSide) {

        if (ObjectUtils.hasNull(to, pieces, playerSide)) {
            return false;
        }

        Pair<CasePosition, CasePosition> pair = null;
        for (Pair<CasePosition, CasePosition> casePositionCasePositionPair : gameBoardData.getPawnPromotionBySide(playerSide)) {
            CasePosition toValue = casePositionCasePositionPair.getSecondValue();

            if (to.equals(toValue)) {
                pair = casePositionCasePositionPair;
                break;
            }
        }

        boolean isPresent = pair != null;
        if (isPresent) {
            gameBoardData.removePawnPromotion(pair, playerSide);
            CasePosition currentPawnFromPosition = pair.getFirstValue();

            gameBoardData.removePiece(currentPawnFromPosition); //remove the pawn
            gameBoardData.setPiecePositionWithoutMoveState(pieces, to); // add the wanted piece
            gameBoardData.setGamePaused(false);
        }

        return isPresent;
    }

    public final Map<CasePosition, Pieces> getPiecesLocation() {
        return gameBoardData.getPiecesLocation();
    }

    protected void addHistory(MoveHistory moveHistory) {
        gameBoardData.addHistory(moveHistory);
    }

    protected void setGamePaused(boolean paused) {
        gameBoardData.setGamePaused(paused);
    }

    protected void changeAllowedMoveSide() {
        gameBoardData.changeAllowedMoveSide();
    }

    protected void removePieceFromBoard(CasePosition position) {
        gameBoardData.removePieceFromBoard(position);
    }

    protected void setPositionPiecesMap(Map<CasePosition, Pieces> positions) {
        gameBoardData.setPositionPiecesMap(positions);
    }

    protected void setCurrentAllowedMoveSide(Side side) {
        gameBoardData.setCurrentAllowedMoveSide(side);
    }

    protected void setWhiteKingCastlingAvailable(boolean casting) {
        gameBoardData.setWhiteKingCastlingAvailable(casting);
    }

    protected void setBlackKingCastlingAvailable(boolean casting) {
        gameBoardData.setBlackKingCastlingAvailable(casting);
    }

    protected void setWhiteQueenCastlingAvailable(boolean casting) {
        gameBoardData.setWhiteQueenCastlingAvailable(casting);
    }

    protected void setBlackQueenCastlingAvailable(boolean casting) {
        gameBoardData.setBlackQueenCastlingAvailable(casting);
    }

    protected void addWhitePlayerPoint(byte point) {
        gameBoardData.addWhitePlayerPoint(point);
    }

    protected void addBlackPlayerPoint(byte point) {
        gameBoardData.addBlackPlayerPoint(point);
    }

    protected short getBlackPlayerPoint() {
        return gameBoardData.getBlackPlayerPoint();
    }

    protected short getWhitePlayerPoint() {
        return gameBoardData.getWhitePlayerPoint();
    }

    public GameBoardData getCloneOfCurrentDataState() {
        try {
            return org.apache.commons.lang3.ObjectUtils.cloneIfPossible(gameBoardData);
        } catch (CloneFailedException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return gameBoardData;
        }
    }

    public boolean isGameDraw() {
        return gameBoardData.isGameDraw();
    }

    public List<MoveHistory> getMoveHistory() {
        return gameBoardData.getMoveHistory();
    }
}
