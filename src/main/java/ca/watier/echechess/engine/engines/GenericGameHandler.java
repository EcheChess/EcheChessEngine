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

package ca.watier.echechess.engine.engines;

import ca.watier.echechess.common.enums.*;
import ca.watier.echechess.common.pojos.MoveHistory;
import ca.watier.echechess.common.responses.GameScoreResponse;
import ca.watier.echechess.common.sessions.Player;
import ca.watier.echechess.common.utils.*;
import ca.watier.echechess.engine.abstracts.GameBoard;
import ca.watier.echechess.engine.constraints.PawnMoveConstraint;
import ca.watier.echechess.engine.exceptions.MoveNotAllowedException;
import ca.watier.echechess.engine.factories.GameConstraintFactory;
import ca.watier.echechess.engine.interfaces.GameConstraintHandler;
import ca.watier.echechess.engine.utils.GameUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

import static ca.watier.echechess.common.enums.KingStatus.OK;
import static ca.watier.echechess.common.enums.KingStatus.STALEMATE;
import static ca.watier.echechess.common.enums.Side.*;


/**
 * Created by yannick on 5/5/2017.
 */
public class GenericGameHandler extends GameBoard {
    private static final long serialVersionUID = 1139291295474732218L;
    private final GameConstraintHandler gameConstraintHandler;
    private final Set<SpecialGameRules> specialGameRules = new HashSet<>();
    private Player playerWhite;
    private Player playerBlack;
    private String uuid;
    private boolean allowOtherToJoin = false;
    private boolean allowObservers = false;
    private List<Player> observerList = new ArrayList<>();
    private GameType gameType;

    public GenericGameHandler(GameConstraintHandler gameConstraintHandler) {
        super();
        this.gameConstraintHandler = gameConstraintHandler;
    }

    public GenericGameHandler() {
        super();
        this.gameConstraintHandler = GameConstraintFactory.getDefaultGameConstraint();
    }

    /**
     * Move a piece to a selected position
     *
     * @param from
     * @param to
     * @param playerSide
     * @return
     */
    public MoveType movePiece(CasePosition from, CasePosition to, Side playerSide) {
        if (ObjectUtils.hasNull(from, to, playerSide)) {
            return MoveType.MOVE_NOT_ALLOWED;
        }

        MoveHistory moveHistory = new MoveHistory(from, to, playerSide);
        Pieces piecesToBeforeAction = getPiece(to);

        MoveType moveType;
        try {
            moveType = movePiece(from, to, playerSide, moveHistory);
        } catch (MoveNotAllowedException e) {
            moveType = MoveType.MOVE_NOT_ALLOWED;
        }
        moveHistory.setMoveType(moveType);
        addHistory(moveHistory);

        if (MoveType.PAWN_PROMOTION.equals(moveType)) {//Check if the promotion kill a piece in the process
            if (piecesToBeforeAction != null) {
                MoveHistory moveHistoryCapture = new MoveHistory(from, to, playerSide);
                moveHistoryCapture.setMoveType(MoveType.CAPTURE);
                addHistory(moveHistoryCapture);
            }
        }
        return moveType;
    }

