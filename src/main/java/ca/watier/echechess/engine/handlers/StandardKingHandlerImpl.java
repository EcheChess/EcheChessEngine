package ca.watier.echechess.engine.handlers;

import ca.watier.echechess.common.enums.*;
import ca.watier.echechess.common.utils.MathUtils;
import ca.watier.echechess.common.utils.ObjectUtils;
import ca.watier.echechess.engine.abstracts.GameBoardData;
import ca.watier.echechess.engine.constraints.PawnMoveConstraint;
import ca.watier.echechess.engine.delegates.PieceMoveConstraintDelegate;
import ca.watier.echechess.engine.interfaces.KingHandler;
import ca.watier.echechess.engine.models.enums.MoveStatus;
import ca.watier.echechess.engine.utils.GameUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static ca.watier.echechess.common.enums.KingStatus.OK;
import static ca.watier.echechess.common.enums.KingStatus.STALEMATE;
import static ca.watier.echechess.common.enums.Side.WHITE;
import static ca.watier.echechess.common.enums.Side.getOtherPlayerSide;

public class StandardKingHandlerImpl implements KingHandler {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StandardKingHandlerImpl.class);

    private PieceMoveConstraintDelegate moveConstraintDelegate;

    public StandardKingHandlerImpl(PieceMoveConstraintDelegate moveConstraintDelegate) {
        this.moveConstraintDelegate = moveConstraintDelegate;
    }


    /**
     * 1) Check if the king can move / kill to escape.
     * 2) If not, try to liberate a case around the king, by killing / blocking the piece with an ally piece (if only one that can hit this target).
     * 3) If not, the king is checkmate.
     *
     * @param playerSide
     * @param gameBoardData
     * @return
     */
    @Override
    public KingStatus getKingStatus(Side playerSide, GameBoardData gameBoardData) {
        if (playerSide == null) {
            return null;
        }

        Pieces kingPiece = Pieces.getKingBySide(playerSide);
        CasePosition kingPosition = GameUtils.getSinglePiecePosition(kingPiece, gameBoardData.getPiecesLocation());
        List<CasePosition> piecesThatCanHitOriginalPosition = getPositionsThatCanMoveOrAttackPosition(kingPosition, getOtherPlayerSide(playerSide), gameBoardData);

        if (CollectionUtils.isNotEmpty(piecesThatCanHitOriginalPosition)) { //One or more piece can hit the king
            return getKingStatusWhenPiecesCanHitKing(playerSide, kingPosition, piecesThatCanHitOriginalPosition, gameBoardData);
        } else if (getPositionKingCanMove(playerSide, gameBoardData).isEmpty()) { //Check if not a stalemate
            return isStalemate(playerSide, gameBoardData) ? STALEMATE : OK;
        } else {
            return OK;
        }
    }


    private List<CasePosition> getPositionKingCanMove(Side playerSide, GameBoardData gameBoardData) {
        if (playerSide == null) {
            return null;
        }

        CasePosition kingPosition = GameUtils.getSinglePiecePosition(Pieces.getKingBySide(playerSide), gameBoardData.getPiecesLocation());
        List<CasePosition> values = new ArrayList<>();
        List<CasePosition> caseAround = MathUtils.getAllPositionsAroundPosition(kingPosition);

        for (CasePosition to : caseAround) {  //Check if the king can kill something to save himself
            MoveStatus moveStatus = moveConstraintDelegate.getMoveStatus(kingPosition, to, gameBoardData);

            if (targetIsNotKing(to, gameBoardData) && MoveStatus.isMoveValid(moveStatus) && !isKingCheckAfterMove(kingPosition, to, gameBoardData)) {
                values.add(to);
            }
        }

        //Add the position, if the castling is authorized on the rook
        Pieces rook = WHITE.equals(playerSide) ? Pieces.W_ROOK : Pieces.B_ROOK;

        for (CasePosition rookPosition : GameUtils.getPiecesPosition(rook, gameBoardData.getPiecesLocation())) {
            if (MoveType.CASTLING.equals(moveConstraintDelegate.getMoveType(kingPosition, rookPosition, gameBoardData))) {
                values.add(rookPosition);
            }
        }

        return values;
    }

    private boolean targetIsNotKing(CasePosition to, GameBoardData gameBoardData) {
        return !Pieces.isKing(gameBoardData.getPiece(to));
    }

    @Override
    public boolean isKingCheckAfterMove(CasePosition from, CasePosition to, GameBoardData gameBoardData) {
        if (ObjectUtils.hasNull(from, to)) {
            return false;
        }

        MoveType moveType = moveConstraintDelegate.getMoveType(from, to, gameBoardData);

        if (MoveType.EN_PASSANT.equals(moveType)) {
            return isKingCheckWithEnPassantMove(from, to, gameBoardData);
        } else {
            return isKingCheckWithOtherMove(from, to, moveType, gameBoardData);
        }
    }

    private boolean isKingCheckWithEnPassantMove(CasePosition from, CasePosition to, GameBoardData gameBoardData) {
        try {
            GameBoardData clonedData = gameBoardData.clone();

            Pieces currentPiece = clonedData.getPiece(from);
            Side side = currentPiece.getSide();
            Side otherPlayerSide = getOtherPlayerSide(side);

            CasePosition enPassantEnemyPawnPosition = PawnMoveConstraint.getEnemyPawnPositionFromEnPassant(to, otherPlayerSide);

            //Copy the values
            Boolean isPawnUsedSpecialMove = clonedData.isPawnUsedSpecialMove(from);
            Integer pieceTurn = clonedData.getPieceTurn(from);

            clonedData.removePieceFromBoard(enPassantEnemyPawnPosition); //Remove the enemy pawn
            clonedData.removePieceFromBoard(from); //Remove the from pawn
            clonedData.setPiecePositionWithoutMoveState(currentPiece, to);

            //To check if the pawn used the +2 move
            Map<CasePosition, Boolean> isPawnUsedSpecialMoveMap = createNewMapFrom(clonedData.getIsPawnUsedSpecialMoveMap());
            isPawnUsedSpecialMoveMap.put(to, isPawnUsedSpecialMove);

            //To check if the piece moved in the current game
            Map<CasePosition, Boolean> isPiecesMovedMap = createNewMapFrom(clonedData.getIsPiecesMovedMap());
            isPiecesMovedMap.put(to, true);

            //To get the number of turns since last move
            Map<CasePosition, Integer> turnNumberPieceMap = new EnumMap<>(clonedData.getTurnNumberPieceMap());
            turnNumberPieceMap.put(to, pieceTurn + 1);

            //Set the new maps
            clonedData.setPiecesGameState(isPawnUsedSpecialMoveMap, turnNumberPieceMap, isPiecesMovedMap);

            Map<CasePosition, Pieces> piecesLocation = clonedData.getPiecesLocation();
            CasePosition singlePiecePosition = GameUtils.getSinglePiecePosition(Pieces.getKingBySide(side), piecesLocation);

            List<CasePosition> positionsThatCanExecuteActionToPosition =
                    getPositionsThatCanMoveOrAttackPosition(singlePiecePosition, otherPlayerSide, clonedData);

            return CollectionUtils.isNotEmpty(positionsThatCanExecuteActionToPosition);
        } catch (CloneNotSupportedException e) {
            LOGGER.error("Unable to clone the data!", e);
            return false;
        }
    }


    public List<CasePosition> getPositionsThatCanMoveOrAttackPosition(CasePosition to, Side otherPlayerSide, GameBoardData gameBoardData) {
        List<CasePosition> positions = new ArrayList<>();
        Map<CasePosition, Pieces> piecesLocation = gameBoardData.getPiecesLocation(otherPlayerSide);

        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : piecesLocation.entrySet()) {
            CasePosition from = casePositionPiecesEntry.getKey();

            if (canMoveAndKingNotCheck(from, to, gameBoardData)) {
                positions.add(from);
            }
        }

        return positions;
    }

    private boolean canMoveAndKingNotCheck(CasePosition from, CasePosition to, GameBoardData gameBoardData) {
        MoveStatus moveStatus = moveConstraintDelegate.getMoveStatus(from, to, gameBoardData);

        if (MoveStatus.KING_ATTACK_KING.equals(moveStatus) || MoveStatus.CAN_PROTECT_FRIENDLY.equals(moveStatus)) {
            return true;
        } else {
            return MoveStatus.isMoveValid(moveStatus)
                    && !isKingCheckAfterMove(from, to, gameBoardData);
        }
    }


    private boolean isKingCheckWithOtherMove(CasePosition from, CasePosition to, MoveType moveType, GameBoardData gameBoardData) {

        try {
            GameBoardData clonedData = gameBoardData.clone();

            Pieces currentPiece = clonedData.getPiece(from);
            Side playerSide = currentPiece.getSide();

            clonedData.removePieceFromBoard(from);
            clonedData.setPiecePositionWithoutMoveState(currentPiece, to);

            //To check if the pawn used the +2 move
            Map<CasePosition, Boolean> isPawnUsedSpecialMoveMap = createNewMapFrom(clonedData.getIsPawnUsedSpecialMoveMap());

            //To check if the piece moved in the current game
            Map<CasePosition, Boolean> isPiecesMovedMap = createNewMapFrom(clonedData.getIsPiecesMovedMap());

            //To get the number of turns since last move
            Map<CasePosition, Integer> turnNumberPieceMap = createNewMapFrom(clonedData.getTurnNumberPieceMap());

            //Set the new values in the maps
            if (MoveType.PAWN_HOP.equals(moveType)) {
                isPawnUsedSpecialMoveMap.remove(from);
                isPawnUsedSpecialMoveMap.put(to, true); //the pawn is now moved
            }

            isPiecesMovedMap.remove(from);
            isPiecesMovedMap.put(to, true);

            turnNumberPieceMap.remove(from);
            turnNumberPieceMap.put(to, clonedData.getNbTotalMove());

            //Set the new maps
            clonedData.setPiecesGameState(isPawnUsedSpecialMoveMap, turnNumberPieceMap, isPiecesMovedMap);

            Map<CasePosition, Pieces> piecesLocation = clonedData.getPiecesLocation();
            CasePosition singlePiecePosition = GameUtils.getSinglePiecePosition(Pieces.getKingBySide(playerSide), piecesLocation);


            Side otherPlayerSide = getOtherPlayerSide(playerSide);

            List<CasePosition> positionsThatCanExecuteActionToPosition = getPositionsThatCanMoveOrAttackPosition(
                    singlePiecePosition,
                    otherPlayerSide,
                    clonedData
            );

            return CollectionUtils.isNotEmpty(positionsThatCanExecuteActionToPosition);
        } catch (CloneNotSupportedException e) {
            LOGGER.error("Unable to clone the data!", e);
            return false;
        }
    }

    private <T> EnumMap<CasePosition, T> createNewMapFrom(Map<CasePosition, T> mapToCreateFrom) {
        if (MapUtils.isEmpty(mapToCreateFrom)) {
            return new EnumMap<>(CasePosition.class);
        }

        return new EnumMap<>(mapToCreateFrom);
    }

    private boolean oneOrMorePieceCanBlock(List<CasePosition> piecesThatCanHitOriginalPosition, CasePosition kingPosition, Side playerSide, GameBoardData gameBoardData) {
        if (CollectionUtils.size(piecesThatCanHitOriginalPosition) == 1) { //We can only block one piece, if more, checkmate

            CasePosition enemyPosition = piecesThatCanHitOriginalPosition.get(0);
            Pieces enemyPiece = gameBoardData.getPiece(enemyPosition);

            if (Pieces.isKnight(enemyPiece)) { //We cannot block a knight
                return false;
            }

            for (CasePosition to : MathUtils.getPositionsBetweenTwoPosition(enemyPosition, kingPosition)) { //For each position between the king and the enemy, we try to block it
                Set<CasePosition> casePositions = gameBoardData.getPiecesLocation(playerSide).keySet();
                for (CasePosition from : casePositions) { //Try to find if one of our piece can block the target
                    if (Pieces.isKing(gameBoardData.getPiece(from))) {
                        continue;
                    }
                    MoveStatus moveStatus = moveConstraintDelegate.getMoveStatus(from, to, gameBoardData);
                    boolean moveValid = MoveStatus.isMoveValid(moveStatus);
                    boolean isSpecialMove = MoveStatus.CAN_PROTECT_FRIENDLY.equals(moveStatus) || MoveStatus.KING_ATTACK_KING.equals(moveStatus);
                    if ((moveValid || isSpecialMove) && !isKingCheckAfterMove(from, to, gameBoardData)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isStillCheckAfterKillingTheEnemy(Side playerSide, CasePosition to, GameBoardData gameBoardData) {

        boolean isCheck = true;
        int toRow = to.getRow();
        int toColPos = to.getColPos();

        Pieces enemyPiece = gameBoardData.getPiece(to);

        CasePosition checkTo = null;

        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : gameBoardData.getPiecesLocation(playerSide).entrySet()) {
            CasePosition from = casePositionPiecesEntry.getKey();

            if (Pieces.isKing(casePositionPiecesEntry.getValue())) {
                continue;
            }

            Pieces currentPiece = gameBoardData.getPiece(from);

            int fromColPos = from.getColPos();
            int fromRow = from.getRow();

            boolean isColumnNearEachOther = (Math.abs(fromColPos - toColPos) == 1);

            //This is the only case, where the piece is not directly the end target (En passant).
            if (isEnPassant(playerSide, to, toRow, enemyPiece, currentPiece, fromRow, isColumnNearEachOther)) {

                CasePosition positionByRankAndColumn = getPositionByRankAndColumn(Ranks.SIX, to.getCol(), playerSide);

                if (!PawnMoveConstraint.isEnPassant(to, positionByRankAndColumn, gameBoardData, playerSide)) {
                    continue;
                }

                CasePosition checkFrom = PawnMoveConstraint.getEnPassantPositionFromEnemyPawn(to, Side.getOtherPlayerSide(playerSide));
                isCheck &= isKingCheckAfterMove(checkFrom, to, gameBoardData);
            } else if (isValidMove(to, gameBoardData, from)) {
                isCheck &= isKingCheckAfterMove(from, to, gameBoardData);
            }
        }

        return isCheck;
    }

    private boolean isValidMove(CasePosition to, GameBoardData gameBoardData, CasePosition from) {
        return MoveStatus.isMoveValid(moveConstraintDelegate.getMoveStatus(from, to, gameBoardData)) && !isKingCheckAfterMove(from, to, gameBoardData);
    }

    private boolean isEnPassant(Side playerSide, CasePosition to, int toRow, Pieces enemyPiece, Pieces currentPiece, int fromRow, boolean isColumnNearEachOther) {
        return Pieces.isPawn(currentPiece) &&
                Pieces.isPawn(enemyPiece) &&
                isColumnNearEachOther &&
                fromRow == toRow &&
                !Pieces.isSameSide(enemyPiece, currentPiece) &&
                Ranks.FIVE.equals(Ranks.getRank(to, playerSide));
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

    private boolean isStalemate(Side playerSide, GameBoardData gameBoardData) {
        return CollectionUtils.isEmpty(getPositionKingCanMove(playerSide, gameBoardData));
    }

    private KingStatus getKingStatusWhenPiecesCanHitKing(Side playerSide, CasePosition kingPosition, List<CasePosition> piecesThatCanHitOriginalPosition, GameBoardData gameBoardData) {

        //Try to move the king
        if (CollectionUtils.isNotEmpty(getPositionKingCanMove(playerSide, gameBoardData))) {
            return KingStatus.CHECK;
        }

        if (CollectionUtils.size(piecesThatCanHitOriginalPosition) == 1) {

            CasePosition to = piecesThatCanHitOriginalPosition.get(0);

            //If not able to move, try to kill the enemy piece with an other piece
            if (!isStillCheckAfterKillingTheEnemy(playerSide, to, gameBoardData)) {
                return KingStatus.CHECK;
            }

            //Try to block the path of the enemy with one of the pieces
            if (oneOrMorePieceCanBlock(piecesThatCanHitOriginalPosition, kingPosition, playerSide, gameBoardData)) {
                return KingStatus.CHECK;
            }
        }

        return KingStatus.CHECKMATE;
    }
}
