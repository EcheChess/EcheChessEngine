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

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.Direction;
import ca.watier.echechess.common.enums.Pieces;
import ca.watier.echechess.common.utils.MathUtils;
import ca.watier.echechess.common.utils.ObjectUtils;
import ca.watier.echechess.engine.abstracts.GameBoardData;
import ca.watier.echechess.engine.interfaces.MoveConstraint;
import ca.watier.echechess.engine.models.enums.MoveStatus;

import java.util.List;
import java.util.Map;

/**
 * Created by yannick on 4/23/2017.
 */
public class KnightMoveConstraint implements MoveConstraint {

    public static final float KNIGHT_RADIUS_EQUATION = 2.23606797749979f;
    private static final List<Direction> DEFAULT_RADIUS_FINDER_POSITION = List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
    private static final long serialVersionUID = 8532461631276349892L;

    @Override
    public MoveStatus getMoveStatus(CasePosition from, CasePosition to, GameBoardData gameBoardData) {

        if (ObjectUtils.hasNull(from, to, gameBoardData) || !isTargetValidPosition(from, to)) {
            return MoveStatus.getInvalidMoveStatusBasedOnTarget(to);
        }

        Map<CasePosition, Pieces> positionPiecesMap = gameBoardData.getPiecesLocation();
        Pieces pieceTo = positionPiecesMap.get(to);
        Pieces pieceFom = positionPiecesMap.get(from);

        if (Pieces.isSameSide(pieceTo, pieceFom)) {
            return MoveStatus.INVALID_ATTACK;
        } else if (Pieces.isKing(pieceTo)) {
            return MoveStatus.ENEMY_KING_PARTIAL_CHECK;
        } else {
            return MoveStatus.getValidMoveStatusBasedOnTarget(to);
        }
    }

    private boolean isTargetValidPosition(CasePosition from, CasePosition to) {
        return MathUtils.isPositionOnCirclePerimeter(from, to, from.getX() + KNIGHT_RADIUS_EQUATION, from.getY()) &&
                !DEFAULT_RADIUS_FINDER_POSITION.contains(MathUtils.getDirectionFromPosition(from, to));
    }
}
