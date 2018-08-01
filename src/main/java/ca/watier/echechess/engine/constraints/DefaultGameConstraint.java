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

package ca.watier.echechess.engine.constraints;

import ca.watier.echechess.common.enums.*;
import ca.watier.echechess.engine.constraints.*;
import ca.watier.echechess.engine.engines.GenericGameHandler;
import ca.watier.echechess.engine.interfaces.GameConstraint;
import ca.watier.echechess.engine.interfaces.MoveConstraint;
import ca.watier.echechess.engine.interfaces.SpecialMoveConstraint;

import java.util.EnumMap;
import java.util.Map;

import static ca.watier.echechess.common.enums.Pieces.*;

/**
 * Created by yannick on 4/26/2017.
 */

public class DefaultGameConstraint implements GameConstraint {

    private static final Map<Pieces, MoveConstraint> MOVE_CONSTRAINT_MAP = new EnumMap<>(Pieces.class);
    private static final MoveConstraint KING = new KingMoveConstraint();
    private static final MoveConstraint QUEEN = new QueenMoveConstraint();
    private static final MoveConstraint ROOK = new RookMoveConstraint();
    private static final MoveConstraint BISHOP = new BishopMoveConstraint();
    private static final MoveConstraint KNIGHT = new KnightMoveConstraint();
    private static final MoveConstraint PAWN = new PawnMoveConstraint();

    static {
        MOVE_CONSTRAINT_MAP.put(W_KING, KING);
        MOVE_CONSTRAINT_MAP.put(B_KING, KING);
        MOVE_CONSTRAINT_MAP.put(W_QUEEN, QUEEN);
        MOVE_CONSTRAINT_MAP.put(B_QUEEN, QUEEN);
        MOVE_CONSTRAINT_MAP.put(W_ROOK, ROOK);
        MOVE_CONSTRAINT_MAP.put(B_ROOK, ROOK);
        MOVE_CONSTRAINT_MAP.put(W_BISHOP, BISHOP);
        MOVE_CONSTRAINT_MAP.put(B_BISHOP, BISHOP);
        MOVE_CONSTRAINT_MAP.put(W_KNIGHT, KNIGHT);
        MOVE_CONSTRAINT_MAP.put(B_KNIGHT, KNIGHT);
        MOVE_CONSTRAINT_MAP.put(W_PAWN, PAWN);
        MOVE_CONSTRAINT_MAP.put(B_PAWN, PAWN);
    }

    @Override
    public boolean isPieceMovableTo(CasePosition from, CasePosition to, Side playerSide, GenericGameHandler gameHandler, MoveMode moveMode) {
        if (from == null || to == null || playerSide == null) {
            return false;
        }

        Pieces fromPiece = gameHandler.getPiece(from);

        if (Side.OBSERVER.equals(playerSide) || !fromPiece.getSide().equals(playerSide)) {
            return false;
        }

        MoveConstraint moveConstraint = MOVE_CONSTRAINT_MAP.get(fromPiece);
        return moveConstraint.isMoveValid(from, to, gameHandler, moveMode);
    }


    @Override
    public MoveType getMoveType(CasePosition from, CasePosition to, GenericGameHandler gameHandler) {
        if (from == null || to == null || gameHandler == null) {
            return MoveType.MOVE_NOT_ALLOWED;
        }

        MoveType value = MoveType.NORMAL_MOVE;

        Pieces fromPiece = gameHandler.getPiece(from);
        MoveConstraint moveConstraint = MOVE_CONSTRAINT_MAP.get(fromPiece);

        if (moveConstraint instanceof SpecialMoveConstraint) {
            value = ((SpecialMoveConstraint) moveConstraint).getMoveType(from, to, gameHandler);

            if (value == null) {
                value = MoveType.NORMAL_MOVE;
            }
        }

        return value;
    }
}
