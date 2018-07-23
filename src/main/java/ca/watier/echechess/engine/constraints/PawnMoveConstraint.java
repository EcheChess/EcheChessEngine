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

package ca.watier.echechess.engine.constraints;

import ca.watier.echechess.common.enums.*;
import ca.watier.echechess.common.interfaces.BaseUtils;
import ca.watier.echechess.common.utils.MathUtils;
import ca.watier.echechess.engine.engines.GenericGameHandler;
import ca.watier.echechess.engine.interfaces.MoveConstraint;
import ca.watier.echechess.engine.interfaces.SpecialMoveConstraint;
import ca.watier.echechess.engine.utils.GameUtils;


/**
 * Created by yannick on 4/23/2017.
 */
public class PawnMoveConstraint implements MoveConstraint, SpecialMoveConstraint {

    public static boolean isEnPassant(CasePosition from, CasePosition to, GenericGameHandler gameHandler, Side currentSide) {
        if (from == null || to == null || gameHandler == null || currentSide == null) {
            return false;
        }

        CasePosition enemyPawnPosition = getEnPassantEnemyPawnPosition(to, Side.getOtherPlayerSide(currentSide));

        if (enemyPawnPosition == null) {
            return false;
        }

        return isEnPassant(from, to, gameHandler, currentSide, enemyPawnPosition, gameHandler.getPiece(enemyPawnPosition));
    }

    public static CasePosition getEnPassantEnemyPawnPosition(CasePosition to, Side otherSide) {
        if (to == null || otherSide == null) {
            return null;
        }

        return MathUtils.getNearestPositionFromDirection(to, otherSide.equals(Side.BLACK) ? Direction.SOUTH : Direction.NORTH);
    }

    private static boolean isEnPassant(CasePosition from, CasePosition to, GenericGameHandler gameHandler, Side currentSide, CasePosition enemyPawnPosition, Pieces enemyPawn) {
        if (from == null || to == null || gameHandler == null || currentSide == null || enemyPawnPosition == null || enemyPawn == null) {
            return false;
        }

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
        if (from == null || to == null || gameHandler == null || moveMode == null) {
            return false;
        }

        Direction direction;
        Direction directionAttackOne;
        Direction directionAttackTwo;

        Pieces pieceFrom = gameHandler.getPiece(from);
        Side sideFrom = pieceFrom.getSide();

        //Pre checks, MUST BE FIRST
        if (Side.BLACK.equals(sideFrom)) {
            direction = Direction.SOUTH;
            directionAttackOne = Direction.SOUTH_WEST;
            directionAttackTwo = Direction.SOUTH_EAST;
        } else {
            direction = Direction.NORTH;
            directionAttackOne = Direction.NORTH_WEST;
            directionAttackTwo = Direction.NORTH_EAST;
        }

        Pieces hittingPiece = gameHandler.getPiece(to);
        int nbCaseBetweenPositions = BaseUtils.getSafeInteger(MathUtils.getDistanceBetweenPositionsWithCommonDirection(from, to));
        Direction directionFromPosition = MathUtils.getDirectionFromPosition(from, to);

        boolean otherPiecesBetweenTarget = GameUtils.isOtherPiecesBetweenTarget(from, to, gameHandler.getPiecesLocation());
        boolean isFrontMove = direction.equals(directionFromPosition);
        boolean isNbOfCaseIsOne = nbCaseBetweenPositions == 1;
        boolean isSpecialMove = isSpecialMove(from, to, gameHandler, pieceFrom, nbCaseBetweenPositions, otherPiecesBetweenTarget);
        boolean isMovable = (isSpecialMove || isNbOfCaseIsOne) && isFrontMove;

        if (directionFromPosition == null) {
            return false;
        }

        boolean isAttackMove = directionFromPosition.equals(directionAttackOne) || directionFromPosition.equals(directionAttackTwo);
        boolean isMoveValid = false;

        if (MoveMode.NORMAL_OR_ATTACK_MOVE.equals(moveMode)) {

            if (isMovable && hittingPiece == null) { //Normal move
                return true;
            } else if (isMovable) { //Blocked by another piece, with a normal move
                return false;
            } else if (hittingPiece == null) { //Not movable and target is null
                return false;
            }

            isMoveValid = !Pieces.isSameSide(hittingPiece, sideFrom) && !Pieces.isKing(hittingPiece) && isAttackMove;

        } else if (MoveMode.IS_KING_CHECK_MODE.equals(moveMode)) {
            isMoveValid = isAttackMove;
        }

        return isMoveValid && isNbOfCaseIsOne;
    }

    private boolean isSpecialMove(CasePosition from, CasePosition to, GenericGameHandler gameHandler, Pieces pieceFrom, int nbCaseBetweenPositions, boolean otherPiecesBetweenTarget) {
        return (isPawnMoveHop(from, pieceFrom, to, gameHandler, nbCaseBetweenPositions) && !otherPiecesBetweenTarget)
                || MoveType.EN_PASSANT.equals(getMoveType(from, to, gameHandler));
    }

    private boolean isPawnMoveHop(CasePosition from, Pieces pieceFrom, CasePosition to, GenericGameHandler gameHandler, int nbCaseBetweenPositions) {
        if (from == null || pieceFrom == null || to == null || gameHandler == null) {
            return false;
        }

        Direction directionFromPosition = MathUtils.getDirectionFromPosition(from, to);
        boolean isNorthOfSouth = Direction.NORTH.equals(directionFromPosition) || Direction.SOUTH.equals(directionFromPosition);

        return isNorthOfSouth && GameUtils.isDefaultPosition(from, pieceFrom, gameHandler) && nbCaseBetweenPositions == 2;
    }

    @Override
    public MoveType getMoveType(CasePosition from, CasePosition to, GenericGameHandler gameHandler) {
        if (from == null || to == null || gameHandler == null) {
            return MoveType.MOVE_NOT_ALLOWED;
        }

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