    private MoveType movePiece(CasePosition from, CasePosition to, Side playerSide, MoveHistory moveHistory) throws MoveNotAllowedException {
        if (ObjectUtils.hasNull(from, to, playerSide, moveHistory)) {
            throw new MoveNotAllowedException();
        }

        Pieces piecesFrom = getPiece(from);

        if (piecesFrom == null || !isPlayerTurn(playerSide) || !piecesFrom.getSide().equals(playerSide)) {
            throw new MoveNotAllowedException();
        }

        Pieces piecesTo = getPiece(to);
        Side otherPlayerSide = getOtherPlayerSide(playerSide);

        if (Pieces.isPawn(piecesFrom) && Ranks.EIGHT.equals(Ranks.getRank(to, playerSide))) {
            addPawnPromotion(from, to, playerSide);
            setGamePaused(true);
            changeAllowedMoveSide();

            return MoveType.PAWN_PROMOTION;
        }

        MoveType moveType = gameConstraintHandler.getMoveType(from, to, this);
        KingStatus evaluatedCurrentKingStatus = OK;
        boolean isEatingPiece = piecesTo != null;

        if (MoveType.NORMAL_MOVE.equals(moveType) || MoveType.PAWN_HOP.equals(moveType)) {
            if (!isPieceMovableTo(from, to, playerSide)) {
                throw new MoveNotAllowedException();
            }
            cloneCurrentState();
            movePieceTo(from, to, piecesFrom);
            evaluatedCurrentKingStatus = getKingStatus(playerSide);

            if (KingStatus.isCheckOrCheckMate(evaluatedCurrentKingStatus)) { //Cannot move, revert
                restoreLastState();
                throw new MoveNotAllowedException();
            } else {
                changeAllowedMoveSide();

                if (isEatingPiece) { //Count the point for the piece
                    updatePointsForSide(playerSide, piecesTo.getPoint());
                    moveType = MoveType.CAPTURE;
                }
                removeLastState();
            }
        } else if (MoveType.CASTLING.equals(moveType)) {
            handleCastlingWhenMove(from, to, playerSide, piecesFrom, piecesTo, isEatingPiece);
        } else if (MoveType.EN_PASSANT.equals(moveType)) {
            evaluatedCurrentKingStatus = handleEnPassantWhenMove(from, to, playerSide, otherPlayerSide, piecesFrom);
        }

        KingStatus evaluatedOtherKingStatusAfterMove = getKingStatus(otherPlayerSide);

        moveHistory.setCurrentKingStatus(evaluatedCurrentKingStatus);
        moveHistory.setOtherKingStatus(evaluatedOtherKingStatusAfterMove);

        setKingStatusBySide(evaluatedCurrentKingStatus, playerSide);
        setKingStatusBySide(evaluatedOtherKingStatusAfterMove, Side.getOtherPlayerSide(playerSide));

        return moveType;
    }

    private KingStatus handleEnPassantWhenMove(CasePosition from, CasePosition to, Side playerSide, Side otherPlayerSide, Pieces piecesFrom) throws MoveNotAllowedException {
        cloneCurrentState();

        movePieceTo(from, to, piecesFrom);
        CasePosition enemyPawnPosition = MathUtils.getNearestPositionFromDirection(to, otherPlayerSide.equals(BLACK) ? Direction.SOUTH : Direction.NORTH);
        Pieces enemyPawnToEat = getPiece(enemyPawnPosition);
        removePieceFromBoard(enemyPawnPosition);

        KingStatus kingStatus = getKingStatus(playerSide);
        if (KingStatus.isCheckOrCheckMate(kingStatus)) {
            restoreLastState();
            throw new MoveNotAllowedException();
        }

        updatePointsForSide(playerSide, enemyPawnToEat.getPoint());
        changeAllowedMoveSide();
        removeLastState();
        return kingStatus;
    }

    public List<CasePosition> getPositionKingCanMove(Side playerSide) {
        if (playerSide == null) {
            return null;
        }

        CasePosition kingPosition = GameUtils.getSinglePiecePosition(Pieces.getKingBySide(playerSide), getPiecesLocation());

        List<CasePosition> values = new ArrayList<>();
        List<CasePosition> caseAround = MathUtils.getAllPositionsAroundPosition(kingPosition);
        for (CasePosition position : caseAround) {  //Check if the king can kill something to save himself
            if (isPieceMovableTo(kingPosition, position, playerSide) && !isKingCheckAtPosition(position, playerSide)) {
                values.add(position);
            }
        }

        //Add the position, if the castling is authorized on the rook
        Pieces rook = WHITE.equals(playerSide) ? Pieces.W_ROOK : Pieces.B_ROOK;
        for (CasePosition rookPosition : GameUtils.getPiecesPosition(rook, getPiecesLocation())) {
            if (MoveType.CASTLING.equals(gameConstraintHandler.getMoveType(kingPosition, rookPosition, this))) {
                values.add(rookPosition);
            }
        }

        return values;
    }


