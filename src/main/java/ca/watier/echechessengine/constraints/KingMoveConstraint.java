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

package ca.watier.echechessengine.constraints;

import ca.watier.echechessengine.engines.GenericGameHandler;
import ca.watier.echechessengine.interfaces.MoveConstraint;
import ca.watier.echechessengine.interfaces.SpecialMoveConstraint;
import ca.watier.echechessengine.utils.GameUtils;
import ca.watier.echesscommon.enums.*;
import ca.watier.echesscommon.interfaces.BaseUtils;
import ca.watier.echesscommon.utils.Assert;
import ca.watier.echesscommon.utils.MathUtils;

import java.util.List;
import java.util.Map;

import static ca.watier.echesscommon.enums.CasePosition.*;
import static ca.watier.echesscommon.enums.Side.BLACK;
import static ca.watier.echesscommon.enums.Side.WHITE;
import static ca.watier.echesscommon.interfaces.BaseUtils.getSafeBoolean;

/**
 * Created by yannick on 4/23/2017.
 */
public class KingMoveConstraint implements MoveConstraint, SpecialMoveConstraint {

    @Override
    public boolean isMoveValid(CasePosition from, CasePosition to, GenericGameHandler gameHandler, MoveMode moveMode) {
        Assert.assertNotNull(from, to, gameHandler);
        Pieces hittingPiece = gameHandler.getPiece(to);
        Pieces pieceFrom = gameHandler.getPiece(from);
        Side sideFrom = pieceFrom.getSide();

        boolean checkHit = true;
        if (MoveMode.NORMAL_OR_ATTACK_MOVE.equals(moveMode)) {
            checkHit = hittingPiece == null || !sideFrom.equals(hittingPiece.getSide()) && !Pieces.isKing(hittingPiece);
        }

        return (BaseUtils.getSafeInteger(MathUtils.getDistanceBetweenPositionsWithCommonDirection(from, to)) == 1) && checkHit;
    }


    /*
       --------- Castling ---------
       URL: https://en.wikipedia.org/wiki/Castling
       Castling is permissible if and only if all of the following conditions hold (Schiller 2003:19):
           The king and the chosen rook are on the player's first rank.
           Neither the king nor the chosen rook has previously moved.
           There are no pieces between the king and the chosen rook.
           The king is not currently in check.
           The king does not pass through a square that is attacked by an enemy piece.
           The king does not end up in check. (True of any legal move.)
    */
    @Override
    public MoveType getMoveType(CasePosition from, CasePosition to, GenericGameHandler gameHandler) {
        Assert.assertNotNull(from, to, gameHandler);

        MoveType moveType = MoveType.NORMAL_MOVE;
        Pieces pieceFrom = gameHandler.getPiece(from);
        Side sideFrom = pieceFrom.getSide();
        Pieces pieceTo = gameHandler.getPiece(to);
        Map<CasePosition, Pieces> piecesLocation = gameHandler.getPiecesLocation();

        if (pieceTo == null) {
            return moveType;
        }

        if (Pieces.isSameSide(pieceFrom, pieceTo) && Pieces.isKing(pieceFrom) && Pieces.isRook(pieceTo)) { //Castling
            List<CasePosition> piecesBetweenKingAndRook = GameUtils.getPiecesBetweenPosition(from, to, piecesLocation);


            boolean isQueenSide = Direction.WEST.equals(MathUtils.getDirectionFromPosition(from, to));

            CasePosition kingPosition;
            CasePosition positionWhereKingPass;
            if (BLACK.equals(sideFrom)) {
                if (isQueenSide) {
                    kingPosition = C8;
                    positionWhereKingPass = D8;
                } else {
                    kingPosition = G8;
                    positionWhereKingPass = F8;
                }
            } else if (WHITE.equals(sideFrom)) {
                if (isQueenSide) {
                    kingPosition = C1;
                    positionWhereKingPass = D1;
                } else {
                    kingPosition = G1;
                    positionWhereKingPass = F1;
                }
            } else {
                throw new IllegalStateException("The king position cannot be null!");
            }

            boolean isPieceAreNotMoved = !getSafeBoolean(gameHandler.isPieceMoved(from)) && !getSafeBoolean(gameHandler.isPieceMoved(to));
            boolean isNoPieceBetweenKingAndRook = piecesBetweenKingAndRook.isEmpty();
            boolean isNoPieceAttackingBetweenKingAndRook = gameHandler.getPiecesThatCanHitPosition(Side.getOtherPlayerSide(sideFrom), positionWhereKingPass).isEmpty();
            boolean isKingNotCheckAtCurrentLocation = !gameHandler.isKingCheckAtPosition(from, sideFrom);
            boolean kingNotCheckAtEndPosition = !gameHandler.isKingCheckAtPosition(kingPosition, sideFrom);

            if (isPieceAreNotMoved && isNoPieceBetweenKingAndRook &&
                    isNoPieceAttackingBetweenKingAndRook && isKingNotCheckAtCurrentLocation &&
                    kingNotCheckAtEndPosition) {
                moveType = MoveType.CASTLING;
            }
        }

        return moveType;
    }
}
