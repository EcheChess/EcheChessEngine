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
import ca.watier.echechess.common.utils.ObjectUtils;
import ca.watier.echechess.engine.abstracts.GameBoardData;
import ca.watier.echechess.engine.interfaces.MoveConstraint;
import ca.watier.echechess.engine.models.enums.MoveStatus;
import ca.watier.echechess.engine.utils.GameUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.Objects;
import java.util.Set;


/**
 * Created by yannick on 4/23/2017.
 */
public class PawnMoveConstraint implements MoveConstraint {

    private static final SetMultimap<Side, Direction> ATTACK_DIRECTION_BY_SIDE;

    static {
        ATTACK_DIRECTION_BY_SIDE = HashMultimap.create(2, 2);
        ATTACK_DIRECTION_BY_SIDE.put(Side.BLACK, Direction.SOUTH_WEST);
        ATTACK_DIRECTION_BY_SIDE.put(Side.BLACK, Direction.SOUTH_EAST);

        ATTACK_DIRECTION_BY_SIDE.put(Side.WHITE, Direction.NORTH_WEST);
        ATTACK_DIRECTION_BY_SIDE.put(Side.WHITE, Direction.NORTH_EAST);

    }

    public static boolean isEnPassant(CasePosition from, CasePosition to, GameBoardData gameBoardData, Side currentSide) {

        if (ObjectUtils.hasNull(from, to, gameBoardData, currentSide)) {
            return false;
        }

        CasePosition enemyPawnPosition = getEnemyPawnPositionFromEnPassant(to, Side.getOtherPlayerSide(currentSide));
        Pieces enemyPawnPiece = gameBoardData.getPiece(enemyPawnPosition);

        if (enemyPawnPosition == null || Pieces.isSameSide(enemyPawnPiece, currentSide)) {
            return false;
        }

        return isEnPassant(from, to, gameBoardData, currentSide, enemyPawnPosition, enemyPawnPiece);
    }

    /**
     * Get the enemy pawn, from the position of the "en passant" move
     *
     * @param enPassantMovePosition - The "en passant" position (not the pawn!)
     * @param otherSide
     * @return
     */
    public static CasePosition getEnemyPawnPositionFromEnPassant(CasePosition enPassantMovePosition, Side otherSide) {
        if (ObjectUtils.hasNull(enPassantMovePosition, otherSide)) {
            return null;
        }

        return MathUtils.getNearestPositionFromDirection(enPassantMovePosition, otherSide.equals(Side.BLACK) ? Direction.SOUTH : Direction.NORTH);
    }

    private static boolean isEnPassant(CasePosition from, CasePosition to, GameBoardData gameBoardData, Side currentSide, CasePosition enemyPawnPosition, Pieces enemyPawn) {


        if (ObjectUtils.hasNull(from, to, gameBoardData, currentSide, enemyPawnPosition, enemyPawn)) {
            return false;
        }

        boolean isEnPassant = false;
        boolean isFromOnFifthRank = Ranks.FIVE.equals(Ranks.getRank(from, currentSide));
        boolean isToOnSixthRank = Ranks.SIX.equals(Ranks.getRank(to, currentSide));

        if (isToOnSixthRank && isFromOnFifthRank && Pieces.isPawn(enemyPawn)) {
            boolean pawnUsedSpecialMove = gameBoardData.isPawnUsedSpecialMove(enemyPawnPosition);
            Integer pieceTurnEnemyPawn = gameBoardData.getPieceTurn(enemyPawnPosition);

            if (pieceTurnEnemyPawn != null) {
                int nbTotalMove = gameBoardData.getNbTotalMove();
                boolean isLastMove = (nbTotalMove - pieceTurnEnemyPawn) == 1;
                boolean isMoveOneCase = BaseUtils.getSafeInteger(MathUtils.getDistanceBetweenPositionsWithCommonDirection(from, to)) == 1;

                isEnPassant = isLastMove && isMoveOneCase && pawnUsedSpecialMove;
            }
        }

        return isEnPassant;
    }

    /**
     * Get the "en passant" move, from the pawn position
     *
     * @param pawnPosition
     * @param otherSide
     * @return
     */
    public static CasePosition getEnPassantPositionFromEnemyPawn(CasePosition pawnPosition, Side otherSide) {
        if (ObjectUtils.hasNull(pawnPosition, otherSide)) {
            return null;
        }

        Direction direction;

        switch (Ranks.getRank(pawnPosition, otherSide)) {
            case TWO: //Not moved
                direction = otherSide.equals(Side.BLACK) ? Direction.SOUTH : Direction.NORTH;
                break;
            case FOUR: //Pawn hop
                direction = otherSide.equals(Side.BLACK) ? Direction.NORTH : Direction.SOUTH;
                break;
            default:
                return null; //Not Valid
        }

        return MathUtils.getNearestPositionFromDirection(pawnPosition, direction);
    }

