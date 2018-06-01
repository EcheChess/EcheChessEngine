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
import ca.watier.echechess.common.utils.Pair;
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
    protected CasePosition getPositionByRankAndColumn(@NotNull Ranks rank, char column, @NotNull Side side) {
        return Arrays.stream(CasePosition.values()).filter(casePosition -> rank.equals(Ranks.getRank(casePosition, side))
                && casePosition.isOnSameColumn(column)).findFirst().orElse(null);
    }


    protected final void addPawnPromotion(@NotNull CasePosition from, @NotNull CasePosition to, @NotNull Side side) {

        if (Side.OBSERVER.equals(side)) {
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
    protected void changePawnSpecialMove(@NotNull Pieces piece, @NotNull CasePosition from, @NotNull CasePosition to) {
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
    protected void updatePlayerTurnValue(@NotNull Side side) {
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
}
