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

package ca.watier.echechess.engine.utils;

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.Direction;
import ca.watier.echechess.common.enums.Pieces;
import ca.watier.echechess.common.utils.MathUtils;
import ca.watier.echechess.engine.abstracts.GameBoardData;
import ca.watier.echechess.engine.models.DistancePiecePositionModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;

/**
 * Created by yannick on 4/23/2017.
 */
public final class GameUtils {

    private static final CasePosition A_1 = CasePosition.A1;
    private static final CasePosition B_1 = CasePosition.B1;
    private static final CasePosition C_1 = CasePosition.C1;
    private static final CasePosition D_1 = CasePosition.D1;
    private static final CasePosition E_1 = CasePosition.E1;
    private static final CasePosition F_1 = CasePosition.F1;
    private static final CasePosition G_1 = CasePosition.G1;
    private static final CasePosition H_1 = CasePosition.H1;
    private static final CasePosition A_2 = CasePosition.A2;
    private static final CasePosition B_2 = CasePosition.B2;
    private static final CasePosition C_2 = CasePosition.C2;
    private static final CasePosition D_2 = CasePosition.D2;
    private static final CasePosition E_2 = CasePosition.E2;
    private static final CasePosition F_2 = CasePosition.F2;
    private static final CasePosition G_2 = CasePosition.G2;
    private static final CasePosition H_2 = CasePosition.H2;
    private static final CasePosition A_8 = CasePosition.A8;
    private static final CasePosition B_8 = CasePosition.B8;
    private static final CasePosition C_8 = CasePosition.C8;
    private static final CasePosition D_8 = CasePosition.D8;
    private static final CasePosition F_8 = CasePosition.F8;
    private static final CasePosition E_8 = CasePosition.E8;
    private static final CasePosition G_8 = CasePosition.G8;
    private static final CasePosition H_8 = CasePosition.H8;
    private static final CasePosition A_7 = CasePosition.A7;
    private static final CasePosition B_7 = CasePosition.B7;
    private static final CasePosition C_7 = CasePosition.C7;
    private static final CasePosition D_7 = CasePosition.D7;
    private static final CasePosition E_7 = CasePosition.E7;
    private static final CasePosition F_7 = CasePosition.F7;
    private static final CasePosition G_7 = CasePosition.G7;
    private static final CasePosition H_7 = CasePosition.H7;
    private static final Pieces W_ROOK = Pieces.W_ROOK;
    private static final Pieces W_KNIGHT = Pieces.W_KNIGHT;
    private static final Pieces W_BISHOP = Pieces.W_BISHOP;
    private static final Pieces W_QUEEN = Pieces.W_QUEEN;
    private static final Pieces W_KING = Pieces.W_KING;
    private static final Pieces W_PAWN = Pieces.W_PAWN;
    private static final Pieces B_ROOK = Pieces.B_ROOK;
    private static final Pieces B_KNIGHT = Pieces.B_KNIGHT;
    private static final Pieces B_BISHOP = Pieces.B_BISHOP;
    private static final Pieces B_QUEEN = Pieces.B_QUEEN;
    private static final Pieces B_KING = Pieces.B_KING;
    private static final Pieces B_PAWN = Pieces.B_PAWN;

    private GameUtils() {
    }

    /**
     * Create a new EnumMap containing the default game
     *
     * @return
     */
    public static Map<CasePosition, Pieces> getDefaultGame() {
        Map<CasePosition, Pieces> gameMap = new EnumMap<>(CasePosition.class);
        gameMap.put(A_1, W_ROOK);
        gameMap.put(B_1, W_KNIGHT);
        gameMap.put(C_1, W_BISHOP);
        gameMap.put(D_1, W_QUEEN);
        gameMap.put(E_1, W_KING);
        gameMap.put(F_1, W_BISHOP);
        gameMap.put(G_1, W_KNIGHT);
        gameMap.put(H_1, W_ROOK);
        gameMap.put(A_2, W_PAWN);
        gameMap.put(B_2, W_PAWN);
        gameMap.put(C_2, W_PAWN);
        gameMap.put(D_2, W_PAWN);
        gameMap.put(E_2, W_PAWN);
        gameMap.put(F_2, W_PAWN);
        gameMap.put(G_2, W_PAWN);
        gameMap.put(H_2, W_PAWN);

        gameMap.put(A_8, B_ROOK);
        gameMap.put(B_8, B_KNIGHT);
        gameMap.put(C_8, B_BISHOP);
        gameMap.put(D_8, B_QUEEN);
        gameMap.put(E_8, B_KING);
        gameMap.put(F_8, B_BISHOP);
        gameMap.put(G_8, B_KNIGHT);
        gameMap.put(H_8, B_ROOK);
        gameMap.put(A_7, B_PAWN);
        gameMap.put(B_7, B_PAWN);
        gameMap.put(C_7, B_PAWN);
        gameMap.put(D_7, B_PAWN);
        gameMap.put(E_7, B_PAWN);
        gameMap.put(F_7, B_PAWN);
        gameMap.put(G_7, B_PAWN);
        gameMap.put(H_7, B_PAWN);
        return gameMap;
    }


