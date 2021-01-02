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
import ca.watier.echechess.engine.abstracts.GameBoardData;
import ca.watier.echechess.engine.interfaces.MoveConstraint;
import ca.watier.echechess.engine.models.DistancePiecePositionModel;
import ca.watier.echechess.engine.models.enums.MoveStatus;
import ca.watier.echechess.engine.utils.GameUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serial;
import java.util.*;

/**
 * Created by yannick on 4/23/2017.
 */
public class DirectionalMoveConstraint implements MoveConstraint {

    @Serial
    private static final long serialVersionUID = 8337007739691270394L;

    private final DirectionPattern pattern;
    private final List<Direction> directionList;

    public DirectionalMoveConstraint(DirectionPattern pattern) {
        this.pattern = pattern;

        if (pattern == null) {
            throw new IllegalArgumentException("The pattern cannot be null!");
        }

        directionList = Arrays.asList(pattern.getDirections());
    }

    @Override
    public MoveStatus getMoveStatus(CasePosition from, CasePosition to, GameBoardData gameBoardData) {
        if (ObjectUtils.hasNull(from, to, pattern)) {
            return MoveStatus.getInvalidMoveStatusBasedOnTarget(from);
        }

        Map<CasePosition, Pieces> positionPiecesMap = gameBoardData.getPiecesLocation();
        Pieces pieceTo = positionPiecesMap.get(to);
        Pieces pieceFrom = positionPiecesMap.get(from);

        Direction directionFromPosition = MathUtils.getDirectionFromPosition(from, to);

        if (!directionList.contains(directionFromPosition)) {
            return MoveStatus.getInvalidMoveStatusBasedOnTarget(pieceTo);
        }

        return isMoveValid(from, to, pieceFrom, pieceTo, directionFromPosition, positionPiecesMap);
    }

    private MoveStatus isMoveValid(CasePosition from, CasePosition to, Pieces pieceFrom, Pieces pieceTo, Direction directionFromPosition, Map<CasePosition, Pieces> piecesMap) {

        if (!MathUtils.isPositionInLine(from, MathUtils.getNearestPositionFromDirection(from, directionFromPosition), to)) {
            return MoveStatus.getInvalidMoveStatusBasedOnTarget(pieceTo);
        }

        Set<DistancePiecePositionModel> piecesBetweenPosition = GameUtils.getPiecesBetweenPosition(from, to, piecesMap);

        Side sideFrom = pieceFrom.getSide();

        if (Pieces.isKing(pieceTo)) {
            return handleKingTarget(piecesBetweenPosition, pieceFrom, pieceTo);
        } else {
            return handleOtherTarget(sideFrom, pieceTo, piecesBetweenPosition);
        }
    }

    //[FROM] [ANY] [ANY] [KING]
    private MoveStatus handleKingTarget(Set<DistancePiecePositionModel> piecesBetweenPosition, Pieces pieceFrom, Pieces pieceTo) {
        boolean isSameSide = Pieces.isSameSide(pieceTo, pieceFrom);

        if (CollectionUtils.isEmpty(piecesBetweenPosition)) {
            if (isSameSide) {
                return MoveStatus.CAN_PROTECT_FRIENDLY;
            } else {
                return MoveStatus.ENEMY_KING_PARTIAL_CHECK;
            }
        }

        return MoveStatus.INVALID_ATTACK;
    }

    //[FROM] [ANY] [ANY] [ANY]
    private MoveStatus handleOtherTarget(Side sideFrom, Pieces pieceTo, Set<DistancePiecePositionModel> piecesBetweenPosition) {

        boolean isTarget = Objects.nonNull(pieceTo);
        boolean isPiecesBetween = CollectionUtils.isNotEmpty(piecesBetweenPosition);

        if (isPiecesBetween) {
            return MoveStatus.INVALID_ATTACK;
        } else if (isTarget) {
            if (Pieces.isSameSide(pieceTo, sideFrom)) {
                return MoveStatus.CAN_PROTECT_FRIENDLY;
            } else {
                return MoveStatus.VALID_ATTACK;
            }
        }
        return MoveStatus.VALID_MOVE;
    }
}
