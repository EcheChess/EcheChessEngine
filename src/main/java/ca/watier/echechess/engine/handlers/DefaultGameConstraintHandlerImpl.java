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

package ca.watier.echechess.engine.handlers;

import ca.watier.echechess.common.enums.*;
import ca.watier.echechess.engine.constraints.*;
import ca.watier.echechess.engine.engines.GenericGameHandler;
import ca.watier.echechess.engine.exceptions.NoMoveTypeDefinedException;
import ca.watier.echechess.engine.interfaces.GameConstraintHandler;
import ca.watier.echechess.engine.interfaces.MoveConstraint;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by yannick on 4/26/2017.
 */

public class DefaultGameConstraintHandlerImpl implements GameConstraintHandler, Serializable {

    private static final long serialVersionUID = -7763545818654487544L;

    private static final MoveConstraint KING = new KingMoveConstraint();
    private static final MoveConstraint QUEEN = new QueenMoveConstraint();
    private static final MoveConstraint ROOK = new RookMoveConstraint();
    private static final MoveConstraint BISHOP = new BishopMoveConstraint();
    private static final MoveConstraint KNIGHT = new KnightMoveConstraint();
    private static final MoveConstraint PAWN = new PawnMoveConstraint();

    @Override
    public MoveType getMoveType(CasePosition from, CasePosition to, GenericGameHandler gameHandler) {

        if (!ObjectUtils.allNotNull(from, to, gameHandler)) {
            return MoveType.MOVE_NOT_ALLOWED;
        }

        Pieces fromPiece = gameHandler.getPiece(from);
        MoveConstraint moveConstraint = getMoveConstraint(fromPiece);

        if (Objects.isNull(moveConstraint)) {
            return null;
        }

        MoveType moveType = getMoveType(from, to, gameHandler, moveConstraint);
        return ObjectUtils.defaultIfNull(moveType, MoveType.NORMAL_MOVE);
    }

    private MoveType getMoveType(CasePosition from, CasePosition to, GenericGameHandler gameHandler, MoveConstraint moveConstraint) {
        try {
            return moveConstraint.getMoveType(from, to, gameHandler);
        } catch (NoMoveTypeDefinedException ignored) {
            return null;
        }
    }

    private MoveConstraint getMoveConstraint(Pieces fromPiece) {

        if (Objects.isNull(fromPiece)) {
            return null;
        }

        switch (fromPiece) {
            case W_KING:
            case B_KING:
                return KING;
            case W_QUEEN:
            case B_QUEEN:
                return QUEEN;
            case W_ROOK:
            case B_ROOK:
                return ROOK;
            case W_BISHOP:
            case B_BISHOP:
                return BISHOP;
            case W_KNIGHT:
            case B_KNIGHT:
                return KNIGHT;
            case W_PAWN:
            case B_PAWN:
                return PAWN;
            default:
                return null;
        }
    }

    @Override
    public boolean isPieceMovableTo(CasePosition from, CasePosition to, Side playerSide, GenericGameHandler gameHandler, MoveMode moveMode) {
        if (!ObjectUtils.allNotNull(from, to, playerSide)) {
            return false;
        }

        Pieces fromPiece = gameHandler.getPiece(from);

        if (Side.OBSERVER.equals(playerSide) || !Pieces.isSameSide(fromPiece, playerSide)) {
            return false;
        }

        MoveConstraint moveConstraint = getMoveConstraint(fromPiece);

        if (Objects.isNull(moveConstraint)) {
            return false;
        } else {
            return moveConstraint.isMoveValid(from, to, gameHandler, moveMode);
        }
    }
}
