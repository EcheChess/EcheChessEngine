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

package ca.watier.echechessengine.engines;

import ca.watier.echechessengine.abstracts.GameBoard;
import ca.watier.echechessengine.constraints.PawnMoveConstraint;
import ca.watier.echechessengine.game.GameConstraints;
import ca.watier.echechessengine.utils.GameUtils;
import ca.watier.echesscommon.enums.*;
import ca.watier.echesscommon.interfaces.WebSocketService;
import ca.watier.echesscommon.pojos.MoveHistory;
import ca.watier.echesscommon.responses.GameScoreResponse;
import ca.watier.echesscommon.sessions.Player;
import ca.watier.echesscommon.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static ca.watier.echesscommon.enums.ChessEventMessage.*;
import static ca.watier.echesscommon.enums.ChessEventMessage.PLAYER_TURN;
import static ca.watier.echesscommon.enums.KingStatus.OK;
import static ca.watier.echesscommon.enums.KingStatus.STALEMATE;
import static ca.watier.echesscommon.enums.Side.*;
import static ca.watier.echesscommon.utils.Constants.*;

/**
 * Created by yannick on 5/5/2017.
 */
public class GenericGameHandler extends GameBoard {
    private final GameConstraints GAME_CONSTRAINTS;
    private final WebSocketService WEB_SOCKET_SERVICE;
    private final Set<SpecialGameRules> SPECIAL_GAME_RULES;
    protected String uuid;
    protected Player playerWhite;
    protected Player playerBlack;
    private boolean allowOtherToJoin = false;
    private boolean allowObservers = false;
    private Side currentAllowedMoveSide = WHITE;
    private List<Player> observerList;
    private List<MoveHistory> moveHistoryList;
    private short blackPlayerPoint = 0;
    private short whitePlayerPoint = 0;
    private GameType gameType;

    public GenericGameHandler(GameConstraints gameConstraints, WebSocketService webSocketService) {
        SPECIAL_GAME_RULES = new HashSet<>();
        observerList = new ArrayList<>();
        moveHistoryList = new ArrayList<>();
        this.GAME_CONSTRAINTS = gameConstraints;
        this.WEB_SOCKET_SERVICE = webSocketService;
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
        MoveHistory moveHistory = new MoveHistory(from, to, playerSide);
        Pieces piecesToBeforeAction = getPiece(to);

        MoveType moveType = movePiece(from, to, playerSide, moveHistory);
        moveHistory.setMoveType(moveType);
        moveHistoryList.add(moveHistory);

        if (MoveType.PAWN_PROMOTION.equals(moveType)) {//Check if the promotion kill a piece in the process
            if (piecesToBeforeAction != null) {
                MoveHistory moveHistoryCapture = new MoveHistory(from, to, playerSide);
                moveHistoryCapture.setMoveType(MoveType.CAPTURE);
                moveHistoryList.add(moveHistoryCapture);
            }
        }
        return moveType;
    }