    @Override
    public MoveStatus getMoveStatus(CasePosition from, CasePosition to, GameBoardData gameBoardData) {
        if (ObjectUtils.hasNull(from, to, gameBoardData)) {
            return MoveStatus.getInvalidMoveStatusBasedOnTarget(to);
        }

        Pieces pieceFrom = gameBoardData.getPiece(from);
        Side sideFrom = pieceFrom.getSide();
        Pieces pieceTo = gameBoardData.getPiece(to);
        Direction directionFromPosition = MathUtils.getDirectionFromPosition(from, to);

        if (Pieces.isSameSide(pieceTo, pieceFrom) || Objects.isNull(directionFromPosition)) {
            return MoveStatus.getInvalidMoveStatusBasedOnTarget(to);
        }

        Direction direction = getMovePositionBySide(sideFrom);
        Set<Direction> attackDirection = ATTACK_DIRECTION_BY_SIDE.get(sideFrom);

        int nbCaseBetweenPositions = BaseUtils.getSafeInteger(MathUtils.getDistanceBetweenPositionsWithCommonDirection(from, to));
        boolean otherPiecesBetweenTarget = GameUtils.isOtherPiecesBetweenTarget(from, to, gameBoardData.getPiecesLocation());
        boolean isNbOfCaseIsOne = nbCaseBetweenPositions == 1;

        boolean isAttackMove = attackDirection.contains(directionFromPosition) && isNbOfCaseIsOne;
        boolean isNormalMove = (direction.equals(directionFromPosition) && isNbOfCaseIsOne) ||
                isSpecialMove(from, to, gameBoardData, pieceFrom, nbCaseBetweenPositions, otherPiecesBetweenTarget);


        if (isAttackMove) {
            return handleAttackMode(from, pieceFrom, to, pieceTo, gameBoardData, sideFrom);
        } else if (isNormalMove) {
            return handleNormalMove(pieceTo);
        } else {
            return MoveStatus.getInvalidMoveStatusBasedOnTarget(to);
        }
    }

    private MoveStatus handleNormalMove(Pieces pieceTo) {
        if (Objects.isNull(pieceTo)) {
            return MoveStatus.VALID_MOVE;
        } else {
            return MoveStatus.INVALID_ATTACK;
        }
    }

    private MoveStatus handleAttackMode(CasePosition from, Pieces pieceFrom, CasePosition to, Pieces pieceTo, GameBoardData gameBoardData, Side sideFrom) {
        if (Objects.isNull(pieceTo)) {
            if (isEnPassant(from, to, gameBoardData, sideFrom)) {
                return MoveStatus.VALID_ATTACK;
            } else {
                return MoveStatus.INVALID_ATTACK;
            }
        } else if (Pieces.isKing(pieceTo)) {
            if (Pieces.isSameSide(pieceFrom, pieceTo)) {
                return MoveStatus.CAN_PROTECT_FRIENDLY;
            } else {
                return MoveStatus.ENEMY_KING_PARTIAL_CHECK;
            }
        } else {
            return MoveStatus.VALID_ATTACK;
        }
    }

    private Direction getMovePositionBySide(Side side) {
        if (Side.BLACK.equals(side)) {
            return Direction.SOUTH;
        } else {
            return Direction.NORTH;
        }
    }

    @Override
    public MoveType getMoveType(CasePosition from, CasePosition to, GameBoardData gameBoardData) {
        if (ObjectUtils.hasNull(from, to, gameBoardData)) {
            return MoveType.MOVE_NOT_ALLOWED;
        }

        MoveType value = MoveType.NORMAL_MOVE;
        Pieces pieceFrom = gameBoardData.getPiece(from);

        if (Pieces.isPawn(pieceFrom)) {
            Side currentSide = pieceFrom.getSide();
            Side otherSide = Side.getOtherPlayerSide(currentSide);

            CasePosition enemyPawnPosition = getEnemyPawnPositionFromEnPassant(to, otherSide);

            if (enemyPawnPosition != null) {
                Pieces enemyPawn = gameBoardData.getPiece(enemyPawnPosition);

                int nbCaseBetweenPositions = BaseUtils.getSafeInteger(MathUtils.getDistanceBetweenPositionsWithCommonDirection(from, to));

                if (isPawnMoveHop(from, pieceFrom, to, gameBoardData, nbCaseBetweenPositions)) {
                    return MoveType.PAWN_HOP;
                } else if (enemyPawn == null || Pieces.isSameSide(pieceFrom, enemyPawn) || !Ranks.FOUR.equals(Ranks.getRank(enemyPawnPosition, otherSide))) {
                    return value;
                }

                if (isEnPassant(from, to, gameBoardData, currentSide, enemyPawnPosition, enemyPawn)) {
                    value = MoveType.EN_PASSANT;
                }
            }
        }

        return value;
    }

    private boolean isSpecialMove(CasePosition from, CasePosition to, GameBoardData gameHandler, Pieces pieceFrom, int nbCaseBetweenPositions, boolean otherPiecesBetweenTarget) {
        return (isPawnMoveHop(from, pieceFrom, to, gameHandler, nbCaseBetweenPositions) && !otherPiecesBetweenTarget)
                || MoveType.EN_PASSANT.equals(getMoveType(from, to, gameHandler));
    }

    private boolean isPawnMoveHop(CasePosition from, Pieces pieceFrom, CasePosition to, GameBoardData gameHandler, int nbCaseBetweenPositions) {
        if (ObjectUtils.hasNull(from, pieceFrom, to, gameHandler)) {
            return false;
        }

        Side side = pieceFrom.getSide();
        Direction directionFromPosition = MathUtils.getDirectionFromPosition(from, to);

        if ((Side.BLACK.equals(side) && !Direction.SOUTH.equals(directionFromPosition)) ||
                (Side.WHITE.equals(side) && !Direction.NORTH.equals(directionFromPosition))) {
            return false;
        }

        return GameUtils.isDefaultPosition(from, pieceFrom, gameHandler) && nbCaseBetweenPositions == 2;
    }
}
