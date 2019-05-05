package ca.watier.echechess.engine.handlers;

import ca.watier.echechess.common.enums.*;
import ca.watier.echechess.common.utils.MathUtils;
import ca.watier.echechess.common.utils.MultiArrayMap;
import ca.watier.echechess.common.utils.ObjectUtils;
import ca.watier.echechess.common.utils.Pair;
import ca.watier.echechess.engine.constraints.PawnMoveConstraint;
import ca.watier.echechess.engine.delegates.PieceMoveConstraintDelegate;
import ca.watier.echechess.engine.engines.GenericGameHandler;
import ca.watier.echechess.engine.interfaces.KingHandler;
import ca.watier.echechess.engine.utils.GameUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static ca.watier.echechess.common.enums.KingStatus.OK;
import static ca.watier.echechess.common.enums.KingStatus.STALEMATE;
import static ca.watier.echechess.common.enums.Side.WHITE;
import static ca.watier.echechess.common.enums.Side.getOtherPlayerSide;

public class KingHandlerImpl implements KingHandler {

    private GenericGameHandler genericGameHandler;
    private PieceMoveConstraintDelegate pieceMoveConstraintDelegate;

    public KingHandlerImpl(GenericGameHandler genericGameHandler, PieceMoveConstraintDelegate pieceMoveConstraintDelegate) {
        this.genericGameHandler = genericGameHandler;
        this.pieceMoveConstraintDelegate = pieceMoveConstraintDelegate;
    }

    @Override
    public boolean isStalemate(Side playerSide, Pieces kingPiece, CasePosition kingPosition) {
        boolean isStalemate = true;

        //Check if we can move the pieces around the king (same color)
        for (CasePosition moveFrom : MathUtils.getAllPositionsAroundPosition(kingPosition)) {
            Pieces currentPiece = genericGameHandler.getPiece(moveFrom);

            if (currentPiece != null && !Pieces.isKing(currentPiece) && Pieces.isSameSide(currentPiece, kingPiece)) {
                for (CasePosition moveTo : genericGameHandler.getAllAvailableMoves(moveFrom, playerSide)) {
                    if (genericGameHandler.isPieceMovableTo(moveFrom, moveTo, playerSide) && !isKingCheckAfterMove(moveFrom, moveTo, playerSide)) {
                        isStalemate = false;
                        break;
                    }
                }
            }
        }
        return isStalemate;
    }

    @Override
    public KingStatus getKingStatusWhenPiecesCanHitKing(Side playerSide, CasePosition kingPosition, MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> piecesThatCanHitOriginalPosition) {

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

        return KingStatus.CHECKMATE;
    }

    public List<CasePosition> getPositionKingCanMove(Side playerSide) {
        if (playerSide == null) {
            return null;
        }

        CasePosition kingPosition = GameUtils.getSinglePiecePosition(Pieces.getKingBySide(playerSide), genericGameHandler.getPiecesLocation());

        List<CasePosition> values = new ArrayList<>();
        List<CasePosition> caseAround = MathUtils.getAllPositionsAroundPosition(kingPosition);
        for (CasePosition position : caseAround) {  //Check if the king can kill something to save himself
            if (genericGameHandler.isPieceMovableTo(kingPosition, position, playerSide) && !genericGameHandler.isKingCheckAtPosition(position, playerSide)) {
                values.add(position);
            }
        }

        //Add the position, if the castling is authorized on the rook
        Pieces rook = WHITE.equals(playerSide) ? Pieces.W_ROOK : Pieces.B_ROOK;
        for (CasePosition rookPosition : GameUtils.getPiecesPosition(rook, genericGameHandler.getPiecesLocation())) {
            if (MoveType.CASTLING.equals(pieceMoveConstraintDelegate.getMoveType(kingPosition, rookPosition, genericGameHandler))) {
                values.add(rookPosition);
            }
        }

        return values;
    }