    private MoveType movePiece(@NotNull CasePosition from, @NotNull CasePosition to, @NotNull Side playerSide, @NotNull MoveHistory moveHistory) {
        Assert.assertNotNull(from, to, playerSide);

        Side otherPlayerSide = Side.getOtherPlayerSide(playerSide);
        Pieces piecesFrom = getPiece(from);
        Pieces piecesTo = getPiece(to);

        if (piecesFrom == null || !isPlayerTurn(playerSide) || !piecesFrom.getSide().equals(playerSide)) {
            return MoveType.MOVE_NOT_ALLOWED;
        } else if (Pieces.isPawn(piecesFrom) && Ranks.EIGHT.equals(Ranks.getRank(to, playerSide))) {
            addPawnPromotion(from, to, playerSide);
            setGamePaused(true);
            changeAllowedMoveSide();

            sendPawnPromotionMessage(to, playerSide);
            sendMovedMessages(from, to, playerSide);
            return MoveType.PAWN_PROMOTION;
        }

        MoveType moveType = GAME_CONSTRAINTS.getMoveType(from, to, this);
        KingStatus currentKingStatus = KingStatus.OK;
        boolean isEatingPiece = piecesTo != null;


        if (MoveType.NORMAL_MOVE.equals(moveType) || MoveType.PAWN_HOP.equals(moveType)) {
            if (!isPieceMovableTo(from, to, playerSide)) {
                return MoveType.MOVE_NOT_ALLOWED;
            }

            movePieceTo(from, to, piecesFrom);
            currentKingStatus = getKingStatus(playerSide, true);

            if (KingStatus.isCheckOrCheckMate(currentKingStatus)) { //Cannot move, revert
                setPiecePositionWithoutMoveState(piecesFrom, from);

                if (isEatingPiece) {
                    setPiecePositionWithoutMoveState(piecesTo, to); //reset the attacked piece
                } else {
                    removePieceFromBoard(to);
                }

                return MoveType.MOVE_NOT_ALLOWED;
            } else {
                changeAllowedMoveSide();

                if (isEatingPiece) { //Count the point for the piece
                    updatePointsForSide(playerSide, piecesTo.getPoint());
                    moveType = MoveType.CAPTURE;
                }
            }
        } else if (MoveType.CASTLING.equals(moveType)) {
            /*
                If queen side, move rook to D1 / D8 and king to C1 / C8
                Otherwise, move rook to F1 / F8 and king to G1 / G8
             */
            CastlingPositionHelper castlingPositionHelper = new CastlingPositionHelper(from, to, playerSide).invoke();
            CasePosition kingPosition = castlingPositionHelper.getKingPosition();
            CasePosition rookPosition = castlingPositionHelper.getRookPosition();

            movePieceTo(from, kingPosition, piecesFrom);

            if (isEatingPiece) {
                movePieceTo(to, rookPosition, piecesTo);
            }
            changeAllowedMoveSide();
        } else if (MoveType.EN_PASSANT.equals(moveType)) {
            movePieceTo(from, to, piecesFrom);
            changeAllowedMoveSide();

            CasePosition enemyPawnPosition = MathUtils.getNearestPositionFromDirection(to, otherPlayerSide.equals(Side.BLACK) ? Direction.SOUTH : Direction.NORTH);
            Pieces enemyPawnToEat = getPiece(enemyPawnPosition);
            updatePointsForSide(playerSide, enemyPawnToEat.getPoint());
            removePieceFromBoard(enemyPawnPosition);
        }

        KingStatus otherKingStatusAfterMove = getKingStatus(otherPlayerSide, true);

        if (MoveType.isMoved(moveType)) {
            sendMovedMessages(from, to, playerSide);
        }

        moveHistory.setCurrentKingStatus(currentKingStatus);
        moveHistory.setOtherKingStatus(otherKingStatusAfterMove);
        sendCheckOrCheckmateMessages(currentKingStatus, otherKingStatusAfterMove, playerSide);

        return moveType;
    }

    protected final boolean isPlayerTurn(Side sideFrom) {
        return isGameHaveRule(SpecialGameRules.NO_PLAYER_TURN) || currentAllowedMoveSide.equals(sideFrom);
    }

    protected final void changeAllowedMoveSide() {
        if (BLACK.equals(currentAllowedMoveSide)) {
            currentAllowedMoveSide = WHITE;
        } else {
            currentAllowedMoveSide = BLACK;
        }
    }

    private void sendPawnPromotionMessage(CasePosition to, Side playerSide) {
        WEB_SOCKET_SERVICE.fireSideEvent(uuid, playerSide, PAWN_PROMOTION, to.name());
        WEB_SOCKET_SERVICE.fireGameEvent(uuid, PAWN_PROMOTION, String.format(GAME_PAUSED_PAWN_PROMOTION, playerSide));
    }

    private void sendMovedMessages(CasePosition from, CasePosition to, Side playerSide) {
        WEB_SOCKET_SERVICE.fireGameEvent(uuid, MOVE, String.format(PLAYER_MOVE, playerSide, from, to));
        WEB_SOCKET_SERVICE.fireSideEvent(uuid, Side.getOtherPlayerSide(playerSide), PLAYER_TURN, Constants.PLAYER_TURN);
        WEB_SOCKET_SERVICE.fireGameEvent(uuid, SCORE_UPDATE, getGameScore());
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
        return GAME_CONSTRAINTS.isPieceMovableTo(from, to, playerSide, this, MoveMode.NORMAL_OR_ATTACK_MOVE);
    }

