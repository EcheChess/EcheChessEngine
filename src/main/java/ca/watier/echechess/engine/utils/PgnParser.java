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

import ca.watier.echechess.engine.engines.GenericGameHandler;
import ca.watier.echechess.engine.exceptions.*;
import ca.watier.echechess.engine.game.GameConstraints;
import ca.watier.echesscommon.enums.*;
import ca.watier.echesscommon.interfaces.WebSocketService;
import ca.watier.echesscommon.pojos.MoveHistory;
import ca.watier.echesscommon.pojos.PieceDataSection;
import ca.watier.echesscommon.pojos.PieceSingleMoveSection;
import ca.watier.echesscommon.utils.MultiArrayMap;
import ca.watier.echesscommon.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ca.watier.echesscommon.enums.Side.BLACK;
import static ca.watier.echesscommon.enums.Side.WHITE;


public class PgnParser {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PgnParser.class);
    private static final List<PgnMoveToken> PAWN_PROMOTION_WITH_CAPTURE_TOKENS = new ArrayList<>();
    private static final Pattern POSITION_PATTERN = Pattern.compile("[a-h][1-8]");

    static {
        PAWN_PROMOTION_WITH_CAPTURE_TOKENS.add(PgnMoveToken.PAWN_PROMOTION);
        PAWN_PROMOTION_WITH_CAPTURE_TOKENS.add(PgnMoveToken.CAPTURE);
    }

    private final List<GenericGameHandler> handlerList = new ArrayList<>();
    private final GameConstraints gameConstraints;
    private final WebSocketService webSocketService;

    private GenericGameHandler gameHandler;
    private Side currentSide = WHITE;
    private Side otherSide = BLACK;


    public PgnParser(@NotNull GameConstraints gameConstraints, @NotNull WebSocketService webSocketService) {
        this.gameConstraints = gameConstraints;
        this.webSocketService = webSocketService;
    }

    @NotNull
    public static List<Pair<String, String>> getMappedHeadersAndGames(@NotNull String rawText) {
        List<Pair<String, String>> values = new ArrayList<>();
        String[] rawValues = getRawHeadersAndGames(rawText);

        for (int i = 0; i < rawValues.length; i = i + 2) {
            String header = rawValues[i];
            String game = rawValues[i + 1];
            values.add(new Pair<>(header, game));
        }

        return values;
    }

    @NotNull
    public static String[] getRawHeadersAndGames(@NotNull String rawText) {
        return rawText.replace("\r\n", "\n").split("\n\n");
    }

    public List<GenericGameHandler> parse(@NotNull String rawText) throws ChessException {
        String[] headersAndGames = getRawHeadersAndGames(rawText);
        int nbOfGames = headersAndGames.length;
        int currentIdx = 1;

        for (int i = 0; i < nbOfGames; i = i + 2) {
            String rawCurrentGame = headersAndGames[i + 1];
            String currentGame = getGame(rawCurrentGame);

            LOGGER.debug("=================================================");
            LOGGER.debug("***{}***", currentIdx);
            LOGGER.debug(currentGame);
            LOGGER.debug("=================================================");

            currentIdx++;

            String[] tokens = currentGame.split("\\s+\\d+\\.");

            if (tokens.length == 0) {
                continue;
            }

            resetSide();
            gameHandler = new GenericGameHandler(gameConstraints, webSocketService);
            handlerList.add(gameHandler);

            for (String currentToken : tokens) {
                currentToken = currentToken.trim();

                String[] actions = currentToken.split(" ");

                for (String action : actions) {
                    action = action.trim();

                    if (action.isEmpty()) {
                        continue;
                    }

                    parseAction(action, currentGame);
                }
            }
        }

        return handlerList;
    }

    @NotNull
    public static String getGame(String rawCurrentGame) {
        return rawCurrentGame.substring(2, rawCurrentGame.length()).replace("\n", " ");
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

    private void validateGameEnding(@NotNull PgnEndGameToken ending) throws InvalidGameEndingException {
        switch (ending) {
            case WHITE_WIN:
                if (!(gameHandler.isGameDone() && KingStatus.CHECKMATE.equals(gameHandler.getKingStatus(BLACK, false)))) {
                    throw new InvalidGameEndingException(WHITE);
                }
                break;
            case BLACK_WIN:
                if (!(gameHandler.isGameDone() && KingStatus.CHECKMATE.equals(gameHandler.getKingStatus(WHITE, false)))) {
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
        if (!KingStatus.CHECK.equals(gameHandler.getKingStatus(otherSide, false))) {
            throw new InvalidCheckException("The other player king is not check!");
        } else {
            LOGGER.debug("{} is CHECK", otherSide);
        }
    }

    private void validateCheckMate() throws InvalidCheckMateException {
        if (!KingStatus.CHECKMATE.equals(gameHandler.getKingStatus(otherSide, false))) {
            throw new InvalidCheckMateException("The other player king is not checkmate!");
        } else {
            LOGGER.debug("{} is CHECKMATE", otherSide);
        }
    }

    private void executeMove(@NotNull String action) throws ChessException {
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
        List<Pieces> validPiecesFromAction = pgnPieceFound.getPieces();
        List<Pair<CasePosition, Pieces>> piecesThatCanHitPosition = gameHandler.getAllPiecesThatCanMoveTo(to, currentSide);
        MultiArrayMap<Pieces, Pair<CasePosition, Pieces>> similarPieceThatHitTarget = getSimilarPiecesThatCanHitSameTarget(piecesThatCanHitPosition, validPiecesFromAction, to, gameHandler);

        CasePosition from = (!similarPieceThatHitTarget.isEmpty() ?
                getPositionWhenMultipleTargetCanHit(action, casePositions, similarPieceThatHitTarget) :
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

    private @NotNull List<String> getPositionsFromAction(@NotNull String action) {
        Matcher m = POSITION_PATTERN.matcher(action);
        List<String> casePositions = new ArrayList<>();

        while (m.find()) {
            String currentPosition = m.group();
            casePositions.add(currentPosition);
        }
        return casePositions;
    }

    private PgnPieceFound isPawnPromotion(@NotNull String action, List<PgnMoveToken> pieceMovesFromLetter) {
        return pieceMovesFromLetter.contains(PgnMoveToken.PAWN_PROMOTION) ? PgnPieceFound.PAWN : PgnPieceFound.getPieceFromAction(action);
    }

    private MultiArrayMap<Pieces, Pair<CasePosition, Pieces>> getSimilarPiecesThatCanHitSameTarget(List<Pair<CasePosition, Pieces>> piecesThatCanHitPosition, List<Pieces> validPiecesFromAction, CasePosition to, GenericGameHandler gameHandler) {
        MultiArrayMap<Pieces, Pair<CasePosition, Pieces>> similarPieceThatHitTarget = new MultiArrayMap<>();

        //Group all similar pieces that can hit the target
        for (int i = 0; i < piecesThatCanHitPosition.size(); i++) {
            Pair<CasePosition, Pieces> firstLayer = piecesThatCanHitPosition.get(i);
            CasePosition firstPosition = firstLayer.getFirstValue();
            Pieces firstPiece = firstLayer.getSecondValue();

            if (!validPiecesFromAction.contains(firstPiece) || gameHandler.isKingCheckAfterMove(firstPosition, to, firstPiece.getSide())) {
                continue;
            }

            boolean isOtherFound = false;

            for (int j = (i + 1); j < piecesThatCanHitPosition.size(); j++) {
                Pair<CasePosition, Pieces> secondLayer = piecesThatCanHitPosition.get(j);
                CasePosition secondPosition = secondLayer.getFirstValue();
                Pieces secondPiece = secondLayer.getSecondValue();

                if (firstPiece.equals(secondPiece) && !gameHandler.isKingCheckAfterMove(secondPosition, to, secondPiece.getSide())) {
                    similarPieceThatHitTarget.put(secondPiece, new Pair<>(secondPosition, secondPiece));
                    isOtherFound = true;
                }
            }

            if (isOtherFound) {
                similarPieceThatHitTarget.put(firstPiece, new Pair<>(firstPosition, firstPiece));
            }
        }


        return similarPieceThatHitTarget;
    }

    private CasePosition getPositionWhenMultipleTargetCanHit(@NotNull String action, List<String> casePositions, MultiArrayMap<Pieces, Pair<CasePosition, Pieces>> similarPieceThatHitTarget) throws ChessException {
        CasePosition value;
        int length = casePositions.size();
        Byte row = null;
        Character column = null;
        boolean isRow;
        boolean isColumn;
        PgnPieceFound pieceFromAction;

        if (length == 2) {
            return CasePosition.valueOf(casePositions.get(0).toUpperCase()); //Contain from (0) and to (1) positions
        } else if (length > 2) {
            throw new IllegalStateException("Invalid type of positioning");
        }

        //Actions
        if (action.contains(PgnMoveToken.CAPTURE.getChars().get(0)) ||
                action.contains(PgnMoveToken.PAWN_PROMOTION.getChars().get(0))) {

            //The position from is in the first action
            PieceDataSection parsedActions = PieceDataSection.getParsedActions(action).stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("Unable to find an action!"));

            String before = parsedActions.getBefore();

            pieceFromAction = PgnPieceFound.getPieceFromAction(before);

            if (!PgnPieceFound.PAWN.equals(pieceFromAction)) {
                before = before.substring(1); //Remove the piece, if not a pawn
            }

            switch (before.length()) {
                case 1: //Row or column
                    char tmp = before.charAt(0);

                    if (Character.isLetter(tmp)) {
                        column = tmp;
                    } else if (Character.isDigit(tmp)) {
                        row = (byte) Character.getNumericValue(tmp);
                    } else {
                        throw new IllegalStateException("Invalid type of Character!");
                    }

                    pieceFromAction = PgnPieceFound.getPieceFromAction(action.substring(0, 1));

                    value = getCasePositionWhenRowOrCol(similarPieceThatHitTarget, row, column, pieceFromAction);
                    break;
                case 2: //Full position
                    value = getCasePositionWhenFullCoordinate(CasePosition.valueOf(before.toUpperCase()), similarPieceThatHitTarget);
                    break;
                default:
                    throw new IllegalStateException("Invalid number of characters!");
            }
        } else { //Normal move
            PieceSingleMoveSection parsedActions = PieceSingleMoveSection.getParsedActions(action);
            row = parsedActions.getRow();
            column = parsedActions.getColumn();
            isRow = row != null && column == null;
            isColumn = row == null && column != null;

            if (parsedActions.isFromPositionFullCoordinate()) { //Full
                value = getCasePositionWhenFullCoordinate(parsedActions.getFromFullCoordinate(), similarPieceThatHitTarget);
            } else if (isRow || isColumn) { //Row
                value = getCasePositionWhenRowOrCol(similarPieceThatHitTarget, row, column, parsedActions.getPgnPieceFound());
            } else {
                throw new InvalidMoveException("The position is now known!");
            }
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

    private CasePosition getCasePositionWhenRowOrCol(MultiArrayMap<Pieces, Pair<CasePosition, Pieces>> similarPieceThatHitTarget, Byte row, Character column, PgnPieceFound pgnPieceFound) {
        CasePosition value = null;
        boolean isRow = row != null && column == null;
        boolean isColumn = row == null && column != null;

        mainLoop:
        for (Map.Entry<Pieces, List<Pair<CasePosition, Pieces>>> entry : similarPieceThatHitTarget.entrySet()) {
            Pieces key = entry.getKey();

            if (!pgnPieceFound.getPieces().contains(key)) {
                continue;
            }

            for (Pair<CasePosition, Pieces> casePositionPiecesPair : entry.getValue()) {
                CasePosition firstValue = casePositionPiecesPair.getFirstValue();

                if ((isColumn && firstValue.isOnSameColumn(column)) || (isRow && firstValue.isOnSameRow(Character.forDigit(row, 10)))) {
                    value = firstValue;
                    break mainLoop;
                }
            }
        }
        return value;
    }

    private CasePosition getCasePositionWhenFullCoordinate(CasePosition fromFullCoordinate, MultiArrayMap<Pieces, Pair<CasePosition, Pieces>> similarPieceThatHitTarget) {
        throw new IllegalStateException("Not Implemented");
    }
}