    /**
     * If queen side, move rook to D1 / D8 and king to C1 / C8
     * Otherwise, move rook to F1 / F8 and king to G1 / G8
     *
     * @param from
     * @param to
     * @param playerSide
     * @param piecesFrom
     * @param piecesTo
     * @param isEatingPiece
     */
    private void handleCastlingWhenMove(CasePosition from, CasePosition to, Side playerSide, Pieces piecesFrom, Pieces piecesTo, boolean isEatingPiece) {
        CastlingPositionHelper castlingPositionHelper = new CastlingPositionHelper(from, to, playerSide).invoke();
        CasePosition kingPosition = castlingPositionHelper.getKingPosition();
        CasePosition rookPosition = castlingPositionHelper.getRookPosition();

        if (castlingPositionHelper.isQueenSide()) {
            switch (playerSide) {
                case BLACK:
                    setBlackQueenCastlingAvailable(false);
                    break;
                case WHITE:
                    setWhiteQueenCastlingAvailable(false);
                    break;
            }
        } else {
            switch (playerSide) {
                case BLACK:
                    setBlackKingCastlingAvailable(false);
                    break;
                case WHITE:
                    setWhiteKingCastlingAvailable(false);
                    break;
            }
        }

        movePieceTo(from, kingPosition, piecesFrom);

        if (isEatingPiece) {
            movePieceTo(to, rookPosition, piecesTo);
        }
        changeAllowedMoveSide();
    }

    private boolean isPlayerTurn(Side sideFrom) {
        if (sideFrom == null) {
            return false;
        }

        return isGameHaveRule(SpecialGameRules.NO_PLAYER_TURN) || getCurrentAllowedMoveSide().equals(sideFrom);
    }


    /**
     * 1) Check if the king can move / kill to escape.
     * 2) If not, try to liberate a case around the king, by killing / blocking the piece with an ally piece (if only one that can hit this target).
     * 3) If not, the king is checkmate.
     *
     * @param playerSide
     * @return
     */
    public KingStatus getKingStatus(Side playerSide) {
        if (playerSide == null) {
            return null;
        }

        KingStatus kingStatus = OK;

        Pieces kingPiece = Pieces.getKingBySide(playerSide);
        CasePosition kingPosition = GameUtils.getSinglePiecePosition(kingPiece, getPiecesLocation());

        if (isGameHaveRule(SpecialGameRules.NO_CHECK_OR_CHECKMATE)) {
            return kingStatus;
        }

        MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> piecesThatCanHitOriginalPosition = getPiecesThatCanHitPosition(getOtherPlayerSide(playerSide), kingPosition);

        if (!piecesThatCanHitOriginalPosition.isEmpty()) { //One or more piece can hit the king
            kingStatus = getKingStatusWhenPiecesCanHitKing(playerSide, kingPosition, piecesThatCanHitOriginalPosition);
        } else if (getPositionKingCanMove(playerSide).isEmpty()) { //Check if not a stalemate
            kingStatus = isStalemate(playerSide, kingPiece, kingPosition) ? STALEMATE : kingStatus;
        }

        return kingStatus;
    }


    private KingStatus getKingStatusWhenPiecesCanHitKing(Side playerSide, CasePosition kingPosition, MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> piecesThatCanHitOriginalPosition) {
        KingStatus kingStatus = KingStatus.CHECKMATE;

        //Try to move the king
        if (!getPositionKingCanMove(playerSide).isEmpty()) {
            return KingStatus.CHECK;
        }

        if (piecesThatCanHitOriginalPosition.size() == 1) {
            //If not able to move, try to kill the enemy piece with an other piece
            if (!isStillCheckAfterKillingTheEnemy(playerSide, kingPosition, piecesThatCanHitOriginalPosition)) {
                return KingStatus.CHECK;
            }

            //Try to block the path of the enemy with one of the pieces
            if (oneOrMorePieceCanBlock(piecesThatCanHitOriginalPosition, kingPosition, playerSide)) {
                return KingStatus.CHECK;
            }
        }

        return kingStatus;
    }

