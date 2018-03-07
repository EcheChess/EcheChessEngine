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

package ca.watier.echechessengine.abstracts;

import ca.watier.echesscommon.enums.CasePosition;
import ca.watier.echesscommon.enums.Pieces;
import ca.watier.echesscommon.enums.Ranks;
import ca.watier.echesscommon.enums.Side;
import ca.watier.echesscommon.interfaces.BaseUtils;
import ca.watier.echesscommon.utils.Assert;
import ca.watier.echesscommon.utils.MathUtils;
import ca.watier.echesscommon.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Created by yannick on 6/29/2017.
 */
public abstract class GameBoard extends GameBoardData {

    /**
     * Find the position for a column and a rank
     *
     * @param rank
     * @param column
     */
    public CasePosition getPositionByRankAndColumn(@NotNull Ranks rank, char column, @NotNull Side side) {
        return Arrays.stream(CasePosition.values()).filter(casePosition -> rank.equals(Ranks.getRank(casePosition, side))
                && casePosition.isOnSameColumn(column)).findFirst().orElse(null);
    }


    public final void addPawnPromotion(@NotNull CasePosition from, @NotNull CasePosition to, @NotNull Side side) {

        if (Side.OBSERVER.equals(side)) {
            return;
        }

        addPawnPromotionToMap(side, new Pair<>(from, to));
    }

    /**
     * Set the specified case at the position, without changing the move state (useful when evaluating)
     *
     * @param piece
     * @param to
     */
    public final void setPiecePositionWithoutMoveState(@NotNull Pieces piece, @NotNull CasePosition to) {
        addPieceToBoard(to, piece);
    }

    /**
     * Get the piece at the specific position
     *
     * @param position
     * @return
     */
    public final Pieces getPiece(@NotNull CasePosition position) {
        return getPieceFromPosition(position);
    }

    /**
     * Change a piece position, there's no check/constraint(s) on this method (Direct access to the Map)
     *
     * @param from
     * @param to
     * @param piece
     */
    protected final void movePieceTo(@NotNull CasePosition from, @NotNull CasePosition to, @NotNull Pieces piece) {
        removePiece(from);
        addPieceToBoard(to, piece);
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
    private void changePawnSpecialMove(@NotNull Pieces piece, @NotNull CasePosition from, @NotNull CasePosition to) {
        Assert.assertNotNull(from, to, piece);

        if (Pieces.isPawn(piece)) {
            boolean isValid = BaseUtils.getSafeBoolean(isPanwUsedSpecialMove(from)) || MathUtils.getDistanceBetweenPositions(from, to) == 2;

            addPawnUsedSpecialMove(to, isValid);
            removePawnUsedSpecialMove(from);
        }
    }

    /**
     * Update the turn number of the player (based on the color of the piece)
     *
     * @param side
     */
    private void updatePlayerTurnValue(@NotNull Side side) {
        Assert.assertNotNull(side);

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

    public final boolean upgradePiece(@NotNull CasePosition to, @NotNull Pieces pieces, @NotNull Side playerSide) {
        Assert.assertNotNull(pieces, playerSide);

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
            addPieceToBoard(to, pieces); // add the wanted piece
            setIsGamePaused(false);
        }

        return isPresent;
    }
}