    @Override
    public boolean isKingCheckAfterMove(CasePosition from, CasePosition to, Side playerSide) {
        if (ObjectUtils.hasNull(from, to, playerSide)) {
            return false;
        }

        boolean isKingCheck;
        MoveType moveType = pieceMoveConstraintDelegate.getMoveType(from, to, genericGameHandler);

        genericGameHandler.cloneCurrentState();
        if (MoveType.EN_PASSANT.equals(moveType)) {
            isKingCheck = isKingCheckWithEnPassantMove(from, to, playerSide);
        } else {
            isKingCheck = isKingCheckWithOtherMove(from, to, playerSide, moveType);
        }
        genericGameHandler.restoreLastState();

        return isKingCheck;
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
        CasePosition kingPosition = GameUtils.getSinglePiecePosition(kingPiece, genericGameHandler.getPiecesLocation());

        if (genericGameHandler.isGameHaveRule(SpecialGameRules.NO_CHECK_OR_CHECKMATE)) {
            return kingStatus;
        }

        MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> piecesThatCanHitOriginalPosition = genericGameHandler.getPiecesThatCanHitPosition(getOtherPlayerSide(playerSide), kingPosition);

        if (!piecesThatCanHitOriginalPosition.isEmpty()) { //One or more piece can hit the king
            kingStatus = getKingStatusWhenPiecesCanHitKing(playerSide, kingPosition, piecesThatCanHitOriginalPosition);
        } else if (getPositionKingCanMove(playerSide).isEmpty()) { //Check if not a stalemate
            kingStatus = isStalemate(playerSide, kingPiece, kingPosition) ? STALEMATE : kingStatus;
        }

        return kingStatus;
    }

    private boolean isKingCheckWithEnPassantMove(CasePosition from, CasePosition to, Side playerSide) {
        CasePosition enPassantEnemyPawnPosition = PawnMoveConstraint.getEnemyPawnPositionFromEnPassant(to, getOtherPlayerSide(playerSide));
        Pieces currentPiece = genericGameHandler.getPiece(from);

        //Copy the values
        Boolean isPawnUsedSpecialMove = genericGameHandler.isPawnUsedSpecialMove(from);
        Integer pieceTurn = genericGameHandler.getPieceTurn(from);

        genericGameHandler.removePieceFromBoard(enPassantEnemyPawnPosition); //Remove the enemy pawn
        genericGameHandler.removePieceFromBoard(from); //Remove the from pawn
        genericGameHandler.setPiecePositionWithoutMoveState(currentPiece, to);

        //To check if the pawn used the +2 move
        Map<CasePosition, Boolean> isPawnUsedSpecialMoveMap = new EnumMap<>(genericGameHandler.getIsPawnUsedSpecialMoveMap());
        isPawnUsedSpecialMoveMap.put(to, isPawnUsedSpecialMove);

        //To check if the piece moved in the current game
        Map<CasePosition, Boolean> isPiecesMovedMap = new EnumMap<>(genericGameHandler.getIsPiecesMovedMap());
        isPiecesMovedMap.put(to, true);

        //To get the number of turns since last move
        Map<CasePosition, Integer> turnNumberPieceMap = new EnumMap<>(genericGameHandler.getTurnNumberPieceMap());
        turnNumberPieceMap.put(to, pieceTurn + 1);

        //Set the new maps
        genericGameHandler.setPiecesGameState(isPawnUsedSpecialMoveMap, turnNumberPieceMap, isPiecesMovedMap);

        Map<CasePosition, Pieces> piecesLocation = genericGameHandler.getPiecesLocation();
        CasePosition singlePiecePosition = GameUtils.getSinglePiecePosition(Pieces.getKingBySide(playerSide), piecesLocation);
        MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> piecesThatCanHitOriginalPosition =
                genericGameHandler.getPiecesThatCanHitPosition(getOtherPlayerSide(playerSide), singlePiecePosition);
        return !piecesThatCanHitOriginalPosition.isEmpty();
    }