    private boolean oneOrMorePieceCanBlock(MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> piecesThatCanHitOriginalPosition, CasePosition kingPosition, Side playerSide) {
        List<Pair<CasePosition, Pieces>> pairs = piecesThatCanHitOriginalPosition.get(kingPosition);
        if (pairs.size() == 1) { //We can only block one piece, if more, checkmate
            Pair<CasePosition, Pieces> casePositionPiecesPair = pairs.get(0);
            Pieces enemyPiece = casePositionPiecesPair.getSecondValue();
            CasePosition enemyPosition = casePositionPiecesPair.getFirstValue();

            if (Pieces.isKnight(enemyPiece)) { //We cannot block a knight
                return false;
            }

            for (CasePosition to : MathUtils.getPositionsBetweenTwoPosition(enemyPosition, kingPosition)) { //For each position between the king and the enemy, we try to block it
                for (CasePosition from : getPiecesLocation(playerSide).keySet()) { //Try to find if one of our piece can block the target
                    if (Pieces.isKing(getPiece(from))) {
                        continue;
                    }

                    if (isPieceMovableTo(from, to, playerSide) && !isKingCheckAfterMove(from, to, playerSide)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isStillCheckAfterKillingTheEnemy(Side playerSide, CasePosition kingPosition, MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> piecesThatCanHitOriginalPosition) {
        Pair<CasePosition, Pieces> enemyPiecesPair = piecesThatCanHitOriginalPosition.get(kingPosition).get(0);
        CasePosition to = enemyPiecesPair.getFirstValue();

        boolean isCheck = true;
        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : getPiecesLocation(playerSide).entrySet()) {
            CasePosition from = casePositionPiecesEntry.getKey();

            if (Pieces.isKing(casePositionPiecesEntry.getValue())) {
                continue;
            }

            Pieces currentPiece = getPiece(from);
            Pieces enemyPiece = getPiece(to);

            int fromColPos = from.getColPos();
            int fromRow = from.getRow();
            int toRow = to.getRow();
            int toColPos = to.getColPos();
            boolean isColumnNearEachOther = (Math.abs(fromColPos - toColPos) == 1);

            //This is the only case, where the piece is not directly the end target (En passant).
            if (Pieces.isPawn(currentPiece) &&
                    Pieces.isPawn(enemyPiece) &&
                    isColumnNearEachOther &&
                    fromRow == toRow &&
                    !enemyPiece.getSide().equals(currentPiece.getSide()) &&
                    Ranks.FIVE.equals(Ranks.getRank(to, playerSide))) {

                CasePosition positionByRankAndColumn = getPositionByRankAndColumn(Ranks.SIX, to.getCol(), playerSide);

                if (!PawnMoveConstraint.isEnPassant(to, positionByRankAndColumn, this, playerSide)) {
                    continue;
                }

                CasePosition enPassantEnemyPawnPosition =
                        PawnMoveConstraint.getEnPassantPositionFromEnemyPawn(to, Side.getOtherPlayerSide(playerSide));

                isCheck &= isKingCheckAfterMove(from, enPassantEnemyPawnPosition, playerSide);
            } else if (isPieceMovableTo(from, to, playerSide) && !isKingCheckAfterMove(from, to, playerSide)) {
                isCheck &= isKingCheckAfterMove(from, to, playerSide); //One or more piece is able to kill the enemy
            }
        }

        return isCheck;
    }

    private void updatePointsForSide(Side side, byte point) {
        if (side == null) {
            return;
        }

        switch (side) {
            case BLACK:
                addBlackPlayerPoint(point);
                break;
            case WHITE:
                addWhitePlayerPoint(point);
                break;
            default:
                break;
        }
    }

    private boolean isGameHaveRule(SpecialGameRules rule) {
        return specialGameRules.contains(rule);
    }

    public GameScoreResponse getGameScore() {
        return new GameScoreResponse(getWhitePlayerPoint(), getBlackPlayerPoint());
    }

    public boolean isKingCheckAtPosition(CasePosition currentPosition, Side playerSide) {
        if (ObjectUtils.hasNull(currentPosition, playerSide) || isGameHaveRule(SpecialGameRules.NO_CHECK_OR_CHECKMATE)) {
            return false;
        }

        return !getPiecesThatCanHitPosition(getOtherPlayerSide(playerSide), currentPosition).isEmpty();
    }

    /**
     * Gets the pieces / CasePosition based on a side
     *
     * @param side
     * @return
     */
    public final Map<CasePosition, Pieces> getPiecesLocation(Side side) {
        Map<CasePosition, Pieces> values = new EnumMap<>(CasePosition.class);

        if (side == null) {
            return values;
        }

        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : getPiecesLocation().entrySet()) {
            CasePosition key = casePositionPiecesEntry.getKey();
            Pieces value = casePositionPiecesEntry.getValue();

            if (side.equals(value.getSide())) {
                values.put(key, value);
            }
        }

        return values;
    }

    private boolean isStalemate(Side playerSide, Pieces kingPiece, CasePosition kingPosition) {
        boolean isStalemate = true;

        //Check if we can move the pieces around the king (same color)
        for (CasePosition moveFrom : MathUtils.getAllPositionsAroundPosition(kingPosition)) {
            Pieces currentPiece = getPiece(moveFrom);

            if (currentPiece != null && !Pieces.isKing(currentPiece) && Pieces.isSameSide(currentPiece, kingPiece)) {
                for (CasePosition moveTo : getAllAvailableMoves(moveFrom, playerSide)) {
                    if (isPieceMovableTo(moveFrom, moveTo, playerSide) && !isKingCheckAfterMove(moveFrom, moveTo, playerSide)) {
                        isStalemate = false;
                        break;
                    }
                }
            }
        }
        return isStalemate;
    }

    private boolean isKingCheckAfterMove(CasePosition from, CasePosition to, Side playerSide) {
        if (ObjectUtils.hasNull(from, to, playerSide)) {
            return false;
        }

        boolean isKingCheck;
        MoveType moveType = gameConstraintHandler.getMoveType(from, to, this);

        cloneCurrentState();
        if (MoveType.EN_PASSANT.equals(moveType)) {
            isKingCheck = isKingCheckWithEnPassantMove(from, to, playerSide);
        } else {
            isKingCheck = isKingCheckWithOtherMove(from, to, playerSide, moveType);
        }
        restoreLastState();

        return isKingCheck;
    }

    /**
     * Check if the piece can be moved to the selected position
     *
     * @param from
     * @param to
     * @param playerSide
     * @return
     */
    public final boolean isPieceMovableTo(CasePosition from, CasePosition to, Side playerSide) {
        if (ObjectUtils.hasNull(from, to, playerSide)) {
            return false;
        }

        return gameConstraintHandler.isPieceMovableTo(from, to, playerSide, this, MoveMode.NORMAL_OR_ATTACK_MOVE);
    }

    private boolean isKingCheckWithEnPassantMove(CasePosition from, CasePosition to, Side playerSide) {
        CasePosition enPassantEnemyPawnPosition = PawnMoveConstraint.getEnemyPawnPositionFromEnPassant(to, getOtherPlayerSide(playerSide));
        Pieces currentPiece = getPiece(from);

        //Copy the values
        Boolean isPawnUsedSpecialMove = isPawnUsedSpecialMove(from);
        Integer pieceTurn = getPieceTurn(from);

        removePieceFromBoard(enPassantEnemyPawnPosition); //Remove the enemy pawn
        removePieceFromBoard(from); //Remove the from pawn
        setPiecePositionWithoutMoveState(currentPiece, to);

        //To check if the pawn used the +2 move
        Map<CasePosition, Boolean> isPawnUsedSpecialMoveMap = new EnumMap<>(getIsPawnUsedSpecialMoveMap());
        isPawnUsedSpecialMoveMap.put(to, isPawnUsedSpecialMove);

        //To check if the piece moved in the current game
        Map<CasePosition, Boolean> isPiecesMovedMap = new EnumMap<>(getIsPiecesMovedMap());
        isPiecesMovedMap.put(to, true);

        //To get the number of turns since last move
        Map<CasePosition, Integer> turnNumberPieceMap = new EnumMap<>(getTurnNumberPieceMap());
        turnNumberPieceMap.put(to, pieceTurn + 1);

        //Set the new maps
        setPiecesGameState(isPawnUsedSpecialMoveMap, turnNumberPieceMap, isPiecesMovedMap);

        MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> piecesThatCanHitOriginalPosition = getPiecesThatCanHitPosition(getOtherPlayerSide(playerSide), GameUtils.getSinglePiecePosition(Pieces.getKingBySide(playerSide), getPiecesLocation()));
        return !piecesThatCanHitOriginalPosition.isEmpty();
    }

    private boolean isKingCheckWithOtherMove(CasePosition from, CasePosition to, Side playerSide, MoveType moveType) {
        boolean isKingCheck;
        Pieces currentPiece = getPiece(from);
        Pieces pieceEaten = getPiece(to);

        removePieceFromBoard(from);
        setPiecePositionWithoutMoveState(currentPiece, to);

        //To check if the pawn used the +2 move
        Map<CasePosition, Boolean> isPawnUsedSpecialMoveMap = new EnumMap<>(getIsPawnUsedSpecialMoveMap());

        //To check if the piece moved in the current game
        Map<CasePosition, Boolean> isPiecesMovedMap = new EnumMap<>(getIsPiecesMovedMap());

        //To get the number of turns since last move
        Map<CasePosition, Integer> turnNumberPieceMap = new EnumMap<>(getTurnNumberPieceMap());

        //Set the new values in the maps
        if (MoveType.PAWN_HOP.equals(moveType)) {
            isPawnUsedSpecialMoveMap.remove(from);
            isPawnUsedSpecialMoveMap.put(to, true); //the pawn is now moved
        }

        isPiecesMovedMap.remove(from);
        isPiecesMovedMap.put(to, true);

        turnNumberPieceMap.remove(from);
        turnNumberPieceMap.put(to, getNbTotalMove());

        //Set the new maps
        setPiecesGameState(isPawnUsedSpecialMoveMap, turnNumberPieceMap, isPiecesMovedMap);

        MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> piecesThatCanHitOriginalPosition = getPiecesThatCanHitPosition(getOtherPlayerSide(playerSide), GameUtils.getSinglePiecePosition(Pieces.getKingBySide(playerSide), getPiecesLocation()));

        isKingCheck = !piecesThatCanHitOriginalPosition.isEmpty();

        //Reset the piece(s)
        setPiecePositionWithoutMoveState(currentPiece, from);
        if (pieceEaten != null) {
            setPiecePositionWithoutMoveState(pieceEaten, to);
        } else {
            removePieceFromBoard(to);
        }
        return isKingCheck;
    }


    /**
     * Return a List containing all the moves for the selected piece
     *
     * @param from
     * @param playerSide
     * @return
     */
    public List<CasePosition> getAllAvailableMoves(CasePosition from, Side playerSide) {
        List<CasePosition> positions = new ArrayList<>();

        if (ObjectUtils.hasNull(from, playerSide)) {
            return positions;
        }

        Pieces pieces = getPiece(from);

        if (pieces == null || !pieces.getSide().equals(playerSide)) {
            return positions;
        }

        for (CasePosition position : CasePosition.values()) {

            boolean isSpecialMove = MoveType.isSpecialMove(gameConstraintHandler.getMoveType(from, position, this));

            if (isSpecialMove || !from.equals(position) && isPieceMovableTo(from, position, playerSide)) {
                positions.add(position);
            }
        }

        return positions;
    }

    /**
     * Return a list of @{@link Pieces} that can moves to the selected position
     *
     * @param to
     * @param sideToKeep
     * @return
     */
    public List<Pair<CasePosition, Pieces>> getAllPiecesThatCanMoveTo(CasePosition to, Side sideToKeep) {
        List<Pair<CasePosition, Pieces>> values = new ArrayList<>();

        if (ObjectUtils.hasNull(to, sideToKeep)) {
            return values;
        }

        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : getPiecesLocation().entrySet()) {
            CasePosition from = casePositionPiecesEntry.getKey();
            Pieces piecesFrom = casePositionPiecesEntry.getValue();

            if (!sideToKeep.equals(piecesFrom.getSide())) {
                continue;
            }

            MoveType moveType = gameConstraintHandler.getMoveType(from, to, this);
            boolean isEnPassant = MoveType.EN_PASSANT.equals(moveType);

            if (!isKingCheckAfterMove(from, to, sideToKeep) && isPieceMovableTo(from, to, sideToKeep) || isEnPassant) {
                values.add(new Pair<>(from, piecesFrom));
            }
        }

        return values;
    }

    /**
     * Gets the pieces that can hit the target, the {@link CasePosition} inside the {@link Pair} is the starting position of the attacking {@link Pieces}
     *
     * @param positions
     * @param sideToKeep
     * @return
     */
    public MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> getPiecesThatCanHitPosition(Side sideToKeep, CasePosition... positions) {
        MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> values = new MultiArrayMap<>();

        if (ArrayUtils.isEmpty(positions)) {
            return values;
        }

        for (CasePosition position : positions) {
            for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : getPiecesLocation().entrySet()) {
                CasePosition key = casePositionPiecesEntry.getKey();
                Pieces value = casePositionPiecesEntry.getValue();

                Side pieceSide = value.getSide();
                if (!pieceSide.equals(sideToKeep)) {
                    continue;
                }

                if (gameConstraintHandler.isPieceMovableTo(key, position, pieceSide, this, MoveMode.IS_KING_CHECK_MODE)) {
                    values.put(position, new Pair<>(key, value));
                }
            }
        }

        return values;
    }

    public final boolean setPlayerToSide(Player player, Side side) {
        if (ObjectUtils.hasNull(player, side)) {
            return false;
        }

        boolean value;

        switch (side) {
            case BLACK: {
                removePlayerFromWhite(player);
                value = changePlayerToBlack(player);
                observerList.remove(player);
                break;
            }
            case WHITE: {
                removePlayerFromBlack(player);
                value = changePlayerToWhite(player);
                observerList.remove(player);
                break;
            }
            default: {
                removePlayerFromWhite(player);
                removePlayerFromBlack(player);
                observerList.add(player);
                value = true;
                break;
            }
        }

        return value;
    }

    private void removePlayerFromWhite(Player player) {
        if (player != null && player.equals(playerWhite)) {
            playerWhite = null;
        }
    }

    private boolean changePlayerToBlack(Player player) {
        if (playerBlack == null) {
            playerBlack = player;
            return true;
        }

        return false;
    }

    private void removePlayerFromBlack(Player player) {
        if (playerBlack == player) {
            playerBlack = null;
        }
    }

    private boolean changePlayerToWhite(Player player) {
        if (playerWhite == null) {
            playerWhite = player;
            return true;
        }

        return false;
    }

    /**
     * Get the side of the player, null if not available
     *
     * @param player
     * @return
     */
    public final Side getPlayerSide(Player player) {
        Side side = null;

        if (player == null) {
            return null;
        }

        if (player.equals(playerWhite)) {
            side = WHITE;
        } else if (player.equals(playerBlack)) {
            side = BLACK;
        } else if (observerList.contains(player)) {
            side = Side.OBSERVER;
        }

        return side;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public final boolean hasPlayer(Player player) {
        if (player == null) {
            return false;
        }

        return observerList.contains(player) || player.equals(playerBlack) || player.equals(playerWhite);
    }

    public Player getPlayerWhite() {
        return playerWhite;
    }

    public Player getPlayerBlack() {
        return playerBlack;
    }

    public boolean isAllowOtherToJoin() {
        return allowOtherToJoin;
    }

    public void setAllowOtherToJoin(boolean allowOtherToJoin) {
        this.allowOtherToJoin = allowOtherToJoin;
    }

    public boolean isAllowObservers() {
        return allowObservers;
    }

    public void setAllowObservers(boolean allowObservers) {
        this.allowObservers = allowObservers;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void addSpecialRule(SpecialGameRules... rules) {
        if (ArrayUtils.isEmpty(rules)) {
            return;
        }

        specialGameRules.addAll(Arrays.asList(rules));
    }

    public void removeSpecialRule(SpecialGameRules... rules) {
        if (ArrayUtils.isEmpty(rules)) {
            return;
        }

        specialGameRules.removeAll(Arrays.asList(rules));
    }

    public List<Player> getObserverList() {
        return Collections.unmodifiableList(observerList);
    }

    public Set<SpecialGameRules> getSpecialGameRules() {
        return Collections.unmodifiableSet(specialGameRules);
    }

    public boolean isGameDone() {
        return KingStatus.CHECKMATE.equals(getEvaluatedKingStatusBySide(BLACK)) ||
                KingStatus.CHECKMATE.equals(getEvaluatedKingStatusBySide(WHITE)) ||
                isGameDraw();
    }
}
