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

package ca.watier.echechess.engine.utils;

import ca.watier.echechess.common.enums.*;
import ca.watier.echechess.common.pojos.MoveHistory;
import ca.watier.echechess.common.pojos.PieceDataSection;
import ca.watier.echechess.common.pojos.PieceSingleMoveSection;
import ca.watier.echechess.common.utils.Pair;
import ca.watier.echechess.engine.engines.GenericGameHandler;
import ca.watier.echechess.engine.exceptions.*;
import ca.watier.echechess.engine.interfaces.GameConstraintHandler;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ca.watier.echechess.common.enums.Side.BLACK;
import static ca.watier.echechess.common.enums.Side.WHITE;


public class PgnGameExtractor {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PgnGameExtractor.class);
    private static final List<PgnMoveToken> PAWN_PROMOTION_WITH_CAPTURE_TOKENS = List.of(PgnMoveToken.PAWN_PROMOTION, PgnMoveToken.CAPTURE);
    private static final Pattern POSITION_PATTERN = Pattern.compile("[a-h][1-8]");

    private final List<GenericGameHandler> handlerList = new ArrayList<>();
    private final GameConstraintHandler gameConstraintHandler;

    private GenericGameHandler gameHandler;
    private Side currentSide = WHITE;
    private Side otherSide = BLACK;


    public PgnGameExtractor(GameConstraintHandler gameConstraintHandler) {
        this.gameConstraintHandler = gameConstraintHandler;
    }

    public static String[] getRawHeadersAndGames(String rawText) {
        return replaceInvalidCharacters(rawText).split("\n\n");
    }

    private static String replaceInvalidCharacters(String rawText) {
        return rawText.replace("\r\n", "\n");
    }

    private static String getGame(String rawCurrentGame) {
        return rawCurrentGame.substring(2).replace("\n", " ");
    }

    public List<GenericGameHandler> parseMultipleGameWithHeader(String rawText) throws ChessException {
        String[] headersAndGames = getRawHeadersAndGames(rawText);
        int nbOfGames = headersAndGames.length;

        for (int i = 0; i < nbOfGames; i = i + 2) {
            parseGame(headersAndGames[i + 1]);
        }

        return handlerList;
    }

    private void parseGame(String rawCurrentGame) throws ChessException {
        String currentGame = getGame(rawCurrentGame);
        String[] tokens = currentGame.split("\\s+\\d+\\.");

        if (tokens.length == 0) {
            return;
        }

        resetSide();
        gameHandler = new GenericGameHandler(gameConstraintHandler);
        handlerList.add(gameHandler);

        for (String currentToken : tokens) {
            currentToken = currentToken.trim();

            for (String action : currentToken.split(" ")) {
                action = action.trim();

                if (action.isEmpty()) {
                    continue;
                }

                parseAction(action, currentGame);
            }
        }
    }

    private void resetSide() {
        currentSide = WHITE;
        otherSide = BLACK;
    }

    private void parseAction(String action, String currentGame) throws ChessException {
        PgnEndGameToken endGameTokenByAction = PgnEndGameToken.getEndGameTokenByAction(action);
        if (PgnEndGameToken.isGameEnded(endGameTokenByAction)) {
            try {
                validateGameEnding(endGameTokenByAction);
            } catch (ChessException chess) { //We cannot be certain that the engine is false (The player can resign of the game)
                LOGGER.debug("Wrong game ending code found ({}) for the game: {}", endGameTokenByAction, currentGame);
            }

            return;
        }

        List<PgnMoveToken> pieceMovesFromLetter = PgnMoveToken.getPieceMovesFromLetter(action);
        boolean pawnPromotionWithCapture = pieceMovesFromLetter.containsAll(PAWN_PROMOTION_WITH_CAPTURE_TOKENS);

        for (PgnMoveToken pgnMoveToken : pieceMovesFromLetter) {
            switch (pgnMoveToken) {
                case KINGSIDE_CASTLING_CHECK:
                case QUEENSIDE_CASTLING_CHECK:
                    executeCastling(pgnMoveToken);
                    validateCheck();
                    break;
                case KINGSIDE_CASTLING_CHECKMATE:
                case QUEENSIDE_CASTLING_CHECKMATE:
                    executeCastling(pgnMoveToken);
                    validateCheckMate();
                    break;
                case NORMAL_MOVE:
                    executeMove(action);
                    break;
                case CAPTURE:
                    validateCapture();
                    break;
                case CHECK:
                    validateCheck();
                    break;
                case CHECKMATE:
                    validateCheckMate();
                    break;
                case KINGSIDE_CASTLING:
                case QUEENSIDE_CASTLING:
                    executeCastling(pgnMoveToken);
                    break;
                case PAWN_PROMOTION:
                    validatePawnPromotion(pawnPromotionWithCapture, action);
                    break;
            }
        }

        switchSide();
    }

    private void validateGameEnding(PgnEndGameToken ending) throws InvalidGameEndingException {
        switch (ending) {
            case WHITE_WIN:
                if (!(gameHandler.isGameDone() && gameHandler.isCheckMate(BLACK))) {
                    throw new InvalidGameEndingException(WHITE);
                }
                break;
            case BLACK_WIN:
                if (!(gameHandler.isGameDone() && gameHandler.isCheckMate(WHITE))) {
                    throw new InvalidGameEndingException(BLACK);
                }
                break;
            case DRAWN:
                if (!gameHandler.isGameDraw()) {
                    throw new InvalidGameEndingException("The game is supposed to be DRAWN");
                }
                break;
            case STILL_IN_PROGRESS:
            case UNKNOWN:
                throw new InvalidGameEndingException(String.format("The game ending is not known (%s)", ending));
        }
    }

    private void executeCastling(PgnMoveToken pgnMoveToken) throws InvalidCastlingException {
        Map<CasePosition, Pieces> piecesLocation = gameHandler.getPiecesLocation(currentSide);
        CasePosition kingPosition = null;
        for (Map.Entry<CasePosition, Pieces> casePositionPiecesEntry : piecesLocation.entrySet()) {
            CasePosition key = casePositionPiecesEntry.getKey();
            Pieces value = casePositionPiecesEntry.getValue();

            if (Pieces.isKing(value)) {
                kingPosition = key;
            }
        }

        CasePosition selectedRookPosition = PgnMoveToken.getCastlingRookPosition(pgnMoveToken, currentSide);

        if (MoveType.CASTLING.equals(gameHandler.movePiece(kingPosition, selectedRookPosition, currentSide))) {
            LOGGER.debug("Castling: King -> {} | Rook {} | ({})", kingPosition, selectedRookPosition, currentSide);
        } else { //Issue with the move / case
            throw new InvalidCastlingException(String.format("Unable to cast at the selected position %s for the current color %s !", selectedRookPosition, currentSide));
        }
    }

    private void validateCheck() throws InvalidCheckException {
        if (!gameHandler.isCheck(otherSide)) {
            throw new InvalidCheckException("The other player king is not check!");
        } else {
            LOGGER.debug("{} is CHECK", otherSide);
        }
    }

    private void validateCheckMate() throws InvalidCheckMateException {
        if (!gameHandler.isCheckMate(otherSide)) {
            throw new InvalidCheckMateException("The other player king is not checkmate!");
        } else {
            LOGGER.debug("{} is CHECKMATE", otherSide);
        }
    }

    private void executeMove(String action) throws ChessException {
        List<String> casePositions = getPositionsFromAction(action);
        List<PgnMoveToken> pieceMovesFromLetter = PgnMoveToken.getPieceMovesFromLetter(action);

        CasePosition to;

        switch (casePositions.size()) {
            case 1:
                to = CasePosition.valueOf(casePositions.get(0).toUpperCase());
                break;
            case 2:
                to = CasePosition.valueOf(casePositions.get(1).toUpperCase());
                break;
            default:
                throw new IllegalStateException("More than two coordinates detected!");
        }

        PgnPieceFound pgnPieceFound = isPawnPromotion(action, pieceMovesFromLetter);
        List<Pair<CasePosition, Pieces>> piecesThatCanHitPosition = gameHandler.getAllPiecesThatCanMoveTo(to, currentSide);
        List<CasePosition> similarPieceThatHitTarget = getSimilarPiecesPositionThatCanHitSameTarget(piecesThatCanHitPosition, pgnPieceFound.getPieceBySide(currentSide));

        CasePosition from = (!similarPieceThatHitTarget.isEmpty() ?
                getFromPositionWhenMultipleTargetCanHit(action, casePositions, similarPieceThatHitTarget) :
                getPositionWhenOneTargetCanHit(piecesThatCanHitPosition, pgnPieceFound));

        LOGGER.debug("MOVE {} to {} ({}) | action -> {}", from, to, currentSide, action);
        MoveType moveType = gameHandler.movePiece(from, to, currentSide);

        if (MoveType.PAWN_PROMOTION.equals(moveType)) {
            PgnPieceFound pieceFromAction = PgnPieceFound.getPieceFromAction(action);
            Pieces pieceBySide = pieceFromAction.getPieceBySide(currentSide);
            gameHandler.upgradePiece(to, pieceBySide, currentSide);
        } else if (!(MoveType.NORMAL_MOVE.equals(moveType) || MoveType.CAPTURE.equals(moveType) || MoveType.EN_PASSANT.equals(moveType) || MoveType.PAWN_HOP.equals(moveType))) {  //Issue with the move / case
            throw new InvalidMoveException(String.format("Unable to move at the selected position %s for the current color %s ! (%s)", to, currentSide, action));
        }
    }

    private void validateCapture() throws InvalidCaptureException {
        List<MoveHistory> moveHistory = gameHandler.getMoveHistory();
        MoveHistory lastMoveHistory = moveHistory.get(moveHistory.size() - 1);
        MoveType moveType = lastMoveHistory.getMoveType();

        if (!(MoveType.CAPTURE.equals(moveType) || MoveType.EN_PASSANT.equals(moveType))) {
            throw new InvalidCaptureException("The capture is not in the history!");
        }
    }

    private void validatePawnPromotion(boolean pawnPromotionWithCapture, String action) throws InvalidPawnPromotionException {
        List<MoveHistory> moveHistory = gameHandler.getMoveHistory();

        //In case of a capture and a pawn promotion in the same turn, the history index of the promotion is before the capture
        MoveHistory lastMoveHistory =
                pawnPromotionWithCapture ?
                        moveHistory.get(moveHistory.size() - 2) :
                        moveHistory.get(moveHistory.size() - 1);

        if (!MoveType.PAWN_PROMOTION.equals(lastMoveHistory.getMoveType())) {
            throw new InvalidPawnPromotionException("The pawn promotion is not in the history!");
        } else {
            LOGGER.debug("PAWN PROMOTION {} to {} ({}) | action -> {}", lastMoveHistory.getFrom(), lastMoveHistory.getTo(), currentSide, action);
        }
    }

    private void switchSide() {
        if (BLACK.equals(currentSide)) {
            currentSide = WHITE;
            otherSide = BLACK;
        } else {
            currentSide = BLACK;
            otherSide = WHITE;
        }
    }

    private List<String> getPositionsFromAction(String action) {
        Matcher m = POSITION_PATTERN.matcher(action);
        List<String> casePositions = new ArrayList<>();

        while (m.find()) {
            String currentPosition = m.group();
            casePositions.add(currentPosition);
        }
        return casePositions;
    }

    private PgnPieceFound isPawnPromotion(String action, List<PgnMoveToken> pieceMovesFromLetter) {
        return pieceMovesFromLetter.contains(PgnMoveToken.PAWN_PROMOTION) ? PgnPieceFound.PAWN : PgnPieceFound.getPieceFromAction(action);
    }

    /**
     * Return a list of position when there's more than one target, with the same type, that can hit the same position.
     *
     * @param piecesThatCanHitPosition
     * @param wantedType
     * @return
     */
    private List<CasePosition> getSimilarPiecesPositionThatCanHitSameTarget(List<Pair<CasePosition, Pieces>> piecesThatCanHitPosition, Pieces wantedType) {

        if (piecesThatCanHitPosition == null || piecesThatCanHitPosition.isEmpty() || piecesThatCanHitPosition.size() < 2 || wantedType == null) {
            return new ArrayList<>();
        }

        Predicate<Pair<CasePosition, Pieces>> similarPiecePredicate = p -> wantedType.equals(p.getSecondValue());

        List<CasePosition> similarPieces =
                piecesThatCanHitPosition.
                        stream().
                        filter(similarPiecePredicate).
                        map(Pair::getFirstValue).collect(Collectors.toList());

        return similarPieces.size() > 1 ? similarPieces : new ArrayList<>();
    }

    private CasePosition getFromPositionWhenMultipleTargetCanHit(String action, List<String> casePositions, List<CasePosition> similarPieceThatHitTarget) throws ChessException {
        CasePosition value;
        int length = casePositions.size();

        if (length == 2) {
            return CasePosition.valueOf(casePositions.get(0).toUpperCase()); //Contain from (0) and to (1) positions
        } else if (length > 2) {
            throw new IllegalStateException("Invalid type of positioning");
        }

        if (action.contains(PgnMoveToken.CAPTURE.getChars().get(0)) ||
                action.contains(PgnMoveToken.PAWN_PROMOTION.getChars().get(0))) {

            String before = getParsedBefore(action);

            switch (before.length()) {
                case 1: //Row or column
                    value = getPositionWhenRowOrCol(similarPieceThatHitTarget, before);
                    break;
                case 2: //Full position
                    value = getPositionWhenFullCoordinate(CasePosition.valueOf(before.toUpperCase()), similarPieceThatHitTarget);
                    break;
                default:
                    throw new IllegalStateException("Invalid number of characters!");
            }
        } else { //Normal move
            value = getPositionWhenNormalMove(action, similarPieceThatHitTarget);
        }

        return value;
    }

    private CasePosition getPositionWhenOneTargetCanHit(List<Pair<CasePosition, Pieces>> piecesThatCanHitPosition, PgnPieceFound pgnPieceFound) {
        CasePosition value = null;

        for (Pair<CasePosition, Pieces> casePositionPiecesPair : piecesThatCanHitPosition) {
            CasePosition casePosition = casePositionPiecesPair.getFirstValue();
            Pieces pieces = casePositionPiecesPair.getSecondValue();

            if ((Pieces.isPawn(pieces) && PgnPieceFound.PAWN.equals(pgnPieceFound)) ||
                    (Pieces.isBishop(pieces) && PgnPieceFound.BISHOP.equals(pgnPieceFound)) ||
                    (Pieces.isKing(pieces) && PgnPieceFound.KING.equals(pgnPieceFound)) ||
                    (Pieces.isKnight(pieces) && PgnPieceFound.KNIGHT.equals(pgnPieceFound)) ||
                    (Pieces.isQueen(pieces) && PgnPieceFound.QUEEN.equals(pgnPieceFound)) ||
                    (Pieces.isRook(pieces) && PgnPieceFound.ROOK.equals(pgnPieceFound))) {
                value = casePosition;

                break;
            }
        }

        return value;
    }

    private String getParsedBefore(String action) {
        //The position from is in the first action
        PieceDataSection parsedActions = PieceDataSection.getParsedActions(action).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to find an action!"));

        String before = parsedActions.getBefore();
        PgnPieceFound pieceFromAction = PgnPieceFound.getPieceFromAction(before);

        //Remove the piece, if not a pawn
        return !PgnPieceFound.PAWN.equals(pieceFromAction) ? before.substring(1) : before;
    }

    private CasePosition getPositionWhenRowOrCol(List<CasePosition> similarPieceThatHitTarget, String before) {
        Character column = null;
        Byte row = null;
        char tmp = before.charAt(0);

        if (Character.isLetter(tmp)) {
            column = tmp;
        } else if (Character.isDigit(tmp)) {
            row = (byte) Character.getNumericValue(tmp);
        } else {
            throw new IllegalStateException("Invalid type of Character!");
        }

        return getCasePositionWhenRowOrCol(similarPieceThatHitTarget, row, column);
    }

    private CasePosition getPositionWhenFullCoordinate(CasePosition fromFullCoordinate, List<CasePosition> similarPieceThatHitTarget) {
        throw new IllegalStateException("Not Implemented");
    }

    private CasePosition getPositionWhenNormalMove(String action, List<CasePosition> similarPieceThatHitTarget) throws InvalidMoveException {
        PieceSingleMoveSection parsedActions = PieceSingleMoveSection.getParsedActions(action);
        Byte row = parsedActions.getRow();
        Character column = parsedActions.getColumn();
        boolean isRow = row != null && column == null;
        boolean isColumn = row == null && column != null;

        if (parsedActions.isFromPositionFullCoordinate()) { //Full
            return getPositionWhenFullCoordinate(parsedActions.getFromFullCoordinate(), similarPieceThatHitTarget);
        } else if (isRow || isColumn) { //Row
            return getCasePositionWhenRowOrCol(similarPieceThatHitTarget, row, column);
        } else {
            throw new InvalidMoveException("The position is now known!");
        }
    }

    private CasePosition getCasePositionWhenRowOrCol(List<CasePosition> similarPieceThatHitTarget, Byte row, Character column) {
        CasePosition value = null;
        boolean isRow = row != null && column == null;
        boolean isColumn = row == null && column != null;

        for (CasePosition position : similarPieceThatHitTarget) {
            if ((isColumn && position.isOnSameColumn(column)) || (isRow && position.isOnSameRow(Character.forDigit(row, 10)))) {
                value = position;
                break;
            }
        }

        return value;
    }

    public GenericGameHandler parseSingleGameWithoutHeader(String rawText) throws ChessException {
        parseGame(replaceInvalidCharacters(rawText));
        return handlerList.get(0);
    }
}