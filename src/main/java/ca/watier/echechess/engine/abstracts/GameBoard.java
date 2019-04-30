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
import ca.watier.echechess.common.enums.Ranks;
import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.common.interfaces.BaseUtils;
import ca.watier.echechess.common.utils.MathUtils;
import ca.watier.echechess.common.utils.ObjectUtils;
import ca.watier.echechess.common.utils.Pair;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by yannick on 6/29/2017.
 */
public abstract class GameBoard extends GameBoardData {

    private static final long serialVersionUID = 807194077405321185L;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GameBoard.class);
    private static final byte MAX_ELEMENT_HISTORY = 64;

    private Deque<GameBoardData> historyStack;

    public GameBoard() {
        super();
        historyStack = new ArrayDeque<>(MAX_ELEMENT_HISTORY);
    }

    /**
     * Find the position for a column and a rank
     *
     * @param rank
     * @param column
     */
    protected CasePosition getPositionByRankAndColumn(Ranks rank, char column, Side side) {
        if (ObjectUtils.hasNull(rank, side)) {
            return null;
        }

        Stream<CasePosition> arrayStream = Arrays.stream(CasePosition.values());
        return arrayStream.filter(getCasePositionPredicateOnSameColumnAndRank(rank, column, side))
                .findFirst()
                .orElse(null);
    }

    private Predicate<CasePosition> getCasePositionPredicateOnSameColumnAndRank(Ranks rank, char column, Side side) {
        return casePosition -> rank.equals(Ranks.getRank(casePosition, side)) && casePosition.isOnSameColumn(column);
    }


    protected final void addPawnPromotion(CasePosition from, CasePosition to, Side side) {
        if (ObjectUtils.hasNull(side, from, to) || Side.OBSERVER.equals(side)) {
            return;
        }

        addPawnPromotionToMap(side, new Pair<>(from, to));
    }

    /**
     * Get the piece at the specific position
     *
     * @param position
     * @return
     */
    public final Pieces getPiece(CasePosition position) {
        return getPieceFromPosition(position);
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

        removePiece(from);
        setPiecePositionWithoutMoveState(piece, to);
        changeMovedStateOfPiece(piece, from, to);
        changePawnSpecialMove(piece, from, to);
        updatePlayerTurnValue(piece.getSide());
        changePieceTurnNumber(from, to);
        incrementTotalMove();
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
            boolean isValid = BaseUtils.getSafeBoolean(isPawnUsedSpecialMove(from)) || MathUtils.getDistanceBetweenPositions(from, to) == 2;

            addPawnUsedSpecialMove(to, isValid);
            removePawnUsedSpecialMove(from);
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
                incrementWhiteTurnNumber();
                break;
            case BLACK:
                incrementBlackTurnNumber();
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
        for (Pair<CasePosition, CasePosition> casePositionCasePositionPair : getPawnPromotionBySide(playerSide)) {
            CasePosition toValue = casePositionCasePositionPair.getSecondValue();

            if (to.equals(toValue)) {
                pair = casePositionCasePositionPair;
                break;
            }
        }

        boolean isPresent = pair != null;
        if (isPresent) {
            removePawnPromotion(pair, playerSide);
            CasePosition currentPawnFromPosition = pair.getFirstValue();

            removePiece(currentPawnFromPosition); //remove the pawn
            setPiecePositionWithoutMoveState(pieces, to); // add the wanted piece
            setGamePaused(false);
        }

        return isPresent;
    }

    public void cloneCurrentState() {
        try {
            historyStack.push(this.clone());
        } catch (CloneNotSupportedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void restoreLastState() {
        restore(historyStack.pop());
    }

    public void removeLastState() {
        historyStack.pop();
    }
}