    /**
     * 1) Check if the king can move / kill to escape.
     * 2) If not, try to liberate a case around the king, by killing / blocking the piece with an ally piece (if only one that can hit this target).
     * 3) If not, the king is checkmate.
     *
     * @param playerSide
     * @param enableCheckForStalemate - Prevent an infinite loop when evaluating
     * @return
     */
    public KingStatus getKingStatus(Side playerSide, boolean enableCheckForStalemate) {
        KingStatus kingStatus = OK;

        Pieces kingPiece = Pieces.getKingBySide(playerSide);
        CasePosition kingPosition = GameUtils.getSinglePiecePosition(kingPiece, getPiecesLocation());

        if (isGameHaveRule(SpecialGameRules.NO_CHECK_OR_CHECKMATE)) {
            return kingStatus;
        }

        Assert.assertNotNull(kingPosition, playerSide);
        MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> piecesThatCanHitOriginalPosition = getPiecesThatCanHitPosition(Side.getOtherPlayerSide(playerSide), kingPosition);

        boolean isCheck = !piecesThatCanHitOriginalPosition.isEmpty();
        if (isCheck) {
            kingStatus = KingStatus.CHECKMATE;

            //Try to move the king
            if (!getPositionKingCanMove(playerSide).isEmpty()) {
                return KingStatus.CHECK;
            }

            //If not able to move, try to kill the enemy piece with an other piece
            if (piecesThatCanHitOriginalPosition.size() == 1) {
                Pair<CasePosition, Pieces> enemyPiecesPair = piecesThatCanHitOriginalPosition.get(kingPosition).get(0);
                CasePosition to = enemyPiecesPair.getFirstValue();

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

                        return KingStatus.CHECK;
                    } else if (isPieceMovableTo(from, to, playerSide) && !isKingCheckAfterMove(from, to, playerSide)) {
                        return KingStatus.CHECK; //One or more piece is able to kill the enemy
                    }
                }
            }

