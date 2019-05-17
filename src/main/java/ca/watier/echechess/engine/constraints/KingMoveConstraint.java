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
import ca.watier.echechess.common.utils.CastlingPositionHelper;
import ca.watier.echechess.common.utils.MathUtils;
import ca.watier.echechess.common.utils.ObjectUtils;
import ca.watier.echechess.engine.abstracts.GameBoardData;
import ca.watier.echechess.engine.engines.GenericGameHandler;
import ca.watier.echechess.engine.interfaces.KingHandler;
import ca.watier.echechess.engine.interfaces.MoveConstraint;
import ca.watier.echechess.engine.utils.GameUtils;

import java.util.List;
import java.util.Map;

import static ca.watier.echechess.common.interfaces.BaseUtils.getSafeBoolean;

/**
 * Created by yannick on 4/23/2017.
 */
public class KingMoveConstraint implements MoveConstraint {

    @Override
    public boolean isMoveValid(CasePosition from, CasePosition to, GenericGameHandler gameHandler, MoveMode moveMode) {
        Pieces hittingPiece = gameHandler.getPiece(to);
        Pieces pieceFrom = gameHandler.getPiece(from);
        Side sideFrom = pieceFrom.getSide();

        boolean checkHit = true;
        if (MoveMode.NORMAL_OR_ATTACK_MOVE.equals(moveMode)) {
            checkHit = (hittingPiece == null || !Pieces.isSameSide(hittingPiece, sideFrom) && !Pieces.isKing(hittingPiece));
        }

        return (BaseUtils.getSafeInteger(MathUtils.getDistanceBetweenPositionsWithCommonDirection(from, to)) == 1) && checkHit;
    }


    /*
       --------- Castling ---------
       URL: https://en.wikipedia.org/wiki/Castling
       Castling is permissible if and only if all of the following conditions hold (Schiller 2003:19):
           The king and the chosen rook are on the player's first rank.
           Neither the king nor the chosen rook has previously moved.
           There are no pieces between the king and the chosen rook.
           The king is not currently in check.
           The king does not pass through a square that is attacked by an enemy piece.
           The king does not end up in check. (True of any legal move.)
    */
    @Override
    public MoveType getMoveType(CasePosition from, CasePosition to, GenericGameHandler gameHandler) {
        if (ObjectUtils.hasNull(from, to, gameHandler)) {
            return MoveType.MOVE_NOT_ALLOWED;
        }

        Pieces pieceFrom = gameHandler.getPiece(from);
        Side sideFrom = pieceFrom.getSide();
        Pieces pieceTo = gameHandler.getPiece(to);
        Map<CasePosition, Pieces> piecesLocation = gameHandler.getPiecesLocation();

        KingHandler kingHandler = gameHandler.getKingHandler();

        if (pieceTo == null) {
            return MoveType.NORMAL_MOVE;
        } else if (isCastlingPieces(pieceFrom, pieceTo)) {
            return handleCastling(from, to, sideFrom, piecesLocation, gameHandler, kingHandler);
        } else {
            return MoveType.NORMAL_MOVE;
        }
    }


    private MoveType handleCastling(CasePosition from, CasePosition to, Side sideFrom, Map<CasePosition, Pieces> piecesLocation, GenericGameHandler gameHandler, KingHandler kingHandler) {

        CastlingPositionHelper castlingPositionHelper = new CastlingPositionHelper(from, to, sideFrom).invoke();

        if (isCastlingAvailable(gameHandler, castlingPositionHelper, sideFrom)) {
            return MoveType.MOVE_NOT_ALLOWED;
        } else if (isCastlingValid(gameHandler, piecesLocation, castlingPositionHelper, kingHandler, from, sideFrom, to)) {
            return MoveType.CASTLING;
        } else {
            return MoveType.NORMAL_MOVE;
        }
    }

    private boolean isCastlingAvailable(GameBoardData gameHandler, CastlingPositionHelper castlingPositionHelper, Side sideFrom) {
        boolean queenSideCastling = castlingPositionHelper.isQueenSide();
        boolean kingSideCastling = !queenSideCastling;

        boolean isQueenSideAvail = false;
        boolean isKingSideAvail = false;

        switch (sideFrom) {
            case BLACK:
                isKingSideAvail = gameHandler.isBlackKingCastlingAvailable();
                isQueenSideAvail = gameHandler.isBlackQueenCastlingAvailable();
                break;
            case WHITE:
                isKingSideAvail = gameHandler.isWhiteKingCastlingAvailable();
                isQueenSideAvail = gameHandler.isWhiteQueenCastlingAvailable();
                break;
        }

        return (queenSideCastling && !isQueenSideAvail) || (kingSideCastling && !isKingSideAvail);
    }

    private boolean isCastlingValid(GenericGameHandler gameHandler,
                                    Map<CasePosition, Pieces> piecesLocation,
                                    CastlingPositionHelper castlingPositionHelper,
                                    KingHandler kingHandler,
                                    CasePosition from,
                                    Side sideFrom,
                                    CasePosition to) {


        List<CasePosition> piecesBetweenKingAndRook = GameUtils.getPiecesBetweenPosition(from, to, piecesLocation);
        CasePosition kingPosition = castlingPositionHelper.getKingPosition();
        CasePosition positionWhereKingPass = castlingPositionHelper.getRookPosition();

        boolean isPieceAreNotMoved = !getSafeBoolean(gameHandler.isPieceMoved(from)) && !getSafeBoolean(gameHandler.isPieceMoved(to));
        boolean isNoPieceBetweenKingAndRook = piecesBetweenKingAndRook.isEmpty();
        boolean isNoPieceAttackingBetweenKingAndRook = gameHandler.getPiecesThatCanHitPosition(Side.getOtherPlayerSide(sideFrom), positionWhereKingPass).isEmpty();
        boolean isKingNotCheckAtCurrentLocation = !kingHandler.isKingCheckAtPosition(from, sideFrom, gameHandler);
        boolean kingNotCheckAtEndPosition = !kingHandler.isKingCheckAtPosition(kingPosition, sideFrom, gameHandler);

        return isPieceAreNotMoved && isNoPieceBetweenKingAndRook &&
                isNoPieceAttackingBetweenKingAndRook && isKingNotCheckAtCurrentLocation &&
                kingNotCheckAtEndPosition;
    }

    private boolean isCastlingPieces(Pieces pieceFrom, Pieces pieceTo) {
        return Pieces.isSameSide(pieceFrom, pieceTo) && Pieces.isKing(pieceFrom) && Pieces.isRook(pieceTo);
    }
}
