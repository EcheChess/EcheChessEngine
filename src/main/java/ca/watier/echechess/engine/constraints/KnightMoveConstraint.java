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
import ca.watier.echechess.common.utils.MathUtils;
import ca.watier.echechess.common.utils.ObjectUtils;
import ca.watier.echechess.engine.engines.GenericGameHandler;
import ca.watier.echechess.engine.interfaces.MoveConstraint;

import java.util.List;
import java.util.Map;

/**
 * Created by yannick on 4/23/2017.
 */
public class KnightMoveConstraint implements MoveConstraint {

    public static final float KNIGHT_RADIUS_EQUATION = 2.23606797749979f;
    private static final List<Direction> DEFAULT_RADIUS_FINDER_POSITION = List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

    @Override
    public boolean isMoveValid(CasePosition from, CasePosition to, GenericGameHandler gameHandler, MoveMode moveMode) {

        if (ObjectUtils.hasNull(from, to, gameHandler)) {
            return false;
        }

        Map<CasePosition, Pieces> positionPiecesMap = gameHandler.getPiecesLocation();
        Pieces hittingPiece = positionPiecesMap.get(to);
        Pieces pieceFrom = positionPiecesMap.get(from);
        Side sideFrom = pieceFrom.getSide();

        boolean canAttack = true;

        if (MoveMode.NORMAL_OR_ATTACK_MOVE.equals(moveMode) && hittingPiece != null) {
            canAttack = !sideFrom.equals(hittingPiece.getSide()) && !Pieces.isKing(hittingPiece);
        }

        return canAttack && isTargetValid(from, to);
    }

    private boolean isTargetValid(CasePosition from, CasePosition to) {
        return MathUtils.isPositionOnCirclePerimeter(from, to, from.getX() + KNIGHT_RADIUS_EQUATION, from.getY()) &&
                !DEFAULT_RADIUS_FINDER_POSITION.contains(MathUtils.getDirectionFromPosition(from, to));
    }
}