            //Try to block the path of the enemy with one of the pieces
            List<Pair<CasePosition, Pieces>> pairs = piecesThatCanHitOriginalPosition.get(kingPosition);
            if (pairs.size() == 1) { //We can only block one piece, if more, checkmate
                Pair<CasePosition, Pieces> casePositionPiecesPair = pairs.get(0);
                Pieces enemyPiece = casePositionPiecesPair.getSecondValue();
                CasePosition enemyPosition = casePositionPiecesPair.getFirstValue();

                if (Pieces.isKnight(enemyPiece)) { //We cannot block a knight
                    return KingStatus.CHECKMATE;
                }

                for (CasePosition to : MathUtils.getPositionsBetweenTwoPosition(enemyPosition, kingPosition)) { //For each position between the king and the enemy, we try to block it
                    for (CasePosition from : getPiecesLocation(playerSide).keySet()) { //Try to find if one of our piece can block the target
                        if (Pieces.isKing(getPiece(from))) {
                            continue;
                        }

                        if (isPieceMovableTo(from, to, playerSide) && !isKingCheckAfterMove(from, to, playerSide)) {
                            return KingStatus.CHECK;
                        }
                    }
                }
            }
        } else if (getPositionKingCanMove(playerSide).isEmpty() && enableCheckForStalemate) { //Check if not a stalemate
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
            kingStatus = isStalemate ? STALEMATE : kingStatus;
        }

        return kingStatus;
    }

    protected void updatePointsForSide(Side side, byte point) {
        Assert.assertNotNull(side);
        Assert.assertNumberSuperiorOrEqualsTo(point, (byte) 0);

        switch (side) {
            case BLACK:
                blackPlayerPoint += point;
                break;
            case WHITE:
                whitePlayerPoint += point;
                break;
            default:
                break;
        }
    }

    private void sendCheckOrCheckmateMessages(KingStatus currentkingStatus, KingStatus otherKingStatusAfterMove, Side playerSide) {
        Assert.assertNotNull(currentkingStatus, otherKingStatusAfterMove);
        Side otherPlayerSide = Side.getOtherPlayerSide(playerSide);

        if (KingStatus.CHECKMATE.equals(currentkingStatus)) {
            WEB_SOCKET_SERVICE.fireGameEvent(uuid, KING_CHECKMATE, String.format(PLAYER_KING_CHECKMATE, playerSide));
        } else if (KingStatus.CHECKMATE.equals(otherKingStatusAfterMove)) {
            WEB_SOCKET_SERVICE.fireGameEvent(uuid, KING_CHECKMATE, String.format(PLAYER_KING_CHECKMATE, otherPlayerSide));
        }

        if (KingStatus.CHECK.equals(currentkingStatus)) {
            WEB_SOCKET_SERVICE.fireSideEvent(uuid, playerSide, KING_CHECK, Constants.PLAYER_KING_CHECK);
        } else if (KingStatus.CHECK.equals(otherKingStatusAfterMove)) {
            WEB_SOCKET_SERVICE.fireSideEvent(uuid, otherPlayerSide, KING_CHECK, Constants.PLAYER_KING_CHECK);
        }

    }

    public boolean isGameHaveRule(SpecialGameRules rule) {
        return SPECIAL_GAME_RULES.contains(rule);
    }

    public GameScoreResponse getGameScore() {
        return new GameScoreResponse(whitePlayerPoint, blackPlayerPoint);
    }

    /**
     * Gets the pieces that can hit the target, the {@link CasePosition} inside the {@link Pair} is the starting position of the attacking {@link Pieces}
     *
     * @param positions
     * @param sideToKeep
     * @return
     */
    public MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> getPiecesThatCanHitPosition(Side sideToKeep, CasePosition... positions) {
        Assert.assertNotEmpty(positions);

        MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> values = new MultiArrayMap<>();

        for (CasePosition position : positions) {
            for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : getPiecesLocation().entrySet()) {
                CasePosition key = casePositionPiecesEntry.getKey();
                Pieces value = casePositionPiecesEntry.getValue();

                Side pieceSide = value.getSide();
                if (!pieceSide.equals(sideToKeep)) {
                    continue;
                }

                if (GAME_CONSTRAINTS.isPieceMovableTo(key, position, pieceSide, this, MoveMode.IS_KING_CHECK_MODE)) {
                    values.put(position, new Pair<>(key, value));
                }
            }
        }

        return values;
    }

    public List<CasePosition> getPositionKingCanMove(Side playerSide) {
        Assert.assertNotNull(playerSide);
        CasePosition kingPosition = GameUtils.getSinglePiecePosition(Pieces.getKingBySide(playerSide), getPiecesLocation());

        List<CasePosition> values = new ArrayList<>();
        List<CasePosition> caseAround = MathUtils.getAllPositionsAroundPosition(kingPosition);
        for (CasePosition position : caseAround) {  //Check if the king can kill something to save himself
            if (isPieceMovableTo(kingPosition, position, playerSide) && !isKingCheckAtPosition(position, playerSide)) {
                values.add(position);
            }
        }

        //Add the position, if the castling is authorized on the rook
        Pieces rook = Side.WHITE.equals(playerSide) ? Pieces.W_ROOK : Pieces.B_ROOK;
        for (CasePosition rookPosition : GameUtils.getPiecesPosition(rook, getPiecesLocation())) {
            if (MoveType.CASTLING.equals(GAME_CONSTRAINTS.getMoveType(kingPosition, rookPosition, this))) {
                values.add(rookPosition);
            }
        }

        return values;
    }

    /**
     * Gets the pieces / CasePosition based on a side
     *
     * @param side
     * @return
     */
    public final Map<CasePosition, Pieces> getPiecesLocation(Side side) {
        Assert.assertNotNull(side);

        Map<CasePosition, Pieces> values = new EnumMap<>(CasePosition.class);

        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : getPiecesLocation().entrySet()) {
            CasePosition key = casePositionPiecesEntry.getKey();
            Pieces value = casePositionPiecesEntry.getValue();

            if (side.equals(value.getSide())) {
                values.put(key, value);
            }
        }

        return values;
    }

    public boolean isKingCheckAfterMove(CasePosition from, CasePosition to, Side playerSide) {
        boolean isKingCheck;

        Pieces currentPiece = getPiece(from);
        MoveType moveType = GAME_CONSTRAINTS.getMoveType(from, to, this);

        //To check if the pawn used the +2 move
        Map<CasePosition, Boolean> isPawnUsedSpecialMoveMap = new EnumMap<>(getIsPawnUsedSpecialMoveMap());
        Map<CasePosition, Boolean> copyOfIsPawnUsedSpecialMoveMap = new EnumMap<>(isPawnUsedSpecialMoveMap);

        //To check if the piece moved in the current game
        Map<CasePosition, Boolean> isPiecesMovedMap = new EnumMap<>(getIsPiecesMovedMap());
        Map<CasePosition, Boolean> copyOfIsPiecesMovedMap = new EnumMap<>(isPiecesMovedMap);

        //To get the number of turns since last move
        Map<CasePosition, Integer> turnNumberPieceMap = new EnumMap<>(getTurnNumberPieceMap());
        Map<CasePosition, Integer> copyOfTurnNumberPieceMap = new EnumMap<>(turnNumberPieceMap);

        if (MoveType.EN_PASSANT.equals(moveType)) {
            CasePosition enPassantEnemyPawnPosition = PawnMoveConstraint.getEnPassantEnemyPawnPosition(to, getOtherPlayerSide(playerSide));

            Pieces enemyPawn = getPiece(enPassantEnemyPawnPosition);

            removePieceFromBoard(enPassantEnemyPawnPosition); //Remove the enemy pawn
            setPiecePositionWithoutMoveState(currentPiece, to);

            //Set the new values in the maps
            isPiecesMovedMap.remove(from);
            isPiecesMovedMap.put(to, true);

            turnNumberPieceMap.remove(from);
            turnNumberPieceMap.put(to, getNbTotalMove());

            //Set the new maps
            setPiecesGameState(isPawnUsedSpecialMoveMap, turnNumberPieceMap, isPiecesMovedMap);

            MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> piecesThatCanHitOriginalPosition = getPiecesThatCanHitPosition(Side.getOtherPlayerSide(playerSide), GameUtils.getSinglePiecePosition(Pieces.getKingBySide(playerSide), getPiecesLocation()));

            isKingCheck = !piecesThatCanHitOriginalPosition.isEmpty();

            //Reset the pawns
            removePieceFromBoard(to);
            setPiecePositionWithoutMoveState(currentPiece, from);
            setPiecePositionWithoutMoveState(enemyPawn, enPassantEnemyPawnPosition);
        } else {
            Pieces pieceEaten = getPiece(to);

            removePieceFromBoard(from);
            setPiecePositionWithoutMoveState(currentPiece, to);

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

            MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> piecesThatCanHitOriginalPosition = getPiecesThatCanHitPosition(Side.getOtherPlayerSide(playerSide), GameUtils.getSinglePiecePosition(Pieces.getKingBySide(playerSide), getPiecesLocation()));

            isKingCheck = !piecesThatCanHitOriginalPosition.isEmpty();

            //Reset the piece(s)
            setPiecePositionWithoutMoveState(currentPiece, from);
            if (pieceEaten != null) {
                setPiecePositionWithoutMoveState(pieceEaten, to);
            } else {
                removePieceFromBoard(to);
            }
        }

        //restore the old maps
        setPiecesGameState(copyOfIsPawnUsedSpecialMoveMap, copyOfTurnNumberPieceMap, copyOfIsPiecesMovedMap);

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
        Pieces pieces = getPiece(from);

        if (pieces == null || !pieces.getSide().equals(playerSide)) {
            return positions;
        }

        for (CasePosition position : CasePosition.values()) {

            boolean isSpecialMove = MoveType.isSpecialMove(GAME_CONSTRAINTS.getMoveType(from, position, this));

            if (isSpecialMove || !from.equals(position) && isPieceMovableTo(from, position, playerSide)) {
                positions.add(position);
            }
        }

        return positions;
    }

    public boolean isKingCheckAtPosition(CasePosition currentPosition, Side playerSide) {
        Assert.assertNotNull(currentPosition, playerSide);

        if (isGameHaveRule(SpecialGameRules.NO_CHECK_OR_CHECKMATE)) {
            return false;
        }

        Assert.assertNotNull(currentPosition, playerSide);

        return !getPiecesThatCanHitPosition(Side.getOtherPlayerSide(playerSide), currentPosition).isEmpty();
    }

    public List<MoveHistory> getMoveHistory() {
        return moveHistoryList;
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

        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : getPiecesLocation().entrySet()) {
            CasePosition from = casePositionPiecesEntry.getKey();
            Pieces piecesFrom = casePositionPiecesEntry.getValue();

            if (!sideToKeep.equals(piecesFrom.getSide())) {
                continue;
            }

            MoveType moveType = GAME_CONSTRAINTS.getMoveType(from, to, this);
            boolean isEnPassant = MoveType.EN_PASSANT.equals(moveType);

            if (!isKingCheckAfterMove(from, to, sideToKeep) && isPieceMovableTo(from, to, sideToKeep) || isEnPassant) {
                values.add(new Pair<>(from, piecesFrom));
            }
        }

        return values;
    }

    public final boolean setPlayerToSide(Player player, Side side) {
        Assert.assertNotNull(player, side);
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
        if (playerWhite == player) {
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

        if (playerWhite == player) {
            side = WHITE;
        } else if (playerBlack == player) {
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
        return observerList.contains(player) || playerBlack == player || playerWhite == player;
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
        Assert.assertNotEmpty(rules);
        SPECIAL_GAME_RULES.addAll(Arrays.asList(rules));
    }

    public void removeSpecialRule(SpecialGameRules... rules) {
        Assert.assertNotEmpty(rules);
        SPECIAL_GAME_RULES.removeAll(Arrays.asList(rules));
    }

    public List<Player> getObserverList() {
        return Collections.unmodifiableList(observerList);
    }

    public Set<SpecialGameRules> getSpecialGameRules() {
        return Collections.unmodifiableSet(SPECIAL_GAME_RULES);
    }

    public boolean isGameDone() {

        return KingStatus.CHECKMATE.equals(getKingStatus(BLACK, false)) ||
                KingStatus.CHECKMATE.equals(getKingStatus(WHITE, false)) ||
                isGameDraw();
    }

}