    private boolean isKingCheckWithOtherMove(CasePosition from, CasePosition to, Side playerSide, MoveType moveType) {
        boolean isKingCheck;
        Pieces currentPiece = genericGameHandler.getPiece(from);
        Pieces pieceEaten = genericGameHandler.getPiece(to);

        genericGameHandler.removePieceFromBoard(from);
        genericGameHandler.setPiecePositionWithoutMoveState(currentPiece, to);

        //To check if the pawn used the +2 move
        Map<CasePosition, Boolean> isPawnUsedSpecialMoveMap = new EnumMap<>(genericGameHandler.getIsPawnUsedSpecialMoveMap());

        //To check if the piece moved in the current game
        Map<CasePosition, Boolean> isPiecesMovedMap = new EnumMap<>(genericGameHandler.getIsPiecesMovedMap());

        //To get the number of turns since last move
        Map<CasePosition, Integer> turnNumberPieceMap = new EnumMap<>(genericGameHandler.getTurnNumberPieceMap());

        //Set the new values in the maps
        if (MoveType.PAWN_HOP.equals(moveType)) {
            isPawnUsedSpecialMoveMap.remove(from);
            isPawnUsedSpecialMoveMap.put(to, true); //the pawn is now moved
        }

        isPiecesMovedMap.remove(from);
        isPiecesMovedMap.put(to, true);

        turnNumberPieceMap.remove(from);
        turnNumberPieceMap.put(to, genericGameHandler.getNbTotalMove());

        //Set the new maps
        genericGameHandler.setPiecesGameState(isPawnUsedSpecialMoveMap, turnNumberPieceMap, isPiecesMovedMap);

        Map<CasePosition, Pieces> piecesLocation = genericGameHandler.getPiecesLocation();
        CasePosition singlePiecePosition = GameUtils.getSinglePiecePosition(Pieces.getKingBySide(playerSide), piecesLocation);

        MultiArrayMap<CasePosition, Pair<CasePosition, Pieces>> piecesThatCanHitOriginalPosition =
                genericGameHandler.getPiecesThatCanHitPosition(getOtherPlayerSide(playerSide), singlePiecePosition);

        isKingCheck = !piecesThatCanHitOriginalPosition.isEmpty();

        //Reset the piece(s)
        genericGameHandler.setPiecePositionWithoutMoveState(currentPiece, from);
        if (pieceEaten != null) {
            genericGameHandler.setPiecePositionWithoutMoveState(pieceEaten, to);
        } else {
            genericGameHandler.removePieceFromBoard(to);
        }
        return isKingCheck;
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
                for (CasePosition from : genericGameHandler.getPiecesLocation(playerSide).keySet()) { //Try to find if one of our piece can block the target
                    if (Pieces.isKing(genericGameHandler.getPiece(from))) {
                        continue;
                    }

                    if (genericGameHandler.isPieceMovableTo(from, to, playerSide) && !isKingCheckAfterMove(from, to, playerSide)) {
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
        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : genericGameHandler.getPiecesLocation(playerSide).entrySet()) {
            CasePosition from = casePositionPiecesEntry.getKey();

            if (Pieces.isKing(casePositionPiecesEntry.getValue())) {
                continue;
            }

            Pieces currentPiece = genericGameHandler.getPiece(from);
            Pieces enemyPiece = genericGameHandler.getPiece(to);

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

                if (!PawnMoveConstraint.isEnPassant(to, positionByRankAndColumn, genericGameHandler, playerSide)) {
                    continue;
                }

                CasePosition enPassantEnemyPawnPosition =
                        PawnMoveConstraint.getEnPassantPositionFromEnemyPawn(to, Side.getOtherPlayerSide(playerSide));

                isCheck &= isKingCheckAfterMove(from, enPassantEnemyPawnPosition, playerSide);
            } else if (genericGameHandler.isPieceMovableTo(from, to, playerSide) && !isKingCheckAfterMove(from, to, playerSide)) {
                isCheck &= isKingCheckAfterMove(from, to, playerSide); //One or more piece is able to kill the enemy
            }
        }

        return isCheck;
    }

    /**
     * Find the position for a column and a rank
     *
     * @param rank
     * @param column
     */
    private CasePosition getPositionByRankAndColumn(Ranks rank, char column, Side side) {
        if (ObjectUtils.hasNull(rank, side)) {
            return null;
        }

        Stream<CasePosition> arrayStream = Arrays.stream(CasePosition.values());
        return arrayStream.filter(getCasePositionPredicateOnSameColumnAndRank(rank, column, side))
                .findFirst()
                .orElse(null);
    }

    private Predicate<CasePosition> getCasePositionPredicateOnSameColumnAndRank(Ranks rank, char column, Side side) {
        return casePosition -> rank.equals(Ranks.getRank(casePosition, side)) && casePosition.isOnSameColumn(column);
    }
}