    /**
     * Init an {@link EnumMap} based of a board
     *
     * @param positionPiecesMap
     * @return
     */
    public static Map<CasePosition, Boolean> initNewMovedPieceMap(Map<CasePosition, Pieces> positionPiecesMap) {
        Map<CasePosition, Boolean> values = new EnumMap<>(CasePosition.class);

        if (positionPiecesMap == null || positionPiecesMap.isEmpty()) {
            return values;
        }

        for (CasePosition position : positionPiecesMap.keySet()) {
            values.put(position, false);
        }

        return values;
    }


    /**
     * Init an {@link EnumMap} based of a board, return the pawns only
     *
     * @param positionPiecesMap
     * @return
     */
    public static Map<CasePosition, Boolean> initPawnMap(Map<CasePosition, Pieces> positionPiecesMap) {
        Map<CasePosition, Boolean> values = new EnumMap<>(CasePosition.class);

        if (positionPiecesMap == null || positionPiecesMap.isEmpty()) {
            return values;
        }

        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : positionPiecesMap.entrySet()) {
            if (Pieces.isPawn(casePositionPiecesEntry.getValue())) {
                values.put(casePositionPiecesEntry.getKey(), false);
            }
        }

        return values;
    }


    /**
     * Init an {@link EnumMap} based of a board
     *
     * @param positionPiecesMap
     * @return
     */
    public static Map<CasePosition, Integer> initTurnMap(Map<CasePosition, Pieces> positionPiecesMap) {
        Map<CasePosition, Integer> values = new EnumMap<>(CasePosition.class);

        if (positionPiecesMap == null || positionPiecesMap.isEmpty()) {
            return values;
        }

        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : positionPiecesMap.entrySet()) {
            values.put(casePositionPiecesEntry.getKey(), 0);
        }

        return values;
    }

    /**
     * Check if it's the default position for the piece
     *
     * @param position
     * @param pieces
     * @param gameBoard
     * @return
     */
    public static boolean isDefaultPosition(CasePosition position, Pieces pieces, GameBoardData gameBoard) {
        if (position == null || pieces == null || gameBoard == null) {
            return false;
        }

        return pieces.equals(gameBoard.getDefaultPositions().get(position));
    }

    /**
     * Check if there's one or more piece between two pieces
     *
     * @param from
     * @param to
     * @param pieces
     * @return
     */
    public static boolean isOtherPiecesBetweenTarget(CasePosition from, CasePosition to, Map<CasePosition, Pieces> pieces) {
        if (from == null || to == null || pieces == null) {
            return false;
        }

        return CollectionUtils.isNotEmpty(getPiecesBetweenPosition(from, to, pieces));
    }

    /**
     * Gets all {@link CasePosition} that have a piece between two positions
     *
     * @param from
     * @param to
     * @param pieces
     * @return
     */
    public static Set<DistancePiecePositionModel> getPiecesBetweenPosition(CasePosition from, CasePosition to, Map<CasePosition, Pieces> pieces) {

        Set<DistancePiecePositionModel> positions = new TreeSet<>();

        if (!ObjectUtils.allNotNull(from, to, pieces)) {
            return positions;
        }

        int distanceFromDestination = MathUtils.getDistanceBetweenPositionsWithCommonDirection(from, to);
        Direction directionToDestination = MathUtils.getDirectionFromPosition(from, to);

        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : pieces.entrySet()) {

            CasePosition position = casePositionPiecesEntry.getKey();
            Pieces piece = casePositionPiecesEntry.getValue();
            if (piece != null && position != from && position != to) {

                int distanceToOther = MathUtils.getDistanceBetweenPositionsWithCommonDirection(from, position);
                Direction directionToOther = MathUtils.getDirectionFromPosition(from, position);

                if (MathUtils.isPositionInLine(from, to, position) && distanceFromDestination > distanceToOther && directionToOther == directionToDestination) {
                    positions.add(new DistancePiecePositionModel(distanceToOther, piece, position));
                }
            }
        }

        return positions;
    }

    /**
     * Gets the position of a piece (first found)
     *
     * @param pieces
     * @param positionPiecesMap
     * @return
     */
    public static CasePosition getSinglePiecePosition(Pieces pieces, Map<CasePosition, Pieces> positionPiecesMap) {
        if (pieces == null || positionPiecesMap == null) {
            return null;
        }

        CasePosition position = null;

        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : positionPiecesMap.entrySet()) {
            if (pieces.equals(casePositionPiecesEntry.getValue())) {
                position = casePositionPiecesEntry.getKey();
                break;
            }
        }

        return position;
    }


    /**
     * Gets the position of a piece
     *
     * @param pieces
     * @param positionPiecesMap
     * @return
     */
    public static List<CasePosition> getPiecesPosition(Pieces pieces, Map<CasePosition, Pieces> positionPiecesMap) {
        List<CasePosition> positions = new ArrayList<>();

        if (pieces == null || positionPiecesMap == null) {
            return positions;
        }

        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : positionPiecesMap.entrySet()) {
            if (pieces.equals(casePositionPiecesEntry.getValue())) {
                positions.add(casePositionPiecesEntry.getKey());
            }
        }

        return positions;
    }
}
