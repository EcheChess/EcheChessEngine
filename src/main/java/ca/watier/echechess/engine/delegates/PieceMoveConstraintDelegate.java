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

package ca.watier.echechess.engine.delegates;

import ca.watier.echechess.common.enums.*;
import ca.watier.echechess.engine.abstracts.GameBoardData;
import ca.watier.echechess.engine.constraints.*;
import ca.watier.echechess.engine.exceptions.NoMoveTypeDefinedException;
import ca.watier.echechess.engine.handlers.StandardKingHandlerImpl;
import ca.watier.echechess.engine.interfaces.KingHandler;
import ca.watier.echechess.engine.interfaces.MoveConstraint;
import ca.watier.echechess.engine.models.enums.MoveStatus;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by yannick on 4/26/2017.
 */

public class PieceMoveConstraintDelegate implements MoveConstraint, Serializable {

    private static final long serialVersionUID = -7763545818654487544L;

    private final MoveConstraint king;
    private final MoveConstraint queen;
    private final MoveConstraint rook;
    private final MoveConstraint bishop;
    private final MoveConstraint knight;
    private final MoveConstraint pawn;
    private final KingHandler kingHandler;

    public PieceMoveConstraintDelegate() {
        kingHandler = new StandardKingHandlerImpl(this);
        pawn = new PawnMoveConstraint();
        knight = new KnightMoveConstraint();
        bishop = new BishopMoveConstraint();
        rook = new RookMoveConstraint();
        queen = new QueenMoveConstraint();
        king = new KingMoveConstraint(kingHandler);
    }

    @Override
    public MoveStatus getMoveStatus(CasePosition from, CasePosition to, GameBoardData gameBoardData) {
        if (!ObjectUtils.allNotNull(from, to, gameBoardData)) {
            return MoveStatus.INVALID_MOVE;
        }

        MoveConstraint moveConstraint = getMoveConstraintFromPiece(from, gameBoardData);

        if (Objects.isNull(moveConstraint)) {
            return MoveStatus.INVALID_MOVE;
        }

        MoveStatus moveStatus = moveConstraint.getMoveStatus(from, to, gameBoardData);

        if (MoveStatus.isAttack(moveStatus)) {
            return handleAttack(from, to, gameBoardData, moveStatus);
        } else if(MoveStatus.isMove(moveStatus)) {
            return handleMove(from, to, gameBoardData, moveStatus);
        } else {
            return moveStatus;
        }
    }

    @Override
    public MoveType getMoveType(CasePosition from, CasePosition to, GameBoardData gameBoardData) {

        if (!ObjectUtils.allNotNull(from, to, gameBoardData)) {
            return MoveType.MOVE_NOT_ALLOWED;
        }

        MoveConstraint moveConstraint = getMoveConstraintFromPiece(from, gameBoardData);

        if (Objects.isNull(moveConstraint)) {
            return null;
        }

        MoveType moveType = getMoveType(from, to, gameBoardData, moveConstraint);
        return ObjectUtils.defaultIfNull(moveType, MoveType.NORMAL_MOVE);
    }

    public KingStatus getKingStatus(Side playerSide, GameBoardData gameBoardData) {
        return kingHandler.getKingStatus(playerSide, gameBoardData);
    }

    private MoveConstraint getMoveConstraintFromPiece(CasePosition from, GameBoardData gameBoardData) {
        Pieces fromPiece = gameBoardData.getPiece(from);
        return getMoveConstraint(fromPiece);
    }

    private MoveType getMoveType(CasePosition from, CasePosition to, GameBoardData gameBoardData, MoveConstraint moveConstraint) {
        try {
            return moveConstraint.getMoveType(from, to, gameBoardData);
        } catch (NoMoveTypeDefinedException ignored) {
            return null;
        }
    }

    private MoveStatus handleMove(CasePosition from, CasePosition to, GameBoardData gameBoardData, MoveStatus moveStatus) {
        if (MoveStatus.isMoveValid(moveStatus) && isKingNotCheck(from, to, gameBoardData)) {
            return MoveStatus.VALID_MOVE;
        } else {
            return MoveStatus.INVALID_MOVE;
        }
    }

    private MoveStatus handleAttack(CasePosition from, CasePosition to, GameBoardData gameBoardData, MoveStatus moveStatus) {
        if (MoveStatus.VALID_ATTACK.equals(moveStatus) && isKingNotCheck(from, to, gameBoardData)) {
            return MoveStatus.VALID_ATTACK;
        } else {
            return MoveStatus.INVALID_ATTACK;
        }
    }

    private boolean isKingNotCheck(CasePosition from, CasePosition to, GameBoardData gameBoardData) {
        return !kingHandler.isKingCheckAfterMove(from, to, gameBoardData);
    }

    private MoveConstraint getMoveConstraint(Pieces fromPiece) {

        if (Objects.isNull(fromPiece)) {
            return null;
        }

        switch (fromPiece) {
            case W_KING:
            case B_KING:
                return king;
            case W_QUEEN:
            case B_QUEEN:
                return queen;
            case W_ROOK:
            case B_ROOK:
                return rook;
            case W_BISHOP:
            case B_BISHOP:
                return bishop;
            case W_KNIGHT:
            case B_KNIGHT:
                return knight;
            case W_PAWN:
            case B_PAWN:
                return pawn;
            default:
                return null;
        }
    }
}
