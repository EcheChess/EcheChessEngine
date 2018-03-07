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

/**
 * Created by yannick on 4/23/2017.
 */
public class PawnMoveConstraint implements MoveConstraint, SpecialMoveConstraint {

    public static boolean isPawnMoveHop(CasePosition from, CasePosition to, GenericGameHandler gameHandler) {
        return GameUtils.isDefaultPosition(from, gameHandler.getPiece(from), gameHandler) &&
                BaseUtils.getSafeInteger(MathUtils.getDistanceBetweenPositionsWithCommonDirection(from, to)) == 2;
    }

    public static boolean isEnPassant(CasePosition from, CasePosition to, GenericGameHandler gameHandler, Side currentSide) {
        boolean isEnPassant = false;
        CasePosition enemyPawnPosition = getEnPassantEnemyPawnPosition(to, Side.getOtherPlayerSide(currentSide));

        if (enemyPawnPosition != null) {
            Pieces enemyPawn = gameHandler.getPiece(enemyPawnPosition);

            isEnPassant = isEnPassant(from, to, gameHandler, currentSide, enemyPawnPosition, enemyPawn);
        }

        return isEnPassant;
    }

    public static CasePosition getEnPassantEnemyPawnPosition(CasePosition to, Side otherSide) {
        return MathUtils.getNearestPositionFromDirection(to, otherSide.equals(Side.BLACK) ? Direction.SOUTH : Direction.NORTH);
    }

    private static boolean isEnPassant(CasePosition from, CasePosition to, GenericGameHandler gameHandler, Side currentSide, CasePosition enemyPawnPosition, Pieces enemyPawn) {
        boolean isEnPassant = false;

        boolean isFromOnFifthRank = Ranks.FIVE.equals(Ranks.getRank(from, currentSide));
        boolean isToOnSixthRank = Ranks.SIX.equals(Ranks.getRank(to, currentSide));

        if (isToOnSixthRank && isFromOnFifthRank && Pieces.isPawn(enemyPawn)) {
            boolean pawnUsedSpecialMove = gameHandler.isPawnUsedSpecialMove(enemyPawnPosition);
            Integer pieceTurnEnemyPawn = gameHandler.getPieceTurn(enemyPawnPosition);

            if (pieceTurnEnemyPawn != null) {
                int nbTotalMove = gameHandler.getNbTotalMove();
                boolean isLastMove = (nbTotalMove - pieceTurnEnemyPawn) == 1;
                boolean isMoveOneCase = BaseUtils.getSafeInteger(MathUtils.getDistanceBetweenPositionsWithCommonDirection(from, to)) == 1;

                isEnPassant = isLastMove && isMoveOneCase && pawnUsedSpecialMove;
            }
        }

        return isEnPassant;
    }

    @Override
    public boolean isMoveValid(CasePosition from, CasePosition to, GenericGameHandler gameHandler, MoveMode moveMode) {
        Assert.assertNotNull(from, to);

        Direction direction = Direction.NORTH;
        Direction directionAttack1 = Direction.NORTH_WEST;
        Direction directionAttack2 = Direction.NORTH_EAST;

        Pieces pieceFrom = gameHandler.getPiece(from);
        Side sideFrom = pieceFrom.getSide();

        //Pre checks, MUST BE FIRST
        if (Side.BLACK.equals(sideFrom)) {
            direction = Direction.SOUTH;
            directionAttack1 = Direction.SOUTH_WEST;
            directionAttack2 = Direction.SOUTH_EAST;
        }

        Pieces hittingPiece = gameHandler.getPiece(to);
        int nbCaseBetweenPositions = BaseUtils.getSafeInteger(MathUtils.getDistanceBetweenPositionsWithCommonDirection(from, to));
        Direction directionFromPosition = MathUtils.getDirectionFromPosition(from, to);

        boolean otherPiecesBetweenTarget = GameUtils.isOtherPiecesBetweenTarget(from, to, gameHandler.getPiecesLocation());

        boolean isFrontMove = direction.equals(directionFromPosition);
        boolean isNbOfCaseIsOne = nbCaseBetweenPositions == 1;

        boolean isSpecialMove = (isPawnMoveHop(from, pieceFrom, to, gameHandler, nbCaseBetweenPositions) && !otherPiecesBetweenTarget)
                || MoveType.EN_PASSANT.equals(getMoveType(from, to, gameHandler));

        boolean isMovable = (isSpecialMove || isNbOfCaseIsOne) &&
                isFrontMove;

        if (directionFromPosition == null) {
            return false;
        }

        boolean isAttackMove = directionFromPosition.equals(directionAttack1) || directionFromPosition.equals(directionAttack2);
        boolean isMoveValid = false;

        if (MoveMode.NORMAL_OR_ATTACK_MOVE.equals(moveMode)) {

            if (isMovable && hittingPiece == null) { //Normal move
                return true;
            } else if (isMovable) { //Blocked by another piece, with a normal move
                return false;
            }

            isMoveValid = hittingPiece != null && !hittingPiece.getSide().equals(sideFrom) && !Pieces.isKing(hittingPiece) &&
                    isAttackMove;

        } else if (MoveMode.IS_KING_CHECK_MODE.equals(moveMode)) {
            isMoveValid = isAttackMove;
        }

        return isMoveValid && isNbOfCaseIsOne;
    }

    private boolean isPawnMoveHop(CasePosition from, Pieces pieceFrom, CasePosition to, GenericGameHandler gameHandler, int nbCaseBetweenPositions) {

        Direction directionFromPosition = MathUtils.getDirectionFromPosition(from, to);
        boolean isNorthOfSouth = Direction.NORTH.equals(directionFromPosition) || Direction.SOUTH.equals(directionFromPosition);

        return isNorthOfSouth && GameUtils.isDefaultPosition(from, pieceFrom, gameHandler) && nbCaseBetweenPositions == 2;
    }

    @Override
    public MoveType getMoveType(CasePosition from, CasePosition to, GenericGameHandler gameHandler) {
        Assert.assertNotNull(from, to, gameHandler);

        MoveType value = MoveType.NORMAL_MOVE;
        Pieces pieceFrom = gameHandler.getPiece(from);

        if (Pieces.isPawn(pieceFrom)) {
            Side currentSide = pieceFrom.getSide();
            Side otherSide = Side.getOtherPlayerSide(currentSide);

            CasePosition enemyPawnPosition = getEnPassantEnemyPawnPosition(to, otherSide);

            if (enemyPawnPosition != null) {
                Pieces enemyPawn = gameHandler.getPiece(enemyPawnPosition);

                int nbCaseBetweenPositions = BaseUtils.getSafeInteger(MathUtils.getDistanceBetweenPositionsWithCommonDirection(from, to));

                if (isPawnMoveHop(from, pieceFrom, to, gameHandler, nbCaseBetweenPositions)) {
                    return MoveType.PAWN_HOP;
                } else if (enemyPawn == null || Pieces.isSameSide(pieceFrom, enemyPawn) || !Ranks.FOUR.equals(Ranks.getRank(enemyPawnPosition, otherSide))) {
                    return value;
                }

                if (isEnPassant(from, to, gameHandler, currentSide, enemyPawnPosition, enemyPawn)) {
                    value = MoveType.EN_PASSANT;
                }

            }
        }

        return value;
    }
}